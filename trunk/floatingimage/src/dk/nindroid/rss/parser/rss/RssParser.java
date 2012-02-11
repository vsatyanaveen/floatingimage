package dk.nindroid.rss.parser.rss;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import dk.nindroid.rss.data.ImageReference;
import dk.nindroid.rss.parser.XMLParser;

public class RssParser extends XMLParser {
	private List<ImageReference> data = new ArrayList<ImageReference>();
	RssImage curData;
	boolean inItem = false;
	StringBuilder sb = new StringBuilder();
	
	@Override
	protected List<ImageReference> getData() {
		return data;
	}
	
	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		super.startElement(uri, localName, qName, attributes);
		if(localName.equals(RssTags.ITEM)){
			curData = new RssImage();
		}else if(curData != null){
			if(localName.equals(RssTags.CONTENT)){
				curData.largeUrl = attributes.getValue(RssTags.URL);
			}else if(localName.equals(RssTags.THUMBNAIL)){
				curData.thumbnailUrl = attributes.getValue(RssTags.URL);
			}else if(localName.equals(RssTags.TITLE)       || localName.equals(RssTags.LINK) || 
					 localName.equals(RssTags.GUID) || localName.equals(RssTags.CREDIT) || 
					 localName.equals(RssTags.CREATOR)){
				sb = new StringBuilder();
			}
		}
	}
	
	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		super.endElement(uri, localName, qName);
		if(localName.equals(RssTags.ITEM)){
			data.add(curData);
			curData = null;
		}else if(curData != null){
			if(RssTags.TITLE.equals(localName)){
				curData.title = sb.toString();
			}else if(RssTags.LINK.equals(localName)){
				curData.pageUrl = sb.toString();
			}else if(RssTags.GUID.equals(localName)){
				curData.guid = sb.toString().replace(":", "_").replace("/", "_");
			}else if(RssTags.CREDIT.equals(localName) || RssTags.CREATOR.equals(localName)){
				curData.owner = sb.toString();
			}
		}
		sb = null;
	}

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		if(sb != null){
			sb.append(ch, start, length);
		}
	}
}
