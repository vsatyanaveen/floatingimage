package dk.nindroid.rss.settings;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Stack;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnTouchListener;
import android.widget.ListView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import dk.nindroid.rss.FeedController;
import dk.nindroid.rss.R;
import dk.nindroid.rss.settings.SourceSelector.SourceFragment;

public class DirectoryBrowser extends SourceFragment implements OnTouchListener, SettingsFragment{	
	private static final int SELECT_ID = Menu.FIRST;
	private boolean showHidden = false;
	private int selected;
	private Stack<File> history;
	
	// Used for non-feed related purposes
	public String returnPath;
	public String initialDir;
	
	private List<String> directories;
	File currentDirectory;
	
	public DirectoryBrowser(){
		super(0);
	}
		
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		SharedPreferences sp = getActivity().getSharedPreferences("dk.nindroid.rss_preferences", 0);
		showHidden = sp.getBoolean("folderShowHiddenFiles", false);
		directories = new ArrayList<String>();
		registerForContextMenu(getListView());
		if(initialDir == null){
			currentDirectory = Environment.getExternalStorageDirectory();
		}else{
			currentDirectory = new File(initialDir);
		}
		history = new Stack<File>();
		if(!currentDirectory.exists()){
			currentDirectory = new File("/");
		}
		browseTo(currentDirectory.getAbsolutePath());
		history.add(currentDirectory);
		this.setHasOptionsMenu(true);
		try{
			getActivity().invalidateOptionsMenu();
		}catch (Throwable t){}
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.file_menu, menu);
		//menu.add(0, PARENT_ID, 0, R.string.selectCurrentFolder);
		//menu.add(0, SHOW_HIDDEN, 0, R.string.showHiddenFiles);
	}
	
	public void browseTo(String curDir) {
		Log.v("Floating Image", "Browse to " + curDir);
		File current = new File(curDir);
		String[] files = current.list();
		directories.clear();
		int images = countPictures(files);
		if(files != null){
			for(String file : files){
				File f = new File(curDir + "/" + file);
				if(f.isDirectory() && (!f.getName().startsWith(".") || this.showHidden)){
					directories.add(file);
				}
			}
			Collections.sort(directories, new stringUncaseComparator());
		}
		setListAdapter(new DirectoryAdapter(this, current.getName(), directories, images));
	}
	
	private int countPictures(String[] files){
		int count = 0;
		if(files == null) return 0;
		for(String file : files){
			if(FeedController.isImage(file)){
				++count;
			}
		}
		return count;
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
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		if(position == 0){
			returnResult(currentDirectory.getAbsolutePath());
		}else if(position == 1){
			oneDirUp();
		}else{
			currentDirectory = new File(currentDirectory, directories.get(position - 2));
			history.add(currentDirectory);
			browseTo(currentDirectory.getAbsolutePath());
		}
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		selected = ((AdapterContextMenuInfo)menuInfo).position - 2;
		if(selected >= 0){
			super.onCreateContextMenu(menu, v, menuInfo);
			menu.add(0, SELECT_ID, 0, R.string.selectFolder);
		}
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		String path = directories.get(selected);
		returnResult(currentDirectory.getAbsolutePath() + "/" + path);
		return false;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case R.id.set_current:
			returnResult(currentDirectory.getAbsolutePath());
			getActivity().finish();
			return super.onOptionsItemSelected(item);
		case R.id.show_hidden:
			this.showHidden ^= true;
			SharedPreferences sp = getActivity().getSharedPreferences("dk.nindroid.rss_preferences", 0);
			SharedPreferences.Editor editor = sp.edit();
			editor.putBoolean("folderShowHiddenFiles", this.showHidden);
			editor.commit();
			browseTo(currentDirectory.getAbsolutePath());
			return super.onOptionsItemSelected(item);
		}
		return super.onOptionsItemSelected(item);
	}
	
	void returnResult(String path){
		returnPath = path;
		Intent intent = new Intent();
		Bundle b = new Bundle();
		b.putInt("TYPE", Settings.TYPE_LOCAL);
		b.putString("PATH", path);
		b.putString("NAME", path);
		b.putString("EXTRAS", "");
		intent.putExtras(b);
		getActivity().setResult(Activity.RESULT_OK, intent);		
		getActivity().finish();
	}
	/*
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
*/
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		
		return false;
	}

	@Override
	public boolean back() {
		if(history.size() > 1){
			history.pop();
			currentDirectory = history.peek();
			browseTo(currentDirectory.getAbsolutePath());
			return true;
		}
		return false;
	}
}

class stringUncaseComparator implements Comparator<String>{

	@Override
	public int compare(String object1, String object2) {
		return object1.compareToIgnoreCase(object2);
	}
	
}
