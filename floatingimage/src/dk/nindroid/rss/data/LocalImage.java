package dk.nindroid.rss.data;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Bitmap.Config;
import android.net.Uri;

public class LocalImage implements ImageReference{
	private final static Paint paint = new Paint();
	private final File 		mFile;
	private final Bitmap 	mBitmap;
	private final float		mWidth;
	private final float		mHeight;
	private int 			mRotation = 0;
	
	public LocalImage(File file, Bitmap bmp, int rotation){
		this.mRotation = rotation;
		this.mFile = file;
		this.mBitmap = Bitmap.createBitmap(128, 128, Config.RGB_565);
		Canvas cvs = new Canvas(this.mBitmap);
		cvs.drawBitmap(bmp, 0, 0, paint);
		this.mWidth = bmp.getWidth() / 128.0f;
		this.mHeight = bmp.getHeight() / 128.0f;
		bmp.recycle();
	}
	
	@Override
	public String getAuthor() {
		return mFile.getAbsolutePath();
	}

	@Override
	public String getBigImageUrl() {
		return mFile.getAbsolutePath();
	}
	
	public String getOriginalImageUrl(){
		return mFile.getAbsolutePath();
	}
	
	@Override
	public String getImagePageUrl() {
		return "";
	}

	@Override
	public Bitmap getBitmap() {
		return mBitmap;
	}

	@Override
	public void getExtended() {
		// No extended available
	}

	@Override
	public float getHeight() {
		return mHeight;
	}

	@Override
	public String getID() {
		return mFile.getAbsolutePath();
	}

	@Override
	public String getImageID() {
		return mFile.getAbsolutePath();
	}

	@Override
	public String getInfo() {
		return null;
	}

	@Override
	public Intent follow() {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.parse("file://" + mFile.getAbsolutePath()), "image/jpeg");
		return intent;
	}

	@Override
	public String getSmallImageUrl() {
		return null;
	}

	@Override
	public String getTitle() {
		return mFile.getName();
	}

	@Override
	public float getWidth() {
		return mWidth;
	}

	@Override
	public boolean isNew() {
		return false;
	}

	@Override
	public boolean isPersonal() {
		return true;
	}

	@Override
	public void parseInfo(DataInputStream is, Bitmap bmp) throws IOException {
		// No can do		
	}

	@Override
	public void set128Bitmap(Bitmap bitmap) {
		// No can do
	}

	@Override
	public void set512Bitmap(Bitmap bitmap) {
		// No can do
	}

	@Override
	public void setOld() {
		// No can do
	}
	public int getRotation(){
		return this.mRotation;
	}
}
