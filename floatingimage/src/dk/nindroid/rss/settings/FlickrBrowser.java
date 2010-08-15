package dk.nindroid.rss.settings;

import java.io.IOException;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Toast;
import dk.nindroid.rss.R;
import dk.nindroid.rss.flickr.FlickrFeeder;
import dk.nindroid.rss.parser.flickr.FlickrAlbumBrowser;
import dk.nindroid.rss.parser.flickr.FlickrUser;

public class FlickrBrowser extends ListActivity {
	// Positions
	private static final int	SHOW_STREAM 				= 0;
	private static final int	SEARCH 						= 1;
	private static final int	EXPLORE						= 2;
	private static final int	PHOTOS_FROM_HERE 			= 3;
	private static final int	AUTHORIZE   				= 4;
	private static final int	MY_STREAM					= 0;
	private static final int	MY_ALBUMS					= 1;
	private static final int	MY_CONTACTS_PHOTOS			= 2;
	private static final int	SHOW_STREAM_AUTHORIZED 		= 3;
	private static final int	SEARCH_AUTHORIZED 			= 4;
	private static final int	EXPLORE_AUTHORIZED			= 5;
	private static final int	PHOTOS_FROM_HERE_AUTHORIZED = 6;
	private static final int	UNAUTHORIZE 				= 7;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		FlickrFeeder.readCode(this);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		fillMenu();
	}
	
	private void fillMenu(){
		if(FlickrFeeder.needsAuthorization()){
			String showStream = this.getResources().getString(R.string.flickrShowStream);
			String search = this.getResources().getString(R.string.flickrSearch);
			String explore = this.getResources().getString(R.string.flickrExplore);
			String photosFromHere = this.getResources().getString(R.string.flickrPhotosFromHere);
			String authorize = this.getResources().getString(R.string.authorize);
			String[] options = new String[]{showStream, search, explore, photosFromHere, authorize};
			setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, options));
		}else{
			String myStream = this.getResources().getString(R.string.flickrMyPhotos);
			String myAlbums = this.getResources().getString(R.string.flickrMyAlbums);
			String myContacts = this.getResources().getString(R.string.flickrMyContactsPhotos);
			String showStream = this.getResources().getString(R.string.flickrShowStream);
			String search = this.getResources().getString(R.string.flickrSearch);
			String explore = this.getResources().getString(R.string.flickrExplore);
			String photosFromHere = this.getResources().getString(R.string.flickrPhotosFromHere);
			String unauthorize = this.getResources().getString(R.string.unauthorize);
			String[] options = new String[]{myStream, myAlbums, myContacts, showStream, search, explore, photosFromHere, unauthorize};
			setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, options));
		}
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		if(FlickrFeeder.needsAuthorization()){
			switch(position){
			case SHOW_STREAM:
				showStream();
				break;
			case SEARCH:
				search();
				break;
			case EXPLORE:
				returnExplore();
			case PHOTOS_FROM_HERE:
				returnPhotosFromHere();
				break;
			case AUTHORIZE:
				try {
					FlickrFeeder.authorize(this);
				} catch (IOException e) {
					Toast.makeText(this, "Something strange happened when authorizing... Please try again!", Toast.LENGTH_LONG);
					Log.w("Floating Image", "Exception thrown authorizing flickr", e);
				}
				break;
			}
		}else{
			switch(position){
			case MY_STREAM:
				returnMyStream();
				break;
			case MY_ALBUMS:
				showMyAlbums();
				break;
			case MY_CONTACTS_PHOTOS:
				returnMyContactsPhotos();
				break;
			case SHOW_STREAM_AUTHORIZED:
				showStream();
				break;
			case SEARCH_AUTHORIZED:
				search();
				break;
			case EXPLORE_AUTHORIZED:
				returnExplore();
				break;
			case PHOTOS_FROM_HERE_AUTHORIZED:
				returnPhotosFromHere();
				break;
			case UNAUTHORIZE:
				FlickrFeeder.unauthorize(this);
				fillMenu();
				break;
			}
		}
	}
	
	private void showMyAlbums(){
		Intent intent = new Intent(this, FlickrAlbumBrowser.class);
		startActivityForResult(intent, MY_ALBUMS);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == RESULT_OK){
			switch(requestCode){
			case MY_ALBUMS:
				setResult(RESULT_OK, data);
				Bundle b = data.getExtras();
				b.putString("EXTRAS", getString(R.string.albumBy) + " " + getString(R.string.me));
				finish();
				break;
			}
		}
	}
	
	private void search(){
		FrameLayout fl = new FrameLayout(this);
		final EditText input = new EditText(this);

		fl.addView(input, FrameLayout.LayoutParams.FILL_PARENT);
		input.setGravity(Gravity.CENTER);
		final AlertDialog searchDialog = new AlertDialog.Builder(this)
		.setView(fl)
		.setTitle(R.string.flickrSearchTerm)
		.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				returnSearch(input.getText().toString());
			}
		}).setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		}).create();
		showKeyboard(searchDialog, input);
		searchDialog.show();
	}
	
	private void showStream(){
		FrameLayout fl = new FrameLayout(this);
		final EditText input = new EditText(this);

		fl.addView(input, FrameLayout.LayoutParams.FILL_PARENT);
		input.setGravity(Gravity.CENTER);
		final AlertDialog streamDialog = new AlertDialog.Builder(this)
		.setView(fl)
		.setTitle(R.string.flickrShowStreamUsername)
		.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				returnStream(input.getText().toString());
			}
		}).setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		}).create();
		showKeyboard(streamDialog, input);
		streamDialog.show();
	}
	
	protected static void showKeyboard(final AlertDialog dialog, EditText editText){
		editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
		    @Override
		    public void onFocusChange(View v, boolean hasFocus) {
		        if (hasFocus) {
		        	dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
		        }
		    }
		});
	}
	private void returnMyStream(){
		FlickrUser user = FlickrFeeder.getAuthorizedUser();
		Intent intent = new Intent();
		Bundle b = new Bundle();
		String streamURL = FlickrFeeder.getPublicPhotos(user.getNsid());
		b.putString("PATH", streamURL);
		b.putString("NAME", "Stream: " + user.getUsername());
		intent.putExtras(b);
		setResult(RESULT_OK, intent);		
		finish();
	}
	
	private void returnMyContactsPhotos(){
		Intent intent = new Intent();
		Bundle b = new Bundle();
		String streamURL = FlickrFeeder.getContactsPhotos();
		b.putString("PATH", streamURL);
		b.putString("NAME", "All contacts' streams");
		intent.putExtras(b);
		setResult(RESULT_OK, intent);		
		finish();
	}
	
	private void returnExplore(){
		Intent intent = new Intent();
		Bundle b = new Bundle();
		String streamURL = FlickrFeeder.getExplore();
		b.putString("PATH", streamURL);
		b.putString("NAME", getString(R.string.flickrExplore));
		intent.putExtras(b);
		setResult(RESULT_OK, intent);		
		finish();
	}
	
	private void returnPhotosFromHere(){
		Intent intent = new Intent();
		Bundle b = new Bundle();
		String streamURL = FlickrFeeder.getPhotosFromHere();
		b.putString("PATH", streamURL);
		b.putString("NAME", "Photos from here");
		intent.putExtras(b);
		setResult(RESULT_OK, intent);		
		finish();
	}

	private void returnStream(String username){
		String uid = FlickrFeeder.findByUsername(username);
		if(username.length() == 0){ // This actually returns a user with no images!
			Toast.makeText(this, R.string.flickrShowStreamNoUsername, Toast.LENGTH_LONG).show();
			return;
		}
		if(uid == null) {// Bad username.
			Toast.makeText(this, R.string.flickrShowStreamBadUsername, Toast.LENGTH_LONG).show();
			return; 
		}
		Intent intent = new Intent();
		Bundle b = new Bundle();
		String streamURL = FlickrFeeder.getPublicPhotos(uid);
		b.putString("PATH", streamURL);
		b.putString("NAME", "Stream: " + username);
		intent.putExtras(b);
		setResult(RESULT_OK, intent);		
		finish();
	}
	
	private void returnSearch(String criteria){
		if(criteria.length() == 0){
			Toast.makeText(this, R.string.flickrSearchNoText, Toast.LENGTH_LONG).show();
			return;
		}
		Intent intent = new Intent();
		Bundle b = new Bundle();
		String streamURL = FlickrFeeder.getSearch(criteria);
		b.putString("PATH", streamURL);
		b.putString("NAME", "Search: " + criteria);
		intent.putExtras(b);
		setResult(RESULT_OK, intent);		
		finish();
	}
}
