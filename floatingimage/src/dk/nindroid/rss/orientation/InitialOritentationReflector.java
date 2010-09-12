package dk.nindroid.rss.orientation;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.util.Log;
import android.view.Display;

public class InitialOritentationReflector {
	private static Method method;
	
	static {
		initCompatibility();
	}

	private static void initCompatibility() {
		try{
			method = Display.class.getMethod("getRotation");
		}catch (NoSuchMethodException e){
			// No can do
		}
	}
	
	private static int runGetRotation(Display display) throws IOException{
		try {
			return (Integer)method.invoke(display);
		} catch (IllegalArgumentException e) {
			Log.e("Floating Image", "Illegal argument exception caught!", e);
		} catch (IllegalAccessException e) {
			Log.e("Floating Image", "Unexpected Exception caught!", e);
		} catch (InvocationTargetException e) {
			Throwable cause = e.getCause();
	           if (cause instanceof IOException) {
	               throw (IOException) cause;
	           } else if (cause instanceof RuntimeException) {
	               throw (RuntimeException) cause;
	           } else if (cause instanceof Error) {
	               throw (Error) cause;
	           } else {
	               /* unexpected checked exception; wrap and re-throw */
	               throw new RuntimeException(e);
	           }
		}
		return -1;
	}
	
	public static int getRotation(Display display){
		if(method != null){
			try{
				return runGetRotation(display);
			}catch(IOException e){
			}
		}
		return -1;
	}
}
