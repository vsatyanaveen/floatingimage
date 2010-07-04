package dk.nindroid.rss.parser.picasa;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

public class GetAlbumsParser extends DefaultHandler {
	List<PicasaAlbum> albums = new ArrayList<PicasaAlbum>();
	PicasaAlbum current;
	StringBuilder sb = null;
	
	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		super.startElement(uri, localName, qName, attributes);
		
		if(PicasaTags.ENTRY.equalsIgnoreCase(localName)){
			current = new PicasaAlbum();
		}else if(PicasaTags.TITLE.equalsIgnoreCase(localName)){
			sb = new StringBuilder();
		}else if(PicasaTags.ALBUM_ID.equalsIgnoreCase(localName)){
			sb = new StringBuilder();
		}else if(PicasaTags.ALBUM_SUMMARY.equalsIgnoreCase(localName)){
			sb = new StringBuilder();
		}
	}
	
	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		super.endElement(uri, localName, qName);
		if(current == null) return;
		if(PicasaTags.ALBUM_SUMMARY.equalsIgnoreCase(localName) && sb != null){
			current.setSummary(sb.toString());
			sb = null;
		}else if(PicasaTags.ALBUM_ID.equalsIgnoreCase(localName) && sb != null){
			Log.v("Floating Image", "Recording Album ID: " + sb.toString());
			current.setId(sb.toString());
			sb = null;
		}else if(PicasaTags.TITLE.equalsIgnoreCase(localName) && sb != null){
			current.setTitle(sb.toString());
			sb = null;
		}else if(PicasaTags.ENTRY.equalsIgnoreCase(localName)){
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
	
	public List<PicasaAlbum> getAlbums(){
		return albums;
	}
}
