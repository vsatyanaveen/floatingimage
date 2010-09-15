package dk.nindroid.rss.renderers.floating;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Random;

import javax.microedition.khronos.opengles.GL10;

import android.graphics.Bitmap;
import android.opengl.GLU;
import android.opengl.GLUtils;
import android.util.Log;
import dk.nindroid.rss.RiverRenderer;
import dk.nindroid.rss.ShowStreams;
import dk.nindroid.rss.TextureBank;
import dk.nindroid.rss.data.ImageReference;
import dk.nindroid.rss.data.Ray;
import dk.nindroid.rss.gfx.ImageUtil;
import dk.nindroid.rss.gfx.Vec3f;
import dk.nindroid.rss.renderers.ImagePlane;
import dk.nindroid.rss.renderers.ProgressBar;
import dk.nindroid.rss.settings.Settings;
import dk.nindroid.rss.uiActivities.Toaster;

public class Image implements ImagePlane {
	private static int  ids = 0;
	private static int	largeTextureID = -1;
	private static int	largeTextureSize = 0;
	private static boolean	revivingTextureNulled = false; 
	private static final int STATE_FLOATING =   0;
	private static final int STATE_FOCUSING =   1;
	private static final int STATE_FOCUSED	=   2;
	private static final int STATE_DEFOCUSING = 3;
	
	private int				mState = STATE_FLOATING;
	
	
	int						mID;
	private IntBuffer   	mVertexBuffer;
	private ByteBuffer  	mIndexBuffer;
	private ByteBuffer  	mLineIndexBuffer;
	private float  			maspect;
	private Vec3f			mPos;
	private int				mRotations;
	private float			mRotation;
	private float			mRotationSaved;
	private Vec3f			mJitter = new Vec3f(0.0f, 0.0f, 0.0f);
	private Random			mRand;
	private TextureBank 	mbank;
	private final long		mStartTime;
	private ImageReference 	mCurImage;
	private ImageReference 	mLastImage;
	private ImageReference 	mShowingImage;
	private boolean			mRewinding = false;	
	private Vec3f[]			mVertices;
	private int				mLastTextureSize;
	
	// Selected vars
	private Vec3f			mSelectedPos = new Vec3f();
	private float 			mYPos;
	private boolean			mUpdateLargeTex = false;
	private Bitmap			mFocusBmp;
	private float			mFocusWidth;
	private float			mFocusHeight;
	private boolean			mLargeTex = false;
	private boolean			mSetBackgroundWhenReady = false;
	
	public enum Pos {
		UP, MIDDLE, DOWN
	};
	
	public void init(GL10 gl, long time){
		int[] textures = new int[1];
		gl.glGenTextures(1, textures, 0);
		mTextureID = textures[0];
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
		return mJitter.getZ() + mPos.getZ();
	}
	
	public void setBackground(){
		if(mLargeTex){
			try {
				if(mFocusBmp.isRecycled()){
					Log.e("Floating Image", "Trying to set recycled bitmap as background!");
				}else{
					ShowStreams.current.setWallpaper(mFocusBmp);
				}
			} catch (IOException e) {
				Log.e("Floating Image", "Failed to get image", e);
				Toaster toaster = new Toaster("Sorry, there was an error setting wallpaper!");
				ShowStreams.current.runOnUiThread(toaster);
			}
		}else{
			mSetBackgroundWhenReady = true;
		}
	}
	
	public void revive(GL10 gl, long time){
		// Make sure the textures are redrawn after subactivity
		if(!revivingTextureNulled){
			revivingTextureNulled = true;
			largeTextureSize = 0;
		}
		mLastTextureSize = 0;
		// Revive textures
		if(mState == STATE_FOCUSED && mFocusBmp != null){
			setFocusTexture(gl);
		}else{
			if(mShowingImage != null){
				mRotations = (int)((time - mStartTime) / Settings.floatingTraversal) + 1;
				setTexture(gl, mShowingImage);
			}
		}
	}
	public void reset(GL10 gl, long time){
		if(mFocusBmp != null && !mFocusBmp.isRecycled()){
			mFocusBmp.recycle();			
		}
		mFocusBmp = null;
		mState = STATE_FLOATING;
		if(mShowingImage != null && !mShowingImage.getBitmap().isRecycled()){
			mShowingImage.getBitmap().recycle();			
		}
		mShowingImage = null;
		if(mCurImage != null && !mCurImage.getBitmap().isRecycled()){
			mCurImage.getBitmap().recycle();
		}
		mCurImage = null;
		if(mLastImage != null && !mLastImage.getBitmap().isRecycled()){
			mLastImage.getBitmap().recycle();
		}
		mLastImage = null;
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
			mRotationSaved = mRotation;
			// Get large texture, if not already there.
			InfoBar.select(gl, this.mShowingImage);			
		}
	}
	
	public void setSelected(GL10 gl, Bitmap bmp, float width, float height, long time){
		mState = STATE_FOCUSED;
		setFocusedPosition();
		mRotation = 0.0f;
		mFocusBmp = bmp;
		mFocusWidth = width;
		mFocusHeight = height;
		mLargeTex = true;
		setFocusTexture(gl);
		if(mSetBackgroundWhenReady){
			setBackground();
		}
		mSetBackgroundWhenReady = false;
	}
	
	protected float getYPos(){
		return mYPos * RiverRenderer.mDisplay.getFocusedHeight();
	}
	
	public Image(TextureBank bank, Pos layer, long startTime){
		this.mID = ids++;
		mbank = bank;
		mRand = new Random(startTime + mID);
		switch(layer){
			case UP: mYPos = 1.25f; break;
			case MIDDLE: mYPos = 0.0f; break;
			case DOWN: mYPos = -1.25f; break;
		}
		mPos = new Vec3f(-FloatingRenderer.getFarRight(), mYPos, FloatingRenderer.mFloatZ);
		reJitter();
		mStartTime = startTime;
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
		return isTall(RiverRenderer.mDisplay.getWidth() / RiverRenderer.mDisplay.getFocusedHeight());
	}
	
	private boolean isTall(float screenAspect){
		boolean tall = maspect < screenAspect;
		return tall;	
	}
	
	public float getScale(float szX, float szY, boolean sideways){
		float height = RiverRenderer.mDisplay.getFocusedHeight() * RiverRenderer.mDisplay.getFill();
		float width = RiverRenderer.mDisplay.getWidth() * RiverRenderer.mDisplay.getFill();
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
		if(mShowingImage == null && !mLargeTex){
			return;
		}
		
		float x, y, z, szX, szY;
		x = mPos.getX() + mJitter.getX();
		y = mPos.getY() + mJitter.getY();
		z = mPos.getZ() + mJitter.getZ();
		if(mShowingImage != null){
			szY = 5.0f; // Huge, since we only scale down.
			szX = szY * maspect;
			
			float scale = getScale(szX, szY, realTime);
			szX *= scale;
			szY *= scale;
		}else{ // Splash screen only!
			if(isTall()){
				szY = RiverRenderer.mDisplay.getFocusedHeight();
				szY *= RiverRenderer.mDisplay.getFill();
				
				szX = maspect * szY;
			}else{
				szX = RiverRenderer.mDisplay.getWidth();
				szX *= RiverRenderer.mDisplay.getFill();
				
				szY = szX / maspect;
			}
		}
		
		gl.glFrontFace(GL10.GL_CCW);
		gl.glEnable(GL10.GL_TEXTURE_2D);
		
		gl.glPushMatrix();
		
		float rotation = mRotation;
		if(mShowingImage != null){
			rotation += mShowingImage.getRotation(realTime);
		}
		float userScale = mScale * mInitialScale;
		float userMoveX = mX + mInitialX;
		float userMoveY = mY + mInitialY;
		
		x += userMoveX;
		y += userMoveY;
		szX *= userScale;
		szY *= userScale;
		
		gl.glEnable(GL10.GL_BLEND);
		
		if(Settings.imageDecorations){
			if(mCurImage != null && mCurImage.isNew()){
				GlowImage.draw(gl, x, y, z, rotation, szX, szY);
			}
			else{
				ShadowPainter.draw(gl, x, y, z, rotation, szX, szY);
			}
		}
		
		gl.glTranslatef(x, y, z);
		gl.glRotatef(rotation, 0, 0, 1);
		gl.glScalef(szX, szY, 1);
		
		gl.glDisable(GL10.GL_BLEND);
		// Draw image
		gl.glTexEnvx(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE, GL10.GL_REPLACE);
		gl.glActiveTexture(GL10.GL_TEXTURE0);
		if(mLargeTex){
			gl.glBindTexture(GL10.GL_TEXTURE_2D, largeTextureID);
		}else{
			gl.glBindTexture(GL10.GL_TEXTURE_2D, mTextureID);
		}
        gl.glVertexPointer(3, GL10.GL_FIXED, 0, mVertexBuffer);
        
		gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, mTexBuffer);
		
		gl.glDrawElements(GL10.GL_TRIANGLE_STRIP, 4, GL10.GL_UNSIGNED_BYTE, mIndexBuffer);
		
		// Smooth images
		gl.glEnable(GL10.GL_BLEND);
		gl.glEnable(GL10.GL_POINT_SMOOTH);
		gl.glEnable(GL10.GL_LINE_SMOOTH);
		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		gl.glLineWidth(1.0f);
		gl.glDrawElements(GL10.GL_LINE_STRIP, 5, GL10.GL_UNSIGNED_BYTE, mLineIndexBuffer);
		
        gl.glPopMatrix();
        
        if(mState == STATE_FOCUSED && !mLargeTex){
        	ProgressBar.draw(gl, FloatingRenderer.mTextureSelector.getProgress());
        }
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
	
	
	/************ User transformations ************/
	float mInitialX, mInitialY, mInitialRotate;
	float mInitialScale = 1.0f;
	float mX, mY, mRotate;
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
		mX = mY = mRotate = 0.0f;
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
		if(!mUseOriginalTex){
			FloatingRenderer.mTextureSelector.applyOriginal();
			mUseOriginalTex = true;
		}else{
			mUseOriginalTex = false;
			FloatingRenderer.mTextureSelector.applyLarge();	
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
			this.mRotation = 0.0f;
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
			height = RiverRenderer.mDisplay.getHeight();
			width = maspect * height;
		}else{
			width = RiverRenderer.mDisplay.getWidth();
			height = width / maspect;
		}
		float scale = mInitialScale * mScale;
		float maxX = width - scale * width;
		float maxY = height - scale * height;
		
		float maxDiffX =  maxX - mInitialX;
		float maxDiffY =  maxY - mInitialY;
		float minDiffX = -maxX - mInitialX;
		float minDiffY = -maxY - mInitialY;
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
		this.mX = x;
		this.mY = y;
	}
	
	/************ Position functions ************/
	private void reJitter(){
		mJitter.setX(mRand.nextFloat() * FloatingRenderer.mJitterX * 2 - FloatingRenderer.mJitterX);
		mJitter.setY(mRand.nextFloat() * FloatingRenderer.mJitterY * 2 - FloatingRenderer.mJitterY);
		mJitter.setZ(mRand.nextFloat() * FloatingRenderer.mJitterZ * 2 - FloatingRenderer.mJitterZ);
		mRotation = Settings.rotateImages ? mRand.nextFloat() * 20.0f - 10.0f : 0;
	}
	
	private float getXPos(long relativeTime){
		return -FloatingRenderer.getFarRight() + (((float)(relativeTime % Settings.floatingTraversal) / Settings.floatingTraversal) * FloatingRenderer.getFarRight() * 2);
	}
	
	private boolean updateFloating(GL10 gl, long time){
		boolean depthChanged = false;
		long totalTime = time - mStartTime;
		
		mPos.setZ(FloatingRenderer.mFloatZ);
		mPos.setY(getYPos()); 
		mPos.setX(getXPos(totalTime));
		boolean isInRewind = totalTime < Settings.floatingTraversal * (mRotations - 1);
		// Get new texture...
		if(totalTime > Settings.floatingTraversal * mRotations && !isInRewind){
        	reJitter();
        	depthChanged = true;
        	resetTexture(gl);
        	++mRotations;
        }
		// Read last texture (Rewind)
		if(isInRewind && mLastImage != null && !mRewinding){
			reJitter();
			depthChanged = true;
        	setTexture(gl, mLastImage);
        	mShowingImage = mLastImage;
        	mRewinding = true;
        }
		// Exit rewinding mode
		if(mRewinding && totalTime > Settings.floatingTraversal * (mRotations - 1)){
			resetTexture(gl);
			mRewinding = false;
		}
		if(mShowingImage == null & mPos.getX() < -5.0f){
			resetTexture(gl);
		}
		return depthChanged;
	}
	
	private float smoothstep(float val){
		return Math.min(val * val * (3.0f - 2.0f * val), 1.0f);
	}
	
	private void moveToFocus(GL10 gl, long realTime){
		float fraction = FloatingRenderer.getFraction(realTime);
		if(fraction > 1){
			mRotation = 0.0f;
			if(!mLargeTex){
				FloatingRenderer.mTextureSelector.selectImage(this, this.mShowingImage);
			}
			mState = STATE_FOCUSED;
			return;
		}
		mRotation = (1.0f - fraction) * mRotationSaved;
		fraction = smoothstep(fraction);
		float selectedX = mSelectedPos.getX();		
		float selectedY = mSelectedPos.getY();
		float selectedZ = mSelectedPos.getZ();
		
		float distX = FloatingRenderer.mFocusX - selectedX - mJitter.getX();
		float distY = FloatingRenderer.mFocusY - selectedY - mJitter.getY() + getFocusedOffset();
		float distZ = FloatingRenderer.mFocusZ - selectedZ - mJitter.getZ();
		
		mPos.setX(distX * fraction + selectedX);
		mPos.setY(distY * fraction + selectedY);
		mPos.setZ(distZ * fraction + selectedZ);		
	}
	
	private void moveToFloat(GL10 gl, long time, long realTime){
		float fraction = FloatingRenderer.getFraction(realTime);
		if(fraction > 1){
			mRotation = mRotationSaved;
			long totalTime = time - mStartTime;
			mRotations = (int)(totalTime / Settings.floatingTraversal) + 1;
			mState = STATE_FLOATING;
			mRewinding = false;
			if(mShowingImage != null){
				setTexture(gl, mShowingImage);
				if(mFocusBmp != null){
					mFocusBmp.recycle();
					mFocusBmp = null;
				}
				mLargeTex = false;
			}
			return;
		}
		
		float selectedX = mSelectedPos.getX();		
		float selectedY = mSelectedPos.getY();
		float selectedZ = mSelectedPos.getZ();
		
		fraction = smoothstep(fraction);
		
		long timeToFloat = realTime - mStartTime - FloatingRenderer.mSelectedTime - FloatingRenderer.mFocusDuration;
		
		mRotation = fraction * mRotationSaved;
		float floatX = getXPos(time + timeToFloat);
		
		float distX = floatX - selectedX;
		float distY = getYPos() - selectedY;
		float distZ = FloatingRenderer.mFloatZ - selectedZ;
		
		mPos.setX(distX * fraction + selectedX);
		mPos.setY(distY * fraction + selectedY);
		mPos.setZ(distZ * fraction + selectedZ);
	}
	
	public boolean update(GL10 gl, long time, long realTime){
		updateTransformation(realTime);
		boolean depthChanged = false;
		if(mState == STATE_FLOATING){
			depthChanged = updateFloating(gl, time);		
		}else{
			if(mState == STATE_FOCUSING){
				moveToFocus(gl, realTime);
			}
			// We might change the value here...
			if(mState == STATE_FOCUSED){
				setFocusedPosition();
				synchronized(this){
					if(mUpdateLargeTex && mFocusBmp != null){
						setFocusTexture(gl);
						mLargeTex = true;
						mUpdateLargeTex = false;
					}
				}
			}
			if(mState == STATE_DEFOCUSING){
				moveToFloat(gl, time, realTime);
			}
		}
		revivingTextureNulled = false;
		return depthChanged;
	}
	
	private void setFocusedPosition(){
		mPos.setX(FloatingRenderer.mFocusX);
		mPos.setY(FloatingRenderer.mFocusY + getFocusedOffset());
		mPos.setZ(FloatingRenderer.mFocusZ);
		mPos.minus(mJitter, mPos);
	}
	
	private float getFocusedOffset(){
		return RiverRenderer.mDisplay.getHeight() - RiverRenderer.mDisplay.getFocusedHeight();
	}

	
	/************ Texture functions ************/
	
	private void resetTexture(GL10 gl){
		mFocusBmp = null;
		ImageReference deprecatedImage = mLastImage;
		if(!mRewinding){
			ImageReference ir = mbank.getTexture(mCurImage);
			if(ir != null){
				mLastImage = mCurImage;
				mCurImage = ir;
				if(deprecatedImage != null){
					deprecatedImage.getBitmap().recycle();
				}
			}
		}	
    	if(mCurImage != null){
    		setTexture(gl, mCurImage);
    		mShowingImage = mCurImage;
    	}else{
    		this.mFocusBmp = null;
    		this.mLargeTex = false;
    	}
	}
	
	public void setFocusTexture(Bitmap texture, float width, float height, int imageSize){
		if(this.stateInFocus()){
			if(this.isTransformed() && imageSize != SIZE_ORIGINAL){
				// Ignore, but set large texture instead.
				if(texture != null){
					texture.recycle();
					FloatingRenderer.mTextureSelector.applyOriginal();
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
			texture.recycle();
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
		if(largeTextureID == -1){
			int[] textures = new int[1];
			gl.glGenTextures(1, textures, 0);
			largeTextureID = textures[0];
		}
		if(largeTextureSize != mFocusBmp.getWidth()){
			largeTextureSize = mFocusBmp.getWidth();
			firstDraw = true;
		}
		gl.glBindTexture(GL10.GL_TEXTURE_2D, largeTextureID);

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
            mTexBuffer = tbb.asFloatBuffer();
            
            float tex[] = {
            	0.0f,  0.0f,
            	0.0f,  height - 0.005f,
            	width - 0.005f,  0.0f,
            	width - 0.005f,  height - 0.005f,
            };
            mTexBuffer.put(tex);
            mTexBuffer.position(0);
            
            //timeC = System.currentTimeMillis();
            //Log.v("Floating Image", "Set texture timings. A: " + (timeA - startTime) + "ms, B:" + (timeB - timeA) + "ms, C: " + (timeC - timeB) + "ms.");
        }catch(IllegalArgumentException e){
        	Log.w("Floating Image", "Large texture could not be shown", e);
        	mFocusBmp.recycle();
			mFocusBmp = null;
			mLargeTex = false;
        	setTexture(gl, mShowingImage);
        }
        setState(gl);
	}
	
	public void setTexture(GL10 gl, ImageReference ir) {
		mLargeTex = false;
		
		float height = ir.getHeight();
		float width  = ir.getWidth();
		
		maspect = width / height;
		gl.glBindTexture(GL10.GL_TEXTURE_2D, mTextureID);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
        gl.glTexEnvf(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE, GL10.GL_BLEND);
        
        try{
        	if(mLastTextureSize != ir.getBitmap().getWidth()){
        		GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, ir.getBitmap(), 0);
        		mLastTextureSize = ir.getBitmap().getWidth();
        	}else{
        		GLUtils.texSubImage2D(GL10.GL_TEXTURE_2D, 0, 0, 0, ir.getBitmap());
        	}
	        
	        ByteBuffer tbb = ByteBuffer.allocateDirect(VERTS * 2 * 4);
	        tbb.order(ByteOrder.nativeOrder());
	        mTexBuffer = tbb.asFloatBuffer();
	        
	        float tex[] = {
	        	0.0f,  0.0f,
	        	0.0f,  height - 0.005f,	
	        	width - 0.005f,  0.0f,
	        	width - 0.005f,  height - 0.005f,
	        };
	        mTexBuffer.put(tex);
	        mTexBuffer.position(0);
        }catch(IllegalArgumentException e){
        	Log.e("Floating Image", "Image: Error setting texture", e);
        }
        setState(gl);
	}
	private int mTextureID;
	private FloatBuffer mTexBuffer;
	
	private int VERTS = 4;
	
	/************ Ray intersection ************/
	
	public float intersect(Ray r){
		if(mShowingImage == null) return -1;
		float posX = mPos.getX() + mJitter.getX();
		float posY = mPos.getY() + mJitter.getY();
		float posZ = mPos.getZ() + mJitter.getZ();
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
			scaleY = RiverRenderer.mDisplay.getFocusedHeight() * RiverRenderer.mDisplay.getFill();
			scaleX = maspect * scaleY;
		}else{
			scaleX = RiverRenderer.mDisplay.getWidth() * RiverRenderer.mDisplay.getFill();
			scaleY = scaleX / maspect;
		}
		
		// o is for origo :)
		float ox1 = mVertices[0].getX() * scaleX;
		float ox2 = mVertices[2].getX() * scaleX;
		float ox3 = mVertices[3].getX() * scaleX;
		float oy1 = mVertices[0].getY() * scaleY;
		float oy2 = mVertices[2].getY() * scaleY;
		float oy3 = mVertices[3].getY() * scaleY;
		float oz1 = mVertices[0].getZ();
		float oz2 = mVertices[2].getZ();
		float oz3 = mVertices[3].getZ();
		
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
}