package dk.nindroid.rss;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

import javax.microedition.khronos.opengles.GL10;

import android.graphics.Paint;
import dk.nindroid.rss.data.ImageReference;
import dk.nindroid.rss.gfx.Vec3f;
import dk.nindroid.rss.helpers.LabelMaker;

public class InfoBar {
	private static final int 	one = 0x10000;
	private static Vec3f[]		mVertices;
	private static IntBuffer	mVertexBuffer;
	private static ByteBuffer	mIndexBuffer;
	private static IntBuffer	mColorBuffer;
	static private LabelMaker 	mInfo;
	static private Paint 		mInfoPaint;
	static private int 			mInfoTitle;
	static private int 			mInfoTitle2;
	static private int 			mInfoAuthor;
	static private int			mWidth;
	static private int			mHeight;
	
	private static final int VERTS = 4;
	static {
		mInfoPaint = new Paint();
		mInfoPaint.setTextSize(22);
        mInfoPaint.setAntiAlias(true);
        mInfoPaint.setARGB(0xff, 0xF0, 0xF0, 0xF0);
        
		ByteBuffer tbb = ByteBuffer.allocateDirect(VERTS * 2 * 4);
        tbb.order(ByteOrder.nativeOrder());
    	
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
		
		int[] colors  = { 
				0, 0, 0, one / 2,
				0, 0, 0, one / 2,
				0, 0, 0, one / 2,
				0, 0, 0, one / 2
				};
		ByteBuffer cbb = ByteBuffer.allocateDirect(64);
		cbb.order(ByteOrder.nativeOrder());
		mColorBuffer = cbb.asIntBuffer();
		mColorBuffer.put(colors);
		mColorBuffer.position(0);
		
		mIndexBuffer = ByteBuffer.allocateDirect(indices.length);
		mIndexBuffer.put(indices);
		mIndexBuffer.position(0);
		
	}
	
	public static void select(GL10 gl, ImageReference ir){
		if(ir == null) return;
		String title = ir.getTitle();
		String author = ir.getAuthor();
		
		mInfoTitle2 = -1;
		mInfoAuthor = -1;
		if (mInfo != null) {
            mInfo.shutdown(gl);
        } else {
            mInfo = new LabelMaker(true, 512, 128);
        }
		mInfo.initialize(gl);
		mInfo.beginAdding(gl);
		if(title.length() > 25);
		int newline = title.indexOf(" ", 20);
		if(newline != -1){
			mInfoTitle = mInfo.add(gl, title.substring(0, newline), mInfoPaint);
			String title2 = title.substring(newline);
			if(title2.length() > 30){
				title2 = title2.substring(0, 28) + "...";
			}
			mInfoTitle2 = mInfo.add(gl, title2, mInfoPaint);
		}else{
			mInfoTitle = mInfo.add(gl, title, mInfoPaint);
		}
		
		if(author != null){
			mInfoPaint.setTextSize(18);
	        mInfoPaint.setARGB(0xff, 0xC0, 0xC0, 0xC0);
	        if(author.length() < 35){
	        	mInfoAuthor = mInfo.add(gl, author, mInfoPaint);
	        }else{
	        	mInfoAuthor = mInfo.add(gl, author.substring(0, 32) + "...", mInfoPaint);
	        }
			mInfoPaint.setTextSize(22);
	        mInfoPaint.setARGB(0xff, 0xF0, 0xF0, 0xF0);
		}
		
		mInfo.endAdding(gl);
		
	}
	
	public static void setView(int width, int height){
		mWidth = width;
		mHeight = height;	
	}
	
	public static void draw(GL10 gl){
		//Set the face rotation
		gl.glFrontFace(GL10.GL_CCW);
        gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
		gl.glDisable(GL10.GL_TEXTURE_2D);
		//Point to our vertex buffer
		gl.glVertexPointer(3, GL10.GL_FIXED, 0, mVertexBuffer);
		gl.glColorPointer(4, GL10.GL_FIXED, 0, mColorBuffer);
				
		gl.glPushMatrix();
		gl.glScalef(1.0f, 0.5f, 1.0f);
		gl.glTranslatef(0.0f, -2.9f, -0.5f);
		
		//Draw the vertices as triangle strip
		gl.glDrawElements(GL10.GL_TRIANGLE_STRIP, 4, GL10.GL_UNSIGNED_BYTE, mIndexBuffer);
		
		gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
		
		mInfo.beginDrawing(gl, mWidth, mHeight);
		
		mInfo.draw(gl, mWidth / 2 - mInfo.getWidth(mInfoTitle) / 2, 52, mInfoTitle);
		if(mInfoTitle2 != -1){
			mInfo.draw(gl, mWidth / 2 - mInfo.getWidth(mInfoTitle2) / 2, 30, mInfoTitle2);
		}
		if(mInfoAuthor != -1){
			mInfo.draw(gl, mWidth - mInfo.getWidth(mInfoAuthor) - 5, 5, mInfoAuthor);
		}
		mInfo.endDrawing(gl);
		
		gl.glPopMatrix();
		
		//Disable the client state before leaving
		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
	}
}
