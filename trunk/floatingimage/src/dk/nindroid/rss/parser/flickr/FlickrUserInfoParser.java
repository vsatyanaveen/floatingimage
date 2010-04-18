package dk.nindroid.rss.parser.flickr;

import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import dk.nindroid.rss.flickr.FlickrUserInfo;

public class FlickrUserInfoParser extends DefaultHandler {
	FlickrUserInfo curData;
	boolean inItem = false;
	Stack<String> stack = new Stack<String>(); 
	StringBuilder sb = new StringBuilder();
	
	public FlickrUserInfo getData(){
		return curData;
	}
	
	@Override
	public void startElement(String uri, String localName, String name,
			Attributes attributes) throws SAXException {
		if(localName.equals(FlickrUserInfoTags.PERSON)){
			curData = new FlickrUserInfo();
			stack.push(FlickrUserInfoTags.PERSON);
		}else if(uri.length() == 0 && !stack.isEmpty() && stack.peek().equals(FlickrUserInfoTags.PERSON) && (
				 localName.equals(FlickrUserInfoTags.USERNAME)       || localName.equals(FlickrUserInfoTags.REALNAME) ||
				 localName.equals(FlickrUserInfoTags.MOBILEURL)
			)){
			stack.push(localName);
		}
	}
	
	@Override
	public void endElement(String uri, String localName, String name)
			throws SAXException {
		if(!stack.isEmpty()){
			if(FlickrUserInfoTags.USERNAME.equals(localName)){
				curData.setUsername(sb.toString());
				stack.pop();
				sb = new StringBuilder();
			}else if(FlickrUserInfoTags.REALNAME.equals(localName)){
				curData.setRealName(sb.toString());
				stack.pop();
				sb = new StringBuilder();
			}else if(FlickrUserInfoTags.MOBILEURL.equals(localName)){
				curData.setUrl(sb.toString());
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
		if(d.equals(FlickrUserInfoTags.USERNAME) || d.equals(FlickrUserInfoTags.MOBILEURL) || d.equals(FlickrUserInfoTags.REALNAME)){
			sb.append(ch, start, length);
		}
	}
}
