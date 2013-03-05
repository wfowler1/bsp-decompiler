// Node class
// Contains all data needed for a node in a BSP tree. Should be usable by any format.

public class Node extends LumpObject {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
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
		this.plane=DataReader.readInt(data[0], data[1], data[2], data[3]); // All formats I've seen use the first 4 bytes as an int, plane index
		switch(type) { // I don't actually need to read or store node information for most of these formats.
		               // Support for them is only provided for completeness and consistency.
			case BSP.TYPE_QUAKE:
				this.child1=(int)DataReader.readShort(data[4], data[5]);
				this.child2=(int)DataReader.readShort(data[6], data[7]);
				break;
			// Nightfire, Source, Quake 2 and Quake 3-based engines all use the first three ints for planenum and children
			case BSP.TYPE_SIN:
			case BSP.TYPE_SOF:
			case BSP.TYPE_QUAKE2:
			case BSP.TYPE_DAIKATANA:
			case BSP.TYPE_NIGHTFIRE:
			case BSP.TYPE_SOURCE17:
			case BSP.TYPE_SOURCE18:
			case BSP.TYPE_SOURCE19:
			case BSP.TYPE_SOURCE20:
			case BSP.TYPE_SOURCE21:
			case BSP.TYPE_SOURCE22:
			case BSP.TYPE_SOURCE23:
			case BSP.TYPE_VINDICTUS:
			case BSP.TYPE_DMOMAM:
			case BSP.TYPE_STEF2:
			case BSP.TYPE_MOHAA:
			case BSP.TYPE_STEF2DEMO:
			case BSP.TYPE_RAVEN:
			case BSP.TYPE_QUAKE3:
			case BSP.TYPE_FAKK:
			case BSP.TYPE_COD:
				this.child1=DataReader.readInt(data[4], data[5], data[6], data[7]);
				this.child2=DataReader.readInt(data[8], data[9], data[10], data[11]);
				break;
		}
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