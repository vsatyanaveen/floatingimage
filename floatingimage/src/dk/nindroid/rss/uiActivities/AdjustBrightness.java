package dk.nindroid.rss.uiActivities;

import android.view.WindowManager;
import dk.nindroid.rss.MainActivity;

public class AdjustBrightness implements Runnable {

	MainActivity mActivity;
	WindowManager.LayoutParams mParams;
	
	public AdjustBrightness(MainActivity activity, WindowManager.LayoutParams params){
		this.mActivity = activity;
		this.mParams = params;
	}
	
	@Override
	public void run() {
		mActivity.getWindow().setAttributes(mParams);
	}
}
