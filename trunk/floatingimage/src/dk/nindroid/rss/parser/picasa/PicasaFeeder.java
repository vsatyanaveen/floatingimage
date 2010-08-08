package dk.nindroid.rss.parser.picasa;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.List;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.basic.DefaultOAuthConsumer;
import oauth.signpost.basic.DefaultOAuthProvider;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import dk.nindroid.rss.HttpTools;


public class PicasaFeeder {
	private static final String BASE_URL = "http://picasaweb.google.com/data/feed/api/";
	private static final String USER_URL = BASE_URL + "user/";
	private static final String MY_URL = USER_URL + "default?imgmax=1024&";
	private static final String SEARCH   = "all?imgmax=1024&max-results=500&q=";
	private static final String POST_RECENT   = "?imgmax=1024&kind=photo&max-results=500";
	
	private static final String SECRET   		= "1gIH0qT3ywaVuLWzSsbBChjj";
	private static final String KEY		 		= "floating-image.appspot.com";
	private static final String SCOPE			= "http://picasaweb.google.com/data";
	private static final String CALLBACK		= "floatingimage://picasa";
	
	private static final String REQUEST_TOKEN_ENDPOINT = "https://www.google.com/accounts/OAuthGetRequestToken?scope=";
	private static final String AUTHORIZE_ENDPOINT = "https://www.google.com/accounts/OAuthAuthorizeToken?btmpl=mobile&hd=default";
	private static final String ACCESS_TOKEN_ENDPOINT = "https://www.google.com/accounts/OAuthGetAccessToken";
	
	private static String requestToken;
	private static String requestTokenSecret;
	
	private static String consumerKey;
	private static String consumerSecret;
	private static String consumerAccessToken;
	private static String consumerAccessSecret;
	
	public static String getSearchUrl(String query){
		return BASE_URL + SEARCH + query.replace(" ", "%20");
	}
	
	public static String getRecent(String userID){
		return USER_URL + userID + POST_RECENT;
	}
	
	public static String getMyRecent(){
		return USER_URL + "default" + POST_RECENT;
	}
	
	public static List<PicasaAlbum> getAlbums(String userID, Context c){
		if(userID == null){
			userID = "default"; // Own user
		}
		String url = USER_URL + userID;
		url = signUrl(url, c);
		Log.v("Floating Image", "Reading own albums: " + url);
		InputStream stream;
		try {
			stream = HttpTools.openHttpConnection(url);
			return parseGetAlbums(stream);
		} catch (IOException e) {
			Log.e("FlickrFeeder", "Unexpected exception caught", e);
		} catch (ParserConfigurationException e) {
			Log.e("FlickrFeeder", "Unexpected exception caught", e);
		} catch (SAXException e) {
			Log.e("FlickrFeeder", "Unexpected exception caught", e);
		} catch (FactoryConfigurationError e) {
			Log.e("FlickrFeeder", "Unexpected exception caught", e);
		}
		return null;
	}
	
	public static List<PicasaAlbum> parseGetAlbums(InputStream stream) throws ParserConfigurationException, SAXException, FactoryConfigurationError, IOException{
		SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
		XMLReader xmlReader = parser.getXMLReader();
		GetAlbumsParser contentHandler = new GetAlbumsParser();
		xmlReader.setContentHandler(contentHandler);
		xmlReader.parse(new InputSource(stream));
		return contentHandler.getAlbums();
	}
	
	public static String getMyAlbums(){
		return MY_URL;
	}
	
	public static String getAlbum(String user, String album){
		if(user == null){
			user = "default";
		}
		return USER_URL + user + "/albumid/" + album;
	}
	
	public static boolean isSignedIn(Context c){
		if(consumerKey == null || consumerSecret == null || consumerAccessToken == null || consumerAccessSecret == null){
			SharedPreferences sp = c.getSharedPreferences("dk.nindroid.rss_preferences", 0);
			consumerKey = sp.getString("PICASA_CONSUMER_KEY", null);
			consumerSecret = sp.getString("PICASA_CONSUMER_SECRET", null);
			consumerAccessToken = sp.getString("PICASA_ACCESS_TOKEN", null);
			consumerAccessSecret = sp.getString("PICASA_ACCESS_TOKEN_SECRET", null);
			if(consumerKey == null || consumerSecret == null || consumerAccessToken == null || consumerAccessSecret == null){
				Log.v("Floating Image", "Not signed in to Picasa");
				return false;
			}
		}
		Log.v("Floating Image", "Signed in to Picasa");
		return true;
	}
	
	public static String signUrl(String url, Context c){
		if(isSignedIn(c)){
			OAuthConsumer consumer = new DefaultOAuthConsumer(KEY, SECRET);
			consumer.setTokenWithSecret(consumerAccessToken, consumerAccessSecret);
			try {
				Log.v("Floating Image", "Signing URL");
				return consumer.sign(url);
			} catch (Exception e) {
				Log.w("Floating Image", "Error signing Picasa URL: " + url, e);
			}
		}
		return url;
	}
	
	public static void signOut(Context c){
		consumerKey = null;
		consumerSecret = null;
		consumerAccessToken = null;
		consumerAccessSecret = null;
		SharedPreferences sp = c.getSharedPreferences("dk.nindroid.rss_preferences", 0);
		SharedPreferences.Editor e = sp.edit();
		e.remove("PICASA_CONSUMER_KEY");
		e.remove("PICASA_CONSUMER_SECRET");
		e.remove("PICASA_ACCESS_TOKEN");
		e.remove("PICASA_ACCESS_TOKEN_SECRET");
		e.commit();
	}
	
	public static void signIn(Context context) {
		String url = REQUEST_TOKEN_ENDPOINT + URLEncoder.encode(SCOPE);
		OAuthConsumer consumer = new DefaultOAuthConsumer(KEY, SECRET);
		OAuthProvider provider = new DefaultOAuthProvider(url, null, AUTHORIZE_ENDPOINT);
		try {
			url = provider.retrieveRequestToken(consumer, CALLBACK);
		} catch (Exception e) {
			Log.e("Floating Image", "Unexpected exception caught!", e);
		}
		Log.v("Floating Image", url);
		
		requestToken = consumer.getToken();
		requestTokenSecret = consumer.getTokenSecret();
		
		Intent intent = new Intent(context, PicasaWebAuth.class);
		intent.putExtra("URL", url);
		context.startActivity(intent);
	}
	
	public static void setAuth(String verifier, String authToken, Context c){
		OAuthConsumer consumer = new DefaultOAuthConsumer(KEY, SECRET);
		consumer.setTokenWithSecret(requestToken, requestTokenSecret);
		OAuthProvider provider = new DefaultOAuthProvider(null, ACCESS_TOKEN_ENDPOINT, null);
		provider.setOAuth10a(true);
		try {
			provider.retrieveAccessToken(consumer, verifier);
		} catch (Exception e) {
			Log.w("Floating Image", "Could not retrieve Picasa Access token", e);		
		}
		
		consumerKey = consumer.getConsumerKey();
		consumerSecret = consumer.getConsumerSecret();
		consumerAccessToken = consumer.getToken();
		consumerAccessSecret = consumer.getTokenSecret();
		
		SharedPreferences sp = c.getSharedPreferences("dk.nindroid.rss_preferences", 0);
		SharedPreferences.Editor e = sp.edit();
		e.putString("PICASA_CONSUMER_KEY", consumerKey);
		e.putString("PICASA_CONSUMER_SECRET", consumerSecret);
		e.putString("PICASA_ACCESS_TOKEN", consumerAccessToken);
		e.putString("PICASA_ACCESS_TOKEN_SECRET", consumerAccessSecret);
		e.commit();
	}
}
