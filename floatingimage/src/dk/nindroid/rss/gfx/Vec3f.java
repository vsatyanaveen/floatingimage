package dk.nindroid.rss.gfx;

public class Vec3f {
	float mX;
	float mY;
	float mZ;
	@Override
	public String toString() {
		return "(" + mX + "," + mY + "," + mZ + ")";
	}
	public Vec3f(float x, float y, float z){
		this.mX = x;
		this.mY = y;
		this.mZ = z;
	}
	public Vec3f(Vec3f vec){
		this.mX = vec.mX;
		this.mY = vec.mY;
		this.mZ = vec.mZ;
	}
	public Vec3f(){
	}
	
	public void set(Vec3f vec){
		mX = vec.mX;
		mY = vec.mY;
		mZ = vec.mZ;
	}
	public float getX() {
		return mX;
	}
	public void setX(float x) {
		this.mX = x;
	}
	public float getY() {
		return mY;
	}
	public void setY(float y) {
		this.mY = y;
	}
	public float getZ() {
		return mZ;
	}
	public void setZ(float z) {
		this.mZ = z;
	}
	public float dot(Vec3f vec){
		return mX * vec.mX + mY * vec.mY + mZ * vec.mZ;
	}
	public Vec3f cross(Vec3f vec){
		float x = mY * vec.mZ - mZ * vec.mY;
		float y = mZ * vec.mX - mX * vec.mZ;
		float z = mX * vec.mY - mY * vec.mX;
		return new Vec3f(x, y, z);
	}
	public Vec3f minus(Vec3f vec){
		return new Vec3f(mX - vec.mX, mY - vec.mY, mZ - vec.mZ);
	}
	public Vec3f plus(Vec3f vec){
		return new Vec3f(mX + vec.mX, mY + vec.mY, mZ + vec.mZ);
	}
	public float length(){
		return (float)Math.sqrt(dot(this));			
	}
	public void normalize(){
		float l = length();
		mX /= l;
		mY /= l;
		mZ /= l;
	}
}
