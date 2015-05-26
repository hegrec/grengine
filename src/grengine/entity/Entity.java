package grengine.entity;
import grengine.appstate.EngineBase;
import grengine.input.Input;
import grengine.manager.EntityManager;
import grengine.network.NetworkedVariable;
import grengine.physics.PhysMesh;
import grengine.physics.Quaternion;
import grengine.physics.State;
import grengine.physics.Vec3;
import grengine.player.Player;
import grengine.render.Model;
import grengine.world.WorldMap;

import java.util.ArrayList;


public class Entity {
	public static final int PHYS_SIM = 0; //apply forces to move this entity physicsSimulate returns a force
	public static final int PHYS_WALK = 1; //set the velocity to move this entity physicsSimulate returns a velocity
	public static final int PHYS_NONE = 2;
	
	
	public int uniqueID; //increased by one for every created entity, used for setNWEntity
	
	public State currentState;
	public State lastState;
	public Input input;
	public boolean remove = false; // if this is true, the main class will clear this entity when it checks the variable
	public ArrayList<State> stateBuffer;
	public boolean held;
	public NetworkedVariable nwVars;
	
	protected String name;
	protected float yaw;
	protected float pitch;
	protected float roll;
	
	private int entID;
	private Vec3 aimVector;
	private Vec3 lastAimVector;
	private Vec3 appliedForce; //this is the net force of the current frame, each physics tick, this is taken and calculated to an acceleration and zeroed
	private float nextThink;
	private boolean noThink;
	private boolean takesDamage;
	private PhysMesh physMesh;
	private Model model;
	private Vec3 appliedTorque;
	public boolean isOnGround;
	public Vec3 lastInternalForce;
	

	public Entity()
	{
		nwVars 			= new NetworkedVariable();
		currentState 	= new State(this);
		lastState 		= new State(this);
		nextThink 		= 0;
		lastInternalForce = new Vec3(0,0,0);
		isOnGround		= false;
		input 			= new Input();
		aimVector 		= new Vec3(0,1,0);
		appliedForce 	= new Vec3(0,0,0);
		appliedTorque 	= new Vec3(0,0,0);
		stateBuffer 	= new ArrayList<State>();
		physMesh		= new PhysMesh(this);
		setModel(EngineBase.baseModel);
		setHealth(100);
		setMaxHealth(100);
		setPos(new Vec3(0,0,0));
		
		
		setVelocity(new Vec3(0,0,0));
		setNoThink(false);
		setMass(20);
		setTakesDamage(true);
		setPhysicsType(PHYS_SIM);
	}
	public void setEntID(int entID) 
	{
		this.entID = entID;
	}
	public int getEntID() 
	{
		return entID;
	}
	public Vec3 getAppliedForce()
	{
		return appliedForce;
	}
	public Vec3 getAppliedTorque()
	{
		return appliedTorque;
	}
	public void applyForce(Vec3 forceVector)
	{
		appliedForce = appliedForce.add(forceVector);
	}
	//immediately change the momentum (without integrating over time like a force)
	public void applyImpulse(Vec3 impulse) 
	{
		this.getPhysicsState().momentum = this.getPhysicsState().momentum.add(impulse);
		this.getPhysicsState().recalculate();	
	}
	public void applyForceOffset(Vec3 localR,Vec3 localF)
	{
		//T = r cross f
		Vec3 torque = localR.CrossProduct(localF);	
		appliedTorque = appliedTorque.add(torque);
	}
	public void onUse(Player user)
	{	
		System.out.println(this+" was used by "+user);
	}
	//if A is parented to B, A follows the same motion as B. TODO: When B rotates, A rotates about the axis of B's position
	public void setParent(Entity parent)
	{
		this.setNWEntity("parent",parent);
	}
	public void remove()
	{
		this.remove = true;
	}
	public Entity getParent()
	{
		return this.getNWEntity("parent");
	}
	public void setPos(Vec3 vec)
	{
		currentState.position = vec;
	}
	public void setVelocity(Vec3 vec)
	{
		currentState.velocity = vec;
	}
	public Vec3 getVelocity()
	{
		return currentState.velocity;
	}
	public Vec3 getPos(){return currentState.position;}
	public void setAimVector(Vec3 aimVector) {
		this.lastAimVector = new Vec3(this.aimVector.x,this.aimVector.y,this.aimVector.z);
		this.aimVector = aimVector.normalize();
	}

	public Vec3 getAimVector() {return aimVector;}
	public Vec3 lastAimVector() {return lastAimVector;}
	public void clearNetworkFrame(){nwVars.clearFrame();}
	public boolean isPlayer(){return false;}
	public Vec3 getAABBMin() {return physMesh.getAABBMin();}
	public Vec3 getAABBMax() {return physMesh.getAABBMax();}
	public void setModel(Model m) {this.model = m;}
	
	public void setNWVector(String varName, Vec3 value)
	{
		nwVars.vecMap.put(varName, value);
		nwVars.vecFrameMap.put(varName, value);
		
	}
	public Vec3 getNWVector(String varName){return nwVars.vecMap.get(varName);}
	
	public void setNWInt(String varName, int value){
		nwVars.intMap.put(varName, value);
		nwVars.intFrameMap.put(varName, value);
	}
	public int getNWInt(String varName)
	{
		if (!nwVars.intMap.containsKey(varName)) return -1;
		return nwVars.intMap.get(varName);
	}
	public void setNWString(String varName, String value)
	{
		
		if (value == null) return;
		
		nwVars.stringMap.put(varName, value);
		nwVars.stringFrameMap.put(varName, value);
	}
	public String getNWString(String varName)
	{
		if (!nwVars.stringMap.containsKey(varName)) return null;
		return nwVars.stringMap.get(varName);
	}
	public void setNWEntity(String varName, Entity value)
	{
		int uniqueID = -1;
		if (value != null)
		{
			uniqueID = value.uniqueID;
		}
		//System.out.println("Setting network entity for "+this+" as "+value+" with id "+uniqueID);
		nwVars.entityMap.put(varName, uniqueID);
		nwVars.entityFrameMap.put(varName, uniqueID);
	}
	public Entity getNWEntity(String varName)
	{
		if (!nwVars.entityMap.containsKey(varName)) return null;
		
		int uniqueID = nwVars.entityMap.get(varName);
		Entity e = EntityManager.getEntityByUniqueID(uniqueID);
		
		return e;
	}
	
	public int getHealth(){return getNWInt("health");}
	
	public int getMaxHealth(){return getNWInt("maxhealth");}
	
	public void setHealth(int h)
	{
		h = h < 0 ? 0 : h;
		setNWInt("health", h);
	}
	
	public void setMaxHealth(int h)
	{
		h = h < 0 ? 0 : h;
		setNWInt("maxhealth", h);
	}
	public void think() {}
	public void setNoThink(boolean b) {this.noThink = b;}
	public void setNextThink(float i) {this.nextThink = i;}
	public float getNextThink() {return this.nextThink;}
	public boolean shouldThink() {return !this.noThink;}
	public void setMass(int mass) {
		
		this.currentState.setMass(mass);
		
		
		 
	}
	public float getMass() {return this.getPhysicsState().getMass(); } 
	public Vec3 physicsSimulate() {return new Vec3(0,0,0);}
	public void setPhysicsType(int physicsType) {this.setNWInt("physType",physicsType);}
	public int getPhysicsType() {return this.getNWInt("physType");}
	public void clearAppliedForce() {
		
		this.appliedForce.x = 0;
		this.appliedForce.y = 0;
		this.appliedForce.z = 0;
	}
	public State getPhysicsState() {
		return currentState;
	}

	public void setTakesDamage(boolean takesDamage) {this.takesDamage = takesDamage;}
	public boolean takesDamage() {return takesDamage;}
	public void setOrientation(Quaternion orientation) {
		this.currentState.orientation = orientation;
		
	}
	public Model getModel() {
		// TODO Auto-generated method stub
		return model;
	}
	public PhysMesh getMesh() {
		// TODO Auto-generated method stub
		return physMesh;
	}
	public void clearAppliedTorque() {
		this.appliedTorque.x = 0;
		this.appliedTorque.y = 0;
		this.appliedTorque.z = 0;
		
	}
}