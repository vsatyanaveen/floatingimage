package dk.nindroid.rss.parser.picasa;

import java.io.IOException;
import java.net.URLEncoder;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.basic.DefaultOAuthConsumer;
import oauth.signpost.basic.DefaultOAuthProvider;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;
import android.util.Log;

public class PicasaFeeder {
	private static final String BASE_URL = "http://picasaweb.google.com/data/feed/api/";
	private static final String USER_URL = BASE_URL + "user/";
	private static final String SEARCH   = "all?max-results=500&q=";
	private static final String POST_RECENT   = "?kind=photo&max-results=500";
	
	private static final String SECRET   	= "1gIH0qT3ywaVuLWzSsbBChjj";
	private static final String KEY		 	= "floating-image.appspot.com";
	private static final String SCOPE		= "http://picasaweb.google.com/data";
	
	public static String getSearchUrl(String query){
		return BASE_URL + SEARCH + query.replace(" ", "%20");
	}
	
	public static String getRecent(String userID){
		return USER_URL + userID + POST_RECENT;
	}
	
	public static String getAlbums(String userID){
		return USER_URL + userID;
	}
	
	public static boolean signIn() throws IOException, OAuthMessageSignerException, OAuthExpectationFailedException, OAuthCommunicationException, OAuthNotAuthorizedException{
		OAuthConsumer consumer = new DefaultOAuthConsumer(	KEY, SECRET);
		OAuthProvider provider = new DefaultOAuthProvider(	"https://www.google.com/accounts/OAuthGetRequestToken?scope=" + URLEncoder.encode(SCOPE, "utf-8"), 
															"https://www.google.com/accounts/OAuthGetAccessToken", 
															"https://www.google.com/accounts/OAuthAuthorizeToken?hd=default");
		provider.setOAuth10a(true);
		String authURL = provider.retrieveRequestToken(consumer, "http://floating-image.appspot.com/");
		
		/*
		OAuthConsumer consumer = new DefaultOAuthConsumer("matthiaskaeppler.de", "etpfOSfQ4e9xnfgOJETy4D56");
		
		String scope = "http://www.blogger.com/feeds";
		OAuthProvider provider = new DefaultOAuthProvider(
						        "https://www.google.com/accounts/OAuthGetRequestToken?scope="
						                + URLEncoder.encode(scope, "utf-8"),
						        "https://www.google.com/accounts/OAuthGetAccessToken",
						        "https://www.google.com/accounts/OAuthAuthorizeToken?hd=default");
		
		String authURL = provider.retrieveRequestToken(consumer, OAuth.OUT_OF_BAND);
		*/
		Log.v("Floating Image", "PicasaAUTH URL: " + authURL);
		
		return false;		
	}
}
