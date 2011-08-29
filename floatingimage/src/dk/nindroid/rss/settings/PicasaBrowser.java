package dk.nindroid.rss.settings;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
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
import dk.nindroid.rss.parser.picasa.SignInTask;
import dk.nindroid.rss.settings.SourceSelector.SourceFragment;

public class PicasaBrowser extends SourceFragment implements SettingsFragment {
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
	boolean mDualPane;
	
	public PicasaBrowser() {
		super(2);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		View sourceFrame = getActivity().findViewById(R.id.source);
        mDualPane = sourceFrame != null && sourceFrame.getVisibility() == View.VISIBLE;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		fillMenu();
	}
	
	private void fillMenu(){
		String[] options = null;
		if(!PicasaFeeder.isSignedIn(this.getActivity())){
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
		
		setListAdapter(new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_list_item_1, options));
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
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
				new SignInTask(this.getActivity()).execute();
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
				PicasaFeeder.signOut(this.getActivity());
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
		b.putInt("TYPE", Settings.TYPE_PICASA);
		b.putString("NAME", "Stream: My stream");
		intent.putExtras(b);
		this.getActivity().setResult(Activity.RESULT_OK, intent);		
		this.getActivity().finish();
	}
	
	private void showMyAlbums(){
		if(mDualPane){
			FragmentTransaction ft = getFragmentManager().beginTransaction();
	        ft.replace(R.id.source, PicasaAlbumBrowser.getInstance(null));
	        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
	        ft.commit();
		}else{
			Intent intent = new Intent(this.getActivity(), AlbumActivity.class);
			startActivityForResult(intent, SHOW_ALBUMS);
		}
	}
	
	private void search(){
		FrameLayout fl = new FrameLayout(this.getActivity());
		final EditText input = new EditText(this.getActivity());

		fl.addView(input, FrameLayout.LayoutParams.FILL_PARENT);
		input.setGravity(Gravity.CENTER);
		final AlertDialog searchDialog = new AlertDialog.Builder(this.getActivity())
		.setView(fl)
		.setTitle(R.string.picasaSearchTerm)
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
	
	private void showUser(){
		FrameLayout fl = new FrameLayout(this.getActivity());
		final EditText input = new EditText(this.getActivity());

		fl.addView(input, FrameLayout.LayoutParams.FILL_PARENT);
		input.setGravity(Gravity.CENTER);
		final AlertDialog streamDialog = new AlertDialog.Builder(this.getActivity())
		.setView(fl)
		.setTitle(R.string.picasaShowUserUsername)
		.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				showUser(input.getText().toString());
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
	
	private void showUser(String user){
		if(user.length() == 0){ // This actually returns a user with no images!
			Toast.makeText(this.getActivity(), R.string.picasaShowStreamNoUsername, Toast.LENGTH_LONG).show();
			return;
		}
		if(user == null) {// Bad username.
			Toast.makeText(this.getActivity(), R.string.picasaShowStreamBadUsername, Toast.LENGTH_LONG).show();
			return; 
		}
		if(mDualPane){
			FragmentTransaction ft = getFragmentManager().beginTransaction();
	        ft.replace(R.id.source, PicasaAlbumBrowser.getInstance(null));
	        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
	        ft.commit();
		}else{
			Intent intent = new Intent(this.getActivity(), UserActivity.class);
			intent.putExtra("ID", user);
			startActivityForResult(intent, SHOW_USER);
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
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == Activity.RESULT_OK){
			data.putExtra("TYPE", Settings.TYPE_PICASA);
			switch(requestCode){
			case SHOW_USER:
				this.getActivity().setResult(Activity.RESULT_OK, data);
				this.getActivity().finish();
				break;
			case SHOW_ALBUMS:
				data.putExtra("EXTRAS", getString(R.string.albumBy) + " " + getString(R.string.me));
				this.getActivity().setResult(Activity.RESULT_OK, data);
				this.getActivity().finish();
				break;
			}
		}
	}
	private void returnSearch(String criteria){
		if(criteria.length() == 0){
			Toast.makeText(this.getActivity(), R.string.picasaSearchNoText, Toast.LENGTH_LONG).show();
			return;
		}
		Intent intent = new Intent();
		Bundle b = new Bundle();
		String streamURL = PicasaFeeder.getSearchUrl(criteria);
		b.putString("PATH", streamURL);
		b.putInt("TYPE", Settings.TYPE_PICASA);
		b.putString("NAME", "Search: " + criteria);
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

				Fragment f = PicasaAlbumBrowser.getInstance(this.getIntent().getStringExtra(PicasaAlbumBrowser.OWNER));
				f.setArguments(getIntent().getExtras());
				getSupportFragmentManager().beginTransaction().add(android.R.id.content, f, "content").commit();
			}
		}
	}
	
	public static class UserActivity extends FragmentActivity{
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
	            	
	            	Fragment f = PicasaUserView.getInstance(this.getIntent().getStringExtra("ID"));
	            	f.setArguments(getIntent().getExtras());
		            getSupportFragmentManager().beginTransaction().add(android.R.id.content, f, "content").commit();
	            }
	        }
	}

	@Override
	public boolean back() {
		return false;
	}
}
