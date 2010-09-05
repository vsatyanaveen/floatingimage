package dk.nindroid.rss.uiActivities;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

public abstract class BlockingTask<Params, Result> extends AsyncTask<Params, Integer, Result> {
	private   int     mMessage;
	protected Context mContext;
	protected ProgressDialog  mBusyDialog;
	private   int	  mError = -1;
	
	public BlockingTask(Context context, int messageRes){
		this.mContext = context;
		this.mMessage = messageRes;
	}
	
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		mBusyDialog = new ProgressDialog(mContext);
		mBusyDialog.setTitle(mMessage);
		mBusyDialog.show();
	}
	
	protected void onPostExecute(Result result) {
		if(mBusyDialog.isShowing()){
			mBusyDialog.dismiss();
		}
		if(mError != -1){
			Toast.makeText(mContext, mError, Toast.LENGTH_LONG).show();
		}
	}
	
	protected final void setError(int res){
		mError = res;
	}
}
