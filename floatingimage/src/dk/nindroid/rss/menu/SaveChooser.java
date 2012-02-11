package dk.nindroid.rss.menu;

import java.io.File;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.view.Window;
import dk.nindroid.rss.R;
import dk.nindroid.rss.settings.DirectoryBrowser;

public class SaveChooser extends FragmentActivity {
	public static final String SAVEDIR = "downloadDir";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
		}
		setContentView(R.layout.save_chooser);
		DirectoryBrowser dirBrowser =  (DirectoryBrowser)getSupportFragmentManager().findFragmentByTag("dirBrowser");
		SharedPreferences sp = getSharedPreferences(Settings.SHARED_PREFS_NAME, 0);
		String defaultDir = sp.getString(SaveChooser.SAVEDIR, Environment.getExternalStorageDirectory().getAbsolutePath() + "/download/");
		File f = new File(defaultDir);
		if(!(f.exists() && f.isDirectory())){
			defaultDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/download/";
		}
		dirBrowser.initialDir = defaultDir;
	}
	
	@Override
	public void finish() {
		DirectoryBrowser dirBrowser =  (DirectoryBrowser)getSupportFragmentManager().findFragmentByTag("dirBrowser");
		if(dirBrowser.returnPath != null){
			Editor e = getSharedPreferences(Settings.SHARED_PREFS_NAME, 0).edit();
			e.putString(SAVEDIR, dirBrowser.returnPath);
			e.commit();
		}
		super.finish();
	}
}
