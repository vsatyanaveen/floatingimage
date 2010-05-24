package dk.nindroid.rss.parser;

import java.io.IOException;
import java.util.List;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import dk.nindroid.rss.data.FeedReference;
import dk.nindroid.rss.data.ImageReference;

public interface FeedParser {
	List<ImageReference> parseFeed(FeedReference feed) throws ParserConfigurationException, SAXException, FactoryConfigurationError, IOException;
}
