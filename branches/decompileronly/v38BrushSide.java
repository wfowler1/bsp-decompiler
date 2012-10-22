// v38BrushSide class

// Contains all info for a brush side in a v38 map

public class v38BrushSide {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	private int plane;
	private int texInfo;
	
	// CONSTRUCTORS
	
	public v38BrushSide(short inPlane, short inTexInfo) {
		plane=(int)inPlane;
		texInfo=(int)inTexInfo;
	}
	
	public v38BrushSide(int plane, int texInfo) {
		this.plane=plane;
		this.texInfo=texInfo;
	}
	
	public v38BrushSide(byte[] in) {
		plane=DataReader.readUShort(in[0], in[1]);
		texInfo=DataReader.readUShort(in[2], in[3]);
	}
	
	// METHODS

	// ACCESSORS/MUTATORS
	
	public int getPlane() {
		return plane;
	}
	
	public void setPlane(int in) {
		plane=in;
	}
	
	public int getTexInfo() {
		return texInfo;
	}
	
	public void setTexInfo(int in) {
		texInfo=in;
	}
}