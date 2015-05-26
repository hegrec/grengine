package grengine.manager;

import java.util.ArrayList;
import java.util.HashMap;

import grengine.util.Hook;

public abstract class HookManager {

	private static HashMap<String,ArrayList<Hook>> hookMap = new HashMap<String,ArrayList<Hook>>();

	public static void addHook(String id,Hook hook)
	{
		ArrayList<Hook> list = hookMap.get(id);
		if (list == null)
		{
			list = new ArrayList<Hook>();
			hookMap.put(id,list);
		}
		
		list.add(hook);
	}
	public static void callHook(String string, Object[] a) {

		if (!hookMap.containsKey(string))
			return;
		
		
		
		ArrayList<Hook> list = hookMap.get(string);
		if (list != null)
		{
			for (int i=0;i<list.size();i++)
				list.get(i).onRun(a);
		}
			
		
	}
	
}
