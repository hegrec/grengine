package grengine.player;
import grengine.appstate.EngineBase;
import grengine.entity.Entity;
import grengine.input.Input;
import grengine.network.NetworkMessage;
import grengine.physics.State;
import grengine.physics.Vec3;
import grengine.render.Model;
import grengine.weapon.BaseWeapon;


public class Player extends Entity {
	
	private String charName;
	private PlayerNetworking myConnection;
	public float lastNetworkMessage; //use for the server to drop laggers
	public Vec3 lastTilePos;

	public double lastInputTime;
	public boolean connected;
	public float lagTime; //the time elapsed since the last network message
	public Input lastInput;
	private BaseWeapon activeWeapon;
	public Vec3 mouseWorldVec;
	public Entity holdingEnt;
	public float holdDist;

	public Player(PlayerNetworking playerNetworking)
	{
		super();
		myConnection = playerNetworking;
		myConnection.setPlayer(this);
		initializePlayer();
	}



	public Player() {
		super();
		initializePlayer();
	}
	
	private void initializePlayer() {
		
		connected = false;
		lastInputTime = 0;
			
		
		
		Model playerModel = new Model();
		
		playerModel.addQuad(new Vec3(-0.5,0,-0.5), new Vec3(0.5,0,-0.5), new Vec3(0.5,2,-0.5), new Vec3(-0.5,2,-0.5));
		playerModel.addQuad(new Vec3(-0.5,0,0.5), new Vec3(0.5,0,0.5), new Vec3(0.5,2,0.5), new Vec3(-0.5,2,0.5));
		playerModel.addQuad(new Vec3(-0.5,0,-0.5), new Vec3(-0.5,0,0.5), new Vec3(-0.5,2,0.5), new Vec3(-0.5,2,-0.5));
		playerModel.addQuad(new Vec3(0.5,0,-0.5), new Vec3(0.5,0,0.5), new Vec3(0.5,2,0.5), new Vec3(0.5,2,-0.5));
		
		
		playerModel.addQuad(new Vec3(-0.5,0,-0.5), new Vec3(0.5,0,-0.5), new Vec3(0.5,0,0.5), new Vec3(-0.5,0,0.5));
		playerModel.addQuad(new Vec3(-0.5,2,-0.5), new Vec3(0.5,2,-0.5), new Vec3(0.5,2,0.5), new Vec3(-0.5,2,0.5));


		
		setModel(playerModel);
		
		
		
		setHealth(100);
		setMass(10);
		setMaxHealth(100);
		lagTime = 0;
		setNoThink(true);
		setPhysicsType(PHYS_WALK);
	}

	public int getPlayerID() 
	{
		return getEntID();
	}

	public void setCharName(String iCharName) 
	{
		this.charName = iCharName;
	}

	public String getCharName() 
	{
		return charName;
	}
	
	public PlayerNetworking getNetworkHandler()
	{
		return myConnection;
	}

	public boolean isPlayer()
	{
		return true;
	}


	
	public Vec3 physicsSimulate() {
		Vec3 position = new Vec3(0,0,0);
		int distance = 50;
		if (this.input.down)
		{
		    position.x -= distance * (float)Math.sin(Math.toRadians(-yaw));
		    position.z += distance * (float)Math.cos(Math.toRadians(-yaw));
		}
		 
		//moves the camera backward relative to its current rotation (yaw)
		if (this.input.up)
		{
		    position.x += distance * (float)Math.sin(Math.toRadians(-yaw));
		    position.z -= distance * (float)Math.cos(Math.toRadians(-yaw));
		}
		 
		//strafes the camera left relitive to its current rotation (yaw)
		if (this.input.right)
		{
		    position.x -= distance * (float)Math.sin(Math.toRadians(-yaw-90));
		    position.z += distance * (float)Math.cos(Math.toRadians(-yaw-90));
		}
		 
		//strafes the camera right relitive to its current rotation (yaw)
		if (this.input.left)
		{
		    position.x -= distance * (float)Math.sin(Math.toRadians(-yaw+90));
		    position.z += distance * (float)Math.cos(Math.toRadians(-yaw+90));
		}
		return position;
	}
	public void disconnect() {
		myConnection.disconnect();
		myConnection = null;
		
	}

	public void setActiveWeapon(BaseWeapon baseWeapon) {
		this.activeWeapon = baseWeapon;
		
	}
	public BaseWeapon getActiveWeapon()
	{
		return this.activeWeapon;
	}


	public String getIPAddress() {
		return this.getNetworkHandler().getSocket().getInetAddress().getHostAddress();
	}


	public void sendMessage(NetworkMessage m) {
		this.getNetworkHandler().sendMessage(m);
		
	}



	public void addYaw(float dx) {
		yaw = yaw+dx;
		
	}



	public void addPitch(float dy) {
		pitch = Math.max(-90,Math.min(90,pitch-dy*0.1f));
		
	}



	public float getYaw() {
		// TODO Auto-generated method stub
		return yaw;
	}
	public float getPitch() {
		return pitch;
	}



	public Vec3 eyePos() {
		// TODO Auto-generated method stub
		return this.getPos().add(new Vec3(0,this.getAABBMax().y*0.75,0));
	}






	public String getName() {
		// TODO Auto-generated method stub
		return "Unnamed Player";
	}








}
