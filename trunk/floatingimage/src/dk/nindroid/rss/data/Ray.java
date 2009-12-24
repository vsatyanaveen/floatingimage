package dk.nindroid.rss.data;

import dk.nindroid.rss.gfx.Vec3f;

public class Ray {
	private Vec3f o;
	private Vec3f d;
	public Ray(Vec3f o, Vec3f d){
		this.o = o;
		this.d = d;
	}
	public Vec3f getO(){
		return o;
	}
	public Vec3f getD(){
		return d;
	}
}
