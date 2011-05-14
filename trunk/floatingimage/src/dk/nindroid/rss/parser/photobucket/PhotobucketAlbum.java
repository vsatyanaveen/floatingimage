package dk.nindroid.rss.parser.photobucket;

public class PhotobucketAlbum {
	String name;
	String url;
	// Other stuff?
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public PhotobucketAlbum(String name, String url) {
		super();
		this.name = name;
		this.url = url;
	}
	/**
	 * Please set values manually. Pretty please?
	 */
	public PhotobucketAlbum(){}
}
