// v38BrushSide class

// Contains all info for a brush side in a v38 map

public class v38BrushSide {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	private short plane;
	private short texInfo;
	
	// CONSTRUCTORS
	
	public v38BrushSide(short inPlane, short inTexInfo) {
		plane=inPlane;
		texInfo=inTexInfo;
	}
	
	public v38BrushSide(byte[] in) {
		plane=(short)((in[1] << 8) | (in[0] & 0xff));
		texInfo=(short)((in[3] << 8) | (in[2] & 0xff));
	}
	
	// METHODS

	// ACCESSORS/MUTATORS
	
	public short getPlane() {
		return plane;
	}
	
	public void setPlane(short in) {
		plane=in;
	}
	
	public short getTexInfo() {
		return texInfo;
	}
	
	public void setTexInfo(short in) {
		texInfo=in;
	}
}