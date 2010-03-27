package dk.nindroid.rss.settings;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Toast;
import dk.nindroid.rss.R;
import dk.nindroid.rss.flickr.FlickrFeeder;

public class FlickrBrowser extends ListActivity {
	// Positions
	private static final int	SHOW_STREAM = 0;
	private static final int	SEARCH 		= 1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		fillMenu();
	}
	
	private void fillMenu(){
		String showStream = this.getResources().getString(R.string.flickrShowStream);
		String search = this.getResources().getString(R.string.flickrSearch);
		String[] options = new String[]{showStream, search};
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
		case SHOW_STREAM:
			new AlertDialog.Builder(this)
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
				}).create().show();
			break;
		case SEARCH:
			new AlertDialog.Builder(this)
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
			}).create().show();
			break;
		}
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
