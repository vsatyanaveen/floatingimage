package dk.nindroid.rss.parser.flickr;

public class FlickrAlbum implements Comparable<FlickrAlbum>{
	String name;
	String id;
	String secret;
	String primary;
	String farm;
	String server;
	String count;
	public FlickrAlbum(String id, String primary, String secret, String farm, String server, String count){
		this.id = id;
		this.primary = primary;
		this.secret = secret;
		this.farm = farm;
		this.server = server;
		this.count = count;
	}
	public FlickrAlbum(String name, String primary, String id, String secret, String farm, String server, String count){
		this(id, primary, secret, farm, server, count);
		this.name = name;
	}
	public String getName() {
		return name;
	}
	public String getId() {
		return id;
	}
	public String getSecret() {
		return secret;
	}
	public String getFarm() {
		return farm;
	}
	public String getServer() {
		return server;
	}
	public String getCount() {
		return count;
	}
	public void setName(String name) {
		this.name = name;
	}
	public void setId(String id) {
		this.id = id;
	}
	public void setSecret(String secret) {
		this.secret = secret;
	}
	public void setFarm(String farm) {
		this.farm = farm;
	}
	public void setServer(String server) {
		this.server = server;
	}
	public void setCount(String count) {
		this.count = count;
	}
	@Override
	public int compareTo(FlickrAlbum another) {
		return name.compareTo(another.name);
	}
	
}
