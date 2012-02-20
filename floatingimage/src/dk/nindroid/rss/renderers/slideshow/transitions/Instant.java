package dk.nindroid.rss.renderers.slideshow.transitions;

import dk.nindroid.rss.gfx.Vec3f;
import dk.nindroid.rss.renderers.slideshow.Image;

public class Instant extends Transition {	
	@Override
	public void init(Image previous, Image next, long now, long duration, boolean isReverse) {
		super.init(previous, next, now, duration, isReverse);
		mNext.setPos(new Vec3f(20.0f, 0.0f, -1.0f));
	}
	
	@Override
	public void updateTransition(float fraction) {
		
	}

}
