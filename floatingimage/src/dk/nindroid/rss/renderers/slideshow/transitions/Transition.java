package dk.nindroid.rss.renderers.slideshow.transitions;

public interface Transition {
	public void update(long frameTime);
	public boolean isFinished();
}
