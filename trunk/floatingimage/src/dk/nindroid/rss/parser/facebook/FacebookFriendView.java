package dk.nindroid.rss.parser.facebook;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import dk.nindroid.rss.R;
import dk.nindroid.rss.settings.Settings;

public class FacebookFriendView extends ListFragment {
	private static final int	PHOTOS_OF		 	= 0;
	private static final int	ALBUMS				= 1;
	
	String id;
	String name;
	
	boolean mDualPane;
	
	public static FacebookFriendView getInstance(String id, String name){
		FacebookFriendView ffv = new FacebookFriendView();
		Bundle args = new Bundle();
        args.putString("ID", id);
        args.putString("Name", name);
        ffv.setArguments(args);
        return ffv;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		id = getArguments().getString("ID");
		name = getArguments().getString("Name");
		View sourceFrame = getActivity().findViewById(R.id.source);
        mDualPane = sourceFrame != null && sourceFrame.getVisibility() == View.VISIBLE;
		
		fillMenu();
	}
	
	private void fillMenu(){
		String photosOf = this.getResources().getString(R.string.facebookPhotosOfFriend) + " " + name;
		String albums = this.getResources().getString(R.string.facebookFriendsAlbums) + " " + name;
		String[] options = new String[]{photosOf, albums};
		setListAdapter(new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_list_item_1, options));
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		FrameLayout fl = new FrameLayout(this.getActivity());
		final EditText input = new EditText(this.getActivity());

		fl.addView(input, FrameLayout.LayoutParams.FILL_PARENT);
		input.setGravity(Gravity.CENTER);
		switch(position){
		case PHOTOS_OF:
			returnPhotosOfFriend();
			break;
		case ALBUMS:
			showAlbums();
			break;
		}
	}

	private void returnPhotosOfFriend() {
		String url = null;
		url = FacebookFeeder.getPhotos(id);
		if(url != null){
			returnResult(url, "Photos of " + name);
		}
	}
	
	private void showAlbums() {
		//Intent intent = new Intent(this.getActivity(), FacebookAlbumBrowser.class);
		//intent.putExtra("ID", id);
		//startActivityForResult(intent, ALBUMS);
		if(mDualPane){
			FragmentTransaction ft = getFragmentManager().beginTransaction();
	        ft.replace(R.id.source, FacebookAlbumBrowser.getInstance(id, name));
	        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
	        ft.commit();
		}else{
			Intent intent = new Intent(this.getActivity(), SubActivity.class);
			intent.putExtra("ID", id);
			intent.putExtra("Name", name);
			startActivityForResult(intent, ALBUMS);
		}
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == Activity.RESULT_OK){
			switch(requestCode){
			case ALBUMS:
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
				Fragment f = FacebookAlbumBrowser.getInstance(this.getIntent().getStringExtra("ID"), this.getIntent().getStringExtra("Name"));
				
				f.setArguments(getIntent().getExtras());
				getSupportFragmentManager().beginTransaction().add(android.R.id.content, f).commit();
			}
		}
	}
}
