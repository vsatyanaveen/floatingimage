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
import dk.nindroid.rss.R;
import dk.nindroid.rss.parser.photobucket.PhotobucketFeeder;
import dk.nindroid.rss.parser.photobucket.PhotobucketShowUser;

public class PhotobucketBrowser extends ListActivity {
	public final static int SHOW_USER = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		fillMenu();
		PhotobucketFeeder.test();
	}
	
	void fillMenu(){
		String showUser = getString(R.string.photobucketShowUser);
		String[] options = new String[]{showUser};
		setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, options));
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		switch(position){
		case SHOW_USER:
			showUser();
			break;
		}
	}
	
	void showUser(){
		FrameLayout fl = new FrameLayout(this);
		final EditText input = new EditText(this);

		fl.addView(input, FrameLayout.LayoutParams.FILL_PARENT);
		input.setGravity(Gravity.CENTER);
		final AlertDialog streamDialog = new AlertDialog.Builder(this)
		.setView(fl)
		.setTitle(R.string.flickrShowStreamUsername)
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
	
	void showUser(String user){
		Intent intent = new Intent(this, PhotobucketShowUser.class);
		Bundle b = new Bundle();
		b.putString(PhotobucketShowUser.USER, user);
		intent.putExtras(b);
		this.startActivityForResult(intent, SHOW_USER);
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
}
