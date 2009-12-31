package dk.nindroid.rss.orientation;

import java.util.TimerTask;

public class ChangeOrientation extends TimerTask {
	OrientationManager manager;
	
	public ChangeOrientation(OrientationManager manager) {
		this.manager = manager;
	}

	@Override
	public void run() {
		// Redundant check
		synchronized(this){
			if(manager.settingOrientation != manager.currentOrientation){
				manager.currentOrientation = manager.settingOrientation;
				for(OrientationSubscriber os : manager.subscribers){
					os.setOrientation(manager.currentOrientation);
				}
			}
		}
	}

}
