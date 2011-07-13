package dk.nindroid.rss.compatibility;

import android.app.Activity;

public class TryGetFragmentManager {
	public static boolean supportsFragments(Activity a){
		try{
			CrashingClass.get(a);
			return true;
			}catch (Throwable t){
				return false;
			}
	}
	
	private static class CrashingClass{
		static void get(Activity a){
			a.getFragmentManager();
		}
	}
}
