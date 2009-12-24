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

public class TextureSelector implements Runnable{
	private final static Bitmap 	mBitmap = Bitmap.createBitmap(512, 512, Config.RGB_565);
	private final static Paint		mPaint  = new Paint();
	private final static Canvas		mCanvas = new Canvas(mBitmap); 
	private static TextureSelector 	mTs;
	private static Image 			mCurSelected;
	private static ImageReference 	mRef;
	public boolean abort = false;
	
	public static void selectImage(Image img, ImageReference ref){
		mCurSelected = img;
		mRef = ref;
		if(mTs != null){
			mTs.abort = true;
		}
		mTs = new TextureSelector();
		Thread t = new Thread(mTs);
		t.start();
	}
	@Override
	public void run() {
		Process.setThreadPriority(5);
		String url = mRef.getBigImageUrl();
		if(mRef instanceof LocalImage){ // Special case, read from disk
			Bitmap bmp = LocalFeeder.readImage(new File(url), 450);
			if(bmp != null){
				applyLarge(bmp);
			}
		}else{ // Download from web
			// Retry 5 times.. Why do I have to do this?! This should just WORK!!
			for(int i = 0; i < 5; ++i){
				Bitmap bmp = BitmapDownloader.downloadImage(url);
				if(bmp != null){
					applyLarge(bmp);
					return;
				}else{
					Log.v("dk.nindroid.rss.TextureSelector", mRef.getBigImageUrl() + " not found!");
					mCurSelected.setFocusTexture(null, 0, 0);
				}
			}
		}
	}
	
	private void applyLarge(Bitmap bmp){
		if(!abort){
			mCanvas.drawBitmap(bmp, 0, 0, mPaint);
			bmp.recycle();
			mCurSelected.setFocusTexture(mBitmap, (float)bmp.getWidth() / 512.0f, (float)bmp.getHeight() / 512.0f);
		}
	}
}
