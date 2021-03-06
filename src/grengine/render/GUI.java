package grengine.render;

import java.awt.Color;
import java.awt.Font;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import grengine.manager.ResourceManager;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import grengine.resource.Texture;
import grengine.resource.TrueTypeFont;
public class GUI {

	//public static GLAutoDrawable drawable;
	public static Color lastColor;
	//public static TextRenderer tRenderer;
	public static GUI single = new GUI();
	public static int parentX;
	public static int parentY;
	private static HashMap<String,TrueTypeFont> fontMap = new HashMap<String,TrueTypeFont>();

	
	public static void setDrawColor(Color color) {
		lastColor = color;
		GL11.glColor4f(color.getRed()/255f, color.getGreen()/255f, color.getBlue()/255f, color.getAlpha()/255f);
		
	}

	public static void drawRect(int x, int y, int width, int height) {
		
		GL11.glPushMatrix();
		
		GL11.glLoadIdentity();

		GL11.glEnable(GL11.GL_BLEND);
		//GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		
		
		GL11.glTranslatef(x+parentX, y+parentY, 0);
	        GL11.glBegin(GL11.GL_QUADS);
	        
	        
	        GL11.glVertex2f(0,0);
	        GL11.glVertex2f(width,0);	
	        GL11.glVertex2f(width,height);
	        GL11.glVertex2f(0,height);
	        
	        GL11.glDisable(GL11.GL_BLEND);
	       // GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glEnd();
		// restore the model view matrix to prevent contamination

		GL11.glPopMatrix();
		
	}

	public static void createFont(String identifier, String fontFace,int fontSize,boolean antialias)
	{
		
		if (fontMap.containsKey(identifier)) return;
		
		Font f = new Font(fontFace, Font.PLAIN, fontSize);
		TrueTypeFont tF = new TrueTypeFont(f, antialias);
		fontMap.put(identifier, tF);
		
	}
	public static void drawText(String text,String fontID, int x, int y) {
		   
		drawText(text,fontID, x, y, TrueTypeFont.ALIGN_LEFT);

	}
	
	public static void drawText(String text,String fontID, int x, int y, int align) {
	   
		if (!fontMap.containsKey(fontID)) return;
		
		
		TrueTypeFont f = fontMap.get(fontID);
		f.drawString(x+parentX, y+parentY, text, 1, -1,align);

	}
	public static void setParentXY(int x, int y) {
		parentX = x;
		parentY = y;
		
	}
	public static void drawLine(float x1, float y1, float x2, float y2)
	{
		GL11.glPushMatrix();
		
		GL11.glLoadIdentity();
		//GL11.glDisable(GL11.GL_DEPTH_TEST);
	       GL11.glTranslatef(x1+parentX, y1+parentY, 0);
	       GL11.glLineWidth(5.0f);
	        GL11.glBegin(GL11.GL_LINES);
	        
	        
	        
	        GL11.glVertex2f(0,0);
	        GL11.glVertex2f(x2-x1,y2-y1);	
	        
	        GL11.glEnd();
		
		// restore the model view matrix to prevent contamination
//GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glPopMatrix();
		
		
		
	}
	
	public static void drawSprite(float centerX,float centerY, float width, float height, Texture texture,float angle)
	{
		//GL11.glDisable(GL11.GL_DEPTH_TEST);
		// store the current model matrix
		GL11.glPushMatrix();
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		//GL11.glEnable(GL11.GL_BLEND);
		//GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		//GL11.glTranslatef(0, 0, 0);		
		texture.bind();
		GL11.glLoadIdentity();
		
		
	       GL11.glTranslatef(centerX, centerY, 0);
	       GL11.glRotatef(angle, 0, 0, 1);
	        GL11.glBegin(GL11.GL_QUADS);
	        
	        GL11.glColor4f(1,1,1,1);
	        
	        
	        GL11.glTexCoord2f(0, 0);	        
			//GL11.glColor3f(0.0f, 0.7f, 0.7f);
	        GL11.glVertex2f(-width/2f,-height/2f);
	        
			GL11.glTexCoord2f(texture.getWidth(),0);

	        GL11.glVertex2f(width/2f,-height/2f);	
	        
			GL11.glTexCoord2f(texture.getWidth(), texture.getHeight());

	        GL11.glVertex2f(width/2f,height/2f);
	        //GL11.glColor3f(1.0f, 0.7f, 0.7f);
	        
			GL11.glTexCoord2f(0, texture.getHeight());
	        GL11.glVertex2f(-width/2f, height/2f);	
	        GL11.glEnd();
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		//GL11.glEnable(GL11.GL_DEPTH_TEST);
		//GL11.glDisable(GL11.GL_BLEND);
		// restore the model view matrix to prevent contamination

		GL11.glPopMatrix();
		
		
		
	}
	public static int getMouseX() {
		// TODO Auto-generated method stub
		return Mouse.getX();
	}
	public static int getMouseY() {
		// TODO Auto-generated method stub
		return ResourceManager.ScrH()-Mouse.getY();
	}
	public static boolean isMouseDown(int i) {
		// TODO Auto-generated method stub
		return Mouse.isButtonDown(i);
	}
	

	
}
