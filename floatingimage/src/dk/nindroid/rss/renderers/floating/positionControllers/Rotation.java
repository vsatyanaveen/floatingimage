package dk.nindroid.rss.renderers.floating.positionControllers;


public class Rotation {
	float angle;
	float x;
	float y;
	float z;
	public Rotation(float angle, float x, float y, float z) {
		super();
		this.angle = angle;
		this.x = x;
		this.y = y;
		this.z = z;
	}
	public void set(Rotation b){
		this.angle = b.angle;
		this.x = b.x;
		this.y = b.y;
		this.z = b.z;
	}
	public float getAngle() {
		return angle;
	}
	public void setAngle(float angle) {
		this.angle = angle;
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
	public float getZ() {
		return z;
	}
	public void setZ(float z) {
		this.z = z;
	}
}
