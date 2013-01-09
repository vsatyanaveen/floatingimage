package dk.nindroid.rss.launchers;

import dk.nindroid.rss.FeedController;

public class ReadFeeds implements Runnable {
	private FeedController 		mFeedController;
	private int					mActive;
	
	public static void runAsync(FeedController feedController, int active){
		new Thread(new ReadFeeds(feedController, active)).start();
	}
	
	public ReadFeeds(FeedController feedController, int active){
		this.mFeedController = feedController;
		this.mActive = active;
	}
	
	@Override
	public void run() {
		mFeedController.readFeeds(mActive, false);
	}
}
