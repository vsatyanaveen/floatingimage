package dk.nindroid.rss.renderers.osd;

import java.io.InputStream;

import javax.microedition.khronos.opengles.GL10;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import dk.nindroid.rss.MainActivity;
import dk.nindroid.rss.R;

public class Settings  extends Button {
	Bitmap 	mSettings;
	int		mSettingsTex;
	MainActivity mActivity;

	public Settings(MainActivity activity) {
		this.mActivity = activity;
		InputStream is = activity.context().getResources().openRawResource(R.drawable.osd_settings);
		mSettings = BitmapFactory.decodeStream(is);
	}

	@Override
	public void click(long time) {
		mActivity.showSettings();
	}

	@Override
	public int getTextureID() {
		return mSettingsTex;
	}

	public void init(GL10 gl) {
		int[] textures = new int[1];
		gl.glGenTextures(1, textures, 0);
		mSettingsTex = textures[0];
		setTexture(gl, mSettings, mSettingsTex);
	}
}
