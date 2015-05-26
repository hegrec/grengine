package grengine.appstate;

import grengine.network.NetworkMessage;
import grengine.physics.Vec3;
import grengine.player.PlayerNetworking;
import grengine.render.Model;
import grengine.world.WorldMap;
 /**
  * The base class of the engine states.
  * @author Chris Hegre
  *
  */
public abstract class EngineBase {
	
	public static final int TICKRATE = 33; //ticks per second
	public static final int MAX_ENTITIES = 1000; //no more than this can be created
	public static final int MAX_PLAYERS = 16; //no more players can connect at this limit
	public static final double INTERPOLATION_DELAY = 0.1d; //we interpolate positions based on a time 0.1 seconds in the past
	public static boolean SERVER = false; //derived states will set this
	public static boolean CLIENT = false;
	protected WorldMap worldMap;
	public static final String ENGINE_NAME = "Grengine";
	public static final String GAME_TITLE = "Grengine Test";
	public static final String ENGINE_VERSION = "v1.0";
	public static int PORT = 1337;
	public int currentFPS;
	public static Model baseModel = new Model();
	private EngineStateManager stateManager;	
	public static float currentTime = 0;
	
	public EngineBase()
	{
		stateManager = new EngineStateManager();
		worldMap = new WorldMap();
		
		
		
		baseModel.addQuad(new Vec3(-0.5,0,-0.5), new Vec3(0.5,0,-0.5), new Vec3(0.5,1,-0.5), new Vec3(-0.5,1,-0.5));
		baseModel.addQuad(new Vec3(-0.5,0,0.5), new Vec3(0.5,0,0.5), new Vec3(0.5,1,0.5), new Vec3(-0.5,1,0.5));
		baseModel.addQuad(new Vec3(-0.5,0,-0.5), new Vec3(-0.5,0,0.5), new Vec3(-0.5,1,0.5), new Vec3(-0.5,1,-0.5));
		baseModel.addQuad(new Vec3(0.5,0,-0.5), new Vec3(0.5,0,0.5), new Vec3(0.5,1,0.5), new Vec3(0.5,1,-0.5));
		
		
		baseModel.addQuad(new Vec3(-0.5,0,-0.5), new Vec3(0.5,0,-0.5), new Vec3(0.5,0,0.5), new Vec3(-0.5,0,0.5));
		baseModel.addQuad(new Vec3(-0.5,1,-0.5), new Vec3(0.5,1,-0.5), new Vec3(0.5,1,0.5), new Vec3(-0.5,1,0.5));


		
		
		
	}
	/**
	 * This method returns the current running FPS of the engine
	 * @return
	 */
	public int getCurrentFPS() {
		// TODO Auto-generated method stub
		return this.currentFPS;
	}
	
	//stub for derived classes
	public void onReceiveMessage(PlayerNetworking connectionOwner, NetworkMessage msgIncoming)
	{
		
	}
	public EngineStateManager engineStateManager() {
		return stateManager;
		
	}

	public abstract void initializeEngine();
	/**
	 * Returns the time in seconds since the server was started
	 * @return
	 */
	public static float getCurrentTime() {
		return currentTime;
	}	

}
