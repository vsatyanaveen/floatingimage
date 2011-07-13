package dk.nindroid.rss;

import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.view.View;
import android.view.Window;
import dk.nindroid.rss.settings.Settings;

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
	public String getSettingsKey();
	public View getView();
}
