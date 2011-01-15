package dk.nindroid.rss.renderers;

import dk.nindroid.rss.Display;
import dk.nindroid.rss.TextureSelector;
import dk.nindroid.rss.gfx.ImageUtil;

public class Rotator {
	private long 	mTurnedAt;
	private float	mPrevious = 0;
	private float 	mTarget = 0;
	private boolean mTurning = false;
	
	public float getFraction(long time){
		return 1.0f - ((float)((mTurnedAt + Display.TURN_TIME) - time)) / Display.TURN_TIME;
	}
	
	public float getRotation(TextureSelector textureSelector, long time){
		if(mTurning){
			float fraction = getFraction(time);
			if(fraction > 1.0f){
				mTurning = false;
				textureSelector.setRotated(mTarget);
				return mTarget;
			}else{
				fraction = ImageUtil.smoothstep(fraction);
				return (mTarget - mPrevious) * fraction + mPrevious;
			}
		}
		return mTarget;
	}
	
	public void turn(long time, float degrees){
		if(mTurning) return;
		mPrevious = mTarget;
		mTarget += degrees;
		mTurnedAt = time;
		mTurning = true;
	}
	
	public void setRotation(float degrees){
		mTarget = degrees;
	}
	
	public float getTargetOrientation(){
		return mTarget;
	}
	
	public float getPreviousOrientation(){
		return mPrevious;
	}
}
