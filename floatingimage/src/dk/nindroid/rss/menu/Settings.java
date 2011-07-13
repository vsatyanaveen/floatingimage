package dk.nindroid.rss.menu;

import java.util.List;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.util.Log;
import dk.nindroid.rss.R;
import dk.nindroid.rss.compatibility.TryGetFragmentManager;

public class Settings extends PreferenceActivity{
	public static final String SHARED_PREFS_NAME = "dk.nindroid.rss_preferences";
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		if(!TryGetFragmentManager.supportsFragments(this)){
			addPreferencesFromResource(R.xml.settings);
		}
	}
	
	@Override
	public void onBuildHeaders(List<Header> target) {
		super.onBuildHeaders(target);
		loadHeadersFromResource(R.xml.settings_headers, target);
	}
	
	public static class Feeds extends PreferenceFragment{
		@Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Can retrieve arguments from preference XML.
            Log.i("Floating Image", "Arguments: " + getArguments());
            
            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.feeds);
        }
	}

	public static class Display extends PreferenceFragment{
		@Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Can retrieve arguments from preference XML.
            Log.i("Floating Image", "Arguments: " + getArguments());
            
            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.display);
        }
	}
	
	public static class Floating extends PreferenceFragment{
		@Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Can retrieve arguments from preference XML.
            Log.i("Floating Image", "Arguments: " + getArguments());
            
            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.floating);
        }
	}
	
	public static class Misc extends PreferenceFragment{
		@Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Can retrieve arguments from preference XML.
            Log.i("Floating Image", "Arguments: " + getArguments());
            
            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.misc);
        }
	}
}
