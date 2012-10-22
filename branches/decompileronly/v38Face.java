// v38Face class

// Holds all the data for a face (surface) in a Quake 2 map.

public class v38Face {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	private int plane;
	private short side;
	private int firstEdge;
	private short numEdges;
	private short texInfo;
	private int lgtStyles;
	private int lgtMaps;
	
	// CONSTRUCTORS
	
	// This constructor takes all data in their proper data types
	public v38Face(int inPlane, short inSide, int inFirstEdge, short inNumEdges, short inTexInfo, int inLgtStyle, int inLgtMaps) {
		plane=inPlane;
		side=inSide;
		firstEdge=inFirstEdge;
		numEdges=inNumEdges;
		texInfo=inTexInfo;
		lgtStyles=inLgtStyle;
		lgtMaps=inLgtMaps;
	}
	
	// This constructor takes 20 bytes in a byte array, as though
	// it had just been read by a FileInputStream.
	public v38Face(byte[] in) {
		plane=DataReader.readUShort(in[0], in[1]);
		side=DataReader.readShort(in[2], in[3]);
		firstEdge=DataReader.readInt(in[4], in[5], in[6], in[7]);
		numEdges=DataReader.readShort(in[8], in[9]);
		texInfo=DataReader.readShort(in[10], in[11]);
		lgtStyles=DataReader.readInt(in[12], in[13], in[14], in[15]);
		lgtMaps=DataReader.readInt(in[16], in[17], in[18], in[19]);
	}
	
	// METHODS
	
	// ACCESSORS/MUTATORS
	
	public int getPlane() {
		return plane;
	}
	
	public void setPlane(int in) {
		plane=in;
	}
	
	public short getSide() {
		return side;
	}
	
	public void setSide(short in) {
		side=in;
	}
	
	public int getFirstEdge() {
		return firstEdge;
	}
	
	public void setFirstEdge(int in) {
		firstEdge=in;
	}
	
	public short getNumEdges() {
		return numEdges;
	}
	
	public void setNumEdges(short in) {
		numEdges=in;
	}
	
	public short getTexInfo() {
		return texInfo;
	}
	
	public void setTexInfo(short in) {
		texInfo=in;
	}
	
	public int getLgtStyles() {
		return lgtStyles;
	}
	
	public void setLgtStyles(int in) {
		lgtStyles=in;
	}
	
	public int getLgtMaps() {
		return lgtMaps;
	}
	
	public void setLgtMaps(int in) {
		lgtMaps=in;
	}
}
