package dk.nindroid.rss.renderers.slideshow.transitions;

import javax.microedition.khronos.opengles.GL10;

import dk.nindroid.rss.Display;
import dk.nindroid.rss.gfx.Vec3f;
import dk.nindroid.rss.renderers.Dimmer;
import dk.nindroid.rss.renderers.slideshow.Image;

public class FadeToWhite extends Transition {
	float fraction = 0;
	Display	mDisplay;
	
	public FadeToWhite(Display display){
		this.mDisplay = display;
	}
	
	@Override
	public void init(Image previous, Image next, long now, long duration, boolean isReverse) {
		super.init(previous, next, now, duration, isReverse);
		mNext.setPos(new Vec3f(20.0f, 0.0f, -1.0f));
	}

	@Override
	public void updateTransition(float fraction) {
		this.fraction = fraction;
		if(fraction > 0.5f){
			mNext.getPos().setX(0.0f);
			mPrevious.getPos().setX(20.0f);
		}
	}
	
	public void postRender(GL10 gl, long frameTime){
		float intensity = fraction > 0.5 ? 1.0f - fraction : fraction;
		intensity *= Math.PI;
		intensity = (float)Math.sin(intensity);
		Dimmer.setColor(1.0f, 1.0f, 1.0f);
		Dimmer.draw(gl, intensity, 1.0f, mDisplay);
	}
}
