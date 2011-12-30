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
import android.database.SQLException;
import android.util.Log;
import dk.nindroid.rss.data.FeedReference;
import dk.nindroid.rss.data.ImageReference;
import dk.nindroid.rss.data.KeyVal;
import dk.nindroid.rss.data.LocalImage;
import dk.nindroid.rss.parser.FeedParser;
import dk.nindroid.rss.parser.ParserProvider;
import dk.nindroid.rss.settings.FeedSettings;
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
	private List<FeedReference>				mFailedFeeds;
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
	
	public int getFeedSize(){
		return mReferences.size();
	}
	
	public int findImageIndex(String id){
		for(int i = 0; i < mReferences.size(); ++i){
			ImageReference ir = mReferences.get(i);
			if(ir.getID().equals(id)){
				return i;
			}
		}
		return -1;
	}
	
	private List<EventSubscriber> eventSubscribers;
	
	public FeedController(MainActivity activity){
		mFeeds = new ArrayList<FeedReference>();
		mReferences = new ArrayList<ImageReference>();
		mActivity = activity;
		eventSubscribers = new ArrayList<EventSubscriber>();
		mFailedFeeds = new ArrayList<FeedReference>();
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
	
	public ImageReference getImageReference(long position){
		if(System.currentTimeMillis() - mLastFeedRead > RETRY_INTERVAL && (mReferences.size() == 0 || mFailedFeeds.size() > 0)){
			Log.v("Floating Image", "No pictures are showing, trying to read again.");
			synchronized (mReferences) {
				readFeeds(mCachedActive);
			}
		}
		int refs = mReferences.size();
		if(refs == 0) return null;
		
		synchronized (mReferences) {
			if(System.currentTimeMillis() - mLastFeedRead > REFRESH_INTERVAL){
				Log.v("Floating Image", "Refreshing feeds.");
				readFeeds(mCachedActive);
			}
			
			if(refs != 0){
				if(mForceFeedId != -1){
					if(position < 0 || position >= mReferences.size()){
						return null;
					}
				}
				position = (position + mReferences.size() * 100) % refs;
				return mReferences.get((int)position);
			}
		}
		return null;
	}
	
	public ImageReference getNextImageReference(){
		return getImageReference(true);
	}
	
	public ImageReference getPrevImageReference(){
		return getImageReference(false);
	}
	
	public void resetCounter(){
		mPosition = new PositionInterval(mCachedActive, mReferences.size());
	}
	
	public ImageReference getImageReference(boolean forward){
		ImageReference ir = null;
		
		if(System.currentTimeMillis() - mLastFeedRead > RETRY_INTERVAL && (mReferences.size() == 0 || mFailedFeeds.size() > 0)){
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
				if(mForceFeedId != -1){
					int pass = forward ? mPosition.getNextPass() : mPosition.getPrevPass();
					if(pass != 0){
						return null;
					}
				}
				ir = mReferences.get(index);
			}
		}
		return ir;
	}
		
	public void readFeeds(int active){
		mCachedActive = active;
		mLastFeedRead = System.currentTimeMillis();
		mFailedFeeds.clear();
		
		List<FeedReference> newFeeds = new ArrayList<FeedReference>();
		FeedsDbAdapter mDbHelper = new FeedsDbAdapter(mActivity.context());
		SharedPreferences sp = mActivity.context().getSharedPreferences(mActivity.getSettingsKey(), 0);
		try{
			mDbHelper.open();
		}catch(SQLException e){
			Log.w("Floating Image", "Database could not be opened", e);
			mReferences.clear(); // Force retry
			return;
		}
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
					enabled = sp.getBoolean("feed_" + Integer.toString(id), false);
				}else{
					enabled = mForceFeedId == id;
				}
				// Only add a single feed once!
				if(enabled && !newFeeds.contains(feed)){
					newFeeds.add(getFeedReference(id, feed, type, name, sorting));
				}
			}
		}catch(Exception e){
			Log.e("FeedController", "Unhandled exception caught", e);
		}finally{
			if(c != null){
				mActivity.stopManagingCursor(c);
				c.close();
			}
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
				reparseFeeds = true;
			}else{
				for(int i = 0; i < mFeeds.size(); ++i){
					if(!(mFeeds.get(i).equals(newFeeds.get(i)))){
						reparseFeeds = true;
						break;
					}
				}
			}
			if(reparseFeeds){
				mFeeds = newFeeds;
				mReferences.clear();
				parseFeeds(mDbHelper, active);
				feedsChanged();
				onFeedsUpdated();
				if(mReferences.size() == 0){
					mActivity.showNoImagesWarning();
				}
			}
			
		}
		mDbHelper.close();
	}
	
	private void feedsChanged(){
		if(mRenderer != null){
			//mRenderer.resetImages();
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
	
	public FeedReference getFeedReference(int id, String path, int type, String name, int sorting){
		return new FeedReference(id, getParser(type), path, name, type, sorting);
	}
	
	// False if no images.
	private synchronized boolean parseFeeds(FeedsDbAdapter db, int active){
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
					references = readLocalFeed(feed, db);
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
					mFailedFeeds.add(feed);
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
						ImageReference ref = refs.get(feedIndex).get(irIndex);
						ref.setFeedPosition(mReferences.size());
						mReferences.add(ref);
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
			FeedParser parser = feed.getParser();
			parser.init(mActivity.getSettings());
			return parser.parseFeed(feed, mActivity.context());
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
	private List<ImageReference> readLocalFeed(FeedReference feed, FeedsDbAdapter db){
		File f = new File(feed.getFeedLocation());
		List<String> recurse = getRecurseList(db, feed, f);
		List<ImageReference> images = new ArrayList<ImageReference>();
		if(f.exists()){
			buildImageIndex(images, f, recurse, 0);
		}
		return images;
	}
	
	// Get list of immediate child directories to search for images in
	List<String> getRecurseList(FeedsDbAdapter db, FeedReference feed, File f){
		List<String> dirs = new ArrayList<String>();
		File[] allDirs = f.listFiles(new FeedSettings.DirFilter());
		if(allDirs == null){
			return null;
		}
		SharedPreferences sp = mActivity.context().getSharedPreferences(dk.nindroid.rss.menu.Settings.SHARED_PREFS_NAME, 0);
		if(sp.getBoolean("feed_allsub_" + feed.getId(), false)){
			Log.v("Floating Image", "Reading all sub directories");
			for(File dir : allDirs){
				dirs.add(dir.getName());
			}
			return dirs;
		}
		
		if(allDirs != null){
			Cursor c = db.getSubDirs(feed.getId());
			List<KeyVal<String, Boolean>> saved = new ArrayList<KeyVal<String,Boolean>>();
			int iDir = c.getColumnIndex(FeedsDbAdapter.KEY_DIR);
			int iEnabled = c.getColumnIndex(FeedsDbAdapter.KEY_ENABLED);
			while(c.moveToNext()){
				String dir = c.getString(iDir);
				boolean enabled = c.getInt(iEnabled) == 1;
				saved.add(new KeyVal<String, Boolean>(dir, enabled));
			}
			for(File dir : allDirs){
				KeyVal<String, Boolean> kv = FeedSettings.find(saved, dir.getName());
				if(kv == null || kv.getVal()){
					dirs.add(dir.getName());
				}
			}
			c.close();
		}
		return dirs;
	}
	
	private void buildImageIndex(List<ImageReference> images, File dir, List<String> recurse, int level){
		File[] files = dir.listFiles();
		if(files == null || files.length == 0) 
			return;
		if(!mActivity.getSettings().shuffleImages){
			Arrays.sort(files, new InverseComparator()); // Show last items first
		}
		for(File f : files){
			if(f.getName().charAt(0) == '.') continue; // Drop hidden files.
			if(f.isDirectory() && level < 20){ // Some high number to avoid any infinite loops...
				if(recurse != null && recurse.contains(f.getName())){
					Log.v("Floating Image", "Recursing through " + f.getName());
					buildImageIndex(images, f, null, level + 1);
				}
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
	
	public static boolean isImage(File f){
		String filename = f.getName();
		return isImage(filename);
	}
	
	public static boolean isImage(String filename){
		int extensionIndex = filename.lastIndexOf('.');
		if(extensionIndex != -1){
			String extension = filename.substring(extensionIndex + 1);
			if("jpg".equalsIgnoreCase(extension) || "jpeg".equalsIgnoreCase(extension) || "png".equalsIgnoreCase(extension) || "gif".equalsIgnoreCase(extension)){
				return true;
			}
		}
		return false;
	}
	
	private class PositionInterval{
		int a;
		int aPass;
		int b;
		int bPass;
		int space;
		int interval;
				
		private boolean isSpread(){
			int y = this.b;
			y = (b - a + space) % space;
			return y == interval;
		}
		
		public int getNext(){
			int oldB = b;
			b = (b + 1) % space;
			if(b < oldB){
				++bPass;
			}
			
			if(isSpread()){
				int oldA = a;
				a = (a + 1) % space;
				if(a < oldA){
					++aPass;
				}
			}
			return b;
		}
		
		public int getPrev(){
			int oldA = a;
			a = ((a - 1) + space) % space;
			if(a > oldA){
				--aPass;
			}
			
			if(isSpread()){
				int oldB = b;
				b = ((b - 1) + space) % space;
				if(b > oldB){
					--bPass;
				}
			}
			return a;
		}
		
		public int getNextPass(){
			return bPass;
		}
		
		public int getPrevPass(){
			return aPass;
		}
		
		public PositionInterval(int intervalLength, int space){
			this.space = space;
			this.interval = intervalLength;
			b = space - 1;
			a = 0;
			aPass = 0;
			bPass = -1;
		}
	}
}
