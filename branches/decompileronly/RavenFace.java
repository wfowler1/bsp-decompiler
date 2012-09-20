// RavenFace class

// Holds all the data for a face (surface).

public class RavenFace extends v46Face {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	private byte[] unknown1=new byte[4];
	private byte[] unknown2=new byte[4];
	private int[] unknown3=new int[3];
	private float[] unknown4=new float[6];
	
	// CONSTRUCTORS
	
	// This constructor takes all data in their proper data types
	public RavenFace(int texture, int effect, int type, int vertex, int numVertexes, int meshvert, int numMeshverts, byte[] unknown1, byte[] unknown2,
	               int lm_index, int[] unknown3, int lm_startX, int lm_startY, int lm_sizeX, int lm_sizeY, float[] unknown4, Vector3D lm_origin,
	               Vector3D lm_s, Vector3D lm_t, Vector3D normal, int sizeX, int sizeY) {
		super(texture, effect, type, vertex, numVertexes, meshvert, numMeshverts, lm_index, lm_startX, lm_startY, lm_sizeX, lm_sizeY, lm_origin, lm_s, lm_t, normal, sizeX, sizeY);
		this.unknown1=unknown1;
		this.unknown2=unknown2;
		this.unknown3=unknown3;
		this.unknown4=unknown4;
	}
	
	// This constructor takes bytes in a byte array, as though
	// it had just been read by a FileInputStream.
	public RavenFace(byte[] in) {
		super(DataReader.readInt(in[0], in[1], in[2], in[3]),
		      DataReader.readInt(in[4], in[5], in[6], in[7]), 
		      DataReader.readInt(in[8], in[9], in[10], in[11]), 
		      DataReader.readInt(in[12], in[13], in[14], in[15]), 
		      DataReader.readInt(in[16], in[17], in[18], in[19]), 
		      DataReader.readInt(in[20], in[21], in[22], in[23]), 
		      DataReader.readInt(in[24], in[25], in[26], in[27]), 
		      DataReader.readInt(in[36], in[37], in[38], in[39]), 
		      DataReader.readInt(in[52], in[53], in[54], in[55]), 
		      DataReader.readInt(in[56], in[57], in[58], in[59]), 
		      DataReader.readInt(in[60], in[61], in[62], in[63]),
		      DataReader.readInt(in[64], in[65], in[66], in[67]), 
				DataReader.readPoint3F(in[92], in[93], in[94], in[95], in[96], in[97], in[98], in[99], in[100], in[101], in[102], in[103]),
				DataReader.readPoint3F(in[104], in[105], in[106], in[107], in[108], in[109], in[110], in[111], in[112], in[113], in[114], in[115]),
				DataReader.readPoint3F(in[116], in[117], in[118], in[119], in[120], in[121], in[122], in[123], in[124], in[125], in[126], in[127]),
				DataReader.readPoint3F(in[128], in[129], in[130], in[131], in[132], in[133], in[134], in[135], in[136], in[137], in[138], in[139]),
				DataReader.readInt(in[140], in[141], in[142], in[143]),
				DataReader.readInt(in[144], in[145], in[146], in[147]));
		unknown1[0]=in[28];
		unknown1[1]=in[29];
		unknown1[2]=in[30];
		unknown1[3]=in[31];
		unknown2[0]=in[32];
		unknown2[1]=in[33];
		unknown2[2]=in[34];
		unknown2[3]=in[35];
		for(int i=0;i<3;i++) {
			unknown3[i]=DataReader.readInt(in[(i*4)+40], in[(i*4)+41], in[(i*4)+42], in[(i*4)+43]);
		}
		for(int i=0;i<6;i++) {
			unknown4[i]=DataReader.readFloat(in[(i*4)+68], in[(i*4)+69], in[(i*4)+70], in[(i*4)+71]);
		}
	}
	
	// METHODS
	
	// ACCESSORS/MUTATORS
	
	public void setUnknown1(byte[] in) {
		unknown1=in;
	}
	
	public byte[] getUnknown1() {
		return unknown1;
	}
	
	public void setUnknown2(byte[] in) {
		unknown2=in;
	}
	
	public byte[] getUnknown2() {
		return unknown2;
	}
	
	public void setUnknown3(int[] in) {
		unknown3=in;
	}
	
	public int[] getUnknown3() {
		return unknown3;
	}
	
	public void setUnknown4(float[] in) {
		unknown4=in;
	}
	
	public float[] getUnknown4() {
		return unknown4;
	}
}
