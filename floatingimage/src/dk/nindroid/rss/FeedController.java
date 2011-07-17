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
import dk.nindroid.rss.uiActivities.Toaster;

public class FeedController {
	private static long						REFRESH_INTERVAL = 7200000; // Every other hour;
	private static long						RETRY_INTERVAL = 30000; // Every half minute;
	private long							mLastFeedRead = 0;
	
	private List<List<ImageReference>> 		mReferences;
	//private List<Integer>					mFeedIndex;
	private List<FeedReference>				mFeeds;
	private List<PositionInterval>			mPositions;
	private Random 							mRand = new Random(System.currentTimeMillis());
	private RiverRenderer					mRenderer;
	private MainActivity					mActivity;
	private int								mCachedActive;
	
	public FeedController(MainActivity activity){
		mFeeds = new ArrayList<FeedReference>();
		mPositions = new ArrayList<PositionInterval>();
		mReferences = new ArrayList<List<ImageReference>>();
		mActivity = activity;
	}
	
	public void setRenderer(RiverRenderer renderer){
		this.mRenderer = renderer;
	}
	
	public ImageReference getNextImageReference(){
		return getImageReference(true);
	}
	
	public ImageReference getPrevImageReference(){
		return getImageReference(false);
	}
	
	public ImageReference getImageReference(boolean forward){
		ImageReference ir = null;
		synchronized (mReferences) {
			if(mReferences.size() == 0) return null;
			if(System.currentTimeMillis() - mLastFeedRead > REFRESH_INTERVAL){
				Log.v("Floating Image", "Refreshing feeds.");
				readFeeds(mCachedActive);
			}
			int thisFeed = getFeed();
			List<ImageReference> feed = mReferences.get(thisFeed);
			
			if(mFeeds.get(thisFeed).getType() == Settings.TYPE_LOCAL && feed.size() == 0 && System.currentTimeMillis() - mLastFeedRead > RETRY_INTERVAL ){
 				Log.v("Floating Image", "A local feed is of zero length, trying to read again.");
				mLastFeedRead = System.currentTimeMillis();
				feed = readLocalFeed(mFeeds.get(thisFeed));
				mReferences.set(thisFeed, feed);
				mPositions.set(thisFeed, new PositionInterval(mCachedActive, feed.size()));
			}
			if(feed.size() != 0){
				int index = forward ? mPositions.get(thisFeed).getNext() : mPositions.get(thisFeed).getPrev();
				ir = feed.get(index);
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
	
	public void readFeeds(int active){
		mCachedActive = active;
		mLastFeedRead = System.currentTimeMillis();
		List<FeedReference> newFeeds = new ArrayList<FeedReference>();
		FeedsDbAdapter mDbHelper = new FeedsDbAdapter(mActivity.context());
		SharedPreferences sp = mActivity.context().getSharedPreferences(mActivity.getSettingsKey(), 0);
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
				mActivity.stopManagingCursor(c);
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
			boolean reparseFeeds = false;
			if(mFeeds.size() != newFeeds.size()){
				feedsChanged();
				reparseFeeds = true;
			}else{
				for(int i = 0; i < mFeeds.size(); ++i){
					if(!(mFeeds.get(i).equals(newFeeds.get(i)))){
						feedsChanged();
						reparseFeeds = true;
						break;
					}
				}
			}
			if(reparseFeeds){
				mFeeds = newFeeds;
				parseFeeds(active);
			}
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
	private synchronized boolean parseFeeds(int active){
		synchronized(mFeeds){
			mReferences.clear();
			mPositions.clear();
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
				if(references != null){
					if(references.size() != 0){
						if(mActivity.getSettings().shuffleImages){
							Collections.shuffle(references);
						}
					}
					synchronized(mReferences){
						Log.v("Floating Image", "Adding feed: " + references.size() + " images");
						mReferences.add(references); 										// These two 
						mPositions.add(new PositionInterval(active, references.size()));	// are in sync!
					}
				}else{
					Log.w("FeedController", "Reading feed failed too many times, giving up!");
				}
				mRenderer.setFeeds(++progress, mFeeds.size());
			}
			return mReferences.size() > 0;
		}
	}
	
	private List<ImageReference> parseFeed(FeedReference feed){
		try {
			return feed.getParser().parseFeed(feed, mActivity.context());
		} catch (IOException e) {
			Log.e("Floating Image", "Unexpected exception caught", e);
		} catch (ParserConfigurationException e) {
			Log.e("Floating Image", "Unexpected exception caught", e);
		} catch (SAXException e) {
			Log.e("Floating Image", "Unexpected exception caught", e);
		} catch (FactoryConfigurationError e) {
			Log.e("Floating Image", "Unexpected exception caught", e);
		} catch (Throwable t){
			Log.e("Floating Image", "Too large feed received", t);
			String msg = mActivity.context().getString(R.string.cannot_read_feed) + "(" + feed.getName() + ")";
			mActivity.runOnUiThread(new Toaster(mActivity.context(), msg));
		}
		return null;
	}
	
	// LOCAL
	private List<ImageReference> readLocalFeed(FeedReference feed){
		File f = new File(feed.getFeedLocation());
		List<ImageReference> images = new ArrayList<ImageReference>();
		if(f.exists()){
			buildImageIndex(images, f, 0);
		}
		return images;
	}
	
	private void buildImageIndex(List<ImageReference> images, File dir, int level){
		File[] files = dir.listFiles();
		if(files == null || files.length == 0) 
			return;
		if(!mActivity.getSettings().shuffleImages){
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
					if(images.size() > 1500){
						Log.v("Floating Image", "Too many files (" + files.length + ") found, bailing!");
						return; // Some people are insane, bail at 1500 images!
					}
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
	
	private class PositionInterval{
		int a;
		int b;
		int space;
		int interval;
				
		private boolean isSpread(){
			int y = this.b;
			y = (b - a + space) % space;
			return y == interval;
		}
		
		public int getNext(){
			b = (b + 1) % space;
			if(isSpread()){
				a = (a + 1) % space;
			}
			return b;
		}
		
		public int getPrev(){
			a = ((a - 1) + space) % space;
			if(isSpread()){
				b = ((b - 1) + space) % space;
			}
			return a;
		}
		
		public PositionInterval(int intervalLength, int space){
			this.space = space;
			this.interval = intervalLength;
			b = space - 1;
			a = b;
		}
	}
}
