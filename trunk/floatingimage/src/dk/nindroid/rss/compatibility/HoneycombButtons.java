package dk.nindroid.rss.compatibility;

import android.view.View;

public class HoneycombButtons {
	public static void HideButtons(View v){
		v.setSystemUiVisibility(View.STATUS_BAR_HIDDEN);
	}
	public static void ShowButtons(View v){
		v.setSystemUiVisibility(View.STATUS_BAR_VISIBLE);
	}
}
