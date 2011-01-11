package dk.nindroid.rss.uiActivities;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;

	public class Toaster implements Runnable {
		static void toast(Activity c, String text){
			c.runOnUiThread(new Toaster(c, text));
		}
		
		Context mContext;
		String text;
		int res;
		public Toaster(Context context, String text){
			this.mContext = context;
			this.text = text;
		}
		public Toaster(Context context, int res){
			this.mContext = context;
			this.res = res;
		}
		@Override
		public void run() {
			Toast t;
			if(text == null){
				t = Toast.makeText(mContext, res, Toast.LENGTH_SHORT);
			}else{
				t = Toast.makeText(mContext, text, Toast.LENGTH_SHORT);
			}
			t.show();
			
		}
	}
