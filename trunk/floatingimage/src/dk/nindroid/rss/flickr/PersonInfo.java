package dk.nindroid.rss.flickr;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import android.util.Log;
import dk.nindroid.rss.HttpTools;
import dk.nindroid.rss.data.FlickrUserInfo;
import dk.nindroid.rss.parser.FlickrUserInfoParser;

public class PersonInfo {
	public static FlickrUserInfo getInfo(String id){
		try {
			InputStream is = HttpTools.openHttpConnection("http://api.flickr.com/services/rest/?method=flickr.people.getInfo&api_key=f6fdb5a636863d148afa8e7bb056bf1b&user_id=" + id);
			return parseStream(is);
		} catch (ParserConfigurationException e) {
			Log.v("dk.nindroid.rss.flickr.PersonInfo", "Parse error!", e);
		} catch (SAXException e) {
			Log.v("dk.nindroid.rss.flickr.PersonInfo", "Parse error!", e);
		} catch (FactoryConfigurationError e) {
			Log.v("dk.nindroid.rss.flickr.PersonInfo", "Parse error!", e);
		} catch (IOException e) {
			Log.v("dk.nindroid.rss.flickr.PersonInfo", "Connection error!", e);
		}
		return null;
	}
	
	public static FlickrUserInfo parseStream(InputStream stream) throws ParserConfigurationException, SAXException, FactoryConfigurationError, IOException{
		SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
		XMLReader xmlReader = parser.getXMLReader();
		FlickrUserInfoParser uiParser = new FlickrUserInfoParser();
		xmlReader.setContentHandler(uiParser);
		xmlReader.parse(new InputSource(stream));
		return uiParser.getData();
	}
}
