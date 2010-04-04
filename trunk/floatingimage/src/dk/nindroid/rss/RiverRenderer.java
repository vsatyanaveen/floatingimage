package dk.nindroid.rss;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Intent;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.util.Log;
import dk.nindroid.rss.data.ImageReference;
import dk.nindroid.rss.gfx.Vec2f;
import dk.nindroid.rss.helpers.MatrixTrackingGL;
import dk.nindroid.rss.renderers.Renderer;
import dk.nindroid.rss.renderers.floating.BackgroundPainter;
import dk.nindroid.rss.renderers.floating.GlowImage;
import dk.nindroid.rss.renderers.floating.ShadowPainter;

public class RiverRenderer implements GLSurfaceView.Renderer {
	public static Display		mDisplay;
	
	
	private boolean 		mTranslucentBackground = false;
	
	
	private TextureBank 	mBank;
	private long			mUpTime;
	private Vec2f 			mClickedPos = new Vec2f();
	private boolean 		mClicked = false;
	private long 			mOffset = 0;
	private float			mFadeOffset = 0;
	private static final float mSensitivityX = 70.0f;
	
	private Renderer  mRenderer;
	
	static {
		mDisplay = new Display();
	}
	
	public RiverRenderer(boolean useTranslucentBackground, TextureBank textureBank){
		mTranslucentBackground = useTranslucentBackground;
		mBank = textureBank;
	}
	
	public void setRenderer(Renderer renderer){
		this.mRenderer = renderer;
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
	        long realTime = System.currentTimeMillis();
	        fadeOffset(realTime);
	        long time = realTime + mOffset;
	        mDisplay.setFrameTime(realTime);
	        
	        if(mClicked){
	        	mClicked = false;
	        	mRenderer.click(gl, mClickedPos.getX(), mClickedPos.getY(), time, realTime);
	        }
	        mRenderer.update(gl, time, realTime);
	        
	        /********* DRAWING *********/
	        gl.glRotatef(mDisplay.getRotation(), 0.0f, 0.0f, 1.0f);
	        mRenderer.render(gl, time, realTime);
	        
	        
		}catch(Throwable t){
			Log.e("Floating Image", "Uncaught exception caught!", t);
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
		if(mDisplay.getOrientation() == Display.UP_IS_UP){
			mOffset += amount * mSensitivityX;
		}else if(mDisplay.getOrientation() == Display.UP_IS_DOWN){
			mOffset -= amount * mSensitivityX;
		}
	}
	
	public void yChanged(float amount){
		if(mDisplay.getOrientation() == Display.UP_IS_LEFT){
			mOffset += amount * mSensitivityX;
		}else if(mDisplay.getOrientation() == Display.UP_IS_RIGHT){
			mOffset -= amount * mSensitivityX;
		}
	}
	
	public void moveRelease(float speedX, float speedY){
		int orientation = mDisplay.getOrientation();
		if(orientation == Display.UP_IS_UP){
			mFadeOffset = speedX;
		}else if(orientation == Display.UP_IS_LEFT){
			mFadeOffset = speedY;
		}else if(orientation == Display.UP_IS_RIGHT){
			mFadeOffset = -speedY;
		}else if(orientation == Display.UP_IS_DOWN){
			mFadeOffset = -speedX;
		}
		mUpTime = System.currentTimeMillis();
	}
	
	public void onClick(float x, float y){
		mClicked = true;
		int orientation = mDisplay.getOrientation();
		if(orientation == Display.UP_IS_UP){
			mClickedPos = new Vec2f((x/mDisplay.getWidthPixels() * 2.0f - 1.0f) * mDisplay.getWidth() / 2.0f, -(y / mDisplay.getHeightPixels() * 2.0f - 1.0f) * mDisplay.getHeight() / 2.0f);
		}else if(orientation == Display.UP_IS_LEFT){
			mClickedPos = new Vec2f((y/mDisplay.getWidthPixels() * 2.0f - 1.0f) * mDisplay.getWidth() / 2.0f,  (x / mDisplay.getHeightPixels() * 2.0f - 1.0f) * mDisplay.getHeight() / 2.0f);
		}else if(orientation == Display.UP_IS_RIGHT){
			mClickedPos = new Vec2f(-(y/mDisplay.getWidthPixels() * 2.0f - 1.0f) * mDisplay.getWidth() / 2.0f,  -(x / mDisplay.getHeightPixels() * 2.0f - 1.0f) * mDisplay.getHeight() / 2.0f);
		}else if(orientation == Display.UP_IS_DOWN){
			mClickedPos = new Vec2f(-(x/mDisplay.getWidthPixels() * 2.0f - 1.0f) * mDisplay.getWidth() / 2.0f, (y / mDisplay.getHeightPixels() * 2.0f - 1.0f) * mDisplay.getHeight() / 2.0f);
		}
		
		Log.v("RiverRenderer", "Clicked position: " + mClickedPos.toString());
	} 
 
	@Override 
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		gl.glMatrixMode(GL10.GL_TEXTURE);
		gl.glLoadIdentity();
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		mRenderer.init(gl, System.currentTimeMillis() + mOffset);
		
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