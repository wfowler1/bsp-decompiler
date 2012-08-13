// v38Leaf class

// Holds all the data for a leaf in a Quake 2 map.

public class v38Leaf {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	private int flags;
	private short cluster;
	private short area;
	private Vector3D mins;
	private Vector3D maxs;
	private short firstMarkSurface;
	private short numMarkSurfaces;
	private short firstMarkBrush;
	private short numMarkBrushes;
	
	// CONSTRUCTORS
	
	// This constructor takes all data in their proper data types
	public v38Leaf(int inFlags, short inCluster, short inArea, short inMinX, short inMinY, short inMinZ, short inMaxX,
	               short inMaxY, short inMaxZ, short inFirstMarkSurface, short inNumMarkSurfaces, short inFirstMarkBrush,
	               short inNumMarkBrushes) {
		flags=inFlags;
		cluster=inCluster;
		area=inArea;
		mins=new Vector3D(inMinX, inMinY, inMinZ);
		maxs=new Vector3D(inMaxX, inMaxY, inMaxZ);
		firstMarkSurface=inFirstMarkSurface;
		numMarkSurfaces=inNumMarkSurfaces;
		firstMarkBrush=inFirstMarkBrush;
		numMarkBrushes=inNumMarkBrushes;
	}
	
	// This constructor takes 20 bytes in a byte array, as though
	// it had just been read by a FileInputStream.
	public v38Leaf(byte[] in) {
		flags=(in[3] << 24) | ((in[2] & 0xff) << 16) | ((in[1] & 0xff) << 8) | (in[0] & 0xff);
		cluster=(short)((in[5] << 8) | (in[4] & 0xff));
		area=(short)((in[7] << 8) | (in[6] & 0xff));
		short minX=(short)((in[9] << 8) | (in[8] & 0xff));
		short minY=(short)((in[11] << 8) | (in[10] & 0xff));
		short minZ=(short)((in[13] << 8) | (in[12] & 0xff));
		short maxX=(short)((in[15] << 8) | (in[14] & 0xff));
		short maxY=(short)((in[17] << 8) | (in[16] & 0xff));
		short maxZ=(short)((in[19] << 8) | (in[18] & 0xff));
		mins=new Vector3D(minX, minY, minZ);
		maxs=new Vector3D(maxX, maxY, maxZ);
		firstMarkSurface=(short)((in[21] << 8) | (in[20] & 0xff));
		numMarkSurfaces=(short)((in[23] << 8) | (in[22] & 0xff));
		firstMarkBrush=(short)((in[25] << 8) | (in[24] & 0xff));
		numMarkBrushes=(short)((in[27] << 8) | (in[26] & 0xff));
	}
	
	// METHODS
	
	// ACCESSORS/MUTATORS
	
	public int getFlags() {
		return flags;
	}
	
	public void setFlags(int in) {
		flags=in;
	}
	
	public short getCluster() {
		return cluster;
	}
	
	public void setCluster(short in) {
		cluster=in;
	}
	
	public short getArea() {
		return area;
	}
	
	public void setArea(short in) {
		area=in;
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
	
	public short getFirstMarkSurface() {
		return firstMarkSurface;
	}
	
	public void setFirstMarkSurface(short in) {
		firstMarkSurface=in;
	}
	
	public short getNumMarkSurfaces() {
		return numMarkSurfaces;
	}
	
	public void setNumMarkSurfaces(short in) {
		numMarkSurfaces=in;
	}
	
	public short getFirstMarkBrush() {
		return firstMarkBrush;
	}
	
	public void setFirstMarkBrush(short in) {
		firstMarkBrush=in;
	}
	
	public short getNumMarkBrushes() {
		return numMarkBrushes;
	}
	
	public void setNumMarkBrushes(short in) {
		numMarkBrushes=in;
	}
}
