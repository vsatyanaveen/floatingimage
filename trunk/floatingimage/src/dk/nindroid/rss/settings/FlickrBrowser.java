package dk.nindroid.rss.settings;

import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
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
import dk.nindroid.rss.flickr.FindUserTask;
import dk.nindroid.rss.flickr.FlickrFeeder;
import dk.nindroid.rss.parser.flickr.FlickrAlbumBrowser;
import dk.nindroid.rss.parser.flickr.FlickrUser;
import dk.nindroid.rss.uiActivities.Toaster;

public class FlickrBrowser extends SourceSelector.SourceFragment implements FindUserTask.Callback, SettingsFragment{
	// Positions
	private static final int	SHOW_STREAM 				= 0;
	private static final int	SEARCH 						= 1;
	private static final int	EXPLORE						= 2;
	private static final int	AUTHORIZE   				= 3;
	private static final int	MY_STREAM					= 0;
	private static final int	MY_ALBUMS					= 1;
	private static final int	MY_CONTACTS_PHOTOS			= 2;
	private static final int	MY_FAVORITES				= 3;
	private static final int	SHOW_STREAM_AUTHORIZED 		= 4;
	private static final int	SEARCH_AUTHORIZED 			= 5;
	private static final int	EXPLORE_AUTHORIZED			= 6;
	private static final int	UNAUTHORIZE 				= 7;
	
	public FlickrBrowser() {
		super(1);
	}
	
	boolean mDualPane;
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		View sourceFrame = getActivity().findViewById(R.id.source);
        mDualPane = sourceFrame != null && sourceFrame.getVisibility() == View.VISIBLE;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		FlickrFeeder.readCode(this.getActivity());
		fillMenu();
	}
	
	private void fillMenu(){
		if(FlickrFeeder.needsAuthorization()){
			String showStream = this.getResources().getString(R.string.flickrShowStream);
			String search = this.getResources().getString(R.string.flickrSearch);
			String explore = this.getResources().getString(R.string.flickrExplore);
			//String photosFromHere = this.getResources().getString(R.string.flickrPhotosFromHere);
			String authorize = this.getResources().getString(R.string.authorize);
			String[] options = new String[]{showStream, search, explore, authorize};
			setListAdapter(new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_list_item_1, options));
		}else{
			String myStream = this.getResources().getString(R.string.flickrMyPhotos);
			String myAlbums = this.getResources().getString(R.string.flickrMyAlbums);
			String myContacts = this.getResources().getString(R.string.flickrMyContactsPhotos);
			String myFavorites = this.getResources().getString(R.string.flickrMyFavorites);
			String showStream = this.getResources().getString(R.string.flickrShowStream);
			String search = this.getResources().getString(R.string.flickrSearch);
			String explore = this.getResources().getString(R.string.flickrExplore);
			//String photosFromHere = this.getResources().getString(R.string.flickrPhotosFromHere);
			String unauthorize = this.getResources().getString(R.string.unauthorize);
			String[] options = new String[]{myStream, myAlbums, myContacts, myFavorites, showStream, search, explore, unauthorize};
			setListAdapter(new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_list_item_1, options));
		}
	}
	
	
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
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
				break;
			case AUTHORIZE:
				try {
					FlickrFeeder.authorize(this.getActivity());
				} catch (IOException e) {
					Toast.makeText(this.getActivity(), R.string.authorization_failed, Toast.LENGTH_LONG);
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
			case MY_FAVORITES:
				returnMyFavorites();
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
			case UNAUTHORIZE:
				FlickrFeeder.unauthorize(this.getActivity());
				fillMenu();
				break;
			}
		}
	}
	
	private void showMyAlbums(){
		if(mDualPane){
			FragmentTransaction ft = getFragmentManager().beginTransaction();
	        ft.replace(R.id.source, FlickrAlbumBrowser.getInstance(null), "content");
	        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
	        ft.commit();
		}else{
			Intent intent = new Intent(this.getActivity(), AlbumActivity.class);
			startActivityForResult(intent, MY_ALBUMS);
		}
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == Activity.RESULT_OK){
			switch(requestCode){
			case MY_ALBUMS:
				this.getActivity().setResult(Activity.RESULT_OK, data);
				Bundle b = data.getExtras();
				b.putString("EXTRAS", getString(R.string.albumBy) + " " + getString(R.string.me));
				b.putInt("TYPE", Settings.TYPE_FLICKR);
				this.getActivity().finish();
				break;
			}
		}
	}
	
	private void search(){
		FrameLayout fl = new FrameLayout(this.getActivity());
		final EditText input = new EditText(this.getActivity());

		fl.addView(input, FrameLayout.LayoutParams.FILL_PARENT);
		input.setGravity(Gravity.CENTER);
		final AlertDialog searchDialog = new AlertDialog.Builder(this.getActivity())
		.setView(fl)
		.setTitle(R.string.flickrSearchTerm)
		.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				returnSearch(input.getText().toString());
			}
		}).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		}).create();
		showKeyboard(searchDialog, input);
		searchDialog.show();
	}
	
	private void showStream(){
		FrameLayout fl = new FrameLayout(this.getActivity());
		final EditText input = new EditText(this.getActivity());

		fl.addView(input, FrameLayout.LayoutParams.FILL_PARENT);
		input.setGravity(Gravity.CENTER);
		final AlertDialog streamDialog = new AlertDialog.Builder(this.getActivity())
		.setView(fl)
		.setTitle(R.string.flickrShowStreamUsername)
		.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				returnStream(input.getText().toString());
			}
		}).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener(){
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
		if(user == null){
			this.getActivity().runOnUiThread(new Toaster(this.getActivity(), R.string.error_getting_flickr_user));
		}else{
			Intent intent = new Intent();
			Bundle b = new Bundle();
			String streamURL = FlickrFeeder.getPublicPhotos(user.getNsid());
			b.putString("PATH", streamURL);
			b.putString("NAME", getString(R.string.stream) + " " + user.getUsername());
			b.putInt("TYPE", Settings.TYPE_FLICKR);
			intent.putExtras(b);
			this.getActivity().setResult(Activity.RESULT_OK, intent);		
			this.getActivity().finish();
		}
	}
	
	private void returnMyContactsPhotos(){
		Intent intent = new Intent();
		Bundle b = new Bundle();
		String streamURL = FlickrFeeder.getContactsPhotos();
		b.putString("PATH", streamURL);
		b.putString("NAME", getString(R.string.flickrAllContactsStreams));
		b.putInt("TYPE", Settings.TYPE_FLICKR);
		intent.putExtras(b);
		this.getActivity().setResult(Activity.RESULT_OK, intent);		
		this.getActivity().finish();
	}
	
	private void returnMyFavorites(){
		Intent intent = new Intent();
		Bundle b = new Bundle();
		String streamURL = FlickrFeeder.getFavorites();
		b.putString("PATH", streamURL);
		b.putString("NAME", getString(R.string.flickrMyFavorites));
		b.putInt("TYPE", Settings.TYPE_FLICKR);
		intent.putExtras(b);
		this.getActivity().setResult(Activity.RESULT_OK, intent);		
		this.getActivity().finish();
	}
	
	private void returnExplore(){
		Intent intent = new Intent();
		Bundle b = new Bundle();
		String streamURL = FlickrFeeder.getExplore();
		b.putString("PATH", streamURL);
		b.putString("NAME", getString(R.string.flickrExplore));
		b.putString("EXTRAS", getString(R.string.flickrExploreSummary));
		b.putInt("TYPE", Settings.TYPE_FLICKR);
		intent.putExtras(b);
		this.getActivity().setResult(Activity.RESULT_OK, intent);		
		this.getActivity().finish();
	}

	private void returnStream(String username){
		FindUserTask task = new FindUserTask(getActivity(), this);
		task.execute(username);		
	}
	
	@Override
	public void findUserCallback(String username, String uid) {
		if(username.length() == 0){ // This actually returns a user with no images!
			Toast.makeText(this.getActivity(), R.string.flickrShowStreamNoUsername, Toast.LENGTH_LONG).show();
			return;
		}
		if(uid == null) {// Bad username.
			Toast.makeText(this.getActivity(), R.string.flickrShowStreamBadUsername, Toast.LENGTH_LONG).show();
			return; 
		}
		Intent intent = new Intent();
		Bundle b = new Bundle();
		String streamURL = FlickrFeeder.getPublicPhotos(uid);
		b.putString("PATH", streamURL);
		b.putString("NAME", getString(R.string.stream) + " " + username);
		b.putInt("TYPE", Settings.TYPE_FLICKR);
		intent.putExtras(b);
		this.getActivity().setResult(Activity.RESULT_OK, intent);		
		this.getActivity().finish();
	}
	
	private void returnSearch(String criteria){
		if(criteria.length() == 0){
			Toast.makeText(this.getActivity(), R.string.flickrSearchNoText, Toast.LENGTH_LONG).show();
			return;
		}
		Intent intent = new Intent();
		Bundle b = new Bundle();
		String streamURL = FlickrFeeder.getSearch(criteria);
		b.putString("PATH", streamURL);
		b.putString("NAME", getString(R.string.flickr_search) + ": " + criteria);
		b.putInt("TYPE", Settings.TYPE_FLICKR);
		intent.putExtras(b);
		this.getActivity().setResult(Activity.RESULT_OK, intent);		
		this.getActivity().finish();
	}
	
	public static class AlbumActivity extends FragmentActivity{
		@Override
		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			if (getResources().getConfiguration().orientation
					== Configuration.ORIENTATION_LANDSCAPE) {
				// If the screen is now in landscape mode, we can show the
				// dialog in-line with the list so we don't need this activity.
				finish();
				return;
			}

			if (savedInstanceState == null) {
				// During initial setup, plug in the details fragment.

				Fragment f = FlickrAlbumBrowser.getInstance(this.getIntent().getStringExtra(FlickrAlbumBrowser.OWNER));
				f.setArguments(getIntent().getExtras());
				getSupportFragmentManager().beginTransaction().add(android.R.id.content, f).commit();
			}
		}
	}

	@Override
	public boolean back() {
		return false;
	}
}
