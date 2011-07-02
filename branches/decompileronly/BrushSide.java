// BrushSide class

// This class holds data of a single brush side.
// The NFBSP BSP lumps table AND the fucking wiki page were wrong, this
// lump references faces first THEN planes.

public class BrushSide {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	private int face; // I don't understand why this lump references
	                  // faces. Brushes are referenced by leaves which
							// also references faces. It seems like it's
							// duplicating faces for no good reason.
	private int plane; // I don't understand why this lump references
	                   // planes if it references faces. The referenced
	                   // faces reference exactly the same plane.
	
	// CONSTRUCTORS
	
	// This one takes the components separate and in the correct data type
	public BrushSide(int inFace, int inPlane) {
		face=inFace;
		plane=inPlane;
	}

	// This one takes an array of bytes (as if read directly from a file) and reads them
	// directly into the proper data types.
	public BrushSide(byte[] in) throws InvalidBrushSideException {
		if(in.length!=8) {
			throw new InvalidBrushSideException();
		}
		face=(in[3] << 24) | ((in[2] & 0xff) << 16) | ((in[1] & 0xff) << 8) | (in[0] & 0xff);
		plane=(in[7] << 24) | ((in[6] & 0xff) << 16) | ((in[5] & 0xff) << 8) | (in[4] & 0xff);
	}
	
	// METHODS
	
	// ACCESSORS/MUTATORS
	public int getFace() {
		return face;
	}
	
	public void setFace(int in) {
		face=in;
	}
	
	public int getPlane() {
		return plane;
	}
	
	public void setPlane(int in) {
		plane=in;
	}
}