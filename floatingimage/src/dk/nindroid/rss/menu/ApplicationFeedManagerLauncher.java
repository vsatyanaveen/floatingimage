package dk.nindroid.rss.menu;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import dk.nindroid.rss.settings.ManageFeeds;

public class ApplicationFeedManagerLauncher extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Intent intent = new Intent(this, ManageFeeds.class);
		intent.putExtra(ManageFeeds.SHARED_PREFS_NAME, Settings.SHARED_PREFS_NAME);
		startActivity(intent);
	}
}
