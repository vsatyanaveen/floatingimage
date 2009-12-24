package dk.nindroid.rss.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

import dk.nindroid.rss.data.RssElement;

public class RSSParser extends DefaultHandler {
	private List<RssElement> data = new ArrayList<RssElement>();
	RssElement curData;
	boolean inItem = false;
	Stack<String> stack = new Stack<String>(); 
	StringBuilder sb = new StringBuilder();
	
	public List<RssElement> getData(){
		return data;
	}
	
	/**
	 * If item, create new element, if parent is item and a coveted field, add to stack for data extraction.
	 */
	@Override
	public void startElement(String uri, String localName, String name,
			Attributes attributes) throws SAXException {
		if(localName.equals(RSSTags.ITEM)){
			curData = new RssElement();
			stack.push(RSSTags.ITEM);
		}else if(uri.length() == 0 && !stack.isEmpty() && stack.peek().equals(RSSTags.ITEM) && (
				 localName.equals(RSSTags.TITLE)       || localName.equals(RSSTags.LINK) ||
				 localName.equals(RSSTags.DESCRIPTION) || localName.equals(RSSTags.DATE)
			)){
			stack.push(localName);
		}
	}
	
	@Override
	public void endElement(String uri, String localName, String name)
			throws SAXException {
		if(localName.equals(RSSTags.ITEM)){
			data.add(curData);
			curData = null;
			stack.pop();
		}else if(uri.length() == 0 && !stack.isEmpty()){
			if(RSSTags.TITLE.equals(localName)){
				Log.v("RSS Parser", sb.toString() + " discovered");
				curData.setTitle(sb.toString());
				stack.pop();
				sb = new StringBuilder();
			}else if(RSSTags.LINK.equals(localName)){
				curData.setLink(sb.toString());
				stack.pop();
				sb = new StringBuilder();
			}else if(RSSTags.DESCRIPTION.equals(localName)){
				curData.setDescription(sb.toString());
				stack.pop();
				sb = new StringBuilder();
			}else if(RSSTags.DATE.equals(localName)){
				curData.setDate(sb.toString());
				stack.pop();
				sb = new StringBuilder();
			}
		}
	}
	
	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		if(stack.isEmpty()) return;
		String d = stack.peek();
		if(d.equals(RSSTags.TITLE) || d.equals(RSSTags.LINK) || d.equals(RSSTags.DESCRIPTION) || d.equals(RSSTags.DATE)){
			sb.append(ch, start, length);
		}
	}
}
