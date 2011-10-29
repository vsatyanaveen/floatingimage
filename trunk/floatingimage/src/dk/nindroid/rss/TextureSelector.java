package dk.nindroid.rss;

import java.io.File;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
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
	private Config		mConfig;
	private Bitmap		mCurrentBitmap;
	private ImagePlane	mCurSelected;
		
	public TextureSelector(Display display, Config config){
		this.mDisplay = display;
		this.mConfig = config;
	}
	
	public Bitmap getCurrentBitmap(){
		return mCurrentBitmap;
	}
	
	public void startThread(){
		if(mWorker != null) stopThread();
		mWorker = new Worker(mDisplay, mConfig);
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
				mWorker = null;
			}
		}
	}
	
	public void selectImage(ImagePlane img, ImageReference ref){
		if(mWorker != null){
			synchronized (mWorker) {
				mCurSelected = img;
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
			return mWorker.progress.isKey(mCurSelected) ? mWorker.progress.getPercentDone() : 2;
		}
		return 0;
	}
	
	private class Worker implements Runnable, Display.ImageSizeChanged{
		private final Paint			mPaint  = new Paint(Paint.FILTER_BITMAP_FLAG); 
		private ImageReference 		mRef;
		private boolean 			mRun	= true;
		private final Progress 		progress = new Progress();
		private boolean				mDoApplyLarge = false;
		private boolean				mDoApplyOriginal = false;
		private int					mTextureResolution;
		private boolean				mSwapSides = false;
		private Display				mDisplay;
		private Config 				mConfig;
		
		public Worker(Display display, Config config){
			this.mDisplay = display;
			this.mConfig = config;
		}
		
		private void setTextureResolution(){
			int max = Math.max(mDisplay.getPortraitHeightPixels(), mDisplay.getPortraitWidthPixels());
			if(max == 0) return; // Not ready yet!
			if(max <= 512){
				mTextureResolution = 512;
			}
			else
			{
				mTextureResolution = 1024;
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
							bmp = ImageFileReader.readImage(new File(url), res, progress, mConfig);
						}catch(Throwable t){
							Log.e("Floating Image", "Unexpected nastyness caught!", t);
						}
						if(bmp != null){
							applyLarge(bmp);
						}
					}else{ // Download from web
						// Retry max 5 times in case we time out.
						for(int i = 0; i < 5; ++i){
							Bitmap bmp = BitmapDownloader.downloadImage(url, progress, mConfig);
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
				bmp = ImageFileReader.scaleAndRecycle(bmp, res, mConfig);
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
				}else{
					if(aspect > 1){
						width  = mTextureResolution;
						height = (int)(width / aspect);
					}else{
						height = mTextureResolution;
						width  = (int)(aspect * height);
					}
				}
				applyBitmap(mCurrentBitmap, ImagePlane.SIZE_LARGE, Math.max(width, height) / (float)mTextureResolution);
			}
		}
		
		private void applyOriginal(){
			if(mCurSelected != null && mCurSelected.validForTextureUpdate()){
				applyBitmap(mCurrentBitmap, ImagePlane.SIZE_ORIGINAL, 1.0f);
			}
		}
		
		private void applyBitmap(Bitmap bmp, int sizeType, float scale){
			if(bmp == null || bmp.isRecycled()) return;
			int res = mTextureResolution;
			Bitmap bitmap = null;
			try{
				bitmap = Bitmap.createBitmap(res, res, mConfig);
			}catch(Throwable t){
				Log.w("Floating Image", "Couldn't apply bitmap, trying again with a smaller version", t);
				res /= 2;
				scale /= 2;
				try{
					bitmap = Bitmap.createBitmap(res, res, mConfig);
				}catch(Throwable tr){
					Log.e("Floating Image", "Still cannot apply image - bailing!", tr);
					ImageFileReader.setProgress(progress, 100);
				}
			}
			if(bitmap == null) {
				return;
			}
			int max = Math.max(bmp.getWidth(), bmp.getHeight());
			float sizeFraction = max / (float)res;
			Canvas canvas = new Canvas(bitmap);
			int drawWidth = (int)(bmp.getWidth() * scale / sizeFraction);
			int drawHeight = (int)(bmp.getHeight() * scale / sizeFraction);
			canvas.drawBitmap(bmp, null, new Rect(0, 0, drawWidth, drawHeight), mPaint);
			if(mRef != null){
				bitmap.recycle();
			}else{
				mCurSelected.setFocusTexture(bitmap, (float)drawWidth/res, (float)drawHeight/res, sizeType);
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
