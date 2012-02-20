package dk.nindroid.rss.renderers.slideshow.transitions;

import dk.nindroid.rss.Display;

public class SlideRightToLeft extends Transition {	
	Display mDisplay;
	
	public SlideRightToLeft(Display display){
		this.mDisplay = display;
	}
	
	public void updateTransition(float fraction){
		float width = mDisplay.getWidth() * 2.0f;
		float nextX = width - smoothstep(fraction) * width;
		mNext.getPos().setX(nextX);
		mPrevious.getPos().setX(nextX - width);
	}
}
