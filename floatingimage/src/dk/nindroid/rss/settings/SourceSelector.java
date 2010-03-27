package dk.nindroid.rss.settings;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import dk.nindroid.rss.R;

public class SourceSelector extends ListActivity {
	public static final int		LOCAL = Menu.FIRST;
	public static final int		FLICKR = Menu.FIRST + 1;
	public static final int 	LOCAL_ACTIVITY = 13;
	public static final int 	FLICKR_ACTIVITY = 14;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		fillMenu();
	}
	
	protected void fillMenu(){
		String local = this.getResources().getString(R.string.local);
		String flickr = this.getResources().getString(R.string.flickr);
		String[] options = new String[] {local, flickr};
		setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, options));
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {	
		super.onListItemClick(l, v, position, id);
		switch(position){
		case 0: // Local
			Intent showFolder = new Intent(this, DirectoryBrowser.class);
			startActivityForResult(showFolder, LOCAL_ACTIVITY);
			break;
		case 1: // Flickr
			Intent showFlickr = new Intent(this, FlickrBrowser.class);
			startActivityForResult(showFlickr, FLICKR_ACTIVITY);
			break;
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == RESULT_OK){
			Intent intent = new Intent();
			Bundle b = data.getExtras();
			switch(requestCode){
			case LOCAL_ACTIVITY:
				b.putInt("TYPE", Settings.TYPE_LOCAL);
				break;
			case FLICKR_ACTIVITY:
				b.putInt("TYPE", Settings.TYPE_FLICKR);
				break;
			}
			intent.putExtras(b);
			setResult(RESULT_OK, intent);
			finish();
		}
	}
}
