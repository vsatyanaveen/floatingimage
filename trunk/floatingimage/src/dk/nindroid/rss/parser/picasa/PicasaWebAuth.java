package dk.nindroid.rss.parser.picasa;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import dk.nindroid.rss.R;

public class PicasaWebAuth extends Activity {
	WebView webView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.webauth);
		webView = (WebView) findViewById(R.id.webauth);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.setWebViewClient(new MyWebViewClient());
		String url = getIntent().getExtras().getString("URL");
		Log.v("Floating Image", "Visiting url: " + url);
		webView.loadUrl(url);
	}
	
	private class MyWebViewClient extends WebViewClient {	
		@Override
	    public boolean shouldOverrideUrlLoading(WebView view, String url) {
	    	Log.v("Floating Image", "Visiting url: " + url);
	    	if(url.startsWith("http") || url.startsWith("https")){
		    	view.loadUrl(url);
		        return true;
	    	}
	    	Uri uri = Uri.parse(url);
	    	String verifier = uri.getQueryParameter("oauth_verifier");
	    	String token = uri.getQueryParameter("oauth_token");
	    	if(verifier == null || token == null){
	    		Log.w("Floating Image", "Invalid response from Google auth: " + url);
	    	}else{
	    		new SetAuthTask(PicasaWebAuth.this, verifier, token).execute();
	    	}
	    	return true;
	    }
	}
}
