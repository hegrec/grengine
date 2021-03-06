package grengine.bgui;

import java.util.ArrayList;

import grengine.render.GUI;

public class BMenu extends BPanel {

	
	private static BMenu lastOpenedMenu = null;
	
	
	private ArrayList<BMenuOption> optionButtons;
	private BPanelList panelList;
	
	public BMenu()
	{
		super();
		optionButtons = new ArrayList<BMenuOption>();
		panelList = (BPanelList) this.addPanel(new BPanelList());
		
	}
	public void addOption(String option, BMenuOption bMenuOption)
	{
		optionButtons.add(bMenuOption);
		
		
		bMenuOption.setHeight(25);
		bMenuOption.setLabel(option);
	}

	public void performLayout()
	{
		
		this.setWidth(120);
		this.setHeight(25*optionButtons.size());
		this.panelList.setSize(this.getWidth(), this.getHeight());
		
		
	}
	public static void clearMenu()
	{
		if (lastOpenedMenu != null)
		{
			lastOpenedMenu.remove();
			lastOpenedMenu = null;
		}
	}
	public void open()
	{
		
		BMenu.clearMenu();
		
		panelList.clear();
		
		for (int index=0;index<optionButtons.size();index++)
		{
			BMenuOption option = optionButtons.get(index);
			panelList.addToList(option);
		}
		
		this.setPos(GUI.getMouseX(), GUI.getMouseY());
		lastOpenedMenu = this;
		this.invalidateLayout();
		
	}
}
