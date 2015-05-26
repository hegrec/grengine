package grengine.manager;

import grengine.appstate.EngineClient;
import grengine.player.Player;
import grengine.player.PlayerNetworking;
import grengine.render.Camera;
import grengine.resource.Texture;
import grengine.resource.TextureLoader;

import java.io.IOException;

/**
 * Our singleton resource loader
 * @author Chris
 *
 */
public abstract class ResourceManager {

	private static Camera viewCam;
	private static EngineClient clientState;
	private static TextureLoader textureLoader = new TextureLoader();
	private static String loadText = "";
	
    public static int ScrW()
    {
    	return clientState.getWidth();
    }
    public static int ScrH()
    {
    	return clientState.getHeight();
    }
	public static Player getLocalPlayer() {
    	return clientState.getLocalPlayer();
	}
	public static Camera getViewCam() {
		// TODO Auto-generated method stub
		return viewCam;
	}
	public static void setViewCam(Camera worldCam)
	{
		viewCam = worldCam;
	}
	public static Texture getTexture(String texture) {
		// TODO Auto-generated method stub
		try {
			return textureLoader.getTexture(texture);
		} catch (IOException e) {
			
			try {
				return textureLoader.getTexture("resource/missing.png");
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
		}
		return null;
	}
	public static EngineClient getClientState() {
		// TODO Auto-generated method stub
		return clientState;
	}
	public static void setClientState(EngineClient cState)
	{
		clientState = cState;
	}
	public static void setLocalPlayer(Player newEnt) {
		clientState.setLocalPlayer(newEnt);
		
	}
	public static void connectToServer(String ip, int port) {
		clientState.connectToServer(ip,port);
		
	}
	public static void setLoadingText(String string) {
		loadText = string;
	}

	public static String getLoadingText() {
		return loadText;
	}
}
