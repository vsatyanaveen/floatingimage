package dk.nindroid.rss.facebook;

import java.io.IOException;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import dk.nindroid.rss.data.ImageReference;

public class FacebookImage extends ImageReference {

	public final static String imageType = "facebook";
	String imgID;
	String owner;
	String sourceURL;
	String thumb128URL;
	String thumb256URL;
	String pageURL;
	boolean unseen;
	boolean personal;
	
	public void setImageID(String id){
		imgID = id;
	}
		
	public void setOwner(String owner){
		this.owner = owner;
	}
	
	public void setSourceURL(String source){
		this.sourceURL = source;
	}
	
	public void setPageURL(String page){
		this.pageURL = page;
	}
	
	public void setThumbnail128URL(String source){
		this.thumb128URL = source;
	}

	public void setThumbnail256URL(String source){
		this.thumb256URL = source;
	}
		
	@Override
	public Intent follow() {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.addCategory(Intent.CATEGORY_BROWSABLE);
		intent.setData(Uri.parse(pageURL));
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
	public String getImageID() {
		return imgID;
	}

	@Override
	public String getImagePageUrl() {
		return pageURL;
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
		sb.append(thumb128URL);
		sb.append(nl);
		sb.append(thumb256URL);
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
		thumb128URL = tokens[6];
		thumb256URL = tokens[7];
		pageURL = tokens[8];
		sourceURL = tokens[9];
		this.mBitmap = bmp;
	}

	@Override
	public String getOriginalImageUrl() {
		return sourceURL;
	}

	@Override
	public String get128ImageUrl() {
		return thumb128URL;
	}
	
	@Override
	public String get256ImageUrl() {
		//return thumb256URL;
		return getBigImageUrl();
	}

	@Override
	public String getTitle() {
		return "- Facebook image -";
	}

	@Override
	public boolean isNew() {
		return unseen;
	}

	@Override
	public boolean isPersonal() {
		return personal;
	}

	@Override
	public void setOld() {
		unseen = false;
	}

}
