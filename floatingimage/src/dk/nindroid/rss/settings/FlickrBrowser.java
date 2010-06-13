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

public class FlickrBrowser extends ListActivity {
	// Positions
	private static final int	SHOW_STREAM = 0;
	private static final int	SEARCH 		= 1;
	private static final int	AUTHORIZE   = 2;
	
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
			String authorize = this.getResources().getString(R.string.authorize);
			String[] options = new String[]{showStream, search, authorize};
			setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, options));
		}else{
			String showStream = this.getResources().getString(R.string.flickrShowStream);
			String search = this.getResources().getString(R.string.flickrSearch);
			String unauthorize = this.getResources().getString(R.string.unauthorize);
			String[] options = new String[]{showStream, search, unauthorize};
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
		switch(position){
		case SHOW_STREAM:
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
			break;
		case SEARCH:
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
			break;
		case AUTHORIZE:
			try {
				if(FlickrFeeder.needsAuthorization()){
					FlickrFeeder.authorize(this);
				}else{
					FlickrFeeder.unauthorize(this);
				}
			} catch (IOException e) {
				Toast.makeText(this, "Something strange happened when authorizing... Please try again!", Toast.LENGTH_LONG);
				Log.w("Floating Image", "Exception thrown authorizing flickr", e);
			}
			break;
		}
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
