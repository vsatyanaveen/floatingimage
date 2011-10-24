package dk.nindroid.rss.parser.facebook;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;
import dk.nindroid.rss.DownloadUtil;
import dk.nindroid.rss.R;
import dk.nindroid.rss.parser.facebook.FacebookAlbumBrowser.Album;
import dk.nindroid.rss.uiActivities.BlockingTask;

public class GetAlbumsTask extends BlockingTask<String, List<FacebookAlbumBrowser.Album>> {
	Callback callback;
	
	public GetAlbumsTask(Context context, Callback callback) {
		super(context, R.string.loadingAlbums);
		this.callback = callback;
	}

	@Override
	protected List<Album> doInBackground(String... params) {
		String url = null;
		String friendID = params[0];
		try {
			url = FacebookFeeder.getAlbumsUrl(friendID);
		} catch (MalformedURLException e) {
			Log.e("Floating Image", "Internal error. URL incorrect!", e);
			super.setError(R.string.internal_error);
		} catch (IOException e) {
			Log.w("Floating Image", "Could not get album url.", e);
			super.setError(R.string.error_fetching_albums);
		}
		if(url == null){
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
				super.setError(R.string.error_fetching_albums);
			} catch (IOException e) {
				Log.w("Floating Image", "Error getting albums", e);
				super.setError(R.string.error_fetching_albums);
			}
		}
		return null;
	}
	
	@Override
	protected void onPostExecute(List<Album> result) {
		super.onPostExecute(result);
		callback.albumsFetched(result);
	}
	
	public interface Callback {
		void albumsFetched(List<Album> param);
	}
}
