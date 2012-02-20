package dk.nindroid.rss.renderers.slideshow.transitions;

import javax.microedition.khronos.opengles.GL10;

import dk.nindroid.rss.gfx.Vec3f;
import dk.nindroid.rss.renderers.slideshow.Image;

public abstract class Transition {
	Image mPrevious, mNext;
	long  mDuration, mStartTime;
	boolean mFinished = true;
	boolean mIsReverse;
	
	public final void update(long frameTime){
		float fraction = getFraction(frameTime);
		if(fraction > 1.0f){
			this.finish();
		}else{
			if(mIsReverse){
				fraction = 1.0f - fraction;
			}
			
			updateTransition(fraction);
		}
	}
	
	protected void updateTransition(float fraction){}
	
	public void preRender(GL10 gl, long frameTime){}
	public void postRender(GL10 gl, long frameTime){}
	public void init(Image previous, Image next, long now, long duration, boolean isReverse){
		next.setFocus(true);
		previous.setFocus(false);
		this.mFinished = false;	
		this.mPrevious = previous;
		this.mNext = next;
		this.mDuration = duration;
		this.mStartTime = now;
		this.mIsReverse = isReverse;
		mNext.setPos(new Vec3f(20.0f, 0.0f, -1.0f));
	}
	
	protected void finish(){
		this.mFinished = true;
		if(mIsReverse){
			mPrevious.getPos().setX(0.0f);
			mPrevious.getPos().setY(0.0f);
			mPrevious.setAlpha(1.0f);
			mNext.getPos().setX(-20.0f);
			mNext.getPos().setY(0.0f);
			mNext.setAlpha(1.0f);
		}else{
			mNext.getPos().setX(0.0f);
			mNext.getPos().setY(0.0f);
			mNext.setAlpha(1.0f);
			mPrevious.getPos().setX(-20.0f);
			mPrevious.getPos().setY(0.0f);
			mPrevious.setAlpha(1.0f);
		}
	}
	
	public final float getFraction(long now){
		return (now - mStartTime) / (float)mDuration;
	}
	
	public final boolean isFinished(){
		return mFinished;
	}
	
	public final boolean isReverse(){
		return this.mIsReverse;
	}
	
	protected final float smoothstep(float val){
		return Math.min(val * val * (3.0f - 2.0f * val), 1.0f);
	}
}
