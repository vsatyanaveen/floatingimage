package dk.nindroid.rss;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Vector;

import android.util.Log;
import dk.nindroid.rss.data.ImageReference;
import dk.nindroid.rss.data.LocalImage;
import dk.nindroid.rss.settings.Settings;

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
			// Don't cache local images.
			if(Settings.useCache && !(ir instanceof LocalImage)){
				ic.saveExploreImage(ir);
			}
		}
	}
	public void addOldBitmap(ImageReference ir){
		if(ir != null){
			synchronized (cached) {
				cached.add(ir);
				Log.v("Texture bank", cached.size() + " old images.");
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
		
	public ImageReference getTexture(ImageReference previousImage){
		ImageReference ir = getUnseen();
		
		if(ir != null){
			// Remove previous image, if any
			if(previousImage != null){
				mActiveBitmaps.remove(previousImage.getID());
			}
			mActiveBitmaps.put(ir.getID(), ir);
			return ir;
		}
		// If no new pictures, try some old ones.
		// Do not show an image that's already being shown
		if(Settings.useCache){
			ir = getCached();
			
			if(previousImage != null){
				mActiveBitmaps.remove(previousImage.getID());
			}
			if(ir != null){
				mActiveBitmaps.put(ir.getID(), ir);
			}
			return ir;
		}else{
			return null;
		}
	}
	private ImageReference getUnseen(){
		ImageReference ir = null;
		synchronized (unseen) {
			int attempts = 0;
			do{
				if(ir != null && !ir.getBitmap().isRecycled()){
					ir.getBitmap().recycle();
				}
				
				if(++attempts == 3){
					ir = null;
					break;
				}
				
				ir = unseen.poll();
				// How the hell do those bitmaps get recycled??!??!
			}while(ir != null && (mActiveBitmaps.containsKey(ir.getID()) || ir.getBitmap().isRecycled()));
			unseen.notify();
		}
		return ir;
	}
	private ImageReference getCached(){
		ImageReference ir = null;
		synchronized (cached) {
			int attempts = 0;
			do{
				if(ir != null){
					ir.getBitmap().recycle();
				}
				
				if(++attempts == 3){
					ir = null;
					break;
				}
				
				ir = cached.poll();
			}while(ir != null && mActiveBitmaps.containsKey(ir.getID()));
			cached.notify();
		}
		return ir;
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
		new Thread(ic).start();
	}
	
	public void start(){
		stopThreads = false;
		startExternal();
	}
}
