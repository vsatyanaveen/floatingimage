package dk.nindroid.rss.menu;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import dk.nindroid.rss.R;

public class Settings extends PreferenceActivity{
	public static final String SHARED_PREFS_NAME = "dk.nindroid.rss_preferences";
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);
	}
}
