// SourceNode class

// Holds all the data for a node in a Source map.
// Funny thing is it's almost identical to the Quake 2 node format

public class SourceNode {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	private int plane; // I'm forced to wonder why they used an int here, and a ushort elsewhere. Oh well, I'm glad they did.
	private int child1;
	private int child2;
	private Vector3D mins;
	private Vector3D maxs;
	private short firstFace;
	private short numFaces;
	private short area;
	private short padding;
	
	public final int X=0;
	public final int Y=1;
	public final int Z=2;
	
	// CONSTRUCTORS
	
	// This constructor takes all data in their proper data types
	public SourceNode(int plane, int child1, int child2, Vector3D mins, Vector3D maxs, short firstFace, short numFaces, short area, short padding) {
		this.plane=plane;
		this.child1=child1;
		this.child2=child2;
		this.mins=mins;
		this.maxs=maxs;
		this.firstFace=firstFace;
		this.numFaces=numFaces;
		this.area=area;
		this.padding=padding;
	}
	
	// This constructor takes 32 bytes in a byte array, as though
	// it had just been read by a FileInputStream.
	public SourceNode(byte[] in) {
		plane=DataReader.readInt(in[0], in[1], in[2], in[3]);
		child1=DataReader.readInt(in[4], in[5], in[6], in[7]);
		child2=DataReader.readInt(in[8], in[9], in[10], in[11]);
		short[] mins=new short[3];
		short[] maxs=new short[3];
		mins[X]=DataReader.readShort(in[12], in[13]);
		mins[Y]=DataReader.readShort(in[14], in[15]);
		mins[Z]=DataReader.readShort(in[16], in[17]);
		this.mins=new Vector3D(mins);
		maxs[X]=DataReader.readShort(in[18], in[19]);
		maxs[Y]=DataReader.readShort(in[20], in[21]);
		maxs[Z]=DataReader.readShort(in[22], in[23]);
		this.maxs=new Vector3D(maxs);
		firstFace=DataReader.readShort(in[24], in[25]);
		numFaces=DataReader.readShort(in[26], in[27]);
		area=DataReader.readShort(in[28], in[29]);
		padding=DataReader.readShort(in[30], in[31]);
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
	
	public double getMinX() {
		return mins.getX();
	}
	
	public void setMinX(short in) {
		mins.setX((double)in);
	}
	
	public double getMinY() {
		return mins.getY();
	}
	
	public void setMinY(short in) {
		mins.setY((double)in);
	}
	
	public double getMinZ() {
		return mins.getX();
	}
	
	public void setMinZ(short in) {
		mins.setZ((double)in);
	}
	
	public Vector3D getMins() {
		return mins;
	}
	
	public void setMins(Vector3D in) {
		mins=in;
	}
	
	public double getMaxX() {
		return maxs.getX();
	}
	
	public void setMaxX(short in) {
		maxs.setX((double)in);
	}
	
	public double getMaxY() {
		return maxs.getY();
	}
	
	public void setMaxY(short in) {
		maxs.setY((double)in);
	}
	
	public double getMaxZ() {
		return maxs.getZ();
	}
	
	public void setMaxZ(short in) {
		maxs.setZ((double)in);
	}
	
	public Vector3D getMaxs() {
		return maxs;
	}
	
	public void setMaxs(Vector3D in) {
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
	
	public short getArea() {
		return area;
	}
	
	public void setArea(short in) {
		area=in;
	}
	
	public short getpadding() {
		return padding;
	}
	
	public void setPadding(short in) {
		padding=in;
	}
}
