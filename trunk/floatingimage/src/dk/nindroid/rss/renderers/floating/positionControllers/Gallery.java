package dk.nindroid.rss.renderers.floating.positionControllers;

import dk.nindroid.rss.Display;
import dk.nindroid.rss.MainActivity;
import dk.nindroid.rss.gfx.Vec3f;

public class Gallery extends PositionController {
	public static final float  	mFloatZ = -3.5f;
	
	MainActivity mActivity;
	Display mDisplay;
	float mYLayerPos = 0;
	
	// Return types - avoid creating new objects
	Vec3f mJitter;
	Vec3f mPos;
	float mRotation;
	final float mSpacing;
	final int mImageId;

	FeedDataProvider mDataProvider;
	
	public Gallery(MainActivity activity, Display display, int image, FeedDataProvider dataProvider){
		mSpacing = 1.0f / (dataProvider.getNumberOfImages() / 3.0f);
		this.mDataProvider = dataProvider;
		this.mImageId = image;
		this.mDisplay = display;
		this.mActivity = activity;
		mJitter = new Vec3f();
		mPos = new Vec3f();
		switch(image % 3){
			case 0: mYLayerPos = 1.5f; break;	
			case 1: mYLayerPos = 0.0f; break;
			case 2: mYLayerPos = -1.5f; break;
		}
		jitter();
	}
	
	@Override
	public void jitter() {
	}

	@Override
	public float getOpacity(float interval) {
		return 1;
	}

	@Override
	public Vec3f getPosition(float interval) {
		float farRight = getFarRight();
		mPos.setX(farRight - (interval * farRight * 2) + mJitter.getX());
		mPos.setY(mYLayerPos * mDisplay.getFocusedHeight() + mJitter.getY());
		mPos.setZ(mFloatZ + mJitter.getZ());	
		return mPos;
	}


	@Override
	public void getRotation(float interval, Rotation a, Rotation b) {
		a.setAngle(0.0f);
		b.setAngle(0.0f);
	}

	public float getFarRight(){
		return mDisplay.getWidth() * (-mFloatZ);
	}

	@Override
	public void getGlobalOffset(float x, float y, Vec3f out) {
		if(y*y > x*x*x*x){
			out.setY(y / 100.0f);
		}
	}

	@Override
	public float getTimeAdjustment(float speedX, float speedY) {
		return -speedX;
	}
	
	@Override
	public float getScale() {
		return 0.70f;
	}

	@Override
	public float adjustInterval(float interval) {
		float spacing = 1.0f / (mDataProvider.getNumberOfImages() / 3.0f);
		int row = mImageId / 3;
		return (interval - spacing * row + 1) % 1;
	}
}
