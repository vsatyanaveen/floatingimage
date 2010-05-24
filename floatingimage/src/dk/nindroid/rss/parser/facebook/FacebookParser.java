package dk.nindroid.rss.parser.facebook;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.SAXException;

import android.util.Log;
import dk.nindroid.rss.HttpTools;
import dk.nindroid.rss.ShowStreams;
import dk.nindroid.rss.data.FeedReference;
import dk.nindroid.rss.data.ImageReference;
import dk.nindroid.rss.facebook.FacebookImage;
import dk.nindroid.rss.parser.FeedParser;

public class FacebookParser implements FeedParser {
	List<ImageReference> images;
	
	@Override
	public List<ImageReference> parseFeed(FeedReference feed)
			throws ParserConfigurationException, SAXException,
			FactoryConfigurationError, IOException {
		FacebookFeeder.readCode(ShowStreams.current);
		String url = FacebookFeeder.constructFeed(feed.getFeedLocation()); // Do this to always use updated access token
		if (url == null){
			return null;
		}
		InputStream stream = HttpTools.openHttpConnection(url);
		BufferedInputStream bis = new BufferedInputStream(stream);
		byte[] buffer = new byte[8192];
		StringBuilder sb = new StringBuilder();
		int read = 0;
		while((read = bis.read(buffer)) != -1){
			sb.append(new String(buffer, 0, read));
		}
		try {
			JSONObject json = new JSONObject(sb.toString());
			
			JSONArray data = json.getJSONArray(FacebookTags.DATA);
			images = new ArrayList<ImageReference>(data.length());
			for(int i = 0; i < data.length(); ++i){
				FacebookImage img = new FacebookImage();
				JSONObject obj = data.getJSONObject(i);
				img.setImageID(obj.getString(FacebookTags.ID));
				img.setOwner(obj.getJSONObject(FacebookTags.FROM).getString(FacebookTags.NAME));
				img.setThumbnailURL(obj.getString(FacebookTags.PICTURE));
				img.setSourceURL(obj.getString(FacebookTags.SOURCE));
				img.setPageURL(obj.getString(FacebookTags.LINK));
				images.add(img);
			}
		} catch (JSONException e) {
			Log.w("Floating Image", "Error reading facebook stream", e);
			return null;
		}
		
		return images;
	}

}
