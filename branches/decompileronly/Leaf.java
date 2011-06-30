// Leaf class

// Holds all the data for a leaf in a NightFire map.

public class Leaf {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	public final int X=0;
	public final int Y=1;
	public final int Z=2;
	
	private int type;
	private int PVS;
	private float[] mins=new float[3];
	private float[] maxs=new float[3];
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
	public Leaf(int inType, int inPVS, float inMinX, float inMinY, float inMinZ, float inMaxX, float inMaxY,
	            float inMaxZ, int inLeafFace, int inNumLeafFace, int inLeafBrush, int inNumLeafBrush) {
		type=inType;
		PVS=inPVS;
		mins[X]=inMinX;
		mins[Y]=inMinY;
		mins[Z]=inMinZ;
		maxs[X]=inMaxX;
		maxs[Y]=inMaxY;
		maxs[Z]=inMaxZ;
		leafFace=inLeafFace;
		numLeafFace=inNumLeafFace;
		leafBrush=inLeafBrush;
		numLeafBrush=inNumLeafBrush;
	}
	
	public Leaf(int inType, int inPVS, float[] inMins, float[] inMaxs, int inLeafFace, int inNumLeafFace, int inLeafBrush, int inNumLeafBrush) throws InvalidLeafException {
		if(inMins.length!=3 || inMaxs.length!=3) {
			throw new InvalidLeafException();
		}
		type=inType;
		PVS=inPVS;
		mins=inMins;
		maxs=inMaxs;
		leafFace=inLeafFace;
		numLeafFace=inNumLeafFace;
		leafBrush=inLeafBrush;
		numLeafBrush=inNumLeafBrush;
	}
	
	// This constructor takes 48 bytes in a byte array, as though
	// it had just been read by a FileInputStream.
	public Leaf(byte[] in) throws InvalidLeafException {
		if(in.length!=48) {
			throw new InvalidLeafException();
		}
		type=(in[3] << 24) | ((in[2] & 0xff) << 16) | ((in[1] & 0xff) << 8) | (in[0] & 0xff);
		PVS=(in[7] << 24) | ((in[6] & 0xff) << 16) | ((in[5] & 0xff) << 8) | (in[4] & 0xff);
		int myInt=(in[11] << 24) | ((in[10] & 0xff) << 16) | ((in[9] & 0xff) << 8) | (in[8] & 0xff);
		mins[X]=Float.intBitsToFloat(myInt);
		myInt=(in[15] << 24) | ((in[14] & 0xff) << 16) | ((in[13] & 0xff) << 8) | (in[12] & 0xff);
		mins[Y]=Float.intBitsToFloat(myInt);
		myInt=(in[19] << 24) | ((in[18] & 0xff) << 16) | ((in[17] & 0xff) << 8) | (in[16] & 0xff);
		mins[Z]=Float.intBitsToFloat(myInt);
		myInt=(in[23] << 24) | ((in[22] & 0xff) << 16) | ((in[21] & 0xff) << 8) | (in[20] & 0xff);
		maxs[X]=Float.intBitsToFloat(myInt);
		myInt=(in[27] << 24) | ((in[26] & 0xff) << 16) | ((in[25] & 0xff) << 8) | (in[24] & 0xff);
		maxs[Y]=Float.intBitsToFloat(myInt);
		myInt=(in[31] << 24) | ((in[30] & 0xff) << 16) | ((in[29] & 0xff) << 8) | (in[28] & 0xff);
		maxs[Z]=Float.intBitsToFloat(myInt);
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
