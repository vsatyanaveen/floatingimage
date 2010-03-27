package dk.nindroid.rss.parser;

import java.util.List;

import org.xml.sax.helpers.DefaultHandler;

import dk.nindroid.rss.data.ImageReference;

public abstract class FeedParser extends DefaultHandler{
	public abstract List<ImageReference> getData();
}
