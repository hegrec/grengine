package grengine.world;

import grengine.physics.Vec3;

import java.awt.Color;
public class WorldQuad {

	public Vec3 point1;
	public Vec3 point2;
	public Vec3 point3;
	public Vec3 point4;
	public Vec3 centerPoint;
	public Plane plane;
	public Vec3 min;
	public Vec3 max;
	public Color col;
	private String texturePath;
	public WorldQuad(Vec3 p1,Vec3 p2,Vec3 p3,Vec3 p4,String texture) {
		this.point1 = p1;
		this.point2 = p2;
		this.point3 = p3;
		this.point4 = p4;
		centerPoint = new Vec3((this.point1.x+this.point2.x+this.point3.x+this.point4.x)/4f,
							   (this.point1.y+this.point2.y+this.point3.y+this.point4.y)/4f,
							   (this.point1.z+this.point2.z+this.point3.z+this.point4.z)/4f);
		
		//calculate normal vector to plane
		
		Vec3 vec1 = point1.subtract(point2);
		Vec3 vec2 = point2.subtract(point3);
		Vec3 normal = vec1.CrossProduct(vec2).normalize();
		float d = Math.abs(normal.DotProduct(centerPoint));

		
		this.plane = new Plane(normal,d);
		//System.out.println("CENTER!!! "+centerPoint);
		//System.out.println("NORMAL!!! "+normal);
		//System.out.println("DISTANCE! "+d);

		//convert the world coords to local coords, we avoid some calculations this way
	//	p1 = p1.add(centerPoint);
		//p2 = p2.add(centerPoint);
	//	p3 = p3.add(centerPoint);
		//p4 = p4.add(centerPoint);
		
		float minX = 99999;
		float minY = 99999;
		float minZ = 99999;
		float maxX = -99999;
		float maxY = -99999;
		float maxZ = -99999;
		

		if (p1.x < minX) minX = p1.x;
		if (p1.x > maxX) maxX = p1.x;
		if (p1.y < minY) minY = p1.y;
		if (p1.y > maxY) maxY = p1.y;
		if (p1.z < minZ) minZ = p1.z;
		if (p1.z > maxZ) maxZ = p1.z;

		if (p2.x < minX) minX = p2.x;
		if (p2.x > maxX) maxX = p2.x;
		if (p2.y < minY) minY = p2.y;
		if (p2.y > maxY) maxY = p2.y;
		if (p2.z < minZ) minZ = p2.z;
		if (p2.z > maxZ) maxZ = p2.z;
		
		if (p3.x < minX) minX = p3.x;
		if (p3.x > maxX) maxX = p3.x;
		if (p3.y < minY) minY = p3.y;
		if (p3.y > maxY) maxY = p3.y;
		if (p3.z < minZ) minZ = p3.z;
		if (p3.z > maxZ) maxZ = p3.z;
		
		if (p4.x < minX) minX = p4.x;
		if (p4.x > maxX) maxX = p4.x;
		if (p4.y < minY) minY = p4.y;
		if (p4.y > maxY) maxY = p4.y;
		if (p4.z < minZ) minZ = p4.z;
		if (p4.z > maxZ) maxZ = p4.z;
		
		this.min = new Vec3(minX,minY,minZ);
		this.max = new Vec3(maxX,maxY,maxZ);
		

		this.texturePath = texture;
	}
	
	
	
	public String getTexture() {
		// TODO Auto-generated method stub
		return texturePath;
	}
}
