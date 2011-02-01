package dk.nindroid.rss.renderers.osd;

import java.io.InputStream;

import javax.microedition.khronos.opengles.GL10;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.Window;
import android.view.WindowManager;
import dk.nindroid.rss.MainActivity;
import dk.nindroid.rss.R;

public class Brightness extends Button {
	private static final float		BRIGHTNESS_LOW  = 0.1f;
	private static final float		BRIGHTNESS_MID  = 0.5f;
	private static final float		BRIGHTNESS_HIGH = 1.0f;
	private static final float[] 	BRIGHTNESS = new float[]{BRIGHTNESS_LOW, BRIGHTNESS_MID, BRIGHTNESS_HIGH};
	private MainActivity 			mActivity;
	
	private int 					mBrightnessIndex = 0; 
	
	Bitmap 	mBrightness;
	int		mBrightnessTex;

	public Brightness(MainActivity activity) {
		this.mActivity = activity;
		InputStream is = activity.context().getResources().openRawResource(R.drawable.osd_brightness);
		mBrightness = BitmapFactory.decodeStream(is);
	}

	@Override
	public void click(long time) {
		Window window = mActivity.getWindow();
		if(window != null){
			WindowManager.LayoutParams lp = window.getAttributes();
			mBrightnessIndex = (mBrightnessIndex + 1) % BRIGHTNESS.length;
			lp.screenBrightness = BRIGHTNESS[mBrightnessIndex];
			window.setAttributes(lp);
		}
	}

	@Override
	public int getTextureID() {
		return mBrightnessTex;
	}

	public void init(GL10 gl) {
		int[] textures = new int[1];
		gl.glGenTextures(1, textures, 0);
		mBrightnessTex = textures[0];
		setTexture(gl, mBrightness, mBrightnessTex);
		
	}

}
