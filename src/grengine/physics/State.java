package grengine.physics;


import grengine.entity.Entity;
import grengine.input.Input;



public class State {

	//primary states
	
	
	
	public Vec3 position; //integrated from velocity
	public Vec3 momentum; //integrated from force, set by impulses
	public Quaternion orientation; //integrated from spin
	public Vec3 angularMomentum; //integrated from torque
	
	//secondary states
	
	public Vec3 velocity; //derived from momentum
	public Quaternion spin; //derived from angular velocity
	public Vec3 angularVelocity; //derived from angular momentum
	public Entity ent;
	
	
	float size;
	float mass;
	float inverseMass;
	float inertiaTensor;
	float inverseInertiaTensor;
	
	public double time;
	public float elasticity;
	public Input input;
	public Vec3 aimVector;
	public State(Entity ent)
	{
		this.ent = ent;
		position = new Vec3();
		momentum = new Vec3();
		orientation = new Quaternion();
		angularMomentum = new Vec3();
		elasticity = 0.5f;
		
		velocity = new Vec3();
		spin = new Quaternion();
		angularVelocity = new Vec3();
		setMass(1f);
		setSize(1f);

		
		this.inertiaTensor = this.mass * this.size * this.size * 1.0f / 6.0f;
		this.inverseInertiaTensor = 1.0f / this.inertiaTensor;
		
		
		time = 0f;
		input = new Input();
	}
	private void setSize(float i) {
		size = i;
		
	}
	public void recalculate() {
		
        velocity = momentum.scale(inverseMass);
        angularVelocity = angularMomentum.scale(inverseInertiaTensor);
        orientation = orientation.normalize();
        spin = new Quaternion(0,angularVelocity.x, angularVelocity.y, angularVelocity.z).mul(orientation).scale(0.5f);
		 
	}
	
	
	public boolean equals(State st)
	{
		//System.out.println(st.position+" "+position);
		//System.out.println(st.velocity+" "+velocity);
		//System.out.println(st.orientation+" "+orientation);
		return st.position.compareTo(position) && st.velocity.compareTo(velocity) && st.orientation.compareTo(orientation);
	}
	public void setMass(float f) {
		this.mass = f;
		this.inverseMass = 1/f;
		recalculate();
	}
	public float getMass() {
		return this.mass;
	}
}
