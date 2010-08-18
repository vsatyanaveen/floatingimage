package dk.nindroid.rss.settings;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Stack;

import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnTouchListener;
import android.widget.ListView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import dk.nindroid.rss.R;

public class DirectoryBrowser extends ListActivity implements OnTouchListener{
	public static final int SHOW_HIDDEN = Menu.FIRST;
	public static final int PARENT_ID = Menu.FIRST + 1;
	public static final int SELECT_ID = Menu.FIRST;
	public static final String ID = "local";
	private boolean showHidden = false;
	private int selected;
	private Stack<File> history;
	
	private List<String> directories;
	File currentDirectory;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SharedPreferences sp = getSharedPreferences("dk.nindroid.rss_preferences", 0);
		showHidden = sp.getBoolean("folderShowHiddenFiles", false);
		directories = new ArrayList<String>();
		registerForContextMenu(getListView());
		currentDirectory = Environment.getExternalStorageDirectory();
		history = new Stack<File>();
		if(!currentDirectory.exists()){
			currentDirectory = new File("/");
		}
		browseTo(currentDirectory.getAbsolutePath());
		history.add(currentDirectory);
	}

	private void browseTo(String curDir) {
		Log.v("Floating Image", "Browse to " + curDir);
		File current = new File(curDir);
		String[] files = current.list();
		directories.clear();
		directories.add("..");
		if(files != null){
			for(String file : files){
				File f = new File(curDir + "/" + file);
				if(f.isDirectory() && (!f.getName().startsWith(".") || this.showHidden)){
					directories.add(file);
				}
			}
			Collections.sort(directories, new stringUncaseComparator());
		}
		setListAdapter(new DirectoryAdapter(this, directories));
	}
	
	private void oneDirUp(){
		String path = currentDirectory.getAbsolutePath();
		if(!path.equals("/")){
			int lastDir = path.lastIndexOf('/');
			currentDirectory = new File(path.substring(0, lastDir));
			history.add(currentDirectory);
			browseTo(currentDirectory.getAbsolutePath());
		}
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		if(position == 0){
			oneDirUp();
		}else{
			currentDirectory = new File(currentDirectory, directories.get(position));
			history.add(currentDirectory);
			browseTo(currentDirectory.getAbsolutePath());
		}
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
		menu.add(0, PARENT_ID, 0, R.string.selectCurrentFolder);
		menu.add(0, SHOW_HIDDEN, 0, R.string.showHiddenFiles);
		return res;
	}
	

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case PARENT_ID:
			returnResult(currentDirectory.getAbsolutePath());
			finish();
			return super.onOptionsItemSelected(item);
		case SHOW_HIDDEN:
			this.showHidden ^= true;
			SharedPreferences sp = getSharedPreferences("dk.nindroid.rss_preferences", 0);
			SharedPreferences.Editor editor = sp.edit();
			editor.putBoolean("folderShowHiddenFiles", this.showHidden);
			editor.commit();
			browseTo(currentDirectory.getAbsolutePath());
			return super.onOptionsItemSelected(item);
		}
		return super.onOptionsItemSelected(item);
	}
	
	void returnResult(String path){
		Intent intent = new Intent();
		Bundle b = new Bundle();
		b.putString("PATH", path);
		b.putString("NAME", path);
		b.putString("EXTRAS", "");
		intent.putExtras(b);
		setResult(RESULT_OK, intent);		
		finish();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch(keyCode){
		case KeyEvent.KEYCODE_BACK:
			if(history.size() > 1){
				history.pop();
				currentDirectory = history.peek();
				browseTo(currentDirectory.getAbsolutePath());
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		
		return false;
	}
}

class stringUncaseComparator implements Comparator<String>{

	@Override
	public int compare(String object1, String object2) {
		return object1.compareToIgnoreCase(object2);
	}
	
}
