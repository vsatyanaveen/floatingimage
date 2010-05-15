package dk.nindroid.rss.uiActivities;

import android.view.WindowManager;
import dk.nindroid.rss.ShowStreams;

public class ToggleNotificationBar implements Runnable {
	boolean doshow;
	public ToggleNotificationBar(boolean doshow) {
		this.doshow = doshow;
	}
	
	@Override
	public void run() {
		if(doshow){
			ShowStreams.current.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN, WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
		}else{
			WindowManager.LayoutParams attrs = ShowStreams.current.getWindow().getAttributes();
			attrs.flags &= (~WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
			ShowStreams.current.getWindow().setAttributes(attrs);
		}
	}
}
