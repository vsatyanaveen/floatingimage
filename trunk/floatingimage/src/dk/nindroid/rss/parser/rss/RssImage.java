package dk.nindroid.rss.parser.rss;

import java.io.IOException;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import dk.nindroid.rss.data.ImageReference;

public class RssImage extends ImageReference {
	public final static String imageType = "rss";
	String thumbnailUrl;
	String largeUrl;
	String pageUrl;
	String title;
	String owner;
	String guid;
	
	@Override
	public String get128ImageUrl() {
		return thumbnailUrl;
	}

	@Override
	public String get256ImageUrl() {
		return thumbnailUrl;
	}

	@Override
	public String getBigImageUrl() {
		return largeUrl;
	}

	@Override
	public String getOriginalImageUrl() {
		return largeUrl;
	}

	@Override
	public String getImagePageUrl() {
		return pageUrl;
	}

	@Override
	public Intent follow() {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.addCategory(Intent.CATEGORY_BROWSABLE);
		intent.setData(Uri.parse(pageUrl));
		return intent;
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public String getID() {
		return guid;
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
		sb.append(guid);
		sb.append(nl);
		sb.append(owner);
		sb.append(nl);
		sb.append(title);
		sb.append(nl);
		sb.append(thumbnailUrl);
		sb.append(nl);
		sb.append(pageUrl);
		sb.append(nl);
		sb.append(largeUrl);
		return sb.toString(); 
	}

	@Override
	public void parseInfo(String[] tokens, Bitmap bmp) throws IOException {
		mWidth = Float.parseFloat(tokens[2]);
		mHeight = Float.parseFloat(tokens[3]);
		guid = tokens[4];
		owner = tokens[5];
		title = tokens[6];
		thumbnailUrl = tokens[7];
		pageUrl = tokens[8];
		largeUrl = tokens[9];
		this.mBitmap = bmp;
	}

	@Override
	public String getAuthor() {
		return this.owner;
	}

	@Override
	public void getExtended() {
		
	}

}
