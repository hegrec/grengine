package grengine.appstate;

import grengine.entity.Entity;
import grengine.gamestate.BaseEngineState;
import grengine.gamestate.GameStateData;
import grengine.gamestate.ServerLoadState;
import grengine.manager.CommandManager;
import grengine.manager.EntityManager;
import grengine.manager.HookManager;
import grengine.manager.NWMessageManager;
import grengine.network.ConnectionMon;
import grengine.network.MessageConstants;
import grengine.network.NetworkMessage;
import grengine.physics.PhysicsIntegrator;
import grengine.physics.Vec3;
import grengine.player.Player;
import grengine.player.PlayerNetworkOut;
import grengine.player.PlayerNetworking;
import grengine.stateloader.SClientLoader;
import grengine.util.ConCommand;
import grengine.util.Hook;
import grengine.util.NWHook;
import grengine.util.Profiler;
import grengine.util.TraceResult;
import grengine.util.Utilities;
import grengine.weapon.BaseWeapon;
import grengine.world.Octree;

import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public abstract class EngineServer extends EngineBase {

	private static boolean closing = false; // are we shutting down
	private BaseEngineState serverGameState;
	private final Octree octree;
	private final SClientLoader sClientLoader;

	public EngineServer(int port) {
		super();
		EngineBase.SERVER = true;
		this.PORT = port;
		octree = new Octree();
		sClientLoader = new SClientLoader(this);
	}

	public void setServerGameState(BaseEngineState serverGameState) {
		this.serverGameState = serverGameState;
	}

	@Override
	public void initializeEngine() {
		System.out.println(">> Starting " + ENGINE_NAME + " " + ENGINE_VERSION);
		if (this.serverGameState == null) {
			System.out
					.println("Error: Server Game State Undefined. Did you call EngineServer.setServerGameState? Exiting...");
			return;
		}

		float dist = new Vec3(0.6, 0.8, 0).DotProduct(new Vec3(10, 4 - 5, 10));
		System.out.println("DIST " + dist);

		GameStateData d = new GameStateData();
		d.addData("serverGameState", this.serverGameState);
		d.addData("serverEngine", this);
		this.engineStateManager().changeState(new ServerLoadState(d));

	}

	public void startServer() {
		registerCommands();
		registerHooks();
		registerNetworkHooks();

		try {
			// create the main socket for all incoming connections
			ConnectionMon.get().initialize(this, this.PORT);

			// start listening for console commands
			CommandManager.get().start();

		} catch (Exception e) {
			System.out.println(e);
		}

		float newTime = 0.0f;
		long currentMillis = System.currentTimeMillis();
		// start the first tick value
		long lastTick = currentMillis;

		long lastSecond = lastTick;
		int frames = 0;

		int fpsGap = 1000 / EngineBase.TICKRATE;
		float inverseTickRate = 1f / EngineBase.TICKRATE;
		double accum = 0;
		while (true) {
			currentMillis = System.currentTimeMillis();
			// calculate the total frames elapsed in the past second (fps)
			if (currentMillis > lastSecond + 1000) {
				currentFPS = frames;
				frames = 0;
				lastSecond = currentMillis;
				// System.out.println(currentFPS);

			}

			if (currentMillis > lastTick + fpsGap)// &&
													// ((EntityManager.getGlobalPlayerCount()
													// > 0)))
			{
				float frameTime = (currentMillis - lastTick) / 1000f;
				lastTick = currentMillis;
				currentTime += frameTime;
				// check for lagged out players
				validateConnections(frameTime);

				octree.queryScene();

				Entity ents[] = EntityManager.getAll();

				for (int i = 0; i < ents.length; i++) {
					Entity ent = ents[i];
					if (ent == null)
						continue;
					// remove entities that need to be removed this frame
					if (ent.remove) {
						removeEntity(ent);
						continue;
					}

					// make the entities think
					if (ent.shouldThink()) {
						if (getCurrentTime() > ent.getNextThink()) {
							ent.think();
						}
					}
					// GRAVITY FUN!
					/*
					 * if (!ent.isPlayer()) { Vec3 grav = new Vec3(0,0,0); for
					 * (int ii=0;ii<ents.length;ii++) { if (ents[ii] == null ||
					 * ents[ii].isPlayer() || ii == i) continue; Vec3 newPos =
					 * ents[ii].getPos(); Vec3 deltaPos =
					 * newPos.subtract(ent.getPos());
					 * 
					 * float gravMag =
					 * 1+(ents[ii].getMass()*ent.getMass())/deltaPos
					 * .magnitudeSquared();
					 * 
					 * grav = grav.add(deltaPos.normalize().scale(gravMag)); }
					 * ent.applyForce(grav); }
					 */

				}

				// Profiler.start();
				// perform a physics iteration

				// we want to be SURE we run the same exact amount of physics
				// integrations per second

				// System.out.println(frameTime);
				accum += frameTime;

				while (accum >= inverseTickRate) {
					PhysicsIntegrator.simulate(inverseTickRate);
					accum -= inverseTickRate;
					// System.out.println(accum + " " + inverseTickRate);
				}
				// Profiler.dump();
				/*
				 * Player[] players = EntityManager.getGlobalPlayers(); for (int
				 * i=0;i<players.length;i++) { NetworkMessage ping = new
				 * NetworkMessage();
				 * ping.writeShort(MessageConstants.PING_UPDATE);
				 * ping.writeFloat(getCurrentTime());
				 * players[i].sendMessage(ping);
				 * 
				 * }
				 */

				// update entity data and finally send all network data buffered
				// this frame
				updateNWVariables();
				updateEntityStates();
				frames++;
			}
			try {
				Thread.sleep(5);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			if (closing) {
				System.out.println("Shutting down Server...");
				break;
			}
		}
		ConnectionMon.get().close();
		CommandManager.get().close();
		System.exit(0);
	}

	/**
	 * This function will remove worldchunks that have no nearby players and add
	 * new ones if needed
	 */

	private void updateEntityStates() {
		Entity ents[] = EntityManager.getAll();
		Player players[] = EntityManager.getAllPlayers();

		for (int index = 0; index < ents.length; index++) {
			Entity ent = ents[index];

			if (ent == null)
				continue;

			if (!ent.currentState.equals(ent.lastState)) {
				// System.out.println("Sending update");
				ent.lastState.position = ent.currentState.position;
				ent.lastState.velocity = ent.currentState.velocity;
				ent.lastState.orientation = ent.currentState.orientation;

				NetworkMessage msg = new NetworkMessage();
				msg.writeShort(MessageConstants.ENTITY_UPDATE);
				msg.writeInt(ent.getEntID());
				msg.writeVector(ent.currentState.position);
				// msg.writeVector(ent.currentState.velocity);
				msg.writeVector(ent.getAimVector());
				msg.writeQuaternion(ent.currentState.orientation);
				if (ent.isPlayer())
					msg.writeDouble(((Player) ent).lastInputTime);
				else
					msg.writeDouble(getCurrentTime());

				for (int i = 0; i < players.length; i++) {
					// if (players[i].getPos().distanceSquared(ent.getPos()) <
					// WorldManager.chunkSize*WorldManager.chunkSize)
					if (players[i] != ent) // players are sent info of
											// themselves elsewhere
						players[i].sendMessage(msg);
				}
			}
		}

	}

	private void updateEntityStates(Entity updated) {

		if (!updated.getAimVector().compareTo(updated.lastAimVector())
				|| !updated.currentState.equals(updated.lastState)) {

			// if (!updated.lastState.velocity.compareTo(new Vec2(0,0)) &&
			// updated.currentState.velocity.compareTo(new Vec2(0,0)))
			// System.out.println("Entity stopped moving");

			// System.out.println("Sending entity update");
			updated.lastState.position = updated.currentState.position;
			updated.lastState.velocity = updated.currentState.velocity;
			updated.lastState.orientation = updated.currentState.orientation;

			// System.out.println(updated.currentState.position);
			NetworkMessage msg = new NetworkMessage();
			msg.writeShort(MessageConstants.ENTITY_UPDATE);
			msg.writeInt(updated.getEntID());
			msg.writeVector(updated.currentState.position);
			// msg.writeVector(updated.currentState.velocity);
			msg.writeVector(updated.getAimVector());
			msg.writeQuaternion(updated.currentState.orientation);
			if (updated.isPlayer())
				msg.writeDouble(((Player) updated).lastInputTime);
			else
				msg.writeDouble(getCurrentTime());

			msg.writeInput(updated.input);
			Player players[] = EntityManager.getAllPlayers();
			for (int i = 0; i < players.length; i++) {
				// if (players[i].getPos().distanceSquared(updated.getPos()) <
				// WorldManager.chunkSize*WorldManager.chunkSize)
				players[i].sendMessage(msg);
			}
		}
	}

	private void removeEntity(Entity ent) {

		int eID = ent.getEntID();

		EntityManager.removeEntity(ent);

		NetworkMessage msg = new NetworkMessage();
		msg.writeShort(MessageConstants.REMOVE_ENTITY);
		msg.writeInt(eID);

		Player plys[] = EntityManager.getAllPlayers();

		for (int i = 0; i < plys.length; i++) {
			plys[i].sendMessage(msg);
		}

		System.out.println("removing entity id " + eID);

	}

	public void validateConnections(float delta) {

		Player plys[] = EntityManager.getAllPlayers();

		for (int i = 0; i < plys.length; i++) {
			Player ply = plys[i];
			ply.lagTime += delta;
			ply.getNetworkHandler().newFrame();
			if (plys[i].lagTime > 20) // lagtime is reset when a network message
										// is received
			{

				ply.disconnect();
				ply.remove();

				System.out.println("Player #" + ply.getPlayerID()
						+ " has lagged out! Dropped player from server...");
			}
		}

	}

	private void updateNWVariables() {
		Entity ents[] = EntityManager.getAll();
		Player plys[] = EntityManager.getAllPlayers();

		for (int i = 0; i < ents.length; i++) {
			Entity ent = ents[i];
			if (ent == null)
				continue;
			for (Map.Entry<String, Integer> entry : ent.nwVars.intFrameMap
					.entrySet()) {
				for (int pID = 0; pID < plys.length; pID++) {
					Player ply = plys[pID];

					NetworkMessage msg = new NetworkMessage();
					msg.writeShort(MessageConstants.NWINT_SYNC);
					msg.writeInt(ents[i].getEntID()); // ent id
					msg.writeString(entry.getKey()); // dName
					msg.writeInt(entry.getValue()); // dData

					ply.sendMessage(msg);
				}

			}
			for (Map.Entry<String, String> entry : ent.nwVars.stringFrameMap
					.entrySet()) {
				for (int pID = 0; pID < plys.length; pID++) {
					Player ply = plys[pID];

					NetworkMessage msg = new NetworkMessage();
					msg.writeShort(MessageConstants.NWSTRING_SYNC);
					msg.writeInt(ents[i].getEntID()); // ent id
					msg.writeString(entry.getKey()); // dName
					msg.writeString(entry.getValue()); // dData

					ply.sendMessage(msg);
				}

			}
			for (Map.Entry<String, Vec3> entry : ent.nwVars.vecFrameMap
					.entrySet()) {
				for (int pID = 0; pID < plys.length; pID++) {
					Player ply = plys[pID];
					NetworkMessage msg = new NetworkMessage();
					msg.writeShort(MessageConstants.NWVEC_SYNC);
					msg.writeInt(ents[i].getEntID()); // eid
					msg.writeString(entry.getKey()); // dName
					msg.writeVector(entry.getValue()); // dData

					ply.sendMessage(msg);
				}
			}
			for (Map.Entry<String, Integer> entry : ent.nwVars.entityFrameMap
					.entrySet()) {
				for (int pID = 0; pID < plys.length; pID++) {
					Player ply = plys[pID];
					NetworkMessage msg = new NetworkMessage();
					msg.writeShort(MessageConstants.NWENTITY_SYNC);
					msg.writeInt(ents[i].getEntID()); // eid
					msg.writeString(entry.getKey()); // dName
					msg.writeInt(entry.getValue()); // dData

					ply.sendMessage(msg);
				}
			}

			ent.nwVars.clearFrame();
		}

	}

	private void updateNWVariables(Entity ent, Player ply) {

		for (Map.Entry<String, Integer> entry : ent.nwVars.intMap.entrySet()) {

			NetworkMessage msg = new NetworkMessage();
			msg.writeShort(MessageConstants.NWINT_SYNC);
			msg.writeInt(ent.getEntID()); // ent id
			msg.writeString(entry.getKey()); // dName
			msg.writeInt(entry.getValue()); // dData

			ply.sendMessage(msg);

		}
		for (Map.Entry<String, String> entry : ent.nwVars.stringMap.entrySet()) {

			NetworkMessage msg = new NetworkMessage();
			msg.writeShort(MessageConstants.NWSTRING_SYNC);
			msg.writeInt(ent.getEntID()); // ent id
			msg.writeString(entry.getKey()); // dName
			msg.writeString(entry.getValue()); // dData

			ply.sendMessage(msg);

		}
		for (Map.Entry<String, Vec3> entry : ent.nwVars.vecMap.entrySet()) {
			NetworkMessage msg = new NetworkMessage();
			msg.writeShort(MessageConstants.NWVEC_SYNC);
			msg.writeInt(ent.getEntID()); // eid
			msg.writeString(entry.getKey()); // dName
			msg.writeVector(entry.getValue()); // dData

			ply.sendMessage(msg);
		}
		for (Map.Entry<String, Integer> entry : ent.nwVars.entityMap.entrySet()) {
			NetworkMessage msg = new NetworkMessage();
			msg.writeShort(MessageConstants.NWENTITY_SYNC);
			msg.writeInt(ent.getEntID()); // eid
			msg.writeString(entry.getKey()); // dName
			msg.writeInt(entry.getValue()); // dData

			ply.sendMessage(msg);
		}

	}

	public void onNewConnection(Socket incoming) {

		// initialize the player's network handler
		PlayerNetworking playerConnection = new PlayerNetworking(incoming, this);

		// create the player
		Player newPlayer = new Player(playerConnection);

		EntityManager.spawnEntity(newPlayer);
		System.out.println("Player has connected (" + newPlayer.getIPAddress()
				+ ") ID #" + newPlayer.getPlayerID());
		// send info about all entities
		sendFullUpdate(newPlayer);
		sClientLoader.startClientLoad(newPlayer); // this will cause the player
													// to sync and download the
													// state of the server, then
													// they will spawn
		newPlayer.setPos(new Vec3(0, 10, 0));

	}

	public void onSpawnedEntity(Entity ent) {
		// System.out.println("SERVERSTATE: NEW ENTITY SPAWNED!");
		Player[] plys = EntityManager.getAllPlayers();
		for (int i = 0; i < plys.length; i++) {
			informOfEntity(plys[i], ent);
		}

	}

	// send the entire state of all entities
	public void sendFullUpdate(Player ply) {
		Entity ents[] = EntityManager.getAll();
		for (int i = 0; i < ents.length; i++) {
			if (ents[i] == null)
				continue;
			if (ents[i] != ply) {
				informOfEntity(ply, ents[i]);
			}
			updateNWVariables(ents[i], ply);
		}
	}

	// notify the player of a new entity to be created on their client
	private void informOfEntity(Player ply, Entity entity) {
		NetworkMessage msg = new NetworkMessage();
		msg.writeShort(MessageConstants.CREATE_ENTITY);

		msg.writeInt(entity.getEntID()); // eid
		msg.writeInt(entity.uniqueID);

		msg.writeString(entity.getClass().getCanonicalName()); // entity class
		msg.writeVector(entity.getPos()); // position
		msg.writeVector(entity.getAimVector());
		// System.out.println("Entity Pos: "+entity.getPos());
		ply.sendMessage(msg);

	}

	// as long as no more than first float is read it should not break!
	private void receiveInput(NetworkMessage msgIncoming, Player ply) {

		float inputTime = msgIncoming.readFloat();
		// System.out.println(time+" "+connectionOwner.getPlayer().lastInputTime);

		// System.out.println("READING" + " " + inputTime);
		if (inputTime < ply.lastInputTime)
			return;
		ply.lagTime = 0f;
		double deltaTime = inputTime - ply.lastInputTime;
		float yawChange = msgIncoming.readFloat();
		float pitchChange = msgIncoming.readFloat();
		// we allow the client to specify the delta so they should get the same
		// calculations as the server
		double uncheckedDelta = msgIncoming.readDouble();
		// we clamp the uncheckedDelta to be a min of 0, and a max of the time
		// elapsed since the last
		deltaTime = (float) Math.max(0, Math.min(uncheckedDelta, deltaTime));

		ply.addYaw(yawChange);
		ply.addPitch(pitchChange);
		ply.lastInput = ply.input;
		ply.input = msgIncoming.readInput();
		ply.lastInputTime = inputTime;

		Vec3 aimVec = new Vec3(
				Math.cos((ply.getYaw() + 90) * Utilities.DEG2RAD)
						* (1 - Math.abs(Math.sin(ply.getPitch()
								* Utilities.DEG2RAD))), Math.sin(ply.getPitch()
						* Utilities.DEG2RAD), Math.cos((ply.getYaw() + 180)
						* Utilities.DEG2RAD)
						* (1 - Math.abs(Math.sin(ply.getPitch()
								* Utilities.DEG2RAD))));
		ply.setAimVector(aimVec);

		// player wants to shoot his weapon

		if (ply.input.attack)
			playerAttack(ply, inputTime);
		else {
			if (ply.holdingEnt != null)
				ply.holdingEnt.held = false;
			ply.holdingEnt = null;

		}
		if (ply.holdingEnt != null) {

			Vec3 newPos = ply.eyePos().add(
					ply.getAimVector().scale(ply.holdDist));
			Vec3 deltaPos = newPos.subtract(ply.holdingEnt.getPos());

			ply.holdingEnt.applyImpulse(deltaPos.scale(deltaPos
					.magnitudeSquared() / 100f));

		}

		if (ply.input.use && !ply.lastInput.use) {
			// Entity useEnt = Utilities.traceLine(ply.getPos(),
			// ply.getPos().add(ply.getAimVector().scale(3f)), ply);
			// if (useEnt != null)
			// useEnt.onUse(ply);
		}
		// System.out.println(deltaTime);
		// System.out.println("NEW INPUT!");
		PhysicsIntegrator.simulate((float) deltaTime, ply);
		updateEntityStates(ply);
	}

	private void playerAttack(Player ply, double inputTime) {

		BaseWeapon playerWep = ply.getActiveWeapon();

		// if (playerWep == null) return;
		// if (playerWep.getNextShot() > getCurrentTime()) return;

		// playerWep.setNextShot((float)
		// (getCurrentTime()+playerWep.getFireRate()));

		// TODO: Go back in time via stored moves serverside to the time of
		// firing (inputTime)

		Vec3 lineStart = ply.eyePos().add(ply.getAimVector());
		Vec3 lineEnd = ply.eyePos().add(ply.getAimVector().scale(10));

		HashMap<Object, Boolean> filter = new HashMap<Object, Boolean>();
		filter.put(ply, true);
		TraceResult hitTrace = Utilities.traceLine(lineStart, lineEnd, filter);

		if (hitTrace.hitEntity != null && ply.holdingEnt == null) {
			ply.holdDist = hitTrace.hitPos.distance(ply.eyePos());
			hitTrace.hitEntity.held = true;
			ply.holdingEnt = hitTrace.hitEntity;

		}

	}

	private void entityTakeDamage(Entity hitEnt, int damage) {

		if (!hitEnt.takesDamage())
			return;

		int newHP = hitEnt.getHealth() - damage;
		if (newHP < 0)
			newHP = 0;
		hitEnt.setHealth(newHP);
		if (hitEnt.getHealth() <= 0) {
			hitEnt.remove();
			System.out.println(hitEnt + " has died");
		}
	}

	private void playerSay(Player ply, String message) {
		int plyID = -1;
		String playerName = "Console: ";
		if (ply != null) {
			plyID = ply.getPlayerID();
			playerName = ply.getName();
		}
		NetworkMessage msg = new NetworkMessage();
		msg.writeShort(MessageConstants.PLAYER_SAY);
		msg.writeInt(plyID);
		msg.writeString(message);

		Player[] plys = EntityManager.getAllPlayers();

		for (int index = 0; index < plys.length; index++)
			plys[index].sendMessage(msg);

		System.out.println(playerName + message);
	}

	public static void shutdown() {
		closing = true;
	}

	private void registerHooks() {

		// add various hooks
		HookManager.addHook("spawnedEntity", new Hook() {
			@Override
			public void onRun(Object[] args) {
				onSpawnedEntity((Entity) args[0]);
			}
		});

	}

	private void registerCommands() {

		// register various console commands
		CommandManager.registerConCommand("fps", new ConCommand() {
			@Override
			public void onRan(String[] args) {
				System.out.println("Current FPS: " + currentFPS);
			}
		});
		CommandManager.registerConCommand("sethealth", new ConCommand() {
			@Override
			public void onRan(String[] args) {
				EntityManager.getEntity(Integer.parseInt(args[1])).setHealth(
						Integer.parseInt(args[2]));
			}
		});

		CommandManager.registerConCommand("say", new ConCommand() {
			@Override
			public void onRan(String[] args)

			{
				String message = "";
				for (int arg = 1; arg < args.length; arg++)
					message += " " + args[arg];

				playerSay(null, message);

			}

		});

		CommandManager.registerConCommand("nrp", new ConCommand() {
			@Override
			public void onRan(String[] args)

			{
				HashMap<Short, Integer> map = PlayerNetworkOut.nwProfiler;

				for (Map.Entry<Short, Integer> entry : map.entrySet()) {
					System.out.println("Message ID: " + entry.getKey()
							+ " KBytes: " + (entry.getValue() / 1024f)
							+ " KB/s: "
							+ ((entry.getValue() / 1024f) / getCurrentTime()));

				}

			}

		});

		CommandManager.registerConCommand("te", new ConCommand() {
			@Override
			public void onRan(String[] args)

			{
				Player ply = (Player) EntityManager.getEntity(0);
				if (ply != null) {
					TraceResult tr = Utilities.traceEntityLine(ply.eyePos(),
							ply.eyePos().add(ply.getAimVector().scale(20f)),
							null);

					System.out.println(tr.start);
					System.out.println(tr.end);
					System.out.println(tr.hit);
					System.out.println(tr.hitWorld);
					System.out.println(tr.hitPos);
					System.out.println(tr.hitNormal);
					System.out.println(tr.hitEntity);
				}

			}

		});

		CommandManager.registerConCommand("bump", new ConCommand() {
			@Override
			public void onRan(String[] args)

			{
				Entity[] ents = EntityManager.getAll();
				for (int i = 0; i < ents.length; i++) {

					Entity e = ents[i];
					if (e == null)
						continue;
					if (!e.isPlayer()) {
						// e.setPos(new Vec3(5,5,5)); {
						e.applyForce(new Vec3(Math.random() * 1600f - 800f,
								Math.random() * 600f,
								Math.random() * 1600f - 800f).scale(e.getMass()));
						// e.applyForceOffset(new
						// Vec3(Math.random()*600f-300f,Math.random()*300f,Math.random()*600f-300f));
					}

				}

			}

		});

		CommandManager.registerConCommand("tt", new ConCommand() {
			@Override
			public void onRan(String[] args)

			{
				Player ply = (Player) EntityManager.getEntity(0);
				if (ply != null) {
					TraceResult tr = Utilities.traceWorldLine(ply.eyePos(), ply
							.eyePos().add(ply.getAimVector().scale(20f)), null);

					System.out.println(tr.start);
					System.out.println(tr.end);
					System.out.println(tr.hit);
					System.out.println(tr.hitWorld);
					System.out.println(tr.hitPos);
					System.out.println(tr.hitNormal);
				}

			}

		});
		CommandManager.registerConCommand("octest", new ConCommand() {
			@Override
			public void onRan(String[] args)

			{
				Profiler.start();
				Entity[] ents = EntityManager.getAll();
				for (int i = 0; i < ents.length; i++) {
					Entity ent = ents[i];
					if (ent == null)
						continue;
					octree.searchEntities(ent.getPos().add(ent.getAABBMax()),
							ent.getPos().add(ent.getAABBMin()));
				}
				Profiler.dump();
			}

		});

		CommandManager.registerConCommand("killserver", new ConCommand() {
			@Override
			public void onRan(String[] args)

			{
				closing = true;

			}

		});

	}

	private void registerNetworkHooks() {

		NWMessageManager.addCallback(MessageConstants.INPUT_SYNC, new NWHook() {
			@Override
			public void onRun(Player ply, NetworkMessage msgIncoming) {
				receiveInput(msgIncoming, ply);
			}
		});

	}

}
