// BSPPlane class

// This class holds data on ONE plane. It's only really useful when
// used in an array along with many others. Each piece of data has
// its own variable.

public class BSPPlane extends Plane {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	private int type; // Still not sure what the fuck this is for. Thought it was an axis, but that'd
	                  // be pointless since Ax+By+Cz=D is sufficient for any plane ever, as long as A.B.C=1,
	                  // where . is the dot product
	
	// CONSTRUCTORS
	
	// This one takes the components separate and in the correct data type
	public BSPPlane(float inA, float inB, float inC, float inDist, int inType) {
		super(inA, inB, inC, inDist);
		type=inType;
	}
	
	public BSPPlane(double inA, double inB, double inC, float inDist, int inType) {
		super(inA, inB, inC, inDist);
		type=inType;
	}
	
	public BSPPlane(double inA, double inB, double inC, double inDist, int inType) {
		super(inA, inB, inC, inDist);
		type=inType;
	}
	
	public BSPPlane(float[] inNormal, float inDist, int inType) {
		super(inNormal[0], inNormal[1], inNormal[2], inDist);
		type=inType;
	}

	// This one takes an array of bytes (as if read directly from a file) and reads them
	// directly into the proper data types.
	// Another reason to port this to a different language: JAVA USES BIG ENDIAN BYTE ORDER
	// Maybe there's some property of the runtime you can set to use little endian. Maybe
	// Java is just retarded.
	public BSPPlane(byte[] in) {
		super(DataReader.readPoint3F(in[0], in[1], in[2], in[3], in[4], in[5], in[6], in[7], in[8], in[9], in[10], in[11]), DataReader.readFloat(in[12], in[13], in[14], in[15]));
		type=(in[19] << 24) | ((in[18] & 0xff) << 16) | ((in[17] & 0xff) << 8) | (in[16] & 0xff);
	}
	
	// METHODS
	
	// ACCESSORS/MUTATORS
	
	public int getType() {
		return type;
	}
	
	public void setType(int in) {
		type=in;
	}
}
