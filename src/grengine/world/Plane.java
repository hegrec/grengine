package grengine.world;

import grengine.physics.Vec3;

//A plane is defined by nX + nY + nZ + distToOrigin = 0
public class Plane {

	public Vec3 normal;
	public float distToOrigin;
	public Plane(Vec3 normal,float distToOrigin) {
		this.normal = normal;
		this.distToOrigin = distToOrigin;
	}
}
