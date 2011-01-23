package dk.nindroid.rss.settings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView.AdapterContextMenuInfo;
import dk.nindroid.rss.R;
import dk.nindroid.rss.gfx.ImageUtil;

public class ManageFeeds extends PreferenceActivity {
	public static final int ADD_ID = Menu.FIRST;
	public static final int CLEAR_ALL_ID = Menu.FIRST + 1;
    private static final int DELETE_ID = Menu.FIRST + 1;
    private static final int SELECT_FOLDER = 12;
	private FeedsDbAdapter 	mDbHelper;
	private List<Feed> 	mRowList = new ArrayList<Feed>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// This works, just need to pass which preference to use
		//PreferenceManager pm = this.getPreferenceManager();
		//pm.setSharedPreferencesName(this.getIntent().getDataString());
		setContentView(R.layout.manage_feeds);
		mDbHelper = new FeedsDbAdapter(this);
		registerForContextMenu(getListView());
		setPreferenceScreen(createPreferenceHierarchy());
	}
	
	void addFeed(){
		startActivityForResult(new Intent(this, SourceSelector.class), SELECT_FOLDER);
	}
	
	private PreferenceScreen createPreferenceHierarchy() {
		PreferenceScreen root = getPreferenceManager().createPreferenceScreen(this);
		
		mRowList.clear();
		Map<Integer, List<Feed>> data = getData();
		
		List<Feed> local = data.get(Settings.TYPE_LOCAL);
		List<Feed> facebook = data.get(Settings.TYPE_FACEBOOK);
		List<Feed> flickr = data.get(Settings.TYPE_FLICKR);
		List<Feed> picasa = data.get(Settings.TYPE_PICASA);
		
		if(local.size() != 0){
			Bitmap bmp = readBitmap(R.drawable.phone_icon);
			PreferenceCategory localCat = new PreferenceCategory(this);
			mRowList.add(null);
			localCat.setTitle(R.string.local);
			root.addPreference(localCat);
			for(Feed f : local){
				localCat.addPreference(createCheckbox(f, bmp));
				mRowList.add(f);
			}
		}
		
		if(facebook.size() != 0){
			Bitmap bmp = readBitmap(R.drawable.facebook_icon);
			PreferenceCategory facebookCat = new PreferenceCategory(this);
			mRowList.add(null);
			facebookCat.setTitle(R.string.facebook);
			root.addPreference(facebookCat);
			for(Feed f : facebook){
				facebookCat.addPreference(createCheckbox(f, bmp));
				mRowList.add(f);
			}
		}
		
		if(flickr.size() != 0){
			Bitmap bmp = readBitmap(R.drawable.flickr_icon);
			PreferenceCategory flickrCat = new PreferenceCategory(this);
			mRowList.add(null);
			flickrCat.setTitle(R.string.flickr);
			root.addPreference(flickrCat);
			for(Feed f : flickr){
				flickrCat.addPreference(createCheckbox(f, bmp));
				mRowList.add(f);
			}
		}
		
		if(picasa.size() != 0){
			Bitmap bmp = readBitmap(R.drawable.picasa_icon);
			PreferenceCategory picasaCat = new PreferenceCategory(this);
			mRowList.add(null);
			picasaCat.setTitle(R.string.picasa);
			root.addPreference(picasaCat);
			for(Feed f : picasa){
				picasaCat.addPreference(createCheckbox(f, bmp));
				mRowList.add(f);
			}
		}
		
		PreferenceCategory newCat = new PreferenceCategory(this);
		newCat.setTitle("");
		root.addPreference(newCat);
		mRowList.add(null);
		PreferenceScreen add = getPreferenceManager().createPreferenceScreen(this);
		add.setOnPreferenceClickListener(new AddClickListener());
		add.setTitle(R.string.feedMenuAdd);
		add.setSummary(R.string.feedMenuAddSummary);
		newCat.addPreference(add);
		mRowList.add(null);
		return root;
	}
	
	Bitmap readBitmap(int res){
		return ImageUtil.readBitmap(this, res);
	}
	
	private Preference createCheckbox(Feed f, Bitmap bmp){
		Preference pref = new ManageFeedPreference(this, bmp);
		pref.setKey("feed_" + Integer.toString(f.id));
		pref.setDefaultValue(true);
		pref.setTitle(f.title);
		pref.setSummary(f.extras);
		return pref;
	}
	
	private Map<Integer, List<Feed>> getData(){
		mDbHelper.open();
		Cursor c = mDbHelper.fetchAllFeeds();
		startManagingCursor(c);
		Map<Integer, List<Feed>> data = new HashMap<Integer, List<Feed>>();
		data.put(Settings.TYPE_LOCAL, new ArrayList<Feed>());
		data.put(Settings.TYPE_FLICKR, new ArrayList<Feed>());
		data.put(Settings.TYPE_FACEBOOK, new ArrayList<Feed>());
		data.put(Settings.TYPE_PICASA, new ArrayList<Feed>());
		int idi = c.getColumnIndex(FeedsDbAdapter.KEY_ROWID);
		int typei = c.getColumnIndex(FeedsDbAdapter.KEY_TYPE);
		int namei = c.getColumnIndex(FeedsDbAdapter.KEY_TITLE);
		int extrasi = c.getColumnIndex(FeedsDbAdapter.KEY_EXTRA);
		while(c.moveToNext()){
			int type = c.getInt(typei);
			String title = c.getString(namei);
			int id = c.getInt(idi);
			String extras = c.getString(extrasi);
			Log.v("Floating Image", "Feed ID: " + id);
			Feed feed = new Feed(title, id, extras);
			data.get(type).add(feed);
		}
		stopManagingCursor(c);
		c.close();
		mDbHelper.close();
		return data;
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		if(mRowList.get(((AdapterContextMenuInfo)menuInfo).position) != null){
			menu.add(0, DELETE_ID, 0, R.string.feedMenuRemove);
		}
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch(item.getItemId()) {
    	case DELETE_ID:
    		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
    		Log.v("Floating Image", "Deleting id: " + info.id);
    		mDbHelper.open();
	        mDbHelper.deleteFeed(mRowList.get(info.position).id);
	        mDbHelper.close();
	        setPreferenceScreen(createPreferenceHierarchy());
	        return true;
		}
		return super.onContextItemSelected(item);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == SELECT_FOLDER && resultCode == RESULT_OK){
			Bundle b = data.getExtras();
			String path = (String)b.get("PATH");
			String name = (String)b.get("NAME");
			int type = b.getInt("TYPE");
			String extras = "";
			if(b.containsKey("EXTRAS")){
				extras = (String)b.get("EXTRAS");
			}
			mDbHelper.open();
			mDbHelper.addFeed(name, path, type, extras);
			mDbHelper.close();
			setPreferenceScreen(createPreferenceHierarchy());
		}
	}
	
	private class AddClickListener implements Preference.OnPreferenceClickListener{
		@Override
		public boolean onPreferenceClick(Preference preference) {
			Intent intent = new Intent(ManageFeeds.this, SourceSelector.class);
			startActivityForResult(intent, SELECT_FOLDER);
			return true;
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
	}
	
	@Override
	protected void onResume() {
		setPreferenceScreen(createPreferenceHierarchy());
		super.onResume();
	}
	
	private class Feed{
		int id;
		String title;
		String extras;
		Feed(String title, int id, String extras){
			this.title = title;
			this.id = id;
			this.extras = extras;
		}
		
	}
}
