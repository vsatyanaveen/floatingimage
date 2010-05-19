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
		Thread t = new Thread(mWorker);
		t.start();
	}
	
	public void stopThread(){
		synchronized (mWorker) {
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
	
	public int getProgress(){
		return mWorker.progress.isKey(mWorker.mCurSelected) ? mWorker.progress.getPercentDone() : 2;
	}
	
	private class Worker implements Runnable{
		private final Paint			mPaint  = new Paint(); 
		private ImagePlane			mCurSelected;
		private ImageReference 		mRef;
		private boolean 			mRun	= true;
		private final Progress 		progress = new Progress();
		
		@Override
		public void run() {
			Process.setThreadPriority(5);
			ImageReference ref = null;
			while (true){
				if(!mRun){
					Log.i("dk.nindroid.rss.TextureSelector", "Stop received");
					return;
				}
				synchronized (this) {
					ref = mRef;
					mRef = null;
				}
				if(ref != null){
					String url = ref.getBigImageUrl();
					progress.setKey(mCurSelected);
					progress.setPercentDone(5);
					if(ref instanceof LocalImage){ // Special case, read from disk
						Bitmap bmp = ImageFileReader.readImage(new File(url), Math.max(RiverRenderer.mDisplay.getPortraitHeightPixels(), RiverRenderer.mDisplay.getPortraitWidthPixels()), progress);
						if(bmp != null){
							applyLarge(bmp);
						}
					}else{ // Download from web
						// Retry 5 times.. Why do I have to do this?! This should just WORK!!
						for(int i = 0; i < 5; ++i){
							Bitmap bmp = BitmapDownloader.downloadImage(url, progress);
							if(bmp != null){
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
			int screenMax = Math.max(RiverRenderer.mDisplay.getHeightPixels(), RiverRenderer.mDisplay.getWidthPixels());
			if(max > screenMax){
				float scale = (float)screenMax / max;
				Bitmap tmp = Bitmap.createScaledBitmap(bmp, (int)(width * scale), (int)(height * scale), true);
				bmp.recycle();
				bmp = tmp;
			}
			Bitmap bitmap = Bitmap.createBitmap(1024, 1024, Config.RGB_565);
			Canvas canvas = new Canvas(bitmap);
			canvas.drawBitmap(bmp, 0, 0, mPaint);
			bmp.recycle();
			synchronized(this){
				if(mRef != null){
					bitmap.recycle();
				}else{
					mCurSelected.setFocusTexture(bitmap, (float)bmp.getWidth() / 1024.0f, (float)bmp.getHeight() / 1024.0f);
				}
			}
		}
	}
}
