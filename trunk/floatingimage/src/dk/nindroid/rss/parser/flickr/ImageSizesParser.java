package dk.nindroid.rss.parser.flickr;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import dk.nindroid.rss.parser.flickr.data.ImageSizes;

public class ImageSizesParser extends DefaultHandler {
	ImageSizes data = new ImageSizes();
	
	@Override
	public void startElement(String uri, String localName, String name,
			Attributes attributes) throws SAXException {
		super.startElement(uri, localName, name, attributes);
		if(localName.equals(ImageSizesTags.SIZE)){
			String sizeLabel = attributes.getValue(ImageSizesTags.SIZE_LABEL);
			if(sizeLabel != null){
				if(sizeLabel.equals(ImageSizesTags.TYPE_SQUARE)){
					data.setSquareUrl(attributes.getValue(ImageSizesTags.SIZE_SOURCE));
				}else if(sizeLabel.equals(ImageSizesTags.TYPE_THUMBNAIL)){
					data.setThumbnailUrl(attributes.getValue(ImageSizesTags.SIZE_SOURCE));
				}else if(sizeLabel.equals(ImageSizesTags.TYPE_SMALL)){
					data.setSmallUrl(attributes.getValue(ImageSizesTags.SIZE_SOURCE));
				}else if(sizeLabel.equals(ImageSizesTags.TYPE_MEDIUM)){
					data.setMediumUrl(attributes.getValue(ImageSizesTags.SIZE_SOURCE));
				}else if(sizeLabel.equals(ImageSizesTags.TYPE_ORIGINAL)){
					data.setOriginalUrl(attributes.getValue(ImageSizesTags.SIZE_SOURCE));
				}
			}
		}
	}
	
	public ImageSizes getData(){
		return data;
	}
}
