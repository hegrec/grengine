package grengine.gamestate;

import grengine.player.Player;




/**
 * This interface provides ways to allow the client to switch from various states such as splash screen, main menu, and the actual game itself.
 * @author Chris
 *
 */
public abstract class BaseEngineState {
	public double stateStartTime;
	public boolean initialized = false;
	protected GameStateData gStateData;
	private boolean isGame;
	public BaseEngineState(GameStateData d)
	{
		gStateData = d;
		this.isGame = false;
		stateStartTime = System.currentTimeMillis()/1000d;
	}
	public abstract void begin();
	public abstract void exit();
	public abstract void think(double delta);
	public void setGameStateData(GameStateData d) {
		gStateData = d;
		
	} 
	public double getStateTime()
	{
		double nowTime = System.currentTimeMillis()/1000d;
		return nowTime-stateStartTime;
	}
	public void onKeyPressed(int keyCode) {
		// TODO Auto-generated method stub
		
	}
	public void onKeyReleased(int keyCode) {
		// TODO Auto-generated method stub
		
	}
	public void onMousePressed(int button) {
		// TODO Auto-generated method stub
		
	}
	public void onMouseReleased(int button) {
		// TODO Auto-generated method stub
		
	}
	public void onMouseDragged(int button) {
		// TODO Auto-generated method stub
		
	}
	public void onMouseMoved(int button) {
		// TODO Auto-generated method stub
		
	}
	public boolean isGame() {
		// TODO Auto-generated method stub
		return this.isGame;
	}
	

	protected void setGame(boolean b) {
	
		this.isGame = b;
		
	}

	
	
}
