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
import dk.nindroid.rss.uiActivities.Toaster;

public class ImageDownloader implements Runnable {
	protected String url;
	protected String name;
	boolean isLocal = false;
	protected boolean setWallpaper;
	MainActivity mActivity;
	
	public static void downloadImage(String url, String name, MainActivity activity){
		Thread t = new Thread(new ImageDownloader(activity, url, name, false, false));
		t.start();
	}
	
	public static void setWallpaper(String url, String name, boolean isLocal, MainActivity activity){
		Thread t = new Thread(new ImageDownloader(activity, url, name, true, isLocal));
		t.start();
	}
	
	protected ImageDownloader(MainActivity activity, String url, String name, boolean setWallpaper, boolean isLocal){
		mActivity = activity;
		this.url = url;
		this.name = name;
		this.setWallpaper = setWallpaper;
		this.isLocal = isLocal;
	}
	
	@Override
	public void run() {
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
				f = new File(mActivity.getSettings().downloadDir + "/" + name.replace("/", "_").replace("*", "") + ".jpg"); // Assuming jpg... Jpeg is popular!
				f.createNewFile();
				OutputStream os = new FileOutputStream(f);
				byte[] dl = DownloadUtil.fetchUrlBytes(url, mActivity.context().getString(R.string.user_agent), null);
				os.write(dl, 0, dl.length);
			} catch (MalformedURLException e1) {
				toaster = new Toaster(mActivity.context(), "Cannot read image address: \"" + this.url + "\"");
				mActivity.runOnUiThread(toaster);
				return;
			} catch (IOException e) {
				Log.e("Floating Image", "Failed to get image", e);
				toaster = new Toaster(mActivity.context(), "Failed fetching image: \"" + name + "\"");
				mActivity.runOnUiThread(toaster);
				return;
			}
		}
		if(setWallpaper){
			try {
				InputStream is = new FileInputStream(f);
				mActivity.setWallpaper(is);
				toaster = new Toaster(mActivity.context(), name + " set as wallpaper.");
			} catch (FileNotFoundException e) {
				Log.e("Floating Image", "Could not read file I just wrote!", e);
				toaster = new Toaster(mActivity.context(), "Error setting " + name + " as wallpaper :(");
			} catch (IOException e) {
				Log.e("Floating Image", "Error setting wallpaper", e);
				toaster = new Toaster(mActivity.context(), "Error setting " + name + " as wallpaper :(");
			}
		}else{
			toaster = new Toaster(mActivity.context(), name + " saved to " + f.getAbsolutePath());
		}
		mActivity.runOnUiThread(toaster);
	}
	
}
