package dk.nindroid.rss;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

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
import dk.nindroid.rss.data.LocalImage;
import dk.nindroid.rss.data.Progress;
import dk.nindroid.rss.data.RssElement;
import dk.nindroid.rss.parser.RSSParser;

public class BitmapDownloader implements Runnable {
	TextureBank bank;
	FeedController mFeedController; 
	public BitmapDownloader(TextureBank bank, FeedController feedController){
		this.bank = bank;
		this.mFeedController = feedController;
	}
	
	@Override
	public void run() {
		Log.v("Bitmap downloader", "*** Starting asynchronous downloader thread");
		Process.setThreadPriority(10);
		while(true){
			try{
				if(bank.stopThreads) return;
				while(bank.unseen.size() < bank.textureCache){
					if(bank.stopThreads){
						Log.v("Bitmap downloader", "*** Stopping asynchronous downloader thread per request");
						return;
					}
					ImageReference ir = mFeedController.getImageReference();
					if(ir == null)
						break;
					if(ir instanceof LocalImage){
						addLocalImage((LocalImage)ir);
					}else {
						addExternalImage(ir);
					}
				}
				synchronized (bank.unseen) {
					if(bank.stopThreads){
						Log.v("Bitmap downloader", "*** Stopping asynchronous downloader thread per request");
						return;
					}
					try {
						bank.unseen.wait();
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
	
	public void addExternalImage(ImageReference ir){
		String url = ir.getSmallImageUrl();
		if(bank.doDownload(ir.getImageID())){
			Bitmap bmp = downloadImage(url, null);
			if(bmp == null){
				return;
			}
			ir.set128Bitmap(bmp);
			ir.getExtended();
			bank.addNewBitmap(ir);
		}else{
			if(mFeedController.isShowing()){
				bank.addFromCache(ir.getImageID());
			}
		}
	}
	
	public void addLocalImage(LocalImage image){
		File file = image.getFile();
		Bitmap bmp = ImageFileReader.readImage(file, 128, null);
		if(bmp != null){
			image.set128Bitmap(bmp);
			bank.addNewBitmap(image);
		}
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
			Log.w("dk.nindroid.rss.BitmaDownloader", "Error handling URL \"" + URL + "\"", e);
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
