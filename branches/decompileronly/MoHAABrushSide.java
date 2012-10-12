// MoHAABrushSide class

// Holds all necessary data for a brush side in a MoHAA BSP

public class MoHAABrushSide extends CoDBrushSide {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	private int texScale;
	
	// CONSTRUCTORS
	
	public MoHAABrushSide(int inPlane, int inTexture, int inTexScale) {
		super(inPlane, inTexture);
		this.texScale=inTexScale;
	}
	
	public MoHAABrushSide(byte[] in) {
		super(DataReader.readInt(in[0], in[1], in[2], in[3]), DataReader.readInt(in[4], in[5], in[6], in[7]));
		texScale=DataReader.readInt(in[8], in[9], in[10], in[11]);
	}
	
	// METHODS

	// ACCESSORS/MUTATORS
	
	public int getTexScale() {
		return texScale;
	}
	
	public void setTexScale(int in) {
		texScale=in;
	}
}
