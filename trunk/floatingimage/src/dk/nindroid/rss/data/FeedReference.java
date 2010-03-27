package dk.nindroid.rss.data;

import dk.nindroid.rss.parser.FeedParser;

public class FeedReference {
	private FeedParser 	mParser;
	private String 		mFeedLocation;
	private String		mName;
	public FeedParser getParser(){
		return mParser;
	}
	
	public String getFeedLocation(){
		return mFeedLocation;
	}
	public String getName(){
		return mName;
	}
	
	public FeedReference(FeedParser parser, String feedLocation, String name){
		this.mParser = parser;
		this.mFeedLocation = feedLocation;
	}
	
	@Override
	public boolean equals(Object o) {
		if(o instanceof FeedReference){
			FeedReference feed = (FeedReference)o;
			return mFeedLocation.equals(feed.mFeedLocation);
		}else if(o instanceof String){
			String feed = (String)o;
			return mFeedLocation.equals(feed);
		}
		return false;
	}
}
