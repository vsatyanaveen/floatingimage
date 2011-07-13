package dk.nindroid.rss.uiActivities;

import android.view.WindowManager;
import dk.nindroid.rss.MainActivity;
import dk.nindroid.rss.compatibility.HoneycombButtons;

public class ToggleNotificationBar implements Runnable {
	boolean doshow;
	MainActivity mActivity;
	
	public ToggleNotificationBar(MainActivity activity, boolean doshow) {
		this.mActivity = activity;
		this.doshow = doshow;
	}
	
	@Override
	public void run() {
		if(doshow){
			mActivity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN, WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
			try{
				HoneycombButtons.ShowButtons(mActivity.getView());
			}catch (Throwable t){}
		}else{
			WindowManager.LayoutParams attrs = mActivity.getWindow().getAttributes();
			attrs.flags &= (~WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
			mActivity.getWindow().setAttributes(attrs);
			try{
				HoneycombButtons.HideButtons(mActivity.getView());
			}catch (Throwable t){}
		}
	}
}
