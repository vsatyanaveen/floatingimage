package dk.nindroid.rss.renderers;

import android.graphics.Bitmap;

public interface ImagePlane {
	public static final int SIZE_LARGE 		= 1;
	public static final int SIZE_ORIGINAL 	= 2;
	
	void setFocusTexture(Bitmap texture, float width, float height, int sizeType);
	boolean validForTextureUpdate();
}
