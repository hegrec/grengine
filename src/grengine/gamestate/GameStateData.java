/**
 * A nice class to pass any data back and forth between game states. Simply pass to constructor and store whatever you need somewhere.
 * GameStateData.java
 * 
 * @author Chris
 *
 */
package grengine.gamestate;

import java.util.HashMap;
public class GameStateData {

	private HashMap<String, Object> mappedData;

	public GameStateData() {
		mappedData = new HashMap<String,Object>();
	}
	public Object pullData(String key) {
		if (mappedData.containsKey(key))
			return mappedData.get(key);
		
		return null;
	}
	public void addData(String string, Object o) {
		mappedData.put(string, o);
		
	}
}
