package dk.nindroid.rss.settings;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;
import dk.nindroid.rss.R;

public class About extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);
		WebView view = (WebView)findViewById(R.id.text);
		String data = "<html><head></head><body>Hello, I am Mark Gj&oslash;l, your friendly neighborhood programmer.<br/><br/>Floating Image is a pet project of mine that has caught the attention of amazing people, such as yourself. It\'s main purpose is to get some experience with the Android platform and eventually to achieve world domination.<br/><br/>Floating Image is free as in speech, which means that the code can freely be downloaded from the <a href=\"http://code.google.com/p/floatingimage/\">Floating Image homepage</a>. I have made it availably such that other people might benefit from my experience so they can also make super awesome applications. All I ask is that you don\'t clone the app, and that you give me due credit.<br/><br/>Floating Image is free as in beer, which means that if you like what you see, I encourage you to buy me a beer, should we ever meet. If you find this unacceptable, the homepage also sports a donate button, where you can encourage me with cash.<br/><br/>If you have any comments, remarks, bugs or suggestions, I encourage you to <a href=\"mailto:bitflipster@gmail.com\">mail me</a>. And remember: If you don\'t like it, tell me. If you do, tell your friends!<br/><br/>Have fun!</string></body></html>";
		view.loadData(data, "text/xml", "UTF-8");
	}
}
