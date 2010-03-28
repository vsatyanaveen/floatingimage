package dk.nindroid.rss.launchers;

import dk.nindroid.rss.FeedController;

public class ReadFeeds implements Runnable {
	private FeedController 		mFeedController;
	
	public static void runAsync(FeedController feedController){
		new Thread(new ReadFeeds(feedController)).start();
	}
	
	public ReadFeeds(FeedController feedController){
		this.mFeedController = feedController;
	}
	
	@Override
	public void run() {
		mFeedController.readFeeds();		
	}
}
