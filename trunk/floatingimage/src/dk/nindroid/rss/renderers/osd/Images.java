package dk.nindroid.rss.renderers.osd;

import java.io.InputStream;

import javax.microedition.khronos.opengles.GL10;

import dk.nindroid.rss.R;
import dk.nindroid.rss.ShowStreams;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class Images  extends Button {
	Bitmap 	mImages;
	int		mImagesTex;

	public Images(Context context) {
		InputStream is = context.getResources().openRawResource(R.drawable.osd_images);
		mImages = BitmapFactory.decodeStream(is);
	}

	@Override
	public void click() {
		ShowStreams.current.showFolder();
	}

	@Override
	public int getTextureID() {
		return mImagesTex;
	}

	@Override
	public void init(GL10 gl) {
		int[] textures = new int[1];
		gl.glGenTextures(1, textures, 0);
		mImagesTex = textures[0];
		setTexture(gl, mImages, mImagesTex);
	}

}
