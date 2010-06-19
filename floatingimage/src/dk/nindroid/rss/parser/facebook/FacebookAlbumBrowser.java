package dk.nindroid.rss.parser.facebook;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import dk.nindroid.rss.DownloadUtil;

public class FacebookAlbumBrowser extends ListActivity {
	private List<Album> albums = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		fillMenu();
	}
	
	private void fillMenu(){
		albums = getAlbums();
		if(albums == null) return;
		
		String[] options = new String[albums.size()];
		for(int i = 0; i < albums.size(); ++i){
			options[i] = albums.get(i).name;
		}
		setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, options));
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
	
	private List<Album> getAlbums(){
		String url = null;
		String friendID = getIntent().getExtras().getString("ID");
		try {
			url = FacebookFeeder.getAlbumsUrl(friendID);
		} catch (MalformedURLException e) {
			Log.e("Floating Image", "Internal error. URL incorrect!", e);
			Toast.makeText(this, "Internal error detected. Please contact developer!", 2).show();
		} catch (IOException e) {
			Log.w("Floating Image", "Could not get url for the photos of me.", e);
			Toast.makeText(this, "Error getting stream. Please try again.", 2).show();
		}
		if(url == null){
			finish();
			return null;
		}else{
			try {
				
				String content = DownloadUtil.readStreamToEnd(url);
				JSONObject json = new JSONObject(content);
				
				JSONArray data = json.getJSONArray(FacebookTags.DATA);
				List<Album> albums = new ArrayList<Album>(data.length());
				for(int i = 0; i < data.length(); ++i){
					JSONObject obj = data.getJSONObject(i);
					String name = obj.getString(FacebookTags.NAME);
					String id = obj.getString(FacebookTags.ID);
					albums.add(new Album(name, id));
				}
				
				return albums;
			} catch (JSONException e) {
				Log.w("Floating Image", "Error reading facebook stream", e);
				Toast.makeText(this, "Error reading stream.", 2).show();
			} catch (IOException e) {
				Log.w("Floaing Image", "Error getting albums", e);
				Toast.makeText(this, "Error getting stream. Please try again.", 2).show();
			}
		}
		finish();
		return null;
	}
	
	private class Album{
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
}
