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
	public Leaf(LumpObject in, int type, boolean isVindictus) {
		super(in.getData());
		new Leaf(in.getData(), type, isVindictus);
	}
	
	public Leaf(byte[] data, int type, boolean isVindictus) {
		super(data);
		if(isVindictus) {
			contents=new byte[] { data[0], data[1], data[2], data[3] };
			firstMarkBrush=DataReader.readInt(data[44], data[45], data[46], data[47]);
			numMarkBrushes=DataReader.readInt(data[48], data[49], data[50], data[51]);
		} else {
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
					contents=new byte[] { data[0], data[1], data[2], data[3] };
					firstMarkBrush=DataReader.readUShort(data[24], data[25]);
					numMarkBrushes=DataReader.readUShort(data[26], data[27]);
				case BSP.TYPE_QUAKE:
					firstMarkFace=DataReader.readUShort(data[20], data[21]);
					numMarkFaces=DataReader.readUShort(data[22], data[23]);
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