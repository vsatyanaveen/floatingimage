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
	//private static long						REFRESH_INTERVAL = 120000; // Every other hour;
	private static long						RETRY_INTERVAL = 30000; // Every half minute;
	private long							mLastFeedRead = 0;
	
	private List<ImageReference> 			mReferences;
	private List<FeedReference>				mFeeds;
	private PositionInterval				mPosition;
	private Random 							mRand = new Random(System.currentTimeMillis());
	private RiverRenderer					mRenderer;
	private MainActivity					mActivity;
	private int								mCachedActive;
	
	private int								mForceFeedId = -1;
	
	public interface EventSubscriber{
		public void feedsUpdated();
	}
	
	public void showFeed(int id){
		this.mForceFeedId = id;
	}
	
	private List<EventSubscriber> eventSubscribers;
	
	public FeedController(MainActivity activity){
		mFeeds = new ArrayList<FeedReference>();
		mReferences = new ArrayList<ImageReference>();
		mActivity = activity;
		eventSubscribers = new ArrayList<EventSubscriber>();
	}
	
	public void addSubscriber(EventSubscriber subscriber){
		eventSubscribers.add(subscriber);
	}
	
	private void onFeedsUpdated(){
		for(EventSubscriber es : eventSubscribers){
			es.feedsUpdated();
		}
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
		
		if(System.currentTimeMillis() - mLastFeedRead > RETRY_INTERVAL && mReferences.size() == 0){
			Log.v("Floating Image", "No pictures are showing, trying to read again.");
			readFeeds(mCachedActive);
		}
		
		if(mReferences.size() == 0) return null;
		synchronized (mReferences) {
			if(System.currentTimeMillis() - mLastFeedRead > REFRESH_INTERVAL){
				Log.v("Floating Image", "Refreshing feeds.");
				readFeeds(mCachedActive);
			}
			
			if(mReferences.size() != 0){
				int index = forward ? mPosition.getNext() : mPosition.getPrev();
				ir = mReferences.get(index);
			}
		}
		return ir;
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
			int sortingi = c.getColumnIndex(FeedsDbAdapter.KEY_SORTING);
			while(c.moveToNext()){
				int type = c.getInt(typei); 
				String feed = c.getString(feedi);
				String name = c.getString(namei);
				int id = c.getInt(idi);
				int sorting = c.getInt(sortingi);
				boolean enabled;
				if(mForceFeedId == -1){
					enabled = sp.getBoolean("feed_" + Integer.toString(id), true);
				}else{
					enabled = mForceFeedId == id;
				}
				// Only add a single feed once!
				if(enabled && !newFeeds.contains(feed)){
					newFeeds.add(getFeedReference(feed, type, name, sorting));
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
			if(mFeeds.size() != newFeeds.size() || mReferences.size() == 0){
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
				onFeedsUpdated();
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
		return mReferences.size();
	}
	
	public FeedReference getFeedReference(String path, int type, String name, int sorting){
		return new FeedReference(getParser(type), path, name, type, sorting);
	}
	
	// False if no images.
	private synchronized boolean parseFeeds(int active){
		synchronized(mFeeds){
			List<List<ImageReference>> refs = new ArrayList<List<ImageReference>>();
			int progress = 0;
			mRenderer.setFeeds(0, mFeeds.size());
			for(FeedReference feed : mFeeds){
				if(feed.getType() == Settings.TYPE_UNKNOWN){
					Log.e("Floating Image", "Unknown feed encountered: " + feed.getName() + ", ignoring.");
					mRenderer.setFeeds(++progress, mFeeds.size());
					continue;
				}
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
							Log.w("Floating Image", "Failed getting feed, retrying...", e);
						}
					}
				}
				if(references != null){
					if(references.size() != 0){
						sortFeed(feed, references);
					}
					Log.v("Floating Image", "Adding feed: " + references.size() + " images");
					refs.add(references);
				}else{
					Log.w("Floating Image", "Reading feed failed too many times, giving up!");
				}
				mRenderer.setFeeds(++progress, mFeeds.size());
			}
			joinFeeds(refs, active);
			return mReferences.size() > 0;
		}
	}
	
	void sortFeed(FeedReference feed, List<ImageReference> refs){
		if(feed.getType() == Settings.TYPE_LOCAL){
			switch(feed.getSorting()){
			case 0: // Name
				Collections.sort(refs, new NameComparator(false));
				break;
			case 1: // Name reversed
				Collections.sort(refs, new NameComparator(true));
				break;
			case 2: // Date
				Collections.sort(refs, new FileDateComparator(false));
				break;
			case 3: // Date reversed
				Collections.sort(refs, new FileDateComparator(true));
				break;
			case 4: // Random
				Collections.shuffle(refs);
				break;
			}
		}else{
			switch(feed.getSorting()){
			case 0: // Name
				Collections.sort(refs, new NameComparator(false));
				break;
			case 1: // Name reversed
				Collections.sort(refs, new NameComparator(true));
				break;
			case 2: // Date
				// Do nothing
				break;
			case 3: // Date reversed
				Collections.reverse(refs);
				break;
			case 4: // Random
				Collections.shuffle(refs);
				break;
			}
		}
	}
	
	class FileDateComparator implements Comparator<ImageReference>{
		boolean reverse;
		public FileDateComparator(boolean reverse) {
			this.reverse = reverse;
		}
		
		@Override
		public int compare(ImageReference lhs, ImageReference rhs) {
			File a = ((LocalImage)lhs).getFile();
			File b = ((LocalImage)rhs).getFile();
			
			int res;
			
			if(a.lastModified() == b.lastModified()){
				res = 0;
			}else{
				res = a.lastModified() < b.lastModified() ? -1 : 1;
			}
			
			if(reverse){
				res *= -1;
			}
			return res;
		}
	}
	
	class NameComparator implements Comparator<ImageReference>{
		boolean reverse;
		public NameComparator(boolean reverse) {
			this.reverse = reverse;
		}
		
		@Override
		public int compare(ImageReference lhs, ImageReference rhs) {
			int res = lhs.getTitle().compareTo(rhs.getTitle());
			if(reverse){
				res *= -1;
			}
			return res;
		}
	}
	
	private void joinFeeds(List<List<ImageReference>> refs, int active){
		List<Integer> feedPos = new ArrayList<Integer>(refs.size());
		for(int i = 0; i < refs.size(); ++i){
			feedPos.add(-1);
		}
		synchronized (mReferences) {
			mReferences.clear();
			while(refs.size() > 0){
				int feedIndex = getFeed(refs);
				if(feedIndex < refs.size()){
					int irIndex = feedPos.get(feedIndex) + 1;
					if(irIndex == refs.get(feedIndex).size()){
						refs.remove(feedIndex);
						feedPos.remove(feedIndex);
					}else{
						mReferences.add(refs.get(feedIndex).get(irIndex));
						feedPos.set(feedIndex, irIndex);
					}
				}
			}
			mPosition = new PositionInterval(active, mReferences.size());
		}
	}
	
	public int getFeed(List<List<ImageReference>> refs){
		float rand = mRand.nextFloat();
		int feeds = refs.size();
		float[] fraction = new float[feeds];
		int total = 0;
		for(int i = 0; i < feeds; ++i){
			total += refs.get(i).size();
			fraction[i] = total;
		}
		for(int i = 0; i < feeds; ++i){
			fraction[i] /= total;
			if(fraction[i] > rand){
				return i;
			}
		}
		return refs.size() - 1;
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
			//Log.v("Floating Image", "Get next: " + b);
			return b;
		}
		
		public int getPrev(){
			a = ((a - 1) + space) % space;
			if(isSpread()){
				b = ((b - 1) + space) % space;
			}
			//Log.v("Floating Image", "Get prev: " + a);
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
