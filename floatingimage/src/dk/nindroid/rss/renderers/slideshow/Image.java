package dk.nindroid.rss.renderers.slideshow;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.microedition.khronos.opengles.GL10;

import android.graphics.Bitmap;
import android.opengl.GLUtils;
import android.util.Log;
import dk.nindroid.rss.RiverRenderer;
import dk.nindroid.rss.TextureSelector;
import dk.nindroid.rss.data.ImageReference;
import dk.nindroid.rss.gfx.Vec3f;
import dk.nindroid.rss.renderers.ImagePlane;

public class Image implements ImagePlane {
	private static final int VERTS = 4;
	private int mTextureID;
	private FloatBuffer mTexBuffer;
	private Vec3f[]		mVertices;
	private IntBuffer   mVertexBuffer;
	private ByteBuffer  mIndexBuffer;
	private ImageReference mImage;
	private Bitmap 		mBitmap;
	private float 		mAspect = 1;
	private float 		mBitmapWidth;
	private float 		mBitmapHeight;
	private boolean		mHasBitmap;
	
	private Vec3f		mPos;
	
	public void init(GL10 gl, long time){
		int[] textures = new int[1];
		gl.glGenTextures(1, textures, 0);
		mTextureID = textures[0];
	}
	
	public void clear(){
		this.mBitmap = null;
		this.mImage = null;
		this.mBitmapHeight = 0;
		this.mBitmapWidth = 0;
	}

	public Image(){
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
	
	private boolean isTall(){
		boolean tall = mAspect < RiverRenderer.mDisplay.getWidth() / RiverRenderer.mDisplay.getFocusedHeight();
		return tall	;	
	}
	
	public void setImage(GL10 gl, ImageReference image){
		this.mHasBitmap = false; // No bitmap until it's large!
		this.mImage = image;
		this.mBitmap = image.getBitmap();
		this.mBitmapWidth = image.getWidth();
		this.mBitmapHeight = image.getHeight();
		setTexture(gl);
		TextureSelector.selectImage(this, image);
	}
	
	public ImageReference getImage(){
		return mImage;
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
	
	public void render(GL10 gl){
		if(this.mBitmap != null){
			setTexture(gl);
		}
		
		float x, y, z, szX, szY;
		x = mPos.getX();
		y = mPos.getY();
		z = mPos.getZ();
		if(isTall()){
			szY = RiverRenderer.mDisplay.getFocusedHeight() * RiverRenderer.mDisplay.getFill();
			szX = mAspect * szY;
		}else{
			szX = RiverRenderer.mDisplay.getWidth() * RiverRenderer.mDisplay.getFill();
			szY = szX / mAspect;
		}
		gl.glTexEnvx(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE, GL10.GL_REPLACE);
		gl.glFrontFace(GL10.GL_CCW);
		gl.glEnable(GL10.GL_TEXTURE_2D);
		
		gl.glActiveTexture(GL10.GL_TEXTURE0);
        gl.glBindTexture(GL10.GL_TEXTURE_2D, mTextureID);
        gl.glVertexPointer(3, GL10.GL_FIXED, 0, mVertexBuffer);
        
		gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, mTexBuffer);
		
		gl.glPushMatrix();
				
		gl.glTranslatef(x, y, z);
		gl.glScalef(szX, szY, 1);
		gl.glDrawElements(GL10.GL_TRIANGLE_STRIP, 4, GL10.GL_UNSIGNED_BYTE, mIndexBuffer);
		
		gl.glPopMatrix();
    }
	
	public void setTexture(GL10 gl) {
		if(mBitmap == null){ // TODO: Handle graciously
			return;
		}
		float width = mBitmapWidth;
		float height = mBitmapHeight;
		
		mAspect = width / height;
        
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
        	GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, mBitmap, 0);
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
        	Log.w("dk.nindroid.rss.renderers.SlideshowRenderer.Image", "Texture could not be set", e);
        }
        mBitmap = null;
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

	@Override
	public void setFocusTexture(Bitmap texture, float width, float height) {
		this.mBitmap = texture;
		this.mBitmapWidth = width;
		this.mBitmapHeight = height;
		this.mHasBitmap = true;
	}
}
