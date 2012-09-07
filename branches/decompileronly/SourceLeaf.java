// SourceLeaf class

// Holds all the data for a leaf in a Quake 2 map.

public class SourceLeaf {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	private int contents;
	private short cluster;
	private short areaFlags; // These apparently share one 16-bit field, with area taking 9 bits and flags taking 7. Stupid. Good thing I don't need either.
	private Vector3D mins;
	private Vector3D maxs;
	private short firstMarkSurface;
	private short numMarkSurfaces;
	private short firstMarkBrush;
	private short numMarkBrushes;
	private short leafWaterDataID;
	private byte[] compressedLightCube=new byte[24];
	private short padding;
	
	private int version;
	
	// CONSTRUCTORS
	
	// This constructor takes all data in their proper data types
	public SourceLeaf(int inContents, short inCluster, short inAreaFlags, short inMinX, short inMinY, short inMinZ, short inMaxX,
	               short inMaxY, short inMaxZ, short inFirstMarkSurface, short inNumMarkSurfaces, short inFirstMarkBrush,
	               short inNumMarkBrushes, short inLeafWaterDataID, short inPadding) {
		contents=inContents;
		cluster=inCluster;
		areaFlags=inAreaFlags;
		mins=new Vector3D(inMinX, inMinY, inMinZ);
		maxs=new Vector3D(inMaxX, inMaxY, inMaxZ);
		firstMarkSurface=inFirstMarkSurface;
		numMarkSurfaces=inNumMarkSurfaces;
		firstMarkBrush=inFirstMarkBrush;
		numMarkBrushes=inNumMarkBrushes;
		leafWaterDataID=inLeafWaterDataID;
		padding=inPadding;
	}
	
	// This constructor takes 32 or 56 bytes in a byte array, as though
	// it had just been read by a FileInputStream.
	public SourceLeaf(byte[] in, int version) {
		if(version==18 || version==19) {
			contents=(in[3] << 24) | ((in[2] & 0xff) << 16) | ((in[1] & 0xff) << 8) | (in[0] & 0xff);
			cluster=(short)((in[5] << 8) | (in[4] & 0xff));
			areaFlags=(short)((in[7] << 8) | (in[6] & 0xff));
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
			leafWaterDataID=(short)((in[29] << 8) | (in[28] & 0xff));
			for(int i=0;i<24;i++) {
				compressedLightCube[i]=in[30+i];
			}
			padding=(short)((in[55] << 8) | (in[54] & 0xff));
		} else {
			contents=(in[3] << 24) | ((in[2] & 0xff) << 16) | ((in[1] & 0xff) << 8) | (in[0] & 0xff);
			cluster=(short)((in[5] << 8) | (in[4] & 0xff));
			areaFlags=(short)((in[7] << 8) | (in[6] & 0xff));
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
			leafWaterDataID=(short)((in[29] << 8) | (in[28] & 0xff));
			padding=(short)((in[31] << 8) | (in[30] & 0xff));
		}
	}
	
	// METHODS
	
	// ACCESSORS/MUTATORS
	
	public int getContents() {
		return contents;
	}
	
	public void setContents(int in) {
		contents=in;
	}
	
	public short getCluster() {
		return cluster;
	}
	
	public void setCluster(short in) {
		cluster=in;
	}
	
	public short getAreaFlags() {
		return areaFlags;
	}
	
	public void setAreaFlags(short in) {
		areaFlags=in;
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
	
	public short getLeafWaterDataID() {
		return leafWaterDataID;
	}
	
	public void setLeafWaterDataID(short in) {
		leafWaterDataID=in;
	}
	
	public byte[] getCompressedLightCube() {
		return compressedLightCube;
	}
	
	public void setCompressedLightCube(byte[] in) {
		compressedLightCube=in;
	}
	
	public short getPadding() {
		return padding;
	}
	
	public void setPadding(short in) {
		padding=in;
	}
}
