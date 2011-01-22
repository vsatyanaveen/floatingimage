package dk.nindroid.rss;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;
import android.view.Surface;
import dk.nindroid.rss.gfx.ImageUtil;
import dk.nindroid.rss.orientation.OrientationSubscriber;
import dk.nindroid.rss.settings.Settings;

public class Display implements OrientationSubscriber {
	public static final long		TURN_TIME = 500;
	private static final long		FULLSCREEN_TIME = 300;
	private final static float		INFOBAR_HEIGHT = 80.0f;
	private static final float		NORMAL_FILL = 0.90f;
	
	private Settings				mSettings;
	private long					mTurnedAt;
	private long					mFullscreenAt;
	private long					mFrameTime;
	private boolean					mTurning = false;
	private boolean					mFullscreen = false;
	private List<ImageSizeChanged>	mImageSizeChangedListeners = new ArrayList<ImageSizeChanged>();
	
	public void RegisterImageSizeChangedListener(ImageSizeChanged listener){
		mImageSizeChangedListeners.add(listener);
	}
	
	public void deRegisterImageSizeChangedListener(ImageSizeChanged listener){
		mImageSizeChangedListeners.remove(listener);
	}
	
	public boolean isFullscreen(){
		return mFullscreen;
	}
	public void toggleFullscreen(){
		mFullscreen ^= true;
		mFullscreenAt = mFrameTime;
		mPreviousInfoBarHeight = mInfoBarHeight;
		mPreviousFill = mFill;
		mTargetInfoBarHeight = mFullscreen ? 0 : INFOBAR_HEIGHT;
		mTargetFill = mFullscreen ? 1.0f : NORMAL_FILL;
		mSettings.setFullscreen(mFullscreen);
		Log.v("Display", "Fullscreen is " + mFullscreen);
	}
		
	public void setFrameTime(long time){
		mFrameTime = time;
		if(mFrameTime < mTurnedAt + TURN_TIME){
			float fraction = 1.0f - ((float)((mTurnedAt + TURN_TIME) - mFrameTime)) / TURN_TIME;
			fraction = ImageUtil.smoothstep(fraction);
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
				onImageSizeChanged();
			}
		}
		if(mTargetInfoBarHeight != mInfoBarHeight){
			if(mFrameTime < mFullscreenAt + FULLSCREEN_TIME){
				float fraction = 1.0f - ((float)((mFullscreenAt + FULLSCREEN_TIME) - mFrameTime)) / FULLSCREEN_TIME;
				fraction = ImageUtil.smoothstep(fraction);
				mInfoBarHeight = (mTargetInfoBarHeight - mPreviousInfoBarHeight) * fraction + mPreviousInfoBarHeight;
				mFocusedHeight = calcFocusedHeight(mHeight, mHeightPixels);
				mFill = (mTargetFill - mPreviousFill) * fraction + mPreviousFill;
				Log.v("Display", "Fullscreen fraction: " + fraction);
			}
			else{
				mInfoBarHeight = mTargetInfoBarHeight;
				mFocusedHeight = calcFocusedHeight(mHeight, mHeightPixels);
				mFill = mTargetFill;
				onImageSizeChanged();
			}
		}
	}
	
	private void onImageSizeChanged(){
		// Make sure we're not transitioning
		if(!mTurning && mInfoBarHeight == mTargetInfoBarHeight){
			for(ImageSizeChanged listener : mImageSizeChangedListeners){
				listener.imageSizeChanged();
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
	private float					mPreviousInfoBarHeight;
	private float					mPreviousFill;
	
	// Target (After rotation)
	private float					mTargetWidth;
	private float					mTargetHeight;
	private int 					mTargetWidthPixels;
	private int 					mTargetHeightPixels;
	private float  					mTargetFocusedHeight;
	private float					mTargetRotation;
	private float					mTargetInfoBarHeight = INFOBAR_HEIGHT;
	private float					mTargetFill = NORMAL_FILL;
	
	// Current
	private float					mWidth;
	private float					mHeight;
	private int 					mWidthPixels;
	private int 					mHeightPixels;
	private float  					mFocusedHeight;
	private float					mRotation;
	private float					mInfoBarHeight = INFOBAR_HEIGHT;
	private float					mFill = NORMAL_FILL;
	
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
	public int getTargetWidthPixels(){
		return mTargetWidthPixels;
	}
	public int getTargetHeightPixels(){
		return mTargetHeightPixels;
	}
	public float getFocusedHeight(){
		return mFocusedHeight;
	}
	public float getRotation(){
		return mRotation;
	}
	public float getInfoBarHeight(){
		return mInfoBarHeight;
	}
	public float getFill(){
		return mFill;
	}
	
	public Display(Settings settings){
		this.mSettings = settings;
		this.mOrientation = Surface.ROTATION_0;
	}
	
	public void onSurfaceChanged(int width, int height){
		Log.v("Display", "Display surface changed!");
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
		
		if(mOrientation != -1){
			setOrientation(mOrientation);
		}
		
		if(mSettings.fullscreen){
			if(!mFullscreen){
				toggleFullscreen();
			}
		}
	}
	
	@Override
	public void setOrientation(int orientation) {
		Log.v("dk.nindroid.rss.RiverRenderer", "Orientation change received: " + orientation);
		if(true){
			mTurnedAt = mFrameTime;
			mOrientation = orientation;
			mPreviousWidth = mWidth;
			mPreviousHeight = mHeight;
			mPreviousWidthPixels  = mWidthPixels;
			mPreviousHeightPixels = mHeightPixels;
			mPreviousFocusedHeight = mFocusedHeight;
			mPreviousRotation = mRotation;
			if(orientation == Surface.ROTATION_0){
				mTargetWidth = mPortraitWidth;
				mTargetHeight = mPortraitHeight;
				mTargetWidthPixels  = mPortraitWidthPixels;
				mTargetHeightPixels = mPortraitHeightPixels;
				mTargetFocusedHeight = calcFocusedHeight(mPortraitHeight, mPortraitHeightPixels);
				mTargetRotation = 0;
			}else if(orientation == Surface.ROTATION_270){
				mTargetWidth = mPortraitHeight;
				mTargetHeight = mPortraitWidth;
				mTargetWidthPixels  = mPortraitHeightPixels;
				mTargetHeightPixels = mPortraitWidthPixels;
				mTargetFocusedHeight = calcFocusedHeight(mPortraitWidth, mPortraitWidthPixels);
				mTargetRotation = -90;
				if(mRotation > 90){
					mRotation -= 360;
					mPreviousRotation = mRotation;
				}
			}else if(orientation == Surface.ROTATION_90){
				mTargetWidth = mPortraitHeight;
				mTargetHeight = mPortraitWidth;
				mTargetWidthPixels  = mPortraitHeightPixels;
				mTargetHeightPixels = mPortraitWidthPixels;
				mTargetFocusedHeight = calcFocusedHeight(mPortraitWidth, mPortraitWidthPixels);
				mTargetRotation = 90;
			}else if(orientation == Surface.ROTATION_180){
				mTargetWidth = mPortraitWidth;
				mTargetHeight = mPortraitHeight;
				mTargetWidthPixels  = mPortraitWidthPixels;
				mTargetHeightPixels = mPortraitHeightPixels;
				mTargetFocusedHeight = calcFocusedHeight(mPortraitHeight, mPortraitHeightPixels);
				mTargetRotation = 180;
				if(mRotation < 0){
					mRotation += 360;
					mPreviousRotation = mRotation;
				}
			}
			
			mTurning = true;
 		}
	}
	
	private float calcFocusedHeight(float height, int heightPixels){
		return height - mInfoBarHeight / heightPixels * height;
	}
	
	public static interface ImageSizeChanged{
		void imageSizeChanged();
	}
}
