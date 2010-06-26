package dk.nindroid.rss.parser.flickr;

import java.util.Collections;
import java.util.List;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import dk.nindroid.rss.flickr.FlickrFeeder;

public class FlickrAlbumBrowser extends ListActivity {
	public final static String OWNER = "OWNER";
	List<FlickrAlbum> albums;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		fillMenu();
	}
	
	private void fillMenu(){
		String owner = getIntent().getStringExtra(OWNER);
		albums = FlickrFeeder.getAlbums(owner);
		Collections.sort(albums);
		String[] albumStrings = new String[albums.size()];
		for(int i = 0; i < albums.size(); ++i){
			albumStrings[i] = albums.get(i).getName();
		}
		setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, albumStrings));
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		String url = FlickrFeeder.getAlbumPhotos(albums.get(position).getId());
		Intent intent = new Intent();
		Bundle b = new Bundle();
		b.putString("PATH", url);
		b.putString("NAME", albums.get(position).getName());
		intent.putExtras(b);
		setResult(RESULT_OK, intent);
		finish();
	}
}
