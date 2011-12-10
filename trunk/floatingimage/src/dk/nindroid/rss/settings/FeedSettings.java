package dk.nindroid.rss.settings;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import dk.nindroid.rss.R;

public class FeedSettings extends Activity{
	public static final String FEED_ID = "feed_id";
	public static final String NEW_FEED = "new_feed";
	public static final String HIDE_ACTIVE = "hide_active";
	
	CheckBox mActive;
	EditText mTitle;
	EditText mExtra;
	Spinner mSorting;
	int mId;
	FeedsDbAdapter mDb;
	
	String mTitleString;
	String mExtraString;
	
	String mSharedPreferences;
	
	boolean mNewFeed;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mSharedPreferences = this.getIntent().getExtras().getString(ManageFeeds.SHARED_PREFS_NAME);
		boolean hideActive = this.getIntent().getExtras().getBoolean(HIDE_ACTIVE);
		mNewFeed = this.getIntent().getExtras().getBoolean(NEW_FEED, false);
		
		setContentView(R.layout.feed_settings);
		mActive = (CheckBox)findViewById(R.id.active);
		mTitle = (EditText)findViewById(R.id.title);
		mExtra = (EditText)findViewById(R.id.extra);
		mSorting = (Spinner)findViewById(R.id.sortOrder);
		
		if(hideActive){
			mActive.setVisibility(View.GONE);
			findViewById(R.id.activeLabel).setVisibility(View.GONE);
		}
		
		mId = getIntent().getIntExtra(FEED_ID, -1);
		
		boolean local = true;
		int sortId = local ? R.array.sortOrderFiles : R.array.sortOrderOnline;
		
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
	            this, sortId, android.R.layout.simple_spinner_item);
	    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    mSorting.setAdapter(adapter);
	    
	    mDb = new FeedsDbAdapter(this).open();
		setData();
		mDb.close();
	    
	    ((Button)findViewById(R.id.ok)).setOnClickListener(new OkClicked());
	    ((Button)findViewById(R.id.cancel)).setOnClickListener(new CancelClicked());
	}
		
	void setData(){
		Cursor c = mDb.fetchFeed(mId);
		if(c.moveToFirst()){
			int iTitle = c.getColumnIndex(FeedsDbAdapter.KEY_TITLE);
			int iExtra = c.getColumnIndex(FeedsDbAdapter.KEY_EXTRA);
			int iSorting = c.getColumnIndex(FeedsDbAdapter.KEY_SORTING);
			int iUserTitle = c.getColumnIndex(FeedsDbAdapter.KEY_USER_TITLE);
			int iUserExtra = c.getColumnIndex(FeedsDbAdapter.KEY_USER_EXTRA);
			
			this.mTitleString = c.getString(iTitle);
			this.mExtraString = c.getString(iExtra);
			int sorting = c.getInt(iSorting);
			String uTitle = c.getString(iUserTitle);
			String uExtra = c.getString(iUserExtra);
			SharedPreferences sp = getSharedPreferences(mSharedPreferences, 0);
			boolean enabled = sp.getBoolean("feed_" + Integer.toString(mId), true);
			this.mActive.setChecked(enabled || mNewFeed);
			
			this.mTitle.setHint(mTitleString);
			this.mExtra.setHint(mExtraString);
			
			if(empty(uTitle)){
				this.mTitle.setText(mTitleString);
			}else{
				this.mTitle.setText(uTitle);
			}
			if(empty(uExtra)){
				this.mExtra.setText(mExtraString);
			}else{
				this.mExtra.setText(uExtra);
			}
			
			mSorting.setSelection(sorting, true);
			mSorting.invalidate();
		}
	}
	
	boolean empty(String s){
		return s == null || s.length() == 0;
	}
	
	void saveFeed(){
		mDb.updateFeed(mId, mSorting.getSelectedItemPosition(), mTitle.getText().toString(), mExtra.getText().toString());
		Editor e = getSharedPreferences(mSharedPreferences, 0).edit();
		e.putBoolean("feed_" + mId, this.mActive.isChecked());
		e.commit();
	}
	
	private class OkClicked implements OnClickListener{
		@Override
		public void onClick(View v) {
			returnOk();
		}
	}
	
	void returnOk(){
		mDb.open();
		saveFeed();
		mDb.close();
		Bundle b = new Bundle();
		b.putInt(FeedSettings.FEED_ID, mId);
		Intent intent = new Intent();
		intent.putExtras(b);
		setResult(RESULT_OK, intent);
		FeedSettings.this.finish();
	}
	
	private class CancelClicked implements OnClickListener{
		@Override
		public void onClick(View v) {
			Bundle b = new Bundle();
			b.putInt(FeedSettings.FEED_ID, mId);
			Intent intent = new Intent();
			intent.putExtras(b);
			setResult(RESULT_CANCELED, intent);
			FeedSettings.this.finish();
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK){
			returnOk();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
}
