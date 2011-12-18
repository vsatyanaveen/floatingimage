package dk.nindroid.rss;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import dk.nindroid.rss.data.LocalImage;
import dk.nindroid.rss.menu.GallerySettings;
import dk.nindroid.rss.settings.FeedsDbAdapter;
import dk.nindroid.rss.settings.Settings;

public class GalleryImageLauncher extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		File f;
		try {
			f = new File(new URI(getIntent().getData().toString()));
		} catch (URISyntaxException e) {
			Log.e("Floating Image", "Cannot read uri", e);
			this.finish();
			return;
		}
		FeedsDbAdapter db = new FeedsDbAdapter(this).open();
		String dirPath = f.getParent();
		Cursor c = db.fetchFeed(dirPath);
		if(c != null && c.moveToFirst()){
			showImage(c.getInt(c.getColumnIndex(FeedsDbAdapter.KEY_ROWID)), f);
			c.close();
		}else{
			c.close();
			db.addFeed(dirPath, dirPath, Settings.TYPE_LOCAL, "");
			c = db.fetchFeed(dirPath);
			showImage(c.getInt(c.getColumnIndex(FeedsDbAdapter.KEY_ROWID)), f);
			c.close();
		}
		db.close();
		
		Log.v("Floating Image", f.getAbsolutePath());
		this.finish();
	}
	
	void showImage(int feedId, File f){
		Intent intent = new Intent(this, ShowStreams.class);
		intent.putExtra(ShowStreams.SHOW_FEED_ID, feedId);
		intent.putExtra(ShowStreams.SHOW_IMAGE_ID, LocalImage.getID(f));
		intent.putExtra(ShowStreams.SETTINGS_NAME, GallerySettings.SHARED_PREFS_NAME);
		this.startActivity(intent);
	}
}
