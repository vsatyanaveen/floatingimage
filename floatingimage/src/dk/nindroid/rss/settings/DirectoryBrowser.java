package dk.nindroid.rss.settings;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import dk.nindroid.rss.R;

public class DirectoryBrowser extends ListActivity {
	public static final int PARENT_ID = Menu.FIRST;
	public static final int SELECT_ID = Menu.FIRST;
	public static final String ID = "local"; 
	private int selected;
	
	private List<String> directories;
	File currentDirectory;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		directories = new ArrayList<String>();
		registerForContextMenu(getListView());
		currentDirectory = new File("/sdcard");
		if(currentDirectory.exists()){
			browseTo("/sdcard");
		}else{
			currentDirectory = new File("/");
			browseTo("/");
		}
	}

	private void browseTo(String curDir) {
		File current = new File(curDir);
		String[] files = current.list();
		directories.clear();
		directories.add("..");
		for(String file : files){
			File f = new File(curDir + "/" + file);
			if(f.isDirectory()){
				directories.add(file);
			}
		}
		Collections.sort(directories, new stringUncaseComparator());
		setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, directories));
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		currentDirectory = new File(currentDirectory, directories.get(position));
		browseTo(currentDirectory.getAbsolutePath());
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		selected = ((AdapterContextMenuInfo)menuInfo).position;
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.add(0, SELECT_ID, 0, R.string.selectFolder);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		String path = directories.get(selected);
		returnResult(currentDirectory.getAbsolutePath() + "/" + path);
		return false;
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean res = super.onCreateOptionsMenu(menu);
		menu.add(0, SELECT_ID, 0, R.string.selectCurrentFolder);
		return res;
	}
	

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		returnResult(currentDirectory.getAbsolutePath());
		finish();
		return super.onOptionsItemSelected(item);
	}
	
	private void returnResult(String path){
		Intent intent = new Intent();
		Bundle b = new Bundle();
		b.putString("PATH", path);
		b.putString("NAME", path);
		intent.putExtras(b);
		setResult(RESULT_OK, intent);		
		finish();
	}
}

class stringUncaseComparator implements Comparator<String>{

	@Override
	public int compare(String object1, String object2) {
		return object1.compareToIgnoreCase(object2);
	}
	
}
