package dk.nindroid.rss.parser.picasa;

import android.content.Context;
import dk.nindroid.rss.R;
import dk.nindroid.rss.uiActivities.BlockingTask;

public class SignInTask extends BlockingTask<String, Boolean> {

	public SignInTask(Context context) {
		super(context, R.string.signingIn);
	}

	@Override
	protected Boolean doInBackground(String... params) {
		PicasaFeeder.signIn(mContext);
		return true;
	}
}
