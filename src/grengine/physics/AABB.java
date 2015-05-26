package grengine.physics;

import grengine.entity.Entity;

public class AABB {

	private Entity ownerEnt; //this AABB is attached to the entity
	private Vec3 localMax;
	private Vec3 localMin;
	public AABB(Vec3 lMax,Vec3 lMin, Entity ownerEnt) {
		this.localMax = lMax;
		this.localMin = lMin;
		this.ownerEnt = ownerEnt;
	}
	
	
}
