// RavenBrushSide class

// Contains all info for a brush side in a Raven map

public class RavenBrushSide extends CoDBrushSide {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	private int face;
	
	// CONSTRUCTORS
	
	public RavenBrushSide(int inPlane, int inTexture, int face) {
		super(inPlane, inTexture);
		face=face;
	}
	
	public RavenBrushSide(byte[] in) {
		super(DataReader.readInt(in[0], in[1], in[2], in[3]), DataReader.readInt(in[4], in[5], in[6], in[7]));
		face=DataReader.readInt(in[8], in[9], in[10], in[11]);
	}
	
	// METHODS

	// ACCESSORS/MUTATORS
	
	public int getFace() {
		return face;
	}
	
	public void setFace(int in) {
		face=in;
	}
}