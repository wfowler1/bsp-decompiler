// SourceDispVertex class

// Holds all the data for a displacement in a Source map.

public class SourceDispVertex extends LumpObject {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	// At this point, screw it, I'm just copying names from the Valve developer wiki Source BSP documentation page
	private Vector3D normal; // The normalized vector direction this vertex points from "flat"
	private float dist; // Magnitude of normal, before normalization
	private float alpha; // Alpha value of texture at this vertex
	
	
	// CONSTRUCTORS
	
	// This constructor takes 32 bytes in a byte array, as though
	// it had just been read by a FileInputStream.
	public SourceDispVertex(byte[] in) {
		super(in);
		this.normal=DataReader.readPoint3F(in[0], in[1], in[2], in[3], in[4], in[5], in[6], in[7], in[8], in[9], in[10], in[11]);
		this.dist=DataReader.readFloat(in[12], in[13], in[14], in[15]);
		this.alpha=DataReader.readFloat(in[16], in[17], in[18], in[19]);
	}
	
	// METHODS
	
	// ACCESSORS/MUTATORS
	
	public Vector3D getNormal() {
		return normal;
	}
	
	public void setNormal(Vector3D in) {
		normal=in;
	}
	
	public float getDist() {
		return dist;
	}
	
	public void setDist(float in) {
		dist=in;
	}
	
	public float getAlpha() {
		return alpha;
	}
	
	public void setAlpha(float in) {
		alpha=in;
	}
}
