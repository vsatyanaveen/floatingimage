package dk.nindroid.rss.settings;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import dk.nindroid.rss.R;

public class BoxPlacement extends View {
	Paint mPaint;
	int mPosX, mPosY;
	boolean mResumed = false;
	boolean mVisible = true;
	float mResumePosX, mResumePosY;
	
	public BoxPlacement(Context context) {
		super(context);
		init();
	}
	
	public BoxPlacement(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	public BoxPlacement(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}
	
	private void init(){
		this.setWillNotDraw(false);
		mPaint = new Paint();
		mPaint.setStrokeWidth(2.0f);
		mPaint.setTextSize(40);
	}
	
	@Override
	public void draw(Canvas canvas) {
		if(!mResumed){
			convertPosX(mResumePosX);
			convertPosY(mResumePosY);
			mResumed = true;
		}
		
		canvas.drawColor(Color.GRAY);
		
		int screenWidth = this.getWidth();
		int screenHeight = this.getHeight();
		if(mVisible){
			int width, height;
			if(Math.max(canvas.getWidth(), canvas.getHeight()) > 1100){
				width = 512;
			}else{
				width = 256;
			}
			height = width / 4;
			
			int left, right, top, bottom;
			left = mPosX - width / 2;
			right = mPosX + width / 2;
			top = mPosY - height / 2;
			bottom = mPosY + height / 2;
			
			int diffX, diffY;
			diffX = diffY = 0;
			if(left < 0){
				diffX = -left;
			}else if(right > screenWidth - 1){
				diffX = screenWidth - right - 1;
			}
			left += diffX;
			right += diffX;
			mPosX += diffX;
			
			if(top < 0){
				diffY = -top;
			}else if(bottom > screenHeight - 1){
				diffY = screenHeight - bottom - 1;
			}
			bottom += diffY;
			top += diffY;
			mPosY += diffY;
		
			mPaint.setStyle(Style.FILL);
			mPaint.setARGB(255, 210, 210, 210);
			canvas.drawRect(left, top, right, bottom, mPaint);
			mPaint.setStyle(Style.STROKE);
			mPaint.setColor(Color.BLACK);
			canvas.drawRect(left, top, right, bottom, mPaint);
			
			mPaint.setStyle(Style.FILL);
			float textWidth = mPaint.measureText("Time");
			float textLeft = mPosX - textWidth / 2;
			float textBottom = mPosY + mPaint.getTextSize() / 2;
			canvas.drawText("Clock", textLeft, textBottom, mPaint);
		}else{
			String disabled = getContext().getString(R.string.disabled);
			float textWidth = mPaint.measureText(disabled);
			canvas.drawText(disabled, getWidth() / 2.0f - textWidth / 2.0f, getHeight() / 2.0f - mPaint.getTextSize() / 2.0f, mPaint);
		}
	}
	
	public float getPosX(){
		float halfWidth = getWidth() / 2.0f;
		float zeroInMiddle = mPosX - halfWidth;
		float normalized = zeroInMiddle / halfWidth;
		return normalized;
	}
	
	public float getPosY(){
		float halfHeight = getHeight() / 2.0f;
		float zeroInMiddle = mPosY - halfHeight;
		float normalized = zeroInMiddle / halfHeight;
		return normalized;
	}
	
	public void setPosX(float x){
		mResumePosX = x;
		mResumed = false;
	}
	
	public void setPosY(float y){
		mResumePosY = y;
		mResumed = false;
	}
	
	public void setVisible(boolean visible){
		this.mVisible = visible;
		this.invalidate();
	}
	
	private void convertPosX(float x){
		x += 1.0f; // From 0-2
		x /= 2.0f; // From 0-1
		x *= getWidth();
		mPosX = (int)x;
		this.invalidate();
	}
	
	private void convertPosY(float y){
		y += 1.0f; // From 0-2
		y /= 2.0f; // From 0-1
		y *= getHeight();
		mPosY = (int)y;
		this.invalidate();
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		mPosX = (int)event.getX();
		mPosY = (int)event.getY();
		this.invalidate();
		return true;
	}

	public void center(boolean isX) {
		if(isX){
			mPosX = this.getWidth() / 2;
		}else{
			mPosY = this.getHeight() / 2;
		}
		this.invalidate();
	}
}
