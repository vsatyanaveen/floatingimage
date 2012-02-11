package dk.nindroid.rss.parser.photobucket;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import android.content.Context;
import android.util.Log;
import dk.nindroid.rss.data.FeedReference;
import dk.nindroid.rss.data.ImageReference;
import dk.nindroid.rss.parser.FeedParser;
import dk.nindroid.rss.settings.Settings;


public class PhotobucketParser extends DefaultHandler implements FeedParser {
	StringBuilder sb;
	List<ImageReference> imgs;
	PhotobucketImage image;
	Stack<String> albums;
	String currentPrefix = "";
	
	public PhotobucketParser(){
		imgs = new ArrayList<ImageReference>();
		albums = new Stack<String>();
	}
	
	protected List<ImageReference> getData() {
		return imgs;
	}
	
	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		super.startElement(uri, localName, qName, attributes);
		String n = qName.toLowerCase();
		if("album".equals(n)){
			albums.add(attributes.getValue("name"));
			updateCurrentPrefix();
		}else if("media".equals(n)){
			if(attributes.getValue("type").equals("image")){
				image = new PhotobucketImage();
				image.setID(currentPrefix + attributes.getValue("name"));
				image.setOwner(attributes.getValue("username"));
			}
		}else if("browseurl".equals(n) || "url".equals(n) || "thumb".equals(n) || "description".equals(n) || "title".equals(n)){
			sb = new StringBuilder();
		}
	}
	
	void updateCurrentPrefix(){
		currentPrefix = "";
		boolean first = true;
		for(String album : albums){
			if(!first){
				currentPrefix += "_";
			}
			first = false;
			currentPrefix += album;
		}
	}
	
	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		super.endElement(uri, localName, qName);
		String n = qName.toLowerCase();
		if("album".equals(n)){
			this.albums.pop();
			updateCurrentPrefix();
		}else if(this.image != null){
			if("media".equals(n)){
				imgs.add(this.image);
				this.image = null;
			}else if("browseurl".equals(n)){
				this.image.pageURL = sb.toString();
			}else if("url".equals(n)){
				this.image.sourceURL = sb.toString();
			}else if("thumb".equals(n)){
				this.image.thumbURL = sb.toString();
			}else if("description".equals(n)){
				// Nothing :(
			}else if("title".equals(n)){
				this.image.title = sb.toString();
			}
		}
		this.sb = null;
	}
	
	@Override
	public void characters(char[] ch, int start, int length)
		throws SAXException {
		if(sb != null){
			sb.append(ch, start, length);
		}
	}

	@Override
	public List<ImageReference> parseFeed(FeedReference feed, Context context)
			throws ParserConfigurationException, SAXException,
			FactoryConfigurationError, IOException {
		try {
			Log.v("Floating Image", "Fetching from photobucket: " + feed.getFeedLocation());
			for(int i = 1; i < 6; ++i){
				String xml = PhotobucketFeeder.getImages(feed.getFeedLocation(), i);
				SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
				XMLReader xmlReader = parser.getXMLReader();
				xmlReader.setContentHandler(this);
				xmlReader.parse(new InputSource(new StringReader(xml)));
			}
		} catch (Exception e) {
			Log.w("Floating Image", "Error getting image list from Photobucket", e);
		}
		return imgs;
	}

	@Override
	public void init(Settings settings) {
		
	}
}
