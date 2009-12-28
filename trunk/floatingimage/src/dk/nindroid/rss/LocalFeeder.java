package dk.nindroid.rss;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.os.Process;
import android.util.Log;
import dk.nindroid.rss.data.ImageReference;
import dk.nindroid.rss.data.LocalImage;
import dk.nindroid.rss.settings.DirectoryBrowser;
import dk.nindroid.rss.settings.FeedsDbAdapter;

public class LocalFeeder implements Runnable{
	private final static int 	SEARCH_LEVELS = 2;
	private final TextureBank 	mBank;
	private Random 				mRand = new Random(new Date().getTime());
	ArrayList<String> 			mImages = new ArrayList<String>();
	private List<String> sources = new ArrayList<String>();
	
	public LocalFeeder(TextureBank bank){
		this.mBank = bank;
	}
	@Override
	public void run() {
		fillSources();
		
		// Find images in sources
		Log.v("LocalFeeder", "Building local image index");
		mImages.clear();
		for(String src : sources){
			File f = new File(src);
			if(f.exists()){
				buildImageIndex(f, 0);
			}
		}
		Process.setThreadPriority(10);
		Log.v("LocalFeeder", mImages.size() + " local images found");
		// Add randomly to texturebank
		while(true){
			try{
				if(mBank.stopThreads){
					Log.v("Bitmap downloader", "*** Stopping asynchronous local feeder");
					return;
				}
				while(mBank.cached.size() < mBank.textureCache && mImages.size() > 0){
					if(mBank.stopThreads) return;
					ImageReference ir = getImageReference();
					if(ir != null){
						mBank.addOldBitmap(ir);
					}
				}
				try {
					synchronized (mBank.cached) {
						mBank.cached.wait();
					}
				}catch (InterruptedException e) {
					Log.v("Bitmap downloader", "*** Stopping asynchronous local feeder");
					return;
				}
			}catch(Exception e){
				Log.e("dk.nindroid.rss.LocalFeeder", "Unexpected exception", e);
			}
		}
	}
	
	private void fillSources() {
		FeedsDbAdapter mDbHelper = new FeedsDbAdapter(ShowStreams.current);
		mDbHelper.open();
		sources.clear();
		Cursor c = mDbHelper.fetchAllFeeds();
		while(c.moveToNext()){
			if(c.getString(1).equals(DirectoryBrowser.ID)){
				sources.add(c.getString(2));
			}
		}
		c.close();
		mDbHelper.close();
		Log.v("LocalFeeder", sources.size() + " local sources added.");
	}
	
	private void buildImageIndex(File dir, int level){;
		for(File f : dir.listFiles()){
			if(f.getName().charAt(0) == '.') continue; // Drop hidden files.
			if(f.isDirectory() && level < SEARCH_LEVELS){
				buildImageIndex(f, level + 1);
			}else{
				if(isImage(f)){
					mImages.add(f.getAbsolutePath());
				}
			}
		}
	}
	
	private ImageReference getImageReference(){
		int idx = mRand.nextInt(mImages.size());
		File file = new File(mImages.get(idx));
		long size = file.length();
		if(size > 2000000){
			mImages.remove(idx);
			return null;
		}
		Bitmap bmp = readImage(file, 128);
		if(bmp != null){
			return new LocalImage(file, bmp);
		}else{
			mImages.remove(idx);
		}
		return null;		
	}
	
	private boolean isImage(File f){
		String filename = f.getName();
		String extension = filename.substring(filename.lastIndexOf('.') + 1);
		if("jpg".equalsIgnoreCase(extension) || "png".equalsIgnoreCase(extension)){
			return true;
		}
		return false;
	}
	
	public static synchronized Bitmap readImage(File f, int size){
		String path = f.getAbsolutePath();
		Options opts = new Options();
		
		// Get bitmap dimensions before reading...
		opts.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, opts);
		int width = opts.outWidth;
		int height = opts.outHeight;
		int largerSide = Math.max(width, height);
		
		opts.inJustDecodeBounds = false;
		if(largerSide > size * 2){
			int sampleSize = getSampleSize(size, largerSide);
			opts.inSampleSize = sampleSize;
		}
		Bitmap bmp = BitmapFactory.decodeFile(f.getAbsolutePath(), opts);
		
		width = bmp.getWidth();
		height = bmp.getHeight();
		largerSide = Math.max(width, height);
		
		if(largerSide > size){
			float scale = (float)size / largerSide;
			Bitmap tmp = Bitmap.createScaledBitmap(bmp, (int)(width * scale), (int)(height * scale), true);
			bmp.recycle();
			bmp = tmp;
		}
		return bmp;
	}
	
	private static int getSampleSize(int target, int source){
		int fraction = source / target;
		if(fraction > 16){
			return 16;
		}if(fraction > 8){
			return 8;
		}if(fraction > 4){
			return 4;
		}if(fraction > 2){
			return 2;
		}
		return 1;
	}
}
