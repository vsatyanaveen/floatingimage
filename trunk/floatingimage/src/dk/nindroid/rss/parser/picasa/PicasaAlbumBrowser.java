package dk.nindroid.rss.parser.picasa;

import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import dk.nindroid.rss.R;
import dk.nindroid.rss.settings.PicasaBrowser;
import dk.nindroid.rss.settings.Settings;
import dk.nindroid.rss.settings.SettingsFragment;

public class PicasaAlbumBrowser extends ListFragment implements GetAlbumsTask.Callback, SettingsFragment{
	public final static String OWNER = "OWNER";
	List<PicasaAlbum> albums;
	String owner;
	boolean error = false;
	
	public static PicasaAlbumBrowser getInstance(String owner){
		PicasaAlbumBrowser pab = new PicasaAlbumBrowser();
		
		Bundle b = new Bundle();
		b.putString(OWNER, owner);
		pab.setArguments(b);
		return pab;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		owner = getArguments().getString(OWNER);
		fillMenu();
	}
	
	private void fillMenu(){
		new GetAlbumsTask(this.getActivity(), this).execute(owner);
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		if(error){
			error = false;
			fillMenu();
		}else{
			String url = PicasaFeeder.getAlbum(owner, albums.get(position).getId());
			Intent intent = new Intent();
			Bundle b = new Bundle();
			b.putString("PATH", url);
			b.putInt("TYPE", Settings.TYPE_PICASA);
			b.putString("NAME", albums.get(position).getTitle());
			b.putString("EXTRAS", getString(R.string.albumBy) + " " + owner);
			intent.putExtras(b);
			this.getActivity().setResult(Activity.RESULT_OK, intent);
			this.getActivity().finish();
		}
	}

	@Override
	public void albumsFetched(List<PicasaAlbum> param) {
		if(param == null){
			this.getActivity().finish();
			return;
		}
		albums = param;
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
		setListAdapter(new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_list_item_1, albumStrings));
	}

	@Override
	public boolean back() {
		FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.source, new PicasaBrowser(), "content");
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.commit();
        return true;
	}
}
