package dk.nindroid.rss.renderers.floating;

import java.util.Comparator;

public class ImageDepthComparator implements Comparator<Image> {

	@Override
	public int compare(Image arg0, Image arg1) {
		// We need reverse sorting
		return Float.compare(arg0.getDepth(), arg1.getDepth());
	}
	
}
