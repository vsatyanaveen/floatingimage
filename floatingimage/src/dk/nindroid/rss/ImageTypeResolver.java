package dk.nindroid.rss;

import java.io.DataInputStream;
import java.io.IOException;

import dk.nindroid.rss.data.ImageReference;
import dk.nindroid.rss.data.LocalImage;
import dk.nindroid.rss.facebook.FacebookImage;
import dk.nindroid.rss.flickr.FlickrImage;
import dk.nindroid.rss.picasa.PicasaImage;

public class ImageTypeResolver {
	public static ImageReference getReference(DataInputStream dis) throws IOException{
		String type = dis.readLine();
		if(type.equals("flickrInternal")){
			return new FlickrImage();
		}else if(type.equals("picasaInternal")){
			return new PicasaImage();
		}else if(type.equals(FacebookImage.imageType)){
			return new FacebookImage();
		}else if(type.equals(LocalImage.imageType)){
			return new LocalImage();
		}
		
		return null; // ?? Unknown internal format! Ought to be an exception!
	}
}
