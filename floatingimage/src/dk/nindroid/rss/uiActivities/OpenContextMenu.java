package dk.nindroid.rss.uiActivities;

import dk.nindroid.rss.ShowStreams;

public class OpenContextMenu implements Runnable {

	@Override
	public void run() {
		ShowStreams.current.openContextMenu();
	}

}
