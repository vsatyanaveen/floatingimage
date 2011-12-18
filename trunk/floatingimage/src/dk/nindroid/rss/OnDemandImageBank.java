package dk.nindroid.rss;

import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import dk.nindroid.rss.compatibility.Exif;
import dk.nindroid.rss.data.ImageReference;
import dk.nindroid.rss.data.LocalImage;
import dk.nindroid.rss.data.Progress;
import dk.nindroid.rss.settings.Settings;

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
	final PreLoader mPreLoader;
	final Loader[] mLoaders;
	final ImageCache mImageCache;
	final Config mConfig;
	
	public OnDemandImageBank(FeedController feedController, MainActivity activity, ImageCache imageCache) {
		this.mFeedController = feedController;
		mLoaders = new Loader[4];
		for(int i = 0; i < mLoaders.length; ++i){
			mLoaders[i] = new Loader(activity.getSettings());
		}
		mPreLoader = new PreLoader(mLoaders, activity.getSettings());
		
		this.mImageCache = imageCache;
		mConfig = activity.getSettings().bitmapConfig;
	}
	
	public void start(){
		for(int i = 0; i < mLoaders.length; ++i){
			Thread t = new Thread(mLoaders[i]);
			t.setPriority(Thread.MIN_PRIORITY);
			t.start();
		}
		Thread t = new Thread(mPreLoader);
		t.setPriority(Thread.MIN_PRIORITY);
		t.start();
	}
	
	public void stop(){
		for(int i = 0; i < mLoaders.length; ++i){
			Handler h = mLoaders[i].mHandler;
			if(h != null){
				Looper l = h.getLooper();
				if(l != null){
					l.quit();
				}
			}
		}
		if(mPreLoader.mHandler != null){
			mPreLoader.mHandler.getLooper().quit();
			mPreLoader.mHandler = null;
		}
	}
	
	public void get(LoaderClient callback, boolean isNext){
		ImageReference ir = mFeedController.getImageReference(isNext);
		if(ir == null) return;
		
		callback.setEmptyImage(ir);
		loadBitmap(callback, ir);
	}
	
	public void get(ImageReference ir, LoaderClient callback){
		loadBitmap(callback, ir);
	}
	
	private void loadBitmap(LoaderClient callback, ImageReference ir){
		Message msg = Message.obtain();
		msg.obj = new LoaderBundle(ir, callback);
		mPreLoader.sendMessage(msg);
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
		public boolean doLoad(String id);
		public boolean bitmapLoaded(String id);
		public Progress getProgressIndicator();
		public void setEmptyImage(ImageReference ir);		
	}
	
	private class PreLoader implements Runnable{
		final Loader[] mLoaders;
		final Settings mSettings;
		PreLoaderHandler mHandler;
		
		
		public PreLoader(Loader[] loaders, Settings settings) {
			this.mLoaders = loaders;
			this.mSettings = settings;
		}
		
		public void sendMessage(Message msg){
			if(mHandler != null){
				mHandler.sendMessage(msg);
			}else{
				LoaderBundle bundle = (LoaderBundle)msg.obj;
				ImageReference ir = bundle.ir;
				bundle.lc.bitmapLoaded(ir.getID());
			}
		}
		
		@Override
		public void run() {
			Looper.prepare();
			mHandler = new PreLoaderHandler(mLoaders, mSettings);
			Looper.loop();
		}
		
		private class PreLoaderHandler extends Handler{
			final Loader[] mLoaders;
			final Settings mSettings;
			int curLoader = -1;
			
			public PreLoaderHandler(Loader[] loaders, Settings settings) {
				this.mLoaders = loaders;
				this.mSettings = settings;
			}
			
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				LoaderBundle bundle = (LoaderBundle)msg.obj;
				ImageReference ir = bundle.ir;
				LoaderClient callback = bundle.lc;
				
				if(ir == null){
					return;
				}
				if(!callback.doLoad(ir.getID())){
					return;
				}
				if(ir.getBitmap() != null){
					return;
				}
				
				if(mImageCache.exists(ir.getID(), this.mSettings.highResThumbs ? 256 : 128)){
					mImageCache.getImage(ir, this.mSettings.highResThumbs ? 256 : 128);
				}
				if(ir.getBitmap() != null){
					if(!callback.bitmapLoaded(ir.getID())){
						ir.recycleBitmap();
					}
					return;
				}
				curLoader = (curLoader + 1) % mLoaders.length;
				mLoaders[curLoader].sendMessage(Message.obtain(msg));
			}
		}
	}
	
	private class Loader implements Runnable{
		LoaderHandler mHandler; 
		Settings mSettings;
		
		public Loader(Settings settings){
			this.mSettings = settings;
		}
		
		public void sendMessage(Message msg){
			mHandler.sendMessage(msg);
		}
		
		@Override
		public void run() {
			Looper.prepare();
			mHandler = new LoaderHandler(mSettings);
			Looper.loop();
		}
		
		class LoaderHandler extends Handler{
			Settings mSettings;
			public LoaderHandler(Settings settings){
				this.mSettings = settings;
			}
			
			Bitmap loadLocal(LocalImage li, Progress progress){
				Bitmap bmp = ImageFileReader.readImage(li.getFile(), mSettings.highResThumbs ? 256 : 128, progress, mConfig);
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
				Bitmap bmp = BitmapDownloader.downloadImage(mSettings.highResThumbs ? ir.get256ImageUrl() : ir.get128ImageUrl(), progress, mConfig);
				synchronized (LoaderHandler.class) {
					if(bmp == null){
						return null;
					}
					if(mSettings.highResThumbs){
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
				if(!callback.doLoad(ir.getID())) return;
				Bitmap bmp;
				synchronized (ir) {
					if(ir.getBitmap() != null) return; // Image already loaded.
					if(ir instanceof LocalImage){
						bmp = loadLocal((LocalImage)ir, callback.getProgressIndicator());
					}else{
						bmp = loadFromWeb(ir, callback.getProgressIndicator());
					}
					if(bmp == null){
						// Force image to retry
						callback.bitmapLoaded(ir.getID());
						return;
					}
					synchronized (LoaderHandler.class) {
						if(callback.doLoad(ir.getID())){
							if(mSettings.highResThumbs){
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
	}
}
