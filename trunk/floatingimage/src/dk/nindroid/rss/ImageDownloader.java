package dk.nindroid.rss;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

import android.graphics.Bitmap;
import android.util.Log;
import dk.nindroid.rss.settings.Settings;
import dk.nindroid.rss.uiActivities.Toaster;

public class ImageDownloader implements Runnable {
	protected String url;
	protected String name;
	protected boolean setWallpaper;
	
	public static void downloadImage(String url, String name){
		Thread t = new Thread(new ImageDownloader(url, name, false));
		t.start();
	}
	
	public static void setWallpaper(String url, String name){
		Thread t = new Thread(new ImageDownloader(url, name, true));
		t.start();
	}
	
	protected ImageDownloader(String url, String name, boolean setWallpaper){
		this.url = url;
		this.name = name;
		this.setWallpaper = setWallpaper;
	}
	
	@Override
	public void run() {
		ShowStreams activity = ShowStreams.current;
		Toaster toaster = null;
		String urlString = this.url;
		if(setWallpaper){
			Bitmap bmp = BitmapDownloader.downloadImage(urlString, null);
			try {
				if(bmp.getWidth() == 240 && bmp.getHeight() == 180){
					toaster = new Toaster("Sorry, there is no large version of this image.");
					activity.runOnUiThread(toaster);
					return;
				}
				activity.setWallpaper(bmp);
				toaster = new Toaster("Wallpaper has been changed to " + this.name);
			} catch (IOException e) {
				Log.e("ImageDownloader", "Failed to get image", e);
				toaster = new Toaster("Sorry, there was an error setting wallpaper!");
				activity.runOnUiThread(toaster);
				return;
			}finally{
				bmp.recycle();
			}
		}else{
			URL url = null;
			File f = null;
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
			toaster = new Toaster(name + " saved to " + f.getAbsolutePath());
		}
		activity.runOnUiThread(toaster);
	}
	
}
