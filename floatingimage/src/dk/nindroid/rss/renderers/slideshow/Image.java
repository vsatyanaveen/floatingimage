package dk.nindroid.rss.renderers.slideshow;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.microedition.khronos.opengles.GL10;

import android.graphics.Bitmap;
import android.opengl.GLUtils;
import android.util.Log;
import dk.nindroid.rss.Display;
import dk.nindroid.rss.MainActivity;
import dk.nindroid.rss.TextureSelector;
import dk.nindroid.rss.data.ImageReference;
import dk.nindroid.rss.gfx.ImageUtil;
import dk.nindroid.rss.gfx.Vec3f;
import dk.nindroid.rss.renderers.ImagePlane;
import dk.nindroid.rss.uiActivities.Toaster;

public class Image implements ImagePlane {
	private static final int 	VERTS = 4;
	private int 				mTextureID;
	private int 				mLargeTextureID;
	private int					mLastLargeSize = 0;
	private int					mLastSmallSize = 0;
	private FloatBuffer 		mTexBuffer;
	private Vec3f[]				mVertices;
	private IntBuffer   		mVertexBuffer;
	private ByteBuffer  		mIndexBuffer;
	private ImageReference 		mImage;
	private Bitmap 				mBitmap;
	private float 				mAspect = 1;
	private float 				mBitmapWidth;
	private float 				mBitmapHeight;
	private boolean				mHasBitmap;
	private float				mAlpha = 1.0f;
	private TextureSelector		mTextureSelector;
	private boolean				mSetBackgroundWhenReady = false;
	private boolean				mRevive = false;
	private boolean				mSetLargeTexture = false;
	private boolean				mInFocus;
	private Display				mDisplay;
	MainActivity 				mActivity; 
	
	private Vec3f				mPos;
	
	public void setFocus(boolean inFocus){
		this.mInFocus = inFocus;
	}
	
	public void init(GL10 gl, long time){
		int[] textures = new int[1];
		gl.glGenTextures(1, textures, 0);
		mTextureID = textures[0];
		gl.glGenTextures(1, textures, 0);
		mLargeTextureID = textures[0];
		mLastLargeSize = 0;
		mLastSmallSize = 0;
		mHasBitmap = false;
	}
	
	public void clear(){
		this.mBitmap = null;
		this.mImage = null;
		this.mBitmapHeight = 0;
		this.mBitmapWidth = 0;
	}

	public Image(Display display, MainActivity activity){
		this.mDisplay = display;
		this.mActivity = activity;
		mTextureSelector = new TextureSelector(display, mActivity.getSettings().bitmapConfig, activity);
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
	/*
	private boolean isTall(){
		boolean tall = mAspect < RiverRenderer.mDisplay.getWidth() / RiverRenderer.mDisplay.getFocusedHeight();
		return tall	;	
	}
	*/
	public void setImage(GL10 gl, ImageReference image){
		if(image == null)
			return;
		this.mImage = image;
		this.mBitmap = image.getBitmap();
		this.mBitmapWidth = image.getWidth();
		this.mBitmapHeight = image.getHeight();
		setTexture(gl, mTextureID, false);
		this.mBitmap = null;
		mTextureSelector.selectImage(this, image);
	}
	
	public ImageReference getImage(){
		return mImage;
	}
	
	public void setAlpha(float alpha){
		this.mAlpha = alpha;
	}
	
	public float getAlpha(){
		return this.mAlpha;
	}
	
	public void setPos(Vec3f pos){
		this.mPos = pos;
	}
	
	public Vec3f getPos(){
		return this.mPos;
	}
	
	public boolean hasBitmap(){
		return mHasBitmap;
	}
	
	public void onResume(){
		mTextureSelector.startThread();
		mRevive = true;
	}
	
	public void onPause(){
		mTextureSelector.stopThread();
	}
	
	private float getRotationFraction(float rotation){
		float fraction = Math.abs((float)Math.sin(rotation / 180.0f * Math.PI));
		return fraction;
	}
	
	public float getScale(float szX, float szY, boolean sideways){
		float height = mDisplay.getFocusedHeight() * mDisplay.getFill();
		float width = mDisplay.getWidth() * mDisplay.getFill();
		if(sideways){
			float scale = 1.0f;
			if(szX > height){
				scale = height / szX;
				szY *= scale;
			}
			if(szY > width){
				scale *= width / szY;
			}
			return scale;
		}else{
			float scale = 1.0f;
			if(szX > width){
				scale = width / szX;
				szY *= scale;
			}
			if(szY > height){
				scale *= height / szY;
			}
			return scale;
		}
	}
	
	public float getScale(float szX, float szY, long realTime){
		float rotationFraction = Math.min(mImage.getRotationFraction(realTime), 1.0f);
		float rotationTarget = getRotationFraction(mImage.getTargetOrientation());
		float rotationOrg = getRotationFraction(mImage.getPreviousOrientation());
		
		float targetScale = getScale(szX, szY, rotationTarget == 1.0f);
		float orgScale = getScale(szX, szY, rotationOrg == 1.0f);
		float scaleDiff = orgScale - targetScale;
		float scale = orgScale - scaleDiff * ImageUtil.smoothstep(rotationFraction);
		return scale;
	}
	
	public void render(GL10 gl, long time){
		if(mPos == null) return;
		if(this.mSetLargeTexture){
			setTexture(gl, mLargeTextureID, true);
			mSetLargeTexture = false;
			if(mBitmap != null && !mBitmap.isRecycled()){
				mBitmap.recycle();
				mBitmap = null;
			}
		}
		if(mRevive){
			setTexture(gl, mTextureID, mHasBitmap);
			mRevive = false;
		}
		if(this.mImage == null){
			return;
		}
		
		float x, y, z, szX, szY;
		x = mPos.getX();
		y = mPos.getY(); 
		z = mPos.getZ();
		
		szY = 5.0f; // Huge, since we only scale down.
		szX = szY * mAspect;
		
		float scale = getScale(szX, szY, time);
		szX *= scale;
		szY *= scale;
		
		/*
		// Doesn't support rotation, but is much more efficient than above. :P 
		if(isTall()){
			szY = RiverRenderer.mDisplay.getFocusedHeight() * RiverRenderer.mDisplay.getFill();
			szX = mAspect * szY;
		}else{
			szX = RiverRenderer.mDisplay.getWidth() * RiverRenderer.mDisplay.getFill();
			szY = szX / mAspect;
		}
		*/
		gl.glTexEnvx(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE, GL10.GL_MODULATE);
		gl.glFrontFace(GL10.GL_CCW);
		gl.glEnable(GL10.GL_TEXTURE_2D);
				
		gl.glColor4f(1.0f, 1.0f, 1.0f, mAlpha);
		gl.glActiveTexture(GL10.GL_TEXTURE0);
		if(!mHasBitmap){
			gl.glBindTexture(GL10.GL_TEXTURE_2D, mTextureID);
		}else{
			gl.glBindTexture(GL10.GL_TEXTURE_2D, mLargeTextureID);
		}
        gl.glVertexPointer(3, GL10.GL_FIXED, 0, mVertexBuffer);
        
		gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, mTexBuffer);
		
		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		gl.glEnable(GL10.GL_BLEND);
		gl.glPushMatrix();
				
		gl.glTranslatef(x, y, z);
		gl.glRotatef(mImage.getRotation(mTextureSelector, time), 0, 0, 1.0f);
		gl.glScalef(szX, szY, 1);
		gl.glDrawElements(GL10.GL_TRIANGLE_STRIP, 4, GL10.GL_UNSIGNED_BYTE, mIndexBuffer);
		
		gl.glPopMatrix();
		gl.glDisable(GL10.GL_BLEND);
    }
	
	public void setTexture(GL10 gl, int textureID, boolean isLarge) {
		if(mBitmap == null){
			return;
		}
		mHasBitmap = isLarge;
		float width = mBitmapWidth;
		float height = mBitmapHeight;
		
		mAspect = width / height;
        
        gl.glBindTexture(GL10.GL_TEXTURE_2D, textureID);

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
        
        try{
        	if((isLarge && mLastLargeSize != mBitmap.getWidth()) || (!isLarge && mLastSmallSize != mBitmap.getWidth())){
        		GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, mBitmap, 0);
        		if(isLarge){
        			mLastLargeSize = mBitmap.getWidth();
        		}else{
        			mLastSmallSize = mBitmap.getWidth();
        		}
        	}else{
        		GLUtils.texSubImage2D(GL10.GL_TEXTURE_2D, 0, 0, 0, mBitmap);
        	}
        	ByteBuffer tbb = ByteBuffer.allocateDirect(VERTS * 2 * 4);
            tbb.order(ByteOrder.nativeOrder());
            mTexBuffer = tbb.asFloatBuffer();
            
            float tex[] = {
            	0.0f,  0.0f,
            	0.0f,  height,
            	width,  0.0f,
            	width,  height,
            };
            mTexBuffer.put(tex);
            mTexBuffer.position(0);
        }catch(IllegalArgumentException e){
        	Log.w("dk.nindroid.rss.renderers.SlideshowRenderer.Image", "Texture could not be set", e);
        }
        setState(gl);
	}
	
	public static void setState(GL10 gl){
		gl.glTexEnvx(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE, GL10.GL_REPLACE);
		gl.glFrontFace(GL10.GL_CCW);
		gl.glEnable(GL10.GL_TEXTURE_2D);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER,GL10.GL_NEAREST);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D,GL10.GL_TEXTURE_MAG_FILTER,GL10.GL_LINEAR);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S,GL10.GL_CLAMP_TO_EDGE);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T,GL10.GL_CLAMP_TO_EDGE);
	}
	
	public static void unsetState(GL10 gl){
		gl.glDisable(GL10.GL_TEXTURE_2D);
	}
	
	public boolean validForTextureUpdate(){
		return this.mInFocus;
	}

	@Override
	public void setFocusTexture(Bitmap texture, float width, float height, int sizeType) {
		this.mBitmap = texture;
		this.mBitmapWidth = width;
		this.mBitmapHeight = height;
		this.mSetLargeTexture = true;
		if(mSetBackgroundWhenReady){
			setBackground();
		}
		mSetBackgroundWhenReady = false;
	}

	public int getProgress() {
		return mTextureSelector.getProgress();
	}
	
	public void setBackground(){
		if(mHasBitmap){
			try {
				mActivity.setWallpaper(mTextureSelector.getCurrentBitmap());
			} catch (IOException e) {
				Log.e("ImageDownloader", "Failed to get image", e);
				Toaster toaster = new Toaster(mActivity.context(), "Sorry, there was an error setting wallpaper!");
				mActivity.runOnUiThread(toaster);
			}
		}else{
			mSetBackgroundWhenReady = true;
		}
	}
}
