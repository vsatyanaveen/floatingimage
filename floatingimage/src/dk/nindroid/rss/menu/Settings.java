package dk.nindroid.rss.menu;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import dk.nindroid.rss.R;

public class Settings extends PreferenceActivity{
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);
	}
}
