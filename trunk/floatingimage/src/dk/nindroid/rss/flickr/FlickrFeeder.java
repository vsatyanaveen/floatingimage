package dk.nindroid.rss.flickr;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import android.util.Log;
import dk.nindroid.rss.HttpTools;
import dk.nindroid.rss.data.ImageReference;
import dk.nindroid.rss.parser.flickr.FlickrParser;
import dk.nindroid.rss.parser.flickr.FindByUsernameParser;
import dk.nindroid.rss.parser.flickr.ImageSizesParser;
import dk.nindroid.rss.parser.flickr.data.ImageSizes;

public class FlickrFeeder {
	private static final String API_KEY = "f6fdb5a636863d148afa8e7bb056bf1b";
	private static final String EXPLORE = "http://api.flickr.com/services/rest/?method=flickr.interestingness.getList&api_key=" + API_KEY + "&per_page=500";
	private static final String FIND_BY_USERNAME = "http://api.flickr.com/services/rest/?method=flickr.people.findByUsername&api_key=" + API_KEY + "&username=";
	private static final String GET_PUBLIC_PHOTOS = "http://api.flickr.com/services/rest/?method=flickr.people.getPublicPhotos&api_key=" + API_KEY + "&per_page=500&user_id=";
	private static final String SEARCH = "http://api.flickr.com/services/rest/?method=flickr.photos.search&api_key=" + API_KEY + "&safe_search=2&per_page=500&tags=";
	private static final String IMAGE_SIZES = "http://api.flickr.com/services/rest/?method=flickr.photos.getSizes&api_key=" + API_KEY + "&photo_id=";
	
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
