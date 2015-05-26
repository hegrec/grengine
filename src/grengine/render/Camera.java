package grengine.render;

import org.lwjgl.opengl.GL11;

import grengine.manager.ResourceManager;
import grengine.physics.Vec3;

public class Camera {

	private Vec3 position = null;
	private float yaw	= 0.0f;
	private float pitch = 0.0f;
	public Camera()
	{
		position = new Vec3(0,0,0); //default to focus on the origin
	}
	public Camera(Vec3 v)
	{
		position = v; //default to focus on the origin
	}

	//increment the camera's current yaw rotation
	public void yaw(float amount)
	{
	    //increment the yaw by the amount param
	    yaw = amount;
	}
	 
	//increment the camera's current yaw rotation
	public void pitch(float amount)
	{
	    //increment the pitch by the amount param
	    pitch = amount;
	}
	public void setPos(Vec3 pos) {
		position = pos;
	}
	public void lookThrough()
    {
		GL11.glLoadIdentity();
        //roatate the pitch around the X axis
        GL11.glRotatef(pitch, -1.0f, 0.0f, 0.0f);
        //roatate the yaw around the Y axis
        GL11.glRotatef(yaw, 0.0f, -1.0f, 0.0f);
        //translate to the position vector's location
        GL11.glTranslatef(-position.x,-position.y, -position.z);
    }
	public Vec3 getPos() {
		// TODO Auto-generated method stub
		return position;
	}
	
	
	
	
	/*
	public Vec3 worldToScreen(Vec3 worldCoord)
	{
		
		int centerX = ResourceManager.ScrW()/2;
		int centerY = ResourceManager.ScrH()/2;
		Vec3 p = new Vec3();
		p.x = (centerX+((worldCoord.x-focusPoint.x)*vectorSize));
		p.y = (centerY-((worldCoord.y-focusPoint.y)*vectorSize));
		return p;
	}
	public Vec3 screenToWorld(int mouseX, int mouseY) {
		// TODO Auto-generated method stub
		int centerX = ResourceManager.ScrW()/2;
		int centerY = ResourceManager.ScrH()/2;
		Vec3 vec = new Vec3();
		vec.x = focusPoint.x+((mouseX-centerX)/vectorSize);
		vec.y = focusPoint.y+((centerY-mouseY)/vectorSize);
		return vec;
	}*/
	
}
