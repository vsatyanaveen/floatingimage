package dk.nindroid.rss;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

public class DockListener extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SharedPreferences sp = getSharedPreferences(dk.nindroid.rss.menu.Settings.SHARED_PREFS_NAME, 0);
		boolean open = sp.getBoolean("open_on_dock", false);
		if(open){
			Log.v("Floating Image", "Autolaunching Floating Image on dock");
			
			Intent intent = new Intent(this, ShowStreams.class);
			intent.putExtra(ShowStreams.DISABLE_KEYGUARD, true);
			startActivityForResult(intent, 0);
		}else{
			finish();
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		finish();
	}
}
