package dk.nindroid.rss.menu;

import dk.nindroid.rss.R;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;

public class GallerySettings extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    public static final String SHARED_PREFS_NAME = "gallerysettings";

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		getPreferenceManager().setSharedPreferencesName(SHARED_PREFS_NAME);
		addPreferencesFromResource(R.xml.gallery_settings);
		getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {}
}
