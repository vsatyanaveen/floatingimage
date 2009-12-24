package dk.nindroid.rss;

import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.opengles.GL10;

import dk.nindroid.rss.gfx.Vec3f;

public class BackgroundPainter {
	List<BackgroundImage> objs;
	private final static float SPEED = .0005f;
	private final static float ZDEPTHSPAN = 7.0f;
	private final static float ZDEPTH = -8.0f;
	private final static int ROWS   = 5;
	private final static int COLS	= 4;
	
	
	public BackgroundPainter(long startTime){
		objs = new ArrayList<BackgroundImage>(ROWS*COLS);
		
		float maxDepth = ZDEPTHSPAN - ZDEPTH;
		maxDepth = -ZDEPTH;
		
		
		float rowSpacing = maxDepth * 2.8f / ROWS;
		float colSpacing = maxDepth / COLS  * 2.2f; // Get real values somehow please!!
		
		float rowBottom = rowSpacing * ROWS / 2.0f;
		float colRight = colSpacing * COLS / 2.0f;
		
		for(int row = 0; row < ROWS; ++row){
			for(int col = 0; col < COLS; ++col){
				//if((row + col) % 2 == 0){
					objs.add(new BackgroundImage(SPEED, new Vec3f(1.5f, 1.5f, ZDEPTHSPAN), new Vec3f(col * colSpacing - colRight, row * rowSpacing - rowBottom, ZDEPTH), startTime, colRight));
				//}
			}
		}
	}
	
	public void draw(GL10 gl, long offset){
		gl.glDisable(GL10.GL_TEXTURE_2D);
		gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
		for(BackgroundImage img : objs){
			img.draw(gl, offset);
		}
		gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
	}
}
