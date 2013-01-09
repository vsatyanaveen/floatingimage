package dk.nindroid.rss;

import java.io.File;
import java.io.IOError;
import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import dk.nindroid.rss.data.Progress;

public class ImageFileReader{
	static Paint mPaint = new Paint(Paint.FILTER_BITMAP_FLAG);
	static boolean canChunkDecode = true;
	
	public static Bitmap readImage(File f, int size, Progress progress, Config config){
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
		opts.inPreferredConfig = config;
		
		Bitmap bmp = null;
		if(canChunkDecode){
			try{
				bmp = ImageFileChunkDecoder.readImage(size, opts, progress, path, config, width, height);
				setProgress(progress, 90);
				return bmp;
			}catch(IOException e){
				Log.e("Floating Image", "Chunk decoder failed - bailing!", e);
				setProgress(progress, 100);
				return null;
			}
			catch(OutOfMemoryError e){
				Log.e("Floating Image", "Chunk decoder out of memory. Bailing!", e);
				return null;
			}
			catch(Throwable t){
				canChunkDecode = false;
				Log.e("Floating Image", "Chunk decoder disabled!", t);
				// Doing it the old fashioned style
			}
		}
		opts.inSampleSize = getSampleSize(size, largerSide);
		try{
			bmp = BitmapFactory.decodeFile(path, opts);
		}catch(Throwable t){
			Log.w("Floating Image", "Oops, image too large. Let's try that again, a bit smaller.", t);
			setProgress(progress, 40);
			opts.inSampleSize = opts.inSampleSize * 2;
			try{
				bmp = BitmapFactory.decodeFile(path, opts);
			}catch(Throwable tr){
				Log.e("Floating Image", "Still not working - bailing!", tr);
				setProgress(progress, 100);
			}
		}
		setProgress(progress, 60);
		if(bmp == null) return null;
		largerSide = Math.max(bmp.getWidth(), bmp.getHeight());
		setProgress(progress, 80);
		if(largerSide > size){
			bmp = scaleAndRecycle(bmp, size, config);
		}
		setProgress(progress, 90);
		
		return bmp;
	}
	
	public static Bitmap scaleAndRecycle(Bitmap bmp, int maxSize, Config config){
		Bitmap tmp = scale(bmp, maxSize, config);
		bmp.recycle();
		return tmp;
	}
	
	public static Bitmap scale(Bitmap bmp, int maxSize, Config config){
		int width = bmp.getWidth();
		int height = bmp.getHeight();
		int largerSide = Math.max(width, height);
		float scale = (float)maxSize / largerSide;
		Bitmap tmp;
		int targetX = (int)(width * scale);
		int targetY = (int)(height * scale);
		
		if(targetX == 0 || targetY == 0){
			return null;
		}
		if(config == Config.RGB_565){
			tmp = Bitmap.createScaledBitmap(bmp, targetX, targetY, true);
		}else{
			tmp = Bitmap.createBitmap(targetX, targetY, config);
			Canvas canvas = new Canvas(tmp);
			canvas.drawBitmap(bmp, null, new Rect(0, 0, tmp.getWidth(), tmp.getHeight()), mPaint);
		}
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
