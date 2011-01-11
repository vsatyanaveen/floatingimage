package dk.nindroid.rss.parser.picasa;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import android.content.Context;
import dk.nindroid.rss.data.ImageReference;
import dk.nindroid.rss.parser.XMLParser;
import dk.nindroid.rss.picasa.PicasaImage;

public class PicasaParser extends XMLParser {
		List<ImageReference> imgs;
		PicasaImage image;
		StringBuilder sb;
		Stack<String> stack;
		String author = null;
		
		public PicasaParser(){
			imgs = new ArrayList<ImageReference>();
			stack = new Stack<String>();
		}
		
		@Override
		protected String extendURL(String url, Context context) {
			return PicasaFeeder.signUrl(url, context);
		}
		
		@Override
		public void startElement(String uri, String localName, String name,
				Attributes attributes) throws SAXException {
			super.startElement(uri, localName, name, attributes);
			if(localName.equals(PicasaTags.ENTRY)){
				image = new PicasaImage();
				stack.push(localName);
			}
			else if(localName.equals(PicasaTags.AUTHOR)){
				if(stack.size() == 0){
					stack.push(PicasaTags.AUTHOR);
				}else if(stack.peek().equals(PicasaTags.ENTRY)){
					stack.push(PicasaTags.AUTHOR);
				}
			}else if(localName.equals(PicasaTags.TITLE)){
				if(!stack.empty() && stack.peek().equals(PicasaTags.ENTRY)){
					sb = new StringBuilder();
				}
			}else if(localName.equals(PicasaTags.AUTHOR_NAME)){
				if(!stack.empty() && stack.peek().equals(PicasaTags.AUTHOR)){
					sb = new StringBuilder();
				}
			}else if(localName.equals(PicasaTags.AUTHOR_URI)){
				if(stack.peek().equals(PicasaTags.AUTHOR)){
					sb = new StringBuilder();
				}
			}else if(localName.equals(PicasaTags.CONTENT)){
				if(stack.peek().equals(PicasaTags.ENTRY)){
					image.setSourceURL(attributes.getValue(PicasaTags.CONTENT_SRC));
				}
			}else if(localName.equals(PicasaTags.LINK_REL)){
				if(stack.peek().equals(PicasaTags.ENTRY)){
					image.setImageURL(attributes.getValue(PicasaTags.HREF));
				}
			}else if(localName.equals(PicasaTags.KEY) && uri.equals(PicasaTags.NS_GPHOTO)){
				if(!stack.empty() && stack.peek().equals(PicasaTags.ENTRY)){
					sb = new StringBuilder();
				}
			}else if(localName.equals(PicasaTags.GROUP) && uri.equals(PicasaTags.NS_MEDIA)){
				if(stack.peek().equals(PicasaTags.ENTRY)){
					stack.add(PicasaTags.GROUP);
				}
			}else if(localName.equals(PicasaTags.GROUP_THUMBNAIL) && uri.equals(PicasaTags.NS_MEDIA)){
				if(stack.peek().equals(PicasaTags.GROUP)){
					image.setThumbnailURL(attributes.getValue(PicasaTags.GROUP_THUMBNAIL_URL), Integer.parseInt(attributes.getValue(PicasaTags.GROUP_THUMBNAIL_WIDTH)), Integer.parseInt(attributes.getValue(PicasaTags.GROUP_THUMBNAIL_HEIGHT)));
				}
			}else if(localName.equals(PicasaTags.LINK)){
				if(!stack.empty() && stack.peek().equals(PicasaTags.ENTRY)){
					if(attributes.getValue(PicasaTags.LINK_REL).equals(PicasaTags.LINK_REL_TYPE)){
						image.setImageURL(attributes.getValue(PicasaTags.HREF));
					}
				}
			}
		}
		
		@Override
		public void endElement(String uri, String localName, String qName)
			throws SAXException {
			if(localName.equals(PicasaTags.ENTRY)){
				stack.pop();
				if(image.getAuthor() == null){
					image.setOwner(author);
				}
				imgs.add(image);
				image = null;
			}
			else if(localName.equals(PicasaTags.TITLE) && image != null && sb != null){
				image.setTitle(sb.toString());
				sb = null;
			}else if(localName.equals(PicasaTags.GROUP)){
				stack.pop();
			}else if(localName.equals(PicasaTags.AUTHOR)){
				stack.pop();
			}else if(localName.equals(PicasaTags.AUTHOR_NAME)){
				if(!stack.empty() && stack.peek().equals(PicasaTags.AUTHOR)){
					if(image != null){
						image.setOwner(sb.toString());
					}else{
						author = sb.toString();
					}
				}
				sb = null;
			}else if(localName.equals(PicasaTags.AUTHOR_URI)){
				sb = null; // We don't use this.
			}else if(localName.equals(PicasaTags.AUTHOR)){
				stack.pop();
			}else if(localName.equals(PicasaTags.KEY) && uri.equals(PicasaTags.NS_GPHOTO) && image != null){
				image.setImageID(sb.toString());
				sb = null;
			}
		}
		
		@Override
		public void characters(char[] ch, int start, int length)
			throws SAXException {
			if(sb != null){
				sb.append(ch, start, length);
			}
		}
		
		public List<ImageReference> getData(){
			return imgs;
		}
}
