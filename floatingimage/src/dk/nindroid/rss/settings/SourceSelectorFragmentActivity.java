package dk.nindroid.rss.settings;

import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.Window;
import dk.nindroid.rss.R;
import dk.nindroid.rss.menu.Settings;

public class SourceSelectorFragmentActivity extends FragmentActivity {
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
		setContentView(R.layout.source_selector);
		
		final SharedPreferences sp = getSharedPreferences(Settings.SHARED_PREFS_NAME, 0);
		boolean warned = sp.getBoolean("warning_shown", false);
		if(!warned){
			Builder b = new Builder(this);
			b.setTitle(R.string.note)
			.setMessage(R.string.beware_of_public_feeds)
			.setCancelable(true).setPositiveButton(android.R.string.ok, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Editor e = sp.edit();
					e.putBoolean("warning_shown", true);
					e.commit();
					dialog.dismiss();
				}
			}).create().show();
		}
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
