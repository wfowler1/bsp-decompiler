// SourceDispInfo class

// Holds all the data for a displacement in a Source map.

public class SourceDispInfo {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	// At this point, screw it, I'm just copying names from the Valve developer wiki Source BSP documentation page
	private Vector3D startPosition;
	private int dispVertStart;
	//private int dispTriStart;
	private int power;
	private int[] allowedVerts; // unsigned
	
	// CONSTRUCTORS
	
	// This constructor takes 32 bytes in a byte array, as though
	// it had just been read by a FileInputStream.
	public SourceDispInfo(byte[] in, int type, boolean isVindictus) {
		startPosition=DataReader.readPoint3F(in[0], in[1], in[2], in[3], in[4], in[5], in[6], in[7], in[8], in[9], in[10], in[11]);
		dispVertStart=DataReader.readInt(in[12], in[13], in[14], in[15]);
		//dispTriStart=DataReader.readInt(in[16], in[17], in[18], in[19]);
		power=DataReader.readInt(in[20], in[21], in[22], in[23]);
		allowedVerts=new int[10];
		if(isVindictus) {
			for(int i=0;i<10;i++) {
				allowedVerts[i]=DataReader.readInt(in[192+(i*4)], in[192+(i*4)], in[192+(i*4)], in[192+(i*4)]);
			}
		} else {
			for(int i=0;i<10;i++) {
				allowedVerts[i]=DataReader.readInt(in[136+(i*4)], in[137+(i*4)], in[138+(i*4)], in[139+(i*4)]);
			}
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
