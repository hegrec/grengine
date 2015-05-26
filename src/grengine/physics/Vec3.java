package grengine.physics;


public class Vec3 {

	public static final Vec3 origin = new Vec3(0,0,0);
	public float x;
	public float y;
	public float z;
	
	public Vec3()
	{
		x = 0;
		y = 0;
		z = 0;
	}
	
	public Vec3(float d, float e, float f)
	{
		x = d;
		y = e;
		z = f;
	}

	public Vec3(double d, double e, double f) {
		x = (float) d;
		y = (float) e;
		z = (float) f;
	}

	public Vec3 rotate(Vec3 axisOfRotation, float angleDeg)
	{
		//
		return this;
	}
	public String toString()
	{
		return "Vec("+x+","+y+","+z+")";
	}
	
	public Vec3 subtract(Vec3 vec)
	{
		return new Vec3(x-vec.x,y-vec.y,z-vec.z);
	}
	
	public Vec3 add(Vec3 vec)
	{
		return new Vec3(x+vec.x,y+vec.y,z+vec.z);
	}
	
	public Vec3 scale(float d)
	{
		return new Vec3(x*d,y*d,z*d);
	}
	
	public Vec3 normalize()
	{
		float len = magnitude()+0.00001f;
		return new Vec3(x/len,y/len,z/len);
	}
	
	public boolean compareTo(Vec3 origin2) 
	{
		if (Math.abs(origin2.x - this.x) > 0.0001f)
			return false;
		if (Math.abs(origin2.y - this.y) > 0.0001f)
			return false;
		if (Math.abs(origin2.z - this.z) > 0.0001f)
			return false;
		
		return true;
	}
	public float angle(Vec3 vec)
	{
		Vec3 n1 = vec.normalize();
		Vec3 n2 = this.normalize();
		double dot = n2.DotProduct(n1);
		
		return (float) Math.acos(dot);
	}
	public float magnitude()
	{
		return (float) Math.sqrt(x*x+y*y+z*z);
	}
	
	public float DotProduct(Vec3 vec)
	{
		return x*vec.x+y*vec.y+z*vec.z;
	}
	
	public float distance(Vec3 vec) {
		return (float) Math.sqrt(distanceSquared(vec));
	}

	//less expensive
	public float distanceSquared(Vec3 vec) {
		// TODO Auto-generated method stub
		return ((vec.x-this.x)*(vec.x-this.x))+((vec.y-this.y)*(vec.y-this.y))+((vec.z-this.z)*(vec.z-this.z));
	}

	public Vec3 abs() {
		Vec3 out = new Vec3(this.x,this.y,this.z);
		
		if (out.x < 0); out.x *= -1;
		if (out.y < 0); out.y *= -1;
		if (out.z < 0); out.z *= -1;
		return out;
	}

	public Vec3 CrossProduct(Vec3 vec2) {
		Vec3 cross = new Vec3(0,0,0);
		cross.x = this.y*vec2.z-this.z*vec2.y;
		cross.y = this.z*vec2.x-this.x*vec2.z;
		cross.z = this.x*vec2.y-this.y*vec2.x;
		
		return cross;
	}

	public Vec3 mult(Vec3 obbMax) {
		// TODO Auto-generated method stub
		return new Vec3(this.x*obbMax.x,this.y*obbMax.y,this.z*obbMax.z);
	}

	public Vec3 copy() {
		// TODO Auto-generated method stub
		return new Vec3(this.x,this.y,this.z);
	}

	public float magnitudeSquared() {
		// TODO Auto-generated method stub
		return x*x+y*y+z*z;
	}
	
}
