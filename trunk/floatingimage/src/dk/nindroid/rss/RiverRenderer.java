package dk.nindroid.rss;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Intent;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.util.Log;
import android.view.Surface;
import dk.nindroid.rss.data.ImageReference;
import dk.nindroid.rss.gfx.Vec2f;
import dk.nindroid.rss.helpers.MatrixTrackingGL;
import dk.nindroid.rss.renderers.OSD;
import dk.nindroid.rss.renderers.Renderer;
import dk.nindroid.rss.renderers.floating.BackgroundPainter;
import dk.nindroid.rss.renderers.floating.GlowImage;
import dk.nindroid.rss.renderers.floating.ShadowPainter;

public class RiverRenderer implements GLSurfaceView.Renderer {
	public static Display		mDisplay;
	
	private boolean 		mTranslucentBackground = false;
	private boolean			mMoveEventHandled = false;
	private TextureBank 	mBank;
	private long			mUpTime;
	private Vec2f 			mClickedPos = new Vec2f();
	private boolean 		mClicked = false;
	private boolean 		mShowOSD = false;
	private boolean 		mHideOSD = false;
	private long 			mOffset = 0;
	private float			mFadeOffset = 0;
	private static final float mSensitivityX = 70.0f;
	
	private Renderer  		mRenderer;
	private OSD				mOSD;
	private long			mLastFrameTime = 0;
	private int				mFrames = 0;
	
	static {
		mDisplay = new Display();
	}
	
	public RiverRenderer(boolean useTranslucentBackground, TextureBank textureBank){
		mTranslucentBackground = useTranslucentBackground;
		mBank = textureBank;
		mOSD = new OSD();
	}
	
	public void setRenderer(Renderer renderer){
		this.mRenderer = renderer;
	}
	
	public Renderer getRenderer(){
		return this.mRenderer;
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
	        //gl.glEnable(GL10.GL_MULTISAMPLE);
	        
	        //gl.glScalef(0.25f, 0.25f, 1.0f);
	        long realTime = System.currentTimeMillis();
	        /*
	        ++mFrames;
	        if(realTime - mLastFrameTime > 1000){
	        	Log.v("Floating Image", "Framerate is " + mFrames + " frames per second");
	        	mFrames = 0;
	        	mLastFrameTime = realTime;
	        }
	        */
	        fadeOffset(realTime);
	        long time = realTime + mOffset;
	        mDisplay.setFrameTime(realTime);
	        if(mShowOSD){
	        	mShowOSD = false;
	        	mOSD.show(realTime);
	        }else if(mHideOSD){
	        	mHideOSD = false;
	        	if(mOSD.hide(realTime)){
	        		mMoveEventHandled = true;
	        	}
	        }else if(mClicked){
	        	mClicked = false;
	        	mRenderer.click(gl, mClickedPos.getX(), mClickedPos.getY(), time, realTime);
	        }
	        
	        mRenderer.update(gl, time, realTime);
	        
	        ///////// DRAWING /////////
	        gl.glRotatef(mDisplay.getRotation(), 0.0f, 0.0f, 1.0f);
	        mRenderer.render(gl, time, realTime);
	        if(!mDisplay.isTurning()){
	        	mOSD.draw(gl, realTime);
	        }
		}catch(Throwable t){
			Log.e("Floating Image", "Unexpected exception caught!", t);
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
		return mRenderer.followCurrent();
	}
	
	public ImageReference getSelected(){
		return mRenderer.getCurrent();
	}
	
	public boolean unselect(){
		return mRenderer.back();
	}
	
	public void onResume(){
		mBank.start();
		mFadeOffset = 0.0f;
		mRenderer.onResume();
	}
	public void onPause(){
		mRenderer.onPause();
		mBank.stop();
	}
	
	public void setBackground(){
		mRenderer.setBackground();
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
		mDisplay.onSurfaceChanged(width, height);
		
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
		if(mDisplay.getOrientation() == Surface.ROTATION_0){
			mOffset += amount * mSensitivityX;
		}else if(mDisplay.getOrientation() == Surface.ROTATION_180){
			mOffset -= amount * mSensitivityX;
		}
	}
	
	public void yChanged(float amount){
		if(mDisplay.getOrientation() == Surface.ROTATION_270){
			mOffset += amount * mSensitivityX;
		}else if(mDisplay.getOrientation() == Surface.ROTATION_90){
			mOffset -= amount * mSensitivityX;
		}
	}
	
	private boolean showOSD(float x, float y, float speedX, float speedY){
		if(y < 150){
			if(speedY > 0){
				//mShowOSD = true;
			}else{
				mHideOSD = true;
			}
			return true;
		}else if(y > mDisplay.getHeightPixels() - 150){
			if(speedY < 0){
				//mShowOSD = true;
			}else{
				mHideOSD = true;
			}
			return true;
		}
		return false;
	}
	
	public boolean isVertical(float speedX, float speedY){
		return Math.abs(speedY) > Math.abs(speedX);
	}
	
	public void move(float x, float y, float speedX, float speedY){
		if(mMoveEventHandled){
			return;
		}
		// Transform event!
		int orientation = mDisplay.getOrientation();
		float tmp;
		switch(orientation){
		case Surface.ROTATION_0:
			// Do nothing
			break;
		case Surface.ROTATION_270:
			tmp = x; x = y; y = tmp;
			y = mDisplay.getHeightPixels() - y;
			tmp = speedX; speedX = speedY; speedY = tmp;
			speedY *= -1;
			break;
		case Surface.ROTATION_90:
			tmp = x; x = y; y = tmp;
			x = mDisplay.getWidthPixels() - x;
			tmp = speedX; speedX = speedY; speedY = tmp;
			speedX *= -1;
			break;
		case Surface.ROTATION_180:
			x = mDisplay.getWidthPixels() - x;
			y = mDisplay.getHeightPixels() - y;
			speedX *= -1;
			speedY *= -1;
		}
		
		if(isVertical(speedX, speedY)){
			showOSD(x, y, speedX, speedY);
		}else{
			if(!(speedX > 0 ? mRenderer.slideRight(System.currentTimeMillis()) : mRenderer.slideLeft(System.currentTimeMillis()))){
				mFadeOffset = speedX;
			}else{
				mMoveEventHandled = true;
			}
			mUpTime = System.currentTimeMillis();
		}
	}
	
	public void moveRelease(){
		mMoveEventHandled = false;
	}
	
	public void onClick(float x, float y){
		// Transform coordinates!
		int orientation = mDisplay.getOrientation();
		float tmp;
		switch(orientation){
		case Surface.ROTATION_0:
			// Do nothing
			break;
		case Surface.ROTATION_270:
			tmp = x; x = y; y = tmp;
			y = mDisplay.getHeightPixels() - y;
			break;
		case Surface.ROTATION_90:
			tmp = x; x = y; y = tmp;
			x = mDisplay.getWidthPixels() - x;
			break;
		case Surface.ROTATION_180:
			x = mDisplay.getWidthPixels() - x;
			y = mDisplay.getHeightPixels() - y;
		}
		
		if(!mOSD.click(x, y)){
			mClicked = true;
			mClickedPos = new Vec2f((x/mDisplay.getWidthPixels() * 2.0f - 1.0f) * mDisplay.getWidth() / 2.0f, -(y / mDisplay.getHeightPixels() * 2.0f - 1.0f) * mDisplay.getHeight() / 2.0f);
		}
		
		Log.v("RiverRenderer", "Clicked position: " + mClickedPos.toString());
	}
	
	public void toggleMenu(){
		if(mOSD.isShowing()){
			mHideOSD = true;
		}else{
			mShowOSD = true;
		}
	}
 
	@Override 
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		gl.glMatrixMode(GL10.GL_TEXTURE);
		gl.glLoadIdentity();
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		mOSD.init(gl);
		mRenderer.init(gl, System.currentTimeMillis() + mOffset, mOSD);
		
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
         
         gl.glHint(GL10.GL_LINE_SMOOTH_HINT, GL10.GL_NICEST);
 		 gl.glHint(GL10.GL_POINT_SMOOTH_HINT, GL10.GL_NICEST);
         

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