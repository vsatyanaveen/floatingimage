package dk.nindroid.rss.renderers.osd;

import java.io.InputStream;

import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import dk.nindroid.rss.Display;
import dk.nindroid.rss.R;

public class Fullscreen extends Button {
	Bitmap 	mFullscreen;
	int		mFullscreenTex;
	Display mDisplay;

	public Fullscreen(Context context) {
		InputStream is = context.getResources().openRawResource(R.drawable.osd_toggle_full);
		mFullscreen = BitmapFactory.decodeStream(is);
	}

	@Override
	public void click(long time) {
		mDisplay.toggleFullscreen();
	}

	@Override
	public int getTextureID() {
		return mFullscreenTex;
	}

	public void init(GL10 gl, Display display) {
		this.mDisplay = display;
		int[] textures = new int[1];
		gl.glGenTextures(1, textures, 0);
		mFullscreenTex = textures[0];
		setTexture(gl, mFullscreen, mFullscreenTex);
	}

}
