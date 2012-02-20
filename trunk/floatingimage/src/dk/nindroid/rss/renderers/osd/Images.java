package dk.nindroid.rss.renderers.osd;

import java.io.InputStream;

import javax.microedition.khronos.opengles.GL10;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import dk.nindroid.rss.MainActivity;
import dk.nindroid.rss.R;

public class Images  extends Button {
	int		mImagesTex;
	MainActivity mActivity;

	public Images(MainActivity activity) {
		this.mActivity = activity;
	}

	@Override
	public void click(long time) {
		mActivity.manageFeeds();
	}

	@Override
	public int getTextureID() {
		return mImagesTex;
	}

	public void init(GL10 gl) {
		InputStream is = mActivity.context().getResources().openRawResource(R.drawable.osd_images);
		Bitmap bmp = BitmapFactory.decodeStream(is);
		
		int[] textures = new int[1];
		gl.glGenTextures(1, textures, 0);
		mImagesTex = textures[0];
		setTexture(gl, bmp, mImagesTex);
		
		bmp.recycle();
	}

}
