package dk.nindroid.rss.parser.photobucket;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class AlbumParser extends DefaultHandler {
	List<String> albums;
	
	int albumDepth = 0;
	
	public AlbumParser(){
		this.albums = new ArrayList<String>();
	}
	
	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		super.startElement(uri, localName, qName, attributes);
		if(qName.equalsIgnoreCase("album")){
			if(albumDepth++ == 1){
				this.albums.add(attributes.getValue("name"));
			}
		}
	}
	
	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		super.endElement(uri, localName, qName);
		if(qName.equalsIgnoreCase("album")){
			--albumDepth;
		}
	}
}
