package dk.nindroid.rss.parser.facebook;

import java.io.IOException;
import java.net.MalformedURLException;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import dk.nindroid.rss.R;

public class WebAuth extends Activity {
	WebView webView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.webauth);
		webView = (WebView) findViewById(R.id.webauth);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.setWebViewClient(new MyWebViewClient(this));
		String url = getIntent().getExtras().getString("URL");
		Log.v("Floating Image", "Visiting url: " + url);
		webView.loadUrl(url);
	}
	
	private class MyWebViewClient extends WebViewClient {
		Activity context; 
		
		MyWebViewClient(Activity context){
			this.context = context;
		}
		
	    @Override
	    public boolean shouldOverrideUrlLoading(WebView view, String url) {
	    	Log.v("Floating Image", "Visiting url: " + url);
	    	if(url.startsWith("http") || url.startsWith("https")){
		    	view.loadUrl(url);
		        return true;
	    	}
	    	Uri uri = Uri.parse(url);
	    	String code = uri.getQueryParameter("code");
	    	try {
				FacebookFeeder.setCodeToken(code, context);
			} catch (MalformedURLException e) {
				Log.e("Floating Image", "Error parsing URL from Facebook", e);
			} catch (IOException e) {
				Log.e("Floating Image", "Error parsing URL from Facebook", e);
			}
			context.finish();
	    	return true;
	    }
	}
}
