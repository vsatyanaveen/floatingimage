package dk.nindroid.rss.data;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Bitmap.Config;
import android.net.Uri;

public class LocalImage extends ImageReference{
	public final static String imageType = "local";
	private final static Paint paint = new Paint();
	private 	  File 		mFile;
	private 	  Bitmap 	mBitmap;
	private 	  float		mWidth;
	private 	  float		mHeight;
	
	public LocalImage(){
		mWidth = 0;
		mHeight = 0;
	}
	
	public LocalImage(File file){
		this.mFile = file;
		mWidth = 0;
		mHeight = 0;
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
	public void recycleBitmap() {
		if(mBitmap != null){
			mBitmap.recycle();
			mBitmap = null;
		}
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
		return mFile.getAbsolutePath().replace('/', '_').replace('.', '_');
	}

	@Override
	public String getImageID() {
		return mFile.getAbsolutePath().replace('/', '_').replace('.', '_');
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
		sb.append(mFile.getAbsolutePath());
		sb.append(nl);
		sb.append(getTargetOrientation());
		return sb.toString();
	}

	@Override
	public Intent follow() {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.parse("file://" + mFile.getAbsolutePath()), "image/jpeg");
		return intent;
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
	public void parseInfo(String[] tokens, Bitmap bmp) throws IOException {
		mWidth = Float.parseFloat(tokens[2]);
		mHeight = Float.parseFloat(tokens[3]);
		if(mFile == null){
			mFile = new File(tokens[4]);
		}
		if(!mFile.exists()){
			throw new FileNotFoundException();
		}
		if(tokens.length > 5){
			String rotation = tokens[5];
			if(rotation != null){
				setRotation(Float.parseFloat(rotation));
				validate();
			}
		}
		this.mBitmap = bmp;
	}

	@Override
	public void set128Bitmap(Bitmap bmp) {
		this.mBitmap = Bitmap.createBitmap(128, 128, Config.ARGB_8888);
		Canvas cvs = new Canvas(this.mBitmap);
		cvs.drawBitmap(bmp, 0, 0, paint);
		this.mWidth = bmp.getWidth() / 128.0f;
		this.mHeight = bmp.getHeight() / 128.0f;
		bmp.recycle();
	}
	
	public void set256Bitmap(Bitmap bmp){
		this.mBitmap = Bitmap.createBitmap(256, 256, Config.ARGB_8888);
		Canvas cvs = new Canvas(this.mBitmap);
		cvs.drawBitmap(bmp, 0, 0, paint);
		this.mWidth = bmp.getWidth() / 256.0f;
		this.mHeight = bmp.getHeight() / 256.0f;
		bmp.recycle();
	}

	@Override
	public void setOld() {
		// No can do
	}
	
	public File getFile(){
		return mFile;
	}
}
