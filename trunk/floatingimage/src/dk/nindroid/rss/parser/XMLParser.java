package dk.nindroid.rss.parser;

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
import org.xml.sax.helpers.DefaultHandler;

import android.content.Context;
import android.util.Log;
import dk.nindroid.rss.HttpTools;
import dk.nindroid.rss.data.FeedReference;
import dk.nindroid.rss.data.ImageReference;
import dk.nindroid.rss.settings.Settings;

public abstract class XMLParser extends DefaultHandler implements FeedParser {
	protected Settings settings;
	
	public List<ImageReference> parseFeed(FeedReference feed, Context context) throws ParserConfigurationException, SAXException, FactoryConfigurationError, IOException {
		String url = feed.getFeedLocation();
		url = extendURL(url, context);
		Log.v("Floating Image", "Fetching from " + url);
		InputStream stream = HttpTools.openHttpConnection(url);
		SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
		XMLReader xmlReader = parser.getXMLReader();
		xmlReader.setContentHandler(this);
		xmlReader.parse(new InputSource(stream));
		List<ImageReference> list = getData();
		
		if(list != null){
			if(list.isEmpty()){
				return null;
			}
			Log.v("FeedController", list.size() + " photos found.");
		}
		return list;
	}

	protected String extendURL(String url, Context context){return url;}
	protected abstract List<ImageReference> getData();
	
	@Override
	public void init(Settings settings) {
		this.settings = settings;
	}
}
