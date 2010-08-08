package dk.nindroid.rss.uiActivities;

import android.app.Activity;
import android.widget.Toast;
import dk.nindroid.rss.ShowStreams;

	public class Toaster implements Runnable {
		static void toast(Activity c, String text){
			c.runOnUiThread(new Toaster(text));
		}
		
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
