package dk.nindroid.rss.picasa;

import java.io.DataInputStream;
import java.io.IOException;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Bitmap.Config;
import android.net.Uri;
import dk.nindroid.rss.data.ImageReference;

public class PicasaImage implements ImageReference{
	private final static String imageType = "picasaInternal";
	private final static Paint paint = new Paint();
	String imgID;
	String title;
	String owner;
	String sourceURL;
	int thumbnailMax = 0;
	String thumbnailURL;
	String imageURL;
	Bitmap bitmap;
	boolean unseen;
	boolean personal;
	float width;
	float height;
	
	public void setImageID(String id){
		imgID = id;
	}
	
	public void setTitle(String title){
		this.title = title;
	}
	
	public void setOwner(String owner){
		this.owner = owner;
	}
	
	public void setSourceURL(String source){
		this.sourceURL = source;
	}
	
	public void setThumbnailURL(String source, int width, int height){
		if(this.thumbnailURL != null){
			if(thumbnailMax > 100 || thumbnailMax < Math.max(width, height)){
				return;
			}
		}
		this.thumbnailURL = source;
	}
	
	public void setImageURL(String url){
		this.imageURL = url;
	}
		
	@Override
	public Intent follow() {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.addCategory(Intent.CATEGORY_BROWSABLE);
		intent.setData(Uri.parse(getImagePageUrl()));
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
	public Bitmap getBitmap() {
		return bitmap;
	}

	@Override
	public void getExtended() {
		// No need, we get everything from the initial lookup!		
	}

	@Override
	public float getHeight() {
		return height;
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
		return imageURL;
	}

	@Override
	public String getInfo() {
		StringBuilder sb = new StringBuilder();
		String nl = "\n";
		sb.append(imageType);
		sb.append(nl);
		sb.append(width);
		sb.append(nl);
		sb.append(height);
		sb.append(nl);
		sb.append(imgID);
		sb.append(nl);
		sb.append(title);
		sb.append(nl);
		sb.append(owner);
		sb.append(nl);
		sb.append(thumbnailURL);
		sb.append(nl);
		sb.append(imageURL);
		sb.append(nl);
		sb.append(sourceURL);
		return sb.toString(); 
	}
	
	@Override
	public void parseInfo(DataInputStream is, Bitmap bmp) throws IOException {
		width = Float.parseFloat(is.readLine());
		height = Float.parseFloat(is.readLine());
		imgID = is.readLine();
		title = is.readLine();
		owner = is.readLine();
		thumbnailURL = is.readLine();
		imageURL = is.readLine();
		sourceURL = is.readLine();
		this.bitmap = bmp;
	}

	@Override
	public String getOriginalImageUrl() {
		return sourceURL;
	}

	@Override
	public int getRotation() {
		return 0;
	}

	@Override
	public String getSmallImageUrl() {
		return thumbnailURL;
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public float getWidth() {
		return width;
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
	public void set128Bitmap(Bitmap bitmap) {
		this.bitmap = Bitmap.createBitmap(128, 128, Config.RGB_565);
		Canvas cvs = new Canvas(this.bitmap);
		cvs.drawBitmap(bitmap, 0, 0, paint);
		this.width = bitmap.getWidth() / 128.0f;
		this.height = bitmap.getHeight() / 128.0f;
		bitmap.recycle();
	}

	@Override
	public void set512Bitmap(Bitmap bitmap) {
		this.bitmap = Bitmap.createBitmap(512, 512, Config.RGB_565);
		Canvas cvs = new Canvas(this.bitmap);
		cvs.drawBitmap(bitmap, 0, 0, paint);
		this.width = bitmap.getWidth() / 512.0f;
		this.height = bitmap.getHeight() / 512.0f;
		bitmap.recycle();
	}

	@Override
	public void setOld() {
		unseen = false;
	}

}
