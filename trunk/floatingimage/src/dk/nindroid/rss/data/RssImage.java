package dk.nindroid.rss.data;

import java.io.DataInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;

// DEPRECATED!
// TODO: BLAH!
public class RssImage implements ImageReference {
	SimpleDateFormat rssDate = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z");
	String title;
	String imageUrl;
	String description;
	Date date;
	Bitmap bitmap;
	public String getImageID(){
		return "";
	}
	public String getID(){
		return "";
	}
	public RssImage(RssElement rss){
		try {
			this.date = rssDate.parse(rss.date);
		} catch (ParseException e) {
			Log.v("ImagePost", "Could not parse date", e);
		}
		this.title = rss.title;
		int imageBegin = rss.description.indexOf("<img src=\"") + 10;
		int imageEnd = rss.description.indexOf("\"", imageBegin);
		this.imageUrl = rss.description.substring(imageBegin, imageEnd);
		int descriptionBegin = rss.description.indexOf("<p>", imageEnd) + 3;
		int descriptionEnd = rss.description.indexOf("</p>", descriptionBegin);
		if(descriptionBegin > imageEnd){
			this.description = rss.description.substring(descriptionBegin, descriptionEnd);
		}else{
			this.description = "";
		}
	}
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("***** " + title + " *****\n");
		sb.append("* Uploaded on " + date + "\n");
		sb.append("* " + imageUrl + "\n");
		sb.append(description.replace("<br />", "\n") + "\n");
		sb.append("***** (_*_) *****");
		return sb.toString();
	}
	public String getTitle() {
		return title;
	}
	public String getUrl() {
		return imageUrl;
	}
	public String getBigUrl() {
		return imageUrl;
	}
	public String getDescription() {
		return description;
	}
	public Date getDate() {
		return date;
	}
	public Bitmap getBitmap(){
		return bitmap;
	}
	public void setBitmap(Bitmap bitmap){
		this.bitmap = bitmap;
	}
	
	@Override
	public String getInfo() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void parseInfo(DataInputStream is, Bitmap bmp) throws IOException {
		// TODO Auto-generated method stub
		
	}
	public boolean isNew(){
		return true;
	}
	public boolean isPersonal(){
		return true;
	}
	public void setOld(){
		
	}
	public String getAuthor(){
		return null;
	}
	@Override
	public void getExtended() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public float getHeight() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public float getWidth() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public void set128Bitmap(Bitmap bitmap) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void set512Bitmap(Bitmap bitmap) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public String getBigImageUrl() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Intent follow() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public String getSmallImageUrl() {
		// TODO Auto-generated method stub
		return null;
	}
	public String getOriginalImageUrl(){
		return null;
	}
	
	@Override
	public String getImagePageUrl() {
		return null;
	}
}
