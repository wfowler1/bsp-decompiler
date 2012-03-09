// Brush class

// This class holds data of a single brush. All brushes really
// do is define their material (solid, nonsolid, water, etc.)
// and their sides (by indexing the NEXT lump).

public class Brush {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	private int firstSide;
	private int numSides;
	private byte[] attributes=new byte[4]; // attributes is a strange animal. It's
	                                       // probably not read as an int by the
	                                       // game engine, it may be a set of bytes
	                                       // or even a bunch of binary flags.
	
	// CONSTRUCTORS
	
	// This one takes the components separate and in the correct data type
	public Brush(int inFirstSide, int inNumSides, byte[] inAttributes) {
		try {
			attributes[0]=inAttributes[0];
			attributes[1]=inAttributes[1];
			attributes[2]=inAttributes[2];
			attributes[3]=inAttributes[3];
		} catch(java.lang.ArrayIndexOutOfBoundsException e) {
			; // Meh, there are worse crimes. Leave them at 0
		}
		firstSide=inFirstSide;
		numSides=inNumSides;
	}

	// This one takes an array of bytes (as if read directly from a file) and reads them
	// directly into the proper data types.
	public Brush(byte[] in) {
		firstSide=(in[3] << 24) | ((in[2] & 0xff) << 16) | ((in[1] & 0xff) << 8) | (in[0] & 0xff);
		numSides=(in[7] << 24) | ((in[6] & 0xff) << 16) | ((in[5] & 0xff) << 8) | (in[4] & 0xff);
		attributes[0]=in[8];
		attributes[1]=in[9];
		attributes[2]=in[10];
		attributes[3]=in[11];
	}
	
	// METHODS
	
	// ACCESSORS/MUTATORS
	public byte[] getAttributes() {
		return attributes;
	}
	
	public void setAttributes(byte[] in) {
		if(in.length==4) {
			attributes=in;
		}
	}
	
	public int getFirstSide() {
		return firstSide;
	}
	
	public void setFirstSide(int in) {
		firstSide=in;
	}
	
	public int getNumSides() {
		return numSides;
	}
	
	public void setNumSides(int in) {
		numSides=in;
	}
}