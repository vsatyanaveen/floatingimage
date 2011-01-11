package dk.nindroid.rss.renderers.slideshow.transitions;

import dk.nindroid.rss.Display;

public class SlideRightToLeft extends Transition {	
	Display mDisplay;
	
	public SlideRightToLeft(Display display){
		this.mDisplay = display;
	}
	
	public void update(long now){
		float fraction = getFraction(now);
		float width = mDisplay.getWidth() * 2.0f;
		if(fraction > 1.0f){
			this.finish();
		}else{
			float nextX = width - smoothstep(fraction) * width;
			mNext.getPos().setX(nextX);
			mPrevious.getPos().setX(nextX - width);
		}
	}
}
