package dk.nindroid.rss.renderers.slideshow.transitions;

import javax.microedition.khronos.opengles.GL10;

import dk.nindroid.rss.Display;
import dk.nindroid.rss.gfx.Vec3f;
import dk.nindroid.rss.renderers.Dimmer;
import dk.nindroid.rss.renderers.slideshow.Image;

public class FadeToBlack extends Transition {
	float fraction = 0.0f;
	Display	mDisplay;
	
	public FadeToBlack(Display display){
		this.mDisplay = display;
	}
	
	@Override
	public void init(Image previous, Image next, long now, long duration) {
		super.init(previous, next, now, duration);
		mNext.setPos(new Vec3f(20.0f, 0.0f, -1.0f));
	}

	@Override
	public void update(long now) {
		float fraction = this.getFraction(now);
		this.fraction = fraction;
		if(fraction > 1.0f){
			this.finish();
		}else if(fraction > 0.5f){
			mNext.getPos().setX(0.0f);
			mPrevious.getPos().setX(20.0f);
		}
	}
	
	@Override
	public void postRender(GL10 gl, long frameTime){
		float intensity = fraction > 0.5 ? 1.0f - fraction : fraction;
		intensity *= Math.PI;
		intensity = (float)Math.sin(intensity);
		Dimmer.setColor(0.0f, 0.0f, 0.0f);
		Dimmer.draw(gl, intensity, 1.0f, mDisplay);
	}
}
