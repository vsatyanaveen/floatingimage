package dk.nindroid.rss.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class Settings {
	public static final int TYPE_LOCAL  = 1;
	public static final int TYPE_FLICKR = 2;
	public static final int TYPE_PICASA = 3;
	
	public static boolean useRandom;
	public static boolean useLocal;
	public static boolean useCache;
	public static boolean rotateImages;
	public static String  downloadDir;
	
	public static boolean fullscreen;

	private static SharedPreferences sp;
	
	public static void readSettings(Context context) {
		Settings.sp = context.getSharedPreferences(
				"dk.nindroid.rss_preferences", 0);
		useRandom = sp.getBoolean("randomImages", true);
		useLocal = sp.getBoolean("localImages", true);
		useCache = sp.getBoolean("useCache", false);
		rotateImages = sp.getBoolean("rotateImages", true);
		downloadDir = sp.getString("downloadDir", "/sdcard/download/");
		fullscreen = sp.getBoolean("fullscreen", false);

		Log.v("Settings", "useRandom: " + (useRandom ? "true" : "false"));
		Log.v("Settings", "useLocal: " + (useLocal ? "true" : "false"));
		Log.v("Settings", "useCache: " + (useCache ? "true" : "false"));
		Log.v("Settings", "rotateImages: " + (rotateImages ? "true" : "false"));
	}
	
	public static void setFullscreen(boolean fullscreen){
		Settings.fullscreen = fullscreen;
		SharedPreferences.Editor editor = sp.edit();
		editor.putBoolean("fullscreen", fullscreen);
		editor.commit();
	}
}
