package dk.nindroid.rss.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.os.Environment;
import android.view.Surface;

public class Settings {
	public static final int TYPE_UNKNOWN  = 0;
	public static final int TYPE_LOCAL  = 1;
	public static final int TYPE_FLICKR = 2;
	public static final int TYPE_PICASA = 3;
	public static final int TYPE_FACEBOOK = 4;
	public static final int TYPE_PHOTOBUCKET = 5;
	public static final int TYPE_FIVEHUNDREDPX = 6;
	
	public static final int MODE_NONE = 0;
	public static final int MODE_SLIDE_RIGHT_TO_LEFT = 1;
	public static final int MODE_SLIDE_TOP_TO_BOTTOM = 2;
	public static final int MODE_CROSSFADE = 3;
	public static final int MODE_FADE_TO_BLACK = 4;
	public static final int MODE_FADE_TO_WHITE = 5;
	public static final int MODE_RANDOM = 6;
	public static final int MODE_FLOATING_IMAGE = 7;
	
	public boolean	galleryMode;
	public boolean 	shuffleImages;
	public boolean 	rotateImages;
	public boolean 	fullscreenBlack;
	public String  	downloadDir;
	public int 		mode;
	public long 	slideshowInterval;
	public long	 	slideSpeed;
	public boolean	imageDecorations;
	public boolean	highResThumbs;
	public int		floatingType = 0;
	public long		floatingTraversal = 30000;
	public int		forceRotation;
	public boolean	tsunami;
	public boolean	blackEdges;
	public boolean	hideEdges;
	public boolean	singleClickDeselect;
	
	public int		backgroundColor;
	public boolean	lowFps;
	public boolean	nudity;
	
	public boolean fullscreen;
	public Bitmap.Config bitmapConfig = Config.RGB_565;

	private SharedPreferences sp;
	
	private String settings;
	
	public Settings(String settings){
		this.settings = settings;
	}
	
	public void readSettings(Context context) {
		//Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		//int max = Math.max(display.getWidth(), display.getHeight());
		
		this.sp = context.getSharedPreferences(settings, 0);
		shuffleImages = sp.getBoolean("shuffleImages", true);
		rotateImages = sp.getBoolean("rotateImages", true);
		downloadDir = sp.getString("downloadDir", Environment.getExternalStorageDirectory().getAbsolutePath() + "/download/");
		fullscreen = sp.getBoolean("fullscreen", false);
		mode = parseMode(sp.getString("mode", "5000"));
		slideshowInterval = Long.parseLong(sp.getString("slideInterval", "10000"));
		slideSpeed = Long.parseLong(sp.getString("slideSpeed", "300"));
		fullscreenBlack = sp.getBoolean("fullscreenBlack", true);
		imageDecorations = sp.getBoolean("imageDecorations", true);
		highResThumbs = sp.getBoolean("highResThumbs", false);
		floatingType = Integer.parseInt(sp.getString("floatingType", "0"));
		floatingTraversal = Long.parseLong(sp.getString("floatingSpeed", "30000"));
		forceRotation = Integer.parseInt(sp.getString("forceRotation", "0"));
		blackEdges = sp.getBoolean("blackEdges", true);
		tsunami = sp.getBoolean("tsunami", false);
		galleryMode = sp.getBoolean("galleryMode", false);
		singleClickDeselect = sp.getBoolean("singleClickDeselect", true);
		nudity = !sp.getBoolean("nudity", true);
			
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
		
		/*
		if(max < 500){
			highResThumbs = false;
		}
		*/
		backgroundColor = Integer.parseInt(sp.getString("backgroundColor", "0"));
		lowFps = sp.getBoolean("liveWallpaperLowFramerate", false);
		
		memoryBalancing(context);
	}
	
	void memoryBalancing(Context context){
		/*
		ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
		MemoryInfo mi = new MemoryInfo();
		am.getMemoryInfo(mi);
		*/
		bitmapConfig = Config.ARGB_8888;
	}
	
	public void PreferenceChanged(SharedPreferences sp, String key){
		if(key.equals("lowFps")){
			lowFps = sp.getBoolean("liveWallpaperLowFramerate", false);
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
	
	public void setFullscreen(boolean fullscreen){
		this.fullscreen = fullscreen;
		SharedPreferences.Editor editor = sp.edit();
		editor.putBoolean("fullscreen", fullscreen);
		editor.commit();
	}
	
	public String typeToString(int type){
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
