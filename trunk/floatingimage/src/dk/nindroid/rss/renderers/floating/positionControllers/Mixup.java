package dk.nindroid.rss.renderers.floating.positionControllers;

import java.util.ArrayList;
import java.util.List;

import dk.nindroid.rss.Display;
import dk.nindroid.rss.MainActivity;
import dk.nindroid.rss.gfx.Vec3f;
import dk.nindroid.rss.renderers.floating.FloatingRenderer;

public class Mixup extends PositionController {
	List<PositionController> controllers;
	int current = 0;
	int imageId;
	long beginTime;
	FloatingRenderer renderer;
	
	Vec3f tmpVec;
	Rotation tmpRot1, tmpRot2;
	
	
	public Mixup(MainActivity activity, Display display, FloatingRenderer renderer, int image, FeedDataProvider dataProvider){
		this.imageId = image;
		this.beginTime = System.currentTimeMillis();
		this.renderer = renderer;
		
		tmpVec = new Vec3f();
		tmpRot1 = new Rotation();
		tmpRot2 = new Rotation();
		controllers = new ArrayList<PositionController>();
		controllers.add(new FloatLeft(activity, display, image, dataProvider));
		controllers.add(new StarSpeed(activity, display, image, dataProvider));
		controllers.add(new TableTop(activity, display, image, dataProvider));
		controllers.add(new FloatDown(activity, display, image, dataProvider));
		controllers.add(new Stack(activity, display, image, dataProvider));
	}
	
	private static final long INTERVAL = 120000;
	
	private float getCurrentPart(){
		long current = (System.currentTimeMillis() - this.beginTime - imageId * 100);
		long currentClamped = current % INTERVAL;
		int lastCurrent = this.current;
		this.current = (int)((current / INTERVAL) % controllers.size());
		if(this.current != lastCurrent){
			this.renderer.updateDepths();
		}
		if(currentClamped > INTERVAL - 500){
			return (currentClamped - (INTERVAL - 500)) / 500.0f;
		}
		return 0;
	}
	
	int next(){
		return (current + 1) % controllers.size();
	}
	
	@Override
	public float adjustInterval(float interval) {
		float a = controllers.get(current).adjustInterval(interval);
		float b = controllers.get(next()).adjustInterval(interval);
		float part = getCurrentPart();
		return (1.0f - part) * a + part * b;
	}

	@Override
	public void getGlobalOffset(float x, float y, Vec3f out) {
		controllers.get(current).getGlobalOffset(x, y, out);
	}

	@Override
	public float getOpacity(float interval) {
		float a = controllers.get(current).getOpacity(interval);
		float b = controllers.get(next()).getOpacity(interval);
		float part = getCurrentPart();
		return (1.0f - part) * a + part * b;
	}

	@Override
	public Vec3f getPosition(float interval) {
		Vec3f a = controllers.get(current).getPosition(interval);
		Vec3f b = controllers.get(next()).getPosition(interval);
		float part = getCurrentPart();
		a.setX((1.0f - part) * a.getX() + part * b.getX());
		a.setY((1.0f - part) * a.getY() + part * b.getY());
		a.setZ((1.0f - part) * a.getZ() + part * b.getZ());
		return a;
	}

	@Override
	public void getRotation(float interval, Rotation a, Rotation b) {
		controllers.get(current).getRotation(interval, a, b);
		controllers.get(next()).getRotation(interval, tmpRot1, tmpRot2);
		float part = getCurrentPart();
		a.setAngle((1.0f - part) * a.getAngle() + part * tmpRot1.getAngle());
		a.setX((1.0f - part) * a.getX() + part * tmpRot1.getX());
		a.setY((1.0f - part) * a.getY() + part * tmpRot1.getY());
		a.setZ((1.0f - part) * a.getZ() + part * tmpRot1.getZ());
		
		b.setAngle((1.0f - part) * b.getAngle() + part * tmpRot2.getAngle());
		b.setX((1.0f - part) * b.getX() + part * tmpRot2.getX());
		b.setY((1.0f - part) * b.getY() + part * tmpRot2.getY());
		b.setZ((1.0f - part) * b.getZ() + part * tmpRot2.getZ());
	}

	@Override
	public float getTimeAdjustment(float speedX, float speedY) {
		return controllers.get(current).getTimeAdjustment(speedX, speedY);
	}

	@Override
	public void jitter() {
		controllers.get(current).jitter();
		controllers.get(next()).jitter();
	}
	
}
