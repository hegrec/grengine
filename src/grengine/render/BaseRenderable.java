package grengine.render;

import grengine.manager.ResourceManager;
import grengine.physics.Vec3;
import grengine.resource.Texture;

import org.lwjgl.opengl.GL11;

/**
 * BaseRenderable.java
 * The basic object rendered in 3D space, this includes all 3D sprites and models.
 * @author Chris
 *
 */
public abstract class BaseRenderable {
	protected Vec3 pos;
	protected Vec3 scale;
	protected static Texture tex = ResourceManager.getTexture("resource/crate0_diffuse.png");
	public BaseRenderable() {
		pos = new Vec3(0,0,0);
		scale = new Vec3(1,1,1);
	}
	public abstract void render();
	public void setPos(Vec3 pos) {
		this.pos = pos;
		
	}
	public void setScale(Vec3 vec3) {
		this.scale = vec3;
		 
	}

}
