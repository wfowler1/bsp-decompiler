// SourceDispInfo class

// Holds all the data for a displacement in a Source map.

public class SourceDispInfo extends LumpObject {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	// At this point, screw it, I'm just copying names from the Valve developer wiki Source BSP documentation page
	private Vector3D startPosition;
	private int dispVertStart;
	//private int dispTriStart;
	private int power;
	private int[] allowedVerts; // unsigned
	
	// CONSTRUCTORS
	
	// This constructor takes bytes in a byte array, as though
	// it had just been read by a FileInputStream.
	public SourceDispInfo(byte[] in, int type) {
		super(in);
		startPosition=DataReader.readPoint3F(in[0], in[1], in[2], in[3], in[4], in[5], in[6], in[7], in[8], in[9], in[10], in[11]);
		dispVertStart=DataReader.readInt(in[12], in[13], in[14], in[15]);
		//dispTriStart=DataReader.readInt(in[16], in[17], in[18], in[19]);
		power=DataReader.readInt(in[20], in[21], in[22], in[23]);
		allowedVerts=new int[10];
		int offset=0;
		switch(type) {
			case BSP.TYPE_SOURCE17:
			case BSP.TYPE_SOURCE18:
			case BSP.TYPE_SOURCE19:
			case BSP.TYPE_SOURCE20:
			case BSP.TYPE_SOURCE21:
			case BSP.TYPE_TACTICALINTERVENTION:
			case BSP.TYPE_DMOMAM:
				offset=136;
				break;
			case BSP.TYPE_SOURCE22:
				offset=140;
				break;
			case BSP.TYPE_SOURCE23:
				offset=144;
				break;
			case BSP.TYPE_VINDICTUS:
				offset=192;
				break;
		}
		for(int i=0;i<10;i++) {
			allowedVerts[i]=DataReader.readInt(in[offset+(i*4)], in[offset+1+(i*4)], in[offset+2+(i*4)], in[offset+3+(i*4)]);
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
	
	/*public int getDispTriStart() {
		return dispTriStart;
	}
	
	public void setDispTriStart(int in) {
		dispTriStart=in;
	}*/
	
	public int getPower() {
		return power;
	}

	public void setPower(int in) {
		power=in;
	}
	
	public int[] getAllowedVerts() {
		return allowedVerts;
	}
	
	public void setAllowedVerts(int[] in) {
		allowedVerts=in;
	}
}
