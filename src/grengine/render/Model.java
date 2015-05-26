package grengine.render;

import grengine.manager.ResourceManager;
import grengine.physics.Vec3;
import grengine.resource.Texture;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import org.lwjgl.opengl.GL11;

public class Model {

	
	
	private ArrayList<Float> verts; //define your model using origin based vertices
	private float[] vertA;
	private float[] tc;
	private float[] norm;
	private ArrayList<Float> textureCoords;
	private ArrayList<Float> normals;
	public Model() {
		
		
		verts = new ArrayList<Float>(); //our format uses 3 floats ( x y z ) per Vec3
		textureCoords = new ArrayList<Float>(); //our format uses 3 floats ( x y z ) per Vec3
		normals = new ArrayList<Float>(); //our format uses 3 floats ( x y z ) per Vec3

	}
	
	public void addVertex(Vec3 vPos,float[] texCoord) {
		verts.add(vPos.x);
		verts.add(vPos.y);
		verts.add(vPos.z);
		textureCoords.add(texCoord[0]);
		textureCoords.add(texCoord[1]);
	}
	 
	//pass in localspace coordinates
	public void addQuad(Vec3 l1,Vec3 l2,Vec3 l3, Vec3 l4) {
		
		addVertex(l1,new float[] {0,0});
		addVertex(l2,new float[] {0,1});
		addVertex(l3,new float[] {1,1});
		addVertex(l4,new float[] {1,0});
		
		
		Vec3 normal = l2.subtract(l1).CrossProduct(l3.subtract(l2));
		normals.add(normal.x);
		normals.add(normal.y);
		normals.add(normal.z);
		normals.add(normal.x);
		normals.add(normal.y);
		normals.add(normal.z);
		normals.add(normal.x);
		normals.add(normal.y);
		normals.add(normal.z);
		normals.add(normal.x);
		normals.add(normal.y);
		normals.add(normal.z);
		
		
		
		vertA = new float[verts.size()];
		for (int i=0;i<verts.size();i++) {
			vertA[i] = verts.get(i);
		}
		
		
		tc = new float[textureCoords.size()];
		for (int i=0;i<textureCoords.size();i++) {
			tc[i] = textureCoords.get(i);
		}
		
		norm = new float[normals.size()];
		for (int i=0;i<normals.size();i++) {
			norm[i] = normals.get(i);
		}
		/*b = BufferUtils.createFloatBuffer(vertA.length);
		b.wrap(vertA);
		
		ByteBuffer secondbuf = ByteBuffer.allocateDirect(vertA.length * Float.SIZE);
		secondbuf.order(ByteOrder.nativeOrder());
		final FloatBuffer ret = secondbuf.asFloatBuffer();
		ret.put(buf);
		ret.flip();

		
		t = BufferUtils.createFloatBuffer(tc.length);
		t.wrap(tc);*/
	}
	
	public int numQuads() {
		return vertA.length/12;
	}
	public int numVerts() {
		return vertA.length/3;
	}
	
	public void render() {
		
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		
		Texture texture = ResourceManager.getTexture("resource/crate0_diffuse.png");
		texture.bind();
		
		
		GL11.glBegin(GL11.GL_QUADS);
		GL11.glColor3f(1f,1f,1f);
		
		
		
		int tI = 0;

		for (int i=0;i<vertA.length-2;i+=3)
		{
			float x = vertA[i];
			float y = vertA[i+1];
			float z = vertA[i+2];
			float u = tc[tI];
			float v = tc[tI+1];
			
			
			float nX = norm[i];
			float nY = norm[i+1];
			float nZ = norm[i+2];
			
			
			
			GL11.glNormal3f(nX, nY, nZ);
			GL11.glVertex3f(x, y, z);
			GL11.glTexCoord2f(u,v);
			tI+=2;
	
		}
		GL11.glEnd();
		
		/*
		GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
		GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
		GL11.glTexCoordPointer(2, 4, t);
		GL11.glVertexPointer(3,4,b);
		GL11.glDrawArrays(GL11.GL_QUADS, 0, vertA.length/4);
		GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
		GL11.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
		*/
		GL11.glDisable(GL11.GL_TEXTURE_2D);
	}
	
	public float[] getVerts() {
		return vertA;
	}
	
	
}
