package grengine.util;

public class Profiler {
	
	
	private static long startTime;
	public static void start()
	{
		startTime = System.nanoTime();
	}
	public static void dump()
	{
		double timeMS = (System.nanoTime()-startTime)/1000000d;
		System.out.println("Executed In "+timeMS+" milliseconds");
	}
} 
