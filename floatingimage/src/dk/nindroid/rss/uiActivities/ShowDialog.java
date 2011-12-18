package dk.nindroid.rss.uiActivities;

import android.app.AlertDialog;
import android.content.Context;

public class ShowDialog  implements Runnable {
	Context mContext;
	AlertDialog.Builder mDialogBuilder;
	
	public ShowDialog(Context context, AlertDialog.Builder dialogBuilder){
		this.mContext = context;
		this.mDialogBuilder = dialogBuilder;
	}
	
	@Override
	public void run() {
		mDialogBuilder.create().show();
	}

}
