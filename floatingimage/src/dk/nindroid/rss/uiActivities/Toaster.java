package dk.nindroid.rss.uiActivities;

import android.widget.Toast;
import dk.nindroid.rss.ShowStreams;

public class Toaster implements Runnable {
	String text;
	public Toaster(String text){
		this.text = text;
	}
	@Override
	public void run() {
		Toast t = Toast.makeText(ShowStreams.current, text, Toast.LENGTH_SHORT);
		t.show();
	}
}
