package dk.nindroid.rss.data;

import java.io.IOException;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;

public class ContentUriImage extends ImageReference{
	Uri uri;
	String title;
	long id;
	
	public ContentUriImage(long id, String title, Uri uri) {
		this.title = title;
		this.id = id;
		this.uri = uri;
	}
	
	public Uri getUri(){
		return uri;
	}
	
	public long getUriId(){
		return id;
	}
	
	@Override
	public String get128ImageUrl() {
		return null;
	}

	@Override
	public String get256ImageUrl() {
		return null;
	}

	@Override
	public String getBigImageUrl() {
		return "";
	}

	@Override
	public String getOriginalImageUrl() {
		return "";
	}

	@Override
	public String getImagePageUrl() {
		return "";
	}

	@Override
	public Intent follow() {
		return null;
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public String getID() {
		return uri.toString().replace(":", "_").replace("/", "_");
	}

	@Override
	public String getInfo() {
		return null;
	}

	@Override
	public void parseInfo(String[] tokens, Bitmap bmp) throws IOException {
		return;
	}

	@Override
	public String getAuthor() {
		return "";
	}

	@Override
	public void getExtended() {
		return;
	}

}
