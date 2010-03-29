package dk.nindroid.rss.data;

import dk.nindroid.rss.parser.FeedParser;

public class FeedReference {
	private FeedParser 	mParser;
	private String 		mFeedLocation;
	private String		mName;
	private int			mType;
	
	public int getType(){
		return mType;
	}
	
	public FeedParser getParser(){
		return mParser;
	}
	
	public String getFeedLocation(){
		return mFeedLocation;
	}
	public String getName(){
		return mName;
	}
	
	public FeedReference(FeedParser parser, String feedLocation, String name, int type){
		this.mParser = parser;
		this.mFeedLocation = feedLocation;
		this.mName = name;
		this.mType = type;
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
