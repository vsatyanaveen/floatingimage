package dk.nindroid.rss.gfx;

import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class ImageUtil {
	public static Bitmap readBitmap(Context c, int res){
		InputStream is = c.getResources().openRawResource(res);
		Bitmap icon = BitmapFactory.decodeStream(is);
		try {
			is.close();
		} catch (IOException e) {
			Log.e("Floating Image", "Error closing resource stream", e);
		}
		return icon;
	}
	
	public static float smoothstep(float val){
		return Math.min(val * val * (3.0f - 2.0f * val), 1.0f);
	}
}
