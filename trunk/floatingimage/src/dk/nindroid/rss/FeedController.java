package dk.nindroid.rss;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import android.database.Cursor;
import android.util.Log;
import dk.nindroid.rss.data.FeedReference;
import dk.nindroid.rss.data.ImageReference;
import dk.nindroid.rss.data.LocalImage;
import dk.nindroid.rss.flickr.FlickrFeeder;
import dk.nindroid.rss.parser.FeedParser;
import dk.nindroid.rss.parser.ParserProvider;
import dk.nindroid.rss.settings.FeedsDbAdapter;
import dk.nindroid.rss.settings.Settings;

public class FeedController {
	private List<List<ImageReference>> 		mReferences;
	private List<Integer>					mFeedIndex;
	private List<FeedReference>				mFeeds;
	private boolean showing = false;
	private int lastFeed = -1;
	
	public boolean isShowing(){
		return showing;
	}
	
	public FeedController(){
		mFeeds = new ArrayList<FeedReference>();
		mFeedIndex = new ArrayList<Integer>();
		mReferences = new ArrayList<List<ImageReference>>();
	}
	
	public ImageReference getImageReference(){
		ImageReference ir = null;
		if(mReferences.size() == 0) return null;
		int thisFeed = (lastFeed + 1) % mReferences.size();
		List<ImageReference> feed = mReferences.get(thisFeed); 
		int index = mFeedIndex.get(thisFeed);
		ir = feed.get(index);
		mFeedIndex.set(thisFeed, (index + 1) % feed.size());
		lastFeed = thisFeed;
		return ir;
	}
	
	public void readFeeds(){
		mFeeds.clear();
		mFeedIndex.clear();
		if(Settings.useRandom){
			mFeeds.add(getFeedReference(FlickrFeeder.getExplore(), Settings.TYPE_FLICKR, "Explore"));
		}
		FeedsDbAdapter mDbHelper = new FeedsDbAdapter(ShowStreams.current);
		
		mDbHelper.open();
		Cursor c = null;
		try{
			c = mDbHelper.fetchAllFeeds();
			while(c.moveToNext()){
				int type = c.getInt(3); 
				String feed = c.getString(2);
				String name = c.getString(1);
				
				// Only add a single feed once!
				if(!mFeeds.contains(feed)){
					mFeeds.add(getFeedReference(feed, type, name));
				}
			}
		}catch(Exception e){
			Log.e("Local feeder", "Unhandled exception caught", e);
		}finally{
			if(c != null){
				c.close();
				mDbHelper.close();
			}
		}
		showing = false;
		
		parseFeeds();
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
	
	public boolean showFeed(FeedReference feed){
		mFeeds.clear();
		mFeeds.add(feed);
		showing = true;
		lastFeed = -1;
		return parseFeeds();
	}
	
	public FeedReference getFeedReference(String path, int type, String name){
		return new FeedReference(getParser(type), path, name, type);
	}
	
	// False if no images.
	private synchronized boolean parseFeeds(){
		mReferences.clear();
		for(FeedReference feed : mFeeds){
			List<ImageReference> references = null;
			if(feed.getType() == Settings.TYPE_LOCAL){
				references = readLocalFeed(feed);
			}else{
				int i = 5;
				while(i-->0){
					try{
						references = parseFeed(feed);
						break;
					}catch (Exception e){
						Log.w("FeedController", "Failed getting feed, retrying...", e);
					}
				}
			}
			if(references != null){
				mReferences.add(references); // These two 
				mFeedIndex.add(0);			// are in sync!
			}else{
				Log.w("FeedController", "Reading feed failed too many times, giving up!");
			}
		}
		Log.v("FeedController", "Showing images from " + mReferences.size() + " feeds");
		return mReferences.size() > 0;
	}
	
	private static List<ImageReference> parseFeed(FeedReference feed){
		try {
			InputStream stream = HttpTools.openHttpConnection(feed.getFeedLocation());
			Log.v("FeedController", "Fetching stream: " + feed.getFeedLocation());
			return parseStream(stream, feed.getParser());
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
	
	private static List<ImageReference> parseStream(InputStream stream, FeedParser feedParser) throws ParserConfigurationException, SAXException, FactoryConfigurationError, IOException{
		SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
		XMLReader xmlReader = parser.getXMLReader();
		xmlReader.setContentHandler(feedParser);
		xmlReader.parse(new InputSource(stream));
		List<ImageReference> list = feedParser.getData();
		if(list != null){
			if(list.isEmpty()){
				return null;
			}
			Log.v("FeedController", list.size() + " photos found.");
		}
		return list;
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
		for(File f : dir.listFiles()){
			if(f.getName().charAt(0) == '.') continue; // Drop hidden files.
			if(f.isDirectory() && level < 20){ // Some high number to avoid any infinite loops...
				buildImageIndex(images, f, level + 1);
			}else{
				if(isImage(f)){
					ImageReference ir = new LocalImage(f, 0);
					images.add(ir);
				}
			}
		}
	}
	
	private static boolean isImage(File f){
		String filename = f.getName();
		String extension = filename.substring(filename.lastIndexOf('.') + 1);
		if("jpg".equalsIgnoreCase(extension) || "png".equalsIgnoreCase(extension)){
			return true;
		}
		return false;
	}
}
