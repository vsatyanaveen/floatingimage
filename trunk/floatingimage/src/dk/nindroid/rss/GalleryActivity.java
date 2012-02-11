package dk.nindroid.rss;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import dk.nindroid.rss.menu.GallerySettings;
import dk.nindroid.rss.settings.FeedSettings;
import dk.nindroid.rss.settings.FeedsDbAdapter;
import dk.nindroid.rss.settings.ManageFeeds;
import dk.nindroid.rss.settings.Settings;

public class GalleryActivity extends ListActivity {
	public static final int ADD_FEED = 0;
	
	GalleryListAdapter mAdapter;
	Cursor mCursor;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.gallery);
		Button settingsButton = (Button)findViewById(R.id.settings);
		settingsButton.setOnClickListener(new SettingsListener());
		
		View showNew = findViewById(R.id.show_new);
		showNew.setBackgroundResource(android.R.drawable.list_selector_background);
		ImageView newIcon = (ImageView)showNew.findViewById(R.id.icon);
		newIcon.setImageResource(R.drawable.plus_circle);
		showNew.findViewById(R.id.enabled).setVisibility(View.GONE);
		TextView newText = (TextView)showNew.findViewById(android.R.id.title);
		TextView newSummary = (TextView)showNew.findViewById(android.R.id.summary);
		newText.setText(R.string.feedMenuAdd);
		newSummary.setText(R.string.feedMenuAddSummary);
		showNew.setOnClickListener(new ShowNewListener());
		
		Editor e = this.getSharedPreferences(GallerySettings.SHARED_PREFS_NAME, 0).edit();
		e.putBoolean("galleryMode", true);
		e.commit();
	}
	
	class ShowNewListener implements OnClickListener{
		@Override
		public void onClick(View v) {
			Intent intent = new Intent(GalleryActivity.this, ManageFeeds.class);
			intent.putExtra(ManageFeeds.ADD_FEED, true);
			intent.putExtra(ManageFeeds.HIDE_CHECKBOXES, true);
			intent.putExtra(ManageFeeds.SHARED_PREFS_NAME, GallerySettings.SHARED_PREFS_NAME);
			startActivityForResult(intent, ADD_FEED);
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch(requestCode){
		case ADD_FEED:
			if(resultCode == Activity.RESULT_OK){
				//fillFeeds();
			}
			break;
		}
	}
	
	private void fillFeeds(){
		if(mCursor != null){
			//stopManagingCursor(mCursor);
		}
		FeedsDbAdapter db = new FeedsDbAdapter(this).open();
		mCursor = db.fetchAllFeeds();
		mAdapter = new GalleryListAdapter(mCursor);
		setListAdapter(mAdapter);
		//this.startManagingCursor(mCursor);
		db.close();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		fillFeeds();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		this.setListAdapter(new ArrayAdapter<String>(GalleryActivity.this, android.R.layout.simple_list_item_1, new String[]{}));
		mCursor.close();
	}
	
	class SettingsListener implements OnClickListener{
		@Override
		public void onClick(View v) {
			Intent intent = new Intent(GalleryActivity.this, GallerySettings.class);
			startActivity(intent);
		}
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Intent intent = new Intent(this, ShowStreams.class);
		intent.putExtra(ShowStreams.SHOW_FEED_ID, (int)mAdapter.getItemId(position));
		intent.putExtra(FeedSettings.HIDE_ACTIVE, true);
		intent.putExtra(ShowStreams.SETTINGS_NAME, GallerySettings.SHARED_PREFS_NAME);
		this.startActivity(intent);
	}
		
	private class GalleryListAdapter extends BaseAdapter{
		Cursor mCursor;
		int idi;
		int typei;
		int namei;
		int extrasi;
		int userNamei;
		int userExtrai;
		final LayoutInflater inflater;
		
		public GalleryListAdapter(Cursor c) {
			this.mCursor = c;
			
			this.inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
			idi = c.getColumnIndex(FeedsDbAdapter.KEY_ROWID);
			typei = c.getColumnIndex(FeedsDbAdapter.KEY_TYPE);
			namei = c.getColumnIndex(FeedsDbAdapter.KEY_TITLE);
			extrasi = c.getColumnIndex(FeedsDbAdapter.KEY_EXTRA);
			userNamei = c.getColumnIndex(FeedsDbAdapter.KEY_USER_TITLE);
			userExtrai = c.getColumnIndex(FeedsDbAdapter.KEY_USER_EXTRA);
		}
		
		@Override
		public int getCount() {
			if(mCursor.isClosed()){
				return 0;
			}
			return mCursor.getCount();
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			if(!mCursor.isClosed()){
				mCursor.moveToPosition(position);
				return mCursor.getInt(idi);
			}
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LinearLayout l;
			
			if(convertView instanceof LinearLayout){
				l = (LinearLayout)convertView;
			}else{
				l = (LinearLayout)inflater.inflate(R.layout.feeds_row, parent, false);
				l.findViewById(R.id.enabled).setVisibility(View.GONE);
				l.findViewById(R.id.edit).setVisibility(View.VISIBLE);
			}
			if(mCursor.isClosed()){
				return l;
			}
			mCursor.moveToPosition(position);
			
			TextView nameView = (TextView)l.findViewById(android.R.id.title);
			TextView extraView = (TextView)l.findViewById(android.R.id.summary);
			ImageView iconView = (ImageView)l.findViewById(R.id.icon);
			ImageView editView = (ImageView)l.findViewById(R.id.edit);
			
			int type = mCursor.getInt(typei);
			String name = mCursor.getString(namei);
			String extras = mCursor.getString(extrasi);
			String uName = mCursor.getString(userNamei);
			String uExtras = mCursor.getString(userExtrai);
			int id = mCursor.getInt(idi);
			
			if(uName == null || uName.length() == 0){
				nameView.setText(name);
			}else{
				nameView.setText(uName);
			}
			
			if(uExtras == null || uExtras.length() == 0){
				extraView.setText(extras);
			}else{
				extraView.setText(uExtras);
			}
			
			int iconId = getIconId(type);
			iconView.setImageResource(iconId);
			
			editView.setOnClickListener(new OnEditListener(id));
			
			return l;
		}
		
		private class OnEditListener implements OnClickListener{
			int id;
			
			public OnEditListener(int id){
				this.id = id;
			}
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(GalleryActivity.this, FeedSettings.class);
				intent.putExtra(FeedSettings.HIDE_ACTIVE, true);
				intent.putExtra(FeedSettings.NEW_FEED, false);
				intent.putExtra(FeedSettings.FEED_ID, this.id);
				intent.putExtra(ManageFeeds.SHARED_PREFS_NAME, GallerySettings.SHARED_PREFS_NAME);
				startActivity(intent);
			}
			
		}
		
		int getIconId(int type){
			switch(type){
			case Settings.TYPE_LOCAL:
				return R.drawable.phone_icon;
			case Settings.TYPE_FLICKR:
				return R.drawable.flickr_icon;
			case Settings.TYPE_FACEBOOK:
				return R.drawable.facebook_icon;
			case Settings.TYPE_PICASA:
				return R.drawable.picasa_icon;
			case Settings.TYPE_PHOTOBUCKET:
				return R.drawable.photobucket_icon;
			case Settings.TYPE_FIVEHUNDREDPX:
				return R.drawable.fivehundredpx_icon;
			case Settings.TYPE_RSS:
				return R.drawable.rss_icon;
			}
			return R.drawable.phone_icon;
		}
	}
}
