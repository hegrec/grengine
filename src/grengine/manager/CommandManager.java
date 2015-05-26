package grengine.manager;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.Scanner;

import grengine.util.ConCommand;




public class CommandManager extends Thread {

	private static final CommandManager single = new CommandManager();
	public static CommandManager get()
	{
		return single;
	}
	
	
	
	private static HashMap<String,ConCommand> cmdMap = new HashMap<String,ConCommand>();

	
	public void run()
	{
		
		Scanner in = new Scanner(System.in);
		while(true)
		{
			if (in == null) continue;
			
			
			String line = "";
			try
			{
				line = in.nextLine();
			}
			catch(NoSuchElementException e)
			{
				System.out.println("CommandManager errored but still works");
				continue;
			}
			String vars[] = line.split(" ");
			//TODO PUT THE COMMANDS THEMSELVES INTO THE MAIN CLASS and use a system like registerCommand("fps",callback);
			
			String cmd = vars[0].toLowerCase();
			
			if (cmdMap.containsKey(cmd))
				cmdMap.get(cmd).onRan(vars);
			
			
			try {
				Thread.sleep(250);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	public static void registerConCommand(String name,ConCommand hndlr)
	{
		cmdMap.put(name.toLowerCase(), hndlr);
	}
	public void close() {
		// TODO Auto-generated method stub
		
	}
}
