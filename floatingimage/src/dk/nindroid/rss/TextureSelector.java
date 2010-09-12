package dk.nindroid.rss;

import java.io.File;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Bitmap.Config;
import android.os.Process;
import android.util.Log;
import dk.nindroid.rss.data.ImageReference;
import dk.nindroid.rss.data.LocalImage;
import dk.nindroid.rss.data.Progress;
import dk.nindroid.rss.renderers.ImagePlane;

public class TextureSelector {
	private Worker 		mWorker;
		
	public void startThread(){
		if(mWorker != null) stopThread();
		mWorker = new Worker();
		mWorker.mRun = true;
		RiverRenderer.mDisplay.RegisterImageSizeChangedListener(mWorker);
		Thread t = new Thread(mWorker);
		t.start();
	}
	
	public void stopThread(){
		synchronized (mWorker) {
			RiverRenderer.mDisplay.deRegisterImageSizeChangedListener(mWorker);
			mWorker.mRun = false;
			mWorker.notifyAll();
		}
	}
	
	public void selectImage(ImagePlane img, ImageReference ref){
		synchronized (mWorker) {
			mWorker.mCurSelected = img;
			mWorker.mRef = ref;
			mWorker.notify();
		}		
	}
	
	public void applyLarge(){
		mWorker.mDoApplyLarge = true;
		synchronized (mWorker) {
			mWorker.notify();
		}
	}
	
	public void setRotated(float rot){
		mWorker.mSwapSides = rot % 180 == 90;
		applyLarge();
	}
	
	public void applyOriginal(){
		mWorker.mDoApplyOriginal = true;
		synchronized (mWorker) {
			mWorker.notify();
		}
	}
	
	public int getProgress(){
		return mWorker.progress.isKey(mWorker.mCurSelected) ? mWorker.progress.getPercentDone() : 2;
	}
	
	private class Worker implements Runnable, Display.ImageSizeChanged{
		private final Paint			mPaint  = new Paint(); 
		private ImagePlane			mCurSelected;
		private ImageReference 		mRef;
		private boolean 			mRun	= true;
		private final Progress 		progress = new Progress();
		private Bitmap				mCurrentBitmap;
		private boolean				mDoApplyLarge = false;
		private boolean				mDoApplyOriginal = false;
		private int					mTextureResolution;
		private boolean				mSwapSides = false;
		
		private void setTextureResolution(){
			int max = Math.max(RiverRenderer.mDisplay.getPortraitHeightPixels(), RiverRenderer.mDisplay.getPortraitWidthPixels());
			if(max == 0) return; // Not ready yet!
			if(max <= 512){
				mTextureResolution = 512;
			}else if(max <= 1024){
				mTextureResolution = 1024;
			}else if(max <= 2048){
				mTextureResolution = 2048;
			}else{
				mTextureResolution = 4092;
			}
		}
		
		@Override
		public void run() {
			Process.setThreadPriority(5);
			setTextureResolution();
			ImageReference ref = null;
			while (true){
				if(!mRun){
					Log.i("dk.nindroid.rss.TextureSelector", "Stop received");
					return;
				}
				if(mDoApplyLarge){
					applyLarge();
					mDoApplyLarge = false;
				}
				if(mDoApplyOriginal){
					applyOriginal();
					mDoApplyOriginal = false;
				}
				synchronized (this) {
					ref = mRef;
					mRef = null;
				}
				if(ref != null){
					int rot = (int)ref.getTargetOrientation();
					mSwapSides = rot % 180 == 90;
					mCurrentBitmap = null;
					String url = ref.getBigImageUrl();
					progress.setKey(mCurSelected);
					progress.setPercentDone(5);
					if(ref instanceof LocalImage){ // Special case, read from disk
						Bitmap bmp = ImageFileReader.readImage(new File(url), Math.max(RiverRenderer.mDisplay.getPortraitHeightPixels(), RiverRenderer.mDisplay.getPortraitWidthPixels()), progress);
						if(bmp != null){
							applyLarge(bmp);
						}
					}else{ // Download from web
						// Retry max 5 times in case we time out.
						for(int i = 0; i < 5; ++i){
							Bitmap bmp = BitmapDownloader.downloadImage(url, progress);
							if(bmp != null && bmp.getWidth() > 0 && bmp.getHeight() > 0){
								applyLarge(bmp);
								break;
							}else{
								mCurSelected.setFocusTexture(null, 0, 0);
							}
						}
					}
				}
				synchronized (this) {
					if(!mRun){
						Log.i("dk.nindroid.rss.TextureSelector", "Stop received");
						return;
					}
					if(mRef == null){
						try {
							this.wait();
						} catch (InterruptedException e) {
						}
					}
				}
			}
		}
		
		private void applyLarge(Bitmap bmp){
			if(mRef != null) return;
			int height = bmp.getHeight();
			int width = bmp.getWidth();
			int max = Math.max(width, height);
			int screenMax = Math.max(RiverRenderer.mDisplay.getPortraitHeightPixels(), RiverRenderer.mDisplay.getPortraitWidthPixels());
			if(max > screenMax){
				float scale = (float)screenMax / max;
				Bitmap tmp = Bitmap.createScaledBitmap(bmp, (int)(width * scale), (int)(height * scale), true);
				bmp.recycle();
				if(mCurrentBitmap != null && !mCurrentBitmap.isRecycled()){
					mCurrentBitmap.recycle();
				}
				bmp = tmp;
			}
			mCurrentBitmap = bmp;
			applyLarge();
		}
		
		// Scale bmp to current screen size, and apply
		private void applyLarge(){
			if(mCurrentBitmap != null && !mCurrentBitmap.isRecycled() && mRef == null){
				float aspect = mCurrentBitmap.getWidth() / (float)mCurrentBitmap.getHeight();
				if(mSwapSides){
					aspect = 1.0f / aspect;
				}
				int height, width;
				if(isTall(aspect)){
					height = (int)(RiverRenderer.mDisplay.getTargetHeightPixels() * (RiverRenderer.mDisplay.getFocusedHeight() / RiverRenderer.mDisplay.getHeight()));
					height *= RiverRenderer.mDisplay.getFill();
					
					width = (int)(aspect * height);
				}else{
					width = RiverRenderer.mDisplay.getTargetWidthPixels();
					width *= RiverRenderer.mDisplay.getFill();
					
					height = (int)(width / aspect);
				}
				if(mSwapSides){
					width ^= height; height ^= width; width ^= height; // mmmMMMMmmm... Donuts!
				}
				Bitmap bmp = Bitmap.createScaledBitmap(mCurrentBitmap, width, height, true);
				
				applyBitmap(bmp);
				bmp.recycle();
			}
		}
		
		private void applyOriginal(){
			applyBitmap(mCurrentBitmap);
		}
		
		private void applyBitmap(Bitmap bmp){
			int res = mTextureResolution;
			if(res == 0){
				setTextureResolution();
				res = mTextureResolution;
			}
			Bitmap bitmap = Bitmap.createBitmap(res, res, Config.RGB_565);
			Canvas canvas = new Canvas(bitmap);
			canvas.drawBitmap(bmp, 0, 0, mPaint);
			synchronized(this){
				if(mRef != null){
					bitmap.recycle();
				}else{
					mCurSelected.setFocusTexture(bitmap, (float)bmp.getWidth() / res, (float)bmp.getHeight() / res);
				}
			}
		}
		
		private boolean isTall(float aspect){
			return aspect < RiverRenderer.mDisplay.getWidth() / RiverRenderer.mDisplay.getFocusedHeight();
		}
		
		@Override
		public void imageSizeChanged() {
			mDoApplyLarge = true;
			synchronized (this) {
				this.notify();
			}
		}
	}
}
