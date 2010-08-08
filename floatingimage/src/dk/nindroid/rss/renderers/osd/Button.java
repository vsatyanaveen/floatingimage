package dk.nindroid.rss.renderers.osd;

import javax.microedition.khronos.opengles.GL10;

import android.graphics.Bitmap;
import android.opengl.GLUtils;
import android.util.Log;

public abstract class Button {
	public abstract void click(long time);
	public abstract int getTextureID();
	public abstract void init(GL10 gl);
	
	protected void setTexture(GL10 gl, Bitmap bmp, int textureID){
        gl.glBindTexture(GL10.GL_TEXTURE_2D, textureID);
        try{
        	gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
            gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
            gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
            gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
            gl.glTexEnvf(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE, GL10.GL_BLEND);
            
        	GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bmp, 0);
        }catch(IllegalArgumentException e){ // TODO: Handle graciously
        	Log.w("Floating Image", "Could not set OSD texture", e);
        }
        //bmp.recycle(); // <--- This causes havoc if active!
	}
}
