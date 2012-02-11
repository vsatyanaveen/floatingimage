package dk.nindroid.rss.parser.photobucket;

import java.io.IOException;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import dk.nindroid.rss.data.ImageReference;

public class PhotobucketImage extends ImageReference {
	public final static String imageType = "photobucket";
	String imgID;
	String owner;
	String title;
	String sourceURL;
	String thumbURL;
	String pageURL;
	
	void setID(String id){
		this.imgID = id;
	}
	
	void setOwner(String owner){
		this.owner = owner;
	}
	
	void setTitle(String title){
		this.title = title;
	}
	
	void setSourceURL(String url){
		this.sourceURL = url;
	}
	
	void setThumbUrl(String url){
		this.thumbURL = url;
	}
	
	void setPageUrl(String url){
		this.pageURL = url;
	}
	
	@Override
	public String get128ImageUrl() {
		return thumbURL;
	}

	@Override
	public String get256ImageUrl() {
		return thumbURL;
	}

	@Override
	public String getBigImageUrl() {
		return sourceURL;
	}

	@Override
	public String getOriginalImageUrl() {
		return sourceURL;
	}

	@Override
	public String getImagePageUrl() {
		return pageURL;
	}

	@Override
	public Intent follow() {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.addCategory(Intent.CATEGORY_BROWSABLE);
		intent.setData(Uri.parse(pageURL));
		return intent;
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public String getID() {
		return imgID;
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
		sb.append(owner);
		sb.append(nl);
		sb.append(title);
		sb.append(nl);
		sb.append(thumbURL);
		sb.append(nl);
		sb.append(pageURL);
		sb.append(nl);
		sb.append(sourceURL);
		return sb.toString(); 
	}

	@Override
	public void parseInfo(String[] tokens, Bitmap bmp) throws IOException {
		mWidth = Float.parseFloat(tokens[2]);
		mHeight = Float.parseFloat(tokens[3]);
		imgID = tokens[4];
		owner = tokens[5];
		title = tokens[6];
		thumbURL = tokens[7];
		pageURL = tokens[8];
		sourceURL = tokens[9];
		this.mBitmap = bmp;
	}

	@Override
	public String getAuthor() {
		return owner;
	}

	@Override
	public void getExtended() {
		
	}

}
