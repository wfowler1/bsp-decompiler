// Edge class

// Holds all the data for an edge in a Quake, Half-Life, or Quake 2 map.

public class Edge {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	private int firstVertex;
	private int secondVertex;
	
	// CONSTRUCTORS
	
	// This constructor takes all data in their proper data types
	public Edge(short inFirstVertex, short inSecondVertex) {
		firstVertex=(int)inFirstVertex;
		secondVertex=(int)inSecondVertex;
	}

	public Edge(int inFirstVertex, int inSecondVertex) {
		firstVertex=inFirstVertex;
		secondVertex=inSecondVertex;
	}
	
	// This constructor takes 20 bytes in a byte array, as though
	// it had just been read by a FileInputStream.
	public Edge(byte[] in) {
		firstVertex=DataReader.readUShort(in[0], in[1]);
		secondVertex=DataReader.readUShort(in[2], in[3]);
	}
	
	// METHODS
	
	// ACCESSORS/MUTATORS
	
	public int getFirstVertex() {
		return firstVertex;
	}
	
	public void setFirstVertex(int in) {
		firstVertex=in;
	}
	
	public int getSecondVertex() {
		return secondVertex;
	}
	
	public void setSecondVertex(int in) {
		secondVertex=in;
	}
}
