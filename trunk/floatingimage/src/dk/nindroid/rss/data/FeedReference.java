package dk.nindroid.rss.data;

import dk.nindroid.rss.parser.FeedParser;

public class FeedReference {
	private int			mId;
	private FeedParser 	mParser;
	private String 		mFeedLocation;
	private String		mName;
	private int			mType;
	private int			mSorting;
	
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
	
	public int getSorting(){
		return mSorting;
	}
	
	public int getId(){
		return this.mId;
	}
	
	public FeedReference(int id, FeedParser parser, String feedLocation, String name, int type, int sorting){
		this.mId = id;
		this.mParser = parser;
		this.mFeedLocation = feedLocation;
		this.mName = name;
		this.mType = type;
		this.mSorting = sorting;
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
