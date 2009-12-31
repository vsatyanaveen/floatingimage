package dk.nindroid.rss;

import android.util.Log;
import dk.nindroid.rss.orientation.OrientationSubscriber;

public class Display implements OrientationSubscriber {
	private static final long		TURN_TIME = 500;
	private long					mTurnedAt;
	private long					mFrameTime;
	private boolean					mTurning = false;
	
	private float smoothstep(float val){
		return Math.min(val * val * (3.0f - 2.0f * val), 1.0f);
	}
	
	public void setFrameTime(long time){
		mFrameTime = time;
		if(mFrameTime < mTurnedAt + TURN_TIME){
			float fraction = 1.0f - ((float)((mTurnedAt + TURN_TIME) - mFrameTime)) / TURN_TIME;
			fraction = smoothstep(fraction);
			mWidth = (mTargetWidth - mPreviousWidth) * fraction + mPreviousWidth;
			mHeight = (mTargetHeight - mPreviousHeight) * fraction + mPreviousHeight;
			mWidthPixels = (int)((mTargetWidthPixels - mPreviousWidthPixels) * fraction + mPreviousHeight);
			mHeightPixels = (int)((mTargetHeightPixels - mPreviousHeightPixels) * fraction + mPreviousHeight);
			mFocusedHeight = (mTargetFocusedHeight - mPreviousFocusedHeight) * fraction + mPreviousFocusedHeight;
			mRotation = (mTargetRotation - mPreviousRotation) * fraction + mPreviousRotation;
		}else{
			if(mTurning){
				mTurning = false;
				mWidth = mTargetWidth;
				mHeight = mTargetHeight;
				mWidthPixels = mTargetWidthPixels;
				mHeightPixels = mTargetHeightPixels;
				mFocusedHeight = mTargetFocusedHeight;
				mRotation = mTargetRotation;
			}
		}
	}
	
	public boolean isTurning(){
		return mTurning;
	}
	
	private int						mOrientation;
	public int getOrientation(){
		return mOrientation;
	}
	
	// Portrait, up is up
	private float					mPortraitWidth;
	private float					mPortraitHeight;
	private int 					mPortraitWidthPixels;
	private int 					mPortraitHeightPixels;
	public float getPortraitWidth(){
		return mPortraitWidth;
	}
	public float getPortraitHeight(){
		return mPortraitHeight;
	}
	public int getPortraitWidthPixels(){
		return mPortraitWidthPixels;
	}
	public int getPortraitHeightPixels(){
		return mPortraitHeightPixels;
	}
	
	// Previous (Before rotation)
	private float					mPreviousWidth;
	private float					mPreviousHeight;
	private int 					mPreviousWidthPixels;
	private int 					mPreviousHeightPixels;
	private float  					mPreviousFocusedHeight;
	private float					mPreviousRotation;
	
	// Target (After rotation)
	private float					mTargetWidth;
	private float					mTargetHeight;
	private int 					mTargetWidthPixels;
	private int 					mTargetHeightPixels;
	private float  					mTargetFocusedHeight;
	private float					mTargetRotation;
	
	// Current
	
	
	private float					mWidth;
	private float					mHeight;
	private int 					mWidthPixels;
	private int 					mHeightPixels;
	private float  					mFocusedHeight;
	private float					mRotation;
	
	public float getWidth(){
		return mWidth;
	}
	public float getHeight(){
		return mHeight;
	}
	public int getWidthPixels(){
		return mWidthPixels;
	}
	public int getHeightPixels(){
		return mHeightPixels;
	}
	public float getFocusedHeight(){
		return mFocusedHeight;
	}
	public float getRotation(){
		return mRotation;
	}
	
	public Display(){
		this.mOrientation = UP_IS_UP;
	}
	
	public void onSurfaceChanged(int width, int height){
		mPortraitHeight = 2.0f;
		mHeight = mPortraitHeight;
		mPortraitWidthPixels = width;
		mWidthPixels = width;
		mPortraitHeightPixels = height;
		mHeightPixels = height;
		mFocusedHeight = calcFocusedHeight(mPortraitHeight, height);
		
		float screenAspect = (float)width / height;
		
		mPortraitWidth = screenAspect * 2.0f;
		mWidth = mPortraitWidth;
	}
	
	@Override
	public void setOrientation(int orientation) {
		Log.v("dk.nindroid.rss.RiverRenderer", "Orientation change received: " + orientation);
		if((orientation == UP_IS_UP || orientation == UP_IS_LEFT) && orientation != mOrientation){
			mTurnedAt = mFrameTime;
			mOrientation = orientation;
			mPreviousWidth = mWidth;
			mPreviousHeight = mHeight;
			mPreviousWidthPixels  = mWidthPixels;
			mPreviousHeightPixels = mHeightPixels;
			mPreviousFocusedHeight = mFocusedHeight;
			mPreviousRotation = mRotation;
			if(orientation == UP_IS_UP){
				mTargetWidth = mPortraitWidth;
				mTargetHeight = mPortraitHeight;
				mTargetWidthPixels  = mPortraitWidthPixels;
				mTargetHeightPixels = mPortraitHeightPixels;
				mTargetFocusedHeight = calcFocusedHeight(mPortraitHeight, mPortraitHeightPixels);
				mTargetRotation = 0;
			}else if(orientation == UP_IS_LEFT){
				mTargetWidth = mPortraitHeight;
				mTargetHeight = mPortraitWidth;
				mTargetWidthPixels  = mPortraitHeightPixels;
				mTargetHeightPixels = mPortraitWidthPixels;
				mTargetFocusedHeight = calcFocusedHeight(mPortraitWidth, mPortraitWidthPixels);
				mTargetRotation = -90;
			}
			mTurning = true;
 		}
	}
	
	private static float calcFocusedHeight(float height, int heightPixels){
		return height - 80.0f / heightPixels * height;
	}
}
