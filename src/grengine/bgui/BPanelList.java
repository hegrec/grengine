package grengine.bgui;

import java.awt.Color;
import java.util.ArrayList;

import grengine.render.GUI;

public class BPanelList extends BPanel {

	
	private BPanel canvas;
	private boolean autoSizeVertical;
	public BPanelList()
	{
		
		super();
		
		canvas = this.addPanel(new BPanel() {
			
			public void onMousePressed(int button) {
			

				
				
			}
			public void paint()
			{
			}

			});
		canvas.setPos(0,0);
		canvas.setSize(this.getWidth(),this.getHeight());
	}
	public void addToList(BPanel pnl)
	{
		
		canvas.addPanel(pnl);
		this.invalidateLayout();
		
	}
	
	public void performLayout()
	{
		canvas.setSize(this.getWidth(),this.getHeight());
		int canvasTall = 0;
		
		ArrayList<BPanel> canvasChildren = canvas.getChildren();
		
		
		for (int i=0;i<canvasChildren.size();i++)
		{
			BPanel child = canvasChildren.get(i);
			
			child.setPos(0,canvasTall);
			canvasTall += child.getHeight();
			child.setWidth(canvas.getWidth());
		}
		canvas.setSize(this.getWidth(),this.getHeight());
		
		
		
		
	}
	public void onMousePressed(int mouseCode)
	{

	}
	public void paint()
	{
		GUI.setDrawColor(new Color(0.5f,0.2f,0.2f,1.0f));
		GUI.drawRect(0, 0, getWidth(), getHeight());	
	}
	public void onMouseReleased(int mouseCode)
	{

	}
	public void setAutoSize(boolean autoSizeVertical) {
		this.autoSizeVertical = autoSizeVertical;
	}
	public boolean getAutoSize() {
		return autoSizeVertical;
	}
	public void clear() {

		ArrayList<BPanel> canvasChildren = canvas.getChildren();
		
		
		for (int i=0;i<canvasChildren.size();i++)
		{
			BPanel child = canvasChildren.get(i);
			child.remove();
			
		}
		canvas.getChildren().clear();
		
	}
	
	
}
