package grengine.manager;

import java.util.ArrayList;
import java.util.HashMap;

import grengine.player.Player;

import grengine.network.NetworkMessage;
import grengine.util.NWHook;

public abstract class NWMessageManager {
	
	
	private static HashMap<Short,NWHook> msgMap = new HashMap<Short,NWHook>();
	private static ArrayList<Object[]> messageQueue = new ArrayList<Object[]>();

	public static void addCallback(short msgID,NWHook hnd)
	{
		msgMap.put(msgID,hnd);
	}
	public static void runMessage(short msgType, NetworkMessage msgIncoming) {

		
		if (msgMap.get(msgType) != null)
			msgMap.get(msgType).onRun(msgIncoming);
		else
			System.out.println("Received bad message: "+msgType);
		
	}
	
	/*
	public static void runQueuedMessages() {
		
		synchronized (messageQueue)
		{
			while (!messageQueue.isEmpty())
			{
				Object[] msgData = messageQueue.get(0);
				
				short msgType = (Short) msgData[0];
				
				NetworkMessage msgIncoming = (NetworkMessage) msgData[1];
				if (msgMap.get(msgType) != null)
				{
					if (msgData.length == 2)
						msgMap.get(msgType).onRun(msgIncoming);
					else
					{
						
						Player ply = (Player) msgData[2];
						msgMap.get(msgType).onRun(ply,msgIncoming);
					}
				}
				else
				{
					System.out.println(msgType+" not a valid message");
				}
				messageQueue.remove(0);
			}
		}
		
	}
	*/
	public static void runMessage(short msgType, NetworkMessage msgIncoming,
			Player ply) {
		
		
		if (msgMap.get(msgType) != null)
			msgMap.get(msgType).onRun(ply,msgIncoming);
		else
			System.out.println(msgType+" not a valid message");
		
	}

}
