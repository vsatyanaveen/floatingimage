package dk.nindroid.rss.settings;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

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
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import dk.nindroid.rss.R;
import dk.nindroid.rss.data.KeyVal;

public class FeedSettings extends Activity{
	public static final String FEED_ID = "feed_id";
	public static final String NEW_FEED = "new_feed";
	public static final String HIDE_ACTIVE = "hide_active";
	
	CheckBox mActive;
	EditText mTitle;
	EditText mExtra;
	Spinner mSorting;
	TextView mSubDirs;
	ListView mList;
	CheckBox mAllSubdirs;
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
		mAllSubdirs = (CheckBox)findViewById(R.id.allSubdirs);
		
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
	    mSubDirs = (TextView)findViewById(R.id.subdirs);
	    mList = (ListView)findViewById(android.R.id.list);
	    
	    mAllSubdirs.setOnCheckedChangeListener(new AllSubdirsChanged());
	    mAllSubdirs.setChecked(getSharedPreferences(dk.nindroid.rss.menu.Settings.SHARED_PREFS_NAME, 0).getBoolean("feed_allsub_" + mId, false));
	    
	    mDb = new FeedsDbAdapter(this).open();
		setData();
		mDb.close();
	    
	    ((Button)findViewById(R.id.ok)).setOnClickListener(new OkClicked());
	    ((Button)findViewById(R.id.cancel)).setOnClickListener(new CancelClicked());
	    Button delete = (Button)findViewById(R.id.delete);
	    if(mNewFeed){
	    	delete.setVisibility(View.GONE);
	    }else{
	    	delete.setOnClickListener(new DeleteClicked());
	    }
	}
	
	private class AllSubdirsChanged implements OnCheckedChangeListener{
		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			mList.setEnabled(!isChecked);
			if(isChecked){
				for(int i = 0; i < mList.getCount(); ++i){
					mList.setItemChecked(i, true);
				}
			}else{
				mDb.open();
				setFileChecksFromDb();
				mDb.close();
			}
		}
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
			
			fillList(c);
			
			mSorting.setSelection(sorting, true);
			mSorting.invalidate();
		}
		c.close();
	}
	
	private void fillList(Cursor c){
		int iType = c.getColumnIndex(FeedsDbAdapter.KEY_TYPE);
		int iUri = c.getColumnIndex(FeedsDbAdapter.KEY_URI);
		
		int type = c.getInt(iType);
		if(type == Settings.TYPE_LOCAL){
			mAllSubdirs.setVisibility(View.VISIBLE);
			String uri = c.getString(iUri);
			File f = new File(uri);
			if(f.exists()){
				File[] dirs = f.listFiles(new DirFilter());
				if(dirs.length > 0){					
					mSubDirs.setVisibility(View.VISIBLE);
					mList.setVisibility(View.VISIBLE);
					mList.setAdapter(new ArrayAdapter<File>(this, android.R.layout.simple_list_item_multiple_choice, dirs));
					mList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
					
					setFileChecksFromDb();
				}
			}
		}
	}
	
	private void setFileChecksFromDb(){
		// Load if items are checked
		Cursor subC = mDb.getSubDirs(mId);
		int iDir = subC.getColumnIndex(FeedsDbAdapter.KEY_DIR);
		int iEnabled = subC.getColumnIndex(FeedsDbAdapter.KEY_ENABLED);
		List<KeyVal<String, Boolean>> saved = new ArrayList<KeyVal<String, Boolean>>();
		while(subC.moveToNext()){
			saved.add(new KeyVal<String, Boolean>(subC.getString(iDir), subC.getInt(iEnabled) == 1));
		}
		subC.close();
		for(int i = 0; i < mList.getCount(); ++i){
			File d = (File)mList.getItemAtPosition(i);
			KeyVal<String, Boolean> kv = find(saved, d.getName());
			if(kv == null){
				mList.setItemChecked(i, false);
			}else{
				mList.setItemChecked(i, kv.getVal());
			}
		}
	}
	
	public static KeyVal<String, Boolean> find(List<KeyVal<String, Boolean>> list, String key){
		for(KeyVal<String, Boolean> kv : list){
			if(kv.getKey().equals(key)){
				return kv;
			}
		}
		return null;
	}
	
	public static class DirFilter implements FileFilter{
		@Override
		public boolean accept(File pathname) {
			return pathname.isDirectory() && !pathname.isHidden();
		}

	}
	
	boolean empty(String s){
		return s == null || s.length() == 0;
	}
	
	void saveFeed(){
		mDb.updateFeed(mId, mSorting.getSelectedItemPosition(), mTitle.getText().toString(), mExtra.getText().toString());
		
		mDb.deleteSubdirs(mId);
		if(mList.getVisibility() == View.VISIBLE){
			for(int i = 0; i < mList.getCount(); ++i){
				String dir = ((File)mList.getItemAtPosition(i)).getName();
				boolean enabled = mList.isItemChecked(i);
				mDb.addSubDir(mId, dir, enabled);
			}
		}
		
		Editor e = getSharedPreferences(mSharedPreferences, 0).edit();
		e.putBoolean("feed_" + mId, this.mActive.isChecked());
		e.commit();
		
		e = getSharedPreferences(dk.nindroid.rss.menu.Settings.SHARED_PREFS_NAME, 0).edit();
		e.putBoolean("feed_allsub_" + mId, this.mAllSubdirs.isChecked());
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
			returnCancel();
		}
	}
	
	private void returnCancel(){
		Bundle b = new Bundle();
		b.putInt(FeedSettings.FEED_ID, mId);
		Intent intent = new Intent();
		intent.putExtras(b);
		setResult(RESULT_CANCELED, intent);
		FeedSettings.this.finish();
	}
	
	private class DeleteClicked implements OnClickListener{
		@Override
		public void onClick(View v) {
			mDb.open();
			mDb.deleteFeed(mId);
			mDb.close();
			setResult(RESULT_OK);
			finish();
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK){
			returnCancel();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
}
