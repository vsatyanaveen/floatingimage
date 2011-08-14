package dk.nindroid.rss.settings;

import java.io.IOException;
import java.net.MalformedURLException;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import dk.nindroid.rss.R;
import dk.nindroid.rss.parser.facebook.FacebookAlbumBrowser;
import dk.nindroid.rss.parser.facebook.FacebookFeeder;
import dk.nindroid.rss.parser.facebook.FacebookFriendsBrowser;
import dk.nindroid.rss.settings.SourceSelector.SourceFragment;

public class FacebookBrowser extends SourceFragment {
	// Positions
	private static final int	AUTHORIZE				= 0;
	private static final int	PHOTOS_OF_ME		 	= 0;
	private static final int	MY_ALBUMS				= 1;
	private static final int	FRIENDS					= 2;
	private static final int	UNAUTHORIZE				= 3;
	boolean authorizing = false;
	boolean showAuthorize;
	
	public FacebookBrowser() {
		super(3);
	}
	
	boolean mDualPane;
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		FacebookFeeder.readCode(this.getActivity());
		View sourceFrame = getActivity().findViewById(R.id.source);
        mDualPane = sourceFrame != null && sourceFrame.getVisibility() == View.VISIBLE;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		if(authorizing){
			if(FacebookFeeder.needsAuthorization()){
				Toast.makeText(this.getActivity(), "Facebook authorization failed!", Toast.LENGTH_LONG).show();
			}
		}
		fillMenu();
	}
	
	private void fillMenu(){
		if(FacebookFeeder.needsAuthorization()){
			showAuthorize = true;
			String authorize = this.getString(R.string.authorize);
			String[] options = new String[]{authorize};
			setListAdapter(new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_list_item_1, options));
		}else{
			showAuthorize = false;
			String photosOfMe = this.getString(R.string.facebookPhotosOfMe);
			String albums = this.getString(R.string.facebookMyAlbums);
			String friends = this.getString(R.string.facebookFriends);
			String unauthorize = this.getString(R.string.unauthorize);
			String[] options = new String[]{photosOfMe, albums, friends, unauthorize};
			setListAdapter(new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_list_item_1, options));
		}
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
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
				FacebookFeeder.unauthorize(this.getActivity());
			}
		}
	}
	
	private void authorize(){
		try {
			FacebookFeeder.initCode(this.getActivity());
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
		if(mDualPane){
			FragmentTransaction ft = getFragmentManager().beginTransaction();
	        ft.replace(R.id.source, FacebookAlbumBrowser.getInstance("me", null), "content");
	        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
	        ft.commit();
		}else{
			Intent intent = new Intent(this.getActivity(), SubActivity.class);
			intent.putExtra("request", MY_ALBUMS);
			startActivityForResult(intent, MY_ALBUMS);
		}
	}
	
	private void showFriends(){
		if(mDualPane){
			FragmentTransaction ft = getFragmentManager().beginTransaction();
	        ft.replace(R.id.source, new FacebookFriendsBrowser(), "content");
	        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
	        ft.commit();
		}else{
			Intent intent = new Intent(this.getActivity(), SubActivity.class);
			intent.putExtra("request", FRIENDS);
			startActivityForResult(intent, FRIENDS);
		}
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == Activity.RESULT_OK){
			data.putExtra("TYPE", Settings.TYPE_FACEBOOK);
			switch(requestCode){
			case FRIENDS:
				this.getActivity().setResult(Activity.RESULT_OK, data);
				this.getActivity().finish();
				break;
			}
		}
	}
	
	private void returnResult(String url, String name){
		Intent intent = new Intent();
		Bundle b = new Bundle();
		b.putInt("TYPE", Settings.TYPE_FACEBOOK);
		b.putString("PATH", url);
		b.putString("NAME", name);
		intent.putExtras(b);
		this.getActivity().setResult(Activity.RESULT_OK, intent);		
		this.getActivity().finish();
	}
	
	public static class SubActivity extends FragmentActivity{
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
				int request = getIntent().getIntExtra("request", -1);
				Fragment f = null;
				switch(request){
				case FRIENDS:
					f = new FacebookFriendsBrowser();
					break;
				case MY_ALBUMS:
					f = FacebookAlbumBrowser.getInstance(null, null);
					break;
				}
				if (f == null){
					finish();
				}else{
					f.setArguments(getIntent().getExtras());
					getSupportFragmentManager().beginTransaction().add(android.R.id.content, f).commit();
				}
			}
		}
	}

	@Override
	public boolean back() {
		return false;
	}
}
