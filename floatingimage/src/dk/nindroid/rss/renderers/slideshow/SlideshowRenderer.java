package dk.nindroid.rss.renderers.slideshow;

import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.WindowManager;
import dk.nindroid.rss.Display;
import dk.nindroid.rss.MainActivity;
import dk.nindroid.rss.TextureBank;
import dk.nindroid.rss.data.ImageReference;
import dk.nindroid.rss.gfx.Vec3f;
import dk.nindroid.rss.renderers.Clock;
import dk.nindroid.rss.renderers.OSD;
import dk.nindroid.rss.renderers.ProgressBar;
import dk.nindroid.rss.renderers.Renderer;
import dk.nindroid.rss.renderers.osd.PlayPauseEventHandler;
import dk.nindroid.rss.renderers.slideshow.transitions.CrossFade;
import dk.nindroid.rss.renderers.slideshow.transitions.FadeToBlack;
import dk.nindroid.rss.renderers.slideshow.transitions.FadeToWhite;
import dk.nindroid.rss.renderers.slideshow.transitions.Instant;
import dk.nindroid.rss.renderers.slideshow.transitions.Random;
import dk.nindroid.rss.renderers.slideshow.transitions.SlideRightToLeft;
import dk.nindroid.rss.renderers.slideshow.transitions.SlideTopToBottom;
import dk.nindroid.rss.renderers.slideshow.transitions.Transition;
import dk.nindroid.rss.settings.Settings;

public class SlideshowRenderer extends Renderer implements PlayPauseEventHandler {
	Image 			mPrevious, mCurrent, mNext;
	TextureBank 	mBank;
	long			mSlideTime;
	Transition 		mCurrentTransition;
	Display 		mDisplay;
	boolean			mPlaying = true;
	boolean			mStartPlaying = false;
	boolean			mNextSet = false;
	boolean			mPreviousSet = false;
	boolean			mResetImages = false;
	boolean			mMoveBack = false;
	MainActivity	mActivity;
	Clock			mClock;
	
	public SlideshowRenderer(MainActivity activity, TextureBank bank, Display display){
		this.mDisplay = display;
		this.mActivity = activity;
		WindowManager wm = (WindowManager)activity.context().getSystemService(Context.WINDOW_SERVICE);
		android.view.Display disp = wm.getDefaultDisplay();
		int maxSide = Math.max(disp.getWidth(), disp.getHeight());
		mClock = new Clock(activity.context(), maxSide);
		mPrevious = new Image(display, activity);
		mCurrent = new Image(display, activity);
		mNext = new Image(display, activity);
		this.mBank = bank;
	}
	
	protected void setTransition(int mode){
		switch(mode){
		case Settings.MODE_CROSSFADE:
			mCurrentTransition = new CrossFade();
			break;
		case Settings.MODE_FADE_TO_BLACK:
			mCurrentTransition = new FadeToBlack(mDisplay);
			break;
		case Settings.MODE_FADE_TO_WHITE:
			mCurrentTransition = new FadeToWhite(mDisplay);
			break;
		case Settings.MODE_NONE:
			mCurrentTransition = new Instant();
			break;
		case Settings.MODE_RANDOM:
			mCurrentTransition = new Random(mDisplay);
			break;
		case Settings.MODE_SLIDE_RIGHT_TO_LEFT:
			mCurrentTransition = new SlideRightToLeft(mDisplay);
			break;
		case Settings.MODE_SLIDE_TOP_TO_BOTTOM:
			mCurrentTransition = new SlideTopToBottom(mDisplay);
			break;
		default:
			mCurrentTransition = new Random(mDisplay); // This should never happen, but better have a safe fallback!
			break;
		}
		Log.v("Slideshow renderer", "Using mode: " + mode);
	}
	
	@Override
	public void onPause(){
		mPrevious.onPause();
		mCurrent.onPause();
		mNext.onPause();
		mPrevious.clear();
		mCurrent.clear();
		mNext.clear();
	}
	
	@Override
	public void onResume(){
		setTransition(this. mActivity.getSettings().mode);
		if(!mDisplay.isFullscreen()){
			mDisplay.toggleFullscreen();
		}
		mPrevious.onResume();
		mCurrent.onResume();
		mNext.onResume();
	}
	
	@Override
	public boolean back() {
		// Function not defined.
		return false;
	}

	@Override
	public boolean click(GL10 gl, float x, float y, long frameTime,
			long realTime) {
		return false;
	}
	
	@Override
	public boolean doubleClick(GL10 gl, float x, float y, long frameTime,
			long realTime) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Intent followCurrent() {
		return mCurrent.getImage().follow();
	}

	@Override
	public ImageReference getCurrent() {
		return mCurrent.getImage();
	}

	@Override
	public void init(GL10 gl, long time, OSD osd) {
		osd.setEnabled(true, false, false, false, true, true, true);
		osd.registerPlayListener(this);
		mPrevious.init(gl, time);
		mCurrent.init(gl, time);
		mNext.init(gl, time);
		mPrevious.setPos(new Vec3f(-20.0f, 0.0f, -1.0f));
		mCurrent.setPos(new Vec3f(0.0f, 0.0f, -1.0f));
		mNext.setPos(new Vec3f(20.0f, 0.0f, -1.0f));
		mClock.resume(gl);
	}

	@Override
	public void render(GL10 gl, long frameTime, long realtime) {
		gl.glDisable(GL10.GL_DEPTH_TEST);
		gl.glDepthMask(false);
		/*
		gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
		gl.glEnable(GL10.GL_TEXTURE_2D);
		gl.glTexEnvx(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE, GL10.GL_REPLACE);
		gl.glFrontFace(GL10.GL_CCW);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER,GL10.GL_LINEAR);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D,GL10.GL_TEXTURE_MAG_FILTER,GL10.GL_LINEAR);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S,GL10.GL_CLAMP_TO_EDGE);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T,GL10.GL_CLAMP_TO_EDGE);
		*/
		
		if(!mCurrentTransition.isFinished()){
			mCurrentTransition.preRender(gl, frameTime);
		}
		
		if(mCurrentTransition.isReverse()){
			mNext.render(gl, realtime);
			mCurrent.render(gl, realtime);
		}else{
			mPrevious.render(gl, realtime);
			mCurrent.render(gl, realtime);
		}
		
		
		if(!mCurrent.hasBitmap()){
			ProgressBar.draw(gl, mCurrent.getProgress(), mDisplay);
		}
		if(!mCurrentTransition.isFinished()){
			mCurrentTransition.postRender(gl, frameTime);
		}
		
		mClock.update(gl, mDisplay, mActivity.getSettings());
	}

	@Override
	public void update(GL10 gl, long time, long realTime) {
		long timeSinceSlide = realTime - mSlideTime;
		if(timeSinceSlide > mActivity.getSettings().slideSpeed && (timeSinceSlide < mActivity.getSettings().slideshowInterval - 40 || !mPlaying)){
			try {
				Thread.sleep(40);
			} catch (InterruptedException e) {}
		}
		
		if (mResetImages){
			resetImages(gl);
		}
		if(mStartPlaying){
			mSlideTime = realTime;
			mStartPlaying = false;
		}
		updateNext(gl, realTime);
		// Read first image to be shown
		if(mCurrent.getImage() == null){
			mCurrent.setImage(gl, mBank.getTexture(null, true));
			mSlideTime = realTime;
		}else if(mCurrent.hasBitmap() && mNext.getImage() == null){ // Load next image when first is done.
				mNext.setImage(gl, mBank.getTexture(null, true));
		}// Continue with normal slideshow
		else if(timeSinceSlide > mActivity.getSettings().slideshowInterval + mActivity.getSettings().slideSpeed && mPlaying){
			if(mMoveBack){
				previous(gl, realTime);
				mMoveBack = false;
			}else{
				next(gl, realTime);
			}
		}
		
		if(!mCurrentTransition.isFinished()){
			mCurrentTransition.update(realTime);
		}
	}
	
	private void next(GL10 gl, long realTime){
		Image temp = mCurrent;
		mCurrent = mNext;
		mNext = mPrevious;
		mPrevious = temp;
		mNextSet = false;
		mSlideTime = realTime;
		mCurrentTransition.init(mPrevious, mCurrent, realTime, mActivity.getSettings().slideSpeed, false);
	}
	
	private void previous(GL10 gl, long realTime){
		Image temp = mCurrent;
		mCurrent = mPrevious;
		mPrevious = mNext;
		mNext = temp;
		mPreviousSet = false;
		mSlideTime = realTime;
		mCurrentTransition.init(mCurrent, mNext, realTime, mActivity.getSettings().slideSpeed, true);
	}
	
	private void updateNext(GL10 gl, long realTime){
		if(realTime - mSlideTime > mActivity.getSettings().slideSpeed){
			if(!mNextSet){
				ImageReference oldImage = mNext.getImage();
				mNext.clear(); // Clean slate
				mNext.setImage(gl, mBank.getTexture(oldImage, true));
				mNextSet = true;
			}else if(!mPreviousSet){
				ImageReference oldImage = mPrevious.getImage();
				mPrevious.clear();
				mPrevious.setImage(gl, mBank.getTexture(oldImage, false));
				mPreviousSet = true;
			}
		}
	}
	
	@Override
	public boolean slideLeft(long realTime) {
		mSlideTime = 0;
		return true;
	}

	@Override
	public boolean slideRight(long realTime) {
		mSlideTime = 0;
		mMoveBack = true;
		return true;
	}
	
	@Override
	public void setBackground() {
		mCurrent.setBackground();
	}

	@Override
	public void Pause() {
		mPlaying = false;
	}

	@Override
	public void Play() {
		mStartPlaying = true;
		mPlaying = true;
	}
	
	@Override
	public void resetImages() {
		mResetImages = true;
	}
	
	public void resetImages(GL10 gl) {
		mResetImages = false;
		mBank.reset();
		mPrevious.clear();
		mCurrent.clear();
		mNext.clear();
	}

	@Override
	public void initTransform() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void transform(float centerX, float centerY, float x, float y, float rotate, float scale) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean freeMove() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void move(float x, float y) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void transformEnd() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int totalImages() {
		return 3;
	}

	@Override
	public void streamMoved(float x, float y) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void wallpaperMove(float fraction) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public float adjustOffset(float speedX, float speedY) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void deleteCurrent() {
		this.slideRight(System.currentTimeMillis());
	}
}
