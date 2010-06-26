package dk.nindroid.rss.parser.flickr;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class CheckTokenParser extends DefaultHandler {
	FlickrUser user;
	
	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		super.startElement(uri, localName, qName, attributes);
		if(localName.equals("user")){
			String nsid = attributes.getValue("nsid");
			String username = attributes.getValue("username");
			user = new FlickrUser(nsid, username);
		}
	}
	
	public FlickrUser getUser(){
		return user;
	}
}
