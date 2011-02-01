package dk.nindroid.rss.menu;

import dk.nindroid.rss.settings.ManageFeeds;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class WallpaperFeedManagerLauncher extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Intent intent = new Intent(this, ManageFeeds.class);
		intent.putExtra(ManageFeeds.SHARED_PREFS_NAME, WallpaperSettings.SHARED_PREFS_NAME);
		startActivity(intent);
	}
}
