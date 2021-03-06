package grengine.bgui;

import java.awt.Color;
import java.util.ArrayList;

import grengine.manager.BGUIManager;
import grengine.render.GUI;

public class BPanel {

	private BPanel parent;

	private ArrayList<BPanel> children;
	private int x;
	private int y;
	private int width;
	private int height;
	public boolean topLevel;

	public BPanel() {

		x = 0;
		y = 0;
		width = 10;
		height = 10;
		topLevel = false;
		//this.setVisible(false);
		children = new ArrayList<BPanel>();
	}
	public void setPos(int x, int y) 
	{
		
		this.x = x;
		this.y = y;
		
		
		
		
	}
	public void remove()
	{
		BGUIManager.remove(this);
	}
	public void think() 
	{
		
		
	}
	public BPanel addPanel(BPanel pnl) {
		
		
		pnl.setParent(this);
		return pnl;
		
	}
	public void paint()
	{
		GUI.setDrawColor(new Color(0.2f,0.2f,0.2f,0.1f));
		GUI.drawRect(0, 0, getWidth(), getHeight());
	}
	public void performLayout()
	{
	
	}
	public void setParent(BPanel parent) {
		
		if (this.parent != null)
			this.parent.removeChild(this);
		
		
		
		parent.addChild(this);
		this.parent = parent;
	}
	public void removeChild(BPanel bPanel) {
		for (int i=0;i<this.children.size();i++)
		{
			if (this.children.get(i) == bPanel)
			{
				this.children.remove(i);
				return;
			}
		}
		
	}
	private void addChild(BPanel bPanel) {


		children.add(bPanel);
		
	}
	public void invalidateLayout()
	{
		this.performLayout();
		for (int i=0;i<this.children.size();i++)
		{
			this.children.get(i).invalidateLayout();
		}
	}
	public BPanel getParent() {
		return parent;
	}
	public void setX(int x) {
		this.x = x;
	}
	public int getX() {
		return x;
	}
	public int getCursorX()
	{
		return GUI.getMouseX() - getXAbsolute();
	}
	public int getXAbsolute() {
		
		int x = 0;
		
		
		BPanel pnl = this;
		while (pnl != null)
		{
			x += pnl.getX();
			pnl = pnl.getParent();
		}
		return x;
		
	}
	public void setY(int y) {
		this.y = y;
	}
	public int getY() {
		return y;
	}
	public int getCursorY()
	{
		return GUI.getMouseY() - getYAbsolute();
	}
	public int getYAbsolute() {
		
		int y = 0;
		
		
		BPanel pnl = this;
		while (pnl != null)
		{
			y += pnl.getY();
			pnl = pnl.getParent();
		}
		return y;
		
	}
	public void setWidth(int width) {
		this.width = width;
	}
	public int getWidth() {
		return width;
	}
	public void setHeight(int height) {
		this.height = height;
	}
	public int getHeight() {
		return height;
	}
	public void setSize(int wide,int tall)
	{
		this.width = wide;
		this.height = tall;
	}
	public ArrayList<BPanel> getChildren() {
		return children;
	}

	public void onMousePressed(int mouseCode)
	{
	}
	public void onMouseReleased(int mouseCode)
	{
	}
	public void onMouseDragged(int button) {
		
		
	}
	public void onMouseMoved(int button) {
		
		
	}
	public void onMouseExited() {
		
	}
	public void onMouseEntered() {
	}
	
	
	
}
