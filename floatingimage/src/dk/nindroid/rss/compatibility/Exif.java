package dk.nindroid.rss.compatibility;

import java.io.IOException;

import android.media.ExifInterface;


public class Exif {
	private ExifInterface mInstance;
	
	static {
		try{
			Class.forName("ExifInterface");
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
