package dk.nindroid.rss.parser.photobucket;

import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import dk.nindroid.rss.R;

public class PhotobucketShowUser extends ListActivity {
	public final static String USER = "user";
	
	private final static int OPTIONS = 2;
	private final static int RECENT = 0;
	private final static int FOLLOWING = 1;
	
	List<PhotobucketAlbum> albums;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		String user = this.getIntent().getStringExtra(USER);
		boolean exists = getUserAlbums(user);
		if(exists){
			String recent = getString(R.string.photobucketShowRecent);
			String following = getString(R.string.photobucketShowFollowing);
			String[] options = new String[albums.size() + OPTIONS];
			options[RECENT] = recent;
			options[FOLLOWING] = following;
			for(int i = 0; i < albums.size(); ++i){
				options[i + OPTIONS] = albums.get(i).getName();
			}
			setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, options));
		}else{
			Toast.makeText(this, R.string.photobucketShowUserBadUsername, Toast.LENGTH_LONG).show();
			setResult(RESULT_CANCELED);
			finish();
		}
	}
	
	boolean getUserAlbums(String user){
		albums = new ArrayList<PhotobucketAlbum>();
		albums.add(new PhotobucketAlbum("test1", ""));
		albums.add(new PhotobucketAlbum("test2", ""));
		albums.add(new PhotobucketAlbum("test3", ""));
		return true;
	}
}
