package dk.nindroid.rss.data;

import java.io.IOException;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import dk.nindroid.rss.TextureSelector;
import dk.nindroid.rss.renderers.Rotator;

public abstract class ImageReference {
	private Rotator mRotator;
	protected Bitmap mBitmap;
	protected boolean mInvalidated = false;
	protected 	  float		mWidth;
	protected 	  float		mHeight;
	private final static Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);
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
	public abstract String getID();
	public abstract String getInfo();
	public abstract void parseInfo(String[] tokens, Bitmap bmp) throws IOException;
	public abstract boolean isNew();
	public abstract void setOld();
	public abstract boolean isPersonal();
	public abstract String getAuthor();
	public abstract void getExtended();
	
	public final void recycleBitmap() {
		if(mBitmap != null){
			synchronized (mBitmap) {
				mBitmap.recycle();
				mBitmap = null;
			}
		}
	}
	
	final public Bitmap getBitmap(){
		return this.mBitmap;
	}
	
	public final void set128Bitmap(Bitmap bmp){
		this.mBitmap = Bitmap.createBitmap(128, 128, bmp.getConfig());
		Canvas cvs = new Canvas(this.mBitmap);
		Rect drawRect = getRect(bmp, 128);
		cvs.drawBitmap(bmp, null, drawRect, paint);
		this.mWidth = drawRect.width() / 128.0f;
		this.mHeight = drawRect.height() / 128.0f;
		bmp.recycle();
	}
	
	
	public final void set256Bitmap(Bitmap bmp){
		this.mBitmap = Bitmap.createBitmap(256, 256, bmp.getConfig());
		Canvas cvs = new Canvas(this.mBitmap);
		Rect drawRect = getRect(bmp, 256);
		cvs.drawBitmap(bmp, null, drawRect, paint);
		this.mWidth = drawRect.width() / 256.0f;
		this.mHeight = drawRect.height() / 256.0f;
		bmp.recycle();
	}
	
	protected Rect getRect(Bitmap bmp, int size){
		int max = Math.max(bmp.getWidth(), bmp.getHeight());
		float scale = size / (float)max;
		return new Rect(0, 0, (int)(bmp.getWidth() * scale), (int)(bmp.getHeight() * scale));
	}
	
	public final float getWidth(){
		return mWidth;
	}
	public final float getHeight(){
		return mHeight;
	}
	
	boolean deleted = false;
	public boolean isDeleted(){
		return deleted;
	}
	public void setDeleted(){
		deleted = true;
	}
	
	public boolean isInvalidated(){
		return mInvalidated;
	}
	public void validate(){
		mInvalidated = false;
	}
	public float getRotation(TextureSelector textureSelector, long time){
		return mRotator.getRotation(textureSelector, time);
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
