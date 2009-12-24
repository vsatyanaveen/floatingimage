package dk.nindroid.rss;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.Date;
import java.util.Random;

import javax.microedition.khronos.opengles.GL10;

import dk.nindroid.rss.gfx.Vec3f;

public class BackgroundImage {
	private Random		mRand;
	private IntBuffer   mVertexBuffer;
	private IntBuffer   mColorBuffer;
	private ByteBuffer  mIndexBuffer;
	private Vec3f		mInitPos;
	private Vec3f		mPos;
	private final long  mStartTime;
	private Vec3f		mJitter = new Vec3f(0.0f, 0.0f, 0.0f);
	private Vec3f 		mJitterSpan;
	private float		mFarRight;
	private final float	mSpeed;
	private float		mSizeX;
	private float		mSizeY;
	//private float		mBrightness;
	
	public BackgroundImage(float speed, Vec3f jitter, Vec3f initPos, long startTime, float farRight){
		this.mJitterSpan = jitter;
		mStartTime = startTime;
		mSpeed = speed;
		mRand = new Random(new Date().getTime());
		this.mFarRight = farRight;
		mInitPos = initPos;
		mPos = new Vec3f(initPos.getX(), initPos.getY(), initPos.getZ());
		
		jitter();
		mSizeX = mRand.nextFloat() + 0.5f;
		mSizeY = mRand.nextFloat() + 0.5f;
		float brightness = mJitter.getZ() / jitter.getZ() / 20.0f + 0.07f;
		
		ByteBuffer tbb = ByteBuffer.allocateDirect(4 * 2 * 4);
        tbb.order(ByteOrder.nativeOrder());
        
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
		
		int brightnessInt = (int)(one * brightness);
		int colors[] = {
				brightnessInt, brightnessInt, brightnessInt, one,
				brightnessInt, brightnessInt, brightnessInt, one,
				brightnessInt, brightnessInt, brightnessInt, one,
				brightnessInt, brightnessInt, brightnessInt, one
		};
		
		ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length*4);
		vbb.order(ByteOrder.nativeOrder());
		mVertexBuffer = vbb.asIntBuffer();
		mVertexBuffer.put(vertices);
		mVertexBuffer.position(0);
		
		mIndexBuffer = ByteBuffer.allocateDirect(indices.length);
		mIndexBuffer.put(indices);
		mIndexBuffer.position(0);
		
		ByteBuffer cbb = ByteBuffer.allocateDirect(colors.length*4);
		cbb.order(ByteOrder.nativeOrder());
		mColorBuffer = cbb.asIntBuffer();
		mColorBuffer.put(colors);
		mColorBuffer.position(0);
	}
	
	public void draw(GL10 gl, long offset){
		update(offset);
		
		float x, y, z;
		x = mPos.getX();// + mJitter.getX();
		y = mPos.getY();// + mJitter.getY();
		z = mPos.getZ();// + mJitter.getZ();
		
		gl.glFrontFace(GL10.GL_CCW);
		gl.glVertexPointer(3, GL10.GL_FIXED, 0, mVertexBuffer);		
		gl.glColorPointer(4, GL10.GL_FIXED, 0, mColorBuffer);
		
		gl.glPushMatrix();
		gl.glTranslatef(x, y, z);
		//gl.glScalef(mSizeX, mSizeY, 1);
		gl.glScalef(2.0f, 2.0f, 1);
		
		gl.glDrawElements(GL10.GL_TRIANGLE_STRIP, 4, GL10.GL_UNSIGNED_BYTE, mIndexBuffer);
        
        gl.glPopMatrix();
	}
	
	private void update(long offset){
		long time = new Date().getTime();
		long curTime = time - mStartTime + offset;
		float totalPos = curTime * mSpeed + mInitPos.getX();
		float nowPos = (totalPos % (2 * mFarRight)) - mFarRight;
		
		
		this.mPos.setX(nowPos);
	}
	
	private void jitter(){
		mJitter.setX(mRand.nextFloat() * mJitterSpan.getX() * 2 - mJitterSpan.getX());
		mJitter.setY(mRand.nextFloat() * mJitterSpan.getY() * 2 - mJitterSpan.getY());
		mJitter.setZ(mRand.nextFloat() * mJitterSpan.getZ() * 2 - mJitterSpan.getZ());
	}
}
