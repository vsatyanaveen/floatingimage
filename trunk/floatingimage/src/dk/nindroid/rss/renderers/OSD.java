package dk.nindroid.rss.renderers;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLUtils;
import android.util.Log;
import android.view.WindowManager;
import dk.nindroid.rss.Display;
import dk.nindroid.rss.R;
import dk.nindroid.rss.RiverRenderer;
import dk.nindroid.rss.ShowStreams;
import dk.nindroid.rss.gfx.Vec3f;
import dk.nindroid.rss.uiActivities.ToggleNotificationBar;

public class OSD {
	private static final long 		FADE_TIME = 400;
	private static final long 		SHOW_TIME = 4000;
	private static final int		VERTS = 4;
	private static final float		BRIGHTNESS_LOW  = 0.1f;
	private static final float		BRIGHTNESS_MID  = 0.5f;
	private static final float		BRIGHTNESS_HIGH = 1.0f;
	private static final float[] 	BRIGHTNESS = new float[]{BRIGHTNESS_LOW, BRIGHTNESS_MID, BRIGHTNESS_HIGH};
	
	private boolean				mExtendDisplayTime = false;
	private int 				mBrightnessIndex = 0; 
	
	long 						mInputTime;
	float						mFraction = 0.0f;
	boolean 					mPlaying = true;
	boolean 					mShowing;
	
	private static Vec3f[]		mVertices;
	private static final int 	one = 0x10000;
	private static IntBuffer	mVertexBuffer;
	private static ByteBuffer	mIndexBuffer;
	
	private static Bitmap 		mBrightness;
	private static Bitmap 		mImages;
	private static Bitmap 		mPause;
	private static Bitmap 		mPlay;
	private static Bitmap 		mSettings;
	
	private static int 			mTexIdBrightness;
	private static int 			mTexIdImages;
	private static int 			mTexIdPause;
	private static int 			mTexIdPlay;
	private static int 			mTexIdSettings;
	
	private static FloatBuffer 	mTexBuffer;
	
	static {
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
	
	public static void init(Context context){
		InputStream is = context.getResources().openRawResource(R.drawable.osd_brightness);
		mBrightness = BitmapFactory.decodeStream(is);
		is = context.getResources().openRawResource(R.drawable.osd_images);
		mImages = BitmapFactory.decodeStream(is);
		is = context.getResources().openRawResource(R.drawable.osd_pause);
		mPause = BitmapFactory.decodeStream(is);
		is = context.getResources().openRawResource(R.drawable.osd_play);
		mPlay = BitmapFactory.decodeStream(is);
		is = context.getResources().openRawResource(R.drawable.osd_settings);
		mSettings = BitmapFactory.decodeStream(is);
	}
	
	public void init(GL10 gl){
		int[] textures = new int[5];
		gl.glGenTextures(5, textures, 0);
		mTexIdBrightness = textures[0];
		mTexIdImages = textures[1];
		mTexIdPause = textures[2];
		mTexIdPlay = textures[3];
		mTexIdSettings = textures[4];
		
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
        
		setTexture(gl, mBrightness, mTexIdBrightness);
		setTexture(gl, mImages, mTexIdImages);
		setTexture(gl, mPause, mTexIdPause);
		setTexture(gl, mPlay, mTexIdPlay);
		setTexture(gl, mSettings, mTexIdSettings);
		
	}
	
	public void draw(GL10 gl, long realtime){
		if(mExtendDisplayTime){
			mExtendDisplayTime = false;
			mInputTime = realtime - FADE_TIME;
		}
		long t = realtime - mInputTime;
		if(t < FADE_TIME){
			// Fade in
			mFraction = ((float)t) / FADE_TIME;
		}else if(t < FADE_TIME + SHOW_TIME){
			ShowStreams.current.runOnUiThread(new ToggleNotificationBar(true));
			mShowing = true;
			mFraction = 1.0f;
		}else if(t < 2 * FADE_TIME + SHOW_TIME){
			// Fade out
			mFraction = 1.0f - (((float)t - FADE_TIME - SHOW_TIME)) / FADE_TIME;
		}else{
			if(mShowing){
				mFraction = 0.0f;
				mShowing = false;
				ShowStreams.current.runOnUiThread(new ToggleNotificationBar(false));
			}
		}
		drawOSD(gl);
	}
	
	private void drawOSD(GL10 gl){
		gl.glPushMatrix();
		gl.glDisable(GL10.GL_DEPTH_TEST);
		gl.glDisable(GL10.GL_DEPTH_BITS);
		gl.glEnable(GL10.GL_BLEND);
		float barHeight = 80.0f / RiverRenderer.mDisplay.getHeightPixels() * RiverRenderer.mDisplay.getHeight();
		boolean upsideDown = false;
		if(RiverRenderer.mDisplay.getOrientation() == Display.UP_IS_DOWN){
			float offset = 2 * (RiverRenderer.mDisplay.getHeight() - barHeight);
			gl.glTranslatef(0.0f, offset, 0.0f);
			upsideDown = true;
		}
		gl.glColor4f(1.0f, 1.0f, 1.0f, mFraction * 0.5f);
		drawBackBar(gl, barHeight);
		gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
		drawIcons(gl, upsideDown);
		gl.glDisable(GL10.GL_BLEND);
		gl.glEnable(GL10.GL_DEPTH_BITS);
		gl.glEnable(GL10.GL_DEPTH_TEST);
		gl.glPopMatrix();
	}
	
	private void drawIcons(GL10 gl, boolean upsideDown){
		float width = 64.0f / RiverRenderer.mDisplay.getWidthPixels() * RiverRenderer.mDisplay.getWidth();
		float height = 64.0f / RiverRenderer.mDisplay.getHeightPixels() * RiverRenderer.mDisplay.getHeight();
		float yPos = -RiverRenderer.mDisplay.getHeight() + 40.0f / RiverRenderer.mDisplay.getHeightPixels() * RiverRenderer.mDisplay.getHeight() + height * 0.5f;
		float dx = RiverRenderer.mDisplay.getWidth() * 0.5f;
		gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
		gl.glTexEnvx(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE, GL10.GL_MODULATE);
		gl.glFrontFace(GL10.GL_CCW);
		gl.glEnable(GL10.GL_TEXTURE_2D);
		gl.glActiveTexture(GL10.GL_TEXTURE0);
				
        gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
        
        float outsideLeft = -RiverRenderer.mDisplay.getWidth() - width;
        float outsideBottom;
        if(!upsideDown){
        	outsideBottom = -RiverRenderer.mDisplay.getHeight() - height;
        }else{
        	outsideBottom = yPos + 2.4f * height; // Argh, magic variable! Should be 2!
        }
        float outsideRight = RiverRenderer.mDisplay.getWidth() + width;
        
		gl.glPushMatrix();
			gl.glTranslatef(getPos(outsideLeft, -1.5f * dx), yPos, -1.0f);	
			gl.glScalef(width, height, 1.0f);	
			drawIcon(gl, mTexIdBrightness, mTexBuffer);
		gl.glPopMatrix();
		gl.glPushMatrix();
			gl.glTranslatef(-0.5f * dx, getPos(outsideBottom, yPos), -1.0f);	
			gl.glScalef(width, height, 1.0f);
			drawIcon(gl, mTexIdImages, mTexBuffer);
		gl.glPopMatrix();
		gl.glPushMatrix();
			gl.glTranslatef(0.5f * dx, getPos(outsideBottom, yPos), -1.0f);
			gl.glScalef(width, height, 1.0f);
			if(mPlaying){
				drawIcon(gl, mTexIdPause, mTexBuffer);
			}else{
				drawIcon(gl, mTexIdPlay, mTexBuffer);
			}
		gl.glPopMatrix();
		gl.glPushMatrix();
			gl.glTranslatef(getPos(outsideRight, 1.5f * dx), yPos, -1.0f);
			gl.glScalef(width, height, 1.0f);
			drawIcon(gl, mTexIdSettings, mTexBuffer);
		gl.glPopMatrix();
	}
	
	private float getPos(float notShowing, float showing){
		return notShowing + (showing - notShowing) * smoothstep(mFraction);
	}
	
	private void drawIcon(GL10 gl, int texId, FloatBuffer texBuffer){
        gl.glBindTexture(GL10.GL_TEXTURE_2D, texId);
        gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, texBuffer);
        gl.glDrawElements(GL10.GL_TRIANGLE_STRIP, 4, GL10.GL_UNSIGNED_BYTE, mIndexBuffer);
	}
	
	private float smoothstep(float val){
		return Math.min(val * val * (3.0f - 2.0f * val), 1.0f);
	}
	
	private void drawBackBar(GL10 gl, float height){
		gl.glPushMatrix();
			//Point to our vertex buffer
			gl.glVertexPointer(3, GL10.GL_FIXED, 0, mVertexBuffer);
			gl.glTranslatef(0.0f, -RiverRenderer.mDisplay.getHeight() + height, -1.0f);
			gl.glScalef(RiverRenderer.mDisplay.getWidth(), height, 1.0f);
			gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
			//Draw the vertices as triangle strip
			gl.glBlendFunc(GL10.GL_ZERO, GL10.GL_ONE_MINUS_SRC_ALPHA);
			gl.glDrawElements(GL10.GL_TRIANGLE_STRIP, 4, GL10.GL_UNSIGNED_BYTE, mIndexBuffer);
		gl.glPopMatrix();
	}
	
	public void show(long realtime){
		if(realtime - mInputTime < FADE_TIME + SHOW_TIME){
			// Don't fade again!
			mInputTime = realtime - FADE_TIME;
		}else{
			mInputTime = realtime;
		}
	}
	
	public void hide(long realtime){
		if(mFraction != 0){
			mInputTime = realtime - FADE_TIME - SHOW_TIME;
		}
	}
	
	private void setBrightness() {
		WindowManager.LayoutParams lp = ShowStreams.current.getWindow().getAttributes();
		mBrightnessIndex = (mBrightnessIndex + 1) % BRIGHTNESS.length;
		lp.screenBrightness = BRIGHTNESS[mBrightnessIndex];
		ShowStreams.current.getWindow().setAttributes(lp);
	}
	
	public boolean click(float x, float y){
		boolean inButtonArea = false;
		if(RiverRenderer.mDisplay.getOrientation() == Display.UP_IS_DOWN){
			inButtonArea = y < 80;
		}else{
			inButtonArea = y > RiverRenderer.mDisplay.getHeightPixels() - 80;
		}
		if(mFraction != 0.0f && inButtonArea){
			mExtendDisplayTime = true;
			float xPos = x / RiverRenderer.mDisplay.getWidthPixels() * 4;
			if(xPos < 1.0f){
				Log.v("Floating Image", "Change brightness");
				setBrightness();
			}else if(xPos < 2.0f){
				Log.v("Floating Image", "Choose pictures");
				ShowStreams.current.showFolder();
			}else if(xPos < 3.0f){
				Log.v("Floating Image", "Play/Pause");
			}else{
				Log.v("Floating Image", "Settings");
				ShowStreams.current.showSettings();
			}
			return true;
		}
		return false;
	}
	
	private void setTexture(GL10 gl, Bitmap bmp, int textureID){
        gl.glBindTexture(GL10.GL_TEXTURE_2D, textureID);
        try{
        	gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
            gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
            gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
            gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
            gl.glTexEnvf(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE, GL10.GL_BLEND);
            
        	GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bmp, 0);
        }catch(IllegalArgumentException e){ // TODO: Handle graciously
        	Log.w("Floating Image", "Could not set OSD texture", e);
        }
        //bmp.recycle();
	}
	
	interface EventHandler{
		void Play();
		void Pause();
	}
}
