package dk.nindroid.rss.renderers.floating;

import java.util.Arrays;

import javax.microedition.khronos.opengles.GL10;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import dk.nindroid.rss.Display;
import dk.nindroid.rss.MainActivity;
import dk.nindroid.rss.R;
import dk.nindroid.rss.TextureBank;
import dk.nindroid.rss.TextureSelector;
import dk.nindroid.rss.data.ImageReference;
import dk.nindroid.rss.data.Ray;
import dk.nindroid.rss.data.Texture;
import dk.nindroid.rss.gfx.Vec3f;
import dk.nindroid.rss.renderers.Dimmer;
import dk.nindroid.rss.renderers.OSD;
import dk.nindroid.rss.renderers.Renderer;
import dk.nindroid.rss.renderers.floating.positionControllers.FloatDown;
import dk.nindroid.rss.renderers.floating.positionControllers.FloatLeft;
import dk.nindroid.rss.renderers.floating.positionControllers.FloatRight;
import dk.nindroid.rss.renderers.floating.positionControllers.FloatUp;
import dk.nindroid.rss.renderers.floating.positionControllers.StarSpeed;
import dk.nindroid.rss.renderers.floating.positionControllers.TableTop;

public class FloatingRenderer extends Renderer {
	public static final long 	mFocusDuration = 300;
	public static long			mSelectedTime;
	public static final float  	mFocusX = 0.0f;
	public static final float  	mFocusY = 0.0f;
	public static final float 	mFocusZ = -1.0f;
	public static final float  	mJitterX = 0.8f;
	public static final float  	mJitterY = 0.5f;
	public static final float  	mJitterZ = 1.5f;
	private static final long	SPLASHTIME = 2000l;
	
	public static final int		FLOATING_TYPE_LEFT = 0;
	public static final int		FLOATING_TYPE_RIGHT = 1;
	public static final int		FLOATING_TYPE_DOWN = 2;
	public static final int		FLOATING_TYPE_UP = 3;
	public static final int		FLOATING_TYPE_STARSPEED = 4;
	public static final int		FLOATING_TYPE_TABLETOP = 5;
	
	private boolean 		mNewStart = true;
	private Image[] 		mImgs;
	private Image[] 		mImgDepths;
	private TextureBank 	mBank;
	public 	TextureSelector mTextureSelector;
	private long			mStartTime;
	private int 			mTotalImgRows = 6;
	private int 			mImgCnt = 0;
	private boolean			mDoUnselect = false;
	private boolean			mResetImages = false;
	private Display 		mDisplay;
	private MainActivity	mActivity;
	private InfoBar			mInfoBar;
	private BackgroundPainter mBackgroundPainter;
	private float			mStreamRotation;
	private float			mRequestedStreamRotation;
	private float			mStreamOffsetX;
	private float			mStreamOffsetY;
	private float			mStreamOffsetZ;
	private Vec3f			mRequestedStreamOffset;
	private long			mUpTime;
	
	private static final Vec3f 		mCamPos = new Vec3f(0,0,0);
	private Image					mSelected = null;
	private int						mSelectedIndex;
	private Image					mSplashImg;
	private long					mDefocusSplashTime;
	private boolean					mSelectingNext;
	private boolean					mSelectingPrev;
	private ImageDepthComparator 	mDepthComparator;
	private boolean			mDoAdjustImagePositions = false;
	
	public FloatingRenderer(MainActivity activity, TextureBank bank, Display display){
		this.mActivity = activity;
		mTextureSelector = new TextureSelector(display);
		mRequestedStreamOffset = new Vec3f();
		mInfoBar = new InfoBar();
		mBackgroundPainter = new BackgroundPainter();
		this.mDisplay = display;
		this.mBank = bank;
		mImgs = new Image[mTotalImgRows * 3 / 2];
		long interval = activity.getSettings().floatingTraversal / mTotalImgRows;
		Texture largeTexture = new Texture();
		long curTime = System.currentTimeMillis();
		long creationOffset = 0;
		boolean createMiddle = true;
		for(int i = 0; i < mTotalImgRows; ++i){
			if(createMiddle){
	        	mImgs[mImgCnt++] = new Image(activity, mBank, display, mInfoBar, largeTexture, mTextureSelector, curTime - creationOffset);
	        }else{
	        	mImgs[mImgCnt++] = new Image(activity, mBank, display, mInfoBar, largeTexture, mTextureSelector, curTime - creationOffset);
	        	mImgs[mImgCnt++] = new Image(activity, mBank, display, mInfoBar, largeTexture, mTextureSelector, curTime - creationOffset);
	        }
	        	createMiddle ^= true;
	        	creationOffset += interval;
		}
		setPositionController(mActivity.getSettings().floatingType);
		mImgDepths = new Image[mImgs.length];
		for(int i = 0; i < mImgs.length; ++i){
			mImgDepths[i] = mImgs[i];
		}
		mDepthComparator = new ImageDepthComparator();
		Arrays.sort(mImgDepths, mDepthComparator);
		mStartTime = System.currentTimeMillis();
	}
	
	public void setPositionController(int type){
		switch(type){
		case FLOATING_TYPE_LEFT:
			for(int i = 0; i < mImgs.length; ++i){
				mImgs[i].setPositionController(new FloatLeft(mActivity, mDisplay, i));
			}
		break;
		case FLOATING_TYPE_RIGHT:
			for(int i = 0; i < mImgs.length; ++i){
				mImgs[i].setPositionController(new FloatRight(mActivity, mDisplay, i));
			}
		break;
		case FLOATING_TYPE_DOWN:
			for(int i = 0; i < mImgs.length; ++i){
				mImgs[i].setPositionController(new FloatDown(mActivity, mDisplay, i));
			}
			break;
		case FLOATING_TYPE_UP:
			for(int i = 0; i < mImgs.length; ++i){
				mImgs[i].setPositionController(new FloatUp(mActivity, mDisplay, i));
			}
			break;
		case FLOATING_TYPE_STARSPEED:
			for(int i = 0; i < mImgs.length; ++i){
				mImgs[i].setPositionController(new StarSpeed(mActivity, mDisplay, i));
			}
			break;
		case FLOATING_TYPE_TABLETOP:
			for(int i = 0; i < mImgs.length; ++i){
				mImgs[i].setPositionController(new TableTop(mActivity, mDisplay, i));
			}
			break;
		}
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
		long illegalDistance = Math.max(0, mStartTime - (realTime + offset));
		long id = illegalDistance / 300;
		return offset + id * id;
	}
	
	public void update(GL10 gl, long frameTime, long realTime){
		if(mResetImages){
			resetImages(gl, frameTime);
		}
		if(mDoAdjustImagePositions){
			adjustImagePositions(frameTime); // If image speed has changed, image positions need to be reset!
			mDoAdjustImagePositions = false;
		}
		// If new start, show splash!
		if(mNewStart){
			mSplashImg = mImgDepths[4];
			Bitmap splash = BitmapFactory.decodeStream(mActivity.context().getResources().openRawResource(R.drawable.splash));
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
        //updateRotation(realTime);
        updateTranslation(realTime);
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
		mBackgroundPainter.draw(gl, mDisplay, mActivity.getSettings().backgroundColor);
        //Image.setState(gl);
		gl.glPushMatrix();
		gl.glTranslatef(mStreamOffsetX, mStreamOffsetY, mStreamOffsetZ);
		gl.glRotatef(mStreamRotation, 0.0f, 1.0f, 0.0f);
        for(int i = 0; i < mImgCnt; ++i){
        	if(mImgDepths[i] != mSelected){
        		mImgDepths[i].draw(gl, time, realTime);
        	}
        }
        gl.glPopMatrix();
        Image.unsetState(gl);
        float fraction = getFraction(realTime);
        if(mSelected != null){
        	float dark = mActivity.getSettings().fullscreenBlack ? mDisplay.getFill() * mDisplay.getFill() : 0.8f;
        	Dimmer.setColor(0.0f, 0.0f, 0.0f);
        	if(mSelected.stateInFocus()){
        		Dimmer.draw(gl, 1.0f, dark, mDisplay);
        		fraction = 1.0f;
        	}else if (mSelected.stateFocusing()){
        		Dimmer.draw(gl, fraction, dark, mDisplay);
        		// Fraction is right!
        	}else{
        		Dimmer.draw(gl, 1.0f - fraction, dark, mDisplay);
        		fraction = 1.0f - fraction;
        	}
        	if(!mDisplay.isTurning()){
        		mInfoBar.setState(gl);
        		mInfoBar.draw(gl, mDisplay, fraction);
        		mInfoBar.unsetState(gl);
    		}
        	
        	gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
        	Image.setState(gl);
        	mSelected.draw(gl, time, realTime);
        	Image.unsetState(gl);
        }
	}
	
	public void updateRotation(long realTime){
		float timeFactor = (4000 - (realTime - mUpTime)) / 4000.0f;
		if(timeFactor > 0){
			float damping = timeFactor * timeFactor;
			mStreamRotation = mRequestedStreamRotation * damping;
		}else{
			mRequestedStreamRotation = 0;
		}
	}
	
	public void updateTranslation(long realTime){
		float timeFactor = (2000 - (realTime - mUpTime)) / 2000.0f;
		if(timeFactor > 0){
			float damping = timeFactor * timeFactor;
			mStreamOffsetX = mRequestedStreamOffset.getX() * damping;
			mStreamOffsetY = mRequestedStreamOffset.getY() * damping;
			mStreamOffsetZ = mRequestedStreamOffset.getZ() * damping;
		}else{
			mRequestedStreamOffset.setX(0);
			mRequestedStreamOffset.setY(0);
			mRequestedStreamOffset.setZ(0);
		}
	}
	
	public static float getFraction(long realTime){
		return Math.min(((float)(realTime - mSelectedTime)) / mFocusDuration, 1.1f);
	}
	
	public void init(GL10 gl, long time, OSD osd){
		mBackgroundPainter.initTexture(gl, mActivity.context(), mActivity.getSettings().backgroundColor);
		osd.setEnabled(false, true, true);
		for(int i = 0; i < mImgCnt; ++i){
			mImgs[i].init(gl, time);
		}
		if(mSelected != null){
			mInfoBar.select(gl, mDisplay, mSelected.getShowing());
		}
	}
	
	@Override
	public void onPause(){
		mTextureSelector.stopThread();
	}
	
	@Override
	public void onResume(){
		setPositionController(mActivity.getSettings().floatingType);
		mTextureSelector.startThread();
		mDoAdjustImagePositions = true;
	}
	
	void adjustImagePositions(long time){
		long interval = mActivity.getSettings().floatingTraversal / mTotalImgRows;
		long prevNewStartTime = mImgs[0].getStartTime();
		long prevOrgStartTime = mImgs[0].getStartTime();
		for(Image img : mImgs){
			long prevOrg = prevOrgStartTime;
			prevOrgStartTime = img.getStartTime();
			if(img.getStartTime() != prevOrg){
				img.setStartTime(prevNewStartTime - interval, time);
			}
			else{
				img.setStartTime(prevNewStartTime, time);
			}
			prevNewStartTime = img.getStartTime();
		}
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

	@Override
	public int totalImages() {
		return mImgs.length;
	}
	
	Vec3f tmpVar = new Vec3f();
	
	@Override
	public void streamMoved(float x, float y) {
		mUpTime = System.currentTimeMillis();
		mRequestedStreamOffset.setX(mStreamOffsetX);
		mRequestedStreamOffset.setY(mStreamOffsetY);
		mRequestedStreamOffset.setZ(mStreamOffsetZ);
		tmpVar.setX(0);
		tmpVar.setY(0);
		tmpVar.setZ(0);
		mImgs[0].getPositionController().getGlobalOffset(x, y, tmpVar);
		mRequestedStreamOffset.minus(tmpVar, mRequestedStreamOffset);
		mRequestedStreamOffset.setX(Math.max(Math.min(1.0f, mRequestedStreamOffset.getX()), -1.0f));
		mRequestedStreamOffset.setY(Math.max(Math.min(1.0f, mRequestedStreamOffset.getY()), -1.0f));
		mRequestedStreamOffset.setZ(Math.max(Math.min(1.0f, mRequestedStreamOffset.getZ()), -1.0f));
	}
	
	public void wallpaperMove(float fraction){
		mStreamRotation = -fraction * 10.0f;
		mStreamOffsetX = -fraction * 1.0f;
		/*
		mUpTime = System.currentTimeMillis();
		mRequestedStreamRotation = mStreamRotation;
		mRequestedStreamRotation += x / 50.0f;
		mRequestedStreamRotation = Math.max(Math.min(15.0f, mRequestedStreamRotation), -15.0f);
		*/
	}

	@Override
	public float adjustOffset(float speedX, float speedY) {
		return mImgs[0].getPositionController().getTimeAdjustment(speedX, speedY);
	}

	@Override
	public void deleteCurrent() {
		if(this.mSelected != null){
			this.mSelected.delete();
			this.mDoUnselect = true;
		}
	}
}
