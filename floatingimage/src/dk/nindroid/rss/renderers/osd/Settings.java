package dk.nindroid.rss.renderers.osd;

import java.io.InputStream;

import javax.microedition.khronos.opengles.GL10;

import dk.nindroid.rss.R;
import dk.nindroid.rss.ShowStreams;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class Settings  extends Button {
	Bitmap 	mSettings;
	int		mSettingsTex;

	public Settings(Context context) {
		InputStream is = context.getResources().openRawResource(R.drawable.osd_settings);
		mSettings = BitmapFactory.decodeStream(is);
	}

	@Override
	public void click(long time) {
		ShowStreams.current.showSettings();
	}

	@Override
	public int getTextureID() {
		return mSettingsTex;
	}

	@Override
	public void init(GL10 gl) {
		int[] textures = new int[1];
		gl.glGenTextures(1, textures, 0);
		mSettingsTex = textures[0];
		setTexture(gl, mSettings, mSettingsTex);
	}
}
