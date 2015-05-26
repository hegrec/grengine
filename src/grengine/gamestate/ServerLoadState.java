package grengine.gamestate;

import grengine.appstate.EngineServer;

public class ServerLoadState extends BaseEngineState {


	private EngineServer eServer;
	private BaseEngineState sGameState; //loaded automatically by the engine after loading
	public ServerLoadState(GameStateData d) {
		super(d);
		eServer = (EngineServer)d.pullData("serverEngine");
		sGameState = (BaseEngineState)d.pullData("serverGameState");
	}

	@Override
	public void begin() {
		// TODO Auto-generated method stub
		System.out.println(">> Server Loading Resources...");
		//TODO: load world and all that jazz, and begin the game state!
		

		
		System.out.println(">> Server Online");
		
		GameStateData d = new GameStateData();
		sGameState.setGameStateData(d);
		eServer.engineStateManager().changeState(sGameState);
		eServer.startServer();
	}

	@Override
	public void exit() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void think(double delta) {
		// TODO Auto-generated method stub
		
	}
}
