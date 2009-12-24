package dk.nindroid.rss.data;

import java.io.File;
import java.io.FileFilter;

public class ExtensionFilter implements FileFilter {
	String mExtension;
	int mExtensionLength;
	public ExtensionFilter(String extension){
		this.mExtension = extension;
		this.mExtensionLength = extension.length();
	}
	@Override
	public boolean accept(File file) {
		String name = file.getName();
		int nameLength = name.length();
		if(nameLength < mExtensionLength) return false;
		return name.substring(nameLength - mExtensionLength, nameLength).equals(mExtension);
	}

}
