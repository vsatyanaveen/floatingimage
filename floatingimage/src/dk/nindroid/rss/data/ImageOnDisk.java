package dk.nindroid.rss.data;

import java.io.File;

public class ImageOnDisk {
	private File bitmap;
	private File info;
	public File getBitmap() {
		return bitmap;
	}
	public File getInfo() {
		return info;
	}
	public ImageOnDisk(File bitmap, File info) {
		super();
		this.bitmap = bitmap;
		this.info = info;
	}	
}
