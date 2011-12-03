package dk.nindroid.rss;

import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import dk.nindroid.rss.menu.GallerySettings;
import dk.nindroid.rss.settings.FeedsDbAdapter;
import dk.nindroid.rss.settings.Settings;

public class GalleryActivity extends ListActivity {
	GalleryListAdapter mAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.gallery);
		
		Button settingsButton = (Button)findViewById(R.id.settings);
		settingsButton.setOnClickListener(new SettingsListener());
		
		Editor e = this.getSharedPreferences(GallerySettings.SHARED_PREFS_NAME, 0).edit();
		e.putBoolean("galleryMode", true);
		e.commit();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		FeedsDbAdapter db = new FeedsDbAdapter(this).open();
		Cursor c = db.fetchAllFeeds();
		mAdapter = new GalleryListAdapter(c);
		setListAdapter(mAdapter);
		this.startManagingCursor(c);
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
		intent.putExtra(ShowStreams.SETTINGS_NAME, GallerySettings.SHARED_PREFS_NAME);
		this.startActivity(intent);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		System.exit(0);
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
			return mCursor.getCount();
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			mCursor.moveToPosition(position);
			return mCursor.getInt(idi);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LinearLayout l;
			
			if(convertView instanceof LinearLayout){
				l = (LinearLayout)convertView;
			}else{
				l = (LinearLayout)inflater.inflate(R.layout.feeds_row, parent, false);
				l.findViewById(R.id.enabled).setVisibility(View.GONE);
			}
			mCursor.moveToPosition(position);
			
			TextView nameView = (TextView)l.findViewById(android.R.id.title);
			TextView extraView = (TextView)l.findViewById(android.R.id.summary);
			ImageView iconView = (ImageView)l.findViewById(R.id.icon);
			
			int type = mCursor.getInt(typei);
			String name = mCursor.getString(namei);
			String extras = mCursor.getString(extrasi);
			String uName = mCursor.getString(userNamei);
			String uExtras = mCursor.getString(userExtrai);
			
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
			
			return l;
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
			}
			return R.drawable.phone_icon;
		}
	}
}
