package dk.nindroid.rss.parser.facebook;

import java.util.List;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class FacebookAlbumBrowser extends ListActivity implements GetAlbumsTask.Callback{
	private List<Album> albums = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		fillMenu();
	}
	
	private void fillMenu(){
		getAlbums();
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Intent intent = new Intent();
		Bundle b = new Bundle();
		String url;
		url = FacebookFeeder.getPhotos(albums.get(position).getId());
		b.putString("PATH", url);
		b.putString("NAME", albums.get(position).getName());
		intent.putExtras(b);
		setResult(RESULT_OK, intent);
		finish();
	}
	
	private void getAlbums(){
		new GetAlbumsTask(this, this).execute(this.getIntent().getExtras().getString("ID"));
	}
	
	static class Album{
		private String name;
		private String id;
		
		Album(String name, String id){
			this.name = name;
			this.id = id;
		}
		
		public String getName(){
			return name;
		}
		
		public String getId(){
			return id;
		}
	}

	@Override
	public void albumsFetched(List<Album> param) {
		if(param == null) {
			finish();
			return;
		}
		albums = param;
		
		String[] options = new String[albums.size()];
		for(int i = 0; i < albums.size(); ++i){
			options[i] = albums.get(i).name;
		}
		setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, options));
	}
}
