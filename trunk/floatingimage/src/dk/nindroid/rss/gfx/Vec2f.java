package dk.nindroid.rss.gfx;

public class Vec2f {
	float x;
	float y;
	public Vec2f(float x, float y){
		this.x = x;
		this.y = y;
	}
	public Vec2f(Vec2f vec){
		this.x = vec.x;
		this.y = vec.y;
	}
	public Vec2f(){
	}
	public float getX() {
		return x;
	}
	public void setX(float x) {
		this.x = x;
	}
	public float getY() {
		return y;
	}
	public void setY(float y) {
		this.y = y;
	}
	public void set(float x, float y){
		this.x = x;
		this.y = y;
	}
	public void set(Vec2f vec){
		this.x = vec.x;
		this.y = vec.y;
	}
	@Override
	public String toString() {
		return x + "x" + y;
	}
	
	public static float dot(float ax, float ay, float bx, float by){
		// Normalize
		return ax * bx + ay * by;
	}
}
