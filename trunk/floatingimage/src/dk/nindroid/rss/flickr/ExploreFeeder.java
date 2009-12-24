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
import dk.nindroid.rss.parser.ExploreParser;

public class ExploreFeeder {
	public static List<ImageReference> getImageUrls(){
		try {
			InputStream stream = HttpTools.openHttpConnection("http://api.flickr.com/services/rest/?method=flickr.interestingness.getList&api_key=f6fdb5a636863d148afa8e7bb056bf1b&per_page=500");
			//InputStream stream = HttpTools.openHttpConnection("http://api.flickr.com/services/rest/?method=flickr.people.getPublicPhotos&api_key=f6fdb5a636863d148afa8e7bb056bf1b&per_page=500&user_id=73523270@N00");			
			return parseStream(stream);
		} catch (IOException e) {
			Log.e("ExploreFeeder", "Unexpected exception caught", e);
		} catch (ParserConfigurationException e) {
			Log.e("ExploreFeeder", "Unexpected exception caught", e);
		} catch (SAXException e) {
			Log.e("ExploreFeeder", "Unexpected exception caught", e);
		} catch (FactoryConfigurationError e) {
			Log.e("ExploreFeeder", "Unexpected exception caught", e);
		}
		return null;
	}
	
	public static List<ImageReference> parseStream(InputStream stream) throws ParserConfigurationException, SAXException, FactoryConfigurationError, IOException{
		SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
		XMLReader xmlReader = parser.getXMLReader();
		ExploreParser exParser = new ExploreParser();
		xmlReader.setContentHandler(exParser);
		xmlReader.parse(new InputSource(stream));
		return exParser.getData();
	}
}
