package dk.nindroid.rss.compatibility;

import android.app.Activity;
import android.view.View;

public class Honeycomb {
	public static void HideButtons(View v){
		v.setSystemUiVisibility(View.STATUS_BAR_HIDDEN);
	}
	public static void ShowButtons(View v){
		v.setSystemUiVisibility(View.STATUS_BAR_VISIBLE);
	}
	public static void InvalidateOptionsMenu(Activity a){
		a.invalidateOptionsMenu();
	}
}
