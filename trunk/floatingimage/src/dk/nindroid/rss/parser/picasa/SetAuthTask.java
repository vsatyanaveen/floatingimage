package dk.nindroid.rss.parser.picasa;

import dk.nindroid.rss.R;
import dk.nindroid.rss.uiActivities.BlockingTask;

public class SetAuthTask extends BlockingTask<Boolean, Boolean> {
	String verifier;
	String token;
	PicasaWebAuth activity;
	
	public SetAuthTask(PicasaWebAuth activity, String verifier, String token) {
		super(activity, R.string.signingIn);
		this.verifier = verifier;
		this.token = token;
		this.activity = activity;
	}

	@Override
	protected Boolean doInBackground(Boolean... params) {
		PicasaFeeder.setAuth(verifier, token, mContext);
		return true;
	}

	@Override
	protected void onPostExecute(Boolean result) {
		super.onPostExecute(result);
		activity.finish();
	}
}
