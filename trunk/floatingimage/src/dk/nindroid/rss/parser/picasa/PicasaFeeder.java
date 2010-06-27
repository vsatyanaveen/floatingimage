package dk.nindroid.rss.parser.picasa;

import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Random;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

import android.util.Log;
import dk.nindroid.rss.parser.Crypto;


public class PicasaFeeder {
	private static final String BASE_URL = "http://picasaweb.google.com/data/feed/api/";
	private static final String USER_URL = BASE_URL + "user/";
	private static final String SEARCH   = "all?max-results=500&q=";
	private static final String POST_RECENT   = "?kind=photo&max-results=500";
	
	private static final String SECRET   		= "1gIH0qT3ywaVuLWzSsbBChjj";
	private static final String KEY		 		= "floating-image.appspot.com";
	private static final String SCOPE			= "http://picasaweb.google.com/data";
	private static final String CALLBACK		= "floatingimage://picasa";
	
	private static final String REQUEST_TOKEN 	= "https://www.google.com/accounts/OAuthGetRequestToken?oauth_consumer_key=" + KEY + "&oauth_nonce=&oauth_signature_method=HMAC-SHA1&oauth_timestamp=&scope=" + SCOPE + "&oauth_callback=" + CALLBACK;
	
	public static String getSearchUrl(String query){
		return BASE_URL + SEARCH + query.replace(" ", "%20");
	}
	
	public static String getRecent(String userID){
		return USER_URL + userID + POST_RECENT;
	}
	
	public static String getAlbums(String userID){
		return USER_URL + userID;
	}
	
	public static boolean signIn() {
		String url = REQUEST_TOKEN;
		Random rand = new Random(System.currentTimeMillis());
		byte[] noncebytes = new byte[8];
		rand.nextBytes(noncebytes);
		String nonce = Crypto.toHex(noncebytes);
		url.replace("&oauth_nonce=&", "&oauth_nonce=" + nonce + "&");
		String timestamp = "" + (System.currentTimeMillis() / 1000);
		url = url.replace("&oauth_timestamp=&", "&oauth_timestamp=" + timestamp + "&");
		url += "&" + signUrl(url);
		Log.v("Floating Image", "Trying to sign in with url: " + url);
		return false;		
	}
	
	private static String signUrl(String url){
		StringBuilder sb = new StringBuilder();
		int parameterStart = url.indexOf("?");
		String method = url.substring(0, parameterStart);
		String parameterPart = url.substring(parameterStart + 1);
		String[] parameters = parameterPart.split("&");
		Arrays.sort(parameters);
		sb.append("GET&");
		sb.append(URLEncoder.encode(method));
		sb.append('&');
		boolean first = true;
		for(String p : parameters){
			if(!first){
				sb.append("%26");
			}
			first = false;
			sb.append(URLEncoder.encode(p.replace(":", "%3A").replace("/", "%2F")));
		}
		Log.v("Floating Image", "Signing with: " + sb.toString());
		SecretKeySpec signingKey = new SecretKeySpec(SECRET.getBytes(), "HmacSHA1");
		Mac mac;
		try {
			mac = Mac.getInstance("HmacSHA1");
			mac.init(signingKey);
			byte[] rawHmac = mac.doFinal(sb.toString().getBytes());
			String result = new String(Base64.encodeBase64(rawHmac));
			return "oauth_signature=" + URLEncoder.encode(result);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
