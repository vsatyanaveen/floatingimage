package dk.nindroid.rss.data;

import javax.microedition.khronos.opengles.GL10;

public class Texture {
	private int	textureID = -1;
	private int	textureSize = 0;
	private boolean	revivingTextureNulled = false;
	
	public void nullTexture(){
		this.textureID = -1;
		this.textureSize = 0;
		this.revivingTextureNulled = true;
	}
	public int getTextureID(GL10 gl) {
		if(textureID == -1){
			int[] textures = new int[1];
			gl.glGenTextures(1, textures, 0);
			textureID = textures[0];
		}
		return textureID;
	}
	public int getTextureSize() {
		return textureSize;
	}
	public void setTextureSize(int largeTextureSize) {
		this.textureSize = largeTextureSize;
	}
	public boolean isRevivingTextureNulled() {
		return revivingTextureNulled;
	}
	public void setRevivingTextureNulled(boolean revivingTextureNulled) {
		this.revivingTextureNulled = revivingTextureNulled;
	}
}
