package grengine.network;

import grengine.input.Input;
import grengine.physics.Quaternion;
import grengine.physics.Vec3;

import java.io.UnsupportedEncodingException;





public class NetworkMessage {

	private byte[] byteArray;
	private int pointer = 0;
	private short size = 0;
	
	public NetworkMessage()
	{
		byteArray = new byte[256];
	}
	
	//should only be used when receiving packets
	public NetworkMessage(byte[] ba)
	{
		byteArray = ba;
		size = (short)ba.length;
	}

	//used to create a NW from a byte array but truncate after specified size
	public NetworkMessage(byte[] ba, int size)
	{
		byteArray = new byte[size];
		for(int i = 0; i < size; i++)
			byteArray[i] = ba[i];
	}
	
	public NetworkMessage(short sizeOfByteArray) 
	{
		byteArray = new byte[sizeOfByteArray];
	}
	
	public int getPointerPosition()
	{
		return pointer;
	}
	
	public short getSize()
	{
		return size;
	}
	//take the size short and convert to a byte array
	public byte[] getSizeAsByteArray() 
	{
		return new byte[] {(byte)((size >> 8) & 0xff),
			(byte)((size >> 0) & 0xff)};
	}
	
	public void setPointerPosition(int pos)
	{
		pointer = pos;
	}
	
	public byte[] getByteArray()
	{
		return byteArray;
	}
	
	public boolean writeString(String str) 
	{
		if(byteArray.length - pointer < str.length() + 4) //not enough room in the byte array to fit the string, 5 to allow room for the size int
			return false;
		
		byte[] tempArray;
		
		try {
			tempArray = str.getBytes("UTF8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return false;
		}
		
		writeInt(tempArray.length);
				
		for(int i = 0; i < tempArray.length; i++)
			byteArray[pointer++] = tempArray[i];
		
		size += 4;
		size += str.length();
		
		return true;
	}
	
	public boolean writeBoolean(boolean value)
	{
		if(pointer >= byteArray.length) //array full
			return false;
		
		byteArray[pointer++] = (byte)(value ? 1 : 0);
		size += 1;
		return true;
	}
	
	public boolean writeDouble(double value)
	{
		if((pointer + 7) >= byteArray.length) //array full
			return false;
		
		long l = Double.doubleToRawLongBits(value);
		
		byteArray[pointer++] = (byte)((l >> 56) & 0xff);
		byteArray[pointer++] = (byte)((l >> 48) & 0xff);
		byteArray[pointer++] = (byte)((l >> 40) & 0xff);
		byteArray[pointer++] = (byte)((l >> 32) & 0xff);
		byteArray[pointer++] = (byte)((l >> 24) & 0xff);
		byteArray[pointer++] = (byte)((l >> 16) & 0xff);
		byteArray[pointer++] = (byte)((l >> 8) & 0xff);
		byteArray[pointer++] = (byte)((l >> 0) & 0xff);
		
		size += 8;
		return true;
	}
	
	public boolean writeShort(short value) 
	{
		if((pointer + 1) >= byteArray.length) //array full, hope its 4 bytes >.>
			return false;
		
		byteArray[pointer++] = (byte)((value >> 8) & 0xff);
		byteArray[pointer++] = (byte)((value >> 0) & 0xff);
		
		size += 2;
		return true;
	}
	
	public boolean writeInt(int value)
	{
		if((pointer + 3) >= byteArray.length) //array full, hope its 4 bytes >.>
			return false;
		
		byteArray[pointer++] = (byte)(value >>> 24);
		byteArray[pointer++] = (byte)(value >>> 16);
		byteArray[pointer++] = (byte)(value >>> 8);
		byteArray[pointer++] = (byte)value;
		
		size += 4;
		return true;
	}
	
	public boolean writeFloat(float value) 
	{
		return writeInt(Float.floatToIntBits(value));
	}
	
	public boolean writeVector(Vec3 value)
	{
		if(pointer + 8 >= byteArray.length)
			return false;
		
		return (writeFloat(value.x) && 	writeFloat(value.y) &&	writeFloat(value.z));
	}
	
	public boolean writeInput(Input value) 
	{
		
		if(pointer + 6 >= byteArray.length)
			return false;
		
		return (writeBoolean(value.up) &&
				writeBoolean(value.down) && 
				writeBoolean(value.left) && 
				writeBoolean(value.right) && 
				writeBoolean(value.attack) && 
				writeBoolean(value.use) && 
				writeBoolean(value.jump));
	}
	
	public String readString()
	{
		int size = readInt();
		try {
			String str = new String(byteArray, pointer, size, "UTF8");
			pointer += size;
			return str;
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	public boolean readBoolean()
	{
		if(pointer >= byteArray.length)
			return false;
		
		return byteArray[pointer++] != 0x00;
	}
	
	
	public double readDouble()
	{
		if(pointer + 7 >= byteArray.length)
			return 0;

		 long temp =  (long)(
				(long)(0xff & byteArray[pointer++]) << 56 |
				(long)(0xff & byteArray[pointer++]) << 48 |
				(long)(0xff & byteArray[pointer++]) << 40 |
				(long)(0xff & byteArray[pointer++]) << 32 |
				(long)(0xff & byteArray[pointer++]) << 24 |
				(long)(0xff & byteArray[pointer++]) << 16 |
				(long)(0xff & byteArray[pointer++]) << 8 |
				(long)(0xff & byteArray[pointer++]) << 0
				);
		 
		 return Double.longBitsToDouble(temp);
	}
	
	public int readInt()
	{
		if(pointer + 3 >= byteArray.length)
			return 0;
		
      return ((byteArray[pointer++] & 0xff) << 24)
      		+ ((byteArray[pointer++] & 0xff) << 16)
      		+ ((byteArray[pointer++] & 0xff) << 8)
      		+ (byteArray[pointer++] & 0xff);
	}
	
	public short readShort()
	{
		if(pointer + 1 >= byteArray.length)
			return 0;
		
		 return (short)(
				 (0xff & byteArray[pointer++]) << 8 |
				 (0xff & byteArray[pointer++]) << 0
				 );
	}
		
	public float readFloat()
	{
		if(pointer + 3 >= byteArray.length)
			return 0;
		
		int temp = ((byteArray[pointer++] & 0xff) << 24)
		+ ((byteArray[pointer++] & 0xff) << 16)
		+ ((byteArray[pointer++] & 0xff) << 8)
		+ (byteArray[pointer++] & 0xff);
		
		return Float.intBitsToFloat(temp);
	}
	
	public Input readInput()
	{
		Input input = new Input();
		if(pointer + 6 >= byteArray.length)
			return input;
		
		
		
		input.up = readBoolean();
		input.down = readBoolean();
		input.left = readBoolean();
		input.right = readBoolean();
		input.attack = readBoolean();
		input.use = readBoolean();
		input.jump = readBoolean();
		
		return input;
	}
	public Vec3 readVector()
	{
		Vec3 vec = new Vec3();
		
		if(pointer + 8 >= byteArray.length)
			return vec;
		
		vec.x = readFloat();
		vec.y = readFloat();
		vec.z = readFloat();
		
		return vec;
	}

	public boolean writeQuaternion(Quaternion orientation) {
		if(pointer + 11 >= byteArray.length)
			return false;
		
		return (writeFloat((float) orientation.w) && 	
				writeFloat((float)orientation.x) &&
				writeFloat((float)orientation.y) &&	
				writeFloat((float)orientation.z));
		
	}
	
	public Quaternion readQuaternion()
	{
		Quaternion vec = new Quaternion();
		
		if(pointer + 11 >= byteArray.length)
			return vec;
		
		vec.w = readFloat();
		vec.x = readFloat();
		vec.y = readFloat();
		vec.z = readFloat();
		
		return vec;
	}
}




