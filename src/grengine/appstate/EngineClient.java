package grengine.appstate;

import grengine.bgui.BMenu;
import grengine.bgui.BPanel;
import grengine.entity.Entity;
import grengine.gamestate.BaseEngineState;
import grengine.gamestate.ClientInterface;
import grengine.gamestate.GameStateData;
import grengine.manager.BGUIManager;
import grengine.manager.EntityManager;
import grengine.manager.HookManager;
import grengine.manager.NWMessageManager;
import grengine.manager.ResourceManager;
import grengine.network.MessageConstants;
import grengine.network.NetworkMessage;
import grengine.physics.PhysicsIntegrator;
import grengine.physics.Quaternion;
import grengine.physics.State;
import grengine.physics.Vec3;
import grengine.player.Player;
import grengine.player.PlayerNetworking;
import grengine.render.Camera;
import grengine.render.GUI;
import grengine.resource.Texture;
import grengine.stateloader.ClientLoader;
import grengine.util.NWHook;
import grengine.util.Utilities;
import grengine.world.Bucket;
import grengine.world.Octree;

import java.awt.Canvas;
import java.awt.Color;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

/**
 * The base client engine code
 * 
 * @author Chris
 * 
 */
public abstract class EngineClient extends EngineBase {
	public static boolean APPLET = false;

	private long lastFrame;
	private static Player localPlayer = new Player();
	private PlayerNetworking network; // the clientside network handler
	private final Camera viewCam;
	private final boolean prediction = false;
	private final boolean interpolation = false;
	private float latency = 0.0f; // latency in seconds
	private final ArrayList<Float> latencyBuffer;
	private BaseEngineState clientMenuState;
	private BaseEngineState clientGameState;
	private BaseEngineState clientLoadState;
	private final ClientLoader cLoader;
	private final Octree octree;

	private final Texture[] skyboxTexture;

	public EngineClient(Canvas canvas) {

		super();
		EngineBase.CLIENT = true;
		ResourceManager.setClientState(this);
		cLoader = new ClientLoader(this);
		latencyBuffer = new ArrayList<Float>();
		viewCam = new Camera();
		octree = new Octree();
		ResourceManager.setViewCam(viewCam);
		skyboxTexture = new Texture[6];

		// create the screen
		try {
			if (canvas == null) {
				Display.setDisplayMode(new DisplayMode(1280, 720));
				Display.setTitle(GAME_TITLE);
			} else {
				Display.setParent(canvas);
			}
			Display.create();
		} catch (LWJGLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		this.ViewPerspective();

		GL11.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		GL11.glShadeModel(GL11.GL_SMOOTH);// Smooth shading, interpolate colors
											// between vertexes
		// GL11.glClearDepth(1.0f);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		// GL11.glDepthMask(true);
		GL11.glDepthFunc(GL11.GL_LEQUAL);
		GL11.glHint(GL11.GL_PERSPECTIVE_CORRECTION_HINT, GL11.GL_NICEST);

		FloatBuffer fB = BufferUtils.createFloatBuffer(4);
		fB.put(0.5f).put(0.5f).put(0.5f).put(1.0f).flip();
		GL11.glLightModel(GL11.GL_LIGHT_MODEL_AMBIENT, fB);

		FloatBuffer fB2 = BufferUtils.createFloatBuffer(4);
		fB2.put(1.0f).put(1.0f).put(1.0f).put(1.0f).flip();
		GL11.glLight(GL11.GL_LIGHT0, GL11.GL_DIFFUSE, fB2);

		FloatBuffer fB3 = BufferUtils.createFloatBuffer(4);
		fB3.put(1.0f).put(1.0f).put(1.0f).put(1.0f).flip();
		GL11.glLight(GL11.GL_LIGHT0, GL11.GL_POSITION, fB3);

		GL11.glEnable(GL11.GL_LIGHT0);
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_COLOR_MATERIAL);
		GL11.glColorMaterial(GL11.GL_FRONT, GL11.GL_AMBIENT_AND_DIFFUSE);
		// initialize the delta value
		getDelta();

		GUI.createFont("Default", "Courier New", 12, false); // create the
																// default font

		engineNetworkHooks();
	}

	protected void bulletFired(NetworkMessage msgIncoming) {
		// TODO Auto-generated method stub

	}

	public void setClientMenuState(BaseEngineState c) {
		this.clientMenuState = c;
	}

	public void setClientGameState(BaseEngineState c) {
		this.clientGameState = c;
	}

	public BaseEngineState getClientGameState() {
		return this.clientGameState;
	}

	public void setClientLoadState(BaseEngineState c) {
		this.clientLoadState = c;
	}

	private void ViewOrtho() // Set Up An Ortho View
	{
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GL11.glOrtho(0, Display.getWidth(), Display.getHeight(), 0, 1, -1);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
	}

	private void ViewPerspective() // Set Up A Perspective View
	{
		GL11.glMatrixMode(GL11.GL_PROJECTION); // Select Projection
		GL11.glLoadIdentity();
		GLU.gluPerspective(65.0f,
				(float) (Display.getWidth() / ((double) Display.getHeight())),
				0.1f, 2000.0f);
		GL11.glMatrixMode(GL11.GL_MODELVIEW); // Select Modelview
		GL11.glLoadIdentity();

	}

	@Override
	public void initializeEngine() {

		GameStateData d = new GameStateData();
		d.addData("clientGameState", this.clientGameState);

		clientMenuState.setGameStateData(d);
		skyboxTexture[0] = ResourceManager.getTexture("resource/sb/sb_top.png");
		skyboxTexture[1] = ResourceManager.getTexture("resource/sb/sb_1.png");
		skyboxTexture[2] = ResourceManager.getTexture("resource/sb/sb_2.png");
		skyboxTexture[3] = ResourceManager.getTexture("resource/sb/sb_3.png");
		skyboxTexture[4] = ResourceManager.getTexture("resource/sb/sb_4.png");
		skyboxTexture[5] = ResourceManager
				.getTexture("resource/sb/sb_bottom.png");
		this.engineStateManager().changeState(clientMenuState);
		startClient();

	}

	private void startClient() {

		// main client loop here
		while (!Display.isCloseRequested()) {
			float delta = getDelta() / 1000f;
			currentTime += delta;
			octree.queryScene();
			processInput(delta);
			preRender(delta);

			// clear the buffer
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
			GL11.glLoadIdentity();

			render(delta);

			Display.update();

			Display.sync(60);
			// System.out.println(currentTime);
		}
		System.out.println("Exiting");
		Display.destroy();
		System.exit(0);

	}

	private void processInput(double delta) {

		// run through the keyboard event buffer, process all queued events
		while (Keyboard.next()) {
			if (Keyboard.getEventKeyState()) // key was pressed
			{
				onKeyPressed(Keyboard.getEventKey());
			} else // key was released
			{
				onKeyReleased(Keyboard.getEventKey());
			}
		}

		// run through the mouse event buffer, process all mouse events
		while (Mouse.next()) {
			if (Mouse.getEventButtonState()) // button pressed
			{
				onMousePressed(Mouse.getEventButton());
			} else if (Mouse.getEventButton() != -1) {
				onMouseReleased(Mouse.getEventButton());
			} else {
				onMouseMoved();
			}
		}

		int dx = Mouse.getDX();
		int dy = Mouse.getDY();
		if (Mouse.isGrabbed() && ResourceManager.getLocalPlayer() != null) {
			ResourceManager.getLocalPlayer().addYaw(-dx * 0.1f);
			ResourceManager.getLocalPlayer().addPitch(-dy);

		} else if (!Mouse.isGrabbed()) {
			dx = 0;
			dy = 0;
		}

		if (engineStateManager().getCurrentState().isGame()) {

			if (prediction)// TODO: check if in a vehicle and predict based on
							// the vehicle's state using getParent() (predict
							// for driver, interp for passengers)
			{
				PhysicsIntegrator.simulate((float) delta, localPlayer);

				State currentPlayerState = new State(localPlayer);
				currentPlayerState.velocity = localPlayer.getVelocity();
				currentPlayerState.position = localPlayer.getPos();
				currentPlayerState.time = EngineBase.getCurrentTime();
				currentPlayerState.input = localPlayer.input;
				// here we store the current state of the player in a move
				// buffer
				// this allows input response messages to have their data
				// corrected in the past
				synchronized (localPlayer.stateBuffer) {
					localPlayer.stateBuffer.add(0, currentPlayerState);
				}

				System.out.println(localPlayer.getVelocity());

			}

			Player localPlayer = ResourceManager.getLocalPlayer();
			NetworkMessage m = new NetworkMessage();
			m.writeShort(MessageConstants.INPUT_SYNC);
			m.writeFloat(EngineBase.getCurrentTime());
			m.writeFloat(-dx * 0.1f);
			m.writeFloat(-dy);
			m.writeDouble(delta);
			m.writeInput(localPlayer.input);
			this.messageServer(m);
		}
	}

	private void preRender(double delta) {

		BaseEngineState state = this.engineStateManager().getState();
		if (state != null && state.initialized)
			state.think(delta);
		BGUIManager.think();

		Entity[] ents = EntityManager.getAll();
		for (int i = 0; i < ents.length; i++) {
			Entity ent = ents[i];
			if (ent == null)
				continue;
			if (ent != localPlayer && interpolation
					&& ent.stateBuffer.size() >= 2) {

				double renderTime = EngineBase.getCurrentTime()
						- EngineBase.INTERPOLATION_DELAY; // we interpolate 0.1
															// seconds behind
															// the true time

				// TODO: run through extra stored states to get the proper
				// location.

				while (ent.stateBuffer.size() > 2
						&& renderTime >= ent.stateBuffer.get(ent.stateBuffer
								.size() - 2).time) {
					// System.out.println(inputTime+" greater than "
					// +ply.stateBuffer.get(0).time);
					ent.stateBuffer.remove(ent.stateBuffer.size() - 1);
				}

				State latestState = ent.stateBuffer.get(0);
				State secondState = ent.stateBuffer.get(1);

				double time1 = latestState.time;
				double time2 = secondState.time;

				/*
				 * double serverDelta = time1-time2; //the time elapsed between
				 * the serverside physics ticks, should be 1/TICKRATE double
				 * renderOffsetFromOld = renderTime-time2;
				 * 
				 * float interpFrac = (float) (renderOffsetFromOld/serverDelta);
				 * 
				 * 
				 * 
				 * Vec2 interpolatedPosition =
				 * Utilities.lerpVector(interpFrac,secondState
				 * .position,latestState.position); Vec2 interpolatedAimVector =
				 * Utilities
				 * .lerpVector(interpFrac,secondState.aimVector,latestState
				 * .aimVector);
				 */

				Vec3 dPdTPos = latestState.position.subtract(
						secondState.position).scale(
						(float) (1 / (time1 - time2)));
				Vec3 dPdTAim = latestState.aimVector.subtract(
						secondState.aimVector).scale(
						(float) (1 / (time1 - time2)));
				float dT = (float) (renderTime - time2);

				if (latestState.velocity.compareTo(Vec3.origin)) // if the
																	// entity
																	// stopped
																	// moving,
																	// we won't
																	// receive
																	// entity
																	// updates,
																	// so clamp
																	// the value
				{

					dT = (float) Math.min(dT, time1 - time2);
				}

				Vec3 interpolatedPosition = secondState.position.add(dPdTPos
						.scale(dT));
				Vec3 interpolatedAimVector = secondState.aimVector.add(dPdTAim
						.scale(dT));

				// if (ent.getEntID()==25)
				// System.out.println(secondState.position+" "+interpolatedPosition+" "+latestState.position);

				ent.setPos(interpolatedPosition);
				ent.setAimVector(interpolatedAimVector);
			}
		}

	}

	/**
	 * 3D drawing loop, pass to various states and let them handle drawing.
	 * 
	 * @param delta
	 */
	private void render(double delta) {

		if (this.engineStateManager().getCurrentState().isGame()) {
			if (ResourceManager.getLocalPlayer() != null) {
				viewCam.setPos(ResourceManager.getLocalPlayer().eyePos());
				viewCam.yaw(ResourceManager.getLocalPlayer().getYaw());
				viewCam.pitch(ResourceManager.getLocalPlayer().getPitch());

			}
			GL11.glLoadIdentity();

			viewCam.lookThrough();
			// GL11.glScalef(-1f, 1f, -1f);
			worldMap.render();
			Entity[] ents = EntityManager.getAll();
			for (int i = 0; i < ents.length; i++) {
				Entity e = ents[i];
				if (e == null)
					continue;
				if (e != ResourceManager.getLocalPlayer()) {

					GL11.glPushMatrix();
					Vec3 pos = e.getPos();
					GL11.glTranslatef(pos.x, pos.y, pos.z);
					GL11.glMultMatrix(e.getPhysicsState().orientation
							.getMatrix().inverse().getBuffer());

					e.getModel().render();

					GL11.glPopMatrix();
				}
				/*
				 * Vec3 p1 = e.getPos().add(e.getAABBMax()); Vec3 p2 =
				 * e.getPos().add(e.getAABBMax()).subtract(new
				 * Vec3(0,0,e.getAABBMax().z-e.getAABBMin().z)); Vec3 p3 =
				 * e.getPos().add(e.getAABBMax()).subtract(new
				 * Vec3(e.getAABBMax(
				 * ).x-e.getAABBMin().x,0,e.getAABBMax().z-e.getAABBMin().z));
				 * Vec3 p4 = e.getPos().add(e.getAABBMax()).subtract(new
				 * Vec3(e.getAABBMax().x-e.getAABBMin().x,0,0));
				 * 
				 * Vec3 p11 = e.getPos().add(e.getAABBMin()).add(new
				 * Vec3(e.getAABBMax
				 * ().x-e.getAABBMin().x,0,e.getAABBMax().z-e.getAABBMin().z));
				 * Vec3 p22 = e.getPos().add(e.getAABBMin()).add(new
				 * Vec3(e.getAABBMax().x-e.getAABBMin().x,0,0)); Vec3 p33 =
				 * e.getPos().add(e.getAABBMin()); Vec3 p44 =
				 * e.getPos().add(e.getAABBMin()).add(new
				 * Vec3(0,0,e.getAABBMax().z-e.getAABBMin().z));
				 * GUI.setDrawColor(new Color(255,0,0,255));
				 * Utilities.drawLine(p1,p2); Utilities.drawLine(p2,p3);
				 * Utilities.drawLine(p3,p4); Utilities.drawLine(p4,p1);
				 * 
				 * 
				 * Utilities.drawLine(p11,p22); Utilities.drawLine(p22,p33);
				 * Utilities.drawLine(p33,p44); Utilities.drawLine(p44,p11);
				 * 
				 * 
				 * Utilities.drawLine(p1,p11); Utilities.drawLine(p2,p22);
				 * Utilities.drawLine(p3,p33); Utilities.drawLine(p4,p44);
				 * 
				 * 
				 * 
				 * 
				 * GUI.setDrawColor(new Color(0,255,0,255));
				 * 
				 * 
				 * 
				 * Vec3[] v = e.getMesh().obbVec;
				 * 
				 * Utilities.drawLine(e.getPos().add(v[0]),e.getPos().add(v[1]));
				 * Utilities
				 * .drawLine(e.getPos().add(v[1]),e.getPos().add(v[2]));
				 * Utilities
				 * .drawLine(e.getPos().add(v[2]),e.getPos().add(v[3]));
				 * Utilities
				 * .drawLine(e.getPos().add(v[3]),e.getPos().add(v[0]));
				 * 
				 * 
				 * Utilities.drawLine(e.getPos().add(v[4]),e.getPos().add(v[5]));
				 * Utilities
				 * .drawLine(e.getPos().add(v[5]),e.getPos().add(v[6]));
				 * Utilities
				 * .drawLine(e.getPos().add(v[6]),e.getPos().add(v[7]));
				 * Utilities
				 * .drawLine(e.getPos().add(v[7]),e.getPos().add(v[4]));
				 * 
				 * 
				 * Utilities.drawLine(e.getPos().add(v[0]),e.getPos().add(v[4]));
				 * Utilities
				 * .drawLine(e.getPos().add(v[1]),e.getPos().add(v[5]));
				 * Utilities
				 * .drawLine(e.getPos().add(v[2]),e.getPos().add(v[6]));
				 * Utilities
				 * .drawLine(e.getPos().add(v[3]),e.getPos().add(v[7]));
				 * 
				 * 
				 * Vec3 normalx =
				 * e.getPhysicsState().orientation.getMatrix().rotateVector(new
				 * Vec3(1,0,0)).normalize(); Vec3 normaly =
				 * e.getPhysicsState().orientation.getMatrix().rotateVector(new
				 * Vec3(0,1,0)).normalize(); Vec3 normalz =
				 * e.getPhysicsState().orientation.getMatrix().rotateVector(new
				 * Vec3(0,0,1)).normalize();
				 * 
				 * Vec3 anchor = e.getPos().add(new Vec3(0,3,0));
				 * 
				 * Utilities.drawLine(anchor,anchor.add(normalx));
				 * Utilities.drawLine(anchor,anchor.add(normaly));
				 * Utilities.drawLine(anchor,anchor.add(normalz));
				 */

			}

			for (int i = 0; i < octree.masterBuckets.size(); i++) {

				Bucket buck = octree.masterBuckets.get(i);

				Vec3 p1 = buck.nodeMax;
				Vec3 p2 = buck.nodeMax.subtract(new Vec3(0, 0, buck.nodeWidth));
				Vec3 p3 = buck.nodeMax.subtract(new Vec3(buck.nodeWidth, 0,
						buck.nodeWidth));
				Vec3 p4 = buck.nodeMax.subtract(new Vec3(buck.nodeWidth, 0, 0));

				Vec3 p11 = buck.nodeMin.add(new Vec3(buck.nodeWidth, 0,
						buck.nodeWidth));
				Vec3 p22 = buck.nodeMin.add(new Vec3(buck.nodeWidth, 0, 0));
				Vec3 p33 = buck.nodeMin;
				Vec3 p44 = buck.nodeMin.add(new Vec3(0, 0, buck.nodeWidth));
				GUI.setDrawColor(new Color(Math.max(0,
						255 - buck.getDepth() * 50), 0, Math.min(255,
						buck.getDepth() * 50), 255));
				Utilities.drawLine(p1, p2);
				Utilities.drawLine(p2, p3);
				Utilities.drawLine(p3, p4);
				Utilities.drawLine(p4, p1);

				Utilities.drawLine(p11, p22);
				Utilities.drawLine(p22, p33);
				Utilities.drawLine(p33, p44);
				Utilities.drawLine(p44, p11);

				Utilities.drawLine(p1, p11);
				Utilities.drawLine(p2, p22);
				Utilities.drawLine(p3, p33);
				Utilities.drawLine(p4, p44);

			}

		}

		BaseEngineState state = this.engineStateManager().getState();
		if (state != null && state.initialized
				&& state instanceof ClientInterface)
			((ClientInterface) state).render(delta);

		this.ViewOrtho();
		GL11.glLoadIdentity();

		GL11.glDisable(GL11.GL_LIGHTING);
		if (state != null && state.initialized
				&& state instanceof ClientInterface)
			((ClientInterface) state).hudPaint();

		BGUIManager.draw();
		GL11.glEnable(GL11.GL_LIGHTING);
		this.ViewPerspective();
	}

	/**
	 * Attempt to connect to the server
	 * 
	 * @param ip
	 * @param port
	 */
	public void connectToServer(String ip, int port) {
		Socket myConnection = null;

		try {
			myConnection = new Socket(ip, port);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			System.out.println("Error connecting to server: " + e.getMessage());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Error connecting to server: " + e.getMessage());
		}
		if (myConnection == null)
			return;
		System.out.println("Client Connection Status: "
				+ myConnection.isConnected());

		network = new PlayerNetworking(myConnection, this);

	}

	private PlayerNetworking getNetwork() {
		return network;
	}

	public void messageServer(NetworkMessage msg) {
		this.getNetwork().sendMessage(msg);
	}

	private void onKeyPressed(int keyCode) {

		if (keyCode == Keyboard.KEY_ESCAPE && Mouse.isGrabbed())
			Mouse.setGrabbed(false);

		// System.out.println("key press "+keyCode);
		if (this.engineStateManager().getCurrentState().isGame()) {
			if (keyCode == Keyboard.KEY_W)
				ResourceManager.getLocalPlayer().input.up = true;
			else if (keyCode == Keyboard.KEY_S)
				ResourceManager.getLocalPlayer().input.down = true;
			if (keyCode == Keyboard.KEY_A)
				ResourceManager.getLocalPlayer().input.left = true;
			if (keyCode == Keyboard.KEY_D)
				ResourceManager.getLocalPlayer().input.right = true;
			if (keyCode == Keyboard.KEY_SPACE) {
				ResourceManager.getLocalPlayer().input.jump = true;
				if (!Mouse.isGrabbed())
					Mouse.setCursorPosition(
							(int) (ResourceManager.ScrW() * 0.5f),
							(int) (ResourceManager.ScrH() * 0.5f));
				Mouse.setGrabbed(true);
			}
		}
		this.engineStateManager().getCurrentState().onKeyPressed(keyCode);
	}

	private void onKeyReleased(int keyCode) {
		// System.out.println("key release "+keyCode);
		if (this.engineStateManager().getCurrentState().isGame()) {
			if (keyCode == Keyboard.KEY_W)
				ResourceManager.getLocalPlayer().input.up = false;
			else if (keyCode == Keyboard.KEY_S)
				ResourceManager.getLocalPlayer().input.down = false;
			if (keyCode == Keyboard.KEY_A)
				ResourceManager.getLocalPlayer().input.left = false;
			if (keyCode == Keyboard.KEY_D)
				ResourceManager.getLocalPlayer().input.right = false;
			if (keyCode == Keyboard.KEY_SPACE)
				ResourceManager.getLocalPlayer().input.jump = false;
		}

		this.engineStateManager().getCurrentState().onKeyReleased(keyCode);

	}

	private void onMousePressed(int button) {
		// System.out.println("Mouse press "+button);
		if (button == 0)
			ResourceManager.getLocalPlayer().input.attack = true;
		BPanel last = BGUIManager.lastMousedPanel;
		BPanel mousePanel = BGUIManager.getMousePanel();
		// System.out.println(mousePanel);
		if (mousePanel == null) {
			if (last != null) // we are not over any panel right now, were we
								// last frame, if so we exited that panel
			{
				last.onMouseExited();
			}
			HookManager.callHook("mousePressed", null);
		} else {
			if (last != mousePanel)// we are not over the same panel as last
									// frame, we entered the new panel
			{

				if (last != null) // if we were on a different panel last frame,
									// we exited it
				{
					last.onMouseExited();
				}
				mousePanel.onMouseEntered();
			}
			mousePanel.onMousePressed(button);
			return;
		}

		BMenu.clearMenu();

		if (this.engineStateManager().getCurrentState() == null)
			return;
		this.engineStateManager().getCurrentState().onMousePressed(button);

	}

	private void onMouseReleased(int button) {
		System.out.println("Mouse release " + button);

		if (button == 0)
			ResourceManager.getLocalPlayer().input.attack = false;
		BPanel last = BGUIManager.lastMousedPanel;
		BPanel mousePanel = BGUIManager.getMousePanel();
		if (mousePanel == null) {
			if (last != null) // we are not over any panel right now, were we
								// last frame, if so we exited that panel
			{
				last.onMouseExited();
			}
			HookManager.callHook("mouseReleased", null);
		} else {
			if (last != mousePanel)// we are not over the same panel as last
									// frame, we entered the new panel
			{

				if (last != null)// if we were on a different panel last frame,
									// we exited it
				{
					last.onMouseExited();
				}
				mousePanel.onMouseEntered();
			}
			mousePanel.onMouseReleased(button);
			return;
		}
		if (this.engineStateManager().getCurrentState() == null)
			return;
		this.engineStateManager().getCurrentState().onMouseReleased(button);

	}

	private void updateLatency(float value) {
		latencyBuffer.add(value);
		// grab a few values so we get a nice clean value for our ping
		if (latencyBuffer.size() > 45) {
			// latency = Utilities.standardDeviation(latencyBuffer);

			latency = Utilities.arraySum(latencyBuffer) / latencyBuffer.size();
			latencyBuffer.clear();
		}

	}

	private void updateEntityState(NetworkMessage msgIncoming) {

		int eID = msgIncoming.readInt();
		Vec3 newPos = msgIncoming.readVector();

		Vec3 aimVec = msgIncoming.readVector();// TODO: make this part of
												// orientation (players)
		Quaternion orientation = msgIncoming.readQuaternion();
		double inputTime = msgIncoming.readDouble(); // prediction stuff TODO
		Entity ent = EntityManager.getEntity(eID);
		if (ent == null)
			return;
		Vec3 newVel = newPos.subtract(ent.getPos()); // msgIncoming.readVector();
		// always set velocity for positional prediction
		ent.setVelocity(newVel);
		ent.setAimVector(aimVec);
		ent.setOrientation(orientation);
		ent.getMesh().recompute(orientation.getMatrix());

		if (ent == localPlayer && prediction) {
			// ent.setPos(newPos);
			// doPredictionCorrection(inputTime,newPos,ResourceManager.getLocalPlayer());
			float clientTime = EngineBase.getCurrentTime();

			float roundTripTimeSeconds = (float) (clientTime - inputTime);
			updateLatency(roundTripTimeSeconds / 2f);

		} else {
			// if (ent.getEntID()==25)
			// System.out.println("new update state "+(BaseState.getCurrentTime()-inputTime)+" secs old");

			State newState = new State(ent);
			newState.position = newPos;
			newState.orientation = orientation;
			newState.velocity = newVel;
			newState.aimVector = aimVec;
			newState.time = inputTime;

			synchronized (ent.stateBuffer) {

				ent.stateBuffer.add(0, newState);
				if (ent.stateBuffer.size() > 10)
					ent.stateBuffer.remove(ent.stateBuffer.size() - 1);

			}

			if (!interpolation || ent == localPlayer) {
				ent.setPos(newPos);
			}
		}

		// only set position if its way off from the real one

		// System.out.println("recieved entity update "+(ResourceFactory.currentTime()-inputTime));
	}

	private void doPredictionCorrection(double inputTime, Vec3 serverPos,
			Player ply) {

		State predictedState = null;
		synchronized (ply.stateBuffer) {
			if (ply.stateBuffer.size() < 1)
				return; // no buffered states to predict off of
			// our verified data starts at inputTime, data buffered from before
			// this time is irrelevant as we don't predict the past, we predict
			// the future

			while (ply.stateBuffer.size() > 0
					&& inputTime >= ply.stateBuffer.get(0).time) {
				// System.out.println(inputTime+" greater than "
				// +ply.stateBuffer.get(0).time);
				predictedState = ply.stateBuffer.remove(0);
			}
		}
		if (predictedState == null)
			return;

		Vec3 entPastPos = predictedState.position;
		Vec3 positionDifference = serverPos.subtract(entPastPos);
		double distanceApart = positionDifference.magnitude();

		// System.out.println("Prediction: C: "+entPastPos+" S: "+serverPos+" D: "+distanceApart);

		// apply corrections all the way up the stack
		if (distanceApart > 0.5) {

			int index = 0;
			while (index != ply.stateBuffer.size()) {

				State indexedState = ply.stateBuffer.get(index);

				if (indexedState == null)
					break;

				if (index == ply.stateBuffer.size() - 1) {// we have resimulated
															// the frames all
															// the way through,
															// now do the
															// correction
					positionDifference = indexedState.position
							.subtract(predictedState.position);
					distanceApart = positionDifference.magnitude();

					if (distanceApart > 2.0) {
						ply.setPos(serverPos);
						System.out.println("large correction " + distanceApart);
					} else if (distanceApart > 0.1) {
						System.out.println("small correction " + distanceApart);

						Vec3 newPos = ply.getPos().add(
								positionDifference.scale(0.1f));
						ply.setPos(newPos);
					}
				}
				indexedState.position = predictedState.position;
				indexedState.velocity = predictedState.velocity;

				index++;

			}

		} else {
			System.out.println("prediction works");
		}

	}

	private void createEntity(NetworkMessage msg) {

		int eID = msg.readInt();
		int uID = msg.readInt();
		String className = msg.readString();
		Vec3 entPos = msg.readVector();
		Vec3 entAim = msg.readVector();
		// System.out.println(className);
		if (EntityManager.getEntity(eID) != null) {
			System.out.println("ERROR: ENTITY WITH ID " + eID
					+ " ALREADY EXISTS!");
			return;
		}

		Entity newEnt = null;

		Class<?> cls = null;
		try {
			cls = Class.forName(className);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			newEnt = (Entity) cls.newInstance();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (eID == ResourceManager.getLocalPlayer().getPlayerID())
			ResourceManager.setLocalPlayer((Player) newEnt);

		newEnt.setEntID(eID);
		newEnt.uniqueID = uID;
		newEnt.setPos(entPos);
		newEnt.setAimVector(entAim);
		EntityManager.addEntity(newEnt, eID);

		System.out.println("New entity " + newEnt + " " + newEnt.getPos() + " "
				+ eID);

	}

	/*
	 * private Entity getClickedEntity() { Camera vC =
	 * ResourceManager.getViewCam(); Entity[] gEnts =
	 * EntityManager.getGlobalEntities(); for (int i=0;i<gEnts.length;i++) {
	 * 
	 * Entity loopEnt = gEnts[i]; Vec3 minsScreen =
	 * vC.worldToScreen(loopEnt.getPos().add(loopEnt.getOBBMin())); Vec3
	 * maxsScreen = vC.worldToScreen(loopEnt.getPos().add(loopEnt.getOBBMax()));
	 * 
	 * if (GUI.getMouseX() > maxsScreen.x)continue; if (GUI.getMouseX() <
	 * minsScreen.x)continue; if (GUI.getMouseY() < maxsScreen.y)continue; if
	 * (GUI.getMouseY() > minsScreen.y)continue;
	 * 
	 * return loopEnt;
	 * 
	 * } return null; }
	 */
	private void removeEntity(Entity ent) {

		int eID = ent.getEntID();

		EntityManager.removeEntity(ent);

		System.out.println("removing entity id " + eID);

	}

	/*
	 * private void drawEntities() {
	 * 
	 * Entity[] globalEnts = EntityManager.getGlobalEntities();
	 * 
	 * for (int i=0;i<globalEnts.length;i++) { Entity ent = globalEnts[i];
	 * 
	 * if (ent.getInstance() == ResourceManager.getLocalPlayer().getInstance())
	 * {
	 * 
	 * 
	 * String texturePath = ent.getTexture();
	 * 
	 * if (ent.getTexture() == null) texturePath = "resource/missing.png";
	 * 
	 * Texture tex = ResourceManager.loadTexture(texturePath);
	 * 
	 * 
	 * float angle = 0; if (!ent.getAimVector().compareTo(Vec3.origin)) angle =
	 * (float) (Math.atan2(ent.getAimVector().x,
	 * ent.getAimVector().y)*180/Math.PI);
	 * 
	 * ent.draw(screenPos,tex,angle);
	 * 
	 * 
	 * // if (ent.stateBuffer.size() > 0) //{
	 * 
	 * // screenPos = viewCam.worldToScreen(ent.stateBuffer.get(0).position); //
	 * GUI.setDrawColor(new Color(255,0,0,255)); //
	 * GUI.drawSprite(screenPos.x,screenPos
	 * .y,(ent.getOBBMax().x-ent.getOBBMin().
	 * x)*Camera.vectorSize,(ent.getOBBMax(
	 * ).y-ent.getOBBMin().y)*Camera.vectorSize,tex,angle); //} } } }
	 */

	private void onMouseDragged(int button) {
		BPanel mousePanel = BGUIManager.getMousePanel();
		if (mousePanel == null) {
			HookManager.callHook("mouseDragged", null);
		} else {
			mousePanel.onMouseDragged(button);
			return;
		}
		if (this.engineStateManager().getCurrentState() == null)
			return;
		this.engineStateManager().getCurrentState().onMouseDragged(button);

	}

	private void onMouseMoved() {
		BPanel last = BGUIManager.lastMousedPanel;
		BPanel mousePanel = BGUIManager.getMousePanel();

		if (mousePanel == null) {
			if (last != null) // we are not over any panel right now, were we
								// last frame, if so we exited that panel
			{
				last.onMouseExited();
			}
			HookManager.callHook("mouseMoved", null);
		} else {
			if (last != mousePanel)// we are not over the same panel as last
									// frame, we entered the new panel
			{

				if (last != null) // if we were on a different panel last frame,
									// we exited it
				{
					last.onMouseExited();
				}
				mousePanel.onMouseEntered();
			}

			mousePanel.onMouseMoved(0);
			return;
		}
		if (this.engineStateManager().getCurrentState() == null)
			return;
		this.engineStateManager().getCurrentState().onMouseMoved(0);

	}

	public int getWidth() {
		// TODO Auto-generated method stub
		return Display.getWidth();
	}

	public int getHeight() {
		return Display.getHeight();
	}

	/**
	 * Get the time in milliseconds
	 * 
	 * @return The system time in milliseconds
	 */
	private long getTime() {
		return (Sys.getTime() * 1000) / Sys.getTimerResolution();
	}

	private int getDelta() {
		long time = getTime();
		int delta = (int) (time - lastFrame);
		lastFrame = time;

		return delta;
	}

	public void endGame() {

	}

	public static Player getLocalPlayer() {
		// TODO Auto-generated method stub
		return localPlayer;
	}

	public void setLocalPlayer(Player newEnt) {
		localPlayer = newEnt;

	}

	public BaseEngineState getLoadState() {
		// TODO Auto-generated method stub
		return clientLoadState;
	}

	private void engineNetworkHooks() {

		NWMessageManager.addCallback(MessageConstants.ENTITY_UPDATE,
				new NWHook() {
					@Override
					public void onRun(NetworkMessage msgIncoming) {
						updateEntityState(msgIncoming);
					}
				});
		NWMessageManager.addCallback(MessageConstants.BULLET_FIRED,
				new NWHook() {
					@Override
					public void onRun(NetworkMessage msgIncoming) {
						bulletFired(msgIncoming);
					}
				});

		NWMessageManager.addCallback(MessageConstants.CREATE_ENTITY,
				new NWHook() {
					@Override
					public void onRun(NetworkMessage msgIncoming) {
						createEntity(msgIncoming);
					}
				});
		NWMessageManager.addCallback(MessageConstants.REMOVE_ENTITY,
				new NWHook() {
					@Override
					public void onRun(NetworkMessage msgIncoming) {
						removeEntity(EntityManager.getEntity(msgIncoming
								.readInt()));
					}
				});

		NWMessageManager.addCallback(MessageConstants.NWINT_SYNC, new NWHook() {
			@Override
			public void onRun(NetworkMessage msgIncoming) {

				Entity ent = EntityManager.getEntity(msgIncoming.readInt());
				if (ent == null)
					return;

				ent.setNWInt(msgIncoming.readString(), msgIncoming.readInt());

			}
		});
		NWMessageManager.addCallback(MessageConstants.NWVEC_SYNC, new NWHook() {
			@Override
			public void onRun(NetworkMessage msgIncoming) {

				Entity ent = EntityManager.getEntity(msgIncoming.readInt());
				if (ent == null)
					return;
				ent.setNWVector(msgIncoming.readString(),
						msgIncoming.readVector());

			}
		});
		NWMessageManager.addCallback(MessageConstants.NWSTRING_SYNC,
				new NWHook() {
					@Override
					public void onRun(NetworkMessage msgIncoming) {

						Entity ent = EntityManager.getEntity(msgIncoming
								.readInt());
						if (ent == null)
							return;
						ent.setNWString(msgIncoming.readString(),
								msgIncoming.readString());

					}
				});

		NWMessageManager.addCallback(MessageConstants.NWENTITY_SYNC,
				new NWHook() {
					@Override
					public void onRun(NetworkMessage msgIncoming) {

						Entity ent = EntityManager.getEntity(msgIncoming
								.readInt());
						if (ent == null)
							return;

						String key = msgIncoming.readString();
						int uniqueID = msgIncoming.readInt();
						Entity e = EntityManager.getEntityByUniqueID(uniqueID);

						ent.setNWEntity(key, e);

					}
				});

	}

}