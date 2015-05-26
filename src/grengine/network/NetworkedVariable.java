package grengine.network;

import java.util.HashMap;
import java.util.Map;

import grengine.physics.Vec3;





public class NetworkedVariable {

	public Map<String, Vec3> vecMap;
	public Map<String, Integer> intMap;
	public Map<String, String> stringMap;
	public Map<String, Integer> entityMap; //entity maps go based off an ever increasing integer for each created entity
	
	public Map<String, Vec3> vecFrameMap;
	public Map<String, Integer> intFrameMap;
	public Map<String, String> stringFrameMap;
	public Map<String, Integer> entityFrameMap; //entity maps go based off an ever increasing integer for each created entity
	
	public NetworkedVariable()
	{
		vecMap = new HashMap<String, Vec3>();
		intMap = new HashMap<String, Integer>();
		stringMap = new HashMap<String, String>();
		entityMap = new HashMap<String, Integer>();
		
		
		vecFrameMap = new HashMap<String, Vec3>();
		intFrameMap = new HashMap<String, Integer>();
		stringFrameMap = new HashMap<String, String>();
		entityFrameMap = new HashMap<String, Integer>();
	}
	
	
	public void clearFrame()
	{
		vecFrameMap.clear();
		intFrameMap.clear();
		stringFrameMap.clear();
		entityFrameMap.clear();
	}
	
	
}
