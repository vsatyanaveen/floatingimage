package dk.nindroid.rss.renderers.floating.positionControllers;

import dk.nindroid.rss.gfx.Vec3f;

public interface PositionController {
	void jitter();
	Vec3f getPosition(float interval);
	void getRotation(float interval, Rotation a, Rotation b);
	float getOpacity(float interval);
	float getTimeAdjustment(float speedX, float speedY);
	void getGlobalOffset(float x, float y, Vec3f out);
}
