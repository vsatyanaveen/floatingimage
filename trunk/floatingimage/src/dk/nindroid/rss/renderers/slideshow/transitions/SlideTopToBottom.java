package dk.nindroid.rss.renderers.slideshow.transitions;

import dk.nindroid.rss.RiverRenderer;
import dk.nindroid.rss.renderers.slideshow.Image;

public class SlideTopToBottom extends Transition {
	@Override
	public void init(Image previous, Image next, long now, long duration) {
		super.init(previous, next, now, duration);
		mNext.getPos().setX(0.0f);
	}
	public void update(long now){
		float fraction = this.getFraction(now);
		float height = RiverRenderer.mDisplay.getHeight() * 2.0f;
		if(fraction > 1.0f){
			this.finish();
		}else{
			float nextY = height - smoothstep(fraction) * height;
			mNext.getPos().setY(nextY);
			mPrevious.getPos().setY(nextY - height);
		}
	}
}
