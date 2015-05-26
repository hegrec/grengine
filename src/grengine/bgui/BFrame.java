package grengine.bgui;

import java.awt.Color;

import grengine.manager.BGUIManager;
import grengine.render.GUI;

public class BFrame extends BPanel {
	public BButton closeButton;
	public boolean dragging;
	private int xDragOff;
	private int yDragOff;
	public BFrame()
	{
		super();
		this.xDragOff = 0;
		this.yDragOff = 0;
		this.dragging = false;
		final BFrame thisPointer = this;
	
		closeButton = (BButton) this.addPanel(new BButton() {
			
			public void onMousePressed(int button) {

				BGUIManager.remove(thisPointer);
			}
			
			public void paint()
			{
				GUI.setDrawColor(new Color(200,20,20,255));
				GUI.drawRect(0, 0, getWidth(), getHeight());
				GUI.setDrawColor(new Color(1f,1f,1f,1f));
				GUI.drawText("X","Default",5,5);
			}
		
		
		});
			closeButton.setSize(20,20);
	}

	public void think()
	{
		if (this.dragging)
			this.setPos(GUI.getMouseX()-this.xDragOff,GUI.getMouseY()-this.yDragOff);
		
		
		if (!GUI.isMouseDown(0))
			this.dragging = false;
	}
	public void onMousePressed(int button)
	{
		
		xDragOff = this.getCursorX();
		yDragOff =  this.getCursorY();
		
		if (yDragOff > 24) return;
		
		this.dragging = true;
	}
	public void performLayout()
	{
		
		closeButton.setPos(this.getWidth()-22,2);
	}
	public void paint()
	{
		GUI.setDrawColor(new Color(0,0,0,255));
		GUI.drawRect(0, 0, getWidth(), getHeight());
		
		GUI.setDrawColor(new Color(100,100,100,255));
		GUI.drawRect(2, 2, getWidth()-4, getHeight()-4);
		
		
		
		GUI.setDrawColor(new Color(10,10,10,255));
		GUI.drawRect(2, 2, getWidth()-4, 22);
	}
	public void onMouseDragged(int button) {
		
		
	}
	public void onMouseMoved(int button) {
		
		
	}
}
