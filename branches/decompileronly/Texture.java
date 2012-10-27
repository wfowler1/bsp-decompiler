// Texture class
//
// An all-encompassing class to handle the texture information of any given BSP format.
// The way texture information is stored varies wildly between versions. As a general
// rule, this class only handles the lump containing the string of a texture's name,
// and data from the same lump associated with it.
// For example, Nightfire's texture lump only contains 64-byte null-padded strings, but
// Quake 2's has texture scaling included, which is lump 17 in Nightfire.

public class Texture extends LumpObject {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	public static final int TYPE_NIGHTFIRE=0;
	public static final int TYPE_QUAKE=1;
	public static final int TYPE_QUAKE2=2;
	public static final int TYPE_QUAKE3=3;
	public static final int TYPE_SOURCE=4;
	public static final int TYPE_SIN=5;
	public static final int TYPE_EF2=6;
	public static final int TYPE_MOHAA=7;
	
	private String name;
	private String mask;
	private byte[] flags;
	private byte[] contents;
	private v42TexMatrix texAxes;
	
	// CONSTRUCTORS
	public Texture(LumpObject in, int type) {
		super(in.getData());
		new Texture(in.getData(), type);
	}
	
	public Texture(byte[] data, int type) {
		super(data);
		switch(type) {
			case TYPE_NIGHTFIRE:
				name=DataReader.readNullTerminatedString(new byte[] { data[0], data[1], data[2], data[3], data[4], data[5], data[6], data[7], data[8], data[9],
				                                                      data[10], data[11], data[12], data[13], data[14], data[15], data[16], data[17], data[18], data[19],
				                                                      data[20], data[21], data[22], data[23], data[24], data[25], data[26], data[27], data[28], data[29],
				                                                      data[30], data[31], data[32], data[33], data[34], data[35], data[36], data[37], data[38], data[39],
				                                                      data[40], data[41], data[42], data[43], data[44], data[45], data[46], data[47], data[48], data[49],
				                                                      data[50], data[51], data[52], data[53], data[54], data[55], data[56], data[57], data[58], data[59],
				                                                      data[60], data[61], data[62], data[63] } );
				break;
			case TYPE_QUAKE:
				int offset0=DataReader.readInt(data[24], data[25], data[26], data[27]);
				if(offset0==0) {
				name=DataReader.readNullTerminatedString(new byte[] { data[0], data[1], data[2], data[3], data[4], data[5], data[6], data[7], data[8], data[9],
				                                                      data[10], data[11], data[12], data[13], data[14], data[15] } );
				} else {
					name="internal_texture"+offset0;
				}
				break;
			case TYPE_QUAKE2:
				texAxes=new v42TexMatrix(DataReader.readFloat(data[0], data[1], data[2], data[3]), DataReader.readFloat(data[4], data[5], data[6], data[7]), DataReader.readFloat(data[8], data[9], data[10], data[11]), 
				                         DataReader.readFloat(data[12], data[13], data[14], data[15]), 
				                         DataReader.readFloat(data[16], data[17], data[18], data[19]), DataReader.readFloat(data[20], data[21], data[22], data[23]), DataReader.readFloat(data[24], data[25], data[26], data[27]), 
				                         DataReader.readFloat(data[28], data[29], data[30], data[31]));
				flags=new byte[] { data[32], data[33], data[34], data[35] };
				name=DataReader.readNullTerminatedString(new byte[] { data[40], data[41], data[42], data[43], data[44], data[45], data[46], data[47], data[48], data[49], 
				                                                      data[50], data[51], data[52], data[53], data[54], data[55], data[56], data[57], data[58], data[59], 
				                                                      data[60], data[61], data[62], data[63], data[64], data[65], data[66], data[67], data[68], data[69], 
				                                                      data[70], data[71] } );
				break;
			case TYPE_MOHAA:
				mask=DataReader.readNullTerminatedString(new byte[] { data[76], data[77], data[78], data[79], data[80], data[81], data[82], data[83], data[84], data[85],
				                                                      data[86], data[87], data[88], data[89], data[90], data[91], data[92], data[93], data[94], data[95],
				                                                      data[96], data[97], data[98], data[99], data[100], data[101], data[102], data[103], data[104], data[105],
				                                                      data[106], data[107], data[108], data[109], data[110], data[111], data[112], data[113], data[114], data[115],
				                                                      data[116], data[117], data[118], data[119], data[120], data[121], data[122], data[123], data[124], data[125],
				                                                      data[126], data[127], data[128], data[129], data[130], data[131], data[132], data[133], data[134], data[135],
				                                                      data[136], data[137], data[138], data[139] } );
			case TYPE_EF2:
			case TYPE_QUAKE3:
				name=DataReader.readNullTerminatedString(new byte[] { data[0], data[1], data[2], data[3], data[4], data[5], data[6], data[7], data[8], data[9],
				                                                      data[10], data[11], data[12], data[13], data[14], data[15], data[16], data[17], data[18], data[19],
				                                                      data[20], data[21], data[22], data[23], data[24], data[25], data[26], data[27], data[28], data[29],
				                                                      data[30], data[31], data[32], data[33], data[34], data[35], data[36], data[37], data[38], data[39],
				                                                      data[40], data[41], data[42], data[43], data[44], data[45], data[46], data[47], data[48], data[49],
				                                                      data[50], data[51], data[52], data[53], data[54], data[55], data[56], data[57], data[58], data[59],
				                                                      data[60], data[61], data[62], data[63] } );
				flags=new byte[] { data[64], data[65], data[66], data[67] };
				contents=new byte[] { data[68], data[69], data[70], data[71] };
				break;
			case TYPE_SOURCE:
				name=DataReader.readString(data);
				break;
			case TYPE_SIN:
				texAxes=new v42TexMatrix(DataReader.readFloat(data[0], data[1], data[2], data[3]), DataReader.readFloat(data[4], data[5], data[6], data[7]), DataReader.readFloat(data[8], data[9], data[10], data[11]), 
				                         DataReader.readFloat(data[12], data[13], data[14], data[15]), 
				                         DataReader.readFloat(data[16], data[17], data[18], data[19]), DataReader.readFloat(data[20], data[21], data[22], data[23]), DataReader.readFloat(data[24], data[25], data[26], data[27]), 
				                         DataReader.readFloat(data[28], data[29], data[30], data[31]));
				flags=new byte[] { data[32], data[33], data[34], data[35] };
				name=DataReader.readNullTerminatedString(new byte[] { data[36], data[37], data[38], data[39], data[40], data[41], data[42], data[43], data[44], data[45], 
				                                                      data[46], data[47], data[48], data[49], data[50], data[51], data[52], data[53], data[54], data[55], 
				                                                      data[56], data[57], data[58], data[59], data[60], data[61], data[62], data[63], data[64], data[65], 
				                                                      data[66], data[67], data[68], data[69], data[70], data[71], data[72], data[73], data[74], data[75], 
				                                                      data[76], data[77], data[78], data[79], data[80], data[81], data[82], data[83], data[84], data[85], 
				                                                      data[86], data[87], data[88], data[89], data[90], data[91], data[92], data[93], data[94], data[95], 
				                                                      data[96], data[97], data[98], data[99] } );
				break;
		}
	}
	
	// METHODS
	
	// ACCESSORS/MUTATORS
	
	public String getName() {
		return name;
	}
	
	public String getMask() {
		return mask;
	}
	
	public byte[] getFlags() {
		return flags;
	}
	
	public byte[] getContents() {
		return contents;
	}
	
	public v42TexMatrix getTexAxes() {
		return texAxes;
	}
}