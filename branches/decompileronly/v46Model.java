// v46Model class

// Holds all the data for a model in a Quake 3 map.

public class v46Model {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	public final int X=0;
	public final int Y=1;
	public final int Z=2;
	
	private Point3D mins;
	private Point3D maxs;
	private int face;
	private int numFaces;
	private int brush;
	private int numBrushes;
	
	// CONSTRUCTORS
	
	// This constructor takes all data in their proper data types
	public v46Model(Point3D inMins, Point3D inMaxs, int inFace, int inNumFaces, int inBrush, int inNumBrushes) {
		mins=inMins;
		maxs=inMaxs;
		face=inFace;
		numFaces=inNumFaces;
		brush=inBrush;
		numBrushes=inNumBrushes;
	}
	
	// This constructor takes 48 bytes in a byte array, as though
	// it had just been read by a FileInputStream.
	public v46Model(byte[] in) {
		float[] point=new float[3];
		int myInt=(in[3] << 24) | ((in[2] & 0xff) << 16) | ((in[1] & 0xff) << 8) | (in[0] & 0xff);
		point[X]=Float.intBitsToFloat(myInt);
		myInt=(in[7] << 24) | ((in[6] & 0xff) << 16) | ((in[5] & 0xff) << 8) | (in[4] & 0xff);
		point[Y]=Float.intBitsToFloat(myInt);
		myInt=(in[11] << 24) | ((in[10] & 0xff) << 16) | ((in[9] & 0xff) << 8) | (in[8] & 0xff);
		point[Z]=Float.intBitsToFloat(myInt);
		mins=new Point3D(point[X], point[Y], point[Z]);
		myInt=(in[15] << 24) | ((in[14] & 0xff) << 16) | ((in[13] & 0xff) << 8) | (in[12] & 0xff);
		point[X]=Float.intBitsToFloat(myInt);
		myInt=(in[19] << 24) | ((in[18] & 0xff) << 16) | ((in[17] & 0xff) << 8) | (in[16] & 0xff);
		point[Y]=Float.intBitsToFloat(myInt);
		myInt=(in[23] << 24) | ((in[22] & 0xff) << 16) | ((in[21] & 0xff) << 8) | (in[20] & 0xff);
		point[Z]=Float.intBitsToFloat(myInt);
		maxs=new Point3D(point[X], point[Y], point[Z]);
		face=(in[27] << 24) | ((in[26] & 0xff) << 16) | ((in[25] & 0xff) << 8) | (in[24] & 0xff);
		numFaces=(in[31] << 24) | ((in[30] & 0xff) << 16) | ((in[29] & 0xff) << 8) | (in[28] & 0xff);
		brush=(in[35] << 24) | ((in[34] & 0xff) << 16) | ((in[33] & 0xff) << 8) | (in[32] & 0xff);
		numBrushes=(in[39] << 24) | ((in[38] & 0xff) << 16) | ((in[37] & 0xff) << 8) | (in[36] & 0xff);
	}
	
	// METHODS
	
	// ACCESSORS/MUTATORS
	public void setMins(Point3D in) {
		mins=in;
	}
	
	public Point3D getMins() {
		return mins;
	}
	
	public void setMaxs(Point3D in) {
		maxs=in;
	}
	
	public Point3D getMaxs() {
		return maxs;
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
	
	public int getBrush() {
		return brush;
	}
	
	public void setBrush(int in) {
		brush=in;
	}
	
	public int getNumBrushes() {
		return numBrushes;
	}
	
	public void setNumBrushes(int in) {
		numBrushes=in;
	}
}