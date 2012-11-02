// Brush class
// Tries to hold the data used by all formats of brush structure

public class Brush extends LumpObject {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	// All four brush formats use some of these in some way
	private int firstSide=-1;
	private int numSides=-1;
	private int texture=-1;
	private byte[] contents;
	
	// CONSTRUCTORS
	public Brush(LumpObject in, int type) {
		super(in.getData());
		new Brush(in.getData(), type);
	}

	public Brush(byte[] data, int type) {
		super(data);
		switch(type) {
			case BSP.TYPE_QUAKE2:
			case BSP.TYPE_SIN:
			case BSP.TYPE_SOURCE17:
			case BSP.TYPE_SOURCE18:
			case BSP.TYPE_SOURCE19:
			case BSP.TYPE_SOURCE20:
			case BSP.TYPE_SOURCE21:
			case BSP.TYPE_SOURCE22:
			case BSP.TYPE_SOURCE23:
				firstSide=DataReader.readInt(data[0], data[1], data[2], data[3]);
				numSides=DataReader.readInt(data[4], data[5], data[6], data[7]);
				contents=new byte[] { data[8], data[9], data[10], data[11] };
				break;
			case BSP.TYPE_NIGHTFIRE:
				contents=new byte[] { data[0], data[1], data[2], data[3] };
				firstSide=DataReader.readInt(data[4], data[5], data[6], data[7]);
				numSides=DataReader.readInt(data[8], data[9], data[10], data[11]);
				break;
			case BSP.TYPE_STEF2:
			case BSP.TYPE_MOHAA:
			case BSP.TYPE_STEF2DEMO:
			case BSP.TYPE_RAVEN:
			case BSP.TYPE_QUAKE3:
				firstSide=DataReader.readInt(data[0], data[1], data[2], data[3]);
				numSides=DataReader.readInt(data[4], data[5], data[6], data[7]);
				texture=DataReader.readInt(data[8], data[9], data[10], data[11]);
				break;
			case BSP.TYPE_COD:
				numSides=DataReader.readUShort(data[0], data[1]);
				texture=DataReader.readUShort(data[2], data[3]);
				break;
		}
	}
	
	// ACCESSORS/MUTATORS
	public int getFirstSide() {
		return firstSide;
	}
	
	public int getNumSides() {
		return numSides;
	}
	
	public int getTexture() {
		return texture;
	}
	
	public byte[] getContents() {
		return contents;
	}
}