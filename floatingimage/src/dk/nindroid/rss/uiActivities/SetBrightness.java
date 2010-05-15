package dk.nindroid.rss.uiActivities;

import android.content.ContentResolver;

public class SetBrightness implements Runnable {
	int brightness;
	ContentResolver contentResolver;
	
	public SetBrightness(ContentResolver contentResolver, int brightness){
		this.contentResolver = contentResolver;
		this.brightness = brightness;
	}
	
	@Override
	public void run() {
		
	}
	
}
