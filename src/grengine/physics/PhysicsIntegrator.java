package grengine.physics;

import grengine.entity.Entity;
import grengine.manager.EntityManager;

public abstract class PhysicsIntegrator {

	// Basic Euler integration
	// Forces are applied all over an object, some directly at the center mass,
	// others at an offset.
	// Offset forces are calculated into a total torque.

	// Linear integration
	// Sum of Forces create a change in the momentum,
	// Velocity is directly recalculated from the momentum
	// Velocity is then integrated to position

	private static void simulateSim(float delta, Entity ent) {
		Vec3 sumOfForces = new Vec3(0, 0, 0);
		Vec3 sumOfTorques = new Vec3(0, 0, 0);
		Vec3 baseForce = ent.getAppliedForce();
		Vec3 lastInternalForce = ent.lastInternalForce;
		Vec3 baseTorque = ent.getAppliedTorque();
		Vec3 internalForce = ent.physicsSimulate();

		State state = ent.getPhysicsState();

		sumOfForces = sumOfForces.add(baseForce);
		sumOfTorques = sumOfTorques.add(baseTorque);
		ent.clearAppliedForce();
		ent.clearAppliedTorque();

		sumOfForces = sumOfForces.add(internalForce);

		ent.getMesh().recompute(ent.getPhysicsState().orientation.getMatrix());

		state.momentum = state.momentum.add(sumOfForces.scale(delta));
		state.orientation = state.orientation.add(state.spin.scale(delta));
		state.angularMomentum = state.angularMomentum.add(sumOfTorques
				.scale(delta));

		if (Math.abs(state.momentum.x) < 0.01f)
			state.momentum.x = 0f;
		if (Math.abs(state.momentum.y) < 0.01f)
			state.momentum.y = 0f;
		if (Math.abs(state.momentum.z) < 0.01f)
			state.momentum.z = 0f;

		state.recalculate();
		state.position = state.position.add(state.velocity.scale(delta));

		ent.isOnGround = false; // reset for collision
		ent.lastInternalForce = internalForce;
	}

	private static void simulateWalk(float delta, Entity ent) {
		Vec3 sumOfForces = new Vec3(0, 0, 0);
		Vec3 baseForce = ent.getAppliedForce();
		Vec3 lastInternalForce = ent.lastInternalForce;
		Vec3 internalForce = ent.physicsSimulate();
		State state = ent.getPhysicsState();
		// System.out.println("walking");
		sumOfForces = sumOfForces.add(baseForce);
		ent.clearAppliedForce();

		state.momentum = state.momentum.add(internalForce);
		state.momentum = state.momentum.add(sumOfForces.scale(delta));

		if (Math.abs(state.momentum.x) < 0.1f)
			state.momentum.x = 0f;
		if (Math.abs(state.momentum.y) < 0.1f)
			state.momentum.y = 0f;
		if (Math.abs(state.momentum.z) < 0.1f)
			state.momentum.z = 0f;

		state.recalculate();
		ent.getMesh().recompute(ent.getPhysicsState().orientation.getMatrix());
		state.position = state.position.add(state.velocity.scale(delta));
		state.momentum = state.momentum.subtract(internalForce);

		ent.isOnGround = false; // reset for collision
		ent.lastInternalForce = internalForce;
	}

	private static void applyExternalForces(Entity entity) {
		if (entity.isOnGround) {
			if (entity.input.jump) {
				entity.applyImpulse(new Vec3(0, 5 * entity.getMass(), 0));
			}
		}
		// gravity
		if (!entity.held)
			entity.applyForce(new Vec3(0, -9.8f * entity.getMass(), 0));
	}

	public static void simulate(float delta) {
		Entity[] ents = EntityManager.getAll();
		// Profiler.start();
		step(delta, ents, false);
		// Profiler.dump();
		PhysicsCollider.colls = true;
		PhysicsCollider.numLoops = 0;
		PhysicsCollider.collisionCheck(ents);
		PhysicsCollider.colls = false;
	}

	public static void simulate(float deltaTime, Entity ent) {
		Entity[] ents = new Entity[1];
		ents[0] = ent;
		step(deltaTime, ents, true);
		PhysicsCollider.collisionCheck(ents);
	}

	private static void step(float delta, Entity[] ents, boolean b) {
		for (int i = 0; i < ents.length; i++) {
			Entity e = ents[i];

			if (e == null)
				continue;

			if (e.getParent() == null && (b || !e.isPlayer())) // players are
																// simulated
																// clientside,
																// serverside
																// off input
			{
				applyExternalForces(e);
				// System.out.println(e);
				if (e.getPhysicsType() == Entity.PHYS_SIM)
					simulateSim(delta, e);
				else if (e.getPhysicsType() == Entity.PHYS_WALK)
					simulateWalk(delta, e);
			}
		}
	}

	/*
	 * 
	 * 
	 * static Derivative evaluate(State state, float t) { Derivative output =
	 * new Derivative(); output.velocity = state.velocity; output.spin =
	 * state.spin; output = forces(state, t,output); return output; }
	 * 
	 * /// Evaluate derivative values for the physics state at future time t+dt
	 * /// using the specified set of derivatives to advance dt seconds from the
	 * /// specified physics state.
	 * 
	 * static Derivative evaluate(State state, float t, float dt, Derivative
	 * derivative) { state.position =
	 * state.position.add(derivative.velocity.scale(dt)); state.momentum =
	 * state.momentum.add(derivative.force.scale(dt)); state.orientation =
	 * state.orientation.add(derivative.spin.scale(dt)); state.angularMomentum =
	 * state.angularMomentum.add(derivative.torque.scale(dt));
	 * state.recalculate();
	 * 
	 * Derivative output = new Derivative(); output.velocity = state.velocity;
	 * output.spin = state.spin; output = forces(state, t+dt,output); return
	 * output; }
	 * 
	 * /// Integrate physics state forward by dt seconds. /// Uses an RK4
	 * integrator to numerically integrate with error O(5).
	 * 
	 * static void integrate(State state, float t, float dt) { Derivative a =
	 * evaluate(state, t); Derivative b = evaluate(state, t, dt*0.5f, a);
	 * Derivative c = evaluate(state, t, dt*0.5f, b); Derivative d =
	 * evaluate(state, t, dt, c);
	 * 
	 * state.position =
	 * state.position.add((a.velocity.add((b.velocity.add(c.velocity
	 * )).scale(2.0f)).add(d.velocity)).scale(1.0f/6.0f * dt)); state.momentum =
	 * state
	 * .momentum.add(a.force.add((b.force.add(c.force)).scale(2.0f)).add(d.force
	 * )).scale(1.0f/6.0f * dt); state.orientation =
	 * state.orientation.add((a.spin
	 * .add((b.spin.add(c.spin)).scale(2.0f)).add(d.spin)).scale(1.0f/6.0f *
	 * dt)); state.angularMomentum =
	 * state.angularMomentum.add((a.torque.add((b.torque
	 * .add(c.torque)).scale(2.0f)).add(d.torque)).scale(1.0f/6.0f * dt));
	 * 
	 * state.recalculate(); }
	 * 
	 * /// Calculate force and torque for physics state at time t. /// Due to
	 * the way that the RK4 integrator works we need to calculate /// force
	 * implicitly from state rather than explictly applying forces /// to the
	 * rigid body once per update. This is because the RK4 achieves /// its
	 * accuracy by detecting curvature in derivative values over the ///
	 * timestep so we need our force values to supply the curvature.
	 * 
	 * static Derivative forces(State state, float t, Derivative d) { Vec3
	 * baseForce = state.ent.getAppliedForce(); Vec3 internalForce =
	 * state.ent.physicsSimulate(); float gravity = 9.8f*state.ent.getMass();
	 * TraceResult groundTrace = Utilities.getGroundTrace(state.ent); boolean
	 * onGround = groundTrace.hit; state.ent.clearAppliedForce(); if (!onGround)
	 * d.force.y -= Math.abs(9.8+d.velocity.y*t*gravity);
	 * 
	 * 
	 * d.force.x += baseForce.x + internalForce.x; d.force.y += baseForce.y +
	 * internalForce.y; d.force.z += baseForce.z + internalForce.z;
	 * System.out.println(d.force);
	 * 
	 * if (state.ent.input.jump && onGround) state.velocity.y += 1000f;
	 * 
	 * 
	 * // sine torque to get some spinning action
	 * 
	 * d.torque.x = (float) (1.0f * Math.sin(t*0.9f + 0.5f)); d.torque.y =
	 * (float) (1.1f * Math.sin(t*0.5f + 0.4f)); d.torque.z = (float) (1.2f *
	 * Math.sin(t*0.7f + 0.9f));
	 * 
	 * // damping torque so we dont spin too fast
	 * 
	 * d.torque = d.torque.subtract(state.angularVelocity.scale(0.2f)); return
	 * d; }
	 */

}
