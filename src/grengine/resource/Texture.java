package grengine.resource;


import java.awt.image.BufferedImage;

import org.lwjgl.opengl.GL11;

/**
 * A texture to be bound within JOGl
 *
 * @author Kevin Glass
 */
public class Texture {
    private String resourceName; 
    private int target; 
    private int textureID;
    private int height;
    private int width;
    private int texWidth;
    private int texHeight;
    private float widthRatio;
    private float heightRatio;
    
    private BufferedImage buffer;
    
    static Texture lastbind = null;
    
    public Texture(String resourceName,int target,int textureID) {
        this.resourceName = resourceName;
        this.target = target;
        this.textureID = textureID;
        
    }
    
    public void setBufferedImage(BufferedImage buffer) {
        this.buffer = buffer;
    }
    
    public BufferedImage getBufferedImage() {
       return buffer;
    }
    
    public void bind() {
       if ((lastbind == null) || (lastbind != this)) {
         GL11.glBindTexture(target, textureID); 
       }
    }
    
    public void setHeight(int height) {
        this.height = height;
    }
    
    public void setWidth(int width) {
        this.width = width;
    }
    
    public int getImageHeight() {
        return height;
    }
    
    public int getImageWidth() {
        return width;
    }
    
    public float getHeight() {
        return heightRatio;
    }
    
    public float getWidth() {
        return widthRatio;
    }
    
    public void setTextureHeight(int texHeight) {
        this.texHeight = texHeight;
        setHeight();
    }
    
    public void setTextureWidth(int texWidth) {
        this.texWidth = texWidth;
        setWidth();
    }
    
    private void setHeight() {
        if (texHeight != 0) {
            heightRatio = ((float) height)/texHeight;
        }
    }
    
    private void setWidth() {
        if (texWidth != 0) {
            widthRatio = ((float) width)/texWidth;
        }
    }

	
}