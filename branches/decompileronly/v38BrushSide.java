// v38BrushSide class

// Contains all info for a brush side in a v38 map

public class v38BrushSide {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	private int plane;
	private short texInfo; // Needs to be a signed short, "-1" is a used value
	
	// CONSTRUCTORS
	
	public v38BrushSide(short inPlane, short inTexInfo) {
		plane=(int)inPlane;
		texInfo=inTexInfo;
	}
	
	public v38BrushSide(int plane, int texInfo) {
		this.plane=plane;
		this.texInfo=(short)texInfo;
	}
	
	public v38BrushSide(byte[] in) {
		plane=DataReader.readUShort(in[0], in[1]);
		texInfo=DataReader.readShort(in[2], in[3]);
	}
	
	// METHODS

	// ACCESSORS/MUTATORS
	
	public int getPlane() {
		return plane;
	}
	
	public void setPlane(int in) {
		plane=in;
	}
	
	public short getTexInfo() {
		return texInfo;
	}
	
	public void setTexInfo(short in) {
		texInfo=in;
	}
}