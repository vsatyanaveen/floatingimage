package dk.nindroid.rss.settings;

import java.io.IOException;
import java.net.MalformedURLException;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Toast;
import dk.nindroid.rss.R;
import dk.nindroid.rss.parser.facebook.FacebookFeeder;
import dk.nindroid.rss.parser.facebook.FeedCallback;

public class FacebookBrowser extends ListActivity {
	// Positions
	private static final int	PHOTOS_OF_ME		 	= 0;
	private static final int	MY_ALBUMS				= 1;
	private static final int	FRIENDS					= 2;
	private static final int	FRIENDS_PHOTOS_OF		= 0;
	private static final int	FRIENDS_ALBUMS			= 1;
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		try {
			if(FacebookFeeder.initCode(this)){
				fillMenu();
				return;
			}
		} catch (MalformedURLException e) {
			Log.e("Floating Image", "Error getting facebook code!", e);
		} catch (IOException e) {
			Log.e("Floating Image", "IO Error getting facebook code!", e);
		}
		Toast.makeText(this, "Facebook authorization failed!", Toast.LENGTH_LONG).show();
		finish();
	}
	
	private void fillMenu(){
		String photosOfMe = this.getResources().getString(R.string.facebookPhotosOfMe);
		String[] options = new String[]{photosOfMe};
		setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, options));
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		FrameLayout fl = new FrameLayout(this);
		final EditText input = new EditText(this);

		fl.addView(input, FrameLayout.LayoutParams.FILL_PARENT);
		input.setGravity(Gravity.CENTER);
		switch(position){
		case PHOTOS_OF_ME:
			returnPhotosOfMe();
			break;
		}
	}
	
	private void returnPhotosOfMe(){
		String url = null;
		try {
			url = FacebookFeeder.getPhotosOfMeUrl();
		} catch (MalformedURLException e) {
			Log.e("Floating Image", "Internal error. URL incorrect!", e);
			Toast.makeText(this, "Internal error detected. Please contact developer!", 2).show();
		} catch (IOException e) {
			Log.w("Floating Image", "Could not get url for the photos of me.", e);
			Toast.makeText(this, "Error getting stream. Please try again.", 2).show();
			return;
		}
		if(url != null){
			returnResult(url, "Photos of me");
		}
	}
	
	private void returnResult(String url, String name){
		Intent intent = new Intent();
		Bundle b = new Bundle();
		
		b.putString("PATH", url);
		b.putString("NAME", name);
		intent.putExtras(b);
		setResult(RESULT_OK, intent);		
		finish();
	}
}
