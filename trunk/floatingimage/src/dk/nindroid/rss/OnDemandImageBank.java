package dk.nindroid.rss;

import java.io.IOException;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import dk.nindroid.rss.compatibility.Exif;
import dk.nindroid.rss.data.ImageReference;
import dk.nindroid.rss.data.LocalImage;
import dk.nindroid.rss.data.Progress;

public class OnDemandImageBank {
	static boolean 	exifAvailable = false;
	
	// Do we have access to the Exif class?
	static{
		try{
			Log.v("Floating Image", "Checking if Exif tool is available...");
			Exif.checkAvailable();
			exifAvailable = true;
			Log.v("Floating Image", "Enabling Exif reading!");
		}catch(Throwable t){
			exifAvailable = false;
			Log.v("Floating Image", "Exif tool is not available");
		}
	}
	
	final FeedController mFeedController;
	final Loader mLoader;
	final ImageCache mImageCache;
	
	public OnDemandImageBank(FeedController feedController, MainActivity activity, ImageCache imageCache) {
		this.mFeedController = feedController;
		this.mLoader = new Loader(activity.getSettings().highResThumbs);
		new Thread(mLoader).start();
		this.mImageCache = imageCache;
	}
	
	public void get(LoaderClient callback, boolean isNext){
		ImageReference ir = mFeedController.getImageReference(isNext);
		if(ir == null) return;
		
		callback.setEmptyImage(ir);
		loadBitmap(callback, ir);
	}
	
	public void get(ImageReference ir, LoaderClient callback){
		Log.v("Floating Image", "Reget image");
		loadBitmap(callback, ir);
	}
	
	private void loadBitmap(LoaderClient callback, ImageReference ir){
		Message msg = new Message();
		msg.obj = new LoaderBundle(ir, callback);
		mLoader.sendMessage(msg);
	}
	
	class LoaderBundle{
		public ImageReference ir;
		public LoaderClient lc;
		public LoaderBundle(ImageReference ir, LoaderClient lc){
			this.ir = ir;
			this.lc = lc;
		}
	}
	
	public interface LoaderClient{
		public boolean isVisible(String id);
		public boolean bitmapLoaded(String id);
		public Progress getProgressIndicator();
		public void setEmptyImage(ImageReference ir);		
	}
	
	private class Loader implements Runnable{
		LoaderHandler mHandler; 
		final boolean hires;
		
		public Loader(boolean hires){
			this.hires = hires;
		}
		
		public void sendMessage(Message msg){
			mHandler.sendMessage(msg);
		}
		
		@Override
		public void run() {
			Looper.prepare();
			mHandler = new LoaderHandler(hires);
			Looper.loop();
		}
		
		class LoaderHandler extends Handler{
			boolean hires;
			public LoaderHandler(boolean hires){
				this.hires = hires;
			}
			
			Bitmap loadLocal(LocalImage li, Progress progress){
				Bitmap bmp = ImageFileReader.readImage(li.getFile(), hires ? 256 : 128, progress);
				if(bmp == null){
					return null;
				}
				if(exifAvailable){
					try {
						Exif exif = new Exif(li.getFile().getAbsolutePath());
						int rotation = exif.getAttributeInt(Exif.TAG_ORIENTATION, -1);
						switch(rotation){
						case Exif.ORIENTATION_NORMAL:
						case -1:
							break;
						case Exif.ORIENTATION_ROTATE_90:
							li.setRotation(270);
							break;
						case Exif.ORIENTATION_ROTATE_180:
							li.setRotation(180);
							break;
						case Exif.ORIENTATION_ROTATE_270:
							li.setRotation(90);
							break;
						}
					} catch (IOException e) {
						Log.w("Floating Image", "Error reading exif info for file", e);
					} catch (Throwable t){
						exifAvailable = false; // Some devices sort of know ExifInterface...
						Log.w("Floating Image", "Disabling Exif Interface, the device lied!");
					}
				}
				return bmp;
			}
			
			public Bitmap loadFromWeb(ImageReference ir, Progress progress){
				Bitmap bmp = BitmapDownloader.downloadImage(hires ? ir.get256ImageUrl() : ir.get128ImageUrl(), progress);
				if(bmp == null){
					return null;
				}
				if(hires){
					int max = Math.max(bmp.getHeight(), bmp.getWidth());
					if(max > 256){
						float scale = (float)256 / max;
						Bitmap tmp = Bitmap.createScaledBitmap(bmp, (int)(bmp.getWidth() * scale), (int)(bmp.getHeight() * scale), true);
						bmp.recycle();
						bmp = tmp;
					}
				}else{
					int max = Math.max(bmp.getHeight(), bmp.getWidth());
					if(max > 128){
						float scale = (float)128 / max;
						Bitmap tmp = Bitmap.createScaledBitmap(bmp, (int)(bmp.getWidth() * scale), (int)(bmp.getHeight() * scale), true);
						bmp.recycle();
						bmp = tmp;
					}
				}
				ir.getExtended();
				return bmp;
			}
			
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				LoaderBundle bundle = (LoaderBundle)msg.obj;
				ImageReference ir = bundle.ir;
				LoaderClient callback = bundle.lc;
				if(ir == null) return;
				if(!callback.isVisible(ir.getID())){
					return;
				}
				if(mImageCache.exists(ir.getID(), hires ? 256 : 128)){
					mImageCache.getImage(ir, hires ? 256 : 128);
				}
				if(ir.getBitmap() != null){
					if(!callback.bitmapLoaded(ir.getID())){
						ir.recycleBitmap();
					}
					return;
				}
				
				Bitmap bmp;
				if(ir instanceof LocalImage){
					bmp = loadLocal((LocalImage)ir, callback.getProgressIndicator());
				}else{
					bmp = loadFromWeb(ir, callback.getProgressIndicator());
				}
				if(bmp == null) return;
				if(callback.isVisible(ir.getID())){
					if(hires){
						ir.set256Bitmap(bmp);
					}else{
						ir.set128Bitmap(bmp);
					}
					if(ir.getBitmap() != null){
						mImageCache.saveImage(ir);
						if(!callback.bitmapLoaded(ir.getID())){
							ir.recycleBitmap();
						}
						
					}
				}
			}
		}
	}
}
