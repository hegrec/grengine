package grengine.util;

import grengine.entity.Entity;
import grengine.physics.Vec3;
import grengine.world.WorldQuad;

public class TraceResult {
	public boolean hitWorld = false;
	public Entity hitEntity = null;
	public boolean hit = false;
	public Vec3 hitPos = null;
	public Vec3 hitNormal = null;
	public Vec3 hullOffset = new Vec3(0,0,0);
	public Vec3 start = new Vec3(0,0,0);
	public Vec3 end = new Vec3(0,0,0);
	public WorldQuad hitQuad = null;
}
