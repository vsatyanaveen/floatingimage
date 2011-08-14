package dk.nindroid.rss.parser.flickr;

import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import dk.nindroid.rss.R;
import dk.nindroid.rss.flickr.FlickrFeeder;
import dk.nindroid.rss.settings.FlickrBrowser;
import dk.nindroid.rss.settings.Settings;
import dk.nindroid.rss.settings.SettingsFragment;

public class FlickrAlbumBrowser extends ListFragment implements GetAlbumsTask.Callback, SettingsFragment {
	public final static String OWNER = "OWNER";
	List<FlickrAlbum> albums;
	
	public static FlickrAlbumBrowser getInstance(String owner){
		FlickrAlbumBrowser fab = new FlickrAlbumBrowser();
		Bundle args = new Bundle();
        args.putString(OWNER, owner);
        fab.setArguments(args);
        
        return fab;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	
		fillMenu();
	}
	
	private void fillMenu(){
		Bundle b = getArguments();
		String owner = null;
		if(b != null){
			owner = b.getString(OWNER);
		}
		new GetAlbumsTask(this.getActivity(), this).execute(owner);
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		String url = FlickrFeeder.getAlbumPhotos(albums.get(position).getId());
		Intent intent = new Intent();
		Bundle b = new Bundle();
		b.putString("PATH", url);
		b.putString("NAME", albums.get(position).getName());
		b.putInt("TYPE", Settings.TYPE_FLICKR);
		intent.putExtras(b);
		getActivity().setResult(Activity.RESULT_OK, intent);
		getActivity().finish();
	}

	@Override
	public void albumsFetched(List<FlickrAlbum> param) {
		if(param == null){
			getActivity().finish();
			return;
		}
		albums = param;
		Collections.sort(albums);
		String[] albumStrings = new String[albums.size()];
		for(int i = 0; i < albums.size(); ++i){
			albumStrings[i] = albums.get(i).getName();
		}
		try{
			setListAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, albumStrings));
		}catch (Exception e){
			Log.w("Floating Image", "Could not set list (did the user leave early?", e);
		}
	}

	@Override
	public boolean back() {
		FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.source, new FlickrBrowser(), "content");
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.commit();
        return true;
	}
}
