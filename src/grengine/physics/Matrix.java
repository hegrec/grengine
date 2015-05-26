package grengine.physics;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;


public class Matrix {
 
	final float epsilon = 0.00001f;                         ///< floating point epsilon for single precision. todo: verify epsilon value and usage
	final float epsilonSquared = epsilon * epsilon; 
	private float m11;
	private float m12;
	private float m13;
	private float m14;
	private float m21;
	private float m22;
	private float m23;
	private float m24;
	private float m31;
	private float m32;
	private float m33;
	private float m34;
	private float m41;
	private float m42;
	private float m43;
	private float m44;
	public Matrix() {
		
	}
	
	public Matrix(Vec3 a,Vec3 b, Vec3 c) {
		
		m11 = a.x; 
		m12 = a.y;
		m13 = a.z;
		m14 = 0;
		m21 = b.x;
		m22 = b.y;
		m23 = b.z;
		m24 = 0;
		m31 = c.x;
		m32 = c.y;
		m33 = c.z;
		m34 = 0;
		m41 = 0;
		m42 = 0;
		m43 = 0;
		m44 = 1;
		
		
	}
	
	public Matrix(float m11, float m12, float m13,
			   float m21, float m22, float m23,
			   float m31, float m32, float m33)
		{
			this.m11 = m11;
			this.m12 = m12;
			this.m13 = m13;
			this.m14 = 0;
			this.m21 = m21;
			this.m22 = m22;
			this.m23 = m23;
			this.m24 = 0;
			this.m31 = m31;
			this.m32 = m32;
			this.m33 = m33;
			this.m34 = 0;
			this.m41 = 0;
			this.m42 = 0;
			this.m43 = 0;
			this.m44 = 1;
		}
	
	Matrix(float m11, float m12, float m13, float m14,
			   float m21, float m22, float m23, float m24,
			   float m31, float m32, float m33, float m34,
			   float m41, float m42, float m43, float m44)
		{
			this.m11 = m11;
			this.m12 = m12;
			this.m13 = m13;
			this.m14 = m14;
			this.m21 = m21;
			this.m22 = m22;
			this.m23 = m23;
			this.m24 = m24;
			this.m31 = m31;
			this.m32 = m32;
			this.m33 = m33;
			this.m34 = m34;
			this.m41 = m41;
			this.m42 = m42;
			this.m43 = m43;
			this.m44 = m44;
		}	
	
	
	
	
	public void zero()
	{
		m11 = 0;
		m12 = 0;
		m13 = 0;
		m14 = 0;
		m21 = 0;
		m22 = 0;
		m23 = 0;
		m24 = 0;
		m31 = 0;
		m32 = 0;
		m33 = 0;
		m34 = 0;
		m41 = 0;
		m42 = 0;
		m43 = 0;
		m44 = 0;
	}
	
	public void identity()
	{
		m11 = 1;
		m12 = 0;
		m13 = 0;
		m14 = 0;
		m21 = 0;
		m22 = 1;
		m23 = 0;
		m24 = 0;
		m31 = 0;
		m32 = 0;
		m33 = 1;
		m34 = 0;
		m41 = 0;
		m42 = 0;
		m43 = 0;
		m44 = 1;
	}

	/// set to a translation matrix.

	public void translate(float x, float y, float z)
	{
		m11 = 1;		  // 1 0 0 x 
		m12 = 0;		  // 0 1 0 y
		m13 = 0;		  // 0 0 1 z
		m14 = x;		  // 0 0 0 1
		m21 = 0;
		m22 = 1;
		m23 = 0;
		m24 = y;
		m31 = 0;
		m32 = 0;
		m33 = 1;
		m34 = z;
		m41 = 0;
		m42 = 0;
		m43 = 0;
		m44 = 1;
	}
	
	public void translate(Vec3 vector)
	{
		m11 = 1;		  // 1 0 0 x 
		m12 = 0;		  // 0 1 0 y
		m13 = 0;		  // 0 0 1 z
		m14 = vector.x;   // 0 0 0 1
		m21 = 0;
		m22 = 1;
		m23 = 0;
		m24 = vector.y;
		m31 = 0;
		m32 = 0;
		m33 = 1;
		m34 = vector.z;
		m41 = 0;
		m42 = 0;
		m43 = 0;
		m44 = 1;
	}

	/// set to a scale matrix.

	public void scale(float s)
	{
		m11 = s;
		m12 = 0;
		m13 = 0;
		m14 = 0;
		m21 = 0;
		m22 = s;
		m23 = 0;
		m24 = 0;
		m31 = 0;
		m32 = 0;
		m33 = s;
		m34 = 0;
		m41 = 0;
		m42 = 0;
		m43 = 0;
		m44 = 1;
	}

	/// set to a diagonal matrix.

	public void diagonal(float a, float b, float c, float d)
	{
		m11 = a;
		m12 = 0;
		m13 = 0;
		m14 = 0;
		m21 = 0;
		m22 = b;
		m23 = 0;
		m24 = 0;
		m31 = 0;
		m32 = 0;
		m33 = c;
		m34 = 0;
		m41 = 0;
		m42 = 0;
		m43 = 0;
		m44 = d;
	}
	public Vec3 rotateVector(Vec3 vec) {
		Vec3 vOut = new Vec3();
		
		vOut.x = m11*vec.x + m12*vec.y + m13*vec.z + m14;
		vOut.y = m21*vec.x + m22*vec.y + m23*vec.z + m24;
		vOut.z = m31*vec.x + m32*vec.y + m33*vec.z + m34;
		
		return vOut;
	}
	
	void rotate(float angle, Vec3 axis)
	{
		// note: adapted from david eberly's code with permission
		
		if (axis.magnitudeSquared()<epsilonSquared)
		{
			identity(); 
		}
		else
		{
			axis.normalize();

			float fCos = (float) Math.cos(angle);
			float fSin = (float) Math.sin(angle);
			float fOneMinusCos = 1.0f-fCos;
			float fX2 = axis.x*axis.x;
			float fY2 = axis.y*axis.y;
			float fZ2 = axis.z*axis.z;
			float fXYM = axis.x*axis.y*fOneMinusCos;
			float fXZM = axis.x*axis.z*fOneMinusCos;
			float fYZM = axis.y*axis.z*fOneMinusCos;
			float fXSin = axis.x*fSin;
			float fYSin = axis.y*fSin;
			float fZSin = axis.z*fSin;
		
			m11 = fX2*fOneMinusCos+fCos;
			m12 = fXYM-fZSin;
			m13 = fXZM+fYSin;
			m14 = 0;
			
			m21 = fXYM+fZSin;
			m22 = fY2*fOneMinusCos+fCos;
			m23 = fYZM-fXSin;
			m24 = 0;
			
			m31 = fXZM-fYSin;
			m32 = fYZM+fXSin;
			m33 = fZ2*fOneMinusCos+fCos;
			m34 = 0;
			
			m41 = 0;
			m42 = 0;
			m43 = 0;
			m44 = 1;
		}
	}
	
	public float determinant()
	{
		return -m13*m22*m31 + m12*m23*m31 + m13*m21*m32 - m11*m23*m32 - m12*m21*m33 + m11*m22*m33;
	}
	
	public Matrix inverse()
	{
		Matrix matrix = new Matrix();
		inverse(matrix);
		return matrix;
	}
	
	void inverse(Matrix inverse)
	{
		float determinant = this.determinant();

		if(determinant == 0) return;

		float k = 1.0f / determinant;

		inverse.m11 = (m22*m33 - m32*m23) * k;
		inverse.m12 = (m32*m13 - m12*m33) * k;
		inverse.m13 = (m12*m23 - m22*m13) * k;
		inverse.m21 = (m23*m31 - m33*m21) * k;
		inverse.m22 = (m33*m11 - m13*m31) * k;
		inverse.m23 = (m13*m21 - m23*m11) * k;
		inverse.m31 = (m21*m32 - m31*m22) * k;
		inverse.m32 = (m31*m12 - m11*m32) * k;
		inverse.m33 = (m11*m22 - m21*m12) * k;

		inverse.m14 = -(inverse.m11*m14 + inverse.m12*m24 + inverse.m13*m34);
		inverse.m24 = -(inverse.m21*m14 + inverse.m22*m24 + inverse.m23*m34);
		inverse.m34 = -(inverse.m31*m14 + inverse.m32*m24 + inverse.m33*m34);

		inverse.m41 = m41;
		inverse.m42 = m42;
		inverse.m43 = m43;
		inverse.m44 = m44;
	}
	
	Matrix transpose()
	{
		Matrix matrix = new Matrix();
		transpose(matrix);
		return matrix;
	}

	/// calculate transpose of matrix and write to parameter matrix.

	void transpose(Matrix transpose)
	{
		transpose.m11 = m11;
		transpose.m12 = m21;
		transpose.m13 = m31;
		transpose.m14 = m41;
		transpose.m21 = m12;
		transpose.m22 = m22;
		transpose.m23 = m32;
		transpose.m24 = m42;
		transpose.m31 = m13;
		transpose.m32 = m23;
		transpose.m33 = m33;
		transpose.m34 = m43;
		transpose.m41 = m14;
		transpose.m42 = m24;
		transpose.m43 = m34;
		transpose.m44 = m44;
	}

	public FloatBuffer getBuffer() {
		
		
		
		FloatBuffer b = BufferUtils.createFloatBuffer(16);
		b.put(m11);
		b.put(m12);
		b.put(m13);
		b.put(m14);
		b.put(m21);
		b.put(m22);
		b.put(m23);
		b.put(m24);
		b.put(m31);
		b.put(m32);
		b.put(m33);
		b.put(m34);
		b.put(m41);
		b.put(m42);
		b.put(m43);
		b.put(m44);
		b.position(0);
		return b;
	}

}
