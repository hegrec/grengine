package grengine.network;

public class NetworkMessageTester {

	public static void main(String[] args) 
	{
		NetworkMessage nw1 = new NetworkMessage();
		
		nw1.writeBoolean(false);
		nw1.writeShort((short) 3);

		nw1.writeString("hello world!");
		nw1.writeString("sup dawg?");
		nw1.writeInt(1234567);
		nw1.writeBoolean(true);
		nw1.writeFloat(134.23f);
		
		NetworkMessage nw2 = new NetworkMessage(nw1.getByteArray());
		System.out.println(nw2.readBoolean());
		System.out.println(nw2.readShort());
		System.out.println(nw2.readString());
		System.out.println(nw2.readString());
		System.out.println(nw2.readInt());
		System.out.println(nw2.readBoolean());
		System.out.println(nw2.readFloat());
	}
	
}
