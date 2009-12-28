package dk.nindroid.rss.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class Settings {
	public static boolean	useRandom;
	public static boolean	useLocal;
	public static boolean	useCache;
	public static boolean	rotateImages;
	
	public static void readSettings(Context context){
		SharedPreferences sp = context.getSharedPreferences("dk.nindroid.rss_preferences", 0);
		useRandom = sp.getBoolean("randomImages", false);
		useLocal = sp.getBoolean("localImages", true);
		useCache = sp.getBoolean("useCache", false);
		rotateImages = sp.getBoolean("rotateImages", true);
		
		Log.v("Settings", "useRandom: " + (useRandom ? "true" : "false"));
		Log.v("Settings", "useLocal: " + (useLocal ? "true" : "false"));
		Log.v("Settings", "useCache: " + (useCache ? "true" : "false"));
		Log.v("Settings", "rotateImages: " + (rotateImages ? "true" : "false"));
	}
}
