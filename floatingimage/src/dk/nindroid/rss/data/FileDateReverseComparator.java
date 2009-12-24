package dk.nindroid.rss.data;

import java.io.File;
import java.util.Comparator;

public class FileDateReverseComparator implements Comparator<File> {

	@Override
	public int compare(File f1, File f2) {
		if(f1.lastModified() > f2.lastModified()) return -1;
		if(f1.lastModified() < f2.lastModified()) return 1;
		return 0;
	}

}
