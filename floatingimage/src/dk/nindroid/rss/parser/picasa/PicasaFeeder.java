package dk.nindroid.rss.parser.picasa;

public class PicasaFeeder {
	private static final String BASE_URL = "http://picasaweb.google.com/data/feed/api/";
	private static final String USER_URL = BASE_URL + "user/";
	private static final String SEARCH   = "all?max-results=500&q=";
	private static final String POST_RECENT   = "?kind=photo&max-results=500";
	
	public static String getSearchUrl(String query){
		return BASE_URL + SEARCH + query.replace(" ", "%20");
	}
	
	public static String getRecent(String userID){
		return USER_URL + userID + POST_RECENT;
	}
	
	public static String getAlbums(String userID){
		return USER_URL + userID;
	}
}
