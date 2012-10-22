// SiNBrushSide class

// Contains all info for a brush side in a SiN map

public class SiNBrushSide extends v38BrushSide {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	private int unknown;
	
	// CONSTRUCTORS
	
	public SiNBrushSide(short inPlane, short inTexInfo, int unknown) {
		super(inPlane, inTexInfo);
		this.unknown=unknown;
	}
	
	public SiNBrushSide(byte[] in) {
		super(DataReader.readUShort(in[0], in[1]), DataReader.readUShort(in[2], in[3]));
		this.unknown=DataReader.readInt(in[4], in[5], in[6], in[7]);
	}
	
	// METHODS

	// ACCESSORS/MUTATORS
	
	public int getUnknown() {
		return unknown;
	}
	
	public void setUnknown(int in) {
		unknown=in;
	}
}