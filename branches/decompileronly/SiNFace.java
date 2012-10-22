// SiNFace class

// Holds all the data for a face (surface) in a Quake 2 map.

public class SiNFace extends v38Face {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	private int unknown0;
	private int unknown1;
	private int unknown2;
	private int unknown3;
	
	// CONSTRUCTORS
	
	// This constructor takes all data in their proper data types
	public SiNFace(short inPlane, short inSide, int inFirstEdge, short inNumEdges, short inTexInfo, int inLgtStyle, int inLgtMaps, int unknown0, int unknown1, int unknown2, int unknown3) {
		super(inPlane, inSide, inFirstEdge, inNumEdges, inTexInfo, inLgtStyle, inLgtMaps);
		this.unknown0=unknown0;
		this.unknown1=unknown1;
		this.unknown2=unknown2;
		this.unknown3=unknown3;
	}
	
	// This constructor takes 20 bytes in a byte array, as though
	// it had just been read by a FileInputStream.
	public SiNFace(byte[] in) {
		super(DataReader.readShort(in[0], in[1]), DataReader.readShort(in[2], in[3]), DataReader.readInt(in[4], in[5], in[6], in[7]), DataReader.readShort(in[8], in[9]), DataReader.readShort(in[10], in[11]), DataReader.readInt(in[12], in[13], in[14], in[15]), DataReader.readInt(in[16], in[17], in[18], in[19]));
		this.unknown0=DataReader.readInt(in[20], in[21], in[22], in[23]);
		this.unknown1=DataReader.readInt(in[24], in[25], in[26], in[27]);
		this.unknown2=DataReader.readInt(in[28], in[29], in[30], in[31]);
		this.unknown3=DataReader.readInt(in[32], in[33], in[34], in[35]);
	}
	
	// METHODS
	
	// ACCESSORS/MUTATORS
	
	public int getUnknown0() {
		return unknown0;
	}
	
	public void setUnknown0(int in) {
		unknown0=in;
	}
	
	public int getUnknown1() {
		return unknown1;
	}
	
	public void setUnknown1(int in) {
		unknown1=in;
	}
	
	public int getUnknown2() {
		return unknown2;
	}
	
	public void setUnknown2(int in) {
		unknown2=in;
	}
	
	public int getUnknown3() {
		return unknown3;
	}
	
	public void setUnknown3(int in) {
		unknown3=in;
	}
}
