package dk.nindroid.rss.renderers.osd;

import java.io.InputStream;

import javax.microedition.khronos.opengles.GL10;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import dk.nindroid.rss.MainActivity;
import dk.nindroid.rss.R;

public class Settings  extends Button {
	int		mSettingsTex;
	MainActivity mActivity;

	public Settings(MainActivity activity) {
		this.mActivity = activity;
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
		InputStream is = mActivity.context().getResources().openRawResource(R.drawable.osd_settings);
		Bitmap bmp = BitmapFactory.decodeStream(is);
		
		int[] textures = new int[1];
		gl.glGenTextures(1, textures, 0);
		mSettingsTex = textures[0];
		setTexture(gl, bmp, mSettingsTex);
		
		bmp.recycle();
	}
}
