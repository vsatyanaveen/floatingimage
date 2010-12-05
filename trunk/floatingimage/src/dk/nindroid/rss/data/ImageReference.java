package dk.nindroid.rss.data;

import java.io.IOException;

import android.content.Intent;
import android.graphics.Bitmap;
import dk.nindroid.rss.renderers.Rotator;

public abstract class ImageReference {
	private Rotator mRotator;
	protected boolean mInvalidated = false;
	public ImageReference(){
		this.mRotator = new Rotator();
	}
	
	public abstract String get128ImageUrl();
	public abstract String get256ImageUrl();
	public abstract String getBigImageUrl();
	public abstract String getOriginalImageUrl();
	public abstract String getImagePageUrl();
	public abstract Intent follow();
	public abstract String getTitle();
	public abstract String getImageID();
	public abstract Bitmap getBitmap();
	public abstract void set128Bitmap(Bitmap bitmap);
	public abstract void set256Bitmap(Bitmap bitmap);
	public abstract float getWidth();
	public abstract float getHeight();
	public abstract String getID();
	public abstract String getInfo();
	public abstract void parseInfo(String[] tokens, Bitmap bmp) throws IOException;
	public abstract boolean isNew();
	public abstract void setOld();
	public abstract boolean isPersonal();
	public abstract String getAuthor();
	public abstract void getExtended();
	public boolean isInvalidated(){
		return mInvalidated;
	}
	public void validate(){
		mInvalidated = false;
	}
	public float getRotation(long time){
		return mRotator.getRotation(time);
	}
	public float getTargetOrientation(){
		return mRotator.getTargetOrientation();
	}
	public float getPreviousOrientation(){
		return mRotator.getPreviousOrientation();
	}
	public float getRotationFraction(long time){
		return mRotator.getFraction(time);
	}
	public void turn(long time, float degrees){
		mRotator.turn(time, degrees);
		mInvalidated = true;
	}
	public void setRotation(float degrees){
		mRotator.setRotation(degrees);
		mInvalidated = true;
	}
}
