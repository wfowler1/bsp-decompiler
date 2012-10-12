// CoDModel class

// Holds all the data for a model in a Quake 3 map.

public class CoDModel extends v46Model {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	private int unknown0;
	private int unknown1;
	
	// CONSTRUCTORS
	
	// This constructor takes all data in their proper data types
	public CoDModel(Vector3D inMins, Vector3D inMaxs, int unknown0, int unknown1, int inFace, int inNumFaces, int inBrush, int inNumBrushes) {
		super(inMins, inMaxs, inFace, inNumFaces, inBrush, inNumBrushes);
		this.unknown0=unknown0;
		this.unknown1=unknown1;
	}
	
	// This constructor takes 48 bytes in a byte array, as though
	// it had just been read by a FileInputStream.
	public CoDModel(byte[] in) {
		super(DataReader.readPoint3F(in[0], in[1], in[2], in[3], in[4], in[5], in[6], in[7], in[8], in[9], in[10], in[11]), 
		      DataReader.readPoint3F(in[12], in[13], in[14], in[15], in[16], in[17], in[18], in[19], in[20], in[21], in[22], in[23]), 
		      DataReader.readInt(in[32], in[33], in[34], in[35]), 
		      DataReader.readInt(in[36], in[37], in[38], in[39]), 
		      DataReader.readInt(in[40], in[41], in[42], in[43]), 
		      DataReader.readInt(in[44], in[45], in[46], in[47]));
		unknown0=DataReader.readInt(in[24], in[25], in[26], in[27]);
		unknown1=DataReader.readInt(in[28], in[29], in[30], in[31]);
	}
	
	// METHODS
	
	// ACCESSORS/MUTATORS
	public int getUnknown0() {
		return unknown0;
	}
	
	public void setUnknown0(int in) {
		unknown0=in;
	}
	
	public int getUnknown1() {
		return unknown1;
	}
	
	public void setUnknown1(int in) {
		unknown1=in;
	}
}