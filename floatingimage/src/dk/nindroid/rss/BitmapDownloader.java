package dk.nindroid.rss;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Process;
import android.util.Log;
import dk.nindroid.rss.data.ImageReference;
import dk.nindroid.rss.data.Progress;
import dk.nindroid.rss.data.RssElement;
import dk.nindroid.rss.flickr.FlickrFeeder;
import dk.nindroid.rss.parser.RSSParser;
import dk.nindroid.rss.settings.Settings;

public class BitmapDownloader implements Runnable {
	TextureBank bank;
	Queue<ImageReference> imageFeed = new LinkedList<ImageReference>();
	List<String> feeds = new ArrayList<String>();
	public BitmapDownloader(TextureBank bank){
		this.bank = bank;
	}
	
	@Override
	public void run() {
		Log.v("Bitmap downloader", "*** Starting asynchronous downloader thread");
		Process.setThreadPriority(10);
		fillFeed();
		while(true){
			try{
				if(bank.stopThreads) return;
				while(bank.unseen.size() < bank.textureCache && !imageFeed.isEmpty()){
					if(bank.stopThreads){
						Log.v("Bitmap downloader", "*** Stopping asynchronous downloader thread per request");
						return;
					}
					ImageReference ir = imageFeed.poll();
					String url = ir.getSmallImageUrl();
					if(bank.doDownload(ir.getImageID())){
						Bitmap bmp = downloadImage(url, null);
						if(bmp == null){
							continue;
						}
						ir.set128Bitmap(bmp);
						ir.getExtended();
						bank.addNewBitmap(ir);
					}
				}
				synchronized (bank.unseen) {
					if(bank.stopThreads){
						Log.v("Bitmap downloader", "*** Stopping asynchronous downloader thread per request");
						return;
					}
					try {
						if(imageFeed.isEmpty()){
							bank.unseen.wait();
						}
					} catch (InterruptedException e) {
						Log.v("Bitmap downloader", "*** Stopping asynchronous downloader thread", e);
						return;
					}
				}
			}catch(Exception e){
				Log.e("dk.nindroid.BitmapDownloader", "Unexpected exception caught...", e);
			}
		}
	}
	
	private void fillFeed(){
		if(useExternal()){			
			if(Settings.showType != null && Settings.showType == Settings.SHOW_FLICKR){
				addImageUrls(Settings.showPath, false);
			}
			else{
				// Soon to disappear!
				if(Settings.useRandom){
					addImageUrls("http://api.flickr.com/services/rest/?method=flickr.interestingness.getList&api_key=f6fdb5a636863d148afa8e7bb056bf1b&per_page=500", true);
				}
				// More to come!
			}
		}
	}
	
	static boolean useExternal(){
		return (Settings.useRandom  && Settings.showType == null) || Settings.showType != Settings.SHOW_LOCAL;
	}
	
	private void addImageUrls(String feed, boolean shuffle){
		int i = 0;
		while(true){
			try{
				List<ImageReference> bitmaps = FlickrFeeder.getImageUrls(feed);
				if(shuffle){
					Collections.shuffle(bitmaps);
				}
				if(bitmaps != null){
					for(ImageReference bmp : bitmaps){
						imageFeed.add(bmp);
					}
				}
				return;
			}catch(Exception e){
				Log.w("dk.nindroid.BitmapDownloader", "Failed getting feed, retrying...");
				++i;
				if(i > 5) {
					Log.e("dk.nindroid.BitmapDownloader", "Failed getting feed too many times, giving up!", e);
					break;
				}
			}
		}
		Log.v("dk.nindroid.rss.BitmapDownloader", imageFeed.size() + " images from feed.");
	}

	public static List<RssElement> parseRss(URL feed) throws ParserConfigurationException, SAXException, FactoryConfigurationError, IOException{
		SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
		XMLReader xmlReader = parser.getXMLReader();
		RSSParser rssParser = new RSSParser();
		xmlReader.setContentHandler(rssParser);
		xmlReader.parse(new InputSource(feed.openStream()));
		return rssParser.getData();
	}
	
	public static Bitmap downloadImage(String URL, Progress progress){
		URL url;
		byte[] bitmapByteArray;
		try {
			url = new URL(URL);		
			bitmapByteArray = DownloadUtil.fetchUrlBytes(url, "Floating image/Android", progress);
			Bitmap bm = BitmapFactory.decodeByteArray(bitmapByteArray, 0, bitmapByteArray.length);
			return bm;
		} catch (Exception e) {
			Log.w("dk.nindroid.rss.BitmaDownloader", e);
		}
		return null;	
	}
		
	public static Bitmap downloadImageDonut(String URL) {
		Bitmap bitmap = null;
		InputStream in = null;
		try {
			in = HttpTools.openHttpConnection(URL);
			bitmap = BitmapFactory.decodeStream(in);
			in.close();
		} catch (IOException e) {
			Log.e("Bitmap downloader", "Error downloading bitmap", e);
		} catch(NullPointerException e) {
			Log.e("Bitmap downloader", "Error downloading bitmap", e);
		}
		
		return bitmap;
	}
}
