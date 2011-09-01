package dk.nindroid.rss;

import java.io.File;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory.Options;
import android.util.Log;
import dk.nindroid.rss.data.Progress;

public class ImageFileReader{

	public static Bitmap readImage(File f, int size, Progress progress){
		String path = f.getAbsolutePath();
		Options opts = new Options();
		setProgress(progress, 10);
		// Get bitmap dimensions before reading...
		opts.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, opts);
		int width = opts.outWidth;
		int height = opts.outHeight;
		int largerSide = Math.max(width, height);
		setProgress(progress, 20);
		opts.inJustDecodeBounds = false;
		opts.inDither = true;
		opts.inPreferredConfig = Config.ARGB_8888;
		if(width + height > size * 1.5f){
			int sampleSize = getSampleSize(size, largerSide);
			opts.inSampleSize = sampleSize;
			//Log.v("Floating Image", "Reading image (" + width + ", " + height + ") at " + (width / sampleSize) + ", " + (height / sampleSize));
		}
		Bitmap bmp = null;
		try{
			bmp = BitmapFactory.decodeFile(path, opts);
		}catch(Throwable t){
			Log.w("Floating Image", "Oops, image too large. Let's try that again, a bit smaller.", t);
			setProgress(progress, 40);
			opts.inSampleSize = opts.inSampleSize * 2;
			try{
				bmp = BitmapFactory.decodeFile(path, opts);
			}catch(Throwable tr){
				Log.e("Floating Image", "Still not working - bailing!.", tr);
				setProgress(progress, 100);
			}
		}
		Log.v("Floating Image", "bmp is: " + bmp.getWidth() + "x" + bmp.getHeight());
		setProgress(progress, 60);
		if(bmp == null) return null;
		
		setProgress(progress, 80);
		if(largerSide > size){
			bmp = scaleAndRecycle(bmp, size);
			Log.v("Floating Image", "(" + width + "," + height + ") - (" + bmp.getWidth() + "," + bmp.getHeight() + ")");
		}
		setProgress(progress, 90);
		return bmp;
	}
	
	public static Bitmap scaleAndRecycle(Bitmap bmp, int maxSize){
		int width = bmp.getWidth();
		int height = bmp.getHeight();
		int largerSide = Math.max(width, height);
		float scale = (float)maxSize / largerSide;
		Bitmap tmp = Bitmap.createBitmap((int)(width * scale), (int)(height * scale), Config.ARGB_8888);
		Canvas canvas = new Canvas(tmp);
		canvas.drawBitmap(bmp, null, new Rect(0, 0, tmp.getWidth(), tmp.getHeight()), new Paint(Paint.FILTER_BITMAP_FLAG));
		bmp.recycle();
		return tmp;
	}
		
	public static void setProgress(Progress progress, int percent){
		if(progress != null){
			progress.setPercentDone(percent);
		}
	}
	
	// Minimize image size, but don't sample too small!
	private static int getSampleSize(int target, int source){
		int fraction = source / target;
		if(fraction > 24){
			return 32;
		}if(fraction > 12){
			return 16;
		}if(fraction > 6){
			return 8;
		}if(fraction > 3){
			return 4;
		}
		//if(fraction > 1){
			return 2;
		//}
		//return 1;
	}
}
