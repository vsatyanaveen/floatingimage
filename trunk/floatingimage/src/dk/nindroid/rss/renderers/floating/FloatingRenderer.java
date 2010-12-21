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
import dk.nindroid.rss.renderers.Dimmer;
import dk.nindroid.rss.renderers.OSD;
import dk.nindroid.rss.renderers.Renderer;
import dk.nindroid.rss.renderers.floating.Image.Pos;
import dk.nindroid.rss.settings.Settings;

public class FloatingRenderer extends Renderer {
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
	private Image[] 		mImgDepths;
	private TextureBank 	mBank;
	public static TextureSelector mTextureSelector;
	private long 			mInterval;
	private long			mMaxTime = 0;
	private int 			mTotalImgRows = 6;
	private int 			mImgCnt = 0;
	private boolean 		mCreateMiddle = true;
	private boolean			mDoUnselect = false;
	private boolean			mResetImages = false;
	
	
	
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
		mInterval = Settings.floatingTraversal / mTotalImgRows;
		long curTime = System.currentTimeMillis();
		long creationOffset = 0;
		for(int i = 0; i < mTotalImgRows; ++i){
			
			if(mCreateMiddle){
	        	mImgs[mImgCnt++] = new Image(mBank, Pos.MIDDLE, curTime - creationOffset);
	        }else{
	        	mImgs[mImgCnt++] = new Image(mBank, Pos.UP, curTime - creationOffset);
	        	mImgs[mImgCnt++] = new Image(mBank, Pos.DOWN, curTime - creationOffset);
	        }
	        	mCreateMiddle ^= true;
	        	creationOffset += mInterval;
		}
		mImgDepths = new Image[mImgs.length];
		for(int i = 0; i < mImgs.length; ++i){
			mImgDepths[i] = mImgs[i];
		}
		mDepthComparator = new ImageDepthComparator();
		Arrays.sort(mImgDepths, mDepthComparator);
	}
		
	public void click(GL10 gl, float x, float y, long frameTime, long realTime){
		if(mSelected != null){
			deselect(gl, frameTime, realTime);
		}else{
			// Ignore click if we're at the splash
        	if(mSplashImg == null){
	        	// Only works for camera looking directly at (0, 0, -1)...
	        	Vec3f rayDir = new Vec3f(x, y, -1);
	        	rayDir.normalize();
	        	Ray r = new Ray(mCamPos, rayDir);
	        	int i = mImgCnt - 1;
	        	Image selected = null;
	        	float closest = Float.MAX_VALUE;
	        	float t;
	        	for(; i > -1; --i){
	        		t = mImgDepths[i].intersect(r); 
	            	if(t > 0 && t < closest){
	            		closest = t;
	            		selected = mImgDepths[i];
	            		break; // Images are depth sorted.
	            	}
	            }
	        	if(selected != null && selected.canSelect()){
	        		mSelectedTime = realTime;
	        		mSelected = selected;
	        		selected.select(gl, frameTime, realTime);
	        		for(i = mImgs.length; i > 0; --i){
	        			if(mImgs[i - 1] == selected)
	        				mSelectedIndex = i - 1;
	        		}
	        	}
	        }
        }
	}
	
	private void deselect(GL10 gl, long frameTime, long realTime){
		if(!mSelected.click(realTime)){
			if(mSelected.stateInFocus()){
	    		mSelectedTime = realTime;
	        	mSelected.select(gl, frameTime, realTime); // Deselect!
	    	}
		}else{
			mSelectingNext = mSelectingPrev = false;
		}
	}
	
	@Override 
	public long editOffset(long offset, long realTime){
		long frameTime = realTime + offset;
		mMaxTime = Math.max(mMaxTime, frameTime);
		long reverseTime = mMaxTime - frameTime;
		long illegalDistance = reverseTime - Settings.floatingTraversal;
		long id = Math.max(0, illegalDistance / 500); // Same, but only positive
		return offset + id * id;
	}
	
	public void update(GL10 gl, long frameTime, long realTime){
		if(mResetImages){
			resetImages(gl, frameTime);
		}
		// If new start, show splash!
		if(mNewStart){
			mSplashImg = mImgDepths[4];
			Bitmap splash = BitmapFactory.decodeStream(ShowStreams.current.getResources().openRawResource(R.drawable.splash));
			mSplashImg.setSelected(gl, splash, 343.0f/512.0f, 1.0f, frameTime);
			mNewStart = false;
			mDefocusSplashTime = realTime + SPLASHTIME;
		}
		if(mDoUnselect){
			mDoUnselect = false;
			deselect(gl, frameTime, realTime);
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
        			int imageCount = mImgDepths.length;
        			mSelectedIndex += (mSelectingNext ? imageCount - 1 : 1);
        			mSelectedIndex %= imageCount;
        			mSelected = mImgs[mSelectedIndex];
        			mSelectedTime = realTime;
        			mSelectingNext = false;
        			mSelectingPrev = false;
        			mSelected.select(gl, frameTime, realTime);
        			Log.v("Floating Image", "SelectedIndex: " + mSelectedIndex);
        		}else{
        			mSelected = null;
        		}
        		Arrays.sort(mImgDepths, mDepthComparator);
        	}
        }
        boolean sortArray = false;
        for(int i = 0; i < mImgCnt; ++i){
        	if(mImgDepths[i].update(gl, frameTime, realTime)){
        		sortArray = true;
        	}
        }
        if(sortArray)
        {
        	Arrays.sort(mImgDepths, mDepthComparator);
        }
	}
	
	void initRender(GL10 gl){
		gl.glDepthMask(false);
		gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
		gl.glEnable(GL10.GL_TEXTURE_2D);
		gl.glDisable(GL10.GL_DEPTH_TEST);
		gl.glTexEnvx(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE, GL10.GL_REPLACE);
		gl.glFrontFace(GL10.GL_CCW);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER,GL10.GL_LINEAR);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D,GL10.GL_TEXTURE_MAG_FILTER,GL10.GL_LINEAR);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S,GL10.GL_CLAMP_TO_EDGE);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T,GL10.GL_CLAMP_TO_EDGE);
	}
	
	public void render(GL10 gl, long time, long realTime){
		initRender(gl);
		// Background first, this is backmost
		BackgroundPainter.draw(gl);
        //Image.setState(gl);
        for(int i = 0; i < mImgCnt; ++i){
        	if(mImgDepths[i] != mSelected){
        		mImgDepths[i].draw(gl, time, realTime);
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
		return RiverRenderer.mDisplay.getWidth() * 0.5f * (-mFloatZ + mJitterZ) * 1.2f + 1.3f + mJitterX;
	}
	
	public static float getFraction(long realTime){
		return Math.min(((float)(realTime - mSelectedTime)) / mFocusDuration, 1.1f);
	}
	
	public void init(GL10 gl, long time, OSD osd){
		osd.setEnabled(false, true, true);
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
			Log.v("Floating Image", "SlideLeft");
			return true;
		}
		return false;
	}

	@Override
	public boolean slideRight(long realTime) {
		if(mSelected != null){
			mSelectingNext = false;
			mSelectingPrev = true;
			Log.v("Floating Image", "SlideRight");
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
	
	@Override
	public void resetImages() {
		mResetImages = true;
	}
	
	public void resetImages(GL10 gl, long time) {
		mResetImages = false;
		mBank.reset();
		for(Image i : mImgs){
			i.reset(gl, time);
		}	
	}

	@Override
	public void transform(float centerX, float centerY, float x, float y, float rotate, float scale) {
		if(mSelected != null){
			mSelected.transform(centerX, centerY, x, y, rotate, scale);
		}
	}
	
	@Override
	public void initTransform() {
		if(mSelected != null){
			mSelected.initTransform();
		}
	}

	@Override
	public boolean freeMove() {
		if(mSelected != null){
			return mSelected.freeMove();
		}
		return false;
	}

	@Override
	public void move(float x, float y) {
		if(mSelected != null){
			mSelected.move(x, y);
		}
	}

	@Override
	public void transformEnd() {
		if(mSelected != null){
			mSelected.transformEnd();
		}
	}
}
