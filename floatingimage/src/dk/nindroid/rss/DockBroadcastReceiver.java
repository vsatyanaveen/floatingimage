package dk.nindroid.rss;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class DockBroadcastReceiver extends BroadcastReceiver{
	@Override
	public void onReceive(Context context, Intent intent) {
		SharedPreferences sp = context.getSharedPreferences(dk.nindroid.rss.menu.Settings.SHARED_PREFS_NAME, 0);
		boolean open = sp.getBoolean("open_on_dock", false);
		if(open){
			Intent i = new Intent(context, DockListener.class);
			if(!sp.getBoolean("running", false)){
				i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(i);
			}
		}
	}
}
