package dk.nindroid.rss.parser.picasa;

import java.util.List;

import android.content.Context;

import dk.nindroid.rss.R;
import dk.nindroid.rss.uiActivities.BlockingTask;

public class GetAlbumsTask extends BlockingTask<String, List<PicasaAlbum>> {
	Callback mCallback;
	
	public GetAlbumsTask(Context context, Callback callback) {
		super(context, R.string.loadingAlbums);
		this.mCallback = callback;
	}

	@Override
	protected List<PicasaAlbum> doInBackground(String... params) {
		String owner = params[0];
		List<PicasaAlbum> albums = PicasaFeeder.getAlbums(owner, mContext);
		if(albums == null){
			super.setError(R.string.error_fetching_albums);
		}
		return albums;
	}
	
	@Override
	protected void onPostExecute(List<PicasaAlbum> result) {
		super.onPostExecute(result);
		mCallback.albumsFetched(result);
	}

	public interface Callback{
		void albumsFetched(List<PicasaAlbum> param);
	}
}
