package dk.nindroid.rss;

import java.util.HashMap;
import java.util.Map;

import dk.nindroid.rss.data.CircularList;
import dk.nindroid.rss.data.ImageReference;

public class TextureBank {
	//Queue<ImageReference> 				unseen   = new LinkedList<ImageReference>();
	CircularList<ImageReference>			images;
	//Vector<String> 							streams  = new Vector<String>();		
	ImageCache 								ic;
	BitmapDownloader						bitmapDownloader; 
	private Map<String, ImageReference> 	mActiveBitmaps = new HashMap<String, ImageReference>();
	boolean 								stopThreads = false;
	boolean 								running = false;
	
	public void initCache(int cacheSize, int activeImages){
		if(this.images == null){
			this.images = new CircularList<ImageReference>(cacheSize, activeImages);
		}
	}
	
	public void setFeeders(BitmapDownloader bitmapDownloader, ImageCache ic){
		this.bitmapDownloader = bitmapDownloader;
		this.ic = ic;
	}
	
	public void addBitmap(ImageReference ir, boolean doCache, boolean next){
		if(ir != null && ir.getBitmap() != null){
			if(doCache){
				ic.saveImage(ir);
			}
			synchronized (images) {
				if(next){
					images.addNext(ir);
				}else{
					images.addPrev(ir);
				}
			}
		}
	}
		
	public boolean doDownload(String url){
		return !ic.exists(url);
	}
	
	public void addFromCache(ImageReference ir, boolean next){
		ic.addImage(ir, next);
	}
		
	public ImageReference getTexture(ImageReference previousImage, boolean next){
		ImageReference ir = get(next);
		if(ir != null && ir.getBitmap() != null){ // What? The bitmap should NEVER be null!
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
	private ImageReference get(boolean next){
		ImageReference ir = null;
		synchronized (images) {	
			do{
				ir = next ? images.next() : images.prev();
				if(ir == null || ir.getBitmap() == null) break;
			}while(mActiveBitmaps.containsKey(ir.getID()));
			images.notifyAll();
		}
		return ir;
	}
	public void reset(){
		this.images.clear();
		this.mActiveBitmaps.clear();
	}
	
	public void stop(){
		running = false;
		stopThreads = true;
		synchronized(images){
			images.notifyAll();
		}
		ic.cleanCache();
	}
	public void startExternal(){
		new Thread(bitmapDownloader).start();
		//new Thread(ic).start();
	}
	
	public void start(){
		if(!running){
			running = true;
			stopThreads = false;
			startExternal();
		}
	}
}
