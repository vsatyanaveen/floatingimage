package dk.nindroid.rss.parser.facebook;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import dk.nindroid.rss.R;

public class FacebookFriendView extends ListActivity {
	private static final int	PHOTOS_OF		 	= 0;
	private static final int	ALBUMS				= 1;
	
	String id;
	String name;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		id = getIntent().getExtras().getString("ID");
		name = getIntent().getExtras().getString("Name");
		fillMenu();
	}
	
	private void fillMenu(){
		String photosOf = this.getResources().getString(R.string.facebookPhotosOfFriend) + " " + name;
		String albums = this.getResources().getString(R.string.facebookFriendsAlbums) + " " + name;
		String[] options = new String[]{photosOf, albums};
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
			returnResult(url, "Photos of " + id);
		}
	}
	
	private void showAlbums() {
		Intent intent = new Intent(this, FacebookAlbumBrowser.class);
		intent.putExtra("ID", id);
		startActivityForResult(intent, ALBUMS);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == RESULT_OK){
			switch(requestCode){
			case ALBUMS:
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
