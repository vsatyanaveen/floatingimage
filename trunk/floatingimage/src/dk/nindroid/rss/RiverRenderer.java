package dk.nindroid.rss;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.util.Log;
import dk.nindroid.rss.Image.Pos;
import dk.nindroid.rss.data.Ray;
import dk.nindroid.rss.gfx.Vec2f;
import dk.nindroid.rss.gfx.Vec3f;

public class RiverRenderer implements GLSurfaceView.Renderer {
	private static final long	SPLASHTIME = 2000l;
	private int 			mScreenWidth;
	private int 			mScreenHeight;
	private float			mFarRight;
	private boolean 		mTranslucentBackground = false;
	private boolean 		mNewStart = true;
	private Image[] 		mImgs;
	private TextureBank 	mBank;
	private long 			mTraversal = 30000;
	private long 			mInterval;
	private int 			mTotalImgRows = 6;
	private int 			mImgCnt = 0;
	private boolean 		mCreateMiddle = true;
	private long 			mOffset = 0;
	private float			mFadeOffset = 0;
	private long			mUpTime;
	private Vec2f 			mClickedPos = new Vec2f();
	private boolean 		mClicked = false;
	private static final float mSensitivityX = 20.0f;
	private static final Vec3f mCamPos = new Vec3f(0,0,0);
	private Image			mSelected = null;
	private Image			mSplashImg;
	private long			mDefocusSplashTime;
	//private BackgroundPainter backdrop;
	//private static final Vec3f mCamDir = new Vec3f(0,0,-1);
	//private long 			mPauseTime = 0; 
	
	public RiverRenderer(boolean useTranslucentBackground){
		mFarRight = 7.0f;
		mTranslucentBackground = useTranslucentBackground;
		mBank = new TextureBank(5);
		mImgs = new Image[mTotalImgRows * 3 / 2];
		mInterval = mTraversal / mTotalImgRows;
		long curTime = new Date().getTime();
		//backdrop = new BackgroundPainter(curTime);
		long creationOffset = 0;
		for(int i = 0; i < mTotalImgRows; ++i){
			
			if(mCreateMiddle){
	        	mImgs[mImgCnt++] = new Image(mBank, mTraversal, 0.8f, 0.5f, 1.5f, Pos.MIDDLE, curTime - creationOffset, mFarRight);
	        }else{
	        	mImgs[mImgCnt++] = new Image(mBank, mTraversal, 0.8f, 0.5f, 1.5f, Pos.UP, curTime - creationOffset, mFarRight);
	        	mImgs[mImgCnt++] = new Image(mBank, mTraversal, 0.8f, 0.5f, 1.5f, Pos.DOWN, curTime - creationOffset, mFarRight);
	        }
	        	mCreateMiddle ^= true;
	        	creationOffset += mInterval;
		}
	}
	@Override
	public void onDrawFrame(GL10 gl) {
		String dataFolder = ShowStreams.current.getString(R.string.dataFolder);
		File error = new File(dataFolder + "openGL.err");
		
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(error);
		} catch (FileNotFoundException e) {
			Log.e("dk.nindroid.rss.RiverRenderer", "Cannot error stream.", e);
		}
		try{
			gl.glTexEnvx(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE,
	                GL10.GL_MODULATE);
			gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
			
			gl.glMatrixMode(GL10.GL_MODELVIEW);
	        gl.glLoadIdentity();
	        gl.glMatrixMode(GL10.GL_PROJECTION);
	        //gl.glLoadIdentity();
	        gl.glMatrixMode(GL10.GL_MODELVIEW);
	        //GLU.gluLookAt(gl, mCamDir.getX(), mCamDir.getY(), mCamDir.getZ(), mCamPos.getX(), mCamPos.getY(), mCamPos.getZ(), 0.0f, 1.0f, 0.0f);
	        GLU.gluLookAt(gl, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f, 1.0f, 0.0f);
	        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
	        gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
	        
	        gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT,
	                GL10.GL_FASTEST);
	        //gl.glScalef(0.25f, 0.25f, 1.0f);
	        long realTime = new Date().getTime();
	        fadeOffset(realTime);
	        long time = realTime + mOffset;
	        
	        if(mNewStart){
				mSplashImg = mImgs[4];
				Bitmap splash = BitmapFactory.decodeStream(ShowStreams.current.getResources().openRawResource(R.drawable.splash));
				mSplashImg.setSelected(gl, splash, 1.0f, 400.0f/512.0f, time);
				mNewStart = false;
				mDefocusSplashTime = time + SPLASHTIME;
			}
	        if(mSplashImg != null && realTime > mDefocusSplashTime){
	        	mSplashImg.select(gl, time, realTime);
	        	mSplashImg = null;
	        }
	        
	        if(mSelected != null){
	        	if(mClicked){
		        	mClicked = false;
		        	if(mSelected.inFocus()){
			        	mSelected.select(gl, time, realTime);
			        	mSelected = null;
		        	}
	        	}
	        }else{
	        	if(mClicked){
		        	// Only works for camera looking directly at (0, 0, -1)...
		        	Vec3f rayDir = new Vec3f(mClickedPos.getX(), mClickedPos.getY(), -1);
		        	rayDir.normalize();
		        	Ray r = new Ray(mCamPos, rayDir);
		        	int i = 0;
		        	Image selected = null;
		        	float closest = Float.MAX_VALUE;
		        	float t;
		        	for(; i < mImgCnt; ++i){
		        		t = mImgs[i].intersect(r); 
		            	if(t > 0 && t < closest){
		            		closest = t;
		            		selected = mImgs[i];
		            	}
		            }
		        	if(selected != null){
		        		mSelected = selected;
		        		selected.select(gl, time, realTime);
		        	}
		        	mClicked = false;
		        }
	        }
	        
	        for(int i = 0; i < mImgCnt; ++i){
	        	mImgs[i].draw(gl, time, realTime);
	        }
	        //backdrop.draw(gl, mOffset);
	        gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
	        gl.glEnable(GL10.GL_TEXTURE_2D);
	        gl.glDepthMask(false);
	        for(int i = 0; i < mImgCnt; ++i){
	        	mImgs[i].drawGlow(gl);
	        }
	        gl.glDepthMask(true);
	        if(mSelected != null && mSelected.inFocus()){
	        	InfoBar.draw(gl);
	        }
		}catch(Throwable t){
			try {
				Log.e("Floating Image", "Uncaught exception caught!", t);
				fos.write(t.getMessage().getBytes());
				fos.flush();
				fos.close();
			} catch (IOException e) {
			}
		}
	}
	
	private void fadeOffset(long time) {
		float timeFactor = (3000 - (time - mUpTime)) / 3000.0f;
		float fadeOffset = mFadeOffset * timeFactor * timeFactor * mSensitivityX;
		if(fadeOffset > 2.0f || fadeOffset < -2.0f){
			mOffset += fadeOffset;
		}else{
			mFadeOffset = 0.0f;
		}
	}
	public Intent followSelected(){
		if(mSelected != null){
			return mSelected.getShowing().follow();
		}
		return null;
	}
	
	public void onResume(){
		mBank.start();
		mFadeOffset = 0.0f;
	}
	public void onPause(){
		mBank.stop();
	}
	
	public int[] getConfigSpec() {
		if (mTranslucentBackground) {
            // We want a depth buffer and an alpha buffer
            int[] configSpec = {
                    EGL10.EGL_RED_SIZE,      8,
                    EGL10.EGL_GREEN_SIZE,    8,
                    EGL10.EGL_BLUE_SIZE,     8,
                    EGL10.EGL_ALPHA_SIZE,    8,
                    EGL10.EGL_DEPTH_SIZE,   16,
                    EGL10.EGL_NONE
            };
            return configSpec;
        } else {
            // We want a depth buffer, don't care about the
            // details of the color buffer.
            int[] configSpec = {
                    EGL10.EGL_DEPTH_SIZE,   16,
                    EGL10.EGL_NONE
            };
            return configSpec;
        }
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		gl.glViewport(0, 0, width, height);
		mScreenWidth = width;
		mScreenHeight = height;
		
		Log.v("RiverRenderer", "Dimensions: " + width + "x" + height);
        /*
         * Set our projection matrix. This doesn't have to be done
         * each time we draw, but usually a new projection needs to
         * be set when the viewport is resized.
         */

        float ratio = (float) width / height;
        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadIdentity();
        gl.glFrustumf(-ratio, ratio, -1, 1, 1, 50);
        InfoBar.setView(width, height);
	}
	
	public void xChanged(float amount){
		mOffset += amount * mSensitivityX;
	}
	
	public void moveRelease(float speed, long time){
		mFadeOffset = speed;
		mUpTime = time;
	}
	
	public void onClick(float x, float y){
		mClicked = true;
		mClickedPos = new Vec2f(x/mScreenWidth * 2.0f - 1.0f, -(y / mScreenHeight * 2.0f - 1.0f));
	} 
 
	@Override 
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		gl.glMatrixMode(GL10.GL_TEXTURE);
		gl.glLoadIdentity();
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		long time = new Date().getTime();
		for(int i = 0; i < mImgCnt; ++i){
			mImgs[i].init(gl, time + mOffset);
		}
		if(mSelected != null){
			InfoBar.select(gl, mSelected.getShowing());
		}
		/*
         * By default, OpenGL enables features that improve quality
         * but reduce performance. One might want to tweak that
         * especially on software renderer.
         */
        gl.glDisable(GL10.GL_DITHER);
        /*
         * Some one-time OpenGL initialization can be made here
         * probably based on features of this particular context
         */
         gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT,
                 GL10.GL_FASTEST);
         gl.glEnable(GL10.GL_TEXTURE_2D);
         

         if (mTranslucentBackground) {
             gl.glClearColor(0,0,0,0);
         } else {
             gl.glClearColor(0,0,0,1);
         }
         gl.glEnable(GL10.GL_CULL_FACE);
         gl.glShadeModel(GL10.GL_SMOOTH);
         gl.glEnable(GL10.GL_DEPTH_TEST);
       	 GlowImage.initTexture(gl);
	}

}