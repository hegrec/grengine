package grengine.manager;

import java.util.ArrayList;

import grengine.render.GUI;
import grengine.bgui.BPanel;

public abstract class BGUIManager {
	
	private static ArrayList<BPanel> topLevel = new ArrayList<BPanel>();
	public static BPanel lastMousedPanel;
	
	public static void remove(BPanel cnt)
	{
		if (cnt.topLevel)
		{
			for (int i=0;i<topLevel.size();i++)
			{
				if (topLevel.get(i) == cnt)
				{
					topLevel.remove(i);
				}
			}
		}
		else
		{
			cnt.getParent().removeChild(cnt);
		}
	}
	public static void draw() 
	{
		for (int i=0;i<topLevel.size();i++)
		{
			BPanel pnl = topLevel.get(i);
			
			GUI.setParentXY(pnl.getX(),pnl.getY());
			paintPanel(topLevel.get(i));
		}
		GUI.setParentXY(0,0);
	}
	public static void paintPanel(BPanel pnl)
	{
		
		pnl.paint();
		
		for (int i=0;i<pnl.getChildren().size();i++)
		{
			
			BPanel childPanel = pnl.getChildren().get(i);
			GUI.setParentXY(childPanel.getXAbsolute(),childPanel.getYAbsolute());
			paintPanel(childPanel);
		}
	}
	
	
	public static BPanel getMousePanel()
	{
	
		
		BPanel foundPanel = null;
		int mouseX = GUI.getMouseX();
		int mouseY = GUI.getMouseY();
		//System.out.println(mouseX+" "+mouseY);
		for (int i=0;i<topLevel.size();i++)
		{
			BPanel pnl = topLevel.get(i);
			if (mouseX < pnl.getX() || mouseX > (pnl.getX()+pnl.getWidth())) continue;
			if (mouseY < pnl.getY() || mouseY > (pnl.getY()+pnl.getHeight())) continue;
			foundPanel = pnl;
		}
		if (foundPanel == null)
		{
			lastMousedPanel = null;
			return null;
		}
		
		return getMousePanel(foundPanel);
	}
	private static BPanel getMousePanel(BPanel panelToUse) {
		BPanel foundPanel = panelToUse;
		int mouseX = GUI.getMouseX();
		int mouseY = GUI.getMouseY();
		for (int i=0;i<panelToUse.getChildren().size();i++)
		{
			BPanel pnl = panelToUse.getChildren().get(i);
			//System.out.println("Checking child "+pnl+" "+mouseX + " "+ pnl.getXAbsolute()+" "+mouseY + " "+ pnl.getYAbsolute());
			if (mouseX < pnl.getXAbsolute() || mouseX > (pnl.getXAbsolute()+pnl.getWidth())) continue;
			if (mouseY < pnl.getYAbsolute() || mouseY > (pnl.getYAbsolute()+pnl.getHeight())) continue;
			foundPanel = pnl;
		}
		
		if (foundPanel == panelToUse) //if we did not find the mouse over a child
		{
			lastMousedPanel = foundPanel;
			return foundPanel;
		}
		//System.out.println("Mouse Panel find CHILD FOUND: "+foundPanel);
		return getMousePanel(foundPanel);
	}
	public static void think() 
	{
		for (int i=0;i<topLevel.size();i++)
		{
			BPanel pnl = topLevel.get(i);
			
			//GUI.setParentXY(pnl.getX(),pnl.getY());
			thinkPanel(topLevel.get(i));
		}
		//GUI.setParentXY(0,0);
	}
	public static void thinkPanel(BPanel pnl)
	{
		
		pnl.think();
		
		for (int i=0;i<pnl.getChildren().size();i++)
		{
			
			BPanel childPanel = pnl.getChildren().get(i);
			//GUI.setParentXY(childPanel.getXAbsolute(),childPanel.getYAbsolute());
			thinkPanel(childPanel);
		}
	}
	public static BPanel create(BPanel pnl) {
		topLevel.add(pnl);
		pnl.topLevel = true;

		pnl.invalidateLayout();
		return pnl;
		
	}
	
}
