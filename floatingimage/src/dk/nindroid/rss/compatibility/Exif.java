package dk.nindroid.rss.compatibility;

import java.io.IOException;

import android.media.ExifInterface;


public class Exif {
	private ExifInterface mInstance;
	
	public static final int ORIENTATION_NORMAL 		= ExifInterface.ORIENTATION_NORMAL;
	public static final int ORIENTATION_ROTATE_90 	= ExifInterface.ORIENTATION_ROTATE_90;
	public static final int ORIENTATION_ROTATE_180 	= ExifInterface.ORIENTATION_ROTATE_180;
	public static final int ORIENTATION_ROTATE_270 	= ExifInterface.ORIENTATION_ROTATE_270;
	public static final String TAG_ORIENTATION 		= ExifInterface.TAG_ORIENTATION;
	
	static {
		try{
			Class.forName("android.media.ExifInterface");
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}
	
	public static void checkAvailable(){} // Init class
	
	public Exif(String filename) throws IOException{
		mInstance = new ExifInterface(filename);
	}
	
	public int getAttributeInt(String tag, int defaultValue){
		return mInstance.getAttributeInt(tag, defaultValue);
	}
}
