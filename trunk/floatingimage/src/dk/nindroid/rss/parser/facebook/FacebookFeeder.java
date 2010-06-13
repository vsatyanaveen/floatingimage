package dk.nindroid.rss.parser.facebook;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

public class FacebookFeeder {
	private static final String APP_ID = "120339624649114";
	private static final String APP_SECRET = "085182d54361333699c436f5e0e45f71";
	
	private static final String CALLBACK_URL = "http://floating-image.appspot.com/facebookauthorization";
	private static final String AUTHORIZATION_URL = "https://graph.facebook.com/oauth/authorize?client_id=" + APP_ID + "&scope=user_photos,read_friendlists,friends_photos,offline_access,user_photo_video_tags&redirect_uri=" + CALLBACK_URL;// + "&display=touch";
	private static final String ACCESS_TOKEN_URL = "https://graph.facebook.com/oauth/access_token?client_id=" + APP_ID + "&redirect_uri=" + CALLBACK_URL + "&client_secret=" + APP_SECRET + "&code=";
	
	private static final String BASEURL		  	= "https://graph.facebook.com/";
	private static final String ME				= "me/";
	private static final String PHOTOS_URL 		= "photos";
	private static final String ALBUMS_URL 		= "albums";
	private static final String FRIENDS_URL 	= "friends";
		
	private static String accessToken = null;
	private static String code = null;
	
	public static void setCodeToken(String code, Context c) throws MalformedURLException, IOException{
		Log.v("Floating image", "Facebook access code set: " + code);
		FacebookFeeder.code = code;
		SharedPreferences sp = c.getSharedPreferences("dk.nindroid.rss_preferences", 0);
		SharedPreferences.Editor e = sp.edit();
		e.putString("FACEBOOK_CODE", code);
		e.commit();
	}
	
	public static void unauthorize(Context context){
		code = null;
		SharedPreferences sp = context.getSharedPreferences("dk.nindroid.rss_preferences", 0);
		SharedPreferences.Editor e = sp.edit();
		e.remove("FACEBOOK_CODE");
		e.commit();
	}
	
	public static void initCode(Context context) throws MalformedURLException, IOException{
		if(code == null){
			getCode(context);
		}
	}
	
	public static void readCode(Context context){
		if(code == null){
			SharedPreferences sp = context.getSharedPreferences("dk.nindroid.rss_preferences", 0);
			code = sp.getString("FACEBOOK_CODE", null);
		}
	}
	
	public static boolean needsAuthorization(){
		return code == null;
	}
	
	public static String constructFeed(String baseURL) throws MalformedURLException, IOException{
		if(code == null){
			return null;
		}else{
			getAccessToken(code);
		}
		return baseURL + "?access_token=" + accessToken + "&limit=500";
	}
	
	private static String getAccessToken(String code) throws MalformedURLException, IOException{
		if(accessToken == null){
			InputStream is = new URL(ACCESS_TOKEN_URL + code).openStream();
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			String[] tokens = br.readLine().split("&");
			for(String token : tokens){
				String[] parts = token.split("=");
				if(parts[0].equals("access_token")){
					Log.v("Floating image", "Facebook access token read: " + parts[1]);
					accessToken = parts[1];
				}
			}
		}
		return accessToken;
	}
	
	private static void getCode(Context context) throws MalformedURLException, IOException{
		if(code == null){
			Intent intent = new Intent(context, WebAuth.class);
			intent.putExtra("URL", AUTHORIZATION_URL);
			context.startActivity(intent);
		}
	}
	
	public static String getPhotosOfMeUrl() {
		return BASEURL + ME + PHOTOS_URL;
	}
	
	public static String getMyAlbumsUrl() throws MalformedURLException, IOException{
		return constructFeed(BASEURL + ME + ALBUMS_URL);
	}
	
	public static String getAlbumsUrl(String id) throws MalformedURLException, IOException{
		return constructFeed(BASEURL + id + "/" + ALBUMS_URL);
	}
	
	public static String getPhotos(String id){
		return BASEURL + id + "/" + PHOTOS_URL; 
	}
	
	public static String getMyFriendsUrl() throws MalformedURLException, IOException{
		return constructFeed(BASEURL + ME + FRIENDS_URL);
	}
}
