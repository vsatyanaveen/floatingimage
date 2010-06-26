package dk.nindroid.rss.parser.flickr;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class GetAlbumsParser extends DefaultHandler {
	List<FlickrAlbum> albums;
	FlickrAlbum current;
	StringBuilder sb = null;
	
	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		super.startElement(uri, localName, qName, attributes);
		if(localName.equals(GetAlbumsTags.PHOTOSETS)){
			albums = new ArrayList<FlickrAlbum>();
		}else if(localName.equals(GetAlbumsTags.PHOTOSET)){
			String id = attributes.getValue(GetAlbumsTags.PHOTOSET_ID);
			String primary = attributes.getValue(GetAlbumsTags.PHOTOSET_PRIMARY);
			String secret = attributes.getValue(GetAlbumsTags.PHOTOSET_SECRET);
			String farm = attributes.getValue(GetAlbumsTags.PHOTOSET_FARM);
			String server = attributes.getValue(GetAlbumsTags.PHOTOSET_SERVER);
			String count = attributes.getValue(GetAlbumsTags.PHOTOSET_PHOTOS);
			current = new FlickrAlbum(id, primary, secret, farm, server, count);
		}else if(localName.equals(GetAlbumsTags.TITLE)){
			sb = new StringBuilder();
		}
	}
	
	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		super.endElement(uri, localName, qName);
		if(localName.equals(GetAlbumsTags.TITLE)){
			current.setName(sb.toString());
			sb = null;
		}else if(localName.equals(GetAlbumsTags.PHOTOSET)){
			albums.add(current);
			current = null;
		}
	}
	
	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		super.characters(ch, start, length);
		if(sb != null){
			sb.append(ch, start, length);
		}
	}
	
	public List<FlickrAlbum> getData(){
		return albums;
	}
}
