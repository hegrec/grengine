package grengine.manager;
import java.util.HashMap;
import java.util.Map;

import grengine.player.Player;

import grengine.appstate.EngineBase;
import grengine.entity.Entity;


public abstract class EntityManager {
	
	private static Entity globalEntities[] = new Entity[EngineBase.MAX_ENTITIES];
	private static Map<Integer,Entity> uniqueIDs = new HashMap<Integer,Entity>();
	public static int uniqueID = 0;
	

	public static Entity[] getAll() {
		synchronized(globalEntities)
		{
		
		return globalEntities;
		}
	}
	public static int getGlobalEntityCount() {
		int count = 0;
		for (int i=0;i<globalEntities.length;i++)
		{
			if (globalEntities[i] != null)
				count++;
		}
		return count;
	}

	
	
	public static int getGlobalPlayerCount() {
		int count = 0;
		for (int i=0;i<EngineBase.MAX_PLAYERS;i++)
		{
			if (globalEntities[i] != null)
				count++;
		}
		return count;
	}
	public static Player[] getAllPlayers() {
		Player[] players = new Player[getGlobalPlayerCount()];
		int count = 0;
		for (int i=0;i<EngineBase.MAX_PLAYERS;i++)
		{
			if (globalEntities[i] != null)
			{
				players[count++] = (Player) globalEntities[i];
			}
				
		}
		return players;
	}
	public static boolean spawnEntity(Entity newEntity) {


		int startingIndex = EngineBase.MAX_PLAYERS;
		//players are stored at the front of the array
		if (newEntity.isPlayer())
		{
			startingIndex=0;
		
			
			if (getGlobalPlayerCount()>=EngineBase.MAX_PLAYERS)
			{
				System.out.println("Max players have been reached!");
				return false;
				
			}
		}
		
		
		
		
		
		boolean foundSlot = false;
		for (int i=startingIndex;i<EntityManager.globalEntities.length;i++)
		{

			
			
			
			if (EntityManager.globalEntities[i] == null)
			{
				foundSlot = true;
				
				newEntity.getMesh().build(newEntity.getModel().getVerts());
				EntityManager.globalEntities[i] = newEntity;
				newEntity.setEntID(i);
				newEntity.uniqueID = uniqueID;
				EntityManager.uniqueIDs.put(uniqueID,newEntity);
				
				uniqueID++;
				//System.out.println("Setting "+newEntity+" id to "+i);
				break;
		 	}
		}
		if (!foundSlot)
		{
			System.out.println("Can't find slot for new entity!");

			
		}
		else
		{
			Object[] a = {newEntity};
			HookManager.callHook("spawnedEntity",a);
			
		}
		return foundSlot;
		
	}


	public static Entity getEntity(int index) {
		// TODO Auto-generated method stub
		return globalEntities[index];
	}
	public static void removeEntity(Entity ent) {

		synchronized (EntityManager.globalEntities)
		{
			int eID = ent.getEntID();
			uniqueIDs.remove(ent.uniqueID);
			globalEntities[eID] = null;
		}
		
	}
	public static void addEntity(Entity newEnt, int eID) {
		synchronized(EntityManager.globalEntities)
		{
			newEnt.getMesh().build(newEnt.getModel().getVerts());
			EntityManager.globalEntities[eID] = newEnt;
			EntityManager.uniqueIDs.put(newEnt.uniqueID,newEnt);
		}
	}
	public static Entity getEntityByUniqueID(int uniqueID) {
		return uniqueIDs.get(uniqueID);
	}

}
