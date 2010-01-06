package dk.nindroid.rss;

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
import dk.nindroid.rss.helpers.MatrixTrackingGL;

public class RiverRenderer implements GLSurfaceView.Renderer {
	public static Display		mDisplay;
	
	public static final long 	mFocusDuration = 300;
	public static long			mSelectedTime;
	public static final float  mFocusX = 0.0f;
	public static final float  mFocusY = 0.0f;
	public static final float  mFocusZ = -1.0f;
	public static final float  mFloatZ = -3.5f;
	public static final float  mJitterX = 0.8f;
	public static final float  mJitterY = 0.5f;
	public static final float  mJitterZ = 1.5f;
	
	private static final long	SPLASHTIME = 2000l;
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
	
	static {
		mDisplay = new Display();
	}
	
	public RiverRenderer(boolean useTranslucentBackground){
		mTranslucentBackground = useTranslucentBackground;
		mBank = new TextureBank(10);
		mImgs = new Image[mTotalImgRows * 3 / 2];
		mInterval = mTraversal / mTotalImgRows;
		long curTime = new Date().getTime();
		long creationOffset = 0;
		for(int i = 0; i < mTotalImgRows; ++i){
			
			if(mCreateMiddle){
	        	mImgs[mImgCnt++] = new Image(mBank, mTraversal, Pos.MIDDLE, curTime - creationOffset);
	        }else{
	        	mImgs[mImgCnt++] = new Image(mBank, mTraversal, Pos.UP, curTime - creationOffset);
	        	mImgs[mImgCnt++] = new Image(mBank, mTraversal, Pos.DOWN, curTime - creationOffset);
	        }
	        	mCreateMiddle ^= true;
	        	creationOffset += mInterval;
		}
	}
	@Override
	public void onDrawFrame(GL10 gl10) {
		MatrixTrackingGL gl = new MatrixTrackingGL(gl10);
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
	        //EGL10 egl = (EGL10)EGLContext.getEGL();
	        //egl.eglGetConfigs(egl.eglGetCurrentDisplay(), egl.eglg, config_size, num_config)
	        gl.glEnable(GL10.GL_MULTISAMPLE);
	        
	        //gl.glScalef(0.25f, 0.25f, 1.0f);
	        long realTime = new Date().getTime();
	        fadeOffset(realTime);
	        long time = realTime + mOffset;
	        mDisplay.setFrameTime(realTime);
	        
	        if(mNewStart){
				mSplashImg = mImgs[4];
				Bitmap splash = BitmapFactory.decodeStream(ShowStreams.current.getResources().openRawResource(R.drawable.splash));
				mSplashImg.setSelected(gl, splash, 343.0f/512.0f, 1.0f, time);
				mNewStart = false;
				mDefocusSplashTime = time + SPLASHTIME;
			}
	        if(mSplashImg != null && realTime > mDefocusSplashTime){
	        	mSelectedTime = realTime;
	        	mSplashImg.select(gl, time, realTime);
	        	mSplashImg = null;
	        }
	        
	        if(mSelected != null){
	        	if(mClicked){
		        	mClicked = false;
		        	if(mSelected.inFocus()){
		        		mSelectedTime = realTime;
			        	mSelected.select(gl, time, realTime);
			        	mSelected = null;
		        	}
	        	}
	        }else{
	        	if(mClicked && mSplashImg == null){
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
		        	if(selected != null && selected.canSelect()){
		        		mSelectedTime = realTime;
		        		mSelected = selected;
		        		selected.select(gl, time, realTime);
		        	}
		        }
	        	mClicked = false;
	        }
	        
	        /********* DRAWING *********/
	        gl.glRotatef(mDisplay.getRotation(), 0.0f, 0.0f, 1.0f);
	        
	        BackgroundPainter.draw(gl);
	        
	        Image.setState(gl);
	        for(int i = 0; i < mImgCnt; ++i){
	        	if(mImgs[i] != mSelected){
	        		mImgs[i].draw(gl, time, realTime);
	        	}
	        }
	        Image.unsetState(gl);
	        
	        gl.glDepthMask(false);
	        GlowImage.setState(gl);
	        for(int i = 0; i < mImgCnt; ++i){
	        	mImgs[i].drawGlow(gl);
	        }
	        GlowImage.unsetState(gl);
	        if(mSelected != null){
	        	if(mSelected.inFocus()){
	        		Dimmer.draw(gl, 1.0f);
	        		if(!mDisplay.isTurning()){
		        		InfoBar.setState(gl);
		        		InfoBar.draw(gl, 1.0f);
		        		InfoBar.unsetState(gl);
	        		}
	        	}else{
	        		float fraction = getFraction(realTime);
	        		Dimmer.draw(gl, fraction);
	        		if(!mDisplay.isTurning()){
		        		InfoBar.setState(gl);
		        		InfoBar.draw(gl, fraction);
		        		InfoBar.unsetState(gl);
	        		}
	        	}
	        	
	        	gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
	        	GlowImage.setState(gl);
	        	mSelected.drawGlow(gl);
	        	GlowImage.unsetState(gl);
	        	Image.setState(gl);
	        	mSelected.draw(gl, time, realTime);
	        	Image.unsetState(gl);
	        }
	        
	        gl.glDepthMask(true);
	        
		}catch(Throwable t){
			Log.e("Floating Image", "Uncaught exception caught!", t);
		}
	}
	
	public static float getFraction(long realTime){
		return Math.min(((float)(realTime - RiverRenderer.mSelectedTime)) / RiverRenderer.mFocusDuration, 1.1f);
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
		TextureSelector.startThread();
		mFadeOffset = 0.0f;
	}
	public void onPause(){
		mBank.stop();
		TextureSelector.stopThread();
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
	
	public static float getFarRight(){
		return mDisplay.getWidth() * 0.5f * (-mFloatZ + mJitterZ) * 1.2f + 1.2f + mJitterX;
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		gl.glViewport(0, 0, width, height);
		mDisplay.onSurfaceChanged(width, height);
		
		Log.v("RiverRenderer", "Dimensions: " + width + "x" + height);
        /*
         * Set our projection matrix. This doesn't have to be done
         * each time we draw, but usually a new projection needs to
         * be set when the viewport is resized.
         */
						
		// Half screen width * depth (plus a little) + a little for rotation of pictures + jitter distance
        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadIdentity();
        float screenAspect = (float)width / height;
        
        gl.glFrustumf(-screenAspect, screenAspect, -1, 1, 1, 50);
	}
	
	public void xChanged(float amount){
		if(mDisplay.getOrientation() == Display.UP_IS_UP)
			mOffset += amount * mSensitivityX;
	}
	
	public void yChanged(float amount){
		if(mDisplay.getOrientation() == Display.UP_IS_LEFT)
			mOffset += amount * mSensitivityX;
	}
	
	public void moveRelease(float speedX, float speedY, long time){
		int orientation = mDisplay.getOrientation();
		if(orientation == Display.UP_IS_UP){
			mFadeOffset = speedX;
		}else if(orientation == Display.UP_IS_LEFT){
			mFadeOffset = speedY;
		}
		mUpTime = time;
	}
	
	public void cancelShowFolder(){
		mBank.cancelShowFolder();
	}
	
	public void onClick(float x, float y){
		mClicked = true;
		int orientation = mDisplay.getOrientation();
		if(orientation == Display.UP_IS_UP){
			mClickedPos = new Vec2f((x/mDisplay.getWidthPixels() * 2.0f - 1.0f) * mDisplay.getWidth() / 2.0f, -(y / mDisplay.getHeightPixels() * 2.0f - 1.0f) * mDisplay.getHeight() / 2.0f);
		}else if(orientation == Display.UP_IS_LEFT){
			mClickedPos = new Vec2f((y/mDisplay.getWidthPixels() * 2.0f - 1.0f) * mDisplay.getWidth() / 2.0f,  (x / mDisplay.getHeightPixels() * 2.0f - 1.0f) * mDisplay.getHeight() / 2.0f);
		}
		
		Log.v("RiverRenderer", "Clicked position: " + mClickedPos.toString());
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
       	 ShadowPainter.initTexture(gl);
       	 BackgroundPainter.initTexture(gl);
	}
}