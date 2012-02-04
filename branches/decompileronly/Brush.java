// Brush class

// This class holds data of a single brush. All brushes really
// do is define their material (solid, nonsolid, water, etc.)
// and their sides (by indexing the NEXT lump).

public class Brush {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	private int firstSide;
	private int numSides;
	private int attributes; // attributes is a strange animal. It's
	                        // probably not read as an int by the
									// game engine, it may be a set of bytes
									// or even a bunch of binary flags.
	
	// CONSTRUCTORS
	
	// This one takes the components separate and in the correct data type
	public Brush(int inFirstSide, int inNumSides, int inAttributes) {
		attributes=inAttributes;
		firstSide=inFirstSide;
		numSides=inNumSides;
	}

	// This one takes an array of bytes (as if read directly from a file) and reads them
	// directly into the proper data types.
	public Brush(byte[] in) {
		firstSide=(in[3] << 24) | ((in[2] & 0xff) << 16) | ((in[1] & 0xff) << 8) | (in[0] & 0xff);
		numSides=(in[7] << 24) | ((in[6] & 0xff) << 16) | ((in[5] & 0xff) << 8) | (in[4] & 0xff);
		attributes=(in[11] << 24) | ((in[10] & 0xff) << 16) | ((in[9] & 0xff) << 8) | (in[8] & 0xff);
	}
	
	// METHODS
	
	// ACCESSORS/MUTATORS
	public int getAttributes() {
		return attributes;
	}
	
	public void setAttributes(int in) {
		attributes=in;
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