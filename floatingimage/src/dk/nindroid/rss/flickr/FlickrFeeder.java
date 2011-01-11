package dk.nindroid.rss.flickr;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import dk.nindroid.rss.DownloadUtil;
import dk.nindroid.rss.HttpTools;
import dk.nindroid.rss.data.ImageReference;
import dk.nindroid.rss.parser.Crypto;
import dk.nindroid.rss.parser.flickr.CheckTokenParser;
import dk.nindroid.rss.parser.flickr.FindByUsernameParser;
import dk.nindroid.rss.parser.flickr.FlickrAlbum;
import dk.nindroid.rss.parser.flickr.FlickrParser;
import dk.nindroid.rss.parser.flickr.FlickrUser;
import dk.nindroid.rss.parser.flickr.GetAlbumsParser;
import dk.nindroid.rss.parser.flickr.ImageSizesParser;
import dk.nindroid.rss.parser.flickr.data.ImageSizes;

public class FlickrFeeder {
	private static final String PHOTOS_FROM_HERE_CONST = "HERE";
	
	private static final String API_KEY = "f6fdb5a636863d148afa8e7bb056bf1b";
	private static final String SECRET = "3358b4c1619e1c98";
	private static final String EXPLORE = "http://api.flickr.com/services/rest/?api_key=" + API_KEY + "&auth_token=&method=flickr.interestingness.getList&per_page=500";
	private static final String FIND_BY_USERNAME = "http://api.flickr.com/services/rest/?api_key=" + API_KEY + "&auth_token=&method=flickr.people.findByUsername&username=";
	private static final String GET_PUBLIC_PHOTOS = "http://api.flickr.com/services/rest/?api_key=" + API_KEY + "&auth_token=&method=flickr.people.getPublicPhotos&per_page=500&user_id=";
	private static final String SEARCH = "http://api.flickr.com/services/rest/?api_key=" + API_KEY + "&auth_token=&method=flickr.photos.search&per_page=500&safe_search=1&tags=";
	private static final String IMAGE_SIZES = "http://api.flickr.com/services/rest/?api_key=" + API_KEY + "&method=flickr.photos.getSizes&photo_id=";
	private static final String CONTACTS_PHOTOS = "http://api.flickr.com/services/rest/?api_key=" + API_KEY + "&auth_token=&count=500&method=flickr.photos.getContactsPhotos";
	//private static final String PHOTOS_FROM_HERE = "http://api.flickr.com/services/rest/?accuracy=8&api_key=" + API_KEY + "&auth_token=&lat=&lon=&method=flickr.photos.search&per_page=500&radius=5";	
	private static final String ALBUMS = "http://api.flickr.com/services/rest/?api_key=" + API_KEY + "&auth_token=&method=flickr.photosets.getList";
	private static final String ALBUM_PHOTOS = "http://api.flickr.com/services/rest/?api_key=" + API_KEY + "&auth_token=&method=flickr.photosets.getPhotos&photoset_id=";
	
	/*  Authentication */
	private static final String AUTHENTICATION_URL = "http://flickr.com/services/auth/?api_key=" + API_KEY + "&perms=read&api_sig=";
	private static final String GET_TOKEN_URL = "http://flickr.com/services/rest/?method=flickr.auth.getToken&api_key=" + API_KEY + "&frob=";
	/* /Authentication */
	
	private static final String CHECK_TOKEN = "http://api.flickr.com/services/rest/?api_key=" + API_KEY + "&auth_token=&method=flickr.auth.checkToken";
	
	static String token = null;
	
	public static void setFrob(String frob, Context c) throws MalformedURLException, IOException{
		Log.v("Floating image", "Flickr frob: " + frob);
		String signature = SECRET + "api_key" + API_KEY + "frob" + frob + "methodflickr.auth.getToken";
		signature = getMD5(signature);
		String getToken = GET_TOKEN_URL + frob + "&api_sig=" + signature;
		Log.v("Floating Image", "Getting token:" + getToken);
		String resp = DownloadUtil.readStreamToEnd(getToken);
		Log.v("Floating Image", "Token response:\n" + resp);
		int start = resp.indexOf("<token>") + 7;
		int end = resp.indexOf("</token>");
		token = resp.substring(start, end);
		
		SharedPreferences sp = c.getSharedPreferences("dk.nindroid.rss_preferences", 0);
		SharedPreferences.Editor e = sp.edit();
		e.putString("FLICKR_CODE", token);
		e.commit();
	}
	
	public static void readCode(Context context){
		if(token == null){
			SharedPreferences sp = context.getSharedPreferences("dk.nindroid.rss_preferences", 0);
			token = sp.getString("FLICKR_CODE", null);
		}
	}
	
	public static boolean needsAuthorization(){
		return token == null;
	}
	
	public static void authorize(Context context) throws IOException{
		if(token == null){
			String signature = getMD5(SECRET + "api_key" + API_KEY + "permsread");
			String url = AUTHENTICATION_URL + signature;
			Log.v("Floating Image", "Flickr authentication: " + url);
			
			Intent intent = new Intent(context, 	WebAuth.class);
			intent.putExtra("URL", url);
			context.startActivity(intent);
		}
	}
	
	public static void unauthorize(Context context){
		token = null;
		SharedPreferences sp = context.getSharedPreferences("dk.nindroid.rss_preferences", 0);
		SharedPreferences.Editor e = sp.edit();
		e.remove("FLICKR_CODE");
		e.commit();
	}
	
	public static String signUrl(String url){
		if(token != null){
			url = url.replace("method=flickr.people.getPublicPhotos", "method=flickr.people.getPhotos");
			url = url.replace("&auth_token=", "&auth_token=" + token);
			url.replace("safe_search=1", "safe_search=3");
			String signature = SECRET + url.substring(url.indexOf('?') + 1).replace("=", "").replace("&", "");
			signature = getMD5(signature);
			return url + "&api_sig=" + signature;
		}else{
			url = url.replace("&auth_token=", "&");
		}
		return url;
	}
	
	private static String getMD5(String s){
		byte[] digest ;
		try {
			digest = MessageDigest.getInstance("MD5").digest(s.getBytes());
			return Crypto.toHex(digest);
		} catch (NoSuchAlgorithmException e) {}
		return null;
	}
	
	public static List<ImageReference> getImageUrls(String url){
		try {
			// Explore //InputStream stream = HttpTools.openHttpConnection("http://api.flickr.com/services/rest/?method=flickr.interestingness.getList&api_key=f6fdb5a636863d148afa8e7bb056bf1b&per_page=500");
			// Mine    //InputStream stream = HttpTools.openHttpConnection("http://api.flickr.com/services/rest/?method=flickr.people.getPublicPhotos&api_key=f6fdb5a636863d148afa8e7bb056bf1b&per_page=500&user_id=73523270@N00");
			InputStream stream = HttpTools.openHttpConnection(url);
			Log.v("FlickrFeeder", "Fetching stream: " + url);
			return parseStream(stream);
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
	
	public static List<ImageReference> parseStream(InputStream stream) throws ParserConfigurationException, SAXException, FactoryConfigurationError, IOException{
		SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
		XMLReader xmlReader = parser.getXMLReader();
		FlickrParser exParser = new FlickrParser();
		xmlReader.setContentHandler(exParser);
		xmlReader.parse(new InputSource(stream));
		List<ImageReference> list = exParser.getData();
		if(list != null){
			Log.v("FlickrFeeder", list.size() + " photos found.");
		}
		return list;
	}
	
	public static String getPublicPhotos(String userID){
		return GET_PUBLIC_PHOTOS + userID;
	}
	
	public static String getExplore(){
		return EXPLORE;
	}
	
	public static String getSearch(String criteria){
		return SEARCH + criteria;
	}
	
	public static String getContactsPhotos(){
		return CONTACTS_PHOTOS;
	}
	/*
	public static String getPhotosFromHere(){
		return PHOTOS_FROM_HERE_CONST;
	}
	*/
	public static String getAlbumPhotos(String id){
		return ALBUM_PHOTOS + id;
	}
	
	public static String finalizeUrl(Context context, String url){
		readCode(context);
		/*
		if(url.equals(PHOTOS_FROM_HERE_CONST)){
			LocationManager locManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
			List<String> providers = locManager.getAllProviders();
			Location location = null;
			for(String p : providers){
				 location = locManager.getLastKnownLocation(p);
				 if(location != null){
					 break;
				 }
			}
			if(location != null){
				String lat = "" + location.getLatitude();
				String lon = "" + location.getLongitude();
				url = PHOTOS_FROM_HERE.replace("&lat=", "&lat=" + lat).replace("&lon=", "&lon=" + lon);
			}else{
				return null;
			}
		}
		*/
		return signUrl(url);
	}
	
	public static FlickrUser getAuthorizedUser(){
		String url = CHECK_TOKEN;
		url = signUrl(url);
		InputStream stream;
		try {
			stream = HttpTools.openHttpConnection(url);
			return parseCheckToken(stream);
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
	
	public static FlickrUser parseCheckToken(InputStream stream) throws ParserConfigurationException, SAXException, FactoryConfigurationError, IOException{
		SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
		XMLReader xmlReader = parser.getXMLReader();
		CheckTokenParser contentHandler = new CheckTokenParser();
		xmlReader.setContentHandler(contentHandler);
		xmlReader.parse(new InputSource(stream));
		return contentHandler.getUser();
	}
	
	public static List<FlickrAlbum> getAlbums(String userID){
		String url = ALBUMS;
		if(userID != null){
			url += "&user_id=" + userID;
		}
		url = signUrl(url);
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
	
	public static List<FlickrAlbum> parseGetAlbums(InputStream stream) throws ParserConfigurationException, SAXException, FactoryConfigurationError, IOException{
		SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
		XMLReader xmlReader = parser.getXMLReader();
		GetAlbumsParser contentHandler = new GetAlbumsParser();
		xmlReader.setContentHandler(contentHandler);
		xmlReader.parse(new InputSource(stream));
		return contentHandler.getData();
	}
	
	public static String findByUsername(String username){
		int length = username.length();
		if (length > 5){
			char[] c = new char[1];
			username.getChars(length - 4, length - 3, c, 0);
			if(c[0] == '@'){
				return username;
			}
		}
		username = username.replaceAll(" ", "%20");
		String url = FIND_BY_USERNAME + username;
		InputStream stream;
		try {
			stream = HttpTools.openHttpConnection(url);
			return parseFindByUsername(stream);
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
	
	public static String parseFindByUsername(InputStream stream) throws ParserConfigurationException, SAXException, FactoryConfigurationError, IOException{
		SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
		XMLReader xmlReader = parser.getXMLReader();
		FindByUsernameParser contentHandler = new FindByUsernameParser();
		xmlReader.setContentHandler(contentHandler);
		xmlReader.parse(new InputSource(stream));
		return contentHandler.getData();
	}
	
	public static ImageSizes getImageSizes(String photoID){
		String url = IMAGE_SIZES + photoID;
		InputStream stream;
		try {
			stream = HttpTools.openHttpConnection(url);
			return parseGetImageSizes(stream);
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
	
	public static ImageSizes parseGetImageSizes(InputStream stream)throws ParserConfigurationException, SAXException, FactoryConfigurationError, IOException{
		SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
		XMLReader xmlReader = parser.getXMLReader();
		ImageSizesParser contentHandler = new ImageSizesParser();
		xmlReader.setContentHandler(contentHandler);
		xmlReader.parse(new InputSource(stream));
		return contentHandler.getData();
	}
}
