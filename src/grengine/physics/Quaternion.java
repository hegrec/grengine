package grengine.physics;


public class Quaternion {
    public double w;
    public double x;
    public double y;
    public double z; 

    // create a new object with the given components
    public Quaternion(double w, double x, double y, double z) {
        this.w = w;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Quaternion() {
		w = 1;
		x = 0;
		y = 0;
		z = 0;
	}

    public float angle() {
    	return (float) (Math.acos(w)*2);
    }
    public Vec3 axis() {
    	Vec3 v = new Vec3();
    	double scale = Math.sqrt(x * x + y * y + z * z);
    	v.x = (float) (x/scale);
    	v.y = (float) (y/scale);
    	v.z = (float) (z/scale);
		return v; 
    }
	// return a string representation of the invoking object
    public String toString() {
        return w + " + " + x + "i + " + y + "j + " + z + "k";
    }


    public Matrix getMatrix()
{
	float x2 = (float) (x * x);
	float y2 = (float) (y * y);
	float z2 = (float) (z * z);
	float xy = (float) (x * y);
	float xz = (float) (x * z);
	float yz = (float) (y * z);
	float wx = (float) (w * x);
	float wy = (float) (w * y);
	float wz = (float) (w * z);
 
	// This calculation would be a lot more complicated for non-unit length quaternions
	// Note: The constructor of Matrix4 expects the Matrix in column-major format like expected by
	//   OpenGL
	return new Matrix( 1.0f - 2.0f * (y2 + z2), 2.0f * (xy - wz), 2.0f * (xz + wy), 0.0f,
			2.0f * (xy + wz), 1.0f - 2.0f * (x2 + z2), 2.0f * (yz - wx), 0.0f,
			2.0f * (xz - wy), 2.0f * (yz + wx), 1.0f - 2.0f * (x2 + y2), 0.0f,
			0.0f, 0.0f, 0.0f, 1.0f);
}
    // return the quaternion norm
    public float norm() {
        return (float) Math.sqrt(w*w + x*x +y*y + z*z);
    }

    // return the quaternion conjugate
    public Quaternion conjugate() {
        return new Quaternion(w, -x, -y, -z);
    }

public Vec3 rotate(Vec3 vec) 
{
	Vec3 vn = vec.normalize();
 
	Quaternion vecQuat = new Quaternion();
	Quaternion resQuat = new Quaternion();
	vecQuat.x = vn.x;
	vecQuat.y = vn.y;
	vecQuat.z = vn.z;
	vecQuat.w = 0.0f;
 
	
	resQuat = this.mul(vecQuat);
	resQuat = resQuat.mul(conjugate());
 
	return (new Vec3(resQuat.x, resQuat.y, resQuat.z));
}
	public Quaternion normalize()
	{
		float len = norm()+0.00001f;
		return new Quaternion(w/len,x/len,y/len,z/len);
	}
    // return a new Quaternion whose value is (this + b)
    public Quaternion add(Quaternion b) {
        Quaternion a = this;
        return new Quaternion(a.w+b.w, a.x+b.x, a.y+b.y, a.z+b.z);
    }


    // return a new Quaternion whose value is (this * b)
    public Quaternion mul(Quaternion b) {
        Quaternion a = this;
        
        double w =  (a.w * b.w - a.x * b.x - a.y * b.y - a.z * b.z);
        double x =  (a.w * b.x + a.x * b.w + a.y * b.z - a.z * b.y);
        double y =  (a.w * b.y + a.y * b.w + a.z * b.x - a.x * b.z);
        double z =  (a.w * b.z + a.z * b.w + a.x * b.y - a.y * b.x);
       
        return new Quaternion(w,x,y,z);
    }

    // return a new Quaternion whose value is the inverse of this
    public Quaternion inverse() {
        double d = w*w + x*x + y*y + z*z;
        return new Quaternion(w/d, -x/d, -y/d, -z/d);
    }


    // return a / b
    public Quaternion divides(Quaternion b) {
         Quaternion a = this;
        return a.inverse().mul(b);
    }

	public Quaternion scale(float f) {
		return new Quaternion(w*f,x*f,y*f,z*f);
	}

	public boolean compareTo(Quaternion orientation) {
		
		if (Math.abs(orientation.w - this.w) > 0.01f)
			return false;
		if (Math.abs(orientation.x - this.x) > 0.01f)
			return false;
		if (Math.abs(orientation.y - this.y) > 0.01f)
			return false;
		if (Math.abs(orientation.z - this.z) > 0.01f)
			return false;
		
		return true;
	}
    
}