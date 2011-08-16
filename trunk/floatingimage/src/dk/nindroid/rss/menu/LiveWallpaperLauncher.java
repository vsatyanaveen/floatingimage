package dk.nindroid.rss.menu;

import java.util.List;

import dk.nindroid.rss.R;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.widget.Toast;

public class LiveWallpaperLauncher extends Activity{
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Intent intent = new Intent("android.service.wallpaper.LIVE_WALLPAPER_CHOOSER");
		List<ResolveInfo> list = getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        final boolean isInstalled = list.size() > 0;
        if(isInstalled){
        	startActivityForResult(intent, 0);
        }else{
        	Toast.makeText(this, R.string.device_does_not_support_live_wallpapers, Toast.LENGTH_LONG).show();
        }
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		this.finish();
	}
}
