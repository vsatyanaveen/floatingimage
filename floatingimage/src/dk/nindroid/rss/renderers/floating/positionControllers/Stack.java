package dk.nindroid.rss.renderers.floating.positionControllers;

import java.util.Random;

import dk.nindroid.rss.Display;
import dk.nindroid.rss.MainActivity;
import dk.nindroid.rss.gfx.Vec3f;
import dk.nindroid.rss.renderers.floating.FloatingRenderer;

public class Stack extends SequentialController {
	public static final float  	mFloatZ = -1.5f;
	
	MainActivity mActivity;
	Display mDisplay;
	Random mRand;
	float mXLayerPos = 0;
	float mYLayerPos = 0;
	
	// Return types - avoid creating new objects
	//Vec3f mJitter;
	Vec3f mPos;
	float mRotationOffset;
	float mRotationAmount;
	
	private final static Vec3f ROTATION = new Vec3f(0, 0, 1);
	
	public Stack(MainActivity activity, Display display, int image, FeedDataProvider dataProvider){
		super(image, dataProvider);
		mRand = new Random(System.currentTimeMillis() + image);
		this.mDisplay = display;
		this.mActivity = activity;
		mPos = new Vec3f();
		mXLayerPos = 0.0f;
		mYLayerPos = 0.0f;
		jitter();
	}
	
	@Override
	public void jitter() {
		mRotationOffset = mActivity.getSettings().rotateImages ? mRand.nextFloat() * 60.0f - 30.0f : 0;
	}

	@Override
	public float getOpacity(float interval) {
		if(interval < 0.01f){
			return 0;
		}else if(interval < 0.025f){
			return (interval - 0.01f) * (1.0f / 0.015f);	
		}else if(interval > 0.95f){
			return (1.0f - interval) * (1 / 0.05f);
		}else{
			return 1;
		}
	}

	@Override
	public Vec3f getPosition(float interval) {
		mPos.setX(-interval * 0.4f * mDisplay.getWidth());
		mPos.setY(-interval * 0.2f * mDisplay.getHeight());
		
		mPos.setZ(-Math.min(0.03f, interval) * 50.0f - 0.1f * interval);
		return mPos;
	}
	
	@Override
	public void getRotation(float interval, Rotation a, Rotation b) {
		a.setX(ROTATION.getX());
		a.setY(ROTATION.getY());
		a.setZ(ROTATION.getZ());
		a.setAngle((0.03f - Math.min(0.03f, interval)) * mRotationAmount * 20);
	}

	public float getFarBottom(){
		return mDisplay.getHeight() * 0.7f * (-mFloatZ + FloatingRenderer.mJitterZ) * 1.2f + 1.3f + FloatingRenderer.mJitterY;
	}

	@Override
	public void getGlobalOffset(float x, float y, Vec3f out) {
		if(x*x*x*x > y*y){
			out.setX(-x / 100.0f);
		}
	}

	@Override
	public float getTimeAdjustment(float speedX, float speedY) {
		if(speedY * speedY * speedY * speedY > speedX * speedX){
			return speedY;
		}
		return 0;
	}
}
