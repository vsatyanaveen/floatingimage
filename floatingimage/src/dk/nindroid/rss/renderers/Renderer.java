package dk.nindroid.rss.renderers;

import javax.microedition.khronos.opengles.GL10;

import android.content.Intent;
import dk.nindroid.rss.data.ImageReference;

public abstract class Renderer {
	public abstract void update(GL10 gl, long time, long realTime);
	public abstract void render(GL10 gl, long frameTime, long realtime);
	public long editOffset(long offset, long realTime){
		return offset;
	}
	public abstract void init(GL10 gl, long time, OSD osd);
	public abstract Intent followCurrent();
	public abstract ImageReference getCurrent();
	public abstract boolean back();
	public abstract void onPause();
	public abstract void onResume();
	public abstract void click(GL10 gl, float x, float y, long frameTime, long realTime);
	public abstract boolean slideRight(long realtime);
	public abstract boolean slideLeft(long realtime);
	public abstract void setBackground();
	public abstract void resetImages();
	public abstract void transform(float centerX, float centerY, float x, float y, float rotate, float scale);
	public abstract void initTransform();
	public abstract void transformEnd();
	public abstract boolean freeMove();
	public abstract void move(float x, float y);
	public abstract int totalImages();
}
