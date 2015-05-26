package grengine.stateloader;
import grengine.appstate.EngineServer;
import grengine.manager.NWMessageManager;
import grengine.manager.ResourceManager;
import grengine.network.MessageConstants;
import grengine.network.NetworkMessage;
import grengine.player.Player;
import grengine.util.NWHook;

/**
 * SClientLoader.java
 * This file handles syncing a connecting client with the current state of the server.
 * We stream the server state in a non linear fashion, storing all updates during the load
 * and applying them when the serverstate has been verified. This leads to an updated and complete state of play
 * upon entering the ClientGameState
 * @author Chris
 *
 */

public class SClientLoader {
	/**
	 * This method notifies the connecting client to enter load mode, as well as beginning the syncing process.
	 * @param ply
	 */
	private EngineServer eServer; 
	public SClientLoader(EngineServer eServer) {
		this.eServer = eServer;

		final SClientLoader thisP = this;
		NWMessageManager.addCallback(MessageConstants.REQUEST_SYNC_CLOCK, new NWHook() { 
			public void onRun(Player ply,NetworkMessage msgIncoming) {thisP.doSync(ply,msgIncoming);	}});
		NWMessageManager.addCallback(MessageConstants.CLIENT_SYNCED, new NWHook() { 
			public void onRun(Player ply,NetworkMessage msgIncoming) {thisP.clientSynced(ply);	}});
	}
	
	protected void clientSynced(Player ply) {
		NetworkMessage m = new NetworkMessage();
		m.writeShort(MessageConstants.INITIAL_SPAWN);
		ply.sendMessage(m);
		
		
	}

	protected void doSync(Player ply, NetworkMessage msgIncoming) {
		float clientTimeSeconds = msgIncoming.readFloat();
		float serverTime = EngineServer.getCurrentTime();
		
		NetworkMessage m = new NetworkMessage();
		m.writeShort(MessageConstants.SYNC_CLOCK);
		m.writeFloat(serverTime); //server game time
		m.writeFloat(clientTimeSeconds); //client game time
		ply.sendMessage(m);
	}

	public void startClientLoad(Player ply) {
		NetworkMessage m = new NetworkMessage();
		m.writeShort(MessageConstants.CLIENT_START_LOAD);
		m.writeInt(ply.getEntID());
		ply.sendMessage(m);
	}

}
