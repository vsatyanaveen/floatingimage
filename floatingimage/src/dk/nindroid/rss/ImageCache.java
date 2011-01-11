package dk.nindroid.rss;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.os.Environment;
import android.util.Log;
import dk.nindroid.rss.data.FileDateReverseComparator;
import dk.nindroid.rss.data.ImageReference;

public class ImageCache {
	TextureBank bank;
	private final String 	mExploreFolder;
	private final String 	mExploreInfoFolder;
	List<File>				mFiles;
	Map<String, Integer>	mCached;
	Random 					mRand;
	File 					mExplore;
	File 					mExploreInfo;
	byte[]					mBuf;
	
	public ImageCache(Context context, TextureBank bank){
		this.bank = bank;
		mBuf = new byte[1024];
		String datafolder = context.getString(R.string.dataFolder);
		datafolder = Environment.getExternalStorageDirectory().getAbsolutePath() + datafolder;
		mExploreInfoFolder = datafolder + context.getString(R.string.exploreFolder);
		mExploreFolder = mExploreInfoFolder + "/bmp";
		mRand = new Random(new Date().getTime());
		mExploreInfo = new File(mExploreInfoFolder);
		mExplore = new File(mExploreFolder);
		//mExplore = ShowStreams.current.getFileStreamPath(mExploreFolder);
		mExploreInfo.mkdirs(); // Make dir if not exists
		mExplore.mkdirs(); // Make dir if not exists
		File[] exploreInfoArray = mExploreInfo.listFiles();
		if(exploreInfoArray == null) return;
		mFiles = new ArrayList<File>(exploreInfoArray.length);
		mCached = new HashMap<String, Integer>(exploreInfoArray.length);
		Log.v("Floating Image", exploreInfoArray.length + " files in cache.");
		for(int i = 0; i < exploreInfoArray.length; ++i){
			File f = exploreInfoArray[i];
			if(f == null) break;
			if(!f.isDirectory()){
				addFile(f);
			}
		}
	}
	
	/*
	public void run(){
		if(mExploreFiles == null) return;
		while(true){
			if(bank.stopThreads) return;
			while(bank.cached.size() < 5 && !mExploreFiles.isEmpty()){
				if(bank.stopThreads) return;
				bank.addOldBitmap(getRandomExplore());
			}
			try {
				synchronized (bank.cached) {
					bank.cached.wait();
				}
			} catch (InterruptedException e) {
				Log.v("Bitmap downloader", "*** Stopping asynchronous cache thread", e);
				return;
			}
		}
	}
	*/
	// Will not delete directory, just fail when trying to...
	public void cleanCache(){
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
			mFiles.clear();
			int entries = Math.min(limit, files.length);
			for(int i = 0; i < entries; ++i){
				addFile(files[i]);
			}
		}catch(Exception e){
			Log.w("Floating Image", "Error removing old images...", e);
		}
	}
	
	public void saveImage(ImageReference ir){
		if(ir == null) return;
		String name = ir.getID();
		if(exists(name)) return;
		try {			
			File f = new File(mExplore.getPath() + "/" + name + ".jpg");
			FileOutputStream fos = new FileOutputStream(f);
			ir.getBitmap().compress(CompressFormat.JPEG, 85, fos);
			fos.flush();
			fos.close();
			File f_info = updateMeta(ir);
			if(f_info == null){
				f.delete();
			}else{
				synchronized(mFiles){
					addFile(f_info);
					mCached.put(f_info.getName(), mFiles.size() - 1);
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
			String thumbPath = mExplore.getPath() + "/" + name + ".jpg";
			File f_info = new File(mExploreInfo.getPath() + "/" + name + ".info");
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
		mFiles.add(file);
		mCached.put(file.getName(), mFiles.size() - 1);
	}
	/*
	public ImageReference getRandomExplore(){
		if(mExploreFiles.size() == 0 || (!mActive || !Settings.useCache)){
			try {
				Thread.sleep(10000); 	// Sleep for a bit, we're not doing anything anyway...
										// TODO: Make this wait() instead!
			} catch (InterruptedException e) {
				Log.w("ImageCache", "We were interrupted!");
			}
			return null;
		}
		Log.v("Image cache", "Adding random cached image");
		return getFile(mRand.nextInt(mExploreFiles.size()));
	}
	*/
	public ImageReference getFile(int index, ImageReference ir){
		try {
			File f_info = mFiles.get(index);
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
				//return getRandomExplore();
				return null;
			}
			// Read bitmap
			Bitmap bmp = BitmapFactory.decodeFile(bmpName);
			
			// Fill image reference
			ir.parseInfo(tokens, bmp);
			
			// Clean up
			return ir;
		} catch (FileNotFoundException e) {
			//mExploreFiles.remove(index);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public boolean exists(String name){
		if(mFiles == null) return false;
		synchronized(mFiles){
			if(mCached.containsKey(name + ".info")){
				return true;
			}
			return false;
		}
	}
	
	public void addImage(ImageReference ir, boolean next){
		bank.addBitmap(getFile(mCached.get(ir.getID() + ".info"), ir), false, next);
	}
}
