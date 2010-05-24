package dk.nindroid.rss;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

import android.util.Log;
import dk.nindroid.rss.settings.Settings;
import dk.nindroid.rss.uiActivities.Toaster;

public class ImageDownloader implements Runnable {
	protected String url;
	protected String name;
	boolean isLocal = false;
	protected boolean setWallpaper;
	
	public static void downloadImage(String url, String name){
		Thread t = new Thread(new ImageDownloader(url, name, false, false));
		t.start();
	}
	
	public static void setWallpaper(String url, String name, boolean isLocal){
		Thread t = new Thread(new ImageDownloader(url, name, true, isLocal));
		t.start();
	}
	
	protected ImageDownloader(String url, String name, boolean setWallpaper, boolean isLocal){
		this.url = url;
		this.name = name;
		this.setWallpaper = setWallpaper;
		this.isLocal = isLocal;
	}
	
	@Override
	public void run() {
		ShowStreams activity = ShowStreams.current;
		Toaster toaster = null;
		String urlString = this.url;
		URL url = null;
		File f = null;
		if(isLocal){
			f = new File(urlString);
		}
		else{
			try {
				url = new URL(urlString);
				String filename = urlString.substring(urlString.lastIndexOf('/'));
				f = new File(Settings.downloadDir + "/" + filename);
				f.createNewFile();
				OutputStream os = new FileOutputStream(f);
				byte[] dl = DownloadUtil.fetchUrlBytes(url, activity.getString(R.string.user_agent), null);
				os.write(dl, 0, dl.length);
			} catch (MalformedURLException e1) {
				toaster = new Toaster("Cannot read image address: \"" + this.url + "\"");
				activity.runOnUiThread(toaster);
				return;
			} catch (IOException e) {
				Log.e("ImageDownloader", "Failed to get image", e);
				toaster = new Toaster("Failed fetching image: \"" + name + "\"");
				activity.runOnUiThread(toaster);
				return;
			}
		}
		if(setWallpaper){
			try {
				InputStream is = new FileInputStream(f);
				activity.setWallpaper(is);
				toaster = new Toaster(name + " set as wallpaper.");
			} catch (FileNotFoundException e) {
				Log.e("Floating Image", "Could not read file I just wrote!", e);
				toaster = new Toaster("Error setting " + name + " as wallpaper :(");
			} catch (IOException e) {
				Log.e("Floating Image", "Error setting wallpaper", e);
				toaster = new Toaster("Error setting " + name + " as wallpaper :(");
			}
		}else{
			toaster = new Toaster(name + " saved to " + f.getAbsolutePath());
		}
		activity.runOnUiThread(toaster);
	}
	
}
