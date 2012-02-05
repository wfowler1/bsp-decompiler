// v46BrushSide class

// Contains all info for a brush side in a v46 map

public class v46BrushSide {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	private int plane;
	private int texture;
	
	// CONSTRUCTORS
	
	public v46BrushSide(int inPlane, int inTexture) {
		plane=inPlane;
		texture=inTexture;
	}
	
	public v46BrushSide(byte[] in) {
		plane=((in[3] << 24) | ((in[2] & 0xff) << 16) | ((in[1] & 0xff) << 8) | (in[0] & 0xff));
		texture=((in[7] << 24) | ((in[6] & 0xff) << 16) | ((in[5] & 0xff) << 8) | (in[4] & 0xff));
	}
	
	// METHODS

	// ACCESSORS/MUTATORS
	
	public int getPlane() {
		return plane;
	}
	
	public void setPlane(int in) {
		plane=in;
	}
	
	public int getTexture() {
		return texture;
	}
	
	public void setTexture(int in) {
		texture=in;
	}
}