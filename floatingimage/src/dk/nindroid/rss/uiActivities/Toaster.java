package dk.nindroid.rss.uiActivities;

import android.app.Activity;
import android.widget.Toast;
import dk.nindroid.rss.ShowStreams;

	public class Toaster implements Runnable {
		static void toast(Activity c, String text){
			c.runOnUiThread(new Toaster(text));
		}
		
		String text;
		int res;
		public Toaster(String text){
			this.text = text;
		}
		public Toaster(int res){
			this.res = res;
		}
		@Override
		public void run() {
			Toast t;
			if(text == null){
				t = Toast.makeText(ShowStreams.current.context(), res, Toast.LENGTH_SHORT);
			}else{
				t = Toast.makeText(ShowStreams.current.context(), text, Toast.LENGTH_SHORT);
			}
			t.show();
			
		}
	}
