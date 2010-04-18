package dk.nindroid.rss.parser.flickr;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import dk.nindroid.rss.data.ImageReference;
import dk.nindroid.rss.flickr.FlickrImage;
import dk.nindroid.rss.parser.FeedParser;

public class FlickrParser extends FeedParser {
	List<ImageReference> imgs;
	StringBuilder data = new StringBuilder();
	
	@Override
	public void startElement(String uri, String localName, String name,
			Attributes attributes) throws SAXException {
		super.startElement(uri, localName, name, attributes);
		if(localName.equals(ExploreTags.PHOTOS)){
			imgs = new ArrayList<ImageReference>(); 
		}
		else if(localName.equals(ExploreTags.PHOTO)){
			String id = attributes.getValue(ExploreTags.PHOTO_ID);
			String owner = attributes.getValue(ExploreTags.PHOTO_OWNER);
			String secret = attributes.getValue(ExploreTags.PHOTO_SECRET);
			String server = attributes.getValue(ExploreTags.PHOTO_SERVER);
			String title = attributes.getValue(ExploreTags.PHOTO_TITLE);
			String farm = attributes.getValue(ExploreTags.PHOTO_FARM);
			imgs.add(new FlickrImage(farm, server, id, secret, title, owner, true, false));
		}
	}
	
	public List<ImageReference> getData(){
		return imgs;
	}
}
