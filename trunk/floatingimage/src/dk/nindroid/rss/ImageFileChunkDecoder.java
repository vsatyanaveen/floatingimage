package dk.nindroid.rss;

import java.io.IOException;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.Rect;
import dk.nindroid.rss.data.Progress;

public class ImageFileChunkDecoder {
	@SuppressLint("NewApi")
	public static Bitmap readImage(int size, Options options, Progress progress, String path, Config config, int width, int height) throws IOException{
		int largerSide = Math.max(width, height);
		float scale = size / (float)largerSide;
		if(largerSide <= scale){
			return BitmapFactory.decodeFile(path);
		}
		int targetWidth = (int)(width * scale);
		int targetHeight = (int)(height * scale);
		
		int sampleSize = getSampleSize(size, largerSide);
		options.inSampleSize = sampleSize;
		
		Bitmap target = Bitmap.createBitmap(targetWidth, targetHeight, config);
		Canvas cvs = new Canvas(target);
		int chunkWidth = width / 6;
		int chunkHeight = height / 6;
		int scaledChunkWidth = (int)(chunkWidth * scale);
		int scaledChunkHeight = (int)(chunkHeight * scale);
		
		BitmapRegionDecoder regionDecoder = BitmapRegionDecoder.newInstance(path, true);
		Rect rect = new Rect();
		Rect dest = new Rect();
		for(int y = 0; y < 6; ++y){
			for(int x = 0; x < 6; ++x){
				rect.set(chunkWidth * x, chunkHeight * y, chunkWidth * x + chunkWidth, chunkHeight * y + chunkHeight);
				Bitmap region = regionDecoder.decodeRegion(rect, options);
				rect.set(0, 0, region.getWidth(), region.getHeight());
				dest.set(scaledChunkWidth * x, scaledChunkHeight * y, scaledChunkWidth * x + scaledChunkWidth, scaledChunkHeight * y + scaledChunkHeight);
				cvs.drawBitmap(region, rect, dest, ImageFileReader.mPaint);
				region.recycle();
				int percent = (int)((x + 6 * y) / 36.0f * 70) + 10;
				ImageFileReader.setProgress(progress, percent);
			}
		}
		return target;
	}
	
	private static int getSampleSize(int target, int source){
		int fraction = source / target;
		if(fraction > 28){
			return 32;
		}if(fraction > 12){
			return 16;
		}if(fraction > 6){
			return 8;
		}if(fraction >= 3){
			return 4;
		}
		if(fraction == 2){
			return 2;
		}
		return 1;
	}
}
