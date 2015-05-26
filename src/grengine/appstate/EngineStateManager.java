package grengine.appstate;

import grengine.gamestate.BaseEngineState;





/**
 * This class manages various states for the client.
 * @author Chris
 *
 */
public class EngineStateManager {

	
	BaseEngineState currentState;
	
	
	public void changeState(BaseEngineState newState)
	{
		if (currentState == newState) return;
		if (currentState != null)
		{
			currentState.exit();
		}
		
		currentState = newState;
		currentState.begin();
		currentState.initialized = true;
	}
	public BaseEngineState getState() {
		// TODO Auto-generated method stub
		return currentState;
	}
	public BaseEngineState getCurrentState() {
		// TODO Auto-generated method stub
		return currentState;
	}
}
