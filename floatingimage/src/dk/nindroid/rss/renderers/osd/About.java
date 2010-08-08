package dk.nindroid.rss.renderers.osd;

import java.io.InputStream;

import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import dk.nindroid.rss.R;
import dk.nindroid.rss.ShowStreams;

public class About extends Button {
	Bitmap 	mAbout;
	int 	mAboutTex;

	public About(Context context) {
		InputStream is = context.getResources().openRawResource(R.drawable.osd_about);
		mAbout = BitmapFactory.decodeStream(is);
	}

	@Override
	public void click(long time) {
		ShowStreams.current.showAbout();
	}

	@Override
	public int getTextureID() {
		return mAboutTex;
	}

	@Override
	public void init(GL10 gl) {
		int[] textures = new int[1];
		gl.glGenTextures(1, textures, 0);
		mAboutTex = textures[0];
		setTexture(gl, mAbout, mAboutTex);
	}

}
