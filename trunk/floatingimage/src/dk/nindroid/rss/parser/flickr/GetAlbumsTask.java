package dk.nindroid.rss.parser.flickr;

import java.util.List;

import android.content.Context;

import dk.nindroid.rss.R;
import dk.nindroid.rss.flickr.FlickrFeeder;
import dk.nindroid.rss.uiActivities.BlockingTask;

public class GetAlbumsTask extends BlockingTask<String, List<FlickrAlbum>> {
	Callback mCallback;
	
	public GetAlbumsTask(Context context, Callback callback) {
		super(context, R.string.loadingAlbums);
		this.mCallback = callback;
	}

	@Override
	protected List<FlickrAlbum> doInBackground(String... params) {
		String owner = params[0];
		List<FlickrAlbum> res = FlickrFeeder.getAlbums(owner);
		if(res == null){
			super.setError(R.string.error_fetching_albums);
		}
		return res;
	}
	
	@Override
	protected void onPostExecute(List<FlickrAlbum> result) {
		super.onPostExecute(result);
		mCallback.albumsFetched(result);
	}

	public interface Callback{
		void albumsFetched(List<FlickrAlbum> param);
	}
}
