package dk.nindroid.rss;

import java.io.DataInputStream;
import java.io.IOException;

import dk.nindroid.rss.data.ImageReference;
import dk.nindroid.rss.flickr.FlickrImage;
import dk.nindroid.rss.picasa.PicasaImage;

public class ImageTypeResolver {
	public static ImageReference getReference(DataInputStream dis) throws IOException{
		String type = dis.readLine();
		if(type.equals("flickrInternal")){
			return new FlickrImage();
		}else if(type.equals("picasaInternal")){
			return new PicasaImage();
		}
		return null;
	}
}
