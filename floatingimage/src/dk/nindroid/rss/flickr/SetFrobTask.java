package dk.nindroid.rss.flickr;

import java.io.IOException;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import dk.nindroid.rss.DownloadUtil;
import dk.nindroid.rss.R;
import dk.nindroid.rss.uiActivities.BlockingTask;

public class SetFrobTask extends BlockingTask<Integer, Boolean> {
	String secret;
	String frob;
	String apiKey;
	String getTokenUrl;
	Callback callback;
	
	public SetFrobTask(Context context, String frob, String secret, String apiKey, String getTokenUrl, Callback callback) {
		super(context, R.string.authorizing);
		this.secret = secret;
		this.frob = frob;
		this.apiKey = apiKey;
		this.getTokenUrl = getTokenUrl;
		this.callback = callback;
	}

	@Override
	protected Boolean doInBackground(Integer... params) {
		Log.v("Floating image", "Flickr frob: " + frob);
		String signature = secret + "api_key" + apiKey + "frob" + frob + "methodflickr.auth.getToken";
		signature = FlickrFeeder.getMD5(signature);
		String getToken = getTokenUrl + frob + "&api_sig=" + signature;
		Log.v("Floating Image", "Getting token:" + getToken);
		String resp;
		try {
			resp = DownloadUtil.readStreamToEnd(getToken);
			Log.v("Floating Image", "Token response:\n" + resp);
			int start = resp.indexOf("<token>") + 7;
			int end = resp.indexOf("</token>");
			String token = resp.substring(start, end);
			
			SharedPreferences sp = mContext.getSharedPreferences("dk.nindroid.rss_preferences", 0);
			SharedPreferences.Editor e = sp.edit();
			e.putString("FLICKR_CODE", token);
			e.commit();
		} catch (IOException e1) {
			return false;
		}
		return true;
	}

	@Override
	protected void onPostExecute(Boolean result) {
		super.onPostExecute(result);
		callback.setFrobFinished(result);
	}
	
	interface Callback{
		void setFrobFinished(Boolean result);
	}
}
