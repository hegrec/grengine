package grengine.util;

public class MoveData {

	private int relX;
	private int relY;
	public MoveData(int x,int y)
	{
		relX = x;
		relY = y;
	}
	public MoveData()
	{
		relX = 0;
		relY = 0;
	}
	public int getX()
	{
		return relX;
	}
	public int getY()
	{
		return relY;
	}
	public void setX(int parseInt) {
		relX = parseInt;
		
	}
	public void setY(int parseInt) {
		relY = parseInt;
		
	}
}
