package grengine.world;

import java.util.ArrayList;

import grengine.entity.Entity;
import grengine.manager.EntityManager;
import grengine.physics.Vec3;

public class Octree {
	protected static int MAX_BUCKET_DEPTH = 5;
	public static Octree MAIN_TREE;
	protected static int MAX_OBJECTS_PER_BUCKET = 5; //any more and this forces a subdivide if possible
	protected static int unitsize = (int) WorldMap.getMax().subtract(WorldMap.getMin()).magnitude();
	private Bucket rootBucket;
	public ArrayList<Bucket> masterBuckets;
	public Octree() {
		int powerOfTwoSize = 0;
		for (int pow=1;pow < 20;pow++)
			if ((powerOfTwoSize = (int) Math.pow(2, pow)) > unitsize)
				break;
		
		MAIN_TREE = this;
		System.out.println(unitsize);
		unitsize = powerOfTwoSize;
		MAX_BUCKET_DEPTH = (int) (Math.log10(unitsize)/Math.log10(2))-1;
		System.out.println(unitsize);
		masterBuckets = new ArrayList<Bucket>();
		Vec3 centerPos = new Vec3(0,0,0);
		rootBucket = new Bucket(this,0,centerPos);
	}
	
	
	public void queryScene() {
		//clear the buckets
		for (int i=0;i<masterBuckets.size();i++)
			masterBuckets.get(i).clear(); 
	
		Entity[] ents = EntityManager.getAll();
		for (int i=0;i<ents.length;i++) {
			Entity ent = ents[i];
			if (ent == null) continue;
			//we find a list of buckets this entity intersects
			ArrayList<Bucket> newBuckets = rootBucket.findBucketList(ent.getPos().add(ent.getAABBMax()),ent.getPos().add(ent.getAABBMin()));
			
			for (int bk=0;bk<newBuckets.size();bk++)
				newBuckets.get(bk).addEntity(ent);
		}
	}


	public ArrayList<Entity> searchEntities(Vec3 min,Vec3 max) {
		
	
		
		ArrayList<Bucket> newBuckets = rootBucket.findBucketList(max,min);
		ArrayList<Entity> ents = new ArrayList<Entity>();

		Entity[] ents2 = EntityManager.getAll();
		for (int i=0;i<ents2.length;i++)
		{
			ents.add(ents2[i]);
		}
		
		if (true) return ents;

		for (int i=0;i<newBuckets.size();i++)
		{
			for (int ind=0;ind<newBuckets.get(i).getEntities().size();ind++) {
				ents.add(newBuckets.get(i).getEntities().get(ind));
			}
		}
		return ents;
	}


	public void addBucket(Bucket bucket) {
		masterBuckets.add(bucket);
		
	}
	
}