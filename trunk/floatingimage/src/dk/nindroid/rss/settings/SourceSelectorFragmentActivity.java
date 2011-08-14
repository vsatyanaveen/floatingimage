package dk.nindroid.rss.settings;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.Window;
import dk.nindroid.rss.R;

public class SourceSelectorFragmentActivity extends FragmentActivity {
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
		setContentView(R.layout.source_selector);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch(keyCode){
		case KeyEvent.KEYCODE_BACK:
			Fragment f = getSupportFragmentManager().findFragmentByTag("content");
			if(f instanceof SettingsFragment){
				SettingsFragment sf = (SettingsFragment)f;
				if(sf.back()){
					return true;
				}
			}
		}
		return super.onKeyDown(keyCode, event);
	}
}
