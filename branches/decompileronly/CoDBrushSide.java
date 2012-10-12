// CoDBrushSide class

// Contains all info for a brush side in a v46 map

public class CoDBrushSide {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	public static final byte TYPE_UNKNOWN=0;
	public static final byte TYPE_DIST=1;
	public static final byte TYPE_PLANE=2;
	
	// This lump uses an implicit index into brush sides. The first brush references side 0, the 
	// second brush uses the next unused side, and so forth.
	private byte[] data;
	private int plane=-1;
	private float dist=Float.NaN;
	private int texture;
	private byte type;
	
	// CONSTRUCTORS
	
	public CoDBrushSide(int plane, int texture) {
		this.plane=plane;
		this.dist=Float.NaN;
		this.texture=texture;
		this.type=TYPE_PLANE;
	}
	
	public CoDBrushSide(float dist, int texture) {
		this.plane=-1;
		this.dist=dist;
		this.texture=texture;
		this.type=TYPE_DIST;
	}
	
	public CoDBrushSide(byte[] in, boolean isPlane) {
		data=in;
		if(isPlane) {
			type=TYPE_PLANE;
			dist=Float.NaN;
			plane=DataReader.readInt(in[0], in[1], in[2], in[3]);
		} else {
			type=TYPE_DIST;
			dist=DataReader.readFloat(in[0], in[1], in[2], in[3]);
			plane=-1;
		}
		texture=DataReader.readInt(in[4], in[5], in[6], in[7]);
	}
	
	// This constructor keeps this brush side in an indeterminate state until its nature is known.
	// If the setType() mutator is not used after this constructor, the object WILL return
	// good values for either dist or plane.
	public CoDBrushSide(byte[] in) {
		data=in;
		plane=DataReader.readInt(in[0], in[1], in[2], in[3]);
		dist=Float.intBitsToFloat(plane);
		texture=DataReader.readInt(in[4], in[5], in[6], in[7]);
		type=TYPE_UNKNOWN;
	}
	
	// METHODS

	// ACCESSORS/MUTATORS
	
	public int getPlane() {
		return plane;
	}
	
	public void setPlane(int in) {
		plane=in;
	}
	
	public float getDist() {
		return dist;
	}
	
	public void setDist(float in) {
		dist=in;
	}
	
	public int getTexture() {
		return texture;
	}
	
	public void setTexture(int in) {
		texture=in;
	}
	
	public byte getType() {
		return type;
	}
	
	public void setType(byte in) {
		type=in;
		if(in==TYPE_DIST) {
			plane=-1;
		} else {
			if(in==TYPE_PLANE) {
				dist=Float.NaN;
			} else {
				plane=DataReader.readInt(data[0], data[1], data[2], data[3]);
				dist=Float.intBitsToFloat(plane);
			}
		}
	}
}