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
	private Display		mDisplay;
		
	public TextureSelector(Display display){
		this.mDisplay = display;
	}
	
	public void startThread(){
		if(mWorker != null) stopThread();
		mWorker = new Worker(mDisplay);
		mWorker.mRun = true;
		mDisplay.RegisterImageSizeChangedListener(mWorker);
		Thread t = new Thread(mWorker);
		t.start();
	}
	
	public void stopThread(){
		if(mWorker != null){
			synchronized (mWorker) {
				mDisplay.deRegisterImageSizeChangedListener(mWorker);
				mWorker.mRun = false;
				mWorker.notifyAll();
			}
		}
	}
	
	public void selectImage(ImagePlane img, ImageReference ref){
		if(mWorker != null){
			synchronized (mWorker) {
				mWorker.mCurSelected = img;
				mWorker.mRef = ref;
				mWorker.notify();
			}		
		}
	}
	
	public void applyLarge(){
		if(mWorker != null){
			mWorker.mDoApplyLarge = true;
			synchronized (mWorker) {
				mWorker.notify();
			}
		}
	}
	
	public void setRotated(float rot){
		if(mWorker != null){
			mWorker.mSwapSides = rot % 180 == 90;
			applyLarge();
		}
	}
	
	public void applyOriginal(){
		if(mWorker != null){
			mWorker.mDoApplyOriginal = true;
			synchronized (mWorker) {
				mWorker.notify();
			}
		}
	}
	
	public int getProgress(){
		if(mWorker != null){
			return mWorker.progress.isKey(mWorker.mCurSelected) ? mWorker.progress.getPercentDone() : 2;
		}
		return 0;
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
		private Display				mDisplay;
		
		public Worker(Display display){
			this.mDisplay = display;
		}
		
		private void setTextureResolution(){
			int max = Math.max(mDisplay.getPortraitHeightPixels(), mDisplay.getPortraitWidthPixels());
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
					int res = mTextureResolution;
					if(res == 0){
						setTextureResolution();
						res = mTextureResolution;
					}
					if(ref instanceof LocalImage){ // Special case, read from disk
						Bitmap bmp = null;
						try{
							bmp = ImageFileReader.readImage(new File(url), res, progress);
						}catch(Throwable t){
							Log.e("Floating Image", "Unexpected nastyness caught!", t);
						}
						if(bmp != null){
							applyLarge(bmp);
						}
					}else{ // Download from web
						// Retry max 5 times in case we time out.
						for(int i = 0; i < 5; ++i){
							Bitmap bmp = BitmapDownloader.downloadImage(url, progress);
							if(bmp != null && bmp.getWidth() > 0 && bmp.getHeight() > 0){
								Log.v("Floating Image", "Image size: (" + bmp.getWidth() + "," + bmp.getHeight() + ")");
								applyLarge(bmp);
								break;
							}else{
								mCurSelected.setFocusTexture(null, 0, 0, ImagePlane.SIZE_LARGE);
							}
						}
					}
				}
				synchronized (this) {
					if(!mRun){
						Log.i("dk.nindroid.rss.TextureSelector", "Stop received");
						return;
					}
					if(mRef == null && !mDoApplyLarge && !mDoApplyOriginal){
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
			int res = mTextureResolution;
			if(max > res){
				float scale = (float)res / max;
				Bitmap tmp = Bitmap.createScaledBitmap(bmp, (int)(width * scale), (int)(height * scale), true);
				bmp.recycle();
				bmp = tmp;
			}
			if(mCurrentBitmap != null && !mCurrentBitmap.isRecycled()){
				mCurrentBitmap.recycle();
			}
			mCurrentBitmap = bmp;
			applyLarge();
		}
		
		// Scale bmp to current screen size, and apply
		private void applyLarge(){
			if(mCurrentBitmap != null && !mCurrentBitmap.isRecycled() && mRef == null){
				int displayMax = Math.max(mDisplay.getTargetHeightPixels(), mDisplay.getTargetWidthPixels());
				float aspect = mCurrentBitmap.getWidth() / (float)mCurrentBitmap.getHeight();
				int height, width;
				if(displayMax < mTextureResolution){
					if(mSwapSides){
						aspect = 1.0f / aspect;
					}
					if(isTall(aspect)){
						height = (int)(mDisplay.getTargetHeightPixels() * (mDisplay.getFocusedHeight() / mDisplay.getHeight()));
						height *= mDisplay.getFill();
						
						width = (int)(aspect * height);
					}else{
						width = mDisplay.getTargetWidthPixels();
						width *= mDisplay.getFill();
						
						height = (int)(width / aspect);
					}
					if(mSwapSides){
						width ^= height; height ^= width; width ^= height; // mmmMMMMmmm... Donuts!
					}
				}else{ // Hello Alexis. :)
					if(aspect > 1){
						width  = mTextureResolution;
						height = (int)(width / aspect);
					}else{
						height = mTextureResolution;
						width  = (int)(aspect * height);
					}
				}
				Bitmap bmp = null;
				try{
					bmp = Bitmap.createScaledBitmap(mCurrentBitmap, width, height, true);
				}catch(Throwable t){
					Log.w("Floating Image", "Oops, let's try again with a smaller image", t);
					try{
						bmp = Bitmap.createScaledBitmap(mCurrentBitmap, width / 2, height / 2, true);
					}catch(Throwable tr){
						Log.e("Floating Image", "Shit, that didn't work either. Bailing!", tr);
					}
				}
				if(bmp != null){
					applyBitmap(bmp, ImagePlane.SIZE_LARGE);
					bmp.recycle();
				}
			}
		}
		
		private void applyOriginal(){
			if(mCurSelected != null && mCurSelected.validForTextureUpdate()){
				applyBitmap(mCurrentBitmap, ImagePlane.SIZE_ORIGINAL);
			}
		}
		
		private void applyBitmap(Bitmap bmp, int sizeType){
			if(bmp == null) return;
			int res = mTextureResolution;
			Bitmap bitmap = null;
			try{
				bitmap = Bitmap.createBitmap(res, res, Config.RGB_565);
			}catch(Throwable t){
				Log.e("Floating Image", "Couldn't apply bitmap.", t);
			}
			if(bitmap == null) return;
			Canvas canvas = new Canvas(bitmap);
			canvas.drawBitmap(bmp, 0, 0, mPaint);
			if(mRef != null){
				bitmap.recycle();
			}else{
				mCurSelected.setFocusTexture(bitmap, (float)bmp.getWidth() / res, (float)bmp.getHeight() / res, sizeType);
			}
		}
		
		private boolean isTall(float aspect){
			return aspect < mDisplay.getWidth() / mDisplay.getFocusedHeight();
		}
		
		@Override
		public void imageSizeChanged() {
			// Don't retexture all large textured planes, only the focused one!
			if(mCurSelected != null && mCurSelected.validForTextureUpdate()){
				mDoApplyLarge = true;
				synchronized (this) {
					this.notify();
				}
			}
		}
	}
}
