package dk.nindroid.rss.parser.photobucket;

import java.net.URLEncoder;
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
	
	private static String performRequest(String request, Map<String, String> params) throws Exception{
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
	
	public static String getImages(String request, int page) throws Exception{
		Map<String,String> params = new HashMap<String,String> ();
		params.put("recurse", "1");
		params.put("view", "flat");
		params.put("media", "images");
		params.put("perpage", "100");
		params.put("page", Integer.toString(page));
		return performRequest(request, params);
	}
	
	public static String getNoImages(String request) throws Exception{
		Map<String,String> params = new HashMap<String,String> ();
		params.put("recurse", "0");
		params.put("view", "flat");
		params.put("media", "none");
		return performRequest(request, params);
	}
	
	public static boolean ping(){
		try {
			getImages(PING, 1);
			return true;
		} catch (Exception e) {
			Log.w("Floating Image", "Error caught pinging!", e);
		}
		return false;
	}
	
	public static String getRecent(String user){
		return "/user/" + URLEncoder.encode(user) + "/search";
	}
	
	public static String getAlbums(String user){
		return "/album/" + URLEncoder.encode(user);
	}
	
	public static String getAlbum(String user, String album){
		return "/album/" + URLEncoder.encode(user + "/" +  album);
	}
	
	public static String getFollowing(String user){
		return "/user/" + URLEncoder.encode(user) + "/followingmedia";
	}
	
	public static String getGroup(String group){
		return "/group/" + URLEncoder.encode(group);
	}
	
	public static String search(String query){
		return "/search/" + URLEncoder.encode(query) + "/image";
	}
	
	public static void test(){
		try {
			String res = getNoImages(getAlbum("annoia", "test2"));
			Log.v("Floating Image", res);
		} catch (Exception e) {
			Log.w("Floating Image", "Error caught testing!", e);
		}
	}
}
