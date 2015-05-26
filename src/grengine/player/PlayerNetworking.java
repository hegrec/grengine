package grengine.player;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

import grengine.appstate.EngineBase;
import grengine.manager.NWMessageManager;
import grengine.network.MessageConstants;
import grengine.network.NetworkMessage;



public class PlayerNetworking {

	
	private Socket mySock;
	private Player myPlayer;
	private PlayerNetworkIn inward;
	private PlayerNetworkOut outward;
	private EngineBase state;
	
	public PlayerNetworking(Socket incoming, EngineBase state)
	{
		mySock = incoming;

		inward = new PlayerNetworkIn(this,incoming);
		outward = new PlayerNetworkOut(incoming);
		this.state = state;
		
		
		Thread in = new Thread(inward);
		in.start();
		
		
		
	}

	public int getBytesInPerSecond()
	{
		return inward.getBytesPerSecond();
	}


	public Player getPlayer()
	{
		return myPlayer;
	}

	public Socket getSocket()
	{
		return mySock;
	}


 

	public void sendMessage(NetworkMessage msg) {
		outward.sendMessage(msg);
	}


	public void newFrame() {
		
		outward.newFrame();
	}



	public void receiveMessage(NetworkMessage msg) {
		short msgType = msg.readShort();
		if (EngineBase.SERVER)
			NWMessageManager.runMessage(msgType,msg,this.getPlayer());
		else
			NWMessageManager.runMessage(msgType,msg);
	}

	public void setPlayer(Player player) {
		myPlayer = player;
		
	}



	public void disconnect() {
		System.out.println("Player has disconnected!");
		try {
			inward.close();
			outward.close();
			mySock.close();
			mySock = null;
			return;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}





	
}
