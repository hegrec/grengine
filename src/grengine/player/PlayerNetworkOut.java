package grengine.player;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;

import grengine.appstate.EngineBase;

import grengine.network.MessageConstants;
import grengine.network.NetworkMessage;


public class PlayerNetworkOut {
	private PrintStream printOut;
	private Socket outSock;
	private short frameSize;
	public static HashMap<Short,Integer> nwProfiler = new HashMap<Short,Integer>();
	public PlayerNetworkOut(Socket incoming) {
		outSock = incoming;
		try
		{
			printOut = new PrintStream(outSock.getOutputStream());
			
		}
		catch(IOException e)
		{
			
		}
	}

	public void close() {
		printOut.close();
		
	}

	public void newFrame() {
		frameSize = 0;
	}
	public void sendMessage(NetworkMessage msgOut) {
		
		if (msgOut != null)
		{
			msgOut.setPointerPosition(0);
			short size = msgOut.getSize();
			short msgID = msgOut.readShort();
			msgOut.setPointerPosition(0);
			frameSize+= size;
			
			
			
			
			byte[] ba = msgOut.getByteArray();
			
			if (msgID == MessageConstants.INPUT_SYNC)
			{
				
				//System.out.println("sending input message now");
			}
			
			synchronized(printOut)
			{
				printOut.write(msgOut.getSizeAsByteArray(),0,2); //write the size of the packet
				printOut.write(ba,0,size); //write the packet itself
				//if (frameSize<2000) 
					printOut.flush();
			}
			
			if (msgID == MessageConstants.INPUT_SYNC)
			{
				
				//System.out.println("sent input message");
			}
			//messagesOut.remove(0);
			
			int newSize = size;
			
			if (nwProfiler.containsKey(msgID))
			{
				newSize+= nwProfiler.get(msgID);
			}
			nwProfiler.put(msgID, newSize);

		}
		
	}
	
	

	
}
