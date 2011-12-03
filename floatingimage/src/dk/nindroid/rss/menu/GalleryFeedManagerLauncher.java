package dk.nindroid.rss.menu;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import dk.nindroid.rss.settings.ManageFeeds;

public class GalleryFeedManagerLauncher extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Intent intent = new Intent(this, ManageFeeds.class);
		intent.putExtra(ManageFeeds.SHARED_PREFS_NAME, GallerySettings.SHARED_PREFS_NAME);
		intent.putExtra(ManageFeeds.HIDE_CHECKBOXES, true);
		startActivityForResult(intent, 0);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		this.finish();
	}
}
