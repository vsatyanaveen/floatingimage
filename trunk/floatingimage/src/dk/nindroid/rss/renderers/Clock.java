package dk.nindroid.rss.renderers;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
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
import android.opengl.GLUtils;
import android.util.Log;
import dk.nindroid.rss.Display;
import dk.nindroid.rss.gfx.Vec3f;
import dk.nindroid.rss.settings.Settings;

public class Clock {
	private static final float SCALE = 0.25f;
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
	private Vec3f[]		mVertices;
	private IntBuffer   mVertexBuffer;
	private ByteBuffer  mIndexBuffer;
	
	private static final int VERTS = 4;
	
	public Clock(Context context, int maxSide){
		if(maxSide > 1300){
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
		ByteBuffer tbb = ByteBuffer.allocateDirect(VERTS * 2 * 4);
        tbb.order(ByteOrder.nativeOrder());
        mTexBuffer = tbb.asFloatBuffer();
        
        float tex[] = {
        	0.0f,  0.0f,
        	0.0f,  0.0f,	
        	0.0f,  0.0f,
        	0.0f,  0.0f,
        };
        mTexBuffer.put(tex);
        mTexBuffer.position(0);
		
		
		int one = 0x10000;
		int vertices[] = {
			 -one,  one, -one,
			 -one, -one, -one,
			  one,  one, -one,
			  one, -one, -one
			  };
		
		byte indices[] = {
				 0, 1, 2, 3
		};
		
		mVertices = new Vec3f[4];
		for(int i = 0; i < 4; ++i){
			Vec3f p = new Vec3f(vertices[i*3] / one, vertices[i*3 + 1] / one, vertices[i*3 + 2] / one);
			mVertices[i] = p;
		}
				
		ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length*4);
		vbb.order(ByteOrder.nativeOrder());
		mVertexBuffer = vbb.asIntBuffer();
		mVertexBuffer.put(vertices);
		mVertexBuffer.position(0);
		
		mIndexBuffer = ByteBuffer.allocateDirect(indices.length);
		mIndexBuffer.put(indices);
		mIndexBuffer.position(0);
	}
	
	public void setTexture(GL10 gl){	
		if(mTextureID == -1){
			int[] textures = new int[1];
	        gl.glGenTextures(1, textures, 0);
			mTextureID = textures[0];
		}
        gl.glBindTexture(GL10.GL_TEXTURE_2D, mTextureID);

        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER,
                GL10.GL_NEAREST);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D,
                GL10.GL_TEXTURE_MAG_FILTER,
                GL10.GL_LINEAR);

        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S,
                GL10.GL_CLAMP_TO_EDGE);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T,
                GL10.GL_CLAMP_TO_EDGE);

        gl.glTexEnvf(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE,
                GL10.GL_BLEND);
        
        GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, mBmp, 0);
		        
        ByteBuffer tbb = ByteBuffer.allocateDirect(VERTS * 2 * 4);
        tbb.order(ByteOrder.nativeOrder());
        mTexBuffer = tbb.asFloatBuffer();
        
        float tex[] = {
        	0.0f,  0.0f,
        	0.0f,  1.0f,	
        	1.0f,  0.0f,
        	1.0f,  1.0f,
        };
        mTexBuffer.put(tex);
        mTexBuffer.position(0);
        
        // Set drawing params
        //gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S,GL10.GL_REPEAT);
        //gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_REPEAT);
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
			//float width = 2.0f * SCALE * scale;
			//float height = 2.0f * SCALE * scale * 0.25f;
			float xPos;
			float yPos;
			xPos = display.getWidth() / 2.0f * settings.clockXPosition;
			yPos = display.getHeight() / 2.0f * settings.clockYPosition;
			xPos = Math.min(display.getWidth() / 2.0f - width, xPos);
			xPos = Math.max(-display.getWidth() / 2.0f + width, xPos);
			
			/*
			if(settings.clockPosition == Settings.CLOCK_POS_BOTTOM_LEFT || settings.clockPosition == Settings.CLOCK_POS_TOP_LEFT){
				xPos = -display.getWidth() / 2.0f + width + 0.05f;
			}else{
				xPos = display.getWidth() / 2.0f - width - 0.05f;
			}
			if(settings.clockPosition == Settings.CLOCK_POS_BOTTOM_LEFT || settings.clockPosition == Settings.CLOCK_POS_BOTTOM_RIGHT){
				yPos = -display.getHeight() / 2.0f + height + 0.05f;
			}else{
				yPos = display.getHeight() / 2.0f - height - 0.1f;
			}
			*/
			
			gl.glTranslatef(xPos, yPos, 0.0f);
			gl.glScalef(width, height, 1);
			gl.glActiveTexture(GL10.GL_TEXTURE0);
	        gl.glBindTexture(GL10.GL_TEXTURE_2D, mTextureID);
			gl.glVertexPointer(3, GL10.GL_FIXED, 0, mVertexBuffer);
			gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, mTexBuffer);
	        gl.glDrawElements(GL10.GL_TRIANGLE_STRIP, 4, GL10.GL_UNSIGNED_BYTE, mIndexBuffer);
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
		
		if(settings.clockXPosition > -0.5f){
			if(settings.clockXPosition > 0.5f){
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
