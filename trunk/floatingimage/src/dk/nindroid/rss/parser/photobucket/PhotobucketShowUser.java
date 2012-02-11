package dk.nindroid.rss.parser.photobucket;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import dk.nindroid.rss.R;
import dk.nindroid.rss.settings.Settings;
import dk.nindroid.rss.settings.SourceSelector;

public class PhotobucketShowUser extends SourceSelector.SourceFragment {
	public PhotobucketShowUser() {
		super(5);
	}

	public final static String USER = "user";
	
	private final static int OPTIONS = 2;
	private final static int RECENT = 0;
	private final static int FOLLOWING = 1;
	
	List<String> albums;
	String mUser;
	
	public static PhotobucketShowUser getInstance(String user){
		PhotobucketShowUser fab = new PhotobucketShowUser();
		Bundle args = new Bundle();
        args.putString(USER, user);
        fab.setArguments(args);
        
        return fab;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		fillMenu();
		super.onActivityCreated(savedInstanceState);
	}
	
	void fillMenu(){
		Bundle b = getArguments();
		if(b != null){
			mUser = b.getString(USER);
			getUserAlbums(mUser);
		}
	}
	
	void getUserAlbums(String user){
		new LoadAlbumsTask(getActivity(), PhotobucketFeeder.getAlbums(mUser), this).execute();
	}
	
	void albumsLoaded(List<String> albums){
		if(albums == null){
			Toast.makeText(getActivity(), R.string.photobucketShowUserBadUsername, Toast.LENGTH_LONG).show();
			getActivity().setResult(Activity.RESULT_CANCELED);
			getActivity().finish();
			return;
		}else{
			this.albums = albums;
			
			String recent = getString(R.string.photobucketShowRecent);
			String following = getString(R.string.photobucketShowFollowing);
			String[] options = new String[albums.size() + OPTIONS];
			options[RECENT] = recent;
			options[FOLLOWING] = following;
			for(int i = 0; i < albums.size(); ++i){
				options[i + OPTIONS] = albums.get(i);
			}
			setListAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, options));
		}
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		if(position == 0){
			returnUrl(PhotobucketFeeder.getRecent(this.mUser), getString(R.string.photobucketRecent), getString(R.string.photobucketImagesBy, mUser));
		}else if(position == 1){
			returnUrl(PhotobucketFeeder.getFollowing(this.mUser), getString(R.string.photobucketFollowing), mUser);
		}else{
			position -= 2;
			returnUrl(PhotobucketFeeder.getAlbum(this.mUser, this.albums.get(position)), this.albums.get(position), getString(R.string.photobucketAlbumBy, mUser));
		}
	}
	
	void returnUrl(String url, String title, String extras){
		Intent intent = new Intent();
		Bundle b = new Bundle();
		b.putString("PATH", url);
		b.putString("NAME", title);
		b.putString("EXTRAS", extras);
		b.putInt("TYPE", Settings.TYPE_PHOTOBUCKET);
		intent.putExtras(b);
		this.getActivity().setResult(Activity.RESULT_OK, intent);		
		this.getActivity().finish();
	}

	@Override
	public boolean back() {
		return false;
	}
}
