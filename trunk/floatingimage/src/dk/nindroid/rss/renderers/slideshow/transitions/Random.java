package dk.nindroid.rss.renderers.slideshow.transitions;

import javax.microedition.khronos.opengles.GL10;

import dk.nindroid.rss.Display;
import dk.nindroid.rss.renderers.slideshow.Image;


public class Random extends Transition{
	Transition[]  		mTransitions = new Transition[5];
	java.util.Random	mRand;
	Transition			mCurrent;
	
	public Random(Display display){
		
		mTransitions[0] = new CrossFade();
		mTransitions[1] = new FadeToBlack(display);
		mTransitions[2] = new FadeToWhite(display);
		mTransitions[3] = new SlideRightToLeft(display);
		mTransitions[4] = new SlideTopToBottom(display);
		mRand = new java.util.Random(System.currentTimeMillis());
	}
	
	@Override
	public void init(Image previous, Image next, long now, long duration) {
		this.mFinished = false;
		mCurrent = mTransitions[mRand.nextInt(5)];
		mCurrent.init(previous, next, now, duration);
	}
	
	@Override
	public void preRender(GL10 gl, long frameTime) {
		mCurrent.preRender(gl, frameTime);
	}
	
	@Override
	public void postRender(GL10 gl, long frameTime) {
		mCurrent.postRender(gl, frameTime);
	}
	
	@Override
	public void update(long frameTime) {
		mCurrent.update(frameTime);
		this.mFinished = mCurrent.mFinished;
	}
}
