package dk.nindroid.rss.settings;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import dk.nindroid.rss.R;

public class About extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);
		TextView text = (TextView)findViewById(R.id.text);
		text.setText(R.string.about_text);
	}
}
