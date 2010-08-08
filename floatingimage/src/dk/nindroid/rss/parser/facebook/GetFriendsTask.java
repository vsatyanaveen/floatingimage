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
import dk.nindroid.rss.parser.facebook.FacebookFriendsBrowser.Friend;
import dk.nindroid.rss.uiActivities.BlockingTask;

public class GetFriendsTask extends BlockingTask<String, List<Friend>> {
	Callback callback;
	
	public GetFriendsTask(Context context, Callback callback) {
		super(context, R.string.loadingFriends);
		this.callback = callback;
	}

	@Override
	protected List<Friend> doInBackground(String... params) {
		String url = null;
		try {
			url = FacebookFeeder.getMyFriendsUrl();
		} catch (MalformedURLException e) {
			Log.e("Floating Image", "Internal error. URL incorrect!", e);
			super.setError(R.string.internal_error);
		} catch (IOException e) {
			Log.w("Floating Image", "Could not get url for the photos of me.", e);
			super.setError(R.string.error_fetching_friends);
		}
		if(url == null){
			return null;
		}else{
			try {
				
				String content = DownloadUtil.readStreamToEnd(url);
				JSONObject json = new JSONObject(content);
				
				JSONArray data = json.getJSONArray(FacebookTags.DATA);
				List<Friend> albums = new ArrayList<Friend>(data.length());
				for(int i = 0; i < data.length(); ++i){
					JSONObject obj = data.getJSONObject(i);
					String name = obj.getString(FacebookTags.NAME);
					String id = obj.getString(FacebookTags.ID);
					albums.add(new Friend(name, id));
				}
				return albums;
			} catch (JSONException e) {
				Log.w("Floating Image", "Error reading facebook stream", e);
				super.setError(R.string.error_fetching_friends);
			} catch (IOException e) {
				Log.w("Floaing Image", "Error getting albums", e);
				super.setError(R.string.error_fetching_friends);
			}
		}
		return null;
	}
	
	@Override
	protected void onPostExecute(List<Friend> result) {
		super.onPostExecute(result);
		callback.friendsFetched(result);
	}

	public interface Callback {
		void friendsFetched(List<Friend> param);
	}
}
