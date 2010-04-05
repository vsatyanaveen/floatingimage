package dk.nindroid.rss.renderers.slideshow.transitions;

import dk.nindroid.rss.RiverRenderer;

public class SlideRightToLeft extends Transition {	
	public void update(long now){
		float fraction = getFraction(now);
		float width = RiverRenderer.mDisplay.getWidth() * 2.0f;
		if(fraction > 1.0f){
			this.finish();
		}else{
			float nextX = width - smoothstep(fraction) * width;
			mNext.getPos().setX(nextX);
			mPrevious.getPos().setX(nextX - width);
		}
	}
}
