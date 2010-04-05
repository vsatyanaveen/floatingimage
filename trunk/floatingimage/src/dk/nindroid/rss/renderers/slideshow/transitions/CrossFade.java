package dk.nindroid.rss.renderers.slideshow.transitions;

import dk.nindroid.rss.gfx.Vec3f;
import dk.nindroid.rss.renderers.slideshow.Image;

public class CrossFade extends Transition {
	@Override
	public void init(Image previous, Image next, long now, long duration) {
		super.init(previous, next, now, duration);
		mNext.setPos(new Vec3f(0.0f, 0.0f, -1.0f));
		mNext.setAlpha(0.0f);
	}
	
	@Override
	public void update(long now) {
		float fraction = getFraction(now);
		if(fraction > 1.0f){
			this.finish();
		}else{
			float alpha = fraction;
			mPrevious.setAlpha(1.0f - alpha);
			mNext.setAlpha(alpha);
		}
	}
}
