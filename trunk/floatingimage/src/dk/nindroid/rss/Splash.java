package dk.nindroid.rss;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class Splash extends Activity {

	private final int DISPLAY_TIME = 1000;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splashscreen);
		new Handler().postDelayed(new Runnable() {
			
			@Override
			public void run() {
				Intent mainIntent = new Intent(Splash.this, ShowStreams.class);
				Splash.this.startActivity(mainIntent);
				Splash.this.finish();
			}
		}, DISPLAY_TIME);
	}
}
