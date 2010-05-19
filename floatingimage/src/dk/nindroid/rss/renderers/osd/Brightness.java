package dk.nindroid.rss.renderers.osd;

import java.io.InputStream;

import javax.microedition.khronos.opengles.GL10;

import dk.nindroid.rss.R;
import dk.nindroid.rss.ShowStreams;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.WindowManager;

public class Brightness extends Button {
	private static final float		BRIGHTNESS_LOW  = 0.1f;
	private static final float		BRIGHTNESS_MID  = 0.5f;
	private static final float		BRIGHTNESS_HIGH = 1.0f;
	private static final float[] 	BRIGHTNESS = new float[]{BRIGHTNESS_LOW, BRIGHTNESS_MID, BRIGHTNESS_HIGH};
	
	private int 				mBrightnessIndex = 0; 
	
	Bitmap 	mBrightness;
	int		mBrightnessTex;

	public Brightness(Context context) {
		InputStream is = context.getResources().openRawResource(R.drawable.osd_brightness);
		mBrightness = BitmapFactory.decodeStream(is);
	}

	@Override
	public void click() {
		WindowManager.LayoutParams lp = ShowStreams.current.getWindow().getAttributes();
		mBrightnessIndex = (mBrightnessIndex + 1) % BRIGHTNESS.length;
		lp.screenBrightness = BRIGHTNESS[mBrightnessIndex];
		ShowStreams.current.getWindow().setAttributes(lp);
	}

	@Override
	public int getTextureID() {
		return mBrightnessTex;
	}

	@Override
	public void init(GL10 gl) {
		int[] textures = new int[1];
		gl.glGenTextures(1, textures, 0);
		mBrightnessTex = textures[0];
		setTexture(gl, mBrightness, mBrightnessTex);
		
	}

}
