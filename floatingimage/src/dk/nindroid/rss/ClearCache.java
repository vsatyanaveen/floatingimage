package dk.nindroid.rss;

import java.io.File;

import android.app.Activity;
import android.os.Bundle;

public class ClearCache extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		String explore = getString(R.string.dataFolder) + getString(R.string.exploreFolder);
		File exploreDir = new File(explore);
		deleteAll(exploreDir);
		File exploreImgDir = new File(exploreDir.getAbsolutePath() + "/bmp");
		exploreImgDir.mkdirs();
		this.finish();
	}
	
	public static void deleteAll(File path){
		if(path.isDirectory()){
			for(String file : path.list()){
				deleteAll(new File(path.getAbsolutePath() + "/" + file));
			}
		}
		path.delete();
	}
}
