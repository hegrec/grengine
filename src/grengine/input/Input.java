package grengine.input;

public class Input {

	
	
	public boolean up;
	public boolean down;
	public boolean left;
	public boolean right;
	public boolean attack;
	public boolean use;
	public boolean jump;
	public Input()
	{
		up = false;
		down = false;
		left = false;
		right = false;
		attack = false;
		use = false;
		jump = false;
	}
	public String toString()
	{
		return up+" "+down+" "+left+" "+right+" "+attack+" "+jump;
	}
}
