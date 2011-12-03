package dk.nindroid.rss.renderers.floating.positionControllers;

import dk.nindroid.rss.gfx.Vec3f;

public abstract class PositionController {
	
	public long adjustTime(long time, long traversalTime){
		return time - (long)(traversalTime * (1.0f - adjustInterval(1.0f)));
	}
	
	public float getScale(){
		return 1.0f;
	}
	
	public boolean isReversed(){
		return false;
	}
	public abstract void jitter();
	public abstract Vec3f getPosition(float interval);
	public abstract void getRotation(float interval, Rotation a, Rotation b);
	public abstract float getOpacity(float interval);
	public abstract float getTimeAdjustment(float speedX, float speedY);
	public abstract void getGlobalOffset(float x, float y, Vec3f out);
	public abstract float adjustInterval(float interval);
}
