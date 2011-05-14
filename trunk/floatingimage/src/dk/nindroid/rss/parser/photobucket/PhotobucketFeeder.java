package dk.nindroid.rss.parser.photobucket;

import java.util.HashMap;
import java.util.Map;

import android.util.Log;

import com.photobucket.api.core.PhotobucketAPI;
import com.photobucket.api.rest.RESTfulResponse;

public class PhotobucketFeeder {
	public static final String KEY = "149831422";
	public static final String SECRET = "6f5e618e25b95a3ad1ba90056a690399";
	public static final String SUBDOMAIN = "api.photobucket.com";
	public static final String PING = "/ping"; // Do we need this? Anyway, it's good for debugging.
	
	public static String performRequest(String request, Map<String, String> params) throws Exception{
		PhotobucketAPI api = new PhotobucketAPI();
		api.setOauthConsumerKey(KEY);
		api.setOauthConsumerSecret(SECRET);
		api.setSubdomain(SUBDOMAIN);
		api.setRequestPath(request);
		api.setMethod("get");
		api.setFormat("xml");
		api.setParameters(params);
		RESTfulResponse response = api.execute();
		return response.getResponseString();
	}
	
	public static boolean ping(){
		try {
			performRequest(PING, null);
			return true;
		} catch (Exception e) {
			Log.w("Floating Image", "Error caught pinging!", e);
		}
		return false;
	}
	
	public static String getUrls(String user){
		return "/user/" + user + "/url";
	}
	
	public static String getAlbum(String user){
		return "/album/" + user;
	}
	
	public static void test(){
		Map<String,String> params = new HashMap<String,String> ();
		params.put("recurse", "1");
		params.put("view", "flat");
		params.put("media", "images");
		try {
			String res = performRequest(getAlbum("annoia"), params);
			Log.v("Floating Image", res);
		} catch (Exception e) {
			Log.w("Floating Image", "Error caught testing!", e);
		}
	}
}
