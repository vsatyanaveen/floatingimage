package dk.nindroid.rss.orientation;

public interface OrientationSubscriber {
	public static int UP_IS_UP 		= 0;
	public static int UP_IS_LEFT	= 1;
	public static int UP_IS_DOWN 	= 2;
	public static int UP_IS_RIGHT 	= 3;
	
	void setOrientation(int orientation);
}
