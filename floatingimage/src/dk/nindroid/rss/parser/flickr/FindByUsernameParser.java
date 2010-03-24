package dk.nindroid.rss.parser.flickr;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class FindByUsernameParser extends DefaultHandler {
	String data = null;
	
	@Override
	public void startElement(String uri, String localName, String name,
			Attributes attributes) throws SAXException {
		super.startElement(uri, localName, name, attributes);
		if(localName.equals(FindByUsernameTags.RESPONSE)){
			if(!attributes.getValue(FindByUsernameTags.RESPONSE_STATUS).equals(FindByUsernameTags.RESPONSE_STATUS_OK)){
				// Bad response... How to signal?!
			}
		}
		else if(localName.equals(FindByUsernameTags.USER)){
			data = attributes.getValue(FindByUsernameTags.USER_ID);
		}
	}
	
	public String getData(){
		return data;
	}
}
