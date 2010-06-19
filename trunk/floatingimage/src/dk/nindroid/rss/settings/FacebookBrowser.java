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
import dk.nindroid.rss.parser.facebook.FacebookAlbumBrowser;
import dk.nindroid.rss.parser.facebook.FacebookFeeder;
import dk.nindroid.rss.parser.facebook.FacebookFriendsBrowser;

public class FacebookBrowser extends ListActivity {
	// Positions
	private static final int	AUTHORIZE				= 0;
	private static final int	PHOTOS_OF_ME		 	= 0;
	private static final int	MY_ALBUMS				= 1;
	private static final int	FRIENDS					= 2;
	private static final int	UNAUTHORIZE				= 3;
	boolean authorizing = false;
	boolean showAuthorize;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		FacebookFeeder.readCode(this);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if(authorizing){
			if(FacebookFeeder.needsAuthorization()){
				Toast.makeText(this, "Facebook authorization failed!", Toast.LENGTH_LONG).show();
			}
		}
		fillMenu();
	}
	
	private void fillMenu(){
		if(FacebookFeeder.needsAuthorization()){
			showAuthorize = true;
			String authorize = this.getResources().getString(R.string.authorize);
			String[] options = new String[]{authorize};
			setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, options));
		}else{
			showAuthorize = false;
			String photosOfMe = this.getResources().getString(R.string.facebookPhotosOfMe);
			String albums = this.getResources().getString(R.string.facebookMyAlbums);
			String friends = this.getResources().getString(R.string.facebookFriends);
			String unauthorize = this.getResources().getString(R.string.unauthorize);
			String[] options = new String[]{photosOfMe, albums, friends, unauthorize};
			setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, options));
		}
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		FrameLayout fl = new FrameLayout(this);
		final EditText input = new EditText(this);

		fl.addView(input, FrameLayout.LayoutParams.FILL_PARENT);
		input.setGravity(Gravity.CENTER);
		if(showAuthorize){
			if(position == AUTHORIZE){
				authorize();
			}
		}else{
			switch(position){
			case PHOTOS_OF_ME:
				returnPhotosOfMe();
				break;
			case MY_ALBUMS:
				showMyAlbums();
				break;
			case FRIENDS:
				showFriends();
				break;
			case UNAUTHORIZE:
				
			}
		}
	}
	
	private void authorize(){
		try {
			FacebookFeeder.initCode(this);
		} catch (MalformedURLException e) {
			Log.e("Floating Image", "Error getting facebook code!", e);
		} catch (IOException e) {
			Log.e("Floating Image", "IO Error getting facebook code!", e);
		}
	}
	
	private void returnPhotosOfMe(){
		String url = null;
		url = FacebookFeeder.getPhotosOfMeUrl();
		if(url != null){
			returnResult(url, "Photos of me");
		}
	}
	
	private void showMyAlbums(){
		Intent intent = new Intent(this, FacebookAlbumBrowser.class);
		intent.putExtra("ID", "me");
		startActivityForResult(intent, MY_ALBUMS);
	}
	
	private void showFriends(){
		Intent intent = new Intent(this, FacebookFriendsBrowser.class);
		startActivityForResult(intent, FRIENDS);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == RESULT_OK){
			switch(requestCode){
			case MY_ALBUMS:
			case FRIENDS:
				setResult(RESULT_OK, data);
				finish();
				break;
			}
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
