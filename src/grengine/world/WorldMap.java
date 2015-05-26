package grengine.world;

import grengine.manager.ResourceManager;
import grengine.physics.Vec3;
import grengine.resource.Texture;

import org.lwjgl.opengl.GL11;

public class WorldMap {

	public static WorldQuad[] worldData = {

			// Skybox

			// top
			// xz
			new WorldQuad(new Vec3(-40, 40, -40), new Vec3(40, 40, -40),
					new Vec3(40, 40, 40), new Vec3(-40, 40, 40),
					"resource/water.png"),
			// sides

			// xy
			new WorldQuad(new Vec3(-40, 10, -40), new Vec3(40, 10, -40),
					new Vec3(40, 40, -40), new Vec3(-40, 40, -40),
					"resource/water.png"),
			new WorldQuad(new Vec3(-40, 10, 40), new Vec3(-40, 40, 40),
					new Vec3(40, 40, 40), new Vec3(40, 10, 40),
					"resource/water.png"),
			// zy
			new WorldQuad(new Vec3(40, 10, -40), new Vec3(40, 10, 40),
					new Vec3(40, 40, 40), new Vec3(40, 40, -40),
					"resource/water.png"),
			new WorldQuad(new Vec3(-40, 10, -40), new Vec3(-40, 40, -40),
					new Vec3(-40, 40, 40), new Vec3(-40, 10, 40),
					"resource/water.png"),
			// concrete
			// xy
			new WorldQuad(new Vec3(-40, 0, -40), new Vec3(40, 0, -40),
					new Vec3(40, 10, -40), new Vec3(-40, 10, -40),
					"resource/sidewalk.png"),
			new WorldQuad(new Vec3(-40, 0, 40), new Vec3(-40, 10, 40),
					new Vec3(40, 10, 40), new Vec3(40, 0, 40),
					"resource/sidewalk.png"),
			// zy
			new WorldQuad(new Vec3(40, 0, -40), new Vec3(40, 0, 40), new Vec3(
					40, 10, 40), new Vec3(40, 10, -40), "resource/sidewalk.png"),
			new WorldQuad(new Vec3(-40, 0, -40), new Vec3(-40, 10, -40),
					new Vec3(-40, 10, 40), new Vec3(-40, 0, 40),
					"resource/sidewalk.png"),
			//
			// //raised edge
			// new WorldQuad(new Vec3(-40,5,30),new Vec3(-40,5,40),new
			// Vec3(40,5,40),new
			// Vec3(40,5,30),"resource/sidewalk.png"),
			// new WorldQuad(new Vec3(-40,5,-40),new Vec3(-40,5,-30),new
			// Vec3(40,5,-30),new Vec3(40,5,-40),"resource/sidewalk.png"),
			//
			// new WorldQuad(new Vec3(-40,5,-30),new Vec3(-40,5,30),new
			// Vec3(-30,5,30),new Vec3(-30,5,-30),"resource/sidewalk.png"),
			// new WorldQuad(new Vec3(30,5,-30),new Vec3(30,5,30),new
			// Vec3(40,5,30),new
			// Vec3(40,5,-30),"resource/sidewalk.png"),
			//
			//
			// //ramps to raised edge
			// new WorldQuad(new Vec3(-30,5,-8),new Vec3(-15,0,-8),new
			// Vec3(-15,0,-16),new Vec3(-30,5,-16),"resource/sidewalk.png"),
			// new WorldQuad(new Vec3(-30,5,16),new Vec3(-15,0,16),new
			// Vec3(-15,0,8),new
			// Vec3(-30,5,8),"resource/sidewalk.png"),
			//
			// new WorldQuad(new Vec3(15,0,-8),new Vec3(30,5,-8),new
			// Vec3(30,5,-16),new
			// Vec3(15,0,-16),"resource/sidewalk.png"),
			// new WorldQuad(new Vec3(15,0,16),new Vec3(30,5,16),new
			// Vec3(30,5,8),new
			// Vec3(15,0,8),"resource/sidewalk.png"),
			//
			//
			// //center ramp
			// new WorldQuad(new Vec3(-30,5,-4),new Vec3(-30,5,4),new
			// Vec3(-20,7,4),new
			// Vec3(-20,7,-4),"resource/sidewalk.png"),
			// new WorldQuad(new Vec3(-20,7,-4),new Vec3(-20,7,4),new
			// Vec3(20,7,4),new
			// Vec3(20,7,-4),"resource/sidewalk.png"),
			// new WorldQuad(new Vec3(20,7,-4),new Vec3(20,7,4),new
			// Vec3(30,5,4),new
			// Vec3(30,5,-4),"resource/sidewalk.png"),
			//
			// //grass
			new WorldQuad(new Vec3(-40, 3, -40), new Vec3(-40, 3, 40),
					new Vec3(40, 3, 40), new Vec3(40, 3, -40),
					"resource/grass.png"),

	};

	public void render() {

		for (int i = 0; i < worldData.length; i++) {

			WorldQuad q = worldData[i];
			Texture texture = ResourceManager.getTexture(q.getTexture());
			// Vec3 ws =
			// ResourceManager.getViewCam().cameraSpaceToWorldSpace(q.centerPoint);

			GL11.glPushMatrix();
			// System.out.println(ws);
			// GL11.glTranslatef(ws.x, ws.y, ws.z);
			GL11.glEnable(GL11.GL_TEXTURE_2D);
			texture.bind();
			GL11.glBegin(GL11.GL_QUADS);
			// GL11.glColor3f(q.col.getRed()/255f,q.col.getGreen()/255f,q.col.getBlue()/255f);
			GL11.glColor3f(1f, 1f, 1f);

			GL11.glNormal3f(q.plane.normal.x, q.plane.normal.y,
					q.plane.normal.z);
			GL11.glTexCoord2f(0, 0);
			GL11.glVertex3f(q.point1.x, q.point1.y, q.point1.z);
			GL11.glNormal3f(q.plane.normal.x, q.plane.normal.y,
					q.plane.normal.z);
			GL11.glTexCoord2f(0, 1);
			GL11.glVertex3f(q.point2.x, q.point2.y, q.point2.z);
			GL11.glNormal3f(q.plane.normal.x, q.plane.normal.y,
					q.plane.normal.z);
			GL11.glTexCoord2f(1, 1);
			GL11.glVertex3f(q.point3.x, q.point3.y, q.point3.z);
			GL11.glNormal3f(q.plane.normal.x, q.plane.normal.y,
					q.plane.normal.z);
			GL11.glTexCoord2f(1, 0);
			GL11.glVertex3f(q.point4.x, q.point4.y, q.point4.z);
			GL11.glEnd();
			GL11.glDisable(GL11.GL_TEXTURE_2D);

			GL11.glPopMatrix();
		}

	}

	public static Vec3 getMax() {

		float maxX = -99999;
		float maxY = -99999;
		float maxZ = -99999;

		for (int i = 0; i < worldData.length; i++) {

			WorldQuad q = worldData[i];
			if (q.point1.x > maxX)
				maxX = q.point1.x;
			if (q.point2.x > maxX)
				maxX = q.point2.x;
			if (q.point3.x > maxX)
				maxX = q.point3.x;
			if (q.point4.x > maxX)
				maxX = q.point4.x;

			if (q.point1.y > maxY)
				maxY = q.point1.y;
			if (q.point2.y > maxY)
				maxY = q.point2.y;
			if (q.point3.y > maxY)
				maxY = q.point3.y;
			if (q.point4.y > maxY)
				maxY = q.point4.y;

			if (q.point1.z > maxZ)
				maxZ = q.point1.z;
			if (q.point2.z > maxZ)
				maxZ = q.point2.z;
			if (q.point3.z > maxZ)
				maxZ = q.point3.z;
			if (q.point4.z > maxZ)
				maxZ = q.point4.z;
		}
		return new Vec3(maxX, maxY, maxZ);
	}

	public static Vec3 getMin() {

		float minX = 99999;
		float minY = 99999;
		float minZ = 99999;

		for (int i = 0; i < worldData.length; i++) {

			WorldQuad q = worldData[i];
			if (q.point1.x < minX)
				minX = q.point1.x;
			if (q.point2.x < minX)
				minX = q.point2.x;
			if (q.point3.x < minX)
				minX = q.point3.x;
			if (q.point4.x < minX)
				minX = q.point4.x;

			if (q.point1.y < minY)
				minY = q.point1.y;
			if (q.point2.y < minY)
				minY = q.point2.y;
			if (q.point3.y < minY)
				minY = q.point3.y;
			if (q.point4.y < minY)
				minY = q.point4.y;

			if (q.point1.z < minZ)
				minZ = q.point1.z;
			if (q.point2.z < minZ)
				minZ = q.point2.z;
			if (q.point3.z < minZ)
				minZ = q.point3.z;
			if (q.point4.z < minZ)
				minZ = q.point4.z;
		}
		return new Vec3(minX, minY, minZ);
	}
}
