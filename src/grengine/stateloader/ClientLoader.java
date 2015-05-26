package grengine.stateloader;
import grengine.appstate.EngineClient;
import grengine.gamestate.GameStateData;
import grengine.manager.EntityManager;
import grengine.manager.NWMessageManager;
import grengine.manager.ResourceManager;
import grengine.network.MessageConstants;
import grengine.network.NetworkMessage;
import grengine.player.Player;
import grengine.util.NWHook;

import org.lwjgl.input.Mouse;

/**
 * SClientLoader.java
 * This file handles syncing a connecting client with the current state of the server.
 * We stream the server state in a non linear fashion, storing all updates during the load
 * and applying them when the serverstate has been verified. This leads to an updated and complete state of play
 * upon entering the ClientGameState
 * @author Chris
 *
 */

public class ClientLoader {
	/**
	 * This method notifies the connecting client to enter load mode, as well as beginning the syncing process.
	 * @param ply
	 */
	private EngineClient eClient; 
	private int clockSyncs = 0;
	private int localPlayerEntID;
	public ClientLoader(EngineClient eClient) {
		this.eClient = eClient;

		final ClientLoader thisP = this;
		NWMessageManager.addCallback(MessageConstants.CLIENT_START_LOAD, new NWHook() { 
			public void onRun(NetworkMessage msgIncoming) {thisP.startLoadState(msgIncoming);	}});
		
		NWMessageManager.addCallback(MessageConstants.SYNC_CLOCK, new NWHook() { 
			public void onRun(NetworkMessage msgIncoming) {thisP.receiveClockSync(msgIncoming);	}});
		NWMessageManager.addCallback(MessageConstants.INITIAL_SPAWN, new NWHook() { 
			public void onRun(NetworkMessage msgIncoming) {thisP.finishLoading(msgIncoming);	}});
		
	}
	
	protected void finishLoading(NetworkMessage msgIncoming) {
		eClient.engineStateManager().changeState(eClient.getClientGameState());
		
		ResourceManager.setLocalPlayer((Player) EntityManager.getEntity(localPlayerEntID));
		Mouse.setCursorPosition((int)(ResourceManager.ScrW()*0.5f), (int)(ResourceManager.ScrH()*0.5f));
		Mouse.setGrabbed(true);
		
	}

	protected void receiveClockSync(NetworkMessage msgIncoming) {
		float serverSendTime = msgIncoming.readFloat();
		float clientSendTime = msgIncoming.readFloat();
		
		float currentTime = EngineClient.getCurrentTime();
		
		float latency = (currentTime-clientSendTime);
		float halfLatency = latency/2;
		
		float clientServerDelta = serverSendTime-currentTime+halfLatency;
		
		EngineClient.currentTime += clientServerDelta;
		
		clockSyncs++;
		
		ResourceManager.setLoadingText("Clock syncing");
		if (clockSyncs<10)
		{
			this.requestClockSync();
		}
		else
		{
			NetworkMessage m = new NetworkMessage();
			m.writeShort(MessageConstants.CLIENT_SYNCED);
			ResourceManager.getClientState().messageServer(m);
		}
		
	}

	protected void startLoadState(NetworkMessage msgIncoming) {
		
		localPlayerEntID = msgIncoming.readInt();
		
		GameStateData g = new GameStateData();
		g.addData("localplayerID",localPlayerEntID);
		g.addData("clientLoader",this);
		this.eClient.getLoadState().setGameStateData(g);
		this.eClient.engineStateManager().changeState(this.eClient.getLoadState());
		this.requestClockSync();
		
	}

	public void requestClockSync() {

		float clientTime = EngineClient.getCurrentTime();
		
		NetworkMessage m = new NetworkMessage();
		m.writeShort(MessageConstants.REQUEST_SYNC_CLOCK);
		m.writeFloat(clientTime); //client game time
		ResourceManager.getClientState().messageServer(m);
		
	}


}
