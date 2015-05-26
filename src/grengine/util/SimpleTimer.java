package grengine.util;

import java.util.ArrayList;

import grengine.appstate.EngineBase;

public class SimpleTimer {

	
	private float startTime;
	private static ArrayList<SimpleTimer> timerList = new ArrayList<SimpleTimer>();
	private float endTime;
	public SimpleTimer(float delay)
	{
		startTime = EngineBase.getCurrentTime();
		endTime = startTime+delay;
	}
}
