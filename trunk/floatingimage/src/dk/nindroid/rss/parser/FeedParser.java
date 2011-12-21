package dk.nindroid.rss.parser;

import java.io.IOException;
import java.util.List;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import android.content.Context;
import dk.nindroid.rss.data.FeedReference;
import dk.nindroid.rss.data.ImageReference;
import dk.nindroid.rss.settings.Settings;

public interface FeedParser {
	List<ImageReference> parseFeed(FeedReference feed, Context context) throws ParserConfigurationException, SAXException, FactoryConfigurationError, IOException;
	void init(Settings settings);
}
