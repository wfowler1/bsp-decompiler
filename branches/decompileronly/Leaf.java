// Leaf class
// Master class for leaf structures. Only four formats needs leaves in order to be
// decompiled; Source, Nightfire, Quake and Quake 2.

public class Leaf extends LumpObject {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	// In some formats (Quake 3 is the notable exclusion), leaves must be used to find
	// a list of brushes or faces to create solids out of.
	private byte[] contents;
	private int firstMarkBrush=-1;
	private int numMarkBrushes=-1;
	private int firstMarkFace=-1;
	private int numMarkFaces=-1;
	
	// CONSTRUCTORS
	public Leaf(LumpObject in, int type) {
		super(in.getData());
		new Leaf(in.getData(), type);
	}
	
	public Leaf(byte[] data, int type) {
		super(data);
		switch(type) {
			case BSP.TYPE_SOF:
				contents=new byte[] { data[0], data[1], data[2], data[3] };
				firstMarkFace=DataReader.readUShort(data[22], data[23]);
				numMarkFaces=DataReader.readUShort(data[24], data[25]);
				firstMarkBrush=DataReader.readUShort(data[26], data[27]);
				numMarkBrushes=DataReader.readUShort(data[28], data[29]);
				break;
			case BSP.TYPE_QUAKE2:
			case BSP.TYPE_SIN:
			case BSP.TYPE_SOURCE17:
			case BSP.TYPE_SOURCE18:
			case BSP.TYPE_SOURCE19:
			case BSP.TYPE_SOURCE20:
			case BSP.TYPE_SOURCE21:
			case BSP.TYPE_SOURCE22:
			case BSP.TYPE_SOURCE23:
			case BSP.TYPE_TACTICALINTERVENTION:
			case BSP.TYPE_DMOMAM:
			case BSP.TYPE_DAIKATANA:
				contents=new byte[] { data[0], data[1], data[2], data[3] };
				firstMarkBrush=DataReader.readUShort(data[24], data[25]);
				numMarkBrushes=DataReader.readUShort(data[26], data[27]);
			case BSP.TYPE_QUAKE:
				firstMarkFace=DataReader.readUShort(data[20], data[21]);
				numMarkFaces=DataReader.readUShort(data[22], data[23]);
				break;
			case BSP.TYPE_VINDICTUS:
				contents=new byte[] { data[0], data[1], data[2], data[3] };
				firstMarkBrush=DataReader.readInt(data[44], data[45], data[46], data[47]);
				numMarkBrushes=DataReader.readInt(data[48], data[49], data[50], data[51]);
				break;
			case BSP.TYPE_NIGHTFIRE:
				contents=new byte[] { data[0], data[1], data[2], data[3] };
			case BSP.TYPE_QUAKE3:
			case BSP.TYPE_FAKK:
			case BSP.TYPE_STEF2DEMO:
			case BSP.TYPE_STEF2:
			case BSP.TYPE_MOHAA:
			case BSP.TYPE_RAVEN:
				firstMarkFace=DataReader.readInt(data[32], data[33], data[34], data[35]);
				numMarkFaces=DataReader.readInt(data[36], data[37], data[38], data[39]);
				firstMarkBrush=DataReader.readInt(data[40], data[41], data[42], data[43]);
				numMarkBrushes=DataReader.readInt(data[44], data[45], data[46], data[47]);
				break;
		}
	}
	
	// METHODS
	public static Lump<Leaf> createLump(byte[] in, int type) throws java.lang.InterruptedException {
		int structLength=0;
		switch(type) {
			case BSP.TYPE_QUAKE:
			case BSP.TYPE_QUAKE2:
			case BSP.TYPE_SIN:
				structLength=28;
				break;
			case BSP.TYPE_SOURCE17:
			case BSP.TYPE_SOURCE20:
			case BSP.TYPE_SOURCE21:
			case BSP.TYPE_SOURCE22:
			case BSP.TYPE_SOURCE23:
			case BSP.TYPE_TACTICALINTERVENTION:
			case BSP.TYPE_SOF:
			case BSP.TYPE_DAIKATANA:
			case BSP.TYPE_DMOMAM:
				structLength=32;
				break;
			case BSP.TYPE_VINDICTUS:
				structLength=56;
				break;
			case BSP.TYPE_COD:
				structLength=36;
				break;
			case BSP.TYPE_NIGHTFIRE:
			case BSP.TYPE_QUAKE3:
			case BSP.TYPE_FAKK:
			case BSP.TYPE_STEF2DEMO:
			case BSP.TYPE_STEF2:
			case BSP.TYPE_RAVEN:
				structLength=48;
				break;
			case BSP.TYPE_SOURCE18:
			case BSP.TYPE_SOURCE19:
				structLength=56;
				break;
			case BSP.TYPE_MOHAA:
				structLength=64;
				break;
			default:
				structLength=0; // This will cause the shit to hit the fan.
		}
		int offset=0;
		Leaf[] elements=new Leaf[in.length/structLength];
		byte[] bytes=new byte[structLength];
		for(int i=0;i<elements.length;i++) {
			if(Thread.currentThread().interrupted()) {
				throw new java.lang.InterruptedException("while populating Leaf array");
			}
			for(int j=0;j<structLength;j++) {
				bytes[j]=in[offset+j];
			}
			elements[i]=new Leaf(bytes, type);
			offset+=structLength;
		}
		return new Lump<Leaf>(elements, in.length, structLength);
	}
	
	// ACCESSORS/MUTATORS
	public byte[] getContents() {
		return contents;
	}
	
	public int getFirstMarkBrush() {
		return firstMarkBrush;
	}
	
	public int getNumMarkBrushes() {
		return numMarkBrushes;
	}
	
	public int getFirstMarkFace() {
		return firstMarkFace;
	}
	
	public int getNumMarkFaces() {
		return numMarkFaces;
	}
}