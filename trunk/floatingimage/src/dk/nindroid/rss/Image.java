package dk.nindroid.rss;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Date;
import java.util.Random;

import javax.microedition.khronos.opengles.GL10;

import android.graphics.Bitmap;
import android.opengl.GLUtils;
import android.util.Log;
import dk.nindroid.rss.data.ImageReference;
import dk.nindroid.rss.data.Ray;
import dk.nindroid.rss.gfx.Vec3f;
import dk.nindroid.rss.helpers.MatrixTrackingGL;
import dk.nindroid.rss.settings.Settings;

public class Image {
	private static int  ids = 0;
	
	private static final int STATE_FLOATING =   0;
	private static final int STATE_FOCUSING =   1;
	private static final int STATE_FOCUSED =    2;
	private static final int STATE_DEFOCUSING = 3;
	private int			mState = STATE_FLOATING;
	
	int					mID;
	private IntBuffer   mVertexBuffer;
	private ByteBuffer  mIndexBuffer;
	private float  		maspect;
	private Vec3f		mPos;
	private int			mRotations;
	private float		mRotation;
	private float		mRotationSaved;
	private Vec3f		mJitter = new Vec3f(0.0f, 0.0f, 0.0f);
	private Random		mRand;
	private TextureBank mbank;
	private final long		mStartTime;
	private final long 	mTraversal;
	private ImageReference mCurImage;
	private ImageReference mLastImage;
	private ImageReference mShowingImage;
	private boolean		mRewinding = false;	
	private Vec3f[]		mVertices;
	private MatrixTrackingGL gl2;
	
	// Selected vars
	private Vec3f		mSelectedPos = new Vec3f();
	private float		mFocusedOffset;
	private float 		mYPos;
	private Bitmap		mFocusBmp;
	private float		mFocusWidth;
	private float		mFocusHeight;
	private boolean		mLargeTex = false;
	private float[]		mModelviewMatrix = new float[16];
	
	public enum Pos {
		UP, MIDDLE, DOWN
	};
	
	public void init(GL10 gl, long time){
		gl2 = new MatrixTrackingGL(gl);
		int[] textures = new int[1];
		gl.glGenTextures(1, textures, 0);
		mTextureID = textures[0];
		revive(gl, time);
	}
	
	public boolean inFocus(){
		return mState == STATE_FOCUSED;
	}
	
	public void revive(GL10 gl, long time){
		// Revive textures
		if(mState == STATE_FOCUSED && mFocusBmp != null){
			setFocusTexture(gl);
		}else{
			if(mShowingImage != null){
				mRotations = (int)((time - mStartTime) / mTraversal) + 1;
				setTexture(gl, mShowingImage);
			}
		}
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
			mState = STATE_DEFOCUSING;
		}else{
			if(mShowingImage == null){
				return;
			}
			mFocusedOffset = isTall() ? 2.0f - RiverRenderer.mDisplayHeight : 0.0f;
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
	}
	
	public Image(TextureBank bank, long traversal, Pos layer, long startTime){
		this.mID = ids++;
		mTraversal = traversal;
		mbank = bank;
		mRand = new Random(new Date().getTime());
		switch(layer){
			case UP: mYPos = 2.5f; break;
			case MIDDLE: mYPos = 0.0f; break;
			case DOWN: mYPos = -2.5f; break;
		}
		mPos = new Vec3f(-RiverRenderer.mFarRight, mYPos, RiverRenderer.mFloatZ);
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
	
	// Draw this after draw.
	public void drawGlow(GL10 gl){
		gl.glPushMatrix();
		gl.glLoadMatrixf(mModelviewMatrix, 0);
		if(mCurImage != null && mCurImage.isNew()){
			GlowImage.draw(gl);
		}
		else{
			ShadowPainter.draw(gl);
		}
		gl.glPopMatrix();
	}
	
	private boolean isTall(){
		return maspect < RiverRenderer.mDisplayRatio * 1.5f;
	}
	
	public void draw(GL10 gl, long time, long realTime){
		update(gl, time, realTime);
		
		float x, y, z, szX, szY;
		x = mPos.getX() + mJitter.getX();
		y = mPos.getY() + mJitter.getY();
		z = mPos.getZ() + mJitter.getZ();
		if(isTall()){
			szY = RiverRenderer.mDisplayHeight * 0.9f;
			szX = maspect * szY;
		}else{
			szX = RiverRenderer.mDisplayRatio * 1.9f;
			szY = szX / maspect;
		}
		
		gl.glActiveTexture(GL10.GL_TEXTURE0);
        gl.glBindTexture(GL10.GL_TEXTURE_2D, mTextureID);
        gl.glVertexPointer(3, GL10.GL_FIXED, 0, mVertexBuffer);
        
		gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, mTexBuffer);
		
		gl2.glPushMatrix();
				
		gl2.glTranslatef(x, y, z);
		gl2.glRotatef(mRotation, 0, 0, 1);
		gl2.glScalef(szX, szY, 1);
		gl2.glDrawElements(GL10.GL_TRIANGLE_STRIP, 4, GL10.GL_UNSIGNED_BYTE, mIndexBuffer);
		
		gl2.getMatrix(mModelviewMatrix, 0);
        
        gl2.glPopMatrix();
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
	
	
	/************ Position functions ************/
	
	private void reJitter(){
		mJitter.setX(mRand.nextFloat() * RiverRenderer.mJitterX * 2 - RiverRenderer.mJitterX);
		mJitter.setY(mRand.nextFloat() * RiverRenderer.mJitterY * 2 - RiverRenderer.mJitterY);
		mJitter.setZ(mRand.nextFloat() * RiverRenderer.mJitterZ * 2 - RiverRenderer.mJitterZ);
		mRotation = Settings.rotateImages ? mRand.nextFloat() * 20.0f - 10.0f : 0;
	}
	
	private float getXPos(long relativeTime){
		return -RiverRenderer.mFarRight + (((float)(relativeTime % mTraversal) / mTraversal) * RiverRenderer.mFarRight * 2);
	}
	
	private void updateFloating(GL10 gl, long time){
		long totalTime = time - mStartTime;
		
		mPos.setZ(RiverRenderer.mFloatZ);
		mPos.setY(mYPos); 
		mPos.setX(getXPos(totalTime));
		boolean isInRewind = totalTime < mTraversal * (mRotations - 1);
		// Get new texture...
		if(totalTime > mTraversal * mRotations && !isInRewind){
        	reJitter();
        	resetTexture(gl);
        	++mRotations;
        }
		// Read last texture (Rewind)
		if(isInRewind && mLastImage != null && !mRewinding){
			reJitter();
        	setTexture(gl, mLastImage);
        	mShowingImage = mLastImage;
        	mRewinding = true;
        }
		// Exit rewinding mode
		if(mRewinding && totalTime > mTraversal * (mRotations - 1)){
			resetTexture(gl);
			mRewinding = false;
		}
		if(mShowingImage == null & mPos.getX() < -5.0f){
			resetTexture(gl);
		}
	}
	
	private float smoothstep(float val){
		return Math.min(val * val * (3.0f - 2.0f * val), 1.0f);
	}
	
	private void moveToFocus(GL10 gl, long realTime){
		float fraction = RiverRenderer.getFraction(realTime);
		if(fraction > 1){
			mRotation = 0.0f;
			if(!mLargeTex){
				TextureSelector.selectImage(this, this.mShowingImage);
			}
			mState = STATE_FOCUSED;
			return;
		}
		mRotation = (1.0f - fraction) * mRotationSaved;
		fraction = smoothstep(fraction);
		float selectedX = mSelectedPos.getX();		
		float selectedY = mSelectedPos.getY();
		float selectedZ = mSelectedPos.getZ();
		
		float distX = RiverRenderer.mFocusX - selectedX - mJitter.getX();
		float distY = RiverRenderer.mFocusY - selectedY - mJitter.getY() + mFocusedOffset;
		float distZ = RiverRenderer.mFocusZ - selectedZ - mJitter.getZ();
		
		mPos.setX(distX * fraction + selectedX);
		mPos.setY(distY * fraction + selectedY);
		mPos.setZ(distZ * fraction + selectedZ);		
	}
	
	private void moveToFloat(GL10 gl, long time, long realTime){
		float fraction = RiverRenderer.getFraction(realTime);
		if(fraction > 1){
			mRotation = mRotationSaved;
			long totalTime = time - mStartTime;
			mRotations = (int)(totalTime / mTraversal) + 1;
			mState = STATE_FLOATING;
			mRewinding = false;
			if(mCurImage != null){
				mCurImage.setOld();
				setTexture(gl, mShowingImage);
				mFocusBmp = null;
			}
			return;
		}
		
		float selectedX = mSelectedPos.getX();		
		float selectedY = mSelectedPos.getY();
		float selectedZ = mSelectedPos.getZ();
		
		fraction = smoothstep(fraction);
		
		long timeToFloat = realTime - mStartTime - RiverRenderer.mSelectedTime - RiverRenderer.mFocusDuration;
		
		mRotation = fraction * mRotationSaved;
		float floatX = getXPos(time + timeToFloat);
		
		float distX = floatX - selectedX;
		float distY = mYPos - selectedY;
		float distZ = RiverRenderer.mFloatZ - selectedZ;
		
		mPos.setX(distX * fraction + selectedX);
		mPos.setY(distY * fraction + selectedY);
		mPos.setZ(distZ * fraction + selectedZ);
	}
	
	private void update(GL10 gl, long time, long realTime){
		if(mState == STATE_FLOATING){
			updateFloating(gl, time);		
		}else{
			if(mState == STATE_FOCUSING){
				moveToFocus(gl, realTime);
			}
			// We might change the value here...
			if(mState == STATE_FOCUSED){
				setFocusedPosition();
				synchronized(this){
					if(!mLargeTex && mFocusBmp != null){
						setFocusTexture(gl);
						mLargeTex = true;
					}
				}
			}
			if(mState == STATE_DEFOCUSING){
				moveToFloat(gl, time, realTime);
			}
		}
	}
	
	private void setFocusedPosition(){
		mPos.setX(RiverRenderer.mFocusX);
		mPos.setY(RiverRenderer.mFocusY + mFocusedOffset);
		mPos.setZ(RiverRenderer.mFocusZ);
		mPos.minus(mJitter, mPos);
	}

	
	/************ Texture functions ************/
	
	private void resetTexture(GL10 gl){
		mFocusBmp = null;
		if(!mRewinding){
			if(mLastImage != null){
				mLastImage.getBitmap().recycle();
			}
			mLastImage = mCurImage;
			mCurImage = mbank.getTexture(mCurImage);
		}	
    	if(mCurImage != null){
    		setTexture(gl, mCurImage);
    		mShowingImage = mCurImage;
    	}
	}
	
	public void setFocusTexture(Bitmap texture, float width, float height){
		synchronized(this){
			this.mFocusBmp = texture;
			this.mFocusWidth = width;
			this.mFocusHeight = height;
		}
	}
	
	private void setFocusTexture(GL10 gl){
		if(mFocusBmp == null){ // TODO: Handle graciously
			return;
		}
		float width = mFocusWidth;
		float height = mFocusHeight;
		
		maspect = width / height;
        
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
        
        try{
        	GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, mFocusBmp, 0);
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
        }catch(IllegalArgumentException e){ // TODO: Handle graciously
        	Log.w("dk.nindroid.rss.Image", "Texture could not be shown", e);
        	setTexture(gl, mShowingImage);
        	return;
        }
        setState(gl);
	}
	
	public void setTexture(GL10 gl, ImageReference ir) {
		mLargeTex = false;
		
		float height = ir.getHeight();
		float width  = ir.getWidth();
		
		maspect = width / height;
		
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
        
        GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, ir.getBitmap(), 0);
        
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
        setState(gl);
	}
	private int mTextureID;
	private FloatBuffer mTexBuffer;
	
	private int VERTS = 4;
	
	/************ Ray intersection ************/
	
	public float intersect(Ray r){
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
		float x1 = mVertices[0].getX() + posX;
		float x2 = mVertices[2].getX() + posX;
		float x3 = mVertices[3].getX() + posX;
		float y1 = mVertices[0].getY() + posY;
		float y2 = mVertices[2].getY() + posY;
		float y3 = mVertices[3].getY() + posY;
		float z1 = mVertices[0].getZ() + posZ;
		float z2 = mVertices[2].getZ() + posZ;
		float z3 = mVertices[3].getZ() + posZ;
		
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
		
		Vec3f v1 = new Vec3f();
		mVertices[2].minus(mVertices[0], v1); // Right
		Vec3f v2 = new Vec3f();
		mVertices[3].minus(mVertices[2], v2); // Down
		Vec3f v4 = new Vec3f();
		mVertices[0].plus(pos, v4);
		hitPoint.minus(v4, v4);
		Vec3f v5 = new Vec3f();
		mVertices[3].plus(pos, v5);
		hitPoint.minus(v5, v5);
	
		
		if(v1.dot(v4) >= 0 && v1.dot(v5) <= 0 && v4.dot(v2) >= 0 && v5.dot(v2) <= 0){
			return t;
		}
		return -1;
	}
}