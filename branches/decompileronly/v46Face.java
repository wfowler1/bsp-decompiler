// v46Face class

// Holds all the data for a face (surface) in a Quake 3 map. This structure
// is an incredible amount of data, I don't even know if it's all used or
// what. The bulk of it is four points stored as floats. This takes up 48
// of the 104 bytes!

public class v46Face {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	public static final int X=0;
	public static final int Y=1;
	public static final int Z=2;
	
	private int texture;
	private int effect;
	private int type;
	private int vertex;
	private int numVertexes;
	private int meshvert;
	private int numMeshverts;
	private int lm_index;
	private int lm_startX;
	private int lm_startY;
	private int lm_sizeX;
	private int lm_sizeY;
	private Vector3D lm_origin;
	private Vector3D lm_s;
	private Vector3D lm_t;
	private Vector3D normal;
	private int sizeX;
	private int sizeY;
	
	// CONSTRUCTORS
	
	// This constructor takes all data in their proper data types
	public v46Face(int texture, int effect, int type, int vertex, int numVertexes, int meshvert, int numMeshverts,
	               int lm_index, int lm_startX, int lm_startY, int lm_sizeX, int lm_sizeY, Vector3D lm_origin,
	               Vector3D lm_s, Vector3D lm_t, Vector3D normal, int sizeX, int sizeY) {
		this.texture=texture;
		this.effect=effect;
		this.type=type;
		this.vertex=vertex;
		this.numVertexes=numVertexes;
		this.meshvert=meshvert;
		this.numMeshverts=numMeshverts;
		this.lm_index=lm_index;
		this.lm_startX=lm_startX;
		this.lm_startY=lm_startY;
		this.lm_sizeX=lm_sizeX;
		this.lm_sizeY=lm_sizeY;
		this.lm_origin=lm_origin;
		this.lm_s=lm_s;
		this.lm_t=lm_t;
		this.normal=normal;
		this.sizeX=sizeX;
		this.sizeY=sizeY;
	}
	
	// This constructor takes 20 bytes in a byte array, as though
	// it had just been read by a FileInputStream.
	public v46Face(byte[] in) {
		texture=(in[3] << 24) | ((in[2] & 0xff) << 16) | ((in[1] & 0xff) << 8) | (in[0] & 0xff);
		effect=(in[7] << 24) | ((in[6] & 0xff) << 16) | ((in[5] & 0xff) << 8) | (in[4] & 0xff);
		type=(in[11] << 24) | ((in[10] & 0xff) << 16) | ((in[9] & 0xff) << 8) | (in[8] & 0xff);
		vertex=(in[15] << 24) | ((in[14] & 0xff) << 16) | ((in[13] & 0xff) << 8) | (in[12] & 0xff);
		numVertexes=(in[19] << 24) | ((in[18] & 0xff) << 16) | ((in[17] & 0xff) << 8) | (in[16] & 0xff);
		meshvert=(in[23] << 24) | ((in[22] & 0xff) << 16) | ((in[21] & 0xff) << 8) | (in[20] & 0xff);
		numMeshverts=(in[27] << 24) | ((in[26] & 0xff) << 16) | ((in[25] & 0xff) << 8) | (in[24] & 0xff);
		lm_index=(in[31] << 24) | ((in[30] & 0xff) << 16) | ((in[29] & 0xff) << 8) | (in[28] & 0xff);
		lm_startX=(in[35] << 24) | ((in[34] & 0xff) << 16) | ((in[33] & 0xff) << 8) | (in[32] & 0xff);
		lm_startY=(in[39] << 24) | ((in[38] & 0xff) << 16) | ((in[37] & 0xff) << 8) | (in[36] & 0xff);
		lm_sizeX=(in[43] << 24) | ((in[42] & 0xff) << 16) | ((in[41] & 0xff) << 8) | (in[40] & 0xff);
		lm_sizeY=(in[47] << 24) | ((in[46] & 0xff) << 16) | ((in[45] & 0xff) << 8) | (in[44] & 0xff);
		
		float[] point=new float[3];
		int myInt=(in[51] << 24) | ((in[50] & 0xff) << 16) | ((in[49] & 0xff) << 8) | (in[48] & 0xff);
		point[X]=Float.intBitsToFloat(myInt);
		myInt=(in[55] << 24) | ((in[54] & 0xff) << 16) | ((in[53] & 0xff) << 8) | (in[52] & 0xff);
		point[Y]=Float.intBitsToFloat(myInt);
		myInt=(in[59] << 24) | ((in[58] & 0xff) << 16) | ((in[57] & 0xff) << 8) | (in[56] & 0xff);
		point[Z]=Float.intBitsToFloat(myInt);
		lm_origin=new Vector3D(point[X], point[Y], point[Z]);
		
		myInt=(in[63] << 24) | ((in[62] & 0xff) << 16) | ((in[61] & 0xff) << 8) | (in[60] & 0xff);
		point[X]=Float.intBitsToFloat(myInt);
		myInt=(in[67] << 24) | ((in[66] & 0xff) << 16) | ((in[65] & 0xff) << 8) | (in[64] & 0xff);
		point[Y]=Float.intBitsToFloat(myInt);
		myInt=(in[71] << 24) | ((in[70] & 0xff) << 16) | ((in[69] & 0xff) << 8) | (in[68] & 0xff);
		point[Z]=Float.intBitsToFloat(myInt);
		lm_s=new Vector3D(point[X], point[Y], point[Z]);
		
		myInt=(in[75] << 24) | ((in[74] & 0xff) << 16) | ((in[73] & 0xff) << 8) | (in[72] & 0xff);
		point[X]=Float.intBitsToFloat(myInt);
		myInt=(in[79] << 24) | ((in[78] & 0xff) << 16) | ((in[77] & 0xff) << 8) | (in[76] & 0xff);
		point[Y]=Float.intBitsToFloat(myInt);
		myInt=(in[83] << 24) | ((in[82] & 0xff) << 16) | ((in[81] & 0xff) << 8) | (in[80] & 0xff);
		point[Z]=Float.intBitsToFloat(myInt);
		lm_t=new Vector3D(point[X], point[Y], point[Z]);
		
		myInt=(in[87] << 24) | ((in[86] & 0xff) << 16) | ((in[85] & 0xff) << 8) | (in[84] & 0xff);
		point[X]=Float.intBitsToFloat(myInt);
		myInt=(in[91] << 24) | ((in[90] & 0xff) << 16) | ((in[89] & 0xff) << 8) | (in[88] & 0xff);
		point[Y]=Float.intBitsToFloat(myInt);
		myInt=(in[95] << 24) | ((in[94] & 0xff) << 16) | ((in[93] & 0xff) << 8) | (in[92] & 0xff);
		point[Z]=Float.intBitsToFloat(myInt);
		normal=new Vector3D(point[X], point[Y], point[Z]);
		
		sizeX=(in[99] << 24) | ((in[98] & 0xff) << 16) | ((in[97] & 0xff) << 8) | (in[96] & 0xff);
		sizeY=(in[103] << 24) | ((in[102] & 0xff) << 16) | ((in[101] & 0xff) << 8) | (in[100] & 0xff);
	}
	
	// METHODS
	
	// ACCESSORS/MUTATORS
	
	public void setTexture(int texture) {
		this.texture=texture;
	}
	
	public int getTexture() {
		return texture;
	}
	
	public void setEffect(int effect) {
		this.effect=effect;
	}
	
	public int getEffect() {
		return effect;
	}
	
	public void setType(int type) {
		this.type=type;
	}
	
	public int getType() {
		return type;
	}
	
	public void setVertex(int vertex) {
		this.vertex=vertex;
	}
	
	public int getVertex() {
		return vertex;
	}
	
	public void setNumVertices(int numVertexes) {
		this.numVertexes=numVertexes;
	}
	
	public int getNumVertices() {
		return numVertexes;
	}
	
	public void setMeshvert(int meshvert) {
		this.meshvert=meshvert;
	}
	
	public int getMeshvert() {
		return meshvert;
	}
	
	public void setNumMeshvert(int numMeshverts) {
		this.numMeshverts=numMeshverts;
	}
	
	public int getNumMeshverts() {
		return numMeshverts;
	}
	
	public void setLightmapIndex(int lm_index) {
		this.lm_index=lm_index;
	}
	
	public int getLightmapIndex() {
		return lm_index;
	}
	
	public void setLightmapStartX(int lm_startX) {
		this.lm_startX=lm_startX;
	}
	
	public int getLightmapStartX() {
		return lm_startX;
	}
	
	public void setLightmapStartY(int lm_startY) {
		this.lm_startY=lm_startY;
	}
	
	public int getLightmapStartY() {
		return lm_startY;
	}
	
	public void setLightmapSizeX(int lm_sizeX) {
		this.lm_sizeX=lm_sizeX;
	}
	
	public int getLightmapSizeX() {
		return lm_sizeX;
	}
	
	public void setLightmapSizeY(int lm_sizeY) {
		this.lm_sizeY=lm_sizeY;
	}
	
	public int getLightmapSizeY() {
		return lm_sizeY;
	}
	
	public void setLightmapOrigin(Vector3D lm_origin) {
		this.lm_origin=lm_origin;
	}
	
	public Vector3D getLightmapOrigin() {
		return lm_origin;
	}
	
	public void setLightmapS(Vector3D lm_s) {
		this.lm_s=lm_s;
	}
	
	public Vector3D getLightmapS() {
		return lm_s;
	}
	
	public void setLightmapT(Vector3D lm_t) {
		this.lm_t=lm_t;
	}
	
	public Vector3D getLightmapT() {
		return lm_t;
	}
	
	public void setSizeX(int sizeX) {
		this.sizeX=sizeX;
	}
	
	public int getSizeX() {
		return sizeX;
	}
	
	public void setSizeY(int sizeY) {
		this.sizeY=sizeY;
	}
	
	public int getSizeY() {
		return sizeY;
	}
}
