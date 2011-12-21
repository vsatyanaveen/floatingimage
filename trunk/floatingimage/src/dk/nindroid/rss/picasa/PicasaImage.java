package dk.nindroid.rss.picasa;

import java.io.IOException;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import dk.nindroid.rss.data.ImageReference;

public class PicasaImage extends ImageReference{
	private final static String imageType = "picasaInternal";
	String imgID;
	String title;
	String owner;
	String sourceURL;
	int thumbnail128Max = 0;
	int thumbnail256Max = 0;
	String thumbnail128URL;
	String thumbnail256URL;
	String imageURL;
	
	public void setImageID(String id){
		imgID = id;
	}
	
	public void setTitle(String title){
		this.title = title;
	}
	
	public void setOwner(String owner){
		this.owner = owner;
	}
	
	public void setSourceURL(String source){
		this.sourceURL = source;
	}
	
	public void setThumbnailURL(String source, int width, int height){
		int max = Math.max(width, height);
		if((thumbnail128Max < 128 && max > thumbnail128Max) || (thumbnail128Max > 128 && max > 128)){ // smaller than target, and max is larger, og larger than target, and max is a closer fit.
			thumbnail128Max = max;
			this.thumbnail128URL = source;
		}
		if((thumbnail256Max < 256 && max > thumbnail256Max) || (thumbnail256Max > 256 && max > 256)){ // Ditto
			thumbnail256Max = max;
			this.thumbnail256URL = source;
		}
	}
	
	public void setImageURL(String url){
		this.imageURL = url;
	}
		
	@Override
	public Intent follow() {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.addCategory(Intent.CATEGORY_BROWSABLE);
		intent.setData(Uri.parse(getImagePageUrl()));
		return intent;
	}

	@Override
	public String getAuthor() {
		return owner;
	}

	@Override
	public String getBigImageUrl() {
		return sourceURL;
	}
	
	@Override
	public void getExtended() {
		// No need, we get everything from the initial lookup!		
	}

	@Override
	public String getID() {
		return imgID;
	}

	@Override
	public String getImagePageUrl() {
		return imageURL;
	}

	@Override
	public String getInfo() {
		StringBuilder sb = new StringBuilder();
		String nl = "\n";
		sb.append(imageType);
		sb.append(nl);
		sb.append(mWidth);
		sb.append(nl);
		sb.append(mHeight);
		sb.append(nl);
		sb.append(imgID);
		sb.append(nl);
		sb.append(title);
		sb.append(nl);
		sb.append(owner);
		sb.append(nl);
		sb.append(thumbnail128URL);
		sb.append(nl);
		sb.append(thumbnail256URL);
		sb.append(nl);
		sb.append(imageURL);
		sb.append(nl);
		sb.append(sourceURL);
		return sb.toString(); 
	}
	
	@Override
	public void parseInfo(String[] tokens, Bitmap bmp) throws IOException {
		mWidth = Float.parseFloat(tokens[2]);
		mHeight = Float.parseFloat(tokens[3]);
		imgID = tokens[4];
		title = tokens[5];
		owner = tokens[6];
		thumbnail128URL = tokens[7];
		thumbnail256URL = tokens[8];
		imageURL = tokens[9];
		sourceURL = tokens[10];
		this.mBitmap = bmp;
	}

	@Override
	public String getOriginalImageUrl() {
		return sourceURL;
	}

	@Override
	public String get128ImageUrl() {
		return thumbnail128URL;
	}
	
	@Override
	public String get256ImageUrl() {
		return thumbnail256URL;
	}

	@Override
	public String getTitle() {
		return title;
	}
}
