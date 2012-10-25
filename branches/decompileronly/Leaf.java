// Leaf class
// Master class for leaf structures. Only four formats needs leaves in order to be
// decompiled; Source, Nightfire, Quake and Quake 2.

public class Leaf extends LumpObject {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	// Use these in the constructors to specify the type of map the leaf is coming from
	public static final int TYPE_QUAKE=0;
	public static final int TYPE_QUAKE2=1;
	public static final int TYPE_NIGHTFIRE=2;
	public static final int TYPE_SOURCE19=3;
	public static final int TYPE_SOURCE20=4;
	
	// In some formats (Quake 3 is the notable exclusion), leaves must be used to find
	// a list of brushes or faces to create solids out of.
	private byte[] contents;
	private int firstMarkBrush=-1;
	private int numMarkBrushes=-1;
	private int firstMarkFace=-1;
	private int numMarkFaces=-1;
	
	// CONSTRUCTORS
	public Leaf(byte[] data, int type) {
		super(data);
		contents=new byte[] { data[0], data[1], data[2], data[3] }; // This seems universal for all formats (so far). Even Nightfire.
		switch(type) {
			case TYPE_QUAKE2:
			case TYPE_SOURCE19: // Different Source BSP versions use different leaf structures with different sizes.
			case TYPE_SOURCE20: // The relevant fields are always at the same offsets though.
				firstMarkBrush=DataReader.readUShort(data[24], data[25]);
				numMarkBrushes=DataReader.readUShort(data[26], data[27]);
			case TYPE_QUAKE:
				firstMarkFace=DataReader.readUShort(data[20], data[21]);
				numMarkFaces=DataReader.readUShort(data[22], data[23]);
				break;
			case TYPE_NIGHTFIRE:
				firstMarkFace=DataReader.readInt(data[32], data[33], data[34], data[35]);
				numMarkFaces=DataReader.readInt(data[36], data[37], data[38], data[39]);
				firstMarkBrush=DataReader.readInt(data[40], data[41], data[42], data[43]);
				numMarkBrushes=DataReader.readInt(data[44], data[45], data[46], data[47]);
				break;
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