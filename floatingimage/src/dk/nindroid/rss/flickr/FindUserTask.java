package dk.nindroid.rss.flickr;

import android.content.Context;
import dk.nindroid.rss.R;
import dk.nindroid.rss.uiActivities.BlockingTask;

public class FindUserTask extends BlockingTask<String, String> {
	Callback callback;
	String username;
	
	public FindUserTask(Context context, Callback callback) {
		super(context, R.string.searching_for_user);
		this.callback = callback;
	}

	@Override
	protected String doInBackground(String... params) {
		this.username = params[0];
		return FlickrFeeder.findByUsername(params[0]);
	}

	@Override
	protected void onPostExecute(String result) {
		super.onPostExecute(result);
		callback.findUserCallback(username, result);
	}
	
	public interface Callback{
		void findUserCallback(String username, String uid);
	}
}
