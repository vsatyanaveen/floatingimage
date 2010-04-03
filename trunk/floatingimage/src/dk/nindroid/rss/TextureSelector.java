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
import dk.nindroid.rss.renderers.Image;

public class TextureSelector implements Runnable{
	private final static Bitmap 	mBitmap = Bitmap.createBitmap(512, 512, Config.RGB_565);
	private final static Paint		mPaint  = new Paint();
	private final static Canvas		mCanvas = new Canvas(mBitmap); 
	private static TextureSelector 	mTs;
	private static Image 			mCurSelected;
	private static ImageReference 	mRef;
	private static boolean 			mRun	= true;
	private static final Progress progress = new Progress();
	public boolean abort = false;
	
	public static void startThread(){
		mRun = true;
		mTs = new TextureSelector();
		Thread t = new Thread(mTs);
		t.start();
	}
	
	public static void stopThread(){
		synchronized (TextureSelector.class) {
			mRun = false;
			TextureSelector.class.notifyAll();
		}
	}
	
	public static void selectImage(Image img, ImageReference ref){
		synchronized (TextureSelector.class) {
			mCurSelected = img;
			mRef = ref;
			TextureSelector.class.notify();
		}		
	}
	@Override
	public void run() {
		Process.setThreadPriority(5);
		ImageReference ref = null;
		while (true){
			if(!mRun){
				Log.i("dk.nindroid.rss.TextureSelector", "Stop received");
				return;
			}
			synchronized (TextureSelector.class) {
				ref = mRef;
				mRef = null;
			}
			if(ref != null){
				String url = ref.getBigImageUrl();
				progress.setKey(mCurSelected);
				progress.setPercentDone(5);
				if(ref instanceof LocalImage){ // Special case, read from disk
					Bitmap bmp = ImageFileReader.readImage(new File(url), 450, progress);
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
			synchronized (TextureSelector.class) {
				if(!mRun){
					Log.i("dk.nindroid.rss.TextureSelector", "Stop received");
					return;
				}
				if(mRef == null){
					try {
						TextureSelector.class.wait();
					} catch (InterruptedException e) {
					}
				}
			}
		}
	}
	
	private void applyLarge(Bitmap bmp){
		if(mRef != null) return;
		mCanvas.drawBitmap(bmp, 0, 0, mPaint);
		bmp.recycle();
		synchronized(TextureSelector.class){
			if(mRef != null) return;
			mCurSelected.setFocusTexture(mBitmap, (float)bmp.getWidth() / 512.0f, (float)bmp.getHeight() / 512.0f);
		}
	}
	
	public static int getProgress(){
		return progress.isKey(mCurSelected) ? progress.getPercentDone() : 2;
	}
}
