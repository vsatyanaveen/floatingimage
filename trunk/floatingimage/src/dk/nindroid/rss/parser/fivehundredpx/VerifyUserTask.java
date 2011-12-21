package dk.nindroid.rss.parser.fivehundredpx;

import java.io.IOException;

import android.content.Context;
import dk.nindroid.rss.HttpTools;
import dk.nindroid.rss.R;
import dk.nindroid.rss.uiActivities.BlockingTask;

public class VerifyUserTask extends BlockingTask<String, Boolean>{
	Callback callback;
	String url;
	String title;
	String extras;
	
	public VerifyUserTask(Context context, Callback callback, String url, String title, String extras) {
		super(context, R.string.fivehundredpxVerifyingUser);
		this.callback = callback;
		this.url = url;
		this.title = title;
		this.extras = extras;
	}

	@Override
	protected Boolean doInBackground(String... params) {
		String url = FiveHundredPxFeeder.getUserProfile(params[0]);
		try {
			return HttpTools.openHttpConnection(url) != null;
		} catch (IOException e) {
			return false;
		}
	}
	
	@Override
	protected void onPostExecute(Boolean result) {
		callback.userVerified(url, result, title, extras);
		super.onPostExecute(result);
	}
	
	public interface Callback{
		void userVerified(String url, boolean isVerified, String title, String extras);
	}
}
