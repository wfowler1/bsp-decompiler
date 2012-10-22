// SiNTexture class

// Contains all the information for a single SiNTexture object

public class SiNTexture extends v38Texture {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	private float unknown0;
	private int unknown1;
	private float[] unknown2=new float[8];
	private int[] unknown3=new int[8];
	
	// CONSTRUCTORS
	
	// Takes everything exactly as it is stored
	public SiNTexture(Vector3D inU, float inUShift, Vector3D inV, float inVShift, byte[] inFlags, String inName, int inNext, float unknown0, int inUnk, int unknown1, float[] unknown2, int[] unknown3) {
		super(inU, inUShift, inV, inVShift, inFlags, inUnk, inName, inNext);
		this.unknown0=unknown0;
		this.unknown1=unknown1;
		this.unknown2=unknown2;
		this.unknown3=unknown3;
	}
	
	// This constructor takes 76 byes in a byte array, as though
	// it had just been read by a FileInputStream.
	public SiNTexture(byte[] in) {
		super(DataReader.readPoint3F(in[0], in[1], in[2], in[3], in[4], in[5], in[6], in[7], in[8], in[9], in[10], in[11]), DataReader.readFloat(in[12], in[13], in[14], in[15]), 
		      DataReader.readPoint3F(in[16], in[17], in[18], in[19], in[20], in[21], in[22], in[23], in[24], in[25], in[26], in[27]), DataReader.readFloat(in[28], in[29], in[30], in[31]),
		      new byte[] { in[32], in[33], in[34], in[35] }, DataReader.readInt(in[108], in[109], in[110], in[111]), 
		      DataReader.readNullTerminatedString(new byte[] { in[36], in[37], in[38], in[39], in[40], in[41], in[42], in[43], in[44], in[45],
		                                                       in[46], in[47], in[48], in[49], in[50], in[51], in[52], in[53], in[54], in[55],
		                                                       in[56], in[57], in[58], in[59], in[60], in[61], in[62], in[63], in[64], in[65],
		                                                       in[66], in[67], in[68], in[69], in[70], in[71], in[72], in[73], in[74], in[75],
		                                                       in[76], in[77], in[78], in[79], in[80], in[81], in[82], in[83], in[84], in[85],
		                                                       in[86], in[87], in[88], in[89], in[90], in[91], in[92], in[93], in[94], in[95],
		                                                       in[96], in[97], in[98], in[99] } ),
				DataReader.readInt(in[100], in[101], in[102], in[103]));
		this.unknown0=DataReader.readFloat(in[104], in[105], in[106], in[107]);
		this.unknown1=DataReader.readInt(in[112], in[113], in[114], in[115]);
		this.unknown2[0]=DataReader.readFloat(in[116], in[117], in[118], in[119]);
		this.unknown2[1]=DataReader.readFloat(in[120], in[121], in[122], in[123]);
		this.unknown2[2]=DataReader.readFloat(in[124], in[125], in[126], in[127]);
		this.unknown2[3]=DataReader.readFloat(in[128], in[129], in[130], in[131]);
		this.unknown2[4]=DataReader.readFloat(in[132], in[133], in[134], in[135]);
		this.unknown2[5]=DataReader.readFloat(in[136], in[137], in[138], in[139]);
		this.unknown2[6]=DataReader.readFloat(in[140], in[141], in[142], in[143]);
		this.unknown2[7]=DataReader.readFloat(in[144], in[145], in[146], in[147]);
		this.unknown3[0]=DataReader.readInt(in[148], in[149], in[150], in[151]);
		this.unknown3[1]=DataReader.readInt(in[152], in[153], in[154], in[155]);
		this.unknown3[2]=DataReader.readInt(in[156], in[157], in[158], in[159]);
		this.unknown3[3]=DataReader.readInt(in[160], in[161], in[162], in[163]);
		this.unknown3[4]=DataReader.readInt(in[164], in[165], in[166], in[167]);
		this.unknown3[5]=DataReader.readInt(in[168], in[169], in[170], in[171]);
		this.unknown3[6]=DataReader.readInt(in[172], in[173], in[174], in[175]);
		this.unknown3[7]=DataReader.readInt(in[176], in[177], in[178], in[179]);
	}
	
	// METHODS
	
	// ACCESSORS/MUTATORS
	
	public void setUnknown0(float in) {
		unknown0=in;
	}
	
	public float getUnknown0() {
		return unknown0;
	}
	
	public void setUnknown1(int in) {
		unknown1=in;
	}
	
	public int getUnknown1() {
		return unknown1;
	}
	
	public void setUnknown2(float[] in) {
		unknown2=in;
	}
	
	public float[] getUnknown2() {
		return unknown2;
	}
	
	public void setUnknown3(int[] in) {
		unknown3=in;
	}
	
	public int[] getUnknown3() {
		return unknown3;
	}
}