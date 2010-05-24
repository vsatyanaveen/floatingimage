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

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.os.Environment;
import android.util.Log;
import dk.nindroid.rss.data.FileDateReverseComparator;
import dk.nindroid.rss.data.ImageReference;
import dk.nindroid.rss.settings.Settings;

public class ImageCache implements Runnable {
	TextureBank bank;
	private final String 	mExploreFolder;
	private final String 	mExploreInfoFolder;
	private boolean			mActive = true;
	List<File>				mExploreFiles;
	Map<String, Integer>	mCached;
	Random 					mRand;
	File 					mExplore;
	File 					mExploreInfo;
	
	public ImageCache(TextureBank bank){
		this.bank = bank;
		String datafolder = ShowStreams.current.getString(R.string.dataFolder);
		datafolder = Environment.getExternalStorageDirectory().getAbsolutePath() + datafolder;
		mExploreInfoFolder = datafolder + ShowStreams.current.getString(R.string.exploreFolder);
		mExploreFolder = mExploreInfoFolder + "/bmp";
		mRand = new Random(new Date().getTime());
		mExploreInfo = new File(mExploreInfoFolder);
		mExplore = new File(mExploreFolder);
		//mExplore = ShowStreams.current.getFileStreamPath(mExploreFolder);
		mExploreInfo.mkdirs(); // Make dir if not exists
		mExplore.mkdirs(); // Make dir if not exists
		File[] exploreInfoArray = mExploreInfo.listFiles();
		if(exploreInfoArray == null) return;
		mExploreFiles = new ArrayList<File>(exploreInfoArray.length);
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
	
	public void pause(){
		mActive = false;
	}
	
	public void resume(){
		mActive = true;
	}
	
	public void run(){
		if(mExploreFiles == null) return;
		while(true){
			if(bank.stopThreads) return;
			while(bank.cached.size() < bank.textureCache && !mExploreFiles.isEmpty()){
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
					Log.w("dk.nindroid.rss.ImageCache", "Unexpected null pointer exception caught.", e);
					files[i].delete();
					files[i] = null;
				}
			}
			mExploreFiles.clear();
			int entries = Math.min(limit, files.length);
			for(int i = 0; i < entries; ++i){
				addFile(files[i]);
			}
		}catch(IOException e){
			Log.w("dk.nindroid.rss.ImageCache", "Error removing old images...", e);
		}
	}
	
	public void saveExploreImage(ImageReference ir){
		if(ir == null) return;
		String name = ir.getID();
		try {			
			File f = new File(mExplore.getPath() + "/" + name + ".jpg");
			FileOutputStream fos = new FileOutputStream(f);
			ir.getBitmap().compress(CompressFormat.JPEG, 75, fos);
			fos.flush();
			fos.close();
			File f_info = new File(mExploreInfo.getPath() + "/" + name + ".info");
			FileOutputStream fos_info = new FileOutputStream(f_info);
			fos_info.write((f.getAbsolutePath() + "\n" + ir.getInfo()).getBytes());
			fos_info.flush();
			fos_info.close();
			synchronized(mExploreFiles){
				addFile(f_info);
				mCached.put(f_info.getName(), mExploreFiles.size() - 1);
			}
		} catch (FileNotFoundException e) {
			Log.w("dk.nindroid.rss.ImageCache", "Image could not be cached", e);
			return;
		} catch (IOException e) {
			Log.w("dk.nindroid.rss.ImageCache", "Image could not be cached", e);
			return;
		}
	}
	
	protected void addFile(File file){
		mExploreFiles.add(file);
		mCached.put(file.getName(), mExploreFiles.size() - 1);
	}
	
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
	
	public ImageReference getFile(int index){
		InputStream is;
		try {
			File f_info = mExploreFiles.get(index);
			InputStream is_info = new FileInputStream(f_info);
			BufferedInputStream bis_info = new BufferedInputStream(is_info, 256);
			DataInputStream dis = new DataInputStream(bis_info);
			String bmpName = dis.readLine();
			if(bmpName == null){
				dis.close();
				bis_info.close();
				f_info.delete();
				return getRandomExplore();
			}
			// Read bitmap
			File bmpFile = new File(bmpName);
			is = new FileInputStream(bmpFile);
			BufferedInputStream bis = new BufferedInputStream(is, 2048);
			Bitmap bmp = BitmapFactory.decodeStream(bis);
			
			// Get image reference
			ImageReference ir = ImageTypeResolver.getReference(dis);
			if(ir != null){
				ir.parseInfo(dis, bmp);
			}
			
			// Clean up
			dis.close();
			bis_info.close();
			bis.close();
			return ir;
		} catch (FileNotFoundException e) {
			//mExploreFiles.remove(index);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public boolean exists(String name){
		if(mExploreFiles == null) return false;
		synchronized(mExploreFiles){
			if(mCached.containsKey(name + ".info")){
				return true;
			}
			return false;
		}
	}
	
	public void addImage(String name){
		Log.v("Image cache", "Adding cached image by request");
		bank.addNewBitmap(getFile(mCached.get(name + ".info")));
	}
}
