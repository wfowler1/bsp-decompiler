// v42Leaf class

// Holds all the data for a leaf in a NightFire map.

public class v42Leaf {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	public final int X=0;
	public final int Y=1;
	public final int Z=2;
	
	private int type;
	private int PVS;
	private Vector3D mins;
	private Vector3D maxs;
	private int leafFace;
	private int numLeafFace; // Faces are indexed onto leaves using
	                         // a middleman lump called "MarkSurfaces".
	                         // It's possible though that order doesn't
	                         // matter since all faces reference their
	                         // own vertex data. As long as the faces
	                         // are applied to the leaf then it doesn't
	                         // matter what order it happened in.
	private int leafBrush;
	private int numLeafBrush; // This is strange. How could a leaf
	                          // use more than one brush?
	
	// CONSTRUCTORS
	
	// This constructor takes all data in their proper data types
	public v42Leaf(int inType, int inPVS, float inMinX, float inMinY, float inMinZ, float inMaxX, float inMaxY,
	            float inMaxZ, int inLeafFace, int inNumLeafFace, int inLeafBrush, int inNumLeafBrush) {
		type=inType;
		PVS=inPVS;
		mins=new Vector3D(inMinX, inMinY, inMinZ);
		maxs=new Vector3D(inMaxX, inMaxY, inMaxZ);
		leafFace=inLeafFace;
		numLeafFace=inNumLeafFace;
		leafBrush=inLeafBrush;
		numLeafBrush=inNumLeafBrush;
	}
	
	public v42Leaf(int inType, int inPVS, float[] inMins, float[] inMaxs, int inLeafFace, int inNumLeafFace, int inLeafBrush, int inNumLeafBrush) {
		type=inType;
		PVS=inPVS;
		mins=new Vector3D(inMins);
		maxs=new Vector3D(inMaxs);
		leafFace=inLeafFace;
		numLeafFace=inNumLeafFace;
		leafBrush=inLeafBrush;
		numLeafBrush=inNumLeafBrush;
	}
	
	// This constructor takes 48 bytes in a byte array, as though
	// it had just been read by a FileInputStream.
	public v42Leaf(byte[] in) {
		type=(in[3] << 24) | ((in[2] & 0xff) << 16) | ((in[1] & 0xff) << 8) | (in[0] & 0xff);
		PVS=(in[7] << 24) | ((in[6] & 0xff) << 16) | ((in[5] & 0xff) << 8) | (in[4] & 0xff);
		float[] theMins=new float[3];
		float[] theMaxs=new float[3];
		int myInt=(in[11] << 24) | ((in[10] & 0xff) << 16) | ((in[9] & 0xff) << 8) | (in[8] & 0xff);
		theMins[X]=Float.intBitsToFloat(myInt);
		myInt=(in[15] << 24) | ((in[14] & 0xff) << 16) | ((in[13] & 0xff) << 8) | (in[12] & 0xff);
		theMins[Y]=Float.intBitsToFloat(myInt);
		myInt=(in[19] << 24) | ((in[18] & 0xff) << 16) | ((in[17] & 0xff) << 8) | (in[16] & 0xff);
		theMins[Z]=Float.intBitsToFloat(myInt);
		myInt=(in[23] << 24) | ((in[22] & 0xff) << 16) | ((in[21] & 0xff) << 8) | (in[20] & 0xff);
		theMaxs[X]=Float.intBitsToFloat(myInt);
		myInt=(in[27] << 24) | ((in[26] & 0xff) << 16) | ((in[25] & 0xff) << 8) | (in[24] & 0xff);
		theMaxs[Y]=Float.intBitsToFloat(myInt);
		myInt=(in[31] << 24) | ((in[30] & 0xff) << 16) | ((in[29] & 0xff) << 8) | (in[28] & 0xff);
		theMaxs[Z]=Float.intBitsToFloat(myInt);
		mins=new Vector3D(theMins);
		maxs=new Vector3D(theMaxs);
		leafFace=(in[35] << 24) | ((in[34] & 0xff) << 16) | ((in[33] & 0xff) << 8) | (in[32] & 0xff);
		numLeafFace=(in[39] << 24) | ((in[38] & 0xff) << 16) | ((in[37] & 0xff) << 8) | (in[36] & 0xff);
		leafBrush=(in[43] << 24) | ((in[42] & 0xff) << 16) | ((in[41] & 0xff) << 8) | (in[40] & 0xff);
		numLeafBrush=(in[47] << 24) | ((in[46] & 0xff) << 16) | ((in[45] & 0xff) << 8) | (in[44] & 0xff);
	}
	
	// METHODS
	
	// ACCESSORS/MUTATORS
	public int getType() {
		return type;
	}
	
	public void setType(int in) {
		type=in;
	}
	
	public int getPVS() {
		return PVS;
	}
	
	// This will usually be used to set PVS reference to zero, since
	// visibility is an overwhelming clusterfuck of "what?"
	public void setPVS(int in) {
		PVS=in;
	}
	
	public Vector3D getMaxs() {
		return maxs;
	}
	
	public Vector3D getMins() {
		return mins;
	}

	public void setMins(Vector3D in) {
		mins=in;
	}
	
	public void setMaxs(Vector3D in) {
		maxs=in;
	}
	
	public double getMinX() {
		return mins.getX();
	}
	
	public void setMinX(double in) {
		mins.setX(in);
	}
	
	public double getMinY() {
		return mins.getY();
	}
	
	public void setMinY(double in) {
		mins.setY(in);
	}
	
	public double getMinZ() {
		return mins.getZ();
	}
	
	public void setMinZ(double in) {
		mins.setZ(in);
	}
	
	public double getMaxX() {
		return maxs.getX();
	}
	
	public void setMaxX(double in) {
		maxs.setX(in);
	}
	
	public double getMaxY() {
		return maxs.getY();
	}
	
	public void setMaxY(double in) {
		maxs.setY(in);
	}
	
	public double getMaxZ() {
		return maxs.getZ();
	}
	
	public void setMaxZ(double in) {
		maxs.setZ(in);
	}
	
	public int getMarkSurface() {
		return leafFace;
	}
	
	public void setMarkSurface(int in) {
		leafFace=in;
	}
	
	public int getNumMarkSurfaces() {
		return numLeafFace;
	}
	
	public void setNumMarkSurfaces(int in) {
		numLeafFace=in;
	}
	
	public int getMarkBrush() {
		return leafBrush;
	}
	
	public void setMarkBrush(int in) {
		leafBrush=in;
	}
	
	public int getNumMarkBrushes() {
		return numLeafBrush;
	}
	
	public void setNumMarkBrushes(int in) {
		numLeafBrush=in;
	}
}
