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
import dk.nindroid.rss.compatibility.Exif;
import dk.nindroid.rss.data.ImageReference;
import dk.nindroid.rss.data.LocalImage;
import dk.nindroid.rss.data.Progress;
import dk.nindroid.rss.data.RssElement;
import dk.nindroid.rss.parser.RSSParser;
import dk.nindroid.rss.settings.Settings;

public class BitmapDownloader implements Runnable {
	TextureBank bank;
	FeedController mFeedController;
	static boolean exifAvailable = false;
	
	// Do we have access to the Exif class?
	static{
		try{
			Log.v("Floating Image", "Checking if Exif tool is available...");
			Exif.checkAvailable();
			exifAvailable = true;
			Log.v("Floating Image", "Enabling Exif reading!");
		}catch(Throwable t){
			exifAvailable = false;
			Log.v("Floating Image", "Exif tool is not available");
		}
	}
	
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
						Log.i("Bitmap downloader", "*** Stopping asynchronous downloader thread per request");
						return;
					}
					ImageReference ir = mFeedController.getImageReference();
					if(ir == null || bank.isShowing(ir.getID())){
						// Nothing (new) to show just yet...
						Thread.sleep(500);
						break;
					}
					if(bank.doDownload(ir.getImageID())){
						if(ir.getBitmap() != null && !ir.getBitmap().isRecycled()){ // Image is being shown, ignore!
							break;
						}else if(ir instanceof LocalImage){
							addLocalImage((LocalImage)ir);
						}else {
							addExternalImage(ir);
						}
					}else{
						bank.addFromCache(ir.getImageID());
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
		String url = Settings.highResThumbs ? ir.get256ImageUrl() : ir.get128ImageUrl();
		Bitmap bmp = downloadImage(url, null);
		if(bmp == null){
			return;
		}
		if(Settings.highResThumbs){
			int max = Math.max(bmp.getHeight(), bmp.getWidth());
			if(max > 256){
				float scale = (float)256 / max;
				Bitmap tmp = Bitmap.createScaledBitmap(bmp, (int)(bmp.getWidth() * scale), (int)(bmp.getHeight() * scale), true);
				bmp.recycle();
				bmp = tmp;
			}
			ir.set256Bitmap(bmp);
		}else{
			int max = Math.max(bmp.getHeight(), bmp.getWidth());
			if(max > 128){
				float scale = (float)128 / max;
				Bitmap tmp = Bitmap.createScaledBitmap(bmp, (int)(bmp.getWidth() * scale), (int)(bmp.getHeight() * scale), true);
				bmp.recycle();
				bmp = tmp;
			}
			ir.set128Bitmap(bmp);
		}
		ir.getExtended();
		bank.addNewBitmap(ir);
	}
	
	public void addLocalImage(LocalImage image){
		File file = image.getFile();
		int size = Settings.highResThumbs ? 256 : 128;
		Bitmap bmp = ImageFileReader.readImage(file, size, null);
		if(bmp != null){
			if(exifAvailable){
				try {
					Exif exif = new Exif(file.getAbsolutePath());
					int rotation = exif.getAttributeInt(Exif.TAG_ORIENTATION, -1);
					switch(rotation){
					case Exif.ORIENTATION_NORMAL:
					case -1:
						break;
					case Exif.ORIENTATION_ROTATE_90:
						image.setRotation(270);
						break;
					case Exif.ORIENTATION_ROTATE_180:
						image.setRotation(180);
						break;
					case Exif.ORIENTATION_ROTATE_270:
						image.setRotation(90);
						break;
					}
				} catch (IOException e) {
					Log.w("Floating Image", "Error reading exif info for file", e);
				} catch (Throwable t){
					exifAvailable = false; // Some devices sort of know ExifInterface...
					Log.w("Floating Image", "Disabling Exif Interface, the device lied!");
				}
			}
			if(Settings.highResThumbs){
				image.set256Bitmap(bmp);
			}else{
				image.set128Bitmap(bmp);
			}
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
