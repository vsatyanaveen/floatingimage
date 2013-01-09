package dk.nindroid.rss.menu;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import dk.nindroid.rss.settings.ClockSettings;

public class WallpaperClockSettingsLauncher  extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Intent intent = new Intent(this, ClockSettings.class);
		intent.putExtra(ClockSettings.SHARED_PREFS_NAME, WallpaperSettings.SHARED_PREFS_NAME);
		startActivityForResult(intent, 0);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		this.finish();
	}
}
