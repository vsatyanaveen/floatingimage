package dk.nindroid.rss;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory.Options;
import android.os.Environment;
import android.util.Log;
import dk.nindroid.rss.data.FileDateReverseComparator;
import dk.nindroid.rss.data.ImageReference;

public class ImageCache {
	TextureBank bank;
	private final String 	mExploreFolder;
	private final String 	mExploreInfoFolder;
	//List<File>				mFiles;
	Map<String, File>		mCached;
	Random 					mRand;
	File 					mExplore;
	File 					mExploreInfo;
	byte[]					mBuf;
	Context					mContext;
	private final Options 	mOpts;					
	
	public ImageCache(Context context, TextureBank bank){
		mOpts = new Options();
		mOpts.inPreferredConfig = Config.ARGB_8888;
		this.bank = bank;
		this.mContext = context;
		mBuf = new byte[1024];
		String datafolder = mContext.getString(R.string.dataFolder);
		datafolder = Environment.getExternalStorageDirectory().getAbsolutePath() + datafolder;
		mExploreInfoFolder = datafolder + context.getString(R.string.exploreFolder);
		mExploreFolder = mExploreInfoFolder + "/bmp";
	}
	
	void setupImageCache(){
		mRand = new Random(new Date().getTime());
		mExploreInfo = new File(mExploreInfoFolder);
		mExplore = new File(mExploreFolder);
		mExploreInfo.mkdirs(); // Make dir if not exists
		mExplore.mkdirs(); // Make dir if not exists
		File[] exploreInfoArray = mExploreInfo.listFiles();
		if(exploreInfoArray == null) return;
		//mFiles = new ArrayList<File>(exploreInfoArray.length);
		mCached = new HashMap<String, File>(exploreInfoArray.length);
		Log.v("Floating Image", exploreInfoArray.length + " files in cache.");
		for(int i = 0; i < exploreInfoArray.length; ++i){
			File f = exploreInfoArray[i];
			if(f == null) break;
			if(!f.isDirectory()){
				addFile(f);
			}
		}
	}
	
	// Will not delete directory, just fail when trying to...
	public void cleanCache(){
		if(mCached == null){
			setupImageCache();
		}
		try{
			int limit = 500;
			File[] files = mExploreInfo.listFiles();
			if(files == null || files.length < limit) return;
			
			Arrays.sort(files, new FileDateReverseComparator());
			int size = files.length;
			for(int i = size - 1; i > limit; --i){
				try{
					InputStream is_info = new FileInputStream(files[i]);
					BufferedInputStream bis_info = new BufferedInputStream(is_info, 64);
					DataInputStream dis = new DataInputStream(bis_info);
					File img = new File(dis.readLine());
					img.delete();
					files[i].delete();
					files[i] = null;
					dis.close();
					bis_info.close();
				}catch(NullPointerException e){
					Log.w("Floating Image", "Unexpected null pointer exception caught.", e);
					files[i].delete();
					files[i] = null;
				}
			}
			synchronized(mCached){
				mCached.clear();
			}
			int entries = Math.min(limit, files.length);
			synchronized(mCached){
				for(int i = 0; i < entries; ++i){
					addFile(files[i]);
				}
			}
		}catch(Exception e){
			Log.w("Floating Image", "Error removing old images...", e);
		}
	}
	
	public void saveImage(ImageReference ir){
		if(mCached == null){
			setupImageCache();
		}
		if(ir == null) return;
		String name = ir.getID();
		if(exists(name, ir.getBitmap().getWidth())) return;
		try {
			File f = new File(mExplore.getPath() + "/" + name + "-" + ir.getBitmap().getWidth() + ".jpg");
			FileOutputStream fos = new FileOutputStream(f);
			ir.getBitmap().compress(CompressFormat.JPEG, 85, fos);
			fos.flush();
			fos.close();
			File f_info = updateMeta(ir);
			if(f_info == null){
				f.delete();
			}else{
				synchronized(mCached){
					addFile(f_info);
				}
			}
		} catch (FileNotFoundException e) {
			Log.w("Floating Image", "Image could not be cached", e);
			return;
		} catch (IOException e) {
			Log.w("Floating Image", "Image could not be cached", e);
			return;
		}
	}
	
	public File updateMeta(ImageReference ir){
		try {
			String name = ir.getID();
			String thumbPath = mExplore.getPath() + "/" + name + "-" + ir.getBitmap().getWidth() + ".jpg";
			File f_info = new File(mExploreInfo.getPath() + "/" + name + "-" + ir.getBitmap().getWidth() + ".info");
			f_info.delete();
			FileOutputStream fos_info = new FileOutputStream(f_info);
			fos_info.write((thumbPath + "\n" + ir.getInfo()).getBytes());
			fos_info.flush();
			fos_info.close();
			return f_info;
		} catch (FileNotFoundException e) {
			Log.w("Floating Image", "Image could not be cached", e);
			return null;
		} catch (IOException e) {
			Log.w("Floating Image", "Image could not be cached", e);
			return null;
		}
	}
	
	protected void addFile(File file){
		//mFiles.add(file);
		mCached.put(file.getName(), file);
	}


	public ImageReference getFile(File f_info, ImageReference ir){
		//File f_info = null;
		try {
			//f_info = mFiles.get(index);
			int length = (int)f_info.length();
			InputStream is_info = new FileInputStream(f_info);
			if(mBuf.length < length){
				mBuf = new byte[length];
			}
			is_info.read(mBuf);
			is_info.close();
			String s = new String(mBuf, 0, length, "UTF-8");
			String[] tokens = s.split("\n");
			
			String bmpName = tokens[0];
			if(bmpName == null){
				f_info.delete();
				mCached.remove(ir.getID() + ".info");
				return null;
			}
			// Read bitmap
			Bitmap bmp = BitmapFactory.decodeFile(bmpName, mOpts);
			
			if(bmp == null){
				f_info.delete();
				synchronized(mCached){
					mCached.remove(ir.getID() + ".info");
				}
				return null;
			}
			
			// Fill image reference
			ir.parseInfo(tokens, bmp);
			
			// Clean up
			return ir;
		} catch (FileNotFoundException e) {
			if(f_info != null){
				f_info.delete();
				synchronized(mCached){
					mCached.remove(ir.getID() + ".info");
				}
			}
			Log.w("Floating Image", "Image cache file not found: " + e);
		} catch (IOException e) {
			Log.w("Floating Image", "IOException reading Image cache!", e);
		}
		return null;
	}
	
	public boolean exists(String name, int size){
		if(mCached == null){
			setupImageCache();
		}
		synchronized(mCached){
			if(mCached.containsKey(name + "-" + size + ".info")){
				return true;
			}
			return false;
		}
	}
	
	public void addImage(ImageReference ir, boolean next, int size){
		bank.addBitmap(getFile(mCached.get(ir.getID() + "-" + size + ".info"), ir), false, next);
	}
}
