// Edge class

// Holds all the data for an edge in a Quake, Half-Life, or Quake 2 map.

public class Edge {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	private short firstVertex;
	private short secondVertex;
	
	// CONSTRUCTORS
	
	// This constructor takes all data in their proper data types
	public Edge(short inFirstVertex, short inSecondVertex) {
		firstVertex=inFirstVertex;
		secondVertex=inSecondVertex;
	}
	
	// This constructor takes 20 bytes in a byte array, as though
	// it had just been read by a FileInputStream.
	public Edge(byte[] in) {
		firstVertex=(short)((in[1] << 8) | (in[0] & 0xff));
		secondVertex=(short)((in[3] << 8) | (in[2] & 0xff));
	}
	
	// METHODS
	
	// ACCESSORS/MUTATORS
	
	public short getFirstVertex() {
		return firstVertex;
	}
	
	public void setFirstVertex(short in) {
		firstVertex=in;
	}
	
	public short getSecondVertex() {
		return secondVertex;
	}
	
	public void setSecondVertex(short in) {
		secondVertex=in;
	}
}
