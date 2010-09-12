package dk.nindroid.rss.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

public class Settings {
	public static final int TYPE_LOCAL  = 1;
	public static final int TYPE_FLICKR = 2;
	public static final int TYPE_PICASA = 3;
	public static final int TYPE_FACEBOOK = 4;
	
	public static final int MODE_NONE = 0;
	public static final int MODE_SLIDE_RIGHT_TO_LEFT = 1;
	public static final int MODE_SLIDE_TOP_TO_BOTTOM = 2;
	public static final int MODE_CROSSFADE = 3;
	public static final int MODE_FADE_TO_BLACK = 4;
	public static final int MODE_FADE_TO_WHITE = 5;
	public static final int MODE_RANDOM = 6;
	public static final int MODE_FLOATING_IMAGE = 7;
	
	
	public static boolean 	useCache;
	public static boolean 	shuffleImages;
	public static boolean 	rotateImages;
	public static boolean 	fullscreenBlack;
	public static String  	downloadDir;
	public static int 		mode;
	public static long 		slideshowInterval;
	public static long	 	slideSpeed;
	public static boolean	imageDecorations;
	public static boolean	highResThumbs;
	public static long		floatingTraversal;
	public static int		forceRotation;
	
	public static boolean fullscreen;

	private static SharedPreferences sp;
	
	public static void readSettings(Context context) {
		Settings.sp = context.getSharedPreferences("dk.nindroid.rss_preferences", 0);
		shuffleImages = sp.getBoolean("shuffleImages", true);
		useCache = sp.getBoolean("useCache", false);
		rotateImages = sp.getBoolean("rotateImages", true);
		downloadDir = sp.getString("downloadDir", Environment.getExternalStorageDirectory().getAbsolutePath() + "/download/");
		fullscreen = sp.getBoolean("fullscreen", false);
		mode = parseMode(sp.getString("mode", "5000"));
		slideshowInterval = Long.parseLong(sp.getString("slideInterval", "10000"));
		slideSpeed = Long.parseLong(sp.getString("slideSpeed", "300"));
		fullscreenBlack = sp.getBoolean("fullscreenBlack", true);
		imageDecorations = sp.getBoolean("imageDecorations", true);
		highResThumbs = sp.getBoolean("highResThumbs", false);
		floatingTraversal = Long.parseLong(sp.getString("floatingSpeed", "30000"));
		forceRotation = Integer.parseInt(sp.getString("forceRotation", "0"));
		switch(forceRotation){
		case 90:
			forceRotation = Surface.ROTATION_90;
			break;
		case 180:
			forceRotation = Surface.ROTATION_180;
			break;
		case 270:
			forceRotation = Surface.ROTATION_270;
			break;
		}
		
		Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		if(display.getWidth() < 400){
			highResThumbs = false;
		}
	}

	private static int parseMode(String mode){
		if(mode.equals("none")){
			return MODE_NONE;
		}
		if(mode.equals("slideRightToLeft")){
			return MODE_SLIDE_RIGHT_TO_LEFT;
		}
		if(mode.equals("SlideTopToBottom")){
			return MODE_SLIDE_TOP_TO_BOTTOM;
		}
		if(mode.equals("crossFade")){
			return MODE_CROSSFADE;
		}
		if(mode.equals("fadeToBlack")){
			return MODE_FADE_TO_BLACK;
		}
		if(mode.equals("fadeToWhite")){
			return MODE_FADE_TO_WHITE;
		}
		if(mode.equals("random")){
			return MODE_RANDOM;
		}
		else{
			return MODE_FLOATING_IMAGE;
		}
	}
	
	public static void setFullscreen(boolean fullscreen){
		Settings.fullscreen = fullscreen;
		SharedPreferences.Editor editor = sp.edit();
		editor.putBoolean("fullscreen", fullscreen);
		editor.commit();
	}
	
	public static String typeToString(int type){
		switch(type){
		case TYPE_LOCAL:
			return "Local";
		case TYPE_FLICKR:
			return "Flickr";
		case TYPE_PICASA:
			return "Picasa";
		case TYPE_FACEBOOK:
			return "Facebook";
		default:
			return "Unknown";
		}
	}
}
