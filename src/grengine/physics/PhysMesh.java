package grengine.physics;

import grengine.entity.Entity;

public class PhysMesh {

	
	public Vec3 obbCentroid;
	public Vec3 obbExtent;
	public Vec3[] obbVec;
	
	public Vec3 nX;
	public Vec3 nY;
	public Vec3 nZ;
	
	public Vec3 localAABBMax;
	public Vec3 localAABBMin;
	public Entity physOwner;
	
	public PhysMesh(Entity entity) {
		this.physOwner = entity;
		obbVec = new Vec3[8];
		for (int i=0;i<obbVec.length;i++)
			obbVec[i] = new Vec3();
		
		nX = new Vec3(1,0,0);
		nY = new Vec3(0,1,0);
		nZ = new Vec3(0,0,1);
	}

	public Vec3 getAABBMax() {
		return localAABBMax;
	}

	public Vec3 getAABBMin() {
		return localAABBMin;
	}

	public void recompute(Matrix rot) {
		
		//start with centroid, add locations of each vertex rotated about an orientation
		
		//3 rotated points to form a normal vector of rotation to compute other points cheaply
		nX = rot.rotateVector(new Vec3(1,0,0)).normalize();
		nY = rot.rotateVector(new Vec3(0,1,0)).normalize();
		nZ = rot.rotateVector(new Vec3(0,0,1)).normalize();
		
		obbVec[0] = rot.rotateVector(obbCentroid.add(obbExtent));
		obbVec[1] = rot.rotateVector(obbCentroid.add(obbExtent.subtract(new Vec3(obbExtent.x*2,0,0))));
		obbVec[2] = rot.rotateVector(obbCentroid.add(obbExtent.subtract(new Vec3(obbExtent.x*2,0,obbExtent.z*2))));
		obbVec[3] = rot.rotateVector(obbCentroid.add(obbExtent.subtract(new Vec3(0,0,obbExtent.z*2))));
		obbVec[4] = rot.rotateVector(obbCentroid.add(obbExtent.subtract(new Vec3(0,obbExtent.y*2,0))));
		obbVec[5] = rot.rotateVector(obbCentroid.add(obbExtent.subtract(new Vec3(obbExtent.x*2,obbExtent.y*2,0))));
		obbVec[6] = rot.rotateVector(obbCentroid.add(obbExtent.subtract(new Vec3(obbExtent.x*2,obbExtent.y*2,obbExtent.z*2))));
		obbVec[7] = rot.rotateVector(obbCentroid.add(obbExtent.subtract(new Vec3(0,obbExtent.y*2,obbExtent.z*2))));
		
		float maxX = -99999;
		float maxY = -99999;
		float maxZ = -99999;
		float minX = 99999;
		float minY = 99999;
		float minZ = 99999;
		

		for (int i=0;i<obbVec.length;i++)
		{
			if (obbVec[i] == null) continue;
			
			
			
		if (obbVec[i].x > maxX) maxX = obbVec[i].x;
		if (obbVec[i].x < minX) minX = obbVec[i].x;
		
		if (obbVec[i].y > maxY) maxY = obbVec[i].y;
		if (obbVec[i].y < minY) minY = obbVec[i].y;
		
		if (obbVec[i].z > maxZ) maxZ = obbVec[i].z;
		if (obbVec[i].z < minZ) minZ = obbVec[i].z;
		
		}		
		Vec3 min = new Vec3(minX,minY,minZ);
		Vec3 max = new Vec3(maxX,maxY,maxZ);
			
		
		localAABBMax = max;
		localAABBMin = min;
		
		
		
		
		
	}
	public void build(float[] verts) {
		
		float maxX = -99999;
		float maxY = -99999;
		float maxZ = -99999;
		float minX = 99999;
		float minY = 99999;
		float minZ = 99999;
		
		
		for (int i=0;i<verts.length;i+=3) {
			float tx = verts[i];
			float ty = verts[i+1];
			float tz = verts[i+2];
			
			if (tx > maxX) maxX = tx;
			if (tx < minX) minX = tx;
			
			if (ty > maxY) maxY = ty;
			if (ty < minY) minY = ty;
			
			if (tz > maxZ) maxZ = tz;
			if (tz < minZ) minZ = tz;
		}
		
		
		Vec3 min = new Vec3(minX,minY,minZ);
		Vec3 max = new Vec3(maxX,maxY,maxZ);
		
		
		localAABBMax = max;
		localAABBMin = min;
		obbCentroid = localAABBMax.add(localAABBMin).scale(0.5f);
		obbExtent = localAABBMax.subtract(localAABBMin).scale(0.5f);
		recompute(physOwner.getPhysicsState().orientation.getMatrix());
		
	}

	public Vec3 getWorldCenter() {
		// TODO Auto-generated method stub
		return physOwner.getPos().add(obbCentroid);
	}

}
