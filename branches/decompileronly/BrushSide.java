// BrushSide class

public class BrushSide extends LumpObject {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	private int plane=-1;
	private float dist=Float.NaN;
	private int texture=-1; // This is a valid texture index in Quake 2. However it means "unused" there
	private int face=-1;
	private int displacement=-1; // In theory, this should always point to the side's displacement info. In practice, displacement brushes are removed on compile, leaving only the faces.
	private byte isBevel=-1;
	
	// CONSTRUCTORS
	public BrushSide(LumpObject in, int type) {
		super(in.getData());
		new BrushSide(in.getData(), type);
	}
	
	public BrushSide(byte[] data, int type) {
		super(data);
		switch(type) {
			case BSP.TYPE_COD: // Call of Duty's format sucks. The first field is either a float or an int
			                   // depending on whether or not it's one of the first six sides in a brush.
			case BSP.TYPE_COD2:
			case BSP.TYPE_COD4:
				dist=DataReader.readFloat(data[0], data[1], data[2], data[3]);
			case BSP.TYPE_QUAKE3:
			case BSP.TYPE_FAKK:
			case BSP.TYPE_STEF2DEMO:
			case BSP.TYPE_MOHAA:
				plane=DataReader.readInt(data[0], data[1], data[2], data[3]);
				texture=DataReader.readInt(data[4], data[5], data[6], data[7]);
				break;
			case BSP.TYPE_STEF2:
				texture=DataReader.readInt(data[0], data[1], data[2], data[3]);
				plane=DataReader.readInt(data[4], data[5], data[6], data[7]);
				break;
			case BSP.TYPE_RAVEN:
				plane=DataReader.readInt(data[0], data[1], data[2], data[3]);
				texture=DataReader.readInt(data[4], data[5], data[6], data[7]);
				face=DataReader.readInt(data[8], data[9], data[10], data[11]);
				break;
			case BSP.TYPE_SOURCE17:
			case BSP.TYPE_SOURCE18:
			case BSP.TYPE_SOURCE19:
			case BSP.TYPE_SOURCE20:
			case BSP.TYPE_SOURCE21:
			case BSP.TYPE_SOURCE22:
			case BSP.TYPE_SOURCE23:
			case BSP.TYPE_TACTICALINTERVENTION:
			case BSP.TYPE_DMOMAM:
				this.displacement=DataReader.readShort(data[4], data[5]);
				this.isBevel=data[6]; // In little endian format, this byte takes the least significant bits of a short
				                      // and can therefore be used for all Source engine formats, including Portal 2.
			case BSP.TYPE_SIN:
			case BSP.TYPE_QUAKE2:
			case BSP.TYPE_DAIKATANA:
			case BSP.TYPE_SOF:
				plane=DataReader.readUShort(data[0], data[1]);
				texture=(int)DataReader.readShort(data[2], data[3]);
				break;
			case BSP.TYPE_VINDICTUS:
				plane=DataReader.readInt(data[0], data[1], data[2], data[3]);
				texture=DataReader.readInt(data[4], data[5], data[6], data[7]);
				displacement=DataReader.readInt(data[8], data[9], data[10], data[11]);
				isBevel=data[12];
				break;
			case BSP.TYPE_NIGHTFIRE:
				face=DataReader.readInt(data[0], data[1], data[2], data[3]);
				plane=DataReader.readInt(data[4], data[5], data[6], data[7]);
				break;
		}
	}
	
	// METHODS
	
	// ACCESSORS/MUTATORS
	public float getDist() {
		return dist;
	}
	
	public int getPlane() {
		return plane;
	}
	
	public int getTexture() {
		return texture;
	}
	
	public int getFace() {
		return face;
	}
	
	public int getDisplacement() {
		return displacement;
	}
	
	public byte isBevel() {
		return isBevel;
	}
}