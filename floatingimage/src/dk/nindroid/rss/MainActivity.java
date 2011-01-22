package dk.nindroid.rss;

import java.io.IOException;
import java.io.InputStream;

import dk.nindroid.rss.settings.Settings;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.view.Window;

public interface MainActivity {
	public Context context();
	public void openContextMenu();
	public void runOnUiThread(Runnable action);
	public void stopManagingCursor(Cursor c);
	public void setWallpaper(InputStream is) throws IOException;
	public void setWallpaper(Bitmap bmp) throws IOException;
	public Window getWindow();
	public void showFolder();
	public void showSettings();
	public Settings getSettings();
}
