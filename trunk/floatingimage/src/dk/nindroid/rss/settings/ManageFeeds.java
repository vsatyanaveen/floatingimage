package dk.nindroid.rss.settings;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.SimpleCursorAdapter;
import android.widget.AdapterView.AdapterContextMenuInfo;
import dk.nindroid.rss.R;

public class ManageFeeds extends ListActivity {
	public static final int ADD_ID = Menu.FIRST;
	public static final int CLEAR_ALL_ID = Menu.FIRST + 1;
    private static final int DELETE_ID = Menu.FIRST + 1;
    private static final int SELECT_FOLDER = 12;
	private FeedsDbAdapter mDbHelper;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.manage_feeds);
		mDbHelper = new FeedsDbAdapter(this);
		//mDbHelper.open();
		//fillData();
		registerForContextMenu(getListView());
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean res = super.onCreateOptionsMenu(menu);
		menu.add(0, ADD_ID, 0, R.string.feedMenuAdd);
		menu.add(0, CLEAR_ALL_ID, 0, R.string.feedMenuClearAll);
		return res;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case ADD_ID:
			addFeed();
			return true;
		case CLEAR_ALL_ID:
			clearAll();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	void addFeed(){
		startActivityForResult(new Intent(this, DirectoryBrowser.class), SELECT_FOLDER);
		fillData();
	}
	
	void clearAll(){
		mDbHelper.deleteAll();
		fillData();
	}

	private void fillData() {
		Cursor c = mDbHelper.fetchAllFeeds();
		startManagingCursor(c);
		String[] from = new String[] {FeedsDbAdapter.KEY_TITLE, FeedsDbAdapter.KEY_URI};
		int[] to = new int[]{R.id.feedRowTitle, R.id.feedRowURI};
		SimpleCursorAdapter feeds = new SimpleCursorAdapter(this, R.layout.feeds_row, c, from, to);
		setListAdapter(feeds);
		stopManagingCursor(c);
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, DELETE_ID, 0, R.string.feedMenuRemove);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch(item.getItemId()) {
    	case DELETE_ID:
    		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
	        mDbHelper.deleteFeed(info.id);
	        fillData();
	        return true;
		}
		return super.onContextItemSelected(item);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == SELECT_FOLDER && resultCode == RESULT_OK){
			Bundle b = data.getExtras();
			mDbHelper.open();	
			mDbHelper.addFeed(DirectoryBrowser.ID, (String)b.get("PATH"));
			fillData();
			mDbHelper.close();
		}
	}
	
	
	@Override
	protected void onPause() {
		mDbHelper.close();
		super.onPause();
	}
	
	@Override
	protected void onResume() {
		mDbHelper.open();
		fillData();
		super.onResume();
	}
}
