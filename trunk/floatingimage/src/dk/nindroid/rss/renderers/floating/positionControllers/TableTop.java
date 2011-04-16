package dk.nindroid.rss.renderers.floating.positionControllers;

import java.util.Random;

import dk.nindroid.rss.Display;
import dk.nindroid.rss.MainActivity;
import dk.nindroid.rss.gfx.Vec3f;
import dk.nindroid.rss.renderers.floating.FloatingRenderer;

public class TableTop extends SequentialController {
	public static final float  	mFloatZ = -2.5f;
	
	MainActivity mActivity;
	Display mDisplay;
	Random mRand;
	float mXLayerPos = 0;
	float mYLayerPos = 0;
	
	// Return types - avoid creating new objects
	Vec3f mJitter;
	Vec3f mPos;
	float mRotationOffset;
	float mRotationAmount;
	
	private final static Vec3f ROTATION_A = new Vec3f(0, 0, 1);
	private final static Vec3f ROTATION_B = new Vec3f(0, 0, 1);
	
	public TableTop(MainActivity activity, Display display, int image, int noImages){
		super(image, noImages);
		mRand = new Random(System.currentTimeMillis() + image);
		this.mDisplay = display;
		this.mActivity = activity;
		mJitter = new Vec3f();
		mPos = new Vec3f();
		/*
		switch(image % 4){
			case 0: 
				mXLayerPos = -1.0f; 
				mYLayerPos = -1.0f;
				break;	
			case 1: 
				mXLayerPos = -1.0f;
				mYLayerPos =  1.0f;
				break;
			case 2: 
				mXLayerPos =  1.0f;
				mYLayerPos = -1.0f;
				break;
			case 3: 
				mXLayerPos =  1.0f;
				mYLayerPos =  1.0f;
				break;
		}
		*/
		mXLayerPos = 0.0f;
		mYLayerPos = 0.0f;
		jitter();
	}
	
	@Override
	public void jitter() {
		mJitter.setX(mRand.nextFloat() * FloatingRenderer.mJitterX * 4 - FloatingRenderer.mJitterX * 2);
		mJitter.setY(mRand.nextFloat() * FloatingRenderer.mJitterY * 4 - FloatingRenderer.mJitterY * 2);
		mJitter.setZ(mRand.nextFloat() * FloatingRenderer.mJitterZ * 4 - FloatingRenderer.mJitterZ * 2);
		mRotationOffset = mActivity.getSettings().rotateImages ? mRand.nextFloat() * 60.0f - 30.0f : 0;
		mRotationAmount = mActivity.getSettings().rotateImages ? mRand.nextFloat() * 60.0f - 30.0f : 0;
	}

	@Override
	public float getOpacity(float interval) {
		if(interval < 0.01f){
			return 0;
		}else if(interval < 0.03f){
			return (interval - 0.01f) * (1 / 0.02f);	
		}else if(interval > 0.95f){
			return (1.0f - interval) * (1 / 0.05f);
		}else{
			return 1;
		}
	}

	@Override
	public Vec3f getPosition(float interval) {
		mPos.setX(mJitter.getX() * mDisplay.getWidth());
		mPos.setY(mJitter.getY() * mDisplay.getHeight());
		
		mPos.setZ(-Math.min(0.03f, interval) * 100.0f - 0.1f * interval);
		return mPos;
	}
	
	@Override
	public void getRotation(float interval, Rotation a, Rotation b) {
		a.setX(ROTATION_A.getX());
		a.setY(ROTATION_A.getY());
		a.setZ(ROTATION_A.getZ());
		a.setAngle((0.03f - Math.min(0.03f, interval)) * mRotationAmount * 20 + mRotationOffset);
		b.setX(ROTATION_B.getX());
		b.setY(ROTATION_B.getY());
		b.setZ(ROTATION_B.getZ());
		b.setAngle(0.0f);
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
		return speedY;
	}
}
