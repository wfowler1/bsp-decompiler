// v38Node class

// Holds all the data for a node in a Quake 2 map.

public class v38Node {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	private int plane;
	private int child1;
	private int child2;
	private short[] mins;
	private short[] maxs;
	private short firstFace;
	private short numFaces;
	
	public final int X=0;
	public final int Y=1;
	public final int Z=2;
	
	// CONSTRUCTORS
	
	// This constructor takes all data in their proper data types
	public v38Node(int plane, int child1, int child2, short[] mins, short[] maxs, short firstFace, short numFaces) {
		this.plane=plane;
		this.child1=child1;
		this.child2=child2;
		this.mins=mins;
		this.maxs=maxs;
		this.firstFace=firstFace;
		this.numFaces=numFaces;
	}
	
	// This constructor takes 28 bytes in a byte array, as though
	// it had just been read by a FileInputStream.
	public v38Node(byte[] in) {
		plane=DataReader.readInt(in[0], in[1], in[2], in[3]);
		child1=DataReader.readInt(in[4], in[5], in[6], in[7]);
		child2=DataReader.readInt(in[8], in[9], in[10], in[11]);
		mins=new short[3];
		maxs=new short[3];
		mins[X]=DataReader.readShort(in[12], in[13]);
		mins[Y]=DataReader.readShort(in[14], in[15]);
		mins[Z]=DataReader.readShort(in[16], in[17]);
		maxs[X]=DataReader.readShort(in[18], in[19]);
		maxs[Y]=DataReader.readShort(in[20], in[21]);
		maxs[Z]=DataReader.readShort(in[22], in[23]);
		firstFace=DataReader.readShort(in[24], in[25]);
		numFaces=DataReader.readShort(in[26], in[27]);
	}
	
	// METHODS
	
	// ACCESSORS/MUTATORS
	
	public int getPlane() {
		return plane;
	}
	
	public void setPlane(int in) {
		plane=in;
	}
	
	public int getChild1() {
		return child1;
	}
	
	public void setChild1(int in) {
		child1=in;
	}
	
	public int getChild2() {
		return child2;
	}
	
	public void setChild2(int in) {
		child2=in;
	}
	
	public short getMinX() {
		return mins[X];
	}
	
	public void setMinX(short in) {
		mins[X]=in;
	}
	
	public short getMinY() {
		return mins[Y];
	}
	
	public void setMinY(short in) {
		mins[Y]=in;
	}
	
	public short getMinZ() {
		return mins[Z];
	}
	
	public void setMinZ(short in) {
		mins[Z]=in;
	}
	
	public short[] getMins() {
		return mins;
	}
	
	public void setMins(short[] in) {
		mins=in;
	}
	
	public short getMaxX() {
		return maxs[X];
	}
	
	public void setMaxX(short in) {
		maxs[X]=in;
	}
	
	public short getMaxY() {
		return maxs[Y];
	}
	
	public void setMaxY(short in) {
		maxs[Y]=in;
	}
	
	public short getMaxZ() {
		return maxs[Z];
	}
	
	public void setMaxZ(short in) {
		maxs[Z]=in;
	}
	
	public short[] getMaxs() {
		return maxs;
	}
	
	public void setMaxs(short[] in) {
		maxs=in;
	}
	
	public short getFirstFace() {
		return firstFace;
	}
	
	public void setFirstFace(short in) {
		firstFace=in;
	}
	
	public short getNumFaces() {
		return numFaces;
	}
	
	public void setNumFaces(short in) {
		numFaces=in;
	}
}
