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
import dk.nindroid.rss.parser.photobucket.PhotobucketFeeder;
import dk.nindroid.rss.parser.photobucket.PhotobucketShowUser;
import dk.nindroid.rss.settings.SourceSelector.SourceFragment;

public class PhotobucketBrowser extends SourceFragment {
	public final static int SHOW_USER = 0;
	public final static int SHOW_GROUP = 1;
	public final static int SEARCH = 2;
	
	public PhotobucketBrowser() {
		super(4);
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
	
	void fillMenu(){
		String showUser = getString(R.string.photobucketShowUser);
		String showGroup = getString(R.string.photobucketShowGroup);
		String search = getString(R.string.photobucketSearch);
		String[] options = new String[]{showUser, showGroup, search};
		setListAdapter(new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_list_item_1, options));
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		switch(position){
		case SHOW_USER:
			showUser();
			break;
		case SHOW_GROUP:
			showGroup();
			break;
		case SEARCH:
			search();
			break;
		}
	}
	
	void showUser(){
		FrameLayout fl = new FrameLayout(this.getActivity());
		final EditText input = new EditText(this.getActivity());

		fl.addView(input, FrameLayout.LayoutParams.FILL_PARENT);
		input.setGravity(Gravity.CENTER);
		final AlertDialog streamDialog = new AlertDialog.Builder(this.getActivity())
		.setView(fl)
		.setTitle(R.string.photobucketShowUser)
		.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(input.getText().toString().isEmpty()){
					Toast.makeText(getActivity(), R.string.photobucketEmptyUserSearch, Toast.LENGTH_LONG).show();
				}else{
					dialog.dismiss();
					showUser(input.getText().toString());
				}
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
	
	void showGroup(){
		FrameLayout fl = new FrameLayout(this.getActivity());
		final EditText input = new EditText(this.getActivity());

		fl.addView(input, FrameLayout.LayoutParams.FILL_PARENT);
		input.setGravity(Gravity.CENTER);
		final AlertDialog streamDialog = new AlertDialog.Builder(this.getActivity())
		.setView(fl)
		.setTitle(R.string.photobucketShowGroup)
		.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(input.getText().toString().isEmpty()){
					Toast.makeText(getActivity(), R.string.photobucketEmptyGroupSearch, Toast.LENGTH_LONG).show();
				}else{
					dialog.dismiss();
					returnUrl(PhotobucketFeeder.getGroup(input.getText().toString()), input.getText().toString(), getString(R.string.photobucketGroup));
				}
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
	
	void search(){
		FrameLayout fl = new FrameLayout(this.getActivity());
		final EditText input = new EditText(this.getActivity());

		fl.addView(input, FrameLayout.LayoutParams.FILL_PARENT);
		input.setGravity(Gravity.CENTER);
		final AlertDialog streamDialog = new AlertDialog.Builder(this.getActivity())
		.setView(fl)
		.setTitle(R.string.photobucketSearch)
		.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(input.getText().toString().isEmpty()){
					Toast.makeText(getActivity(), R.string.photobucketEmptySearch, Toast.LENGTH_LONG).show();
				}else{
					dialog.dismiss();
					returnUrl(PhotobucketFeeder.search(input.getText().toString()), input.getText().toString(), getString(R.string.photobucketSearch));
				}
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
	
	void showUser(String user){
		if(mDualPane){
			FragmentTransaction ft = getFragmentManager().beginTransaction();
	        ft.replace(R.id.source, PhotobucketShowUser.getInstance(user), "content");
	        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
	        ft.commit();
		}else{
			Intent intent = new Intent(this.getActivity(), ShowUserActivity.class);
			intent.putExtra(PhotobucketShowUser.USER, user);
			startActivityForResult(intent, SHOW_USER);
		}
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == Activity.RESULT_OK){
			switch(requestCode){
			case SHOW_USER:
				this.getActivity().setResult(Activity.RESULT_OK, data);
				this.getActivity().finish();
				break;
			}
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
	public boolean back() {
		return false;
	}
	
	public static class ShowUserActivity extends FragmentActivity{
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

				Fragment f = PhotobucketShowUser.getInstance(this.getIntent().getStringExtra(PhotobucketShowUser.USER));
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
		b.putInt("TYPE", Settings.TYPE_PHOTOBUCKET);
		intent.putExtras(b);
		this.getActivity().setResult(Activity.RESULT_OK, intent);		
		this.getActivity().finish();
	}
}
