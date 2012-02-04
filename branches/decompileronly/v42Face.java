// v42Face class

// Holds all the data for a face (surface) in a NightFire map.

public class v42Face {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	private int plane;
	private int vert;
	private int numVerts;
	private int meshs;
	private int numMeshs;
	private int type;
	private int texture;
	private int material;
	private int texStyle;
	private int unknown;
	private int lgtStyles;
	private int lgtMaps;
	// where the hell is the amount of lightmaps loaded specified?
	// There's an offset reference into L10 but no length. It may be
	// universal for the whole map, which would be RETARDED.
	
	// CONSTRUCTORS
	
	// This constructor takes all data in their proper data types
	public v42Face(int inPlane, int inVert, int inNumVerts, int inMeshs, int inNumMeshs, int inType, int inText, int inMatrl, int inTexStyle, int inUnk, int inLgtStyle, int inLgtMaps) {
		plane=inPlane;
		vert=inVert;
		numVerts=inNumVerts;
		meshs=inMeshs;
		numMeshs=inNumMeshs;
		type=inType;
		texture=inText;
		material=inMatrl;
		texStyle=inTexStyle;
		unknown=inUnk;
		lgtStyles=inLgtStyle;
		lgtMaps=inLgtMaps;
	}
	
	// This constructor takes 48 bytes in a byte array, as though
	// it had just been read by a FileInputStream.
	public v42Face(byte[] in) {
		plane=(in[3] << 24) | ((in[2] & 0xff) << 16) | ((in[1] & 0xff) << 8) | (in[0] & 0xff);
		vert=(in[7] << 24) | ((in[6] & 0xff) << 16) | ((in[5] & 0xff) << 8) | (in[4] & 0xff);
		numVerts=(in[11] << 24) | ((in[10] & 0xff) << 16) | ((in[9] & 0xff) << 8) | (in[8] & 0xff);
		meshs=(in[15] << 24) | ((in[14] & 0xff) << 16) | ((in[13] & 0xff) << 8) | (in[12] & 0xff);
		numMeshs=(in[19] << 24) | ((in[18] & 0xff) << 16) | ((in[17] & 0xff) << 8) | (in[16] & 0xff);
		type=(in[23] << 24) | ((in[22] & 0xff) << 16) | ((in[21] & 0xff) << 8) | (in[20] & 0xff);
		texture=(in[27] << 24) | ((in[26] & 0xff) << 16) | ((in[25] & 0xff) << 8) | (in[24] & 0xff);
		material=(in[31] << 24) | ((in[30] & 0xff) << 16) | ((in[29] & 0xff) << 8) | (in[28] & 0xff);
		texStyle=(in[35] << 24) | ((in[34] & 0xff) << 16) | ((in[33] & 0xff) << 8) | (in[32] & 0xff);
		unknown=(in[39] << 24) | ((in[38] & 0xff) << 16) | ((in[37] & 0xff) << 8) | (in[36] & 0xff);
		lgtStyles=(in[43] << 24) | ((in[42] & 0xff) << 16) | ((in[41] & 0xff) << 8) | (in[40] & 0xff);
		lgtMaps=(in[47] << 24) | ((in[46] & 0xff) << 16) | ((in[45] & 0xff) << 8) | (in[44] & 0xff);
	}
	
	// METHODS
	
	// ACCESSORS/MUTATORS
	
	public int getPlane() {
		return plane;
	}
	
	public void setPlane(int in) {
		plane=in;
	}
	
	public int getVert() {
		return vert;
	}
	
	public void setVert(int in) {
		vert=in;
	}
	
	public int getNumVerts() {
		return numVerts;
	}
	
	public void setNumVerts(int in) {
		numVerts=in;
	}
	
	public int getMeshs() {
		return meshs;
	}
	
	public void setMeshs(int in) {
		meshs=in;
	}
	
	public int getNumMeshs() {
		return numMeshs;
	}
	
	public void setNumMeshs(int in) {
		numMeshs=in;
	}
	
	public int getType() {
		return type;
	}
	
	public void setType(int in) {
		type=in;
	}
	
	public int getTexture() {
		return texture;
	}
	
	public void setTexture(int in) {
		texture=in;
	}
	
	public int getMaterial() {
		return material;
	}
	
	public void setMaterial(int in) {
		material=in;
	}
	
	public int getTexStyle() {
		return texStyle;
	}
	
	public void setTexStyle(int in) {
		texStyle=in;
	}
	
	public int getUnknown() {
		return unknown;
	}
	
	public void setUnknown(int in) {
		unknown=in;
	}
	
	public int getLgtStyles() {
		return lgtStyles;
	}
	
	public void setLgtStyles(int in) {
		lgtStyles=in;
	}
	
	public int getLgtMaps() {
		return lgtMaps;
	}
	
	public void setLgtMaps(int in) {
		lgtMaps=in;
	}
}
