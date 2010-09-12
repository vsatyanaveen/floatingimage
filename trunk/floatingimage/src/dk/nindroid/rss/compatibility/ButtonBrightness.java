package dk.nindroid.rss.compatibility;

import android.view.WindowManager;

public class ButtonBrightness {
	public static void setButtonBrightness(WindowManager.LayoutParams lp, float amount){
		lp.buttonBrightness = 0;
	}
}
