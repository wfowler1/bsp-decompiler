// SourceDispInfo class

// Holds all the data for a displacement in a Source map.

public class SourceDispInfo {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	// At this point, screw it, I'm just copying names from the Valve developer wiki Source BSP documentation page
	private Vector3D startPosition;
	private int dispVertStart;
	private int dispTriStart;
	private int power;
	private int minTess;
	private float smoothingAngle;
	private int contents;
	private int mapFace; // unsigned short, integer boxed to avoid issues with sign
	private int lightmapAlphaStart;
	private int lightmapSamplePositionStart;
	private byte[] neighborInfo=new byte[90];
	private long[] allowedVerts; // unsigned
	
	// CONSTRUCTORS
	
	// This constructor takes all data in their proper data types
	//public SourceDispInfo() {
		// Fuck it
	//}
	
	// This constructor takes 32 bytes in a byte array, as though
	// it had just been read by a FileInputStream.
	public SourceDispInfo(byte[] in) {
		startPosition=DataReader.readPoint3F(in[0], in[1], in[2], in[3], in[4], in[5], in[6], in[7], in[8], in[9], in[10], in[11]);
		dispVertStart=DataReader.readInt(in[12], in[13], in[14], in[15]);
		dispTriStart=DataReader.readInt(in[16], in[17], in[18], in[19]);
		power=DataReader.readInt(in[20], in[21], in[22], in[23]);
		minTess=DataReader.readInt(in[24], in[25], in[26], in[27]);
		smoothingAngle=DataReader.readFloat(in[28], in[29], in[30], in[31]);
		contents=DataReader.readInt(in[32], in[33], in[34], in[35]);
		mapFace=DataReader.readUShort(in[36], in[37]);
		lightmapAlphaStart=DataReader.readInt(in[38], in[39], in[40], in[41]);
		lightmapSamplePositionStart=DataReader.readInt(in[42], in[43], in[44], in[45]);
		allowedVerts=new long[5];
		for(int i=0;i<90;i++) {
			neighborInfo[i]=in[46+i];
		}
		for(int i=0;i<5;i++) {
			allowedVerts[i]=DataReader.readLong(in[136+(i*8)], in[137+(i*8)], in[138+(i*8)], in[139+(i*8)], in[140+(i*8)], in[141+(i*8)], in[142+(i*8)], in[143+(i*8)]); // unsigned
		}
	}
	
	// METHODS
	
	// ACCESSORS/MUTATORS
	
	public Vector3D getStartPosition() {
		return startPosition;
	}
	
	public void setStartPosition(Vector3D in) {
		startPosition=in;
	}
	
	public int getDispVertStart() {
		return dispVertStart;
	}
	
	public void setDispVertStart(int in) {
		dispVertStart=in;
	}
	
	public int getDispTriStart() {
		return dispTriStart;
	}
	
	public void setDispTriStart(int in) {
		dispTriStart=in;
	}
	
	public int getPower() {
		return power;
	}

	public void setPower(int in) {
		power=in;
	}
	
	public int getMinTess() {
		return minTess;
	}
	
	public void setMinTess(int in) {
		minTess=in;
	}
	
	public float getSmoothingAngle() {
		return smoothingAngle;
	}
	
	public void setSmoothingAngle(float in) {
		smoothingAngle=in;
	}
	
	public int getContents() {
		return contents;
	}

	public void setContents(int in) {
		contents=in;
	}
	
	public int getMapFace() {
		return mapFace;
	}
	
	public void setMapFace(int in) {
		mapFace=in;
	}
	
	public int getLightmapAlphaStart() {
		return lightmapAlphaStart;
	}
	
	public void setLightmapAlphaStart(int in) {
		lightmapAlphaStart=in;
	}
	
	public int getLightmapSamplePositionStart() {
		return lightmapSamplePositionStart;
	}
	
	public void setLightmapSamplePositionStart(int in) {
		lightmapSamplePositionStart=in;
	}
	
	public byte[] getNeighborInfo() {
		return neighborInfo;
	}
	
	public void setNeighborInfo(byte[] in) {
		neighborInfo=in;
	}
	
	public long[] getAllowedVerts() {
		return allowedVerts;
	}
	
	public void setAllowedVerts(long[] in) {
		allowedVerts=in;
	}
}
