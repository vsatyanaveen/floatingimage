package dk.nindroid.rss;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Vector;

import dk.nindroid.rss.data.ImageReference;

public class TextureBank {
	Queue<ImageReference> 					unseen   = new LinkedList<ImageReference>();
	Vector<String> 							streams  = new Vector<String>();
	Queue<ImageReference> 					cached   = new LinkedList<ImageReference>();
	ImageCache 								ic;
	BitmapDownloader						bitmapDownloader; 
	private Map<String, ImageReference> 	mActiveBitmaps = new HashMap<String, ImageReference>();
	int 									textureCache;
	boolean 								stopThreads = false;
	
	public TextureBank(int textureCache){
		this.textureCache = textureCache;
	}
	
	public void setFeeders(BitmapDownloader bitmapDownloader, ImageCache ic){
		this.bitmapDownloader = bitmapDownloader;
		this.ic = ic;
	}
	
	public void addNewBitmap(ImageReference ir){
		if(ir != null){
			synchronized (unseen) {
				unseen.add(ir);
			}
			ic.saveImage(ir);
		}
	}
	public void addOldBitmap(ImageReference ir){
		if(ir != null){
			synchronized (cached) {
				cached.add(ir);
			}
		}
	}
	
	public boolean doDownload(String url){
		return !ic.exists(url);
	}
	
	public void addFromCache(String name){
		ic.addImage(name);
	}
	
	public void addStream(String stream){
		streams.add(stream);
		synchronized (this) {
			this.notify();
		}
	}
	
	public boolean isShowing(String id){
		return mActiveBitmaps.containsKey(id);
	}
		
	public ImageReference getTexture(ImageReference previousImage){
		ImageReference ir = getUnseen();
		if(ir != null){
			// Remove previous image, if any
			if(previousImage != null){
				mActiveBitmaps.remove(previousImage.getID());
				if(previousImage.isInvalidated()){
					ic.updateMeta(previousImage);
					previousImage.validate();
				}
			}
			mActiveBitmaps.put(ir.getID(), ir);
			return ir;
		}
		return null;
	}
	private ImageReference getUnseen(){
		ImageReference ir = null;
		synchronized (unseen) {
			int attempts = 0;
			do{
				if(ir != null){
					ir.getBitmap().recycle();
				}
				
				if(++attempts == 3){
					ir = null;
					break;
				}
				
				ir = unseen.poll();
			}while(ir != null && isShowing(ir.getID()));
			unseen.notify();
		}
		return ir;
	}
	public void reset(){
		this.cached.clear();
		this.unseen.clear();
		this.mActiveBitmaps.clear();
	}
	public void stop(){
		stopThreads = true;
		synchronized(unseen){
			unseen.notifyAll();
		}
		synchronized(cached){
			cached.notifyAll();
		}
		ic.cleanCache();
	}
	public void startExternal(){
		new Thread(bitmapDownloader).start();
		//new Thread(ic).start();
	}
	
	public void start(){
		stopThreads = false;
		startExternal();
	}
}
