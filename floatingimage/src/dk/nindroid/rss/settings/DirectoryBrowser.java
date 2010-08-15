package dk.nindroid.rss.settings;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Stack;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
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
	public static final int PARENT_ID = Menu.FIRST;
	public static final int SELECT_ID = Menu.FIRST;
	public static final String ID = "local"; 
	private int selected;
	private Stack<File> history;
	
	private List<String> directories;
	File currentDirectory;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		directories = new ArrayList<String>();
		registerForContextMenu(getListView());
		currentDirectory = new File("/sdcard");
		history = new Stack<File>();
		if(currentDirectory.exists()){
			browseTo("/sdcard");
		}else{
			currentDirectory = new File("/");
			browseTo("/");
		}
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
				if(f.isDirectory() && !f.getName().startsWith(".")){
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
		menu.add(0, SELECT_ID, 0, R.string.selectCurrentFolder);
		return res;
	}
	

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		returnResult(currentDirectory.getAbsolutePath());
		finish();
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
