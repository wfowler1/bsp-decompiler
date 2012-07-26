// v46Brush class

// This class holds data of a single brush in a Quake 3 map. All brushes really
// do is define their sides (by indexing the NEXT lump) and a texture.

public class v46Brush {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	private int firstSide;
	private int numSides;
	private int texture;
	
	// CONSTRUCTORS
	
	// This one takes the components separate and in the correct data type
	public v46Brush(int inFirstSide, int inNumSides, int inTexture) {
		firstSide=inFirstSide;
		numSides=inNumSides;
		texture=inTexture;
	}

	// This one takes an array of bytes (as if read directly from a file) and reads them
	// directly into the proper data types.
	public v46Brush(byte[] in) {
		firstSide=(in[3] << 24) | ((in[2] & 0xff) << 16) | ((in[1] & 0xff) << 8) | (in[0] & 0xff);
		numSides=(in[7] << 24) | ((in[6] & 0xff) << 16) | ((in[5] & 0xff) << 8) | (in[4] & 0xff);
		texture=DataReader.readInt(in[8], in[9], in[10], in[11]);
	}
	
	// METHODS
	
	// ACCESSORS/MUTATORS
	public int getTexture() {
		return texture;
	}
	
	public void setTexture(int in) {
		texture=in;
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