package dk.nindroid.rss.renderers.floating;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.microedition.khronos.opengles.GL10;

import android.graphics.Bitmap;
import android.opengl.GLUtils;
import dk.nindroid.rss.Display;
import dk.nindroid.rss.data.ImageReference;
import dk.nindroid.rss.gfx.Vec3f;

public class InfoBar {
	private static final int 	one = 0x10000;
	private Vec3f[]		mVertices;
	private IntBuffer	mVertexBuffer;
	private ByteBuffer	mIndexBuffer;
	private IntBuffer	mColorBuffer;
	private final InfoPainter	mInfoPainter;
	private int			mTextureID = -1;
	private FloatBuffer 	mTexBuffer;
	private int 			mLastDisplayWidth = 0;
	
	private static final int VERTS = 4;
	public InfoBar(){
	    mInfoPainter = new InfoPainter(22, 18);
    	
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
	
	private void updateTextureCoords(GL10 gl, Display display) {
		if(mLastDisplayWidth == display.getWidthPixels()) return;
		mLastDisplayWidth = display.getWidthPixels();
		ByteBuffer tbb = ByteBuffer.allocateDirect(VERTS * 2 * 4);
        tbb.order(ByteOrder.nativeOrder());
        mTexBuffer = tbb.asFloatBuffer();
        float ratioX = display.getWidthPixels() / 1024.0f;
        float ratioY = 80.0f / 128.0f;
        
        float tex[] = {
        	0.0f,  0.0f,
        	0.0f,  ratioY,	
        	ratioX,  0.0f,
        	ratioX,  ratioY,
        };
        mTexBuffer.put(tex);
        mTexBuffer.position(0);
        mInfoPainter.paintCanvas(Math.min(display.getWidthPixels(), 1024), 80);
		gl.glBindTexture(GL10.GL_TEXTURE_2D, mTextureID);
        setTexture(gl, mInfoPainter.getBitmap());
	}
	
	public void select(GL10 gl, Display display, ImageReference ir){
		if(mTextureID == -1){
			int[] textures = new int[1];
			gl.glGenTextures(1, textures, 0);
			mTextureID = textures[0];
		}		
		mInfoPainter.setInfo(ir.getTitle(), ir.getAuthor(), 1024, 128);
		mInfoPainter.paintCanvas(Math.min(display.getWidthPixels(), 1024), 80);
		gl.glBindTexture(GL10.GL_TEXTURE_2D, mTextureID);
        setTexture(gl, mInfoPainter.getBitmap());
	}
	
	protected void setTexture(GL10 gl, Bitmap bmp){		
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
        
        GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bmp, 0);
        bmp.recycle();
        gl.glTexEnvx(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE, GL10.GL_REPLACE);
	}
	
	void setAlpha(float col){
		int alpha = (int)(one * col);
		int[] colors  = { 
				0, 0, 0, alpha,
				0, 0, 0, alpha,
				0, 0, 0, alpha,
				0, 0, 0, alpha
				};
		ByteBuffer cbb = ByteBuffer.allocateDirect(64);
		cbb.order(ByteOrder.nativeOrder());
		mColorBuffer = cbb.asIntBuffer();
		mColorBuffer.put(colors);
		mColorBuffer.position(0);
	}
	
	public void setState(GL10 gl){
		gl.glFrontFace(GL10.GL_CCW);
		gl.glDisable(GL10.GL_TEXTURE_2D);	
	}
	
	public void unsetState(GL10 gl){
	}
	
	public void draw(GL10 gl, Display display, float fraction){
		updateTextureCoords(gl, display);
		gl.glEnable(GL10.GL_BLEND);
		gl.glPushMatrix();
			float height = display.getInfoBarHeight() / display.getHeightPixels() * display.getHeight();
			gl.glTranslatef(0.0f, -display.getHeight() + height, -1.0f);
			gl.glScalef(display.getWidth(), height, 1.0f);
			gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
			gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
			drawBlackBar(gl, fraction);
			gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
			gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
			drawInfo(gl);
			
		gl.glPopMatrix();
		gl.glDisable(GL10.GL_BLEND);
	}
	
	protected void drawBlackBar(GL10 gl, float fraction){
		setAlpha(1.0f - fraction * 0.25f);
		
		//Set the face rotation
		
		//Point to our vertex buffer
		gl.glVertexPointer(3, GL10.GL_FIXED, 0, mVertexBuffer);
		gl.glColorPointer(4, GL10.GL_FIXED, 0, mColorBuffer);
		//gl.glScalef(RiverRenderer.mScreenWidth, 0.05f, 1.0f);
		
		
		//Draw the vertices as triangle strip
		gl.glBlendFunc(GL10.GL_ZERO, GL10.GL_SRC_ALPHA);
		
		gl.glDrawElements(GL10.GL_TRIANGLE_STRIP, 4, GL10.GL_UNSIGNED_BYTE, mIndexBuffer);
		
	}
	
	protected void drawInfo(GL10 gl){
        gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S,GL10.GL_REPEAT);
        gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_REPEAT);
		gl.glEnable(GL10.GL_TEXTURE_2D);
		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE);
		gl.glActiveTexture(GL10.GL_TEXTURE0);
		gl.glBindTexture(GL10.GL_TEXTURE_2D, mTextureID);
		gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, mTexBuffer);
		gl.glDrawElements(GL10.GL_TRIANGLE_STRIP, 4, GL10.GL_UNSIGNED_BYTE, mIndexBuffer);
	}
}
