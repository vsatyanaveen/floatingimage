package dk.nindroid.rss.renderers;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Calendar;

import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.opengl.GLU;
import android.util.Log;
import dk.nindroid.rss.Display;
import dk.nindroid.rss.settings.Settings;

public class Clock {
	private static final int HALF_HOUR = 1800000;
	private boolean mLarge;
	int mLastMinute = -1;
	Bitmap mBmp;
	Canvas mCanvas;
	Calendar mCal;
	long mLastUpdated;
	java.text.DateFormat mTimeFormat;
	java.text.DateFormat mDateFormat;
	Paint mTimePaint;
	Paint mDatePaint;
	
	
	// GL Stuff
	private int mTextureID = -1;
	private FloatBuffer mTexBuffer;
	private IntBuffer   mVertexBuffer;
	private ByteBuffer  mIndexBuffer;
	
	public Clock(Context context, int maxSide){
		if(maxSide > 1100){
			mLarge = true;
		}else{
			mLarge = false;
		}
		int width, height;
		if(mLarge){
			width = 512;
			height = 128;
		}else{
			width = 256;
			height = 64;
		}
		mBmp = Bitmap.createBitmap(width, height, Config.ARGB_4444);
		mCanvas = new Canvas(mBmp);
		mCal = Calendar.getInstance();
		mTimeFormat = java.text.DateFormat.getTimeInstance(java.text.DateFormat.SHORT);
		mDateFormat = java.text.DateFormat.getDateInstance(java.text.DateFormat.FULL);
		mTimePaint = new Paint();
		mTimePaint.setStrokeWidth(mLarge ? 2.0f : 1.0f);
		mTimePaint.setAntiAlias(true);
		mTimePaint.setDither(true);
		mTimePaint.setTextSize(mLarge ? 100 : 45);
		mDatePaint = new Paint();
		mDatePaint.setStrokeWidth(mLarge ? 0.5f : 0.25f);
		mDatePaint.setAntiAlias(true);
		mDatePaint.setDither(true);
		mDatePaint.setTextSize(mLarge ? 28 : 19);
		initPlane();
	}
	
	private void initPlane(){
		mVertexBuffer = GLHelper.createVertices();
		mIndexBuffer = GLHelper.createIndexBuffer();
	}
	
	public void setTexture(GL10 gl){	
		if(mTextureID == -1){
			mTextureID = GLHelper.createTexture(gl);
		}
		
		float tex[] = {
        	0.0f,  0.0f,
        	0.0f,  1.0f,	
        	1.0f,  0.0f,
        	1.0f,  1.0f,
        };
		
		GLHelper.setTexture(gl, mBmp, mTextureID);
		mTexBuffer = GLHelper.createTexBuffer(tex);
	}
	
	public void resume(GL10 gl){
		setTexture(gl);
		mLastUpdated = 0;
	}
	
	public void update(GL10 gl, Display display, Settings settings){
		if(!settings.clockVisible) return;
		
		long now = System.currentTimeMillis();
		if(now - mLastUpdated > HALF_HOUR){
			updateCanvas(gl, settings, now);
		}else{
			mCal.setTimeInMillis(System.currentTimeMillis());
			int minute = mCal.get(Calendar.MINUTE);
			if(minute != mLastMinute){
				updateCanvas(gl, settings, now);
			}
		}
		
		draw(gl, 1, display, settings);
	}

	private void draw(GL10 gl, float scale, Display display, Settings settings) {
		gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
        gl.glEnable(GL10.GL_TEXTURE_2D);
		gl.glFrontFace(GL10.GL_CCW);
		gl.glEnable(GL10.GL_BLEND);
		gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		gl.glTexEnvx(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE, GL10.GL_MODULATE);
		
		gl.glPushMatrix();
			float width = mBmp.getWidth() / (float)display.getPortraitHeightPixels() * scale;
			float height = mBmp.getWidth() / (float)display.getPortraitHeightPixels() * scale * 0.25f;
			float xPos, yPos;
			xPos = display.getWidth() / 2.0f * settings.clockXPosition;
			yPos = display.getHeight() / 2.0f * settings.clockYPosition;
			xPos = Math.min(display.getWidth() / 2.0f - width, xPos);
			xPos = Math.max(-display.getWidth() / 2.0f + width, xPos);
			
			gl.glTranslatef(xPos, yPos, 0.0f);
			gl.glScalef(width, height, 1);
			GLHelper.draw(gl, mTextureID, mVertexBuffer, mTexBuffer, mIndexBuffer);
        gl.glPopMatrix();
        
        gl.glDisable(GL10.GL_BLEND);
		gl.glDisable(GL10.GL_TEXTURE_2D);
		
		int error = gl.glGetError();
		if(error != 0){
			Log.e("Floating Image", "GL Error painting clock: " + GLU.gluErrorString(error));
		}
	}

	private void updateCanvas(GL10 gl, Settings settings, long now) {
		mCal.setTimeInMillis(now);
		mLastMinute = mCal.get(Calendar.MINUTE);
		mLastUpdated = now;
		mBmp.eraseColor(Color.TRANSPARENT);
		String time = mTimeFormat.format(mCal.getTime());
		String date = mDateFormat.format(mCal.getTime());
		
		float timeOffset = 0;
		float dateOffset = 0;
		
		float timeWidth = mTimePaint.measureText(time);
		float dateWidth = mDatePaint.measureText(date);
		if(dateWidth > mBmp.getWidth()){
			mDateFormat = java.text.DateFormat.getDateInstance(java.text.DateFormat.LONG);
			dateWidth = mDatePaint.measureText(date);
		}
		
		if(settings.clockXPosition > -0.4f){
			if(settings.clockXPosition > 0.4f){
				timeOffset = mBmp.getWidth() - timeWidth;
				dateOffset = mBmp.getWidth() - dateWidth;
			}else{
				timeOffset = mBmp.getWidth() / 2.0f - timeWidth / 2.0f;
				dateOffset = mBmp.getWidth() / 2.0f - dateWidth / 2.0f;
			}
		}
		
		float timeYOffset, dateYOffset;
		if(mLarge){
			timeYOffset = 42.0f;
			dateYOffset = 10.0f;
		}else{
			timeYOffset = 26.0f;
			dateYOffset = 5.0f;
		}
		
		mTimePaint.setStyle(Style.FILL);
		mTimePaint.setColor(Color.WHITE);
		mCanvas.drawText(time, timeOffset, mBmp.getHeight() - timeYOffset, mTimePaint);
		mDatePaint.setStyle(Style.FILL);
		mDatePaint.setColor(Color.WHITE);
		mCanvas.drawText(date, dateOffset, mBmp.getHeight() - dateYOffset, mDatePaint);
		
		mTimePaint.setStyle(Style.STROKE);
		mTimePaint.setColor(Color.BLACK);
		mCanvas.drawText(time, timeOffset, mBmp.getHeight() - timeYOffset, mTimePaint);
		mDatePaint.setStyle(Style.STROKE);
		mDatePaint.setColor(Color.BLACK);
		mCanvas.drawText(date, dateOffset, mBmp.getHeight() - dateYOffset, mDatePaint);
		
		setTexture(gl);
	}
}
