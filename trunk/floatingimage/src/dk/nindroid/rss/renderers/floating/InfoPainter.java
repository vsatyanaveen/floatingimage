package dk.nindroid.rss.renderers.floating;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

public class InfoPainter {
	int 	mTitleSize;
	int 	mAuthorSize;
	int 	mWidth;
	int		mHeight;
	int mTitleContinuedWidth;
	int mAuthorContinuedWidth;
	
	String 	mTitle;
	String 	mAuthor;
	Paint 	mPainter;
	Bitmap 	mBmp;
	
	public InfoPainter(int titleSize, int authorSize){
		this.mTitleSize = titleSize;
		this.mAuthorSize = authorSize;
		mPainter = new Paint();
		mPainter.setAntiAlias(true);
		//mPainter.setTextSize(titleSize);
		//mPainter.setColor(0);
		mTitleContinuedWidth = (int)Math.ceil(mPainter.measureText("..."));
		mPainter.setTextSize(authorSize);
		mAuthorContinuedWidth = (int)Math.ceil(mPainter.measureText("..."));
	}
	
	public void setInfo(String title, String author, int width, int height){
		this.mTitle = title;
		this.mAuthor = author;
		this.mWidth = width;
		this.mHeight = height;
	}
	
	public void paintCanvas(int textWidth, int textHeight){
		mBmp = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_4444);
		Canvas canvas = new Canvas(mBmp);
		
		
		mPainter.setARGB(0xff, 0xF0, 0xF0, 0xF0);
		mPainter.setTextSize(mTitleSize);
		drawTitle(canvas, textWidth);
		mPainter.setTextSize(mAuthorSize);
		mPainter.setARGB(0xC0, 0xC0, 0xC0, 0xC0);
		drawAuthor(canvas, textWidth, textHeight);
	}
	
	protected void drawTitle(Canvas canvas, int textWidth){
		String title1 = mTitle;
		int title1Width;
		String title2 = "";
		int title2Width = 0;
		title1Width = (int)Math.ceil(mPainter.measureText(title1));
		if(title1Width > textWidth){
			int nextSpace = 45; // Somewhere close?
			do {
				nextSpace = mTitle.lastIndexOf(" ", nextSpace - 1);
				if(nextSpace == -1) break;
				title1 = mTitle.substring(0, nextSpace);
				title1Width = (int)Math.ceil(mPainter.measureText(title1));
			}while(title1Width > textWidth);
			
			int lastSpace;
			if(nextSpace != -1){
				title2 = mTitle.substring(nextSpace);
				title2Width = (int)Math.ceil(mPainter.measureText(title2));
				if(title2Width > textWidth){
					lastSpace = mTitle.lastIndexOf(" ");
					while(title2Width > textWidth - mTitleContinuedWidth) {
						lastSpace = mTitle.lastIndexOf(" ", lastSpace - 1);
						if(lastSpace == -1) break;
						title2 = mTitle.substring(nextSpace, lastSpace);
						title2Width = (int)Math.ceil(mPainter.measureText(title2));
					}
					title2 += "...";
					title2Width += mTitleContinuedWidth;
				}
			}
		}
		
		float center1Offset = ((textWidth - title1Width) / 2.0f);
		canvas.drawText(title1, 0, title1.length(), center1Offset, mTitleSize, mPainter);
		float center2Offset = ((textWidth - title2Width) / 2.0f);
		canvas.drawText(title2, 0, title2.length(), center2Offset, mTitleSize * 2, mPainter);
	}
	
	protected void drawAuthor(Canvas canvas, int textWidth, int textHeight){
		String author = mAuthor;
		if(author == null) return;
		int authorWidth = (int)Math.ceil(mPainter.measureText(author));
		if(authorWidth > textWidth){
			int lastSpace = 50;
			do {
				lastSpace = mTitle.lastIndexOf(" ", lastSpace - 1);
				if(lastSpace == -1) break;
				author = mTitle.substring(0, lastSpace);
				authorWidth = (int)Math.ceil(mPainter.measureText(author));
			}while(authorWidth > textWidth + mAuthorContinuedWidth);
			author += "...";
			authorWidth += mAuthorContinuedWidth;
		}
		
		float offset = (textWidth - authorWidth);
		canvas.drawText(author, offset, textHeight - 2, mPainter);
	}
	
	public Bitmap getBitmap(){
		return mBmp;
	}
}
