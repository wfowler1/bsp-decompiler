// v38Leaf class

// Holds all the data for a leaf in a Quake 2 map.

public class v38Leaf {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	private int flags;
	private short cluster;
	private short area;
	private short minX; // Yes, Quake 2 handles the bounding box as shorts. I'm not sure
	private short minY; // about whether to add a constructor for shorts in the Point3D
	private short minZ; // class or whether to just do this. So I'm doing this.
	private short maxX;
	private short maxY;
	private short maxZ;
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
		minX=inMinX;
		minY=inMinY;
		minZ=inMinZ;
		maxX=inMaxX;
		maxY=inMaxY;
		maxZ=inMaxZ;
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
		minX=(short)((in[9] << 8) | (in[8] & 0xff));
		minY=(short)((in[11] << 8) | (in[10] & 0xff));
		minZ=(short)((in[13] << 8) | (in[12] & 0xff));
		maxX=(short)((in[15] << 8) | (in[14] & 0xff));
		maxY=(short)((in[17] << 8) | (in[16] & 0xff));
		maxZ=(short)((in[19] << 8) | (in[18] & 0xff));
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
	
	public short getMinX() {
		return minX;
	}
	
	public void setMinX(short in) {
		minX=in;
	}
	
	public short getMinY() {
		return minY;
	}
	
	public void setMinY(short in) {
		minY=in;
	}
	
	public short getMinZ() {
		return minZ;
	}
	
	public void setMinZ(short in) {
		minZ=in;
	}
	
	public short getMaxX() {
		return maxX;
	}
	
	public void setMaxX(short in) {
		maxX=in;
	}
	
	public short getMaxY() {
		return maxY;
	}
	
	public void setMaxY(short in) {
		maxY=in;
	}
	
	public short getMaxZ() {
		return maxZ;
	}
	
	public void setMaxZ(short in) {
		maxZ=in;
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
