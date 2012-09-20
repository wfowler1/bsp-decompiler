// RavenVertex class

// Contains all data on a single Vertex object in the Raven BSP.

public class RavenVertex extends v46Vertex {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	private float[] unknowns=new float[6];
	private int[] unknownInts=new int[3];
	
	// CONSTRUCTORS
	
	// This constructor takes all data in their proper data types
	public RavenVertex(Vector3D vertex, float surf_texCoordX, float surf_texCoordY, float lm_texCoordX, float lm_texCoordY, float[] unknowns, Vector3D normal, byte[] color, int[] unknownInts) {
		super(vertex, surf_texCoordX, surf_texCoordY, lm_texCoordX, lm_texCoordY, normal, color);
		this.unknowns=unknowns;
		this.unknownInts=unknownInts;
	}
	
	// This constructor takes 20 bytes in a byte array, as though
	// it had just been read by a FileInputStream.
	public RavenVertex(byte[] in) {
		super(DataReader.readPoint3F(in[0], in[1], in[2], in[3], in[4], in[5], in[6], in[7], in[8], in[9], in[10], in[11]),
		      DataReader.readFloat(in[12], in[13], in[14], in[15]), DataReader.readFloat(in[16], in[17], in[18], in[19]),
				DataReader.readFloat(in[20], in[21], in[22], in[23]), DataReader.readFloat(in[24], in[25], in[26], in[27]),
		      DataReader.readPoint3F(in[52], in[53], in[54], in[55], in[56], in[57], in[58], in[59], in[60], in[61], in[62], in[63]),
		      new byte[] { in[64], in[65], in[66], in[67] } );
		for(int i=0;i<6;i++) {
			unknowns[i]=DataReader.readFloat(in[(i*4)+28], in[(i*4)+29], in[(i*4)+30], in[(i*4)+31]);
		}
		for(int i=0;i<3;i++) {
			unknowns[i]=DataReader.readFloat(in[(i*4)+68], in[(i*4)+69], in[(i*4)+70], in[(i*4)+71]);
		}
	}
	
	// METHODS
	
	// ACCESSORS/MUTATORS
	
	public void setUnknowns(float[] in) {
		unknowns=in;
	}
	
	public float[] getUnknowns() {
		return unknowns;
	}
	
	public void setUnknownInts(int[] in) {
		unknownInts=in;
	}
	
	public int[] getUnknownInts() {
		return unknownInts;
	}
}