package dk.nindroid.rss.parser.facebook;

import java.io.IOException;
import java.net.MalformedURLException;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

public class FacebookAuth extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.v("Floating Image", "FacebookAuth: onCreate");
		Intent i = getIntent();
		Log.v("Floating Image", "intent");
		Uri uri = i.getData();
		Log.v("Floating Image", "uri");
		String code = uri.getQueryParameter("code");
		Log.v("Floating Image", "code");
		try {
			FacebookFeeder.setCodeToken(code, this);
		} catch (MalformedURLException e) {
			Log.e("Floating Image", "Cannot get Access token! Code: " + code, e);
		} catch (IOException e) {
			Log.e("Floating Image", "Cannot get Access token! Code: " + code, e);
		}
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		Log.v("Floating Image", "FacebookAuth: onNewIntent");
	}
}
