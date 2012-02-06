// v38Model class

// Holds all the data for a model in a Quake 2 map.

public class v38Model {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	public final int X=0;
	public final int Y=1;
	public final int Z=2;
	
	private Vector3D mins;
	private Vector3D maxs;
	private Vector3D origin;
	private int head;
	private int face;
	private int numFaces;
	
	// CONSTRUCTORS
	
	// This constructor takes all data in their proper data types
	public v38Model(Vector3D inMins, Vector3D inMaxs, Vector3D inOrigin, int inHead, int inFace, int inNumFaces) {
		mins=inMins;
		maxs=inMaxs;
		origin=inOrigin;
		head=inHead;
		face=inFace;
		numFaces=inNumFaces;
	}
	
	// This constructor takes 48 bytes in a byte array, as though
	// it had just been read by a FileInputStream.
	public v38Model(byte[] in) {
		float[] point=new float[3];
		int myInt=(in[3] << 24) | ((in[2] & 0xff) << 16) | ((in[1] & 0xff) << 8) | (in[0] & 0xff);
		point[X]=Float.intBitsToFloat(myInt);
		myInt=(in[7] << 24) | ((in[6] & 0xff) << 16) | ((in[5] & 0xff) << 8) | (in[4] & 0xff);
		point[Y]=Float.intBitsToFloat(myInt);
		myInt=(in[11] << 24) | ((in[10] & 0xff) << 16) | ((in[9] & 0xff) << 8) | (in[8] & 0xff);
		point[Z]=Float.intBitsToFloat(myInt);
		mins=new Vector3D(point[X], point[Y], point[Z]);
		myInt=(in[15] << 24) | ((in[14] & 0xff) << 16) | ((in[13] & 0xff) << 8) | (in[12] & 0xff);
		point[X]=Float.intBitsToFloat(myInt);
		myInt=(in[19] << 24) | ((in[18] & 0xff) << 16) | ((in[17] & 0xff) << 8) | (in[16] & 0xff);
		point[Y]=Float.intBitsToFloat(myInt);
		myInt=(in[23] << 24) | ((in[22] & 0xff) << 16) | ((in[21] & 0xff) << 8) | (in[20] & 0xff);
		point[Z]=Float.intBitsToFloat(myInt);
		maxs=new Vector3D(point[X], point[Y], point[Z]);
		myInt=(in[27] << 24) | ((in[26] & 0xff) << 16) | ((in[25] & 0xff) << 8) | (in[24] & 0xff);
		point[X]=Float.intBitsToFloat(myInt);
		myInt=(in[31] << 24) | ((in[30] & 0xff) << 16) | ((in[29] & 0xff) << 8) | (in[28] & 0xff);
		point[Y]=Float.intBitsToFloat(myInt);
		myInt=(in[35] << 24) | ((in[34] & 0xff) << 16) | ((in[33] & 0xff) << 8) | (in[32] & 0xff);
		point[Z]=Float.intBitsToFloat(myInt);
		origin=new Vector3D(point[X], point[Y], point[Z]);
		head=(in[39] << 24) | ((in[38] & 0xff) << 16) | ((in[37] & 0xff) << 8) | (in[36] & 0xff);
		face=(in[43] << 24) | ((in[42] & 0xff) << 16) | ((in[41] & 0xff) << 8) | (in[40] & 0xff);
		numFaces=(in[47] << 24) | ((in[46] & 0xff) << 16) | ((in[45] & 0xff) << 8) | (in[44] & 0xff);
	}
	
	// METHODS
	
	// ACCESSORS/MUTATORS
	public void setMins(Vector3D in) {
		mins=in;
	}
	
	public Vector3D getMins() {
		return mins;
	}
	
	public void setMaxs(Vector3D in) {
		maxs=in;
	}
	
	public Vector3D getMaxs() {
		return maxs;
	}
	
	public void setOrigin(Vector3D in) {
		origin=in;
	}
	
	public Vector3D getOrigin() {
		return origin;
	}
	
	public int getHead() {
		return head;
	}
	
	public void setHead(int in) {
		head=in;
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