package dk.nindroid.rss.parser.flickr;

public class FlickrUser {
	String nsid;
	String username;
	public FlickrUser(String nsid, String username){
		this.nsid = nsid;
		this.username = username;
	}
	public String getNsid() {
		return nsid;
	}
	public String getUsername() {
		return username;
	}
}
