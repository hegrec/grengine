package grengine.network;

import grengine.entity.Entity;
import grengine.manager.EntityManager;
import grengine.manager.NWMessageManager;
import grengine.util.NWHook;

public class MessageConstants {

	//more packet IDs
		public static final short UNKNOWN_TYPE = 0;
		public static final short ENTITY_UPDATE = 1;
		public static final short CURTIME_SYNC = 2;
		public static final short NWINT_SYNC = 3;
		public static final short NWVEC_SYNC = 4;
		public static final short NWSTRING_SYNC = 5;
		public static final short NWENTITY_SYNC = 6;
		
		
		public static final short CLIENT_SYNCED = 28;
		public static final short CLIENT_START_LOAD = 29;		
		public static final short INITIAL_SPAWN = 30;
		public static final short INPUT_SYNC = 31;
		

		public static final short SET_LOCALPLAYER = 95;
		public static final short SYNC_CLOCK = 96;
		public static final short REQUEST_SYNC_CLOCK = 97;
		public static final short REMOVE_ENTITY = 100;
		public static final short CREATE_ENTITY = 101;
		public static final short BULLET_FIRED = 102;
		public static final short PLAYER_SAY = 103;
		

		public static final short PING_UPDATE = 562;
		



}
