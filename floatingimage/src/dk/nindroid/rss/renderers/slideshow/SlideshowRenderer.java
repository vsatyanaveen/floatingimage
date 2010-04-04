package dk.nindroid.rss.renderers.slideshow;

import javax.microedition.khronos.opengles.GL10;

import android.content.Intent;
import dk.nindroid.rss.TextureBank;
import dk.nindroid.rss.TextureSelector;
import dk.nindroid.rss.data.ImageReference;
import dk.nindroid.rss.gfx.Vec3f;
import dk.nindroid.rss.helpers.MatrixTrackingGL;
import dk.nindroid.rss.renderers.ProgressBar;
import dk.nindroid.rss.renderers.Renderer;
import dk.nindroid.rss.renderers.slideshow.transitions.SlideRightToLeft;
import dk.nindroid.rss.renderers.slideshow.transitions.Transition;

public class SlideshowRenderer implements Renderer {
	public final long SLIDETIME = 5000;
	public final long TRANSITIONDURATION = 300;
	
	Image 			mPrevious, mCurrent, mNext;
	TextureBank 	mBank;
	long			mSlideTime;
	Transition 		mCurrentTransition;
	
	
	public SlideshowRenderer(TextureBank bank){
		mPrevious = new Image();
		mCurrent = new Image();
		mNext = new Image();
		this.mBank = bank;
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
	public void init(GL10 gl, long time) {
		mPrevious.init(gl, time);
		mCurrent.init(gl, time);
		mNext.init(gl, time);
		mPrevious.setPos(new Vec3f(-20.0f, 0.0f, -1.0f));
		mCurrent.setPos(new Vec3f(0.0f, 0.0f, -1.0f));
		mNext.setPos(new Vec3f(20.0f, 0.0f, -1.0f));
	}

	@Override
	public void render(MatrixTrackingGL gl, long frameTime, long realtime) {
		mPrevious.render(gl);
		mCurrent.render(gl);
		if(!mCurrent.hasBitmap()){
			gl.glDepthMask(false);
			ProgressBar.draw(gl, TextureSelector.getProgress());
			gl.glDepthMask(true);
		}
	}

	@Override
	public void update(MatrixTrackingGL gl, long time, long realTime) {
		// Read first image to be shown
		if(mCurrent.getImage() == null){
			mCurrent.setImage(gl, mBank.getTexture(null));
			mSlideTime = realTime;
		}else if(mCurrent.hasBitmap() && mNext.getImage() == null){ // Load next image when first is done.
				mNext.setImage(gl, mBank.getTexture(null));
		}// Continue with normal slideshow
		else if(realTime - mSlideTime > SLIDETIME){
			next(gl, realTime);
		}
		
		if(mCurrentTransition != null){
			if(mCurrentTransition.isFinished()){
				mCurrentTransition = null;
			}else{
				mCurrentTransition.update(realTime);
			}
		}
	}
	
	private void next(MatrixTrackingGL gl, long realTime){
		Image temp = mCurrent;
		mCurrent = mNext;
		mNext = mPrevious;
		mPrevious = temp;
		mNext.clear(); // Clean slate
		mNext.setImage(gl, mBank.getTexture(null));
		mSlideTime = realTime;
		mCurrentTransition = new SlideRightToLeft(mPrevious, mCurrent, realTime, TRANSITIONDURATION);
	}
}
