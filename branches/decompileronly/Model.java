// Model class
// An attempt at an all-encompassing model class containing all data needed for any
// given models lump in any given BSP. This is accomplished by throwing out all
// unnecessary information and keeping any relevant information.
//
// Some BSP formats hold the relevant information in different ways, so this will
// will handle any given format and will always be sufficient in one way or another
// to point to all the necessary data.

public class Model extends LumpObject {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	// In general, we need to use models to find one or more leaves containing the
	// information for the solids described by this model. Some formats do it by
	// referencing a head node to iterate through and find the leaves. Others
	// directly point to a set of leaves, and still others simply directly reference
	// brushes. The ideal format simply points to brush information from here (Quake
	// 3), but most of them don't.
	private int headNode=-1; // Quake, Half-life, Quake 2, SiN
	private int firstLeaf=-1; // 007 nightfire
	private int numLeaves=-1;
	private int firstBrush=-1; // Quake 3 and derivatives
	private int numBrushes=-1;
	private int firstFace=-1; // Quake/GoldSrc
	private int numFaces=-1;
	
	// CONSTRUCTORS
	public Model(LumpObject in, int type) {
		super(in.getData());
		new Model(in.getData(), type);
	}

	public Model(byte[] data, int type) {
		super(data);
		switch(type) {
			case BSP.TYPE_QUAKE:
				firstFace=DataReader.readInt(data[56], data[57], data[58], data[59]);
				numFaces=DataReader.readInt(data[60], data[61], data[62], data[63]);
			case BSP.TYPE_QUAKE2:
			case BSP.TYPE_DAIKATANA:
			case BSP.TYPE_SIN:
			case BSP.TYPE_SOF:
			case BSP.TYPE_SOURCE17:
			case BSP.TYPE_SOURCE18:
			case BSP.TYPE_SOURCE19:
			case BSP.TYPE_SOURCE20:
			case BSP.TYPE_SOURCE21:
			case BSP.TYPE_SOURCE22:
			case BSP.TYPE_SOURCE23:
			case BSP.TYPE_VINDICTUS:
				// In all these formats, the "head node" index comes after 9 floats.
				headNode=DataReader.readInt(data[36], data[37], data[38], data[39]);
				break;
			case BSP.TYPE_DMOMAM:
				headNode=DataReader.readInt(data[40], data[41], data[42], data[43]);
				break;
			case BSP.TYPE_NIGHTFIRE:
				firstLeaf=DataReader.readInt(data[40], data[41], data[42], data[43]);
				numLeaves=DataReader.readInt(data[44], data[45], data[46], data[47]);
				break;
			case BSP.TYPE_STEF2:
			case BSP.TYPE_STEF2DEMO:
			case BSP.TYPE_QUAKE3:
			case BSP.TYPE_MOHAA:
			case BSP.TYPE_RAVEN:
			case BSP.TYPE_FAKK:
				firstBrush=DataReader.readInt(data[32], data[33], data[34], data[35]);
				numBrushes=DataReader.readInt(data[36], data[37], data[38], data[39]);
				break;
			case BSP.TYPE_COD:
			case BSP.TYPE_COD2:
			case BSP.TYPE_COD4:
				firstBrush=DataReader.readInt(data[40], data[41], data[42], data[43]);
				numBrushes=DataReader.readInt(data[44], data[45], data[46], data[47]);
				break;
		}
	}
	
	// ACCESSORS/MUTATORS
	public int getHeadNode() {
		return headNode;
	}
	
	public int getFirstLeaf() {
		return firstLeaf;
	}
	
	public int getNumLeaves() {
		return numLeaves;
	}
	
	public int getFirstBrush() {
		return firstBrush;
	}
	
	public int getNumBrushes() {
		return numBrushes;
	}
	
	public int getFirstFace() {
		return firstFace;
	}
	
	public int getNumFaces() {
		return numFaces;
	}
}