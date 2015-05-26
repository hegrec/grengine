package grengine.physics;

import grengine.entity.Entity;
import grengine.world.Plane;
import grengine.world.WorldMap;
import grengine.world.WorldQuad;

import java.util.ArrayList;

public class PhysicsCollider {
	public static int numLoops = 0;
	public static boolean colls = false;

	protected static void collisionCheck(Entity[] ents) {

		// ArrayList<WorldEntityPair> wPairs = getWorldPairs(ents);
		for (int i = 0; i < ents.length; i++) {
			if (ents[i] == null)
				continue;
			worldCollide(ents[i]);
		}

		/*
		 * ArrayList<CollisionPair> cPairs = getCollisionPairs();
		 * 
		 * 
		 * for (int i=0;i<ents.length;i++) { if (ents[i] == null) continue;
		 * entityCollide(ents[i]); }
		 */

	}

	private static ArrayList<WorldEntityPair> getWorldPairs(Entity[] testEnts) {

		ArrayList<WorldEntityPair> wPairs = new ArrayList<WorldEntityPair>();
		for (int index = 0; index < testEnts.length; index++) {

			Entity ent = testEnts[index];
			if (ent == null)
				continue;
			// TODO: research whether or not sphere testing is more efficient.
			// First we test the AABB overlap
			Vec3 finalPos = ent.getPos();
			float epsilon = 0.01f; // keep us out of objects and the world
			Vec3 push = new Vec3(0, 0, 0);
			Vec3 eAABBMax = ent.getPos().add(ent.getAABBMax());
			Vec3 eAABBMin = ent.getPos().add(ent.getAABBMin());
			for (int worldFaceIndex = 0; worldFaceIndex < WorldMap.worldData.length; worldFaceIndex++) {

				WorldQuad q = WorldMap.worldData[index];
				Vec3 backwardFill = q.plane.normal.scale(-999); // this would be
																// the block bit
																// that sticks
																// into the void

				Vec3 min1 = q.min.copy();
				Vec3 max1 = q.max.copy();

				if (backwardFill.x > 0)
					max1.x += backwardFill.x;
				else
					min1.x += backwardFill.x;

				if (backwardFill.y > 0)
					max1.y += backwardFill.y;
				else
					min1.y += backwardFill.y;

				if (backwardFill.z > 0)
					max1.z += backwardFill.z;
				else
					min1.z += backwardFill.z;

				Vec3 tPush = AABB_CollisionTest(eAABBMin, eAABBMax, min1, max1);
				if (tPush.compareTo(Vec3.origin))
					continue;
				// System.out.println("TPUSH: "+min2+" "+max2+" "+tPush);
				// narrow_collision(ent.getMesh(),q);

				Vec3 posNorm = q.plane.normal.copy();
				if (posNorm.x < 0)
					posNorm.x *= -1;
				if (posNorm.y < 0)
					posNorm.y *= -1;
				if (posNorm.z < 0)
					posNorm.z *= -1;

				push = push.add(tPush.mult(posNorm));
				if (push.y > 0) {
					ent.isOnGround = true;
				}
				finalPos = finalPos.add(tPush.mult(posNorm));

				Vec3 m = ent.getPhysicsState().momentum;
				Vec3 safeM = ent.getVelocity().scale(ent.getMass()); // this
																		// will
																		// work
																		// for
																		// PHYS_WALKS

				float normalMag = q.plane.normal.DotProduct(safeM);

				m.x = (1 - Math.abs(q.plane.normal.x)) * m.x;
				m.y = (1 - Math.abs(q.plane.normal.y)) * m.y;
				m.z = (1 - Math.abs(q.plane.normal.z)) * m.z;

				float frictionCoefficient = 0.95f;
				Vec3 frictionalForce = new Vec3(0, 0, 0);
				frictionalForce = m.scale(-frictionCoefficient);
				ent.applyImpulse(frictionalForce);
			}
			ent.setPos(finalPos);
		}

		return wPairs;
	}

	private static void worldCollide(Entity ent) {

		Vec3 start = ent.lastState.position;
		Vec3 end = ent.getPos();

		// if (start.compareTo(end)) return; //dont worry about non moving
		// entities
		// the moving ones will let them know they were hit.
		// System.out.println("THE DIFF! "+start+" "+(end));
		Vec3 finalPos = end;
		float epsilon = 0.01f; // keep us out of objects and the world
		Vec3 push = new Vec3(0, 0, 0);

		Vec3 min = finalPos.add(ent.getAABBMin());
		Vec3 max = finalPos.add(ent.getAABBMax());

		for (int index = 0; index < WorldMap.worldData.length; index++) {

			WorldQuad q = WorldMap.worldData[index];

			Plane p = q.plane;

			Vec3 min1 = q.min.copy();
			Vec3 max1 = q.max.copy();

			Vec3 tPush = AABB_CollisionTest(min, max, min1, max1);

			if (tPush.compareTo(Vec3.origin))
				continue; // broad phase, are we touching the box created by the
							// potentially rotated quad?
			// System.out.println("TPUSH: "+min1+" "+max1);

			// if so project our deepest point onto the plane's normal
			Vec3 deepestLocal = null;
			Vec3 deepestAbsolute = null;
			float deepestPenetration = 0;
			boolean foundSplit = false;
			for (int vI = 0; vI < ent.getMesh().obbVec.length; vI++) {
				Vec3 vertPos = ent.getPos().add(ent.getMesh().obbVec[vI]);
				vertPos = vertPos.subtract(p.normal.scale(p.distToOrigin));
				float dist = p.normal.DotProduct(vertPos);
				// System.out.println(q.centerPoint+" "+dist+" "+p.distToOrigin+" "+vI);
				if (dist < deepestPenetration) {

					deepestLocal = ent.getMesh().obbVec[vI];
					deepestAbsolute = ent.getPos().add(deepestLocal);
					deepestPenetration = dist;
				} else // we know at least one point is above the plane, we're
						// split between the plane
				{
					foundSplit = true;
				}

			}

			if (deepestLocal != null) // we penetrated the infinite plane, make
										// sure the projection is within the
										// polygon
			{

				Vec3 projected = deepestAbsolute.add(p.normal.scale(Math
						.abs(deepestPenetration)));
				// System.out.println(q.centerPoint+" "+q.plane.normal+" "+q.plane.distToOrigin+" "+projected);
				if (pointInPoly(projected, q.point1, q.point2, q.point3,
						q.point4)) {

					if (q.plane.normal.y > 0.5)
						ent.isOnGround = true;
					// System.out.println(p.normal+" "+q.centerPoint);
					projected = projected.subtract(deepestLocal);
					// System.out.println(projected+" "+deepestPenetration);
					ent.setPos(projected);
					Vec3 m = ent.getPhysicsState().momentum;
					m.x = (1 - Math.abs(q.plane.normal.x)) * m.x;
					m.y = (1 - Math.abs(q.plane.normal.y)) * m.y;
					m.z = (1 - Math.abs(q.plane.normal.z)) * m.z;
				}

			}

			/*
			 * Vec3 posNorm = q.plane.normal.copy(); if (posNorm.x < 0)
			 * posNorm.x *= -1; if (posNorm.y < 0) posNorm.y *= -1; if
			 * (posNorm.z < 0) posNorm.z *= -1;
			 * 
			 * 
			 * push = push.add(tPush.mult(posNorm)); if (push.y > 0) {
			 * ent.isOnGround = true; } finalPos =
			 * finalPos.add(tPush.mult(posNorm));
			 * 
			 * 
			 * 
			 * 
			 * 
			 * 
			 * 
			 * Vec3 safeM = ent.getVelocity().scale(ent.getMass()); //this will
			 * work for PHYS_WALKS
			 * 
			 * float normalMag = q.plane.normal.DotProduct(safeM);
			 * 
			 * m.x = (1-Math.abs(q.plane.normal.x))*m.x; m.y =
			 * (1-Math.abs(q.plane.normal.y))*m.y; m.z =
			 * (1-Math.abs(q.plane.normal.z))*m.z;
			 * 
			 * float frictionCoefficient = 0.95f; Vec3 frictionalForce = new
			 * Vec3(0,0,0); frictionalForce = m.scale(-frictionCoefficient);
			 * ent.applyImpulse(frictionalForce);
			 */
		}
		// ent.setPos(finalPos);
	}

	private static boolean pointInPoly(Vec3 pt, Vec3 point1, Vec3 point2,
			Vec3 point3, Vec3 point4) {
		float radSum = 0f;
		Vec3 d1 = point1.subtract(pt).normalize();
		Vec3 d2 = point2.subtract(pt).normalize();
		Vec3 d3 = point3.subtract(pt).normalize();
		Vec3 d4 = point4.subtract(pt).normalize();

		radSum += Math.acos(d1.DotProduct(d2));
		radSum += Math.acos(d2.DotProduct(d3));
		radSum += Math.acos(d3.DotProduct(d4));
		radSum += Math.acos(d4.DotProduct(d1));
		if (Math.abs(radSum - (2 * Math.PI)) < 0.1f) { // we hit the bounded
														// quad
			return true;
		}
		return false;
	}

	public static Vec3 AABB_CollisionTest(Vec3 min1, Vec3 max1, Vec3 min2,
			Vec3 max2) {

		float xOverlap = PhysicsCollider
				.OneDSAT(min1.x, max1.x, min2.x, max2.x);
		if (xOverlap == 0)
			return Vec3.origin;
		float yOverlap = PhysicsCollider
				.OneDSAT(min1.y, max1.y, min2.y, max2.y);

		if (yOverlap == 0)
			return Vec3.origin;
		float zOverlap = PhysicsCollider
				.OneDSAT(min1.z, max1.z, min2.z, max2.z);
		if (zOverlap == 0)
			return Vec3.origin;

		Vec3 tPush = new Vec3(xOverlap, yOverlap, zOverlap);

		return tPush;

	}

	/**
	 * This returns the minimum distance to get interval [min,max] outside of
	 * the interval[min2,max2];
	 * 
	 * @param min
	 * @param max
	 * @param min2
	 * @param max2
	 * @return
	 */
	public static float OneDSAT(float min, float max, float min2, float max2) {
		if (min > max2)
			return 0;
		if (max < min2)
			return 0;
		if (max >= max2) // hanging off the right, move by max2-min
			return max2 - min;
		else if (min <= min2) // hanging off the right
			return min2 - max;
		else if (min > min2 && max < max2) {// contained within

			float leftDist = min - min2;
			float rightDist = max2 - max;
			if (leftDist < rightDist)
				return min2 - max;
			else
				return max2 - min;
		}
		return 0;
	}

	/*
	 * private static void entityCollide(Entity ent) { Vec3 start =
	 * ent.lastState.position; Vec3 end = ent.getPos(); Vec3 finalPos = end;
	 * float epsilon = 0.01f; //keep us out of objects and the world Vec3 push =
	 * new Vec3(0,0,0); ArrayList<Entity> ents =
	 * Octree.MAIN_TREE.searchEntities(ent.getPos().add(ent.getAABBMax()),
	 * ent.getPos().add(ent.getAABBMin())); for (int
	 * index=0;index<ents.size();index++) { Entity e = ents.get(index); if (e ==
	 * null || e == ent) continue; //dont test overlap on ourselves Vec3 min1 =
	 * finalPos.add(ent.getAABBMin()); Vec3 max1 =
	 * finalPos.add(ent.getAABBMax()); Vec3 min2 =
	 * e.getPos().add(e.getAABBMin()); Vec3 max2 =
	 * e.getPos().add(e.getAABBMax());
	 * 
	 * Vec3 tPush = AABB_CollisionTest(min2,max2,min1,max1);
	 * 
	 * if (tPush.compareTo(Vec3.origin)) continue;
	 * 
	 * Vec3 posNorm = q.plane.normal.copy(); if (posNorm.x < 0) posNorm.x *= -1;
	 * if (posNorm.y < 0) posNorm.y *= -1; if (posNorm.z < 0) posNorm.z *= -1;
	 * 
	 * 
	 * push = push.add(tPush.mult(posNorm)); if (push.y > 0) { ent.isOnGround =
	 * true; } finalPos = finalPos.add(tPush.mult(posNorm));
	 * 
	 * 
	 * 
	 * Vec3 hitForce = ent.getPhysicsState().velocity.scale(ent.getMass());
	 * 
	 * Vec3 m = ent.getPhysicsState().momentum; TraceResult t =
	 * Utilities.traceEntity(start, finalPos, e); if (t != null && t.hitNormal
	 * != null) {
	 * 
	 * m.x = (1-Math.abs(t.hitNormal.x))*m.x; m.y =
	 * (1-Math.abs(t.hitNormal.y))*m.y; m.z = (1-Math.abs(t.hitNormal.z))*m.z;
	 * 
	 * }
	 * 
	 * 
	 * // float frictionCoefficient = 0.05f; //Vec3 frictionalForce = new
	 * Vec3(0,0,0); //frictionalForce = m.scale(-frictionCoefficient);
	 * //ent.applyImpulse(frictionalForce);
	 * 
	 * //System.out.println(hitForce); //e.applyImpulse(hitForce);
	 * 
	 * //if (e.getPhysicsType() == Entity.PHYS_SIM) //e.applyImpulse(hitForce);
	 * 
	 * 
	 * 
	 * } finalPos = end.add(push); //TODO: change this to a ray casted along the
	 * movement direction, //we should not end up on the side of a prop cause we
	 * fell on top of it really fast. ent.setPos(finalPos); }
	 */

	/*
	 * private static boolean SpanOverlap(float min0, float max0, float min1,
	 * float max1) { return !(min0 > max1 || max0 < min1); } private static void
	 * ComputeSpan(Vec3 Axis, PhysMesh Box,float[] minmax) { float p =
	 * Axis.DotProduct(Box.getWorldCenter()); float r =
	 * Math.abs(Axis.DotProduct(Box.nX)) * Box.obbExtent.x*2 +
	 * Math.abs(Axis.DotProduct(Box.nY)) * Box.obbExtent.y*2 +
	 * Math.abs(Axis.DotProduct(Box.nZ)) * Box.obbExtent.z*2;
	 * 
	 * minmax[0] = p-r; minmax[1] = p+r; } private static boolean
	 * AxisOverlap(Vec3 Axis, PhysMesh B0, PhysMesh B1, ArrayList<Float> fB) {
	 * float[] minmax1 = new float[2]; float[] minmax2 = new float[2];
	 * ComputeSpan(Axis, B0,minmax1); ComputeSpan(Axis, B1, minmax2);
	 * 
	 * 
	 * fB.add(minmax1[0]); fB.add(minmax1[1]); fB.add(minmax2[0]);
	 * fB.add(minmax2[1]);
	 * 
	 * return SpanOverlap(minmax1[0],minmax1[1], minmax2[0], minmax2[1]); }
	 * 
	 * private static boolean Intersect(PhysMesh B0, PhysMesh B1, Vec3 pNormal,
	 * float[] depth) {
	 * 
	 * ArrayList<Float> fB = new ArrayList<Float>(); Vec3[] Axis = new Vec3[12];
	 * 
	 * 
	 * Axis[0] = B0.nX; if (!AxisOverlap(Axis[0], B0, B1,fB)) return false;
	 * Axis[1] = B0.nY; if (!AxisOverlap(Axis[1], B0, B1,fB)) return false;
	 * Axis[2] = B0.nZ; if (!AxisOverlap(Axis[2], B0, B1,fB)) return false;
	 * Axis[3] = B0.nX.CrossProduct(B1.nX); if (!AxisOverlap(Axis[3], B0,
	 * B1,fB)) return false; Axis[4] = B0.nX.CrossProduct(B1.nY); if
	 * (!AxisOverlap(Axis[4], B0, B1,fB)) return false; Axis[5] =
	 * B0.nX.CrossProduct(B1.nZ); if (!AxisOverlap(Axis[5], B0, B1,fB)) return
	 * false; Axis[6] = B0.nY.CrossProduct(B1.nX); if (!AxisOverlap(Axis[6], B0,
	 * B1,fB)) return false; Axis[7] = B0.nY.CrossProduct(B1.nY); if
	 * (!AxisOverlap(Axis[7], B0, B1,fB)) return false; Axis[8] =
	 * B0.nY.CrossProduct(B1.nZ); if (!AxisOverlap(Axis[8], B0, B1,fB)) return
	 * false; Axis[9] = B0.nZ.CrossProduct(B1.nX); if (!AxisOverlap(Axis[9], B0,
	 * B1,fB)) return false; Axis[10] = B0.nZ.CrossProduct(B1.nY); if
	 * (!AxisOverlap(Axis[10], B0, B1,fB)) return false; Axis[11] =
	 * B0.nZ.CrossProduct(B1.nZ); if (!AxisOverlap(Axis[11], B0, B1,fB)) return
	 * false;
	 * 
	 * 
	 * depth[0] = 10000000f; int fbindex = 0; Vec3 nT = null; for(int i = 0; i <
	 * 12; i ++) {
	 * 
	 * 
	 * 
	 * float min = (fB.get(fbindex+2) > fB.get(fbindex))? fB.get(fbindex+2) :
	 * fB.get(fbindex); float max = (fB.get(fbindex+3) < fB.get(fbindex+1))?
	 * fB.get(fbindex+3) : fB.get(fbindex+1); fbindex+=4; float d = max - min;
	 * 
	 * float Alength = Axis[i].magnitude();
	 * 
	 * if (Alength < 0.0001f) continue;
	 * 
	 * // nromalise the penetration depth, are axes are not normalised
	 * 
	 * d /= Alength;
	 * 
	 * if (d < depth[0]) { depth[0] = d; nT = Axis[i].scale(1/Alength); }
	 * 
	 * 
	 * }
	 * 
	 * // make sure the normal points towards the box0
	 * 
	 * Vec3 Diff = B1.getWorldCenter().subtract(B0.getWorldCenter());
	 * 
	 * if (Diff.DotProduct(nT) > 0.0f) nT = nT.scale(-1f);
	 * 
	 * 
	 * 
	 * pNormal.x = nT.x; pNormal.y = nT.y; pNormal.z = nT.z; return true; }
	 */
}
