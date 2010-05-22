package dk.nindroid.rss.renderers.floating;

import java.util.Arrays;

import javax.microedition.khronos.opengles.GL10;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import dk.nindroid.rss.R;
import dk.nindroid.rss.RiverRenderer;
import dk.nindroid.rss.ShowStreams;
import dk.nindroid.rss.TextureBank;
import dk.nindroid.rss.TextureSelector;
import dk.nindroid.rss.data.ImageReference;
import dk.nindroid.rss.data.Ray;
import dk.nindroid.rss.gfx.Vec3f;
import dk.nindroid.rss.helpers.MatrixTrackingGL;
import dk.nindroid.rss.renderers.Dimmer;
import dk.nindroid.rss.renderers.OSD;
import dk.nindroid.rss.renderers.Renderer;
import dk.nindroid.rss.renderers.floating.Image.Pos;
import dk.nindroid.rss.settings.Settings;

public class FloatingRenderer implements Renderer {
	public static final long 	mFocusDuration = 300;
	public static long			mSelectedTime;
	public static final float  	mFocusX = 0.0f;
	public static final float  	mFocusY = 0.0f;
	public static final float 	mFocusZ = -1.0f;
	public static final float  	mFloatZ = -3.5f;
	public static final float  	mJitterX = 0.8f;
	public static final float  	mJitterY = 0.5f;
	public static final float  	mJitterZ = 1.5f;
	private static final long	SPLASHTIME = 2000l;
	
	private boolean 		mNewStart = true;
	private Image[] 		mImgs;
	private TextureBank 	mBank;
	public static TextureSelector mTextureSelector;
	private long 			mTraversal = 30000;
	private long 			mInterval;
	private int 			mTotalImgRows = 6;
	private int 			mImgCnt = 0;
	private boolean 		mCreateMiddle = true;
	private boolean			mDoUnselect = false;
	
	
	private static final Vec3f mCamPos = new Vec3f(0,0,0);
	private Image			mSelected = null;
	private int				mSelectedIndex;
	private Image			mSplashImg;
	private long			mDefocusSplashTime;
	private boolean			mSelectingNext;
	private boolean			mSelectingPrev;
	private ImageDepthComparator mDepthComparator;
	
	public FloatingRenderer(TextureBank bank){
		mTextureSelector = new TextureSelector();
		this.mBank = bank;
		mImgs = new Image[mTotalImgRows * 3 / 2];
		mInterval = mTraversal / mTotalImgRows;
		long curTime = System.currentTimeMillis();
		long creationOffset = 0;
		for(int i = 0; i < mTotalImgRows; ++i){
			
			if(mCreateMiddle){
	        	mImgs[mImgCnt++] = new Image(mBank, mTraversal, Pos.MIDDLE, curTime - creationOffset);
	        }else{
	        	mImgs[mImgCnt++] = new Image(mBank, mTraversal, Pos.UP, curTime - creationOffset);
	        	mImgs[mImgCnt++] = new Image(mBank, mTraversal, Pos.DOWN, curTime - creationOffset);
	        }
	        	mCreateMiddle ^= true;
	        	creationOffset += mInterval;
		}
		mDepthComparator = new ImageDepthComparator();
		Arrays.sort(mImgs, mDepthComparator);
	}
		
	public void click(MatrixTrackingGL gl, float x, float y, long frameTime, long realTime){
		if(mSelected != null){
        	deselect(gl, frameTime, realTime);
		}else{
			// Ignore click if we're at the splash
        	if(mSplashImg == null){
	        	// Only works for camera looking directly at (0, 0, -1)...
	        	Vec3f rayDir = new Vec3f(x, y, -1);
	        	rayDir.normalize();
	        	Ray r = new Ray(mCamPos, rayDir);
	        	int i = 0;
	        	Image selected = null;
	        	float closest = Float.MAX_VALUE;
	        	float t;
	        	for(; i < mImgCnt; ++i){
	        		t = mImgs[i].intersect(r); 
	            	if(t > 0 && t < closest){
	            		closest = t;
	            		selected = mImgs[i];
	            		mSelectedIndex = i;
	            	}
	            }
	        	if(selected != null && selected.canSelect()){
	        		mSelectedTime = realTime;
	        		mSelected = selected;
	        		selected.select(gl, frameTime, realTime);
	        	}
	        }
        }
	}
	
	private void deselect(MatrixTrackingGL gl, long frameTime, long realTime){
		if(mSelected.stateInFocus()){
    		mSelectedTime = realTime;
        	mSelected.select(gl, frameTime, realTime); // Deselect!
    	}
	}
	
	public void update(MatrixTrackingGL gl, long frameTime, long realTime){
		// If new start, show splash!
		if(mNewStart){
			mSplashImg = mImgs[4];
			Bitmap splash = BitmapFactory.decodeStream(ShowStreams.current.getResources().openRawResource(R.drawable.splash));
			mSplashImg.setSelected(gl, splash, 343.0f/512.0f, 1.0f, frameTime);
			mNewStart = false;
			mDefocusSplashTime = realTime + SPLASHTIME;
		}
		if(mDoUnselect){
			mDoUnselect = false;
			deselect(gl, frameTime, realTime);
			Log.v("Floating Image", "Splash deselect");
		}
		// Defocus splash image after defined period.
        if(mSplashImg != null){
        	if(realTime > mDefocusSplashTime && mSplashImg.stateInFocus()){
	        	mSelectedTime = realTime;
	        	mSplashImg.select(gl, frameTime, realTime);
        	}
        	if(mSplashImg.stateFloating()){
        		mSplashImg = null;
        	}
        }
        // Deselect selected when it is floating.
        if(mSelected != null){
        	if(mSelected.stateInFocus()){
        		if(mSelectingNext || mSelectingPrev){
        			deselect(gl, frameTime, realTime);
        		}
        	}
        	if(mSelected.stateFloating()){
        		if(mSelectingNext || mSelectingPrev){
        			Log.v("FloatingRenderer", mSelectingNext ? "next" : "prev");
        			int imageCount = mImgs.length;
        			Log.v("FloatingRenderer", "Previous selected: " + mSelectedIndex);
        			mSelectedIndex += (mSelectingNext ? imageCount - 1 : 1);
        			mSelectedIndex %= imageCount;
        			Log.v("FloatingRenderer", "Updated  selected: " + mSelectedIndex);
        			mSelected = mImgs[mSelectedIndex];
        			mSelectedTime = realTime;
        			mSelectingNext = false;
        			mSelectingPrev = false;
        			mSelected.select(gl, frameTime, realTime);
        		}else{
        			mSelected = null;
        		}
        	}
        }
        boolean sortArray = false;
        for(int i = 0; i < mImgCnt; ++i){
        	if(mImgs[i].update(gl, frameTime, realTime)){
        		sortArray = true;
        	}
        }
        if(sortArray)
        {
        	Arrays.sort(mImgs, mDepthComparator);
        }
	}
	
	public void render(MatrixTrackingGL gl, long time, long realTime){
		gl.glDepthMask(false);
		gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
		gl.glEnable(GL10.GL_TEXTURE_2D);
		gl.glDisable(GL10.GL_DEPTH_BITS);
		gl.glDisable(GL10.GL_DEPTH_TEST);
		// Background first, this is backmost
		BackgroundPainter.draw(gl);
		
        Image.setState(gl);
        for(int i = 0; i < mImgCnt; ++i){
        	if(mImgs[i] != mSelected){
        		mImgs[i].draw(gl, time, realTime);
        	}
        }
        Image.unsetState(gl);
        
        float fraction = getFraction(realTime);
        if(mSelected != null){
        	float dark = Settings.fullscreenBlack ? RiverRenderer.mDisplay.getFill() * RiverRenderer.mDisplay.getFill() : 0.8f;
        	Dimmer.setColor(0.0f, 0.0f, 0.0f);
        	if(mSelected.stateInFocus()){
        		Dimmer.draw(gl, 1.0f, dark);
        		fraction = 1.0f;
        	}else if (mSelected.stateFocusing()){
        		Dimmer.draw(gl, fraction, dark);
        		// Fraction is right!
        	}else{
        		Dimmer.draw(gl, 1.0f - fraction, dark);
        		fraction = 1.0f - fraction;
        	}
        	if(!RiverRenderer.mDisplay.isTurning()){
        		InfoBar.setState(gl);
        		InfoBar.draw(gl, fraction);
        		InfoBar.unsetState(gl);
    		}
        	
        	gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
        	Image.setState(gl);
        	mSelected.draw(gl, time, realTime);
        	Image.unsetState(gl);
        }
	}
	
	public static float getFarRight(){
		return RiverRenderer.mDisplay.getWidth() * 0.5f * (-mFloatZ + mJitterZ) * 1.2f + 1.2f + mJitterX;
	}
	
	public static float getFraction(long realTime){
		return Math.min(((float)(realTime - mSelectedTime)) / mFocusDuration, 1.1f);
	}
	
	public void init(GL10 gl, long time, OSD osd){
		osd.setEnabled(false, true);
		for(int i = 0; i < mImgCnt; ++i){
			mImgs[i].init(gl, time);
		}
		if(mSelected != null){
			InfoBar.select(gl, mSelected.getShowing());
		}
	}
	
	@Override
	public void onPause(){
		mTextureSelector.stopThread();
	}
	
	@Override
	public void onResume(){
		mTextureSelector.startThread();
	}

	@Override
	public boolean back() {
		if(mSelected != null && mSelected.stateInFocus()){
			mDoUnselect = true;
			return true;
		}
		return false;
	}

	@Override
	public Intent followCurrent() {
		if(mSelected != null){
			return mSelected.getShowing().follow();
		}
		return null;
	}

	@Override
	public ImageReference getCurrent() {
		return mSelected != null ? mSelected.getShowing() : null;
	}

	@Override
	public boolean slideLeft(long realTime) {
		if(mSelected != null){
			mSelectingNext = true;
			mSelectingPrev = false;
			Log.v("FloatingRenderer", "Select left");
			return true;
		}
		return false;
	}

	@Override
	public boolean slideRight(long realTime) {
		if(mSelected != null){
			mSelectingNext = false;
			mSelectingPrev = true;
			Log.v("FloatingRenderer", "Select right");
			return true;
		}
		return false;
	}
	
	@Override
	public void setBackground(){
		if(mSelected != null){
			mSelected.setBackground();
		}
	}
}
