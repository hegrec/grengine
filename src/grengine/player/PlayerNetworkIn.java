package grengine.player;

import java.io.*;
import java.net.*;
import java.util.concurrent.locks.ReentrantLock;

import grengine.network.NetworkMessage;

public class PlayerNetworkIn implements Runnable {
	private InputStream readerIn;
	private PlayerNetworking networkHandler;
	private Socket inSock;
	private byte[] byteBuffer;
	private int bufferSize = 655355;
	private int currentBufferSize = 0;
	private int bytesThisSecond;
	private long nextSecond;
	private Thread thisThread;
	private int bytesLastSecond;
	private ReentrantLock lock;

	
	public PlayerNetworkIn(PlayerNetworking playerNetworking, Socket incoming) {
		networkHandler = playerNetworking;
		inSock = incoming;
		bytesThisSecond=0;
		nextSecond=System.currentTimeMillis()+1000;
		byteBuffer = new byte[bufferSize];
		
		lock = new ReentrantLock();
		try {
			inSock.setReceiveBufferSize(8192*4);
			readerIn = inSock.getInputStream();
			
		} catch (IOException e) {

		}
	}

	public void run() {
		
		while (true) 
		{		

			lock.lock();
			if (readerIn == null || inSock == null)
				continue;
			try 
			{
				int availableBytes = readerIn.available();
				//if (availableBytes > 0 || currentBufferSize > 0)
					//System.out.println(readerIn.available()+" bytes to be read this loop, buffer size will be "+(availableBytes+currentBufferSize));
				currentBufferSize = currentBufferSize + readerIn.read(byteBuffer, currentBufferSize, availableBytes); //read the packet into a byte buffer
				bytesThisSecond += availableBytes;
			}
			catch (SocketException e1)
			{
				e1.printStackTrace();
			}
			catch (IOException e1)
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			if(currentBufferSize > 0)
			{
				processBufferedPackets();
			}
			else if(currentBufferSize == -1)//if no byte are read, the player left
			{
				networkHandler.disconnect();
			}			
			try 
			{
				Thread.sleep(15);
			} 
			catch (InterruptedException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if (nextSecond<System.currentTimeMillis())
			{
				bytesLastSecond=bytesThisSecond;
				nextSecond = System.currentTimeMillis()+1000;
				bytesThisSecond=0;
			}
			
			lock.unlock();
		}
	}

	public int getBytesPerSecond()
	{
		return bytesLastSecond;
	}
	private void processBufferedPackets() {
		short sizeOfPacket =  (short)((0xff & byteBuffer[0]) << 8 | (0xff & byteBuffer[1]) << 0);
		//System.out.println("Message size: "+sizeOfPacket +" " + byteBuffer[0] + " " + byteBuffer[1] + " " + currentBufferSize);
		if(sizeOfPacket > 0 && currentBufferSize >= sizeOfPacket + 2)
		{
			byte[] temp = new byte[sizeOfPacket];
			for(int i = 0; i < sizeOfPacket; i++)
				temp[i] = byteBuffer[i + 2];
			
			NetworkMessage msg = new NetworkMessage(temp, sizeOfPacket); //create a new networkmessage from the bytebuffer
			
			int k = 0;

			
			for(int i = sizeOfPacket+2; i < currentBufferSize; i++)
			{
				
				byteBuffer[k++] = byteBuffer[i];
				
			}
		
			currentBufferSize -= sizeOfPacket+2;
			
			//msg.setPointerPosition(0);
			//System.out.println("Message ID: "+msg.readShort());
			msg.setPointerPosition(0);
			
			
			networkHandler.receiveMessage(msg);
			
			//attempt to process the next packet in the buffer
			if (currentBufferSize>=2)
				processBufferedPackets();
		}
		else
		{
			//System.out.println("DUN GOOFED Message size: "+sizeOfPacket +" " + byteBuffer[0] + " " + byteBuffer[1] + " " + currentBufferSize);
		}

		
	}


	public void close() {
		if (readerIn == null) return;
		try {
			readerIn.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		readerIn = null;
		
		
	}

}
