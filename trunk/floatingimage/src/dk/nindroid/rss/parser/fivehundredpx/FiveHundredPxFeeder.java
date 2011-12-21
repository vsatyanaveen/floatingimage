package dk.nindroid.rss.parser.fivehundredpx;

public class FiveHundredPxFeeder {
	private static final String CONSUMER_KEY = "CGJHowNEp3IdvWEnfGC0Zcmf5UpqyjCcKuEz7qUJ";
	
	private static final String BASE_URL = "https://api.500px.com/v1/photos";
	private static final String IMAGE_LIST_URL = BASE_URL + "?rpp=100&sort=created_at&consumer_key=" + CONSUMER_KEY;
	private static final String FEATURE_URL = IMAGE_LIST_URL + "&feature=";
	private static final String CATEGORY_URL = IMAGE_LIST_URL + "&only=";
	private static final String USER_URL = IMAGE_LIST_URL + "&user=";
	private static final String USER_FRIENDS_URL = IMAGE_LIST_URL + "&user_friends=";
	private static final String USER_FAVORITES_URL = IMAGE_LIST_URL + "&user_favorites=";
	private static final String SEARCH_URL = IMAGE_LIST_URL + "&term=";
	private static final String GET_USER = "https://api.500px.com/v1/users/show?consumer_key=" + CONSUMER_KEY + "&username=";
	
	public static final String CATEGORY_CELEBRITIES = "Celebrities";
	public static final String CATEGORY_FILM = "Film";
	public static final String CATEGORY_JOURNALISM = "Journalism";
	public static final String CATEGORY_NUDE = "Nude";
	public static final String CATEGORY_BLACK_AND_WHITE = "Black and White";
	public static final String CATEGORY_STILL_LIFE = "Still Life";
	public static final String CATEGORY_PEOPLE = "People";
	public static final String CATEGORY_LANDSCAPES = "Landscapes";
	public static final String CATEGORY_CITY_AND_ARCHITECTURE = "City and Architecture";
	public static final String CATEGORY_ABSTRACT = "Abstract";
	public static final String CATEGORY_ANIMALS = "Animals";
	public static final String CATEGORY_MACRO = "Macro";
	public static final String CATEGORY_TRAVEL = "Travel";
	public static final String CATEGORY_FASHION = "Fashion";
	public static final String CATEGORY_COMMERCIAL = "Commercial";
	public static final String CATEGORY_CONCERT = "Concert";
	public static final String CATEGORY_SPORT = "Sport";
	public static final String CATEGORY_NATURE = "Nature";
	public static final String CATEGORY_PERFORMING_ARTS = "Performing Arts";
	public static final String CATEGORY_FAMILY = "Family";
	public static final String CATEGORY_STREET = "Street";
	public static final String CATEGORY_UNDERWATER = "Underwater";
	public static final String CATEGORY_FOOD = "Food";
	public static final String CATEGORY_FINE_ART = "Fine Art";
	
	public static String getPopular(){
		return FEATURE_URL + "popular";
	}
	
	public static String getUpcoming(){
		return FEATURE_URL + "upcoming";
	}
	
	public static String getEditors(){
		return FEATURE_URL + "editors";
	}
	
	public static String getToday(){
		return FEATURE_URL + "today";
	}
	
	public static String getYesterday(){
		return FEATURE_URL + "yesterday";
	}
	
	public static String getWeek(){
		return FEATURE_URL + "week";
	}
	
	public static String getCategory(String category){
		return CATEGORY_URL + category;
	}
	
	public static String appendPage(String url, int page){
		return url + "&page=" + page;
	}
	
	public static String censor(String url){
		return url + "&exclude=" + CATEGORY_NUDE;
	}
	
	public static String getExtendedPhoto(String id, boolean large){
		return BASE_URL + "/" + id + "?image_size=" + (large ? "3" : "2") + "&consumer_key=" + CONSUMER_KEY;
	}
	
	public static String getSearch(String term){
		return SEARCH_URL + term;
	}
	
	public static String getUser(String user){
		return USER_URL + user;
	}
	
	public static String getUserFriends(String user){
		return USER_FRIENDS_URL + user;
	}
	
	public static String getUserFavorites(String user){
		return USER_FAVORITES_URL + user;
	}
	
	public static String getUserProfile(String user){
		return GET_USER + user;
	}
}
