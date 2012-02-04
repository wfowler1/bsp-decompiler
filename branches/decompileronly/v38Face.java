// v38Face class

// Holds all the data for a face (surface) in a Quake 2 map.

public class v38Face {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	private short plane;
	private short side;
	private int firstEdge;
	private short numEdges;
	private short texInfo;
	private int lgtStyles;
	private int lgtMaps;
	
	// CONSTRUCTORS
	
	// This constructor takes all data in their proper data types
	public v38Face(short inPlane, short inSide, int inFirstEdge, short inNumEdges, short inTexInfo, short inLgtStyle, short inLgtMaps) {
		short plane=inPlane;
		short side=inSide;
		int firstEdge=inFirstEdge;
		short numEdges=inNumEdges;
		short texInfo=inTexInfo;
		int lgtStyles=inLgtStyle;
		int lgtMaps=inLgtMaps;
	}
	
	// This constructor takes 20 bytes in a byte array, as though
	// it had just been read by a FileInputStream.
	public v38Face(byte[] in) {
		plane=(short)((in[1] << 8) | (in[0] & 0xff));
		side=(short)((in[3] << 8) | (in[2] & 0xff));
		firstEdge=(in[7] << 24) | ((in[6] & 0xff) << 16) | ((in[5] & 0xff) << 8) | (in[4] & 0xff);
		numEdges=(short)((in[9] << 8) | (in[8] & 0xff));
		texInfo=(short)((in[11] << 8) | (in[10] & 0xff));
		lgtStyles=(in[15] << 24) | ((in[14] & 0xff) << 16) | ((in[13] & 0xff) << 8) | (in[12] & 0xff);
		lgtMaps=(in[19] << 24) | ((in[18] & 0xff) << 16) | ((in[17] & 0xff) << 8) | (in[16] & 0xff);
	}
	
	// METHODS
	
	// ACCESSORS/MUTATORS
	
	public short getPlane() {
		return plane;
	}
	
	public void setPlane(short in) {
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
