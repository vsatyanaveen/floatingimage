package dk.nindroid.rss.parser.fivehundredpx;

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

import android.content.Context;
import android.util.Log;
import dk.nindroid.rss.HttpTools;
import dk.nindroid.rss.data.FeedReference;
import dk.nindroid.rss.data.ImageReference;
import dk.nindroid.rss.parser.FeedParser;
import dk.nindroid.rss.settings.Settings;

public class FiveHundredPxParser implements FeedParser, FiveHundredPxTags {
	List<ImageReference> images;
	Settings settings;
	
	@Override
	public List<ImageReference> parseFeed(FeedReference feed, Context context)
			throws ParserConfigurationException, SAXException,
			FactoryConfigurationError, IOException {
		int totalItems = -1;
		images = new ArrayList<ImageReference>();
		for(int i = 1; i < 6; ++i){ // We can only request 100 photos per page :(
			if(totalItems != -1 && i - 1 * 100 > totalItems){
				break;
			}
			String url = feed.getFeedLocation();
			if (url == null){
				return null;
			}
			
			url = FiveHundredPxFeeder.appendPage(url, i);
			if(!settings.nudity){
				url = FiveHundredPxFeeder.censor(url);
			}
			
			Log.v("Floating Image", "Getting images from: " + url);
			
			InputStream stream = HttpTools.openHttpConnection(url);
			BufferedInputStream bis = new BufferedInputStream(stream);
			byte[] buffer = new byte[8192];
			StringBuilder sb = new StringBuilder();
			int read = 0;
			try{
				while((read = bis.read(buffer)) != -1){
					sb.append(new String(buffer, 0, read));
				}
				try {
					JSONObject json = new JSONObject(sb.toString());
					if(totalItems == -1){
						totalItems = json.getInt(TOTAL_ITEMS);
					}
					
					JSONArray photos = json.getJSONArray(PHOTOS);
					for(int j = 0; j < photos.length(); ++j){
						FiveHundredPxImage img = new FiveHundredPxImage();
						JSONObject photo = photos.getJSONObject(j);
						img.setImageID(photo.getString(ID));
						img.setTitle(photo.getString(NAME));
						String imageUrl = photo.getString(IMAGE_URL);
						String large = imageUrl.replace("/2.jpg", "/4.jpg");
						String med = imageUrl.replace("/2.jpg", "/3.jpg");
						img.setSourceURL(large);
						img.setThumbnail128URL(imageUrl);
						img.setThumbnail256URL(med);
						JSONObject user = photo.getJSONObject(USER);
						img.setOwner(user.getString(FULLNAME));
						img.setPageURL("http://500px.com/photo/" + photo.getString(ID));
						images.add(img);
					}
				} catch (JSONException e) {
					Log.w("Floating Image", "Error reading 500px stream", e);
					return null;
				}
			}catch(IOException e){
				Log.w("Floating Image", "Error reading 500px stream", e);
				return null;
			}
			
		}
		if(images != null){
			Log.v("Floating Image", images.size() + " 500px images found.");
		}
		return images;
	}
	
	public static String getThumbnailUrl(String id){
		String url = FiveHundredPxFeeder.getExtendedPhoto(id, true);
		try{
			InputStream stream = HttpTools.openHttpConnection(url);
			BufferedInputStream bis = new BufferedInputStream(stream);
			byte[] buffer = new byte[8192];
			StringBuilder sb = new StringBuilder();
			int read = 0;
			while((read = bis.read(buffer)) != -1){
				sb.append(new String(buffer, 0, read));
			}
			try {
				JSONObject json = new JSONObject(sb.toString()).getJSONObject(PHOTO);
				return json.getString(IMAGE_URL);
			} catch (JSONException e) {
				Log.w("Floating Image", "Error reading 500px stream", e);
				return null;
			}
		}catch(IOException e){
			Log.w("Floating Image", "Error reading 500px stream", e);
			return null;
		}
	}

	@Override
	public void init(Settings settings) {
		this.settings = settings;
	}
}
