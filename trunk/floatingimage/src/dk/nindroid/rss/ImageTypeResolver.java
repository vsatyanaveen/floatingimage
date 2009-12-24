package dk.nindroid.rss;

import java.io.DataInputStream;
import java.io.IOException;

import dk.nindroid.rss.data.FlickrImage;
import dk.nindroid.rss.data.ImageReference;

public class ImageTypeResolver {
	public static ImageReference getReference(DataInputStream dis) throws IOException{
		String type = dis.readLine();
		if(type.equals("flickrInternal")){
			return new FlickrImage();
		}
		return null;
	}
}
