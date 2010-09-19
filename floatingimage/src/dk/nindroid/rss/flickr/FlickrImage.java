package dk.nindroid.rss.flickr;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Bitmap.Config;
import android.net.Uri;
import dk.nindroid.rss.data.ImageReference;
import dk.nindroid.rss.parser.flickr.data.ImageSizes;

public class FlickrImage extends ImageReference{
	private final static String imageType = "flickrInternal";
	private final static Paint paint = new Paint();
	String farmID;
	String serverID;
	String imgID;
	String secret;
	String title;
	String owner;
	FlickrUserInfo userInfo;
	Bitmap bitmap;
	boolean unseen;
	boolean personal;
	float width;
	float height;
	ImageSizes sizes;
	
	public String getID(){
		return imgID;
	}
	
	public Bitmap getBitmap(){
		return bitmap;
	}
	@Override
	public void set128Bitmap(Bitmap bitmap){
		this.bitmap = Bitmap.createBitmap(128, 128, Config.RGB_565);
		Canvas cvs = new Canvas(this.bitmap);
		cvs.drawBitmap(bitmap, 0, 0, paint);
		this.width = bitmap.getWidth() / 128.0f;
		this.height = bitmap.getHeight() / 128.0f;
		bitmap.recycle();
	}
	@Override
	public void set256Bitmap(Bitmap bitmap){
		this.bitmap = Bitmap.createBitmap(256, 256, Config.RGB_565);
		Canvas cvs = new Canvas(this.bitmap);
		cvs.drawBitmap(bitmap, 0, 0, paint);
		this.width = bitmap.getWidth() / 256.0f;
		this.height = bitmap.getHeight() / 256.0f;
		bitmap.recycle();		
	}
	public void getExtended(){
		userInfo = PersonInfo.getInfo(owner);
	}
	public String getFarmID() {
		return farmID;
	}
	public void setFarmID(String farmID) {
		this.farmID = farmID;
	}
	public String getServerID() {
		return serverID;
	}
	public void setServerID(String serverID) {
		this.serverID = serverID;
	}
	public String getImageID() {
		return imgID;
	}
	public void setImgID(String imgID) {
		this.imgID = imgID;
	}
	public String getSecret() {
		return secret;
	}
	public void setSecret(String secret) {
		this.secret = secret;
	}
	public void setTitle(String title){
		this.title = title;
	}
	public String getTitle(){
		return title;
	}
	public void setOwner(String owner){
		this.owner = owner;
	}
	public String getOwner(){
		return owner;
	}
	public String getAuthor(){
		if(userInfo != null){
			return userInfo.getUsername();
		}
		return null;
	}
	public FlickrUserInfo getUserInfo(){
		return userInfo;
	}
	public void setPersonInfo(FlickrUserInfo userInfo){
		this.userInfo = userInfo;
	}
	public FlickrImage(String farmID, String serverID, String imgID,
			String secret, String title, String owner, boolean isNew, boolean isPersonal) {
		super();
		this.farmID = farmID;
		this.serverID = serverID;
		this.imgID = imgID;
		this.secret = secret;
		this.title = title;
		this.owner = owner;
		this.unseen = isNew;
		this.personal = isPersonal;
	}
	public FlickrImage(){
		this.unseen = false;
	}
	@Override
	public String get128ImageUrl() {
		return "http://farm" + farmID + ".static.flickr.com/" + serverID + "/" + imgID + "_" + secret + "_t.jpg";
	}
	@Override
	public String get256ImageUrl() {
		return "http://farm" + farmID + ".static.flickr.com/" + serverID + "/" + imgID + "_" + secret + "_m.jpg"; // 240, but 500 is waaaay too large!
	}
	@Override
	public String getBigImageUrl() {
		if(sizes == null){
			sizes = FlickrFeeder.getImageSizes(imgID);
		}
		if(sizes == null){
			return null;
		}
		if(sizes.getMediumUrl() != null) {
			return sizes.getMediumUrl();
		}
		if(sizes.getSmallUrl() != null) {
			return sizes.getSmallUrl();
		}
		return null;
	}
	@Override
	public String getOriginalImageUrl() {
		if(sizes == null){
			sizes = FlickrFeeder.getImageSizes(imgID);
		}
		if(sizes == null){
			return null;
		}
		if(sizes.getOriginalUrl() != null) {
			return sizes.getOriginalUrl();
		}
		if(sizes.getMediumUrl() != null) {
			return sizes.getMediumUrl();
		}
		if(sizes.getSmallUrl() != null) {
			return sizes.getSmallUrl();
		}
		return null;
	}
	
	@Override
	public String getImagePageUrl() {
		return "http://m.flickr.com/photos/" + owner + "/" + imgID;
	}
	
	@Override
	public Intent follow(){
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.addCategory(Intent.CATEGORY_BROWSABLE);
		intent.setData(Uri.parse(getImagePageUrl()));
		return intent;
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
		sb.append(farmID);
		sb.append(nl);
		sb.append(serverID);
		sb.append(nl);
		sb.append(imgID);
		sb.append(nl);
		sb.append(secret);
		sb.append(nl);
		sb.append(URLEncoder.encode(title));
		sb.append(nl);
		sb.append(URLEncoder.encode(owner));
		// Person info
		if(userInfo != null){
			sb.append(nl);
			sb.append(userInfo.getUsername());
			sb.append(nl);
			sb.append(userInfo.getRealName());
			sb.append(nl);
			sb.append(userInfo.getUrl());
		}else{
			sb.append(nl);
			sb.append(nl);
			sb.append(nl);
			sb.append(nl);
			sb.append(nl);
			sb.append(nl);
		}
		return sb.toString(); 
	}
	@Override
	public void parseInfo(DataInputStream is, Bitmap bmp) throws IOException {
		width = Float.parseFloat(is.readLine());
		height = Float.parseFloat(is.readLine());
		farmID = is.readLine();
		serverID = is.readLine();
		imgID = is.readLine();
		secret = is.readLine();
		title = URLDecoder.decode(is.readLine());
		owner = URLDecoder.decode(is.readLine());
		userInfo = new FlickrUserInfo();
		userInfo.setUsername(is.readLine());
		userInfo.setRealName(is.readLine());
		userInfo.setUrl(is.readLine());
		this.bitmap = bmp;
	}
	public boolean isNew(){
		return unseen;
	}
	public void setOld(){
		unseen = false;
	}
	public boolean isPersonal(){
		return personal;
	}
	@Override
	public float getHeight() {
		return height;
	}
	@Override
	public float getWidth() {
		return width;
	}
}
