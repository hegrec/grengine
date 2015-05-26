package grengine.world;

import grengine.entity.Entity;
import grengine.physics.PhysicsCollider;
import grengine.physics.Vec3;

import java.util.ArrayList;

public class Bucket {

	private ArrayList<Entity> entities;
	private Bucket[] childBuckets;
	private int depth;
	private boolean split = false;
	public int nodeWidth;
	public Vec3 nodeMax;
	public Vec3 nodeMin;
	private int halfWidth;
	public Vec3 center;
	private Octree tree;
	public Bucket(Octree tree,int depth,Vec3 center) {
		this.depth = depth;
		this.center = center;
		this.tree = tree;
		this.tree.addBucket(this);
		this.nodeWidth = (int) (Octree.unitsize/Math.pow(2, this.depth));
		this.halfWidth = this.nodeWidth/2;
		this.nodeMax = center.add(new Vec3(this.halfWidth,this.halfWidth,this.halfWidth));
		this.nodeMin = center.subtract(new Vec3(this.halfWidth,this.halfWidth,this.halfWidth));
		setEntities(new ArrayList<Entity>());
		childBuckets = new Bucket[8];
	}
	public void addEntity(Entity ent) {
		if (this.split) {
			int cId = getChildBucket(ent.getPos());
			childBuckets[cId].addEntity(ent);
		}
		else if (getEntities().size() < Octree.MAX_OBJECTS_PER_BUCKET || depth == Octree.MAX_BUCKET_DEPTH)
			getEntities().add(ent);
		else if (!this.split)
			split(ent);	
	} 
	private void split(Entity splittingEnt) {
		//System.out.println("Splitting bucket at "+center);
		
		int xhalf = halfWidth/2;
		int yhalf = halfWidth/2;
		int zhalf = halfWidth/2;
		
		if (center.x<0)
			xhalf = -xhalf;
		if (center.y<0)
			yhalf = -yhalf;
		if (center.z<0)
			zhalf = -zhalf;
		
		
		
		//-X -Y -Z
		childBuckets[0] = new Bucket(this.tree,this.depth+1,center.add(new Vec3(-xhalf,-yhalf,-zhalf)));
		//-X -Y Z
		childBuckets[1] = new Bucket(this.tree,this.depth+1,center.add(new Vec3(-xhalf,-yhalf,zhalf)));
		//-X Y -Z
		childBuckets[2] = new Bucket(this.tree,this.depth+1,center.add(new Vec3(-xhalf,yhalf,-zhalf)));
		//-X Y Z
		childBuckets[3] = new Bucket(this.tree,this.depth+1,center.add(new Vec3(-xhalf,yhalf,zhalf)));
		//X -Y -Z
		childBuckets[4] = new Bucket(this.tree,this.depth+1,center.add(new Vec3(xhalf,-yhalf,-zhalf)));
		//X -Y Z
		childBuckets[5] = new Bucket(this.tree,this.depth+1,center.add(new Vec3(xhalf,-yhalf,zhalf)));
		//X Y -Z
		childBuckets[6] = new Bucket(this.tree,this.depth+1,center.add(new Vec3(xhalf,yhalf,-zhalf)));
		//X Y Z
		childBuckets[7] = new Bucket(this.tree,this.depth+1,center.add(new Vec3(xhalf,yhalf,zhalf)));
		
		
		for (int i=0;i<this.getEntities().size();i++)
		{
			Entity e = this.getEntities().get(i);
			Vec3 min1 = e.getPos().add(e.getAABBMin());
			Vec3 max1 = e.getPos().add(e.getAABBMax());
			for (int cID=0;cID<8;cID++) {

			Vec3 min2 = childBuckets[cID].nodeMin;
			Vec3 max2 = childBuckets[cID].nodeMax;
			float xOverlap = PhysicsCollider.OneDSAT(min1.x,max1.x,min2.x,max2.x);
			if (xOverlap == 0) continue;
			float yOverlap = PhysicsCollider.OneDSAT(min1.y,max1.y,min2.y,max2.y);
			if (yOverlap == 0) continue;
			float zOverlap = PhysicsCollider.OneDSAT(min1.z,max1.z,min2.z,max2.z);
			if (zOverlap == 0) continue;

			//System.out.println("Sending entity at "+e.getPos()+" to bucket at "+childBuckets[cID].center);
			childBuckets[cID].addEntity(e);
			}
		}
		this.split = true;
		this.getEntities().clear();
		int cId = getChildBucket(splittingEnt.getPos());
		childBuckets[cId].addEntity(splittingEnt);
		
	}
	public int getChildBucket(Vec3 vec) {
		
		Vec3 localVec = this.center.subtract(vec);
		
		if (localVec.x<0)
			if (localVec.y<0)
				if (localVec.z<0)
					return 0;
				else
					return 1;
			else
				if (localVec.z<0)
					return 2;
				else
					return 3;
		else
			if (localVec.y<0)
				if (localVec.z<0)
					return 4;
				else
					return 5;
			else
				if (localVec.z<0)
					return 6;
				else
					return 7;
	}
	
	public int getDepth() { return depth; }
	public ArrayList<Entity> searchEntities(Vec3 vecSearch) {

		 if (!split) return getEntities();
		 
		 
		 
		
		return null;
	}
	public ArrayList<Bucket> findBucketList(Vec3 max1, Vec3 min1) {

		ArrayList<Bucket> bucketList = new ArrayList<Bucket>();
		if (!this.split) {
			bucketList.add(this);
			return bucketList;
		}
		
		for (int i=0;i<8;i++)
		{
			Vec3 min2 = childBuckets[i].nodeMin;
			Vec3 max2 = childBuckets[i].nodeMax;
			float xOverlap = PhysicsCollider.OneDSAT(min1.x,max1.x,min2.x,max2.x);
			if (xOverlap == 0) {continue; }
			float yOverlap = PhysicsCollider.OneDSAT(min1.y,max1.y,min2.y,max2.y);
			if (yOverlap == 0) {continue; }
			float zOverlap = PhysicsCollider.OneDSAT(min1.z,max1.z,min2.z,max2.z);
			if (zOverlap == 0) {continue; } 
		
			bucketList.addAll(childBuckets[i].findBucketList(max1,min1));
		}

		return bucketList;
	}
	public void clear() {
		getEntities().clear();
		
	}
	public ArrayList<Entity> getEntities() {
		return entities;
	}
	public void setEntities(ArrayList<Entity> entities) {
		this.entities = entities;
	}
}
