package dk.nindroid.rss.data;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;

public class LocalImage extends ImageReference{
	public final static String imageType = "local";
	private 	  File 		mFile;
	
	public LocalImage(){}
	
	public LocalImage(File file){
		this.mFile = file;
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
	public void getExtended() {
		// No extended available
	}

	@Override
	public String getID() {
		return getID(mFile);
	}

	@Override
	public String getImageID() {
		return getID(mFile);
	}
	
	public static String getID(File f){
		return f.getAbsolutePath().replace('/', '_').replace('.', '_');
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
	public void setOld() {
		// No can do
	}
	
	public File getFile(){
		return mFile;
	}
}
