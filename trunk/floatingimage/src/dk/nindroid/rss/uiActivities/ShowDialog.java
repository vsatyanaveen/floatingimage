package dk.nindroid.rss.uiActivities;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;

public class ShowDialog  implements Runnable {
	Context mContext;
	AlertDialog.Builder mDialogBuilder;
	
	public ShowDialog(Context context, AlertDialog.Builder dialogBuilder){
		this.mContext = context;
		this.mDialogBuilder = dialogBuilder;
	}
	
	@Override
	public void run() {
		try{
			mDialogBuilder.create().show();
		}catch(Exception e){
			Log.w("Floating Image", "Could not show dialog", e);
		}
	}

}
