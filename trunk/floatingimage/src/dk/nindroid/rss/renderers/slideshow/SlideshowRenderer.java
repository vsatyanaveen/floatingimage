package dk.nindroid.rss.renderers.slideshow;

import javax.microedition.khronos.opengles.GL10;

import android.content.Intent;
import android.util.Log;
import dk.nindroid.rss.RiverRenderer;
import dk.nindroid.rss.TextureBank;
import dk.nindroid.rss.data.ImageReference;
import dk.nindroid.rss.gfx.Vec3f;
import dk.nindroid.rss.helpers.MatrixTrackingGL;
import dk.nindroid.rss.renderers.OSD;
import dk.nindroid.rss.renderers.ProgressBar;
import dk.nindroid.rss.renderers.Renderer;
import dk.nindroid.rss.renderers.slideshow.Image;
import dk.nindroid.rss.renderers.slideshow.transitions.CrossFade;
import dk.nindroid.rss.renderers.slideshow.transitions.FadeToBlack;
import dk.nindroid.rss.renderers.slideshow.transitions.FadeToWhite;
import dk.nindroid.rss.renderers.slideshow.transitions.Instant;
import dk.nindroid.rss.renderers.slideshow.transitions.Random;
import dk.nindroid.rss.renderers.slideshow.transitions.SlideRightToLeft;
import dk.nindroid.rss.renderers.slideshow.transitions.SlideTopToBottom;
import dk.nindroid.rss.renderers.slideshow.transitions.Transition;
import dk.nindroid.rss.settings.Settings;

public class SlideshowRenderer extends Renderer implements dk.nindroid.rss.renderers.osd.Play.EventHandler {
	Image 			mPrevious, mCurrent, mNext;
	TextureBank 	mBank;
	long			mSlideTime;
	Transition 		mCurrentTransition;
	boolean			mPlaying = true;
	boolean			mStartPlaying = false;
	boolean			mNextSet = false;
	boolean			mResetImages = false;
	
	public SlideshowRenderer(TextureBank bank){
		mPrevious = new Image();
		mCurrent = new Image();
		mNext = new Image();
		this.mBank = bank;
	}
	
	protected void setTransition(int mode){
		switch(mode){
		case Settings.MODE_CROSSFADE:
			mCurrentTransition = new CrossFade();
			break;
		case Settings.MODE_FADE_TO_BLACK:
			mCurrentTransition = new FadeToBlack();
			break;
		case Settings.MODE_FADE_TO_WHITE:
			mCurrentTransition = new FadeToWhite();
			break;
		case Settings.MODE_NONE:
			mCurrentTransition = new Instant();
			break;
		case Settings.MODE_RANDOM:
			mCurrentTransition = new Random();
			break;
		case Settings.MODE_SLIDE_RIGHT_TO_LEFT:
			mCurrentTransition = new SlideRightToLeft();
			break;
		case Settings.MODE_SLIDE_TOP_TO_BOTTOM:
			mCurrentTransition = new SlideTopToBottom();
			break;
		default:
			mCurrentTransition = new Random(); // This should never happen, but better have a safe fallback!
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
		setTransition(Settings.mode);
		if(!RiverRenderer.mDisplay.isFullscreen()){
			RiverRenderer.mDisplay.toggleFullscreen();
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
	public void click(MatrixTrackingGL gl, float x, float y, long frameTime,
			long realTime) {
		// TODO Auto-generated method stub

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
		osd.setEnabled(true, false, true);
		osd.registerPlayListener(this);
		mPrevious.init(gl, time);
		mCurrent.init(gl, time);
		mNext.init(gl, time);
		mPrevious.setPos(new Vec3f(-20.0f, 0.0f, -1.0f));
		mCurrent.setPos(new Vec3f(0.0f, 0.0f, -1.0f));
		mNext.setPos(new Vec3f(20.0f, 0.0f, -1.0f));
	}

	@Override
	public void render(MatrixTrackingGL gl, long frameTime, long realtime) {
		gl.glDisable(GL10.GL_DEPTH_TEST);
		gl.glDepthMask(false);
		if(!mCurrentTransition.isFinished()){
			mCurrentTransition.preRender(gl, frameTime);
		}
		mPrevious.render(gl, realtime);
		mCurrent.render(gl, realtime);
		if(!mCurrent.hasBitmap()){
			ProgressBar.draw(gl, mCurrent.getProgress());
		}
		if(!mCurrentTransition.isFinished()){
			mCurrentTransition.postRender(gl, frameTime);
		}
		gl.glDepthMask(true);
		gl.glEnable(GL10.GL_DEPTH_TEST);
	}

	@Override
	public void update(MatrixTrackingGL gl, long time, long realTime) {
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
			mCurrent.setImage(gl, mBank.getTexture(null));
			mSlideTime = realTime;
		}else if(mCurrent.hasBitmap() && mNext.getImage() == null){ // Load next image when first is done.
				mNext.setImage(gl, mBank.getTexture(null));
		}// Continue with normal slideshow
		else if(realTime - mSlideTime > Settings.slideshowInterval + Settings.slideSpeed && mPlaying){
			next(gl, realTime);
		}
		
		if(!mCurrentTransition.isFinished()){
			mCurrentTransition.update(realTime);
		}
	}
	
	private void next(MatrixTrackingGL gl, long realTime){
		Image temp = mCurrent;
		mCurrent = mNext;
		mNext = mPrevious;
		mPrevious = temp;
		mNextSet = false;
		mSlideTime = realTime;
		mCurrentTransition.init(mPrevious, mCurrent, realTime, Settings.slideSpeed);
	}
	
	private void updateNext(GL10 gl, long realTime){
		if(!mNextSet && realTime - mSlideTime > Settings.slideSpeed){
			ImageReference oldImage = mNext.getImage();
			mNext.clear(); // Clean slate
			mNext.setImage(gl, mBank.getTexture(oldImage));
			mNextSet = true;
		}
	}
	
	@Override
	public boolean slideLeft(long realTime) {
		mSlideTime = 0;
		return true;
	}

	@Override
	public boolean slideRight(long realTime) {
		return false;
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
		mCurrent.clear();
		mNext.clear();
	}
}
