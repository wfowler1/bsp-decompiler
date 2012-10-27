// Node class
// Contains all data needed for a node in a BSP tree. Should be usable by any format.

public class Node extends LumpObject {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	public static final int TYPE_QUAKE2=0;
	public static final int TYPE_SOURCE=1;
	
	private int plane=-1;
	private int child1=0; // Negative values are valid here. However, the child can never be zero,
	private int child2=0; // since that would reference the head node causing an infinite loop.
	
	// CONSTRUCTORS
	public Node(LumpObject in, int type) {
		super(in.getData());
		new Node(in.getData(), type);
	}
	
	public Node(byte[] data, int type) {
		super(data);
		// Normally, this is where a switch(type) would go. But, for map formats that I need nodes
		// for, the data is always in the same place.
		plane=DataReader.readInt(data[0], data[1], data[2], data[3]);
		child1=DataReader.readInt(data[4], data[5], data[6], data[7]);
		child2=DataReader.readInt(data[8], data[9], data[10], data[11]);
	}
	
	// METHODS
	
	// ACCESSORS/MUTATORS
	public int getPlane() {
		return plane;
	}
	
	public int getChild1() {
		return child1;
	}
	
	public int getChild2() {
		return child2;
	}
}