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
import dk.nindroid.rss.parser.fivehundredpx.CategoryBrowser;
import dk.nindroid.rss.parser.fivehundredpx.DiscoverBrowser;
import dk.nindroid.rss.parser.fivehundredpx.FiveHundredPxFeeder;
import dk.nindroid.rss.parser.fivehundredpx.VerifyUserTask;

public class FiveHundredPxBrowser extends SourceSelector.SourceFragment implements VerifyUserTask.Callback {
	public static final int DISCOVER_PHOTOS = 0;
	public static final int SHOW_CATEGORY = 1;
	public static final int SHOW_USER = 2;
	public static final int SHOW_FAVORITES = 3;
	public static final int SHOW_FRIENDS = 4;
	public static final int SEARCH = 5;
	
	public FiveHundredPxBrowser() {
		super(5);
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
		fillMenu();
	}
	
	private void fillMenu(){
			String discover = this.getResources().getString(R.string.fivehundredpxDiscover);
			String category = this.getResources().getString(R.string.fivehundredpxCategories);
			String user = this.getResources().getString(R.string.fivehundredpxShowUser);
			String favorites = this.getResources().getString(R.string.fivehundredpxShowFavorites);
			String friends = this.getResources().getString(R.string.fivehundredpxShowFriends);
			String search = this.getResources().getString(R.string.fivehundredpxSearch);
			String[] options = new String[]{discover, category, user, favorites, friends, search};
			setListAdapter(new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_list_item_1, options));
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		switch(position){
		case DISCOVER_PHOTOS:
			discover();
			break;
		case SHOW_CATEGORY:
			categories();
			break;
		case SHOW_USER:
			showUser();
			break;
		case SHOW_FAVORITES:
			showFavorites();
			break;
		case SHOW_FRIENDS:
			showFriends();
			break;
		case SEARCH:
			search();
			break;
		}
	}
	
	void discover(){
		if(mDualPane){
			FragmentTransaction ft = getFragmentManager().beginTransaction();
	        ft.replace(R.id.source, new DiscoverBrowser(), "content");
	        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
	        ft.commit();
		}else{
			Intent intent = new Intent(this.getActivity(), DiscoverActivity.class);
			startActivityForResult(intent, DISCOVER_PHOTOS);
		}
	}
	
	void categories(){
		if(mDualPane){
			FragmentTransaction ft = getFragmentManager().beginTransaction();
	        ft.replace(R.id.source, new CategoryBrowser(), "content");
	        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
	        ft.commit();
		}else{
			Intent intent = new Intent(this.getActivity(), CategoryActivity.class);
			startActivityForResult(intent, SHOW_CATEGORY);
		}
	}
	
	void showUser(){
		FrameLayout fl = new FrameLayout(this.getActivity());
		final EditText input = new EditText(this.getActivity());

		fl.addView(input, FrameLayout.LayoutParams.FILL_PARENT);
		input.setGravity(Gravity.CENTER);
		final AlertDialog searchDialog = new AlertDialog.Builder(this.getActivity())
		.setView(fl)
		.setTitle(R.string.fivehundredpxTypeUsername)
		.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				String user = input.getText().toString();
				verifyUser(FiveHundredPxFeeder.getUser(user), user, getString(R.string.fivehundredpxShowing, user), "");
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
	
	void showFavorites(){
		FrameLayout fl = new FrameLayout(this.getActivity());
		final EditText input = new EditText(this.getActivity());

		fl.addView(input, FrameLayout.LayoutParams.FILL_PARENT);
		input.setGravity(Gravity.CENTER);
		final AlertDialog searchDialog = new AlertDialog.Builder(this.getActivity())
		.setView(fl)
		.setTitle(R.string.fivehundredpxTypeUsername)
		.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				String user = input.getText().toString();
				verifyUser(FiveHundredPxFeeder.getUserFavorites(user), user, getString(R.string.fivehundredpxShowingUsersFavorites, user), "");
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
	
	void showFriends(){
		FrameLayout fl = new FrameLayout(this.getActivity());
		final EditText input = new EditText(this.getActivity());

		fl.addView(input, FrameLayout.LayoutParams.FILL_PARENT);
		input.setGravity(Gravity.CENTER);
		final AlertDialog searchDialog = new AlertDialog.Builder(this.getActivity())
		.setView(fl)
		.setTitle(R.string.fivehundredpxTypeUsername)
		.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				String user = input.getText().toString();
				verifyUser(FiveHundredPxFeeder.getUserFriends(user), user, getString(R.string.fivehundredpxShowingUsersFriends, user), "");
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
	
	void search(){
		FrameLayout fl = new FrameLayout(this.getActivity());
		final EditText input = new EditText(this.getActivity());

		fl.addView(input, FrameLayout.LayoutParams.FILL_PARENT);
		input.setGravity(Gravity.CENTER);
		final AlertDialog searchDialog = new AlertDialog.Builder(this.getActivity())
		.setView(fl)
		.setTitle(R.string.fivehundredpxTypeSearchTerm)
		.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String term = input.getText().toString();
				if(term.isEmpty()){
					Toast.makeText(getActivity(), R.string.fivehundredpxEmptySearch, Toast.LENGTH_LONG).show();
				}else{
					dialog.dismiss();
					returnUrl(FiveHundredPxFeeder.getSearch(term), getString(R.string.fivehundredpxSearch), term);
				}
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
	
	private void verifyUser(String url, String user, String title, String extras){
		VerifyUserTask task = new VerifyUserTask(getActivity(), this, url, title, extras);
		task.execute(user);
	}
	
	@Override
	public void userVerified(String url, boolean isVerified, String title, String extras){
		if(isVerified){
			returnUrl(url, title, extras);
		}else{
			Toast.makeText(getActivity(), R.string.fivehundredpxUserNotFound, Toast.LENGTH_LONG).show();
		}
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == Activity.RESULT_OK){
			switch(requestCode){
			case DISCOVER_PHOTOS:
				this.getActivity().setResult(Activity.RESULT_OK, data);
				Bundle b1 = data.getExtras();
				b1.putInt("TYPE", Settings.TYPE_FIVEHUNDREDPX);
				this.getActivity().finish();
				break;
			case SHOW_CATEGORY:
				this.getActivity().setResult(Activity.RESULT_OK, data);
				Bundle b2 = data.getExtras();
				b2.putInt("TYPE", Settings.TYPE_FIVEHUNDREDPX);
				this.getActivity().finish();
				break;
			}
		}
	}
	
	
	
	public static class DiscoverActivity extends FragmentActivity{
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

				Fragment f = new DiscoverBrowser();
				f.setArguments(getIntent().getExtras());
				getSupportFragmentManager().beginTransaction().add(android.R.id.content, f).commit();
			}
		}
	}
	
	void returnUrl(String url, String title, String extras){
		Intent intent = new Intent();
		Bundle b = new Bundle();
		b.putString("PATH", url);
		b.putString("NAME", title);
		b.putString("EXTRAS", extras);
		b.putInt("TYPE", Settings.TYPE_FIVEHUNDREDPX);
		intent.putExtras(b);
		this.getActivity().setResult(Activity.RESULT_OK, intent);		
		this.getActivity().finish();
	}
	
	public static class CategoryActivity extends FragmentActivity{
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

				Fragment f = new CategoryBrowser();
				
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
