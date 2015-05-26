package grengine.bgui;

import java.awt.Color;

import grengine.render.GUI;
import grengine.resource.TrueTypeFont;

public class BButton extends BPanel {
	
	
	private String lbl;
	private boolean readyDown;
	public BButton()
	{
		super();
		lbl = "";
	}
	
	public void paint()
	{
		GUI.setDrawColor(new Color(0.5f,0.5f,0.5f,1.0f));
		GUI.drawRect(0, 0, getWidth(), getHeight());
		GUI.setDrawColor(new Color(255,0,0,255));
		
		if (lbl.length() > 0)
		GUI.drawText(lbl,"Default", getWidth()/2, getHeight()/2,TrueTypeFont.ALIGN_CENTER);
	}
	public void onMousePressed(int mouseCode)
	{
		this.readyDown = true;
	}
	public void onMouseReleased(int mouseCode)
	{
		if (this.readyDown)
		{
			onClicked();
		}
	}

	public void onMouseExited()
	{
		this.readyDown = false;
	}
	public void onMouseEntered()
	{
	}
	public void onClicked()
	{
	}
	public void setLabel(String string) 
	{
		this.lbl = string;
	}
}
