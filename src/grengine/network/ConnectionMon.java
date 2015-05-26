package grengine.network;

import java.io.*;
import java.net.*;

import grengine.appstate.EngineServer;

public class ConnectionMon extends Thread {
	private static ConnectionMon single = new ConnectionMon();
	public static ConnectionMon get()
	{
		return single;
	}
	private ServerSocket serverSocket;
	private EngineServer serverEngine;
	private boolean close = false;
	public void run()
	{

		
		while(true)
		{
			if (close)
				break;
			try
			{
				Socket incoming = serverSocket.accept();
				
				serverEngine.onNewConnection(incoming);
				
			}
			catch(IOException e)
			{
				System.out.println("Failed client connection!");
			}
			
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			

		}
	}
	public ConnectionMon()
	{

	}
	public void initialize(EngineServer serverEngine,int port)
	{
		this.serverEngine = serverEngine;
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		
		this.start();
	}
	public String getServerIP()
	{
		return serverSocket.getInetAddress().getHostAddress();
	}
	public void close() {
		close = true;
		try {
			serverSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
