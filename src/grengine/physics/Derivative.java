package grengine.physics;


public class Derivative {

	Vec3 velocity; //derivative of position: velocity
	Vec3 force; //derivative of velocity: acceleration
	Quaternion spin;
	Vec3 torque;
	public Derivative() {
		velocity = new Vec3();
		force = new Vec3();
		spin = new Quaternion();
		torque = new Vec3();
	}
}
