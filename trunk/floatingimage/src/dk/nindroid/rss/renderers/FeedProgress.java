package dk.nindroid.rss.renderers;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.microedition.khronos.opengles.GL10;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.opengl.GLUtils;
import android.util.Log;
import dk.nindroid.rss.R;
import dk.nindroid.rss.RiverRenderer;
import dk.nindroid.rss.ShowStreams;
import dk.nindroid.rss.gfx.Vec3f;

public class FeedProgress {
	private static final int 	one = 0x10000;
	private static Vec3f[]		mVertices;
	private static IntBuffer	mVertexBuffer;
	private static ByteBuffer	mIndexBuffer;
	private static int			mTextureID = -1;
	private static FloatBuffer 	mTexBuffer;
	Paint 	mPainter;
	String 	mLoadingText;
	int		mLoaded = -1;
	
	public static void init()
	{
		// Make sure a texture ID is generated, and a new texture is made.
		mTextureID = -1;
	}
	
	private static final int VERTS = 4;
	static {
    	
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
		ByteBuffer tbb = ByteBuffer.allocateDirect(VERTS * 2 * 4);
        tbb.order(ByteOrder.nativeOrder());
		mTexBuffer = tbb.asFloatBuffer();
		
		float tex[] = {
	        	0.0f,  0.0f,
	        	0.0f,  1.0f,	
	        	1.0f,  0.0f,
	        	1.0f,  1.0f,
	        };
        mTexBuffer.put(tex);
        mTexBuffer.position(0);
		
		ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length*4);
		vbb.order(ByteOrder.nativeOrder());
		mVertexBuffer = vbb.asIntBuffer();
		mVertexBuffer.put(vertices);
		mVertexBuffer.position(0);
		
		mIndexBuffer = ByteBuffer.allocateDirect(indices.length);
		mIndexBuffer.put(indices);
		mIndexBuffer.position(0);
	}
	
	public FeedProgress(){
		mPainter = new Paint();
		mPainter.setAntiAlias(true);
		mPainter.setTextSize(22);
		mLoadingText = ShowStreams.current.getString(R.string.loading_feeds);
	}
	
	public void draw(GL10 gl, int loaded, int total){
		if(loaded != total)
		{
			gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
			gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
			
			if(mTextureID == -1){
				int[] textures = new int[1];
				gl.glGenTextures(1, textures, 0);
				mTextureID = textures[0];
			}
			gl.glActiveTexture(GL10.GL_TEXTURE0);
			if(mLoaded != loaded){
				Bitmap bmp = Bitmap.createBitmap(256, 64, Bitmap.Config.ARGB_4444);
				String text = mLoadingText + " (" + loaded + "/" + total + ")";
				mPainter.setARGB(0xFF, 0xFF, 0xFF, 0xFF);
				Canvas canvas = new Canvas(bmp);
				canvas.drawText(text, 0, 20, mPainter);
				gl.glBindTexture(GL10.GL_TEXTURE_2D, mTextureID);
				gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
				gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
				setTexture(gl, bmp);
				mLoaded = loaded;
				bmp.recycle();
			}
			gl.glPushMatrix();
				gl.glEnable(GL10.GL_BLEND);
				float height = 64.0f / RiverRenderer.mDisplay.getHeightPixels() * RiverRenderer.mDisplay.getHeight();
				float width = 256.0f / RiverRenderer.mDisplay.getWidthPixels() * RiverRenderer.mDisplay.getWidth();
				gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S,GL10.GL_CLAMP_TO_EDGE);
		        gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
				gl.glTranslatef(-RiverRenderer.mDisplay.getWidth() + width, RiverRenderer.mDisplay.getHeight() - height, -1.0f);
				gl.glScalef(width, height, 1.0f);
				
		        gl.glEnable(GL10.GL_TEXTURE_2D);
				gl.glBlendFunc(GL10.GL_ONE_MINUS_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
				gl.glActiveTexture(GL10.GL_TEXTURE0);
				gl.glBindTexture(GL10.GL_TEXTURE_2D, mTextureID);
				gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, mTexBuffer);
				gl.glDrawElements(GL10.GL_TRIANGLE_STRIP, 4, GL10.GL_UNSIGNED_BYTE, mIndexBuffer);
			gl.glPopMatrix();
			gl.glDisable(GL10.GL_TEXTURE_2D);
		}	
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
        gl.glTexEnvx(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE, GL10.GL_REPLACE);
	}
}
