package dk.nindroid.rss.renderers;

import javax.microedition.khronos.opengles.GL10;

import android.content.Intent;
import dk.nindroid.rss.data.ImageReference;
import dk.nindroid.rss.helpers.MatrixTrackingGL;

public interface Renderer {
	void update(MatrixTrackingGL gl, long time, long realTime);
	void render(MatrixTrackingGL gl, long frameTime, long realtime);
	void init(GL10 gl, long time);
	Intent followCurrent();
	ImageReference getCurrent();
	boolean back();
	void onPause();
	void onResume();
	void click(MatrixTrackingGL gl, float x, float y, long frameTime, long realTime);
	boolean slideRight(long realtime);
	boolean slideLeft(long realtime);
	void setBackground();
}
