package dk.nindroid.rss.parser.picasa;

public class PicasaAlbum implements Comparable<PicasaAlbum>{
	String id;
	String title;
	String summary;
	PicasaAlbum(){
		
	}
	public String getId() {
		return id;
	}
	public String getTitle() {
		return title;
	}
	public String getSummary() {
		return summary;
	}
	void setId(String id) {
		this.id = id;
	}
	void setTitle(String title) {
		this.title = title;
	}
	void setSummary(String summary) {
		this.summary = summary;
	}
	@Override
	public int compareTo(PicasaAlbum another) {
		return title.compareTo(another.title);
	}
}
