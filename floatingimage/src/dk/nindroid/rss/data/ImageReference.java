package dk.nindroid.rss.data;

import java.io.DataInputStream;
import java.io.IOException;

import android.content.Intent;
import android.graphics.Bitmap;

public interface ImageReference {
	String getSmallImageUrl();
	String getBigImageUrl();
	String getOriginalImageUrl();
	String getImagePageUrl();
	Intent follow();
	String getTitle();
	String getImageID();
	Bitmap getBitmap();
	void set128Bitmap(Bitmap bitmap);
	void set512Bitmap(Bitmap bitmap);
	float getWidth();
	float getHeight();
	String getID();
	String getInfo();
	void parseInfo(DataInputStream is, Bitmap bmp) throws IOException;
	boolean isNew();
	void setOld();
	boolean isPersonal();
	String getAuthor();
	void getExtended();
}
