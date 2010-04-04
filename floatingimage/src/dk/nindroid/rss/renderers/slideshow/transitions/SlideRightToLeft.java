package dk.nindroid.rss.renderers.slideshow.transitions;

import android.util.Log;
import dk.nindroid.rss.RiverRenderer;
import dk.nindroid.rss.gfx.Vec3f;
import dk.nindroid.rss.renderers.slideshow.Image;

public class SlideRightToLeft implements Transition {
	Image mPrevious, mNext;
	long  mDuration, mStartTime;
	boolean mFinished = false;
	
	public boolean isFinished(){
		return mFinished;
	}
	
	public SlideRightToLeft(Image previous, Image next, long now, long duration) {
		this.mPrevious = previous;
		this.mNext = next;
		this.mDuration = duration;
		this.mStartTime = now;
		
		mNext.setPos(new Vec3f(2.0f, 0.0f, -1.0f));
	}
	
	public void update(long now){
		float fraction = (now - mStartTime) / (float)mDuration;
		Log.v("SlideRightToLeft", "fraction: " + fraction);
		float width = RiverRenderer.mDisplay.getWidth() * 2.0f;
		if(fraction > 1.0f){
			mNext.getPos().setX(0.0f);
			mPrevious.getPos().setX(-20.0f);
			this.mFinished = true;
		}else{
			float nextX = width - smoothstep(fraction) * width;
			mNext.getPos().setX(nextX);
			mPrevious.getPos().setX(nextX - width);
		}
	}
	
	private float smoothstep(float val){
		return Math.min(val * val * (3.0f - 2.0f * val), 1.0f);
	}
}
