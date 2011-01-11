package dk.nindroid.rss.uiActivities;

import dk.nindroid.rss.MainActivity;

public class OpenContextMenu implements Runnable {

	MainActivity mActivity;
	
	public OpenContextMenu(MainActivity activity){
		this.mActivity = activity;
	}
	
	@Override
	public void run() {
		mActivity.openContextMenu();
	}

}
