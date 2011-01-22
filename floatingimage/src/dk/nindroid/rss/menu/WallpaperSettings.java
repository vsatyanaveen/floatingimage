package dk.nindroid.rss.menu;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import dk.nindroid.rss.R;

public class WallpaperSettings extends PreferenceActivity
							implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String SHARED_PREFS_NAME="wallpapersettings";

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		getPreferenceManager().setSharedPreferencesName(SHARED_PREFS_NAME);
		addPreferencesFromResource(R.xml.wallpaper_settings);
		getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(
                this);

	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {}
}
