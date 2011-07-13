package dk.nindroid.rss.parser.facebook;

import java.util.List;

import dk.nindroid.rss.R;
import dk.nindroid.rss.settings.Settings;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class FacebookAlbumBrowser extends ListFragment implements GetAlbumsTask.Callback{
	private List<Album> albums = null;
	
	public static FacebookAlbumBrowser getInstance(String id, String name){
		FacebookAlbumBrowser fab = new FacebookAlbumBrowser();
		Bundle args = new Bundle();
        args.putString("ID", id);
        args.putString("Name", name);
        fab.setArguments(args);
        
        return fab;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
			
		fillMenu();
	}
	
	private void fillMenu(){
		getAlbums();
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Intent intent = new Intent();
		Bundle b = new Bundle();
		String url;
		url = FacebookFeeder.getPhotos(albums.get(position).getId());
		b.putString("PATH", url);
		b.putInt("TYPE", Settings.TYPE_FACEBOOK);
		b.putString("NAME", albums.get(position).getName());
		String name = this.getArguments().getString("Name");
		if(name == null){
			name = getString(R.string.me);
		}
		b.putString("EXTRAS", getString(R.string.albumBy) + " " + name);
		intent.putExtras(b);
		this.getActivity().setResult(Activity.RESULT_OK, intent);
		this.getActivity().finish();
	}
	
	public void getAlbums(){
		new GetAlbumsTask(this.getActivity(), this).execute(this.getArguments().getString("ID"));
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
			this.getActivity().finish();
			return;
		}
		albums = param;
		
		String[] options = new String[albums.size()];
		for(int i = 0; i < albums.size(); ++i){
			options[i] = albums.get(i).name;
		}
		setListAdapter(new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_list_item_1, options));
	}
}
