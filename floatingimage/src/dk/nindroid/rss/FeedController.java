package dk.nindroid.rss;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.util.Log;
import dk.nindroid.rss.data.FeedReference;
import dk.nindroid.rss.data.ImageReference;
import dk.nindroid.rss.data.LocalImage;
import dk.nindroid.rss.parser.FeedParser;
import dk.nindroid.rss.parser.ParserProvider;
import dk.nindroid.rss.settings.FeedsDbAdapter;
import dk.nindroid.rss.settings.Settings;

public class FeedController {
	private List<List<ImageReference>> 		mReferences;
	private List<Integer>					mFeedIndex;
	private List<FeedReference>				mFeeds;
	private Random 							mRand = new Random(System.currentTimeMillis());
	private RiverRenderer					mRenderer;
	
	public FeedController(){
		mFeeds = new ArrayList<FeedReference>();
		mFeedIndex = new ArrayList<Integer>();
		mReferences = new ArrayList<List<ImageReference>>();
	}
	
	public void setRenderer(RiverRenderer renderer){
		this.mRenderer = renderer;
	}
	
	public ImageReference getImageReference(){
		ImageReference ir = null;
		synchronized (mReferences) {
			if(mReferences.size() == 0) return null;
			int thisFeed = getFeed();
			List<ImageReference> feed = mReferences.get(thisFeed);
			int index = (mFeedIndex.get(thisFeed) + 1) % feed.size();
			ir = feed.get(index);
			try{
				mFeedIndex.set(thisFeed, index);
			}catch(Exception e){
				Log.v("FeedController", "Could not save position. FeedIndex size: " + mFeedIndex.size() + ", trying to set index: " + index, e);
			}
		}
		return ir;
	}
	
	public int getFeed(){
		float rand = mRand.nextFloat();
		int feeds = mReferences.size();
		float[] fraction = new float[feeds];
		int total = 0;
		for(int i = 0; i < feeds; ++i){
			total += mReferences.get(i).size();
			fraction[i] = total;
		}
		for(int i = 0; i < feeds; ++i){
			fraction[i] /= total;
			if(fraction[i] > rand){
				return i;
			}
		}
		return mReferences.size() - 1;
	}
	
	public void readFeeds(){
		List<FeedReference> newFeeds = new ArrayList<FeedReference>();
		FeedsDbAdapter mDbHelper = new FeedsDbAdapter(ShowStreams.current);
		SharedPreferences sp = ShowStreams.current.getSharedPreferences("dk.nindroid.rss_preferences", 0);
		mDbHelper.open();
		Cursor c = null;
		try{
			c = mDbHelper.fetchAllFeeds();
			int typei = c.getColumnIndex(FeedsDbAdapter.KEY_TYPE);
			int feedi = c.getColumnIndex(FeedsDbAdapter.KEY_URI);
			int namei = c.getColumnIndex(FeedsDbAdapter.KEY_TITLE);
			int idi   = c.getColumnIndex(FeedsDbAdapter.KEY_ROWID);
			while(c.moveToNext()){
				int type = c.getInt(typei); 
				String feed = c.getString(feedi);
				String name = c.getString(namei);
				int id = c.getInt(idi);
				boolean enabled = sp.getBoolean("feed_" + Integer.toString(id), true);
				// Only add a single feed once!
				if(enabled && !newFeeds.contains(feed)){
					newFeeds.add(getFeedReference(feed, type, name));
				}
			}
		}catch(Exception e){
			Log.e("FeedController", "Unhandled exception caught", e);
		}finally{
			if(c != null){
				ShowStreams.current.stopManagingCursor(c);
				c.close();
			}
			mDbHelper.close();
		}
		
		// Make sure local feeds are read first - they load crazy fast! :)
		Collections.sort(newFeeds, new Comparator<FeedReference>() {
			@Override
			public int compare(FeedReference a, FeedReference b) {
				int tA = a.getType(); int tB = b.getType();
				if (tA < tB) return -1;
				if (tA > tB) return 1;
				return 0;
			}
		});
		
		synchronized(mFeeds){
			if(mFeeds.size() != newFeeds.size()){
				feedsChanged();
			}else{
				for(int i = 0; i < mFeeds.size(); ++i){
					if(!(mFeeds.get(i).equals(newFeeds.get(i)))){
						feedsChanged();
						break;
					}
				}
			}
			
			mFeeds = newFeeds;
			mFeedIndex.clear();
			parseFeeds();
		}
	}
	
	private void feedsChanged(){
		if(mRenderer != null){
			mRenderer.resetImages();
		}
	}
	
	private FeedParser getParser(int feedType){
		if(feedType == Settings.TYPE_LOCAL){
			return null;
		}
		return ParserProvider.getParser(feedType);
	}
	
	public int getShowing(){
		int count = 0;
		for(List<ImageReference> list : mReferences){
			count += list.size();
		}
		return count;
	}
	
	public FeedReference getFeedReference(String path, int type, String name){
		return new FeedReference(getParser(type), path, name, type);
	}
	
	// False if no images.
	private synchronized boolean parseFeeds(){
		synchronized(mFeeds){
			mReferences.clear();
			mFeedIndex.clear();
			int progress = 0;
			mRenderer.setFeeds(0, mFeeds.size());
			for(FeedReference feed : mFeeds){
				List<ImageReference> references = null;
				if(feed.getType() == Settings.TYPE_LOCAL){
					references = readLocalFeed(feed);
				}else{
					int i = 5;
					while(i --> 0){ // Oh glorious obfuscation! :D
						try{
							references = parseFeed(feed);
							break;
						}catch (Exception e){
							Log.w("FeedController", "Failed getting feed, retrying...", e);
						}
					}
				}
				if(references != null && references.size() != 0){
					if(Settings.shuffleImages){
						Collections.shuffle(references);
					}
					synchronized(mReferences){
						mReferences.add(references); // These two 
						mFeedIndex.add(-1);			// are in sync!
					}
				}else{
					Log.w("FeedController", "Reading feed failed too many times, giving up!");
				}
				mRenderer.setFeeds(++progress, mFeeds.size());
			}
			Log.v("Floating Image", "Showing images from " + mReferences.size() + " feeds");
			return mReferences.size() > 0;
		}
	}
	
	private static List<ImageReference> parseFeed(FeedReference feed){
		try {
			return feed.getParser().parseFeed(feed);
		} catch (IOException e) {
			Log.e("FeedController", "Unexpected exception caught", e);
		} catch (ParserConfigurationException e) {
			Log.e("FeedController", "Unexpected exception caught", e);
		} catch (SAXException e) {
			Log.e("FeedController", "Unexpected exception caught", e);
		} catch (FactoryConfigurationError e) {
			Log.e("FeedController", "Unexpected exception caught", e);
		}
		return null;
	}
	
	// LOCAL
	private static List<ImageReference> readLocalFeed(FeedReference feed){
		File f = new File(feed.getFeedLocation());
		List<ImageReference> images = new ArrayList<ImageReference>();
		if(f.exists()){
			buildImageIndex(images, f, 0);
		}
		return images;
	}
	
	private static void buildImageIndex(List<ImageReference> images, File dir, int level){
		File[] files = dir.listFiles();
		if(files == null || files.length == 0) 
			return;
		if(!Settings.shuffleImages){
			Arrays.sort(files, new InverseComparator()); // Show last items first
		}
		for(File f : files){
			if(f.getName().charAt(0) == '.') continue; // Drop hidden files.
			if(f.isDirectory() && level < 20){ // Some high number to avoid any infinite loops...
				buildImageIndex(images, f, level + 1);
			}else{
				if(isImage(f)){
					ImageReference ir = new LocalImage(f);
					images.add(ir);
				}
			}
		}
	}
	
	private static class InverseComparator implements Comparator<File>{

		@Override
		public int compare(File arg0, File arg1) {
			return arg1.compareTo(arg0);
		}
		
	}
	
	private static boolean isImage(File f){
		String filename = f.getName();
		String extension = filename.substring(filename.lastIndexOf('.') + 1);
		if("jpg".equalsIgnoreCase(extension) || "jpeg".equalsIgnoreCase(extension) || "png".equalsIgnoreCase(extension)){
			return true;
		}
		return false;
	}
}
