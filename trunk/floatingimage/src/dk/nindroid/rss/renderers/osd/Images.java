package dk.nindroid.rss.renderers.osd;

import java.io.InputStream;

import javax.microedition.khronos.opengles.GL10;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import dk.nindroid.rss.MainActivity;
import dk.nindroid.rss.R;

public class Images  extends Button {
	Bitmap 	mImages;
	int		mImagesTex;
	MainActivity mActivity;

	public Images(MainActivity activity) {
		this.mActivity = activity;
		InputStream is = activity.context().getResources().openRawResource(R.drawable.osd_images);
		mImages = BitmapFactory.decodeStream(is);
	}

	@Override
	public void click(long time) {
		mActivity.showFolder();
	}

	@Override
	public int getTextureID() {
		return mImagesTex;
	}

	public void init(GL10 gl) {
		int[] textures = new int[1];
		gl.glGenTextures(1, textures, 0);
		mImagesTex = textures[0];
		setTexture(gl, mImages, mImagesTex);
	}

}
