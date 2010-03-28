package dk.nindroid.rss.settings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.SimpleAdapter;
import android.widget.AdapterView.AdapterContextMenuInfo;
import dk.nindroid.rss.R;

public class ManageFeeds extends ListActivity {
	public static final int ADD_ID = Menu.FIRST;
	public static final int CLEAR_ALL_ID = Menu.FIRST + 1;
    private static final int DELETE_ID = Menu.FIRST + 1;
    private static final int SELECT_FOLDER = 12;
	private FeedsDbAdapter 	mDbHelper;
	private List<Integer> 	mRowList = new ArrayList<Integer>();
	
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
		startActivityForResult(new Intent(this, SourceSelector.class), SELECT_FOLDER);
		fillData();
	}
	
	void clearAll(){
		mDbHelper.deleteAll();
		fillData();
	}

	private void fillData() {
		List<Map<String, String>> data = getData();
		
		String[] from = new String[] {"name", "type"}; 
		int[] to = new int[]{R.id.feedRowTitle, R.id.feedRowURI};
		
		SimpleAdapter adapter = new SimpleAdapter(this, data, R.layout.feeds_row, from, to);
		
		setListAdapter(adapter);
		
		
	}
	
	private List<Map<String, String>> getData(){
		Cursor c = mDbHelper.fetchAllFeeds();
		startManagingCursor(c);
		mRowList.clear();
		List<Map<String, String>> data = new ArrayList<Map<String, String>>();
		while(c.moveToNext()){
			Map<String, String> item = new HashMap<String, String>();
			int type = c.getInt(3);
			String desc = Settings.typeToString(type);
			String name = c.getString(1);
			item.put("name", name);
			item.put("type", desc);
			mRowList.add(c.getInt(0));
			data.add(item);
		}
		stopManagingCursor(c);
		return data;
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
	        mDbHelper.deleteFeed(mRowList.get((int)info.id));
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
			String path = (String)b.get("PATH");
			String name = (String)b.get("NAME");
			int type = b.getInt("TYPE");
			mDbHelper.addFeed(name, path, type);
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
