package grengine.util;

import grengine.entity.Entity;
import grengine.manager.EntityManager;
import grengine.physics.PhysicsCollider;
import grengine.physics.Vec3;
import grengine.world.Octree;
import grengine.world.Plane;
import grengine.world.WorldMap;
import grengine.world.WorldQuad;

import java.util.ArrayList;
import java.util.HashMap;

import org.lwjgl.opengl.GL11;

public abstract class Utilities {
	
	public static float DEG2RAD = (float) (Math.PI/180f);
	public static float lerp( float delta,float from,float to)
	{
	    if ( delta > 1 ) return to;
	    if ( delta < 0 ) return from;
	    
	    return from + (to - from) * delta;
	}
	
	public static Vec3 lerpVector(float delta, Vec3 from, Vec3 to)
	{
		Vec3 lerpedVec = new Vec3(0,0,0);
		lerpedVec.x = lerp(delta,from.x,to.x);
		lerpedVec.y = lerp(delta,from.y,to.y);
		lerpedVec.z = lerp(delta,from.z,to.z);
		
		return lerpedVec;
		
	}
	public static int ClassifyPoint(Plane p,Vec3 v) {
		float dist = p.normal.DotProduct(v) + p.distToOrigin;
		
		if (dist > 0f ) return 1;
		if (dist < 0f ) return -1;
		
		
		return 0;
		
	}
	public static TraceResult getGroundTrace(Entity ent) {
		
		Vec3 start = ent.getPos();
		Vec3 end = start.subtract(new Vec3(0,0.05,0));
		
		HashMap<Object,Boolean> test = new HashMap<Object,Boolean>();
		test.put(ent,true);
		
		TraceResult tr = Utilities.traceLine(start, end, test);
		//System.out.println(t.hitWorld+" "+t.hitPos+" "+t.hitNormal+t.start+" "+t.end);

		
		
		
		return tr;
	}
	public static void drawLine(Vec3 start, Vec3 end) 
	{
		GL11.glPushMatrix();
	       GL11.glLineWidth(5.0f);
	        GL11.glBegin(GL11.GL_LINES);
	        	GL11.glVertex3f(start.x,start.y,start.z);
	        	GL11.glVertex3f(end.x,end.y,end.z);	
	        GL11.glEnd();
	    GL11.glPopMatrix();		
	}
	private static TraceResult traceWorldQuad(Vec3 start,Vec3 end,WorldQuad q) {
		Plane p = q.plane;
		TraceResult tr = new TraceResult();
		tr.start = start;
		tr.end = end;
		if (PhysicsCollider.colls)
		PhysicsCollider.numLoops++; 
		Vec3 ray = end.subtract(start);
		int iClass = Utilities.ClassifyPoint(p, start);
		int fClass = Utilities.ClassifyPoint(p, end);
		if (iClass != fClass) { //the line crossed the infinite plane
			float t = -(p.normal.DotProduct(start) + p.distToOrigin)/p.normal.DotProduct(ray);
			Vec3 intersection = start.add(ray.scale(t));
			float radSum = 0f;
			Vec3 d1 = q.point1.subtract(intersection).normalize();
			Vec3 d2 = q.point2.subtract(intersection).normalize();
			Vec3 d3 = q.point3.subtract(intersection).normalize();
			Vec3 d4 = q.point4.subtract(intersection).normalize();
			
			radSum += Math.acos(d1.DotProduct(d2));
			radSum += Math.acos(d2.DotProduct(d3));
			radSum += Math.acos(d3.DotProduct(d4));
			radSum += Math.acos(d4.DotProduct(d1));
			if (Math.abs(radSum-(2*Math.PI)) < 0.1f) { //we hit the bounded quad
				tr.hitNormal = p.normal;
				tr.hitWorld = true;
				tr.hitPos = intersection;
				tr.hit = true;
				tr.hitQuad = q;
			}
		}
		return tr;
	}	
	public static TraceResult traceWorldLine(Vec3 start,Vec3 end,HashMap<Object,Boolean> filterMap)
	{
		TraceResult tr = new TraceResult();
		TraceResult closestCollision = tr;
		float closestDistSquared = 9999999999f; //hopefully the maps are never larger than the square root of this (99999)
		for (int i=0;i<WorldMap.worldData.length;i++)
		{		
			WorldQuad q = WorldMap.worldData[i];
			if (filterMap != null && filterMap.containsKey(q)) continue;
			tr = Utilities.traceWorldQuad(start, end, q);
			if (tr.hit) {
				float sqDist = start.distanceSquared(tr.hitPos);
				if (closestCollision == null || closestDistSquared > sqDist) {
					closestCollision = tr;
					closestDistSquared = sqDist;
				}
			}
		}
		if (!closestCollision.hit)
			closestCollision.hitPos = end;
		return closestCollision;
	}
	private static TraceResult traceFace(Vec3 start, Vec3 end, Vec3 point1,
			Vec3 point2, Vec3 point3, Vec3 point4,boolean flippedNormal) 
	{	
		Vec3 centerPoint = new Vec3((point1.x+point2.x+point3.x+point4.x)/4f,
				   (point1.y+point2.y+point3.y+point4.y)/4f,
				   (point1.z+point2.z+point3.z+point4.z)/4f);	
		if (PhysicsCollider.colls)
		PhysicsCollider.numLoops++;
		TraceResult tr = new TraceResult();
		tr.start = start;
		tr.end = end;
		Vec3 vec1 = point1.subtract(point2);
		Vec3 vec2 = point2.subtract(point3);
		Vec3 normal = vec1.CrossProduct(vec2).normalize();
		float d =  Math.abs(normal.DotProduct(centerPoint));

		if (centerPoint.x < 0)
			normal.x *= -1;
		if (centerPoint.y < 0)
			normal.y *= -1;
		if (centerPoint.z < 0)
			normal.z *= -1;
		
		
		Plane p = new Plane(normal.scale(-1),d);
		Vec3 ray = end.subtract(start);
		int iClass = Utilities.ClassifyPoint(p, start);
		int fClass = Utilities.ClassifyPoint(p,end);
		if (iClass != fClass) { //the line crossed the infinite plane
			
			float t = -(p.normal.DotProduct(start) + p.distToOrigin)/p.normal.DotProduct(ray);
			Vec3 intersection = start.add(ray.scale(t));
		
			float radSum = 0f;
			Vec3 d1 = point1.subtract(intersection).normalize();
			Vec3 d2 = point2.subtract(intersection).normalize();
			Vec3 d3 = point3.subtract(intersection).normalize();
			Vec3 d4 = point4.subtract(intersection).normalize();
			
			
			radSum += Math.acos(d1.DotProduct(d2));
			radSum += Math.acos(d2.DotProduct(d3));
			radSum += Math.acos(d3.DotProduct(d4));
			radSum += Math.acos(d4.DotProduct(d1));
			//System.out.println("Trace Rads: "+radSum/Math.PI);
			if (Math.abs(radSum-(2*Math.PI)) < 0.1f) { //we hit the bounded quad
				//System.out.println("HIT!!@#!@#!@");
				
				Vec3 normalOut = p.normal;

				if (flippedNormal)
					normalOut = normal;
				
				if (centerPoint.x < 0)
					normalOut.x *= -1;
				if (centerPoint.y < 0)
					normalOut.y *= -1;
				if (centerPoint.z < 0)
					normalOut.z *= -1;
				
				tr.hitNormal = normalOut;
				tr.hitPos = intersection;
				tr.hit = true;
			}
		}
		return tr;
	}
	public static TraceResult traceEntity(Vec3 start,Vec3 end,Entity ent) {
		
		Vec3 aabbMax = ent.getPos().add(ent.getAABBMax());
		Vec3 aabbMin = ent.getPos().add(ent.getAABBMin());
		float xLen = ent.getAABBMax().x-ent.getAABBMin().x;
		float yLen = ent.getAABBMax().y-ent.getAABBMin().y;
		float zLen = ent.getAABBMax().z-ent.getAABBMin().z;
		//top clockwise
		Vec3 point1 = aabbMax;
		Vec3 point2 = aabbMax.subtract(new Vec3(0,0,zLen));
		Vec3 point3 = aabbMax.subtract(new Vec3(xLen,0,zLen));
		Vec3 point4 = aabbMax.subtract(new Vec3(xLen,0,0));
		
		//bottom clockwise
		Vec3 point5 = aabbMin;
		Vec3 point6 = aabbMin.add(new Vec3(0,0,zLen));
		Vec3 point7 = aabbMin.add(new Vec3(xLen,0,zLen));
		Vec3 point8 = aabbMin.add(new Vec3(xLen,0,0));
		
		TraceResult[] faceTrace = new TraceResult[6];
		

		//-Z
		faceTrace[0] = traceFace(start,end,point2,point3,point5,point8,false);
		//+Z
		faceTrace[1] = traceFace(start,end,point4,point6,point7,point1,true);
		//+X
		faceTrace[2] = traceFace(start,end,point2,point1,point7,point8,true);
		//-X
		faceTrace[3] = traceFace(start,end,point6,point5,point3,point4,false);
		//-Y
		faceTrace[4] = traceFace(start,end,point8,point7,point6,point5,true);
		//+Y
		faceTrace[5] = traceFace(start,end,point1,point2,point3,point4,true);
		TraceResult closestTrace = new TraceResult();
		float closestDist = 9999999999f;
		for (int i=0;i<6;i++) {
//System.out.println("face "+i+" "+faceTrace[i].hitNormal+" "+faceTrace[i].hitPos);
			float dist;
			if (faceTrace[i].hitPos != null) {
				dist = faceTrace[i].hitPos.distanceSquared(start);
				if (dist < closestDist) {
					closestTrace = faceTrace[i];
					closestDist = dist;
					continue;
				}
			}
		}
		if (closestTrace.hit)
			closestTrace.hitEntity = ent;
		return closestTrace;
	}	

	public static TraceResult traceLine(Vec3 start,Vec3 end,HashMap<Object,Boolean> filterMap) {
		
		TraceResult tr = new TraceResult();
		TraceResult closestCollision = tr;
		float closestDistSquared = 9999999999f; //hopefully the maps are never larger than the square root of this (99999)
		TraceResult worldLine = traceWorldLine(start,end,filterMap);
		if (worldLine.hit) {
			closestCollision = worldLine;
			closestDistSquared = worldLine.hitPos.distanceSquared(start);
		}
		TraceResult entityLine = traceEntityLine(start,end,filterMap);
		float entDist = 0f;
		if (entityLine.hit && (entDist = entityLine.hitPos.distanceSquared(start)) < closestDistSquared) {
			closestCollision = entityLine;
			closestDistSquared = entDist;
		}
		
		return closestCollision;
	}
	public static TraceResult traceHull(Vec3 start,Vec3 end,HashMap<Object,Boolean> filterMap) {
		
		TraceResult tr = new TraceResult();
		TraceResult closestCollision = tr;
		float closestDistSquared = 9999999999f; //hopefully the maps are never larger than the square root of this (99999)
		TraceResult worldLine = traceWorldLine(start,end,filterMap);
		if (worldLine.hit) {
			closestCollision = worldLine;
			closestDistSquared = worldLine.hitPos.distanceSquared(start);
		}
		TraceResult entityLine = traceEntityLine(start,end,filterMap);
		float entDist = 0f;
		if (entityLine.hit && (entDist = entityLine.hitPos.distanceSquared(start)) < closestDistSquared) {
			closestCollision = entityLine;
			closestDistSquared = entDist;
		}
		
		return closestCollision;
	}	
    public static float arraySum(ArrayList<Float> nums)
	{
		float total = 0;
		for(int i=0;i<nums.size();i++)
		{
			total += nums.get(i);
		}
		
		return total;
	}

	public static TraceResult traceEntityLine(Vec3 start, Vec3 end,
			HashMap<Object,Boolean> filterMap) {
		TraceResult tr = new TraceResult();
		
		TraceResult closestCollision = new TraceResult();
		float closestDistSquared = 9999999999f; //hopefully the maps are never larger than the square root of this (99999)
		ArrayList<Entity> ents = Octree.MAIN_TREE.searchEntities(start, end);
		
		for (int i=0;i<ents.size();i++)
		{
			Entity ent = ents.get(i);
			if (ent == null || (filterMap != null && filterMap.containsKey(ent))) continue;
			tr = Utilities.traceEntity(start, end, ent);
			if (tr.hit) {
				float sqDist = start.distanceSquared(tr.hitPos);		
				if (closestCollision == null || closestDistSquared > sqDist) {
					closestCollision = tr;
					closestDistSquared = sqDist;
				}
			}
		}
		if (!closestCollision.hit)
			closestCollision.hitPos = end;
		
		return closestCollision;
	}
	

	public static TraceResult traceWorldHull(Vec3 start, Vec3 end, Vec3 min,
			Vec3 max, HashMap<Object,Boolean> filterMap) {
		Object[][] hullTraces = new Object[8][2]; //we are going to trace each line here to be sure we can go there
		hullTraces[0][0] = max;
		hullTraces[0][1] = Utilities.traceWorldLine(start.add((Vec3) hullTraces[0][0]), end.add((Vec3) hullTraces[0][0]), filterMap);
		hullTraces[1][0] = min;
		hullTraces[1][1] = Utilities.traceWorldLine(start.add((Vec3) hullTraces[1][0]), end.add((Vec3) hullTraces[1][0]), filterMap);
		
		
		hullTraces[2][0] = max.subtract(new Vec3(0,0,max.z-min.z));
		hullTraces[2][1] = Utilities.traceWorldLine(start.add((Vec3) hullTraces[2][0]), end.add((Vec3) hullTraces[2][0]), filterMap);
		
		hullTraces[3][0] = max.subtract(new Vec3(max.x-min.x,0,max.z-min.z));
		hullTraces[3][1] = Utilities.traceWorldLine(start.add((Vec3) hullTraces[3][0]), end.add((Vec3) hullTraces[3][0]), filterMap);
		
		hullTraces[4][0] = max.subtract(new Vec3(max.x-min.x,0,0));
		hullTraces[4][1] = Utilities.traceWorldLine(start.add((Vec3) hullTraces[4][0]), end.add((Vec3) hullTraces[4][0]), filterMap);
		
		hullTraces[5][0] = min.add(new Vec3(0,0,max.z-min.z));
		hullTraces[5][1] = Utilities.traceWorldLine(start.add((Vec3) hullTraces[5][0]), end.add((Vec3) hullTraces[5][0]), filterMap);
		
		hullTraces[6][0] = min.add(new Vec3(max.x-min.x,0,max.z-min.z));
		hullTraces[6][1] = Utilities.traceWorldLine(start.add((Vec3) hullTraces[6][0]), end.add((Vec3) hullTraces[6][0]), filterMap);
		
		hullTraces[7][0] = min.add(new Vec3(max.x-min.x,0,0));
		hullTraces[7][1] = Utilities.traceWorldLine(start.add((Vec3) hullTraces[7][0]), end.add((Vec3) hullTraces[7][0]), filterMap);
			
		TraceResult closestCollision = new TraceResult();
		float closestDistSquared = 9999999999f; //hopefully the maps are never larger than the square root of this (99999)
			
		for (int i=0;i<hullTraces.length;i++)
		{
			TraceResult tr = (TraceResult) hullTraces[i][1];
			Vec3 hullOffset = (Vec3) hullTraces[i][0];
			if (tr.hit) {
				float sqDist = start.add(hullOffset).distanceSquared(tr.hitPos);
				
				if (!closestCollision.hit || closestDistSquared > sqDist) {
					closestCollision = tr;
					closestCollision.hullOffset = hullOffset;
					closestDistSquared = sqDist;
				}
			}
		}

		return closestCollision;	
	}
	
	
	public static TraceResult traceEntityHull(Vec3 start, Vec3 end, Vec3 min,
			Vec3 max, HashMap<Object,Boolean> filterMap) {
		Object[][] hullTraces = new Object[8][2]; //we are going to trace each line here to be sure we can go there
		hullTraces[0][0] = max;
		hullTraces[0][1] = Utilities.traceEntityLine(start.add((Vec3) hullTraces[0][0]), end.add((Vec3) hullTraces[0][0]), filterMap);
		hullTraces[1][0] = min;
		hullTraces[1][1] = Utilities.traceEntityLine(start.add((Vec3) hullTraces[1][0]), end.add((Vec3) hullTraces[1][0]), filterMap);
		
		
		hullTraces[2][0] = max.subtract(new Vec3(0,0,max.z-min.z));
		hullTraces[2][1] = Utilities.traceEntityLine(start.add((Vec3) hullTraces[2][0]), end.add((Vec3) hullTraces[2][0]), filterMap);
		
		hullTraces[3][0] = max.subtract(new Vec3(max.x-min.x,0,max.z-min.z));
		hullTraces[3][1] = Utilities.traceEntityLine(start.add((Vec3) hullTraces[3][0]), end.add((Vec3) hullTraces[3][0]), filterMap);
		
		hullTraces[4][0] = max.subtract(new Vec3(max.x-min.x,0,0));
		hullTraces[4][1] = Utilities.traceEntityLine(start.add((Vec3) hullTraces[4][0]), end.add((Vec3) hullTraces[4][0]), filterMap);
		
		hullTraces[5][0] = min.add(new Vec3(0,0,max.z-min.z));
		hullTraces[5][1] = Utilities.traceEntityLine(start.add((Vec3) hullTraces[5][0]), end.add((Vec3) hullTraces[5][0]), filterMap);
		
		hullTraces[6][0] = min.add(new Vec3(max.x-min.x,0,max.z-min.z));
		hullTraces[6][1] = Utilities.traceEntityLine(start.add((Vec3) hullTraces[6][0]), end.add((Vec3) hullTraces[6][0]), filterMap);
		
		hullTraces[7][0] = min.add(new Vec3(max.x-min.x,0,0));
		hullTraces[7][1] = Utilities.traceEntityLine(start.add((Vec3) hullTraces[7][0]), end.add((Vec3) hullTraces[7][0]), filterMap);
		
		
		TraceResult closestCollision = new TraceResult();
		float closestDistSquared = 9999999999f; //hopefully the maps are never larger than the square root of this (99999)
			
		for (int i=0;i<hullTraces.length;i++)
		{
			TraceResult tr = (TraceResult) hullTraces[i][1];
			Vec3 hullOffset = (Vec3) hullTraces[i][0];
			if (tr.hit) {
				float sqDist = start.add(hullOffset).distanceSquared(tr.hitPos);
				
				if (!closestCollision.hit || closestDistSquared > sqDist) {
					closestCollision = tr;
					closestCollision.hullOffset = hullOffset;
					closestDistSquared = sqDist;
				}
			}
		}

		return closestCollision;	
	}
}
