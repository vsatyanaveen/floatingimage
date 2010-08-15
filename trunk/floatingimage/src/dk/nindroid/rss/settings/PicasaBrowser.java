package dk.nindroid.rss.settings;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Toast;
import dk.nindroid.rss.R;
import dk.nindroid.rss.parser.picasa.PicasaAlbumBrowser;
import dk.nindroid.rss.parser.picasa.PicasaFeeder;
import dk.nindroid.rss.parser.picasa.PicasaUserView;

public class PicasaBrowser extends ListActivity {
	// Positions
	private static final int	UNAUTHD_SIGN_IN		= 0;
	private static final int	UNAUTHD_SHOW_USER 	= 1;
	private static final int	UNAUTHD_SEARCH 		= 2;
	
	private static final int	AUTHD_SHOW_MY_RECENT 	= 0;
	private static final int	AUTHD_SHOW_MY_ALBUMS 	= 1;
	private static final int	AUTHD_SHOW_USER 		= 2;
	private static final int	AUTHD_SEARCH 			= 3;
	private static final int	AUTHD_SIGN_OUT			= 4;
	
	private static final int	SHOW_USER				= 40;
	private static final int	SHOW_ALBUMS				= 41;
	
	boolean signedIn = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		fillMenu();
	}
	
	private void fillMenu(){
		String[] options = null;
		if(!PicasaFeeder.isSignedIn(this)){
			signedIn = false;
			String signin = this.getResources().getString(R.string.authorize);
			String showUser = this.getResources().getString(R.string.picasaShowUser);
			String search = this.getResources().getString(R.string.picasaSearch);
			options = new String[]{signin, showUser, search};
		}else{
			signedIn = true;
			String showMyRecent = this.getResources().getString(R.string.picasaShowMyStream);
			String showMyAlbums = this.getResources().getString(R.string.picasaShowMyAlbums);
			String showUser = this.getResources().getString(R.string.picasaShowUser);
			String search = this.getResources().getString(R.string.picasaSearch);
			String signout = this.getResources().getString(R.string.unauthorize);
			options = new String[]{showMyRecent, showMyAlbums, showUser, search, signout};
		}
		
		setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, options));
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		if(!signedIn){
			switch(position){
			case UNAUTHD_SHOW_USER:
				showUser();
				break;
			case UNAUTHD_SEARCH:
				search();
				break;
			case UNAUTHD_SIGN_IN:
				PicasaFeeder.signIn(this);
				fillMenu();
				break;
			}
		}else{
			switch(position){
			case AUTHD_SHOW_MY_RECENT:
				showMyRecent();
				break;
			case AUTHD_SHOW_MY_ALBUMS:
				showMyAlbums();
				break;
			case AUTHD_SHOW_USER:
				showUser();
				break;
			case AUTHD_SEARCH:
				search();
				break;
			case AUTHD_SIGN_OUT:
				PicasaFeeder.signOut(this);
				fillMenu();
				break;
			}
		}
	}
	
	private void showMyRecent(){
		Intent intent = new Intent();
		Bundle b = new Bundle();
		String streamURL = PicasaFeeder.getMyRecent();
		b.putString("PATH", streamURL);
		b.putString("NAME", "Stream: My stream");
		intent.putExtras(b);
		setResult(RESULT_OK, intent);		
		finish();
	}
	
	private void showMyAlbums(){
		Intent intent = new Intent(this, PicasaAlbumBrowser.class);
		startActivityForResult(intent, SHOW_ALBUMS);
	}
	
	private void search(){
		FrameLayout fl = new FrameLayout(this);
		final EditText input = new EditText(this);

		fl.addView(input, FrameLayout.LayoutParams.FILL_PARENT);
		input.setGravity(Gravity.CENTER);
		final AlertDialog searchDialog = new AlertDialog.Builder(this)
		.setView(fl)
		.setTitle(R.string.picasaSearchTerm)
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
	
	private void showUser(){
		FrameLayout fl = new FrameLayout(this);
		final EditText input = new EditText(this);

		fl.addView(input, FrameLayout.LayoutParams.FILL_PARENT);
		input.setGravity(Gravity.CENTER);
		final AlertDialog streamDialog = new AlertDialog.Builder(this)
		.setView(fl)
		.setTitle(R.string.picasaShowUserUsername)
		.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				showUser(input.getText().toString());
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
	
	private void showUser(String user){
		if(user.length() == 0){ // This actually returns a user with no images!
			Toast.makeText(this, R.string.picasaShowStreamNoUsername, Toast.LENGTH_LONG).show();
			return;
		}
		if(user == null) {// Bad username.
			Toast.makeText(this, R.string.picasaShowStreamBadUsername, Toast.LENGTH_LONG).show();
			return; 
		}
		Intent intent = new Intent(this, PicasaUserView.class);
		intent.putExtra("ID", user);
		startActivityForResult(intent, SHOW_USER);
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
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == RESULT_OK){
			switch(requestCode){
			case SHOW_USER:
				setResult(RESULT_OK, data);
				finish();
				break;
			case SHOW_ALBUMS:
				Bundle b = data.getExtras();
				b.putString("EXTRAS", getString(R.string.albumBy) + " " + getString(R.string.me));
				setResult(RESULT_OK, data);
				finish();
				break;
			}
		}
	}
	private void returnSearch(String criteria){
		if(criteria.length() == 0){
			Toast.makeText(this, R.string.picasaSearchNoText, Toast.LENGTH_LONG).show();
			return;
		}
		Intent intent = new Intent();
		Bundle b = new Bundle();
		String streamURL = PicasaFeeder.getSearchUrl(criteria);
		b.putString("PATH", streamURL);
		b.putString("NAME", "Search: " + criteria);
		intent.putExtras(b);
		setResult(RESULT_OK, intent);		
		finish();
	}
}
