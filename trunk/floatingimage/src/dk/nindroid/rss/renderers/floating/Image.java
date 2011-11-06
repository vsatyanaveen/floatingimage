package dk.nindroid.rss.renderers.floating;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import android.graphics.Bitmap;
import android.opengl.GLU;
import android.opengl.GLUtils;
import android.util.Log;
import dk.nindroid.rss.Display;
import dk.nindroid.rss.MainActivity;
import dk.nindroid.rss.OnDemandImageBank;
import dk.nindroid.rss.TextureSelector;
import dk.nindroid.rss.data.ImageReference;
import dk.nindroid.rss.data.Progress;
import dk.nindroid.rss.data.Ray;
import dk.nindroid.rss.data.Texture;
import dk.nindroid.rss.gfx.ImageUtil;
import dk.nindroid.rss.gfx.Vec3f;
import dk.nindroid.rss.renderers.ImagePlane;
import dk.nindroid.rss.renderers.ProgressBar;
import dk.nindroid.rss.renderers.floating.positionControllers.PositionController;
import dk.nindroid.rss.renderers.floating.positionControllers.Rotation;
import dk.nindroid.rss.uiActivities.Toaster;

public class Image implements ImagePlane, OnDemandImageBank.LoaderClient {
	private static final int STATE_FLOATING =   0;
	private static final int STATE_FOCUSING =   1;
	private static final int STATE_FOCUSED	=   2;
	private static final int STATE_DEFOCUSING = 3;
	
	private Texture			mLargeTexture;
	private int				mState = STATE_FLOATING;
	private boolean			mDelete;
	
	private final OnDemandImageBank mOnDemandBank;
	private Display			mDisplay;
	private IntBuffer   	mVertexBuffer;
	private ByteBuffer  	mIndexBuffer;
	private ByteBuffer  	mLineIndexBuffer;
	private float  			maspect;
	private int				mRotations;
	private Rotation		mRotationA;
	private Rotation		mRotationB;
	private Vec3f			mPos;
	private float			mPositionControlerScale;
	private Rotation		mRotationASaved;
	private Rotation		mRotationBSaved;
	private PositionController	mPositionController;
	private float			mAlpha = 1;
	//private long			mStartTime;
	private ImageReference 	mShowingImage;
	private Vec3f[]			mVertices;
	private int				mLastTextureSize;
	private MainActivity	mActivity;
	private InfoBar			mInfoBar;
	private TextureSelector mTextureSelector;
	private boolean			mSetThumbnailTexture = false;
	private boolean			mImageNotSet = true;
	
	// Selected vars
	private Vec3f			mSelectedPos = new Vec3f();
	private boolean			mUpdateLargeTex = false;
	private Bitmap			mFocusBmp;
	private float			mFocusWidth;
	private float			mFocusHeight;
	private boolean			mLargeTex = false;
	private long			mLargeTexTime = 0;
	private boolean			mSetBackgroundWhenReady = false;
	private long			mImageAppliedTime = 0;
	
	// Smoothing vars
	int[] mPixelVertices;
	float[] mImageTex;
	float[] mSmoothTex;
	FloatBuffer mSmoothTexBuffer;
	FloatBuffer mImageTexBuffer;
	IntBuffer mSmoothingVertexBuffer;
	private Bitmap			mLineSmoothBitmap;
	
	public void setPositionController(PositionController pc){
		this.mPositionController = pc;
	}
	
	public PositionController getPositionController(){
		return this.mPositionController;
	}
	
	public void init(GL10 gl, long time){
		int[] textures = new int[2];
		gl.glGenTextures(2, textures, 0);
		mTextureID = textures[0];
		mSmoothTextureID = textures[1];
		revive(gl, time);
	}
	
	public boolean stateFloating(){
		return this.mState == STATE_FLOATING;
	}
	
	public boolean stateFocusing(){
		return this.mState == STATE_FOCUSING;
	}
	
	public boolean stateInFocus(){
		return mState == STATE_FOCUSED;
	}
	
	public float getDepth(){
		return mPos.getZ();
	}
	
	public void delete(){
		mDelete = true;
	}
	
	public void getImageIfEmpty(){
		if(mShowingImage == null){
			mOnDemandBank.get(this, true);
		}
	}
	
	public void setBackground(){
		if(mLargeTex){
			try {
				Bitmap bmp = mTextureSelector.getCurrentBitmap();
				if(bmp.isRecycled()){
					Log.e("Floating Image", "Trying to set recycled bitmap as background!");
				}else{
					mActivity.setWallpaper(bmp);
				}
			} catch (IOException e) {
				Log.e("Floating Image", "Failed to get image", e);
				Toaster toaster = new Toaster(mActivity.context(), "Sorry, there was an error setting wallpaper!");
				mActivity.runOnUiThread(toaster);
			}
		}else{
			mSetBackgroundWhenReady = true;
		}
	}
	
	public void revive(GL10 gl, long time){
		// Make sure the textures are redrawn after subactivity
		mLargeTexture.nullTexture();
		mLastTextureSize = 0;
		mImageNotSet = true;
		// Revive textures
		if(mShowingImage != null){
			mOnDemandBank.get(mShowingImage, this);
		}
		
		if(mState == STATE_FOCUSED){
			mTextureSelector.applyLarge();
		}else{
			mLargeTex = false;
		}
		
		// Set smoothing texture
		gl.glBindTexture(GL11.GL_TEXTURE_2D, mSmoothTextureID);
		gl.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
		gl.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		gl.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP_TO_EDGE);
		gl.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP_TO_EDGE);
		gl.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_BLEND);

		GLUtils.texImage2D(GL11.GL_TEXTURE_2D, 0, mLineSmoothBitmap, 0);
	}
	public void reset(GL10 gl, long time){
		if(mFocusBmp != null && !mFocusBmp.isRecycled()){
			mFocusBmp.recycle();			
		}
		mFocusBmp = null;
		mState = STATE_FLOATING;
		
		mShowingImage = null;
	}
	
	public boolean canSelect(){
		if(mShowingImage == null){
			return false;
		}
		return true;
	}
	
	public ImageReference getShowing(){
		return mShowingImage;
	}
	
	public void select(GL10 gl, long time, long realTime){
		mSelectedPos.set(mPos);
		if(mState == STATE_FOCUSED){
			if(!this.isTransformed()){
				mState = STATE_DEFOCUSING;
			}
		}else{
			if(mShowingImage == null){
				return;
			}
			mShowingImage.setOld();
			// Select
			mState = STATE_FOCUSING;
			mPositionController.getRotation(getInterval(time), mRotationASaved, mRotationBSaved);
			// Get large texture, if not already there.
			mInfoBar.select(gl, mDisplay, this.mShowingImage);
		}
	}
	
	public void setSelected(GL10 gl, Bitmap bmp, float width, float height, long time){
		mState = STATE_FOCUSED;
		setFocusedPosition();
		mRotationA.setAngle(0.0f);
		mRotationB.setAngle(0.0f);
		mFocusBmp = bmp;
		mFocusWidth = width;
		mFocusHeight = height;
		mLargeTex = true; 
		mPositionController.getRotation(getInterval(time), mRotationASaved, mRotationBSaved);
		setFocusTexture(gl);
		if(mSetBackgroundWhenReady){
			setBackground();
		}
		mSetBackgroundWhenReady = false;
	}
	
	void initSmoothing(){
		mPixelVertices = new int[8 * 3];
		mImageTex = new float[8 * 2];
		mSmoothTex = new float[8 * 2];
        
		ByteBuffer tbb = ByteBuffer.allocateDirect(8 * 2 * 4);
		tbb.order(ByteOrder.nativeOrder());
		mSmoothTexBuffer = tbb.asFloatBuffer();
		
		tbb = ByteBuffer.allocateDirect(8 * 2 * 4);
		tbb.order(ByteOrder.nativeOrder()); 
		mImageTexBuffer = tbb.asFloatBuffer();
		
		ByteBuffer vbb = ByteBuffer.allocateDirect(8 * 3 * 4);
		vbb.order(ByteOrder.nativeOrder());
		mSmoothingVertexBuffer = vbb.asIntBuffer();
	}
		
	public Image(MainActivity activity, 
				 Display display, 
				 InfoBar infoBar, 
				 Texture largeTexture, 
				 TextureSelector textureSelector,
				 Bitmap lineSmoothBitmap,
				 OnDemandImageBank onDemandBank){
		this.mDisplay = display;
		this.mOnDemandBank = onDemandBank;
		this.mLineSmoothBitmap = lineSmoothBitmap;
		this.mActivity = activity;
		this.mInfoBar = infoBar;
		this.mTextureSelector = textureSelector;
		this.mLargeTexture = largeTexture;
        mPos = new Vec3f(0, 0, 0);
        mRotationA = new Rotation(0, 0, 0, 1);
        mRotationB = new Rotation(0, 0, 0, 1);
        mRotationASaved = new Rotation(0, 0, 0, 1);
        mRotationBSaved = new Rotation(0, 0, 0, 1);
        
        ByteBuffer tbb = ByteBuffer.allocateDirect(VERTS * 2 * 4);
        tbb.order(ByteOrder.nativeOrder());
        mTexBuffer = tbb.asFloatBuffer();
        mLargeTexBuffer = tbb.asFloatBuffer();

		initSmoothing();
        
        float tex[] = {
        	0.0f,  0.0f,
        	0.0f,  0.0f,	
        	0.0f,  0.0f,
        	0.0f,  0.0f,
        };
        mTexBuffer.put(tex);
        mTexBuffer.position(0);	
        
        mLargeTexBuffer.put(tex);
        mLargeTexBuffer.position(0);
		
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
		
		// Outer square
		byte lineIndices[] = {
			0, 1, 3, 2, 0
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
		
		mLineIndexBuffer = ByteBuffer.allocateDirect(indices.length + 1);
		mLineIndexBuffer.put(lineIndices);
		mLineIndexBuffer.position(0);
	}
	
	private float getRotationFraction(float rotation){
		float fraction = Math.abs((float)Math.sin(rotation / 180.0f * Math.PI));
		return fraction;
	}
	
	private boolean isTall(){
		return isTall(mDisplay.getWidth() / mDisplay.getFocusedHeight());
	}
	
	private boolean isTall(float screenAspect){
		boolean tall = maspect < screenAspect;
		return tall;	
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
		float rotationFraction = Math.min(mShowingImage.getRotationFraction(realTime), 1.0f);
		float rotationTarget = getRotationFraction(mShowingImage.getTargetOrientation());
		float rotationOrg = getRotationFraction(mShowingImage.getPreviousOrientation());
		float targetScale = getScale(szX, szY, rotationTarget == 1.0f);
		float orgScale = getScale(szX, szY, rotationOrg == 1.0f);
		float scaleDiff = orgScale - targetScale;
		float scale = orgScale - scaleDiff * ImageUtil.smoothstep(rotationFraction);
		return scale;
	}
	
	public void draw(GL10 gl, long time, long realTime){
		if(mShowingImage == null){
			if(!mLargeTex){
				return;
			}
		}else{
			if(mShowingImage.isDeleted()){
				return;
			}
		}
		if(mImageNotSet) return;
				
		float x, y, z, szX, szY;
		x = mPos.getX();
		y = mPos.getY();
		z = mPos.getZ();
		if(mShowingImage != null && (Math.min(mShowingImage.getRotationFraction(realTime), 1.0f) != 1.0f || mShowingImage.getTargetOrientation() != 0.0f)){
			szY = 5.0f; // Huge, since we only scale down.
			szX = szY * maspect;
			
			float scale = getScale(szX, szY, realTime);
			szX *= scale;
			szY *= scale;
		}else{ // Splash screen only!
			if(isTall()){
				szY = mDisplay.getFocusedHeight();
				szY *= mDisplay.getFill();
				
				szX = maspect * szY;
			}else{
				szX = mDisplay.getWidth();
				szX *= mDisplay.getFill();
				
				szY = szX / maspect;
			}
		}
		
		szX *= mPositionControlerScale;
		szY *= mPositionControlerScale;
		
		gl.glFrontFace(GL10.GL_CCW);
		gl.glEnable(GL10.GL_TEXTURE_2D);
		
		float userRotation = 0;
		if(mShowingImage != null){
			userRotation = mShowingImage.getRotation(mTextureSelector, realTime);
		}
		
		gl.glPushMatrix();
		
		float userScale = mScale * mInitialScale;
		float userMoveX = mX + mInitialX;
		float userMoveY = mY + mInitialY;
		
		x += userMoveX;
		y += userMoveY;
		szX *= userScale;
		szY *= userScale;
		
		float interval = getInterval(time);
		gl.glEnable(GL10.GL_BLEND);
		//Vec3f rot = mPositionController.getRotation(interval);
		float alpha = (mState == STATE_FLOATING ? mPositionController.getOpacity(interval) : 1) * mAlpha;
		float imageAppliedFadeInterval = getImageAppliedFadeInterval(realTime);
		alpha *= imageAppliedFadeInterval;
		z += imageAppliedFadeInterval * 0.5f - 0.5f;
		
		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		gl.glEnable(GL10.GL_BLEND);
		gl.glTexEnvx(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE, GL10.GL_MODULATE);
		gl.glColor4f(1.0f, 1.0f, 1.0f, alpha);
		if(!mImageNotSet){
			if(mActivity.getSettings().imageDecorations){
				ShadowPainter.draw(gl, x, y, z, mRotationA, mRotationB, userRotation, szX, szY);
			}
		}
		gl.glTranslatef(x, y, z);
		gl.glRotatef(mRotationA.getAngle(), mRotationA.getX(), mRotationA.getY(), mRotationA.getZ());
		gl.glRotatef(mRotationB.getAngle(), mRotationB.getX(), mRotationB.getY(), mRotationB.getZ());
		gl.glRotatef(userRotation, 0, 0, 1);
		gl.glScalef(szX, szY, 1);
	
		// Draw image
		gl.glActiveTexture(GL10.GL_TEXTURE0); 
		gl.glVertexPointer(3, GL10.GL_FIXED, 0, mVertexBuffer);
		
		gl.glBindTexture(GL10.GL_TEXTURE_2D, mTextureID);
		gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, mTexBuffer);
		
		if(!mImageNotSet){	
			gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);
		
			if(mLargeTex){
				float largeAlpha = Math.min(1.0f, (realTime - mLargeTexTime) / 500.0f);
				gl.glColor4f(1.0f, 1.0f, 1.0f, largeAlpha);
				gl.glBindTexture(GL10.GL_TEXTURE_2D, mLargeTexture.getTextureID(gl));
				gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, mLargeTexBuffer);
				gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);
			}
		}
			
	    
		// Show smoothing
		//if((System.currentTimeMillis() % 10000) < 5000){
	    if(mActivity.getSettings().blackEdges){
			if(mShowingImage != null && mState != Image.STATE_FOCUSED){
				drawSideSmoothing(gl, alpha, 0, 1, 0, true, szX, szY); // Left
				drawSideSmoothing(gl, alpha, 1, 3, mShowingImage.getHeight(), false, szX, szY); // Bottom
				drawSideSmoothing(gl, alpha, 0, 2, 0, false, szX, szY); // Top
				drawSideSmoothing(gl, alpha, 2, 3, mShowingImage.getWidth(), true, szX, szY); // Right
			}
	    }
		//}
		
		//if(realTime % 10000 < 5000){
			// Smooth images
			//gl.glEnable(GL10.GL_BLEND);
		/*
			gl.glEnable(GL10.GL_POINT_SMOOTH);
			gl.glEnable(GL10.GL_LINE_SMOOTH);
			gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
			gl.glLineWidth(1.0f);
			gl.glDrawElements(GL10.GL_LINE_STRIP, 5, GL10.GL_UNSIGNED_BYTE, mLineIndexBuffer);
			//*/
		//}
		gl.glPopMatrix();
		
        
        if(mState == STATE_FOCUSED && !mLargeTex){
        	ProgressBar.draw(gl, mTextureSelector.getProgress(), mDisplay);
        }
	}
	
	private float getImageAppliedFadeInterval(long realTime){
		long timePassed = realTime - mImageAppliedTime;
		double dInterval = timePassed / 500.0;
		float interval = smoothstep((float)Math.min(dInterval, 1.0));
		return interval;
	}
	
	void drawSideSmoothing(GL10 gl, float alpha, int indexA, int indexB, float uvSide, boolean vertical, float scaleX, float scaleY){
		// Scale is added to counteract openGL scaling.

		GL11 gl11 = (GL11)gl;
		
		Vec3f a = mVertices[indexA];
		Vec3f b = mVertices[indexB];

		
		
		float lineWidth = 8; // Off by ^2 for some reason. :(
/*		
		long time = (System.currentTimeMillis() / 1000) % 8;
		if(time < 2){
			return;
		}else if(time < 4){
			lineWidth = 8;
		}else if(time < 6){
			lineWidth = 30;
		}		
*/
		int one = 0x10000;
		int aX = (int)(a.getX() * one);
		int aY = (int)(a.getY() * one);
		int aZ = (int)(a.getZ() * one);
		int bX = (int)(b.getX() * one);
		int bY = (int)(b.getY() * one);
		int bZ = (int)(b.getZ() * one);

		float z = mPos.getZ(); // WTF er min z? 

		float planeHeightA = -z; // Near plane distance is hardcoded to one, height is also one.
		float planeWidthA = -z * mDisplay.getPortraitWidthPixels() / mDisplay.getPortraitHeightPixels(); // Near plane is still one. Division by one is silly!

		float pixelHeightA = planeHeightA / mDisplay.getPortraitHeightPixels() * lineWidth / scaleY;
		float pixelWidthA = planeWidthA / mDisplay.getPortraitWidthPixels() * lineWidth / scaleX;

		float halfWidthA = pixelWidthA / 2.0f;
		float halfHeightA = pixelHeightA / 2.0f;

		float planeHeightB = -z; // Near plane is hardcoded to one, height is also one.
		float planeWidthB = -z * mDisplay.getPortraitWidthPixels() / mDisplay.getPortraitHeightPixels(); // Near plane is still one. Division by one is silly!

		float pixelHeightB = planeHeightB / mDisplay.getPortraitHeightPixels() * lineWidth / scaleY;
		float pixelWidthB = planeWidthB / mDisplay.getPortraitWidthPixels() * lineWidth / scaleX;

		float halfWidthB = pixelWidthB / 2.0f;
		float halfHeightB = pixelHeightB / 2.0f;

		float vertexDistance = -z; 
		
		if(vertical){
			float imageHeight = mShowingImage.getHeight() - 1.0f/128.0f;
			float pixelPartA = halfHeightA / vertexDistance;
			float pixelPartB = halfHeightB / vertexDistance;
			
			float offset = imageHeight / 256.0f;

			mPixelVertices[0] = aX + (int)(halfWidthA * one);
			mPixelVertices[1] = aY + (int)(halfHeightA * one);
			mPixelVertices[2] = aZ;
			
			mPixelVertices[3] = aX - (int)(halfWidthA * one);
			mPixelVertices[4] = aY + (int)(halfHeightA * one);
			mPixelVertices[5] = aZ;
			
			mPixelVertices[6] = aX + (int)(halfWidthA * one);
			mPixelVertices[7] = aY - (int)(halfHeightA * one);
			mPixelVertices[8] = aZ;
			
			mPixelVertices[9] = aX - (int)(halfWidthA * one);
			mPixelVertices[10] = aY - (int)(halfHeightA * one);
			mPixelVertices[11] = aZ;

			mPixelVertices[12] = bX + (int)(halfWidthB * one);
			mPixelVertices[13] = bY + (int)(halfHeightB * one);
			mPixelVertices[14] = bZ;
			
			mPixelVertices[15] = bX - (int)(halfWidthB * one);
			mPixelVertices[16] = bY + (int)(halfHeightB * one);
			mPixelVertices[17] = bZ;
			
			mPixelVertices[18] = bX + (int)(halfWidthB * one);
			mPixelVertices[19] = bY - (int)(halfHeightB * one);
			mPixelVertices[20] = bZ;
			
			mPixelVertices[21] = bX - (int)(halfWidthB * one);
			mPixelVertices[22] = bY - (int)(halfHeightB * one);
			mPixelVertices[23] = bZ;
			
			mSmoothTex[0] = 1.0f;
			mSmoothTex[1] = 0.0f;
			
			mSmoothTex[2] = 0.0f;
			mSmoothTex[3] = 0.0f;
			
			mSmoothTex[4] = 1.0f;
			mSmoothTex[5] = 0.5f;
			
			mSmoothTex[6] = 0.0f;
			mSmoothTex[7] = 0.5f;	

			mSmoothTex[8] = 1.0f;
			mSmoothTex[9] = 0.5f;
			
			mSmoothTex[10] = 0.0f;
			mSmoothTex[11] = 0.5f;
			
			mSmoothTex[12] = 1.0f;
			mSmoothTex[13] = 1.0f;
			
			mSmoothTex[14] = 0.0f;
			mSmoothTex[15] = 1.0f;
			
			mImageTex[0] = uvSide - offset;
			mImageTex[1] = 0.0f;
			
			mImageTex[2] = uvSide - offset;
			mImageTex[3] = 0.0f;
			
			mImageTex[4] = uvSide - offset;
			mImageTex[5] = pixelPartA;
			
			mImageTex[6] = uvSide - offset;
			mImageTex[7] = pixelPartA;	

			mImageTex[8] = uvSide - offset;
			mImageTex[9] = imageHeight - pixelPartB;
			
			mImageTex[10] = uvSide - offset;
			mImageTex[11] = imageHeight - pixelPartB;
			
			mImageTex[12] = uvSide - offset;
			mImageTex[13] = imageHeight;
			
			mImageTex[14] = uvSide - offset;
			mImageTex[15] = imageHeight;
		}else{
			float imageWidth = mShowingImage.getWidth() - 1.0f/128.0f;
			float pixelPartA = halfWidthA / vertexDistance;
			float pixelPartB = halfWidthB / vertexDistance; 
			
			float offset = imageWidth / 256;

			mPixelVertices[0] = aX - (int)(halfWidthA * one);
			mPixelVertices[1] = aY + (int)(halfHeightA * one);
			mPixelVertices[2] = aZ;
			
			mPixelVertices[3] = aX - (int)(halfWidthA * one);
			mPixelVertices[4] = aY - (int)(halfHeightA * one);
			mPixelVertices[5] = aZ;
			
			mPixelVertices[6] = aX + (int)(halfWidthA * one);
			mPixelVertices[7] = aY + (int)(halfHeightA * one);
			mPixelVertices[8] = aZ;
			
			mPixelVertices[9] = aX + (int)(halfWidthA * one);
			mPixelVertices[10] = aY - (int)(halfHeightA * one);
			mPixelVertices[11] = aZ;

			mPixelVertices[12] = bX - (int)(halfWidthB * one);
			mPixelVertices[13] = bY + (int)(halfHeightB * one);
			mPixelVertices[14] = bZ;
			
			mPixelVertices[15] = bX - (int)(halfWidthB * one);
			mPixelVertices[16] = bY - (int)(halfHeightB * one);
			mPixelVertices[17] = bZ;
			
			mPixelVertices[18] = bX + (int)(halfWidthB * one);
			mPixelVertices[19] = bY + (int)(halfHeightB * one);
			mPixelVertices[20] = bZ;
			
			mPixelVertices[21] = bX - (int)(halfWidthB * one);
			mPixelVertices[22] = bY - (int)(halfHeightB * one);
			mPixelVertices[23] = bZ;
			
			mSmoothTex[0] = 0.0f;
			mSmoothTex[1] = 0.0f;
			
			mSmoothTex[2] = 0.0f;
			mSmoothTex[3] = 1.0f;
			
			mSmoothTex[4] = 0.5f;
			mSmoothTex[5] = 0.0f;
			
			mSmoothTex[6] = 0.5f;
			mSmoothTex[7] = 1.0f;	

			mSmoothTex[8] = 0.5f;
			mSmoothTex[9] = 0.0f;
			
			mSmoothTex[10] = 0.5f;
			mSmoothTex[11] = 1.0f;
			
			mSmoothTex[12] = 1.0f;
			mSmoothTex[13] = 0.0f;
			
			mSmoothTex[14] = 1.0f;
			mSmoothTex[15] = 1.0f;
			
			mImageTex[0] = 0.0f;
			mImageTex[1] = uvSide - offset;
			
			mImageTex[2] = 0.0f;
			mImageTex[3] = uvSide - offset;
			
			mImageTex[4] = pixelPartA;
			mImageTex[5] = uvSide - offset;
			
			mImageTex[6] = pixelPartA;
			mImageTex[7] = uvSide - offset;	

			mImageTex[8] = imageWidth - pixelPartB;
			mImageTex[9] = uvSide - offset;
			
			mImageTex[10] = imageWidth - pixelPartB;
			mImageTex[11] = uvSide - offset;
			
			mImageTex[12] = imageWidth;
			mImageTex[13] = uvSide - offset;
			
			mImageTex[14] = imageWidth;
			mImageTex[15] = uvSide - offset;
		}

		mSmoothTexBuffer.put(mSmoothTex);
		mSmoothTexBuffer.position(0);

		mImageTexBuffer.put(mImageTex);
		mImageTexBuffer.position(0);
		
		mSmoothingVertexBuffer.put(mPixelVertices);
		mSmoothingVertexBuffer.position(0);

//		if(time >= 6){
//			gl11.glDisable(GL11.GL_TEXTURE_2D);
//			gl11.glEnable(GL11.GL_COLOR_ARRAY);
//			gl.glColor4f(1.0f, 0.0f, 0.0f, 1.0f);
//			gl.glTexEnvx(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE, GL10.GL_MODULATE);
//		}else{
		if(mActivity.getSettings().blackEdges){
			gl11.glActiveTexture( GL11.GL_TEXTURE0 );
			gl11.glTexEnvi( GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE );
			gl11.glBindTexture(GL10.GL_TEXTURE_2D, mSmoothTextureID);
			gl11.glTexCoordPointer(2, GL10.GL_FLOAT, 0, mSmoothTexBuffer);
		}else{
			gl11.glActiveTexture( GL11.GL_TEXTURE0 );
			gl11.glTexEnvi( GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE );
			gl11.glColor4f(1.0f, 1.0f, 1.0f, alpha);
			gl11.glBindTexture(GL10.GL_TEXTURE_2D, mTextureID);
			
			
			gl11.glClientActiveTexture( GL11.GL_TEXTURE1 );
			gl11.glActiveTexture( GL11.GL_TEXTURE1 );
			
			gl11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
			
			gl11.glEnable(GL11.GL_TEXTURE_2D);
			gl11.glBindTexture(GL10.GL_TEXTURE_2D, mSmoothTextureID);
			
			gl11.glTexEnvi( GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_COMBINE );
			gl11.glTexEnvi( GL11.GL_TEXTURE_ENV, GL11.GL_COMBINE_RGB, GL11.GL_REPLACE );
			gl11.glTexEnvi( GL11.GL_TEXTURE_ENV, GL11.GL_SRC0_RGB, GL11.GL_PREVIOUS ); //note: just use same rgb
			gl11.glTexEnvi( GL11.GL_TEXTURE_ENV, GL11.GL_OPERAND0_RGB, GL11.GL_SRC_COLOR );
	
			/**/
			//note: replace alpha with tex-alpha
			gl11.glTexEnvi( GL11.GL_TEXTURE_ENV, GL11.GL_COMBINE_ALPHA, GL11.GL_REPLACE );
			gl11.glTexEnvi( GL11.GL_TEXTURE_ENV, GL11.GL_SRC0_ALPHA, GL11.GL_TEXTURE );
			gl11.glTexEnvi( GL11.GL_TEXTURE_ENV, GL11.GL_OPERAND0_ALPHA, GL11.GL_SRC_ALPHA );
			/*/
			//note: modulate image-alpha (1.0) with tex-alpha
			gl11.glTexEnvi( GL11.GL_TEXTURE_ENV, GL11.GL_COMBINE_ALPHA, GL11.GL_MODULATE );
			gl11.glTexEnvi( GL11.GL_TEXTURE_ENV, GL11.GL_SRC0_ALPHA, GL11.GL_PREVIOUS );
			gl11.glTexEnvi( GL11.GL_TEXTURE_ENV, GL11.GL_OPERAND0_ALPHA, GL11.GL_SRC_ALPHA );
			gl11.glTexEnvi( GL11.GL_TEXTURE_ENV, GL11.GL_SRC1_ALPHA, GL11.GL_TEXTURE );
			gl11.glTexEnvi( GL11.GL_TEXTURE_ENV, GL11.GL_OPERAND1_ALPHA, GL11.GL_SRC_ALPHA );
			/**/
			
			//gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, texBuffer);
		
			gl11.glClientActiveTexture( GL11.GL_TEXTURE0 );
			gl11.glTexCoordPointer(2, GL10.GL_FLOAT, 0, mImageTexBuffer);
			
			gl11.glClientActiveTexture( GL11.GL_TEXTURE1 );
			gl11.glTexCoordPointer(2, GL10.GL_FLOAT, 0, mSmoothTexBuffer);
			
			gl11.glActiveTexture( GL11.GL_TEXTURE1 );
		}
		gl11.glVertexPointer(3, GL10.GL_FIXED, 0, mSmoothingVertexBuffer);
		
		gl11.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 8);
		
		gl11.glActiveTexture( GL11.GL_TEXTURE1 ); 
		gl11.glDisable(GL11.GL_TEXTURE_2D);
		gl11.glActiveTexture( GL11.GL_TEXTURE0 );
		gl11.glEnable(GL11.GL_TEXTURE_2D);
		gl11.glClientActiveTexture( GL11.GL_TEXTURE0 );
		
		int error = gl11.glGetError();
		if(error != 0){
			Log.w("Floating Image", "Error: " + gl.glGetString(error));
		}
		
	}
	
	public static void setState(GL10 gl){
		gl.glTexEnvx(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE, GL10.GL_REPLACE);
		gl.glFrontFace(GL10.GL_CCW);
		gl.glEnable(GL10.GL_TEXTURE_2D);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER,GL10.GL_LINEAR);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D,GL10.GL_TEXTURE_MAG_FILTER,GL10.GL_LINEAR);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S,GL10.GL_CLAMP_TO_EDGE);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T,GL10.GL_CLAMP_TO_EDGE);
	}
	
	public static void unsetState(GL10 gl){
		gl.glDisable(GL10.GL_TEXTURE_2D);
	}
	
	
	/************ User transformations ************/
	float mInitialX, mInitialY, mInitialRotate;
	float mInitialScale = 1.0f;
	float mX, mY, mRotate;
	float mTmpX, mTmpY, mTmpRotate;
	float mScale = 1.0f;
	float mRestoreX, mRestoreY, mRestoreRotate;
	float mRestoreScale = 1.0f;
	boolean mRestoreTransformation = false;
	private static final long RESTORE_TIME = 400;
	long mRestoredAt;
	boolean mUseOriginalTex = false;
	boolean transforming = true;
	
	public void initTransform(){
		mInitialX += this.mX;
		mInitialY += this.mY;
		mInitialRotate += this.mRotate;
		mInitialScale *= this.mScale;
		mX = mY = mRotate = mTmpX = mTmpY = mTmpRotate = 0.0f;
		mScale = 1.0f;
		transforming = true;
	}
	
	public void transform(float centerX, float centerY, float x, float y, float rotate, float scale) {
		if(this.mState == STATE_FOCUSED){
			float invScale = 1.0f / mInitialScale;
			if(scale < invScale * 1.01f){
				scale = invScale;
				return;
			}else{
				this.mScale = Math.min(scale, 5.0f / mInitialScale); // Avoid focusing on a single pixel, that's just silly!
				x = x + centerX * (1.0f - scale);
				y = y + centerY * (1.0f - scale);
				this.move(x, y);
			}
			this.mRotate = rotate;
		}
	}
	
	public void transformEnd(){
		updateTexture();
		transforming = false;
	}
	
	private void updateTexture(){
		if(isTransformed()){
			if(!mUseOriginalTex){
				mTextureSelector.applyOriginal();
				mUseOriginalTex = true;
			}
		}else{
			if(mUseOriginalTex){
				mUseOriginalTex = false;
				mTextureSelector.applyLarge();
			}
		}
	}
	
	private boolean isTransformed(){
		return (mScale * mInitialScale > 1.1f) || mRotate < -1.0f || mRotate > 1.0f;
	}
	
	public boolean click(long realTime){
		if(isTransformed()){
			mRestoreTransformation = true;
			mRestoredAt = realTime + RESTORE_TIME;
			mInitialX += this.mX;
			mInitialY += this.mY;
			mInitialRotate += this.mRotate;
			mInitialScale *= this.mScale;
			this.mX = 0.0f;
			this.mY = 0.0f;
			this.mRotationA.setAngle(0.0f);
			this.mRotationB.setAngle(0.0f);
			this.mScale = 1.0f;
			mRestoreX = mInitialX;
			mRestoreY = mInitialY;
			mRestoreRotate = mInitialRotate;
			mRestoreScale = mInitialScale;
			return true;
		}
		return false;
	}
	
	public void updateTransformation(long realTime){
		if(mRestoreTransformation){
			if(realTime > mRestoredAt){
				mX = mY = mRotate = mInitialX = mInitialY = mInitialRotate = 0.0f;
				mInitialScale = mScale = 1.0f;
				mRestoreTransformation = false;
				updateTexture();
			}else{
				float fraction = (realTime - mRestoredAt + RESTORE_TIME) / (float)RESTORE_TIME;
				fraction = ImageUtil.smoothstep(fraction);
				mInitialX = mRestoreX * (1.0f - fraction);
				mInitialY = mRestoreY * (1.0f - fraction);
				mInitialRotate = mRestoreRotate * (1.0f - fraction);
				mInitialScale = mRestoreScale - (mRestoreScale - 1.0f) * (fraction);
			}
		}
		adjustMove();
	}
	
	public void adjustMove(){
		float width;
		float height;
		boolean isTall = isTall();
		if(isTall){
			height = mDisplay.getHeight();
			width = maspect * height;
		}else{
			width = mDisplay.getWidth();
			height = width / maspect;
		}
		float scale = mInitialScale * mScale;
		float maxX = width - scale * width;
		float maxY = height - scale * height;
		
		float maxDiffX =  maxX - mInitialX;
		float maxDiffY =  maxY - mInitialY;
		float minDiffX = -maxX - mInitialX;
		float minDiffY = -maxY - mInitialY;
		if(transforming){
			mX = mTmpX;
			mY = mTmpY;
		}
		if(mX < maxDiffX){
			float adjust = (mX - maxDiffX);
			mX -= adjust * 0.4f;
		}
		if(mX > minDiffX){
			float adjust = (mX - minDiffX);
			mX -= adjust * 0.4f;
		}
		if(mY < maxDiffY){
			float adjust = (mY - maxDiffY);
			mY -= adjust * 0.4f;
		}
		if(mY > minDiffY){
			float adjust = (mY - minDiffY);
			mY -= adjust * 0.4f;
		}
	}
	
	public boolean freeMove(){
		return isTransformed() && !mRestoreTransformation;
	}
	
	public void move(float x, float y){		
		this.mTmpX = x;
		this.mTmpY = y;
	}
	
	/************ Position functions ************/
	private void reJitter(){
		mPositionController.jitter();
	}
	
	public void traversalChanged(long frametime){
		mRotations = (int)((mPositionController.adjustTime(frametime, mActivity.getSettings().floatingTraversal) + 300 * mActivity.getSettings().floatingTraversal) / (float)mActivity.getSettings().floatingTraversal) - 299;
	}
			
	private boolean updateFloating(GL10 gl, long time){
		mAlpha = mDelete ? 0 : 1;
		boolean depthChanged = false;
		long totalTime = mPositionController.adjustTime(time, mActivity.getSettings().floatingTraversal);
		float interval = getInterval(time);
		
		Vec3f pos = mPositionController.getPosition(interval);
		mPos.set(pos);
		mPositionController.getRotation(interval, mRotationA, mRotationB);
		
		mPositionControlerScale = mPositionController.getScale();
		
		boolean isInRewind = totalTime < mActivity.getSettings().floatingTraversal * (mRotations - 1);
		// Get new texture...
		if(totalTime > mActivity.getSettings().floatingTraversal * mRotations && !isInRewind){
        	reJitter();
        	depthChanged = true;
        	//Log.v("Floating Image", "Getting new texture - forward!");
        	resetTexture(gl, true);
        	++mRotations;
        }
		// Read last texture (Rewind)
		if(isInRewind){
			reJitter();
			depthChanged = true;
			//Log.v("Floating Image", "Getting new texture - rewind!");
			resetTexture(gl, false);
			--mRotations;
        }
		if(mShowingImage == null && getInterval(time) < 0.01f){
			//Log.v("Floating Image", "Getting new texture - I need one!");
			resetTexture(gl, true);
		}
		return depthChanged;
	}
	
	private float smoothstep(float val){
		return Math.min(val * val * (3.0f - 2.0f * val), 1.0f);
	}
	
	private void moveToFocus(GL10 gl, long realTime){
		float fraction = FloatingRenderer.getFraction(realTime);
		if(fraction > 1){
			mRotationA.setAngle(0.0f);
			mRotationB.setAngle(0.0f);
			if(!mLargeTex){
				mTextureSelector.selectImage(this, this.mShowingImage);
			}
			mState = STATE_FOCUSED;
			return;
		}
		mRotationA.setAngle((1.0f - fraction) * mRotationASaved.getAngle());
		mPositionControlerScale = fraction + (1.0f - fraction) * mPositionController.getScale();
		fraction = smoothstep(fraction);
		float selectedX = mSelectedPos.getX();		
		float selectedY = mSelectedPos.getY();
		float selectedZ = mSelectedPos.getZ();
		
		float distX = FloatingRenderer.mFocusX - selectedX;// - mJitter.getX();
		float distY = FloatingRenderer.mFocusY + getFocusedOffset() - selectedY;// - mJitter.getY() + getFocusedOffset();
		float distZ = FloatingRenderer.mFocusZ - selectedZ;// - mJitter.getZ();
		
		mPos.setX(distX * fraction + selectedX);
		mPos.setY(distY * fraction + selectedY);
		mPos.setZ(distZ * fraction + selectedZ);		
	}
	
	private void moveToFloat(GL10 gl, long time, long realTime){
		float fraction = FloatingRenderer.getFraction(realTime);
		long totalTime = mPositionController.adjustTime(time, mActivity.getSettings().floatingTraversal);
		long timeToFloat = realTime - FloatingRenderer.mSelectedTime - FloatingRenderer.mFocusDuration;
		float interval = getInterval(time + timeToFloat);
		if(fraction > 1){
			if(mDelete){
				mAlpha = 0;
				mShowingImage.setDeleted();
			}
			
			mPositionController.getRotation(interval, mRotationA, mRotationB);
			mPositionControlerScale = mPositionController.getScale();
			long offset = mActivity.getSettings().floatingTraversal * 1024;
			mRotations = (int)((totalTime + offset) / mActivity.getSettings().floatingTraversal) + 1 - 1024;
			mState = STATE_FLOATING;
			if(mShowingImage != null){
				if(mActivity.getSettings().galleryMode){
					//mOnDemandBank.get(mShowingImage, this);
				}else{
					//setTexture(gl, mShowingImage);
				}
				mLargeTex = false;
			}
			return;
		}
		mAlpha = mDelete ? 1.0f - fraction : 1;
		
		float selectedX = mSelectedPos.getX();		
		float selectedY = mSelectedPos.getY();
		float selectedZ = mSelectedPos.getZ();
		
		fraction = smoothstep(fraction);
		mPositionController.getRotation(interval, mRotationA, mRotationB);
		
		mRotationA.setAngle(fraction * mRotationA.getAngle());
		mRotationB.setAngle(fraction * mRotationB.getAngle());
		Vec3f pos = mPositionController.getPosition(interval);
		
		mPositionControlerScale = 1.0f - fraction + (fraction) * mPositionController.getScale();
				
		float distX = pos.getX() - selectedX;
		float distY = pos.getY() - selectedY;
		float distZ = pos.getZ() - selectedZ;
		
		mPos.setX(distX * fraction + selectedX);
		mPos.setY(distY * fraction + selectedY);
		mPos.setZ(distZ * fraction + selectedZ);
	}
	
	float getInterval(long time){
		return mPositionController.adjustInterval(((time  + (mActivity.getSettings().floatingTraversal << 10)) % mActivity.getSettings().floatingTraversal) / (float)mActivity.getSettings().floatingTraversal);
	}
	
	public boolean update(GL10 gl, long time, long realTime){
		boolean depthChanged = false;
		if(mSetThumbnailTexture && mShowingImage != null){
			if(mShowingImage.getBitmap() != null){
				setTexture(gl, mShowingImage);
				mShowingImage.recycleBitmap();
				mImageAppliedTime = realTime;
			}else{
				mOnDemandBank.get(mShowingImage, this);
			}
		}
		mSetThumbnailTexture = false;
		// Make sure time is positive, then divide by traversal time to get how far image is
		if(mState == STATE_FLOATING){
			depthChanged = updateFloating(gl, time);		
		}else{
			if(mState == STATE_FOCUSING){
				moveToFocus(gl, realTime);
			}
			// We might change the value here...
			if(mState == STATE_FOCUSED){
				updateTransformation(realTime);
				mPositionControlerScale = 1.0f;
				setFocusedPosition();
				synchronized(this){
					if(mUpdateLargeTex && mFocusBmp != null){
						setFocusTexture(gl);
						mFocusBmp.recycle();
						mFocusBmp = null;
						if(!mLargeTex){
							// Only fade if coming from small texture!
							mLargeTexTime = realTime;
						}
						mLargeTex = true;
						mUpdateLargeTex = false;
					}
				}
			}
			if(mState == STATE_DEFOCUSING){
				moveToFloat(gl, time, realTime);
			}
		}
		return depthChanged;
	}
	
	private void setFocusedPosition(){
		mPos.setX(FloatingRenderer.mFocusX);
		mPos.setY(FloatingRenderer.mFocusY + getFocusedOffset());
		mPos.setZ(FloatingRenderer.mFocusZ);
	}
	
	private float getFocusedOffset(){
		return mDisplay.getHeight() - mDisplay.getFocusedHeight();
	}

	
	/************ Texture functions ************/
	
	private void resetTexture(GL10 gl, boolean next){
		mFocusBmp = null;
		mDelete = false;
		mImageNotSet = true;
		maspect = 1.3f;
		mOnDemandBank.get(this, next);
	}
	
	public boolean validForTextureUpdate(){
		return this.stateInFocus();
	}
	
	public void setFocusTexture(Bitmap texture, float width, float height, int imageSize){
		if(this.stateInFocus()){
			if(this.isTransformed() && imageSize != SIZE_ORIGINAL){
				// Ignore, but set large texture instead.
				if(texture != null){
					texture.recycle();
					mTextureSelector.applyOriginal();
				}
				return;
			}
			synchronized(this){
				this.mFocusBmp = texture;
				this.mFocusWidth = width;
				this.mFocusHeight = height;
				this.mUpdateLargeTex = true;
			}
		}else{
			if(texture != null){
				texture.recycle();
			}
		}
	}
	
	private void setFocusTexture(GL10 gl){
		if(mFocusBmp == null || mState != STATE_FOCUSED){
			return;
		}
		//long startTime = System.currentTimeMillis();
		//long timeA, timeB, timeC;
		gl.glGetError(); // Clear errors
		gl.glGetError(); // Clear errors
		float width = mFocusWidth;
		float height = mFocusHeight;
		
		maspect = width / height;
		boolean firstDraw = false;
		
		
		if(mLargeTexture.getTextureSize() != mFocusBmp.getWidth()){
			mLargeTexture.setTextureSize(mFocusBmp.getWidth());
			firstDraw = true;
		}
		gl.glBindTexture(GL10.GL_TEXTURE_2D, mLargeTexture.getTextureID(gl));

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
        	gl.glFinish();
        	//timeA = System.currentTimeMillis();
        	if(firstDraw){
        		Log.v("Floating Image", "Setting large texture");
    			GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, mFocusBmp, 0);
    		}else{
    			GLUtils.texSubImage2D(GL10.GL_TEXTURE_2D, 0, 0, 0, mFocusBmp);
    		}

        	//timeB = System.currentTimeMillis();
        	int error = gl.glGetError();
        	if(error != 0){
        		Log.w("Floating Image", "Error drawing on old image, trying to set new image.");
        		GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, mFocusBmp, 0);
        		if(error != 0){
        			String errString = GLU.gluErrorString(error);
        			throw new IllegalArgumentException("OpenGL error caught: " + errString);
        		}
        	}
        	
        	ByteBuffer tbb = ByteBuffer.allocateDirect(VERTS * 2 * 4);
            tbb.order(ByteOrder.nativeOrder());
            mLargeTexBuffer = tbb.asFloatBuffer();
            
            float tex[] = {
            	0.0f,  0.0f,
            	0.0f,  height - 1.0f/512.0f,
            	width - 1.0f/512.0f,  0.0f,
            	width - 1.0f/512.0f,  height - 1.0f/512.0f,
            };
            mLargeTexBuffer.put(tex);
            mLargeTexBuffer.position(0);
            
            //timeC = System.currentTimeMillis();
            //Log.v("Floating Image", "Set texture timings. A: " + (timeA - startTime) + "ms, B:" + (timeB - timeA) + "ms, C: " + (timeC - timeB) + "ms.");
        }catch(IllegalArgumentException e){
        	Log.w("Floating Image", "Large texture could not be shown", e);
			mLargeTex = false;
        	setTexture(gl, mShowingImage);
        }
        setState(gl);
	}
	
	public void setTexture(GL10 gl, ImageReference ir) {
		mLargeTex = false;
		float height = ir.getHeight();
		float width  = ir.getWidth();
		//Log.v("Floating Image", "Setting texture (" + ir.getBitmap().getWidth()+ "," + ir.getBitmap().getHeight()+ ")");
		maspect = width / height;
		gl.glBindTexture(GL10.GL_TEXTURE_2D, mTextureID);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
        gl.glTexEnvf(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE, GL10.GL_BLEND);
        
        try{
        	gl.glFinish();
        	if(mLastTextureSize != ir.getBitmap().getWidth()){
        		GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, ir.getBitmap(), 0);
        		mLastTextureSize = ir.getBitmap().getWidth();
        	}else{
        		GLUtils.texSubImage2D(GL10.GL_TEXTURE_2D, 0, 0, 0, ir.getBitmap());
        	}
        	
        	int error = gl.glGetError();
        	if(error != 0){
        		Log.w("Floating Image", "GL error in Image when setting small texture: " + GLU.gluErrorString(error));
        		GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, ir.getBitmap(), 0);
        		mLastTextureSize = ir.getBitmap().getWidth();
        		error = gl.glGetError();
            	if(error != 0){
            		Log.e("Floating Image", "GL error persistant in Image when setting small texture: " + GLU.gluErrorString(error));
            	}
        	}
	        
	        ByteBuffer tbb = ByteBuffer.allocateDirect(VERTS * 2 * 4);
	        tbb.order(ByteOrder.nativeOrder());
	        mTexBuffer = tbb.asFloatBuffer();
	        
	        float tex[] = {
	        	0.0f,  0.0f,
	        	0.0f,  height - 1.0f/128.0f,	
	        	width - 1.0f/128.0f,  0.0f,
	        	width - 1.0f/128.0f,  height - 1.0f/128.0f,
	        };
	        mTexBuffer.put(tex);
	        mTexBuffer.position(0);
	        mImageNotSet = false;
        }catch(IllegalArgumentException e){
        	Log.e("Floating Image", "Image: Error setting texture", e);
        }
        //setState(gl);
	}
	private int mTextureID;
	private int mSmoothTextureID;
	private FloatBuffer mTexBuffer;
	private FloatBuffer mLargeTexBuffer;
	
	private int VERTS = 4;
	
	/************ Ray intersection ************/
	
	public float intersect(Ray r){
		if(mShowingImage == null) return -1;
		if(mImageNotSet) return -1;
		float posX = mPos.getX();
		float posY = mPos.getY();
		float posZ = mPos.getZ();
		Vec3f pos = new Vec3f(posX, posY, posZ);

		float x0 = r.getO().getX();
		float y0 = r.getO().getY();
		float z0 = r.getO().getZ();
		float xd = r.getD().getX();
		float yd = r.getD().getY();
		float zd = r.getD().getZ();
		
		float scaleX;
		float scaleY;
		if(isTall()){ // Close enough, doing this right requires lots of work. :(
			scaleY = mDisplay.getFocusedHeight() * mDisplay.getFill();
			scaleX = maspect * scaleY;
		}else{
			scaleX = mDisplay.getWidth() * mDisplay.getFill();
			scaleY = scaleX / maspect;
		}
		scaleX *= mPositionControlerScale;
		scaleY *= mPositionControlerScale;
		
		// o is for origo :)
		float ox1;
		float ox2;
		float ox3;
		float oy1;
		float oy2;
		float oy3;
		float oz1;
		float oz2;
		float oz3;
		
		if(mShowingImage.getTargetOrientation() == 90 || mShowingImage.getTargetOrientation() == 270){
			oy1 = mVertices[0].getX() * scaleX;
			oy2 = mVertices[2].getX() * scaleX;
			oy3 = mVertices[3].getX() * scaleX;
			ox1 = mVertices[0].getY() * scaleY;
			ox2 = mVertices[2].getY() * scaleY;
			ox3 = mVertices[3].getY() * scaleY;
			oz1 = mVertices[0].getZ();
			oz2 = mVertices[2].getZ();
			oz3 = mVertices[3].getZ();
		}else{
			ox1 = mVertices[0].getX() * scaleX;
			ox2 = mVertices[2].getX() * scaleX;
			ox3 = mVertices[3].getX() * scaleX;
			oy1 = mVertices[0].getY() * scaleY;
			oy2 = mVertices[2].getY() * scaleY;
			oy3 = mVertices[3].getY() * scaleY;
			oz1 = mVertices[0].getZ();
			oz2 = mVertices[2].getZ();
			oz3 = mVertices[3].getZ();
		}		
		
		float x1 = ox1 + posX;
		float x2 = ox2 + posX;
		float x3 = ox3 + posX;
		float y1 = oy1 + posY;
		float y2 = oy2 + posY;
		float y3 = oy3 + posY;
		float z1 = oz1 + posZ;
		float z2 = oz2 + posZ;
		float z3 = oz3 + posZ;
		
		float a = y1 * (z2-z3) + y2 * (z3-z1) + y3 * (z1-z2); 
		float b = z1 * (x2-x3) + z2 * (x3-x1) + z3 * (x1-x2);
		float c = x1 * (y2-y3) + x2 * (y3-y1) + x3 * (y1-y2);
		float d =-x1 * (y2*z3 - y3*z2) - x2 * (y3*z1 - y1*z3) - x3 * (y1*z2 - y2*z1);
		
		
		float t = -(a*x0 + b*y0 + c*z0 + d) / (a*xd + b*yd + c*zd);
		float hitX = x0 + xd * t;
		float hitY = y0 + yd * t;
		float hitZ = z0 + zd * t;
		if(t < 0) return -1;
		
		Vec3f hitPoint = new Vec3f(hitX, hitY, hitZ);
		
		Vec3f v1 = new Vec3f(ox2 - ox1, oy2 - oy1, oz2 - oz1); // Right
		Vec3f v2 = new Vec3f(ox3 - ox2, oy3 - oy2, oz3 - oz2); // Down
		Vec3f v4 = new Vec3f(x1, y1, z1);
		hitPoint.minus(v4, v4);
		Vec3f v5 = new Vec3f(ox3, oy3, oz3);
		v5.plus(pos, v5);
		hitPoint.minus(v5, v5);
		
		if(v1.dot(v4) >= 0 && v1.dot(v5) <= 0 && v4.dot(v2) >= 0 && v5.dot(v2) <= 0){
			return t;
		}
		return -1;
	}

	@Override
	public boolean bitmapLoaded(String id) {
		if(this.doLoad(id)){
			mSetThumbnailTexture = true;
			return true;
		}	
    	return false;
	}

	@Override
	public Progress getProgressIndicator() {
		return null;
	}

	@Override
	public boolean doLoad(String id) {
		return mShowingImage != null && mShowingImage.getID().equals(id) && mImageNotSet;
	}

	@Override
	public void setEmptyImage(ImageReference ir) {
		this.mShowingImage = ir;
	}
}