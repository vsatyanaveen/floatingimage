package dk.nindroid.rss.compatibility;

import java.io.IOException;
import java.io.InputStream;

import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;


public class SetWallpaper {
	public static void setWallpaper(Bitmap bmp, Context ctx) throws IOException{
		WallpaperManager.getInstance(ctx).setBitmap(bmp);
	}
	
	public static void setWallpaper(InputStream is, Context ctx) throws IOException{
		WallpaperManager.getInstance(ctx).setStream(is);
	}
}
