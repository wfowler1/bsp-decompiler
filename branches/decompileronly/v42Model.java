// v42Model class

// Holds all the data for a model in a NightFire map.

public class v42Model {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	public final int X=0;
	public final int Y=1;
	public final int Z=2;
	
	private float[] mins=new float[3];
	private float[] maxs=new float[3];
	// The unknowns are usually null
	private int unknown0;
	private int unknown1;
	private int unknown2;
	private int unknown3;
	private int leaf;
	private int numLeafs;
	private int face;
	private int numFaces;
	
	// CONSTRUCTORS
	
	// This constructor takes all data in their proper data types
	public v42Model(float inMinX, float inMinY, float inMinZ, float inMaxX, float inMaxY, float inMaxZ, int unk0,
	            int unk1, int unk2, int unk3, int inLeaf, int inNumLeafs, int inFace, int inNumFaces) {
		mins[X]=inMinX;
		mins[Y]=inMinY;
		mins[Z]=inMinZ;
		maxs[X]=inMaxX;
		maxs[Y]=inMaxY;
		maxs[Z]=inMaxZ;
		unknown0=unk0;
		unknown1=unk1;
		unknown2=unk2;
		unknown3=unk3;
		leaf=inLeaf;
		numLeafs=inNumLeafs;
		face=inFace;
		numFaces=inNumFaces;
	}
	
	// This constructor takes all data in their proper data types with mins and maxs as float3s
	public v42Model(float[] inMins, float[] inMaxs, int unk0, int unk1, int unk2, int unk3, int inLeaf, int inNumLeafs, int inFace, int inNumFaces) {
		mins=inMins;
		maxs=inMaxs;
		unknown0=unk0;
		unknown1=unk1;
		unknown2=unk2;
		unknown3=unk3;
		leaf=inLeaf;
		numLeafs=inNumLeafs;
		face=inFace;
		numFaces=inNumFaces;
	}
	
	// This constructor takes 56 bytes in a byte array, as though
	// it had just been read by a FileInputStream.
	public v42Model(byte[] in) {
		int myInt=(in[3] << 24) | ((in[2] & 0xff) << 16) | ((in[1] & 0xff) << 8) | (in[0] & 0xff);
		mins[X]=Float.intBitsToFloat(myInt);
		myInt=(in[7] << 24) | ((in[6] & 0xff) << 16) | ((in[5] & 0xff) << 8) | (in[4] & 0xff);
		mins[Y]=Float.intBitsToFloat(myInt);
		myInt=(in[11] << 24) | ((in[10] & 0xff) << 16) | ((in[9] & 0xff) << 8) | (in[8] & 0xff);
		mins[Z]=Float.intBitsToFloat(myInt);
		myInt=(in[15] << 24) | ((in[14] & 0xff) << 16) | ((in[13] & 0xff) << 8) | (in[12] & 0xff);
		maxs[X]=Float.intBitsToFloat(myInt);
		myInt=(in[19] << 24) | ((in[18] & 0xff) << 16) | ((in[17] & 0xff) << 8) | (in[16] & 0xff);
		maxs[Y]=Float.intBitsToFloat(myInt);
		myInt=(in[23] << 24) | ((in[22] & 0xff) << 16) | ((in[21] & 0xff) << 8) | (in[20] & 0xff);
		maxs[Z]=Float.intBitsToFloat(myInt);
		
		unknown0=(in[27] << 24) | ((in[26] & 0xff) << 16) | ((in[25] & 0xff) << 8) | (in[24] & 0xff);
		unknown1=(in[31] << 24) | ((in[30] & 0xff) << 16) | ((in[29] & 0xff) << 8) | (in[28] & 0xff);
		unknown2=(in[35] << 24) | ((in[34] & 0xff) << 16) | ((in[33] & 0xff) << 8) | (in[32] & 0xff);
		unknown3=(in[39] << 24) | ((in[38] & 0xff) << 16) | ((in[37] & 0xff) << 8) | (in[36] & 0xff);
		
		leaf=(in[43] << 24) | ((in[42] & 0xff) << 16) | ((in[41] & 0xff) << 8) | (in[40] & 0xff);
		numLeafs=(in[47] << 24) | ((in[46] & 0xff) << 16) | ((in[45] & 0xff) << 8) | (in[44] & 0xff);
		face=(in[51] << 24) | ((in[50] & 0xff) << 16) | ((in[49] & 0xff) << 8) | (in[48] & 0xff);
		numFaces=(in[55] << 24) | ((in[54] & 0xff) << 16) | ((in[53] & 0xff) << 8) | (in[52] & 0xff);
	}
	
	// METHODS
	
	// ACCESSORS/MUTATORS
	public float getMinX() {
		return mins[X];
	}
	
	public void setMinX(float in) {
		mins[X]=in;
	}
	
	public float getMinY() {
		return mins[Y];
	}
	
	public void setMinY(float in) {
		mins[Y]=in;
	}
	
	public float getMinZ() {
		return mins[Z];
	}
	
	public void setMinZ(float in) {
		mins[Z]=in;
	}
	
	public float getMaxX() {
		return maxs[X];
	}
	
	public void setMaxX(float in) {
		maxs[X]=in;
	}
	
	public float getMaxY() {
		return maxs[Y];
	}
	
	public void setMaxY(float in) {
		maxs[Y]=in;
	}
	
	public float getMaxZ() {
		return maxs[Z];
	}
	
	public void setMaxZ(float in) {
		maxs[Z]=in;
	}
	
	public int getUnk0() {
		return unknown0;
	}
	
	public void setUnk0(int in) {
		unknown0=in;
	}
	
	public int getUnk1() {
		return unknown1;
	}
	
	public void setUnk1(int in) {
		unknown1=in;
	}
	
	public int getUnk2() {
		return unknown2;
	}
	
	public void setUnk2(int in) {
		unknown2=in;
	}
	
	public int getUnk3() {
		return unknown3;
	}
	
	public void setUnk3(int in) {
		unknown3=in;
	}
	
	public int getLeaf() {
		return leaf;
	}
	
	public void setLeaf(int in) {
		leaf=in;
	}
	
	public int getNumLeafs() {
		return numLeafs;
	}
	
	public void setNumLeafs(int in) {
		numLeafs=in;
	}
	
	public int getFace() {
		return face;
	}
	
	public void setFace(int in) {
		face=in;
	}
	
	public int getNumFaces() {
		return numFaces;
	}
	
	public void setNumFaces(int in) {
		numFaces=in;
	}
}