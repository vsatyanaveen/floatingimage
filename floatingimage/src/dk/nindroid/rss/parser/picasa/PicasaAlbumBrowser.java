package dk.nindroid.rss.parser.picasa;

import java.util.Collections;
import java.util.List;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import dk.nindroid.rss.R;

public class PicasaAlbumBrowser extends ListActivity {
	public final static String OWNER = "OWNER";
	List<PicasaAlbum> albums;
	String owner;
	boolean error = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		owner = getIntent().getStringExtra(OWNER);
		fillMenu();
	}
	
	private void fillMenu(){
		albums = PicasaFeeder.getAlbums(owner, this);
		String[] albumStrings = null;
		if(albums == null){
			albumStrings = new String[]{getString(R.string.error_showing_fetching_album_list)};
			error = true;
		}else{
			Collections.sort(albums);
			albumStrings = new String[albums.size()];
			for(int i = 0; i < albums.size(); ++i){
				albumStrings[i] = albums.get(i).getTitle();
			}
		}
		setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, albumStrings));
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		if(error){
			error = false;
			fillMenu();
		}else{
			String url = PicasaFeeder.getAlbum(owner, albums.get(position).getId());
			Intent intent = new Intent();
			Bundle b = new Bundle();
			b.putString("PATH", url);
			b.putString("NAME", albums.get(position).getTitle());
			intent.putExtras(b);
			setResult(RESULT_OK, intent);
			finish();
		}
	}
}
