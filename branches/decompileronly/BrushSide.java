// BrushSide class

public class BrushSide extends LumpObject {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	public static final int TYPE_COD=0;
	public static final int TYPE_NIGHTFIRE=1;
	public static final int TYPE_MOHAA=2;
	public static final int TYPE_RAVEN=3;
	public static final int TYPE_SIN=4;
	public static final int TYPE_QUAKE2=5;
	public static final int TYPE_QUAKE3=6;
	public static final int TYPE_SOURCE=7;
	
	private int plane=-1;
	private float dist=Float.NaN;
	private int texture=-1; // This is a valid texture index in Quake 2. However it means "unused" there
	private int face=-1;
	private int displacement=-1;
	private byte isBevel=-1;
	
	// CONSTRUCTORS
	public BrushSide(LumpObject in, int type) {
		super(in.getData());
		new BrushSide(in.getData(), type);
	}
	
	public BrushSide(byte[] data, int type) {
		super(data);
		switch(type) {
			case TYPE_COD: // Call of Duty's format sucks. The first field is either a float or an int
			               // depending on whether or not it's one of the first six sides in a brush.
				dist=DataReader.readFloat(data[0], data[1], data[2], data[3]);
			case TYPE_QUAKE3:
				plane=DataReader.readInt(data[0], data[1], data[2], data[3]);
				texture=DataReader.readInt(data[4], data[5], data[6], data[7]);
				break;
			case TYPE_MOHAA:
				plane=DataReader.readInt(data[0], data[1], data[2], data[3]);
				texture=DataReader.readInt(data[4], data[5], data[6], data[7]);
				break;
			case TYPE_RAVEN:
				plane=DataReader.readInt(data[0], data[1], data[2], data[3]);
				texture=DataReader.readInt(data[4], data[5], data[6], data[7]);
				face=DataReader.readInt(data[8], data[9], data[10], data[11]);
				break;
			case TYPE_SOURCE:
				this.displacement=DataReader.readShort(data[4], data[5]);
				this.isBevel=data[6]; // In little endian format, this byte takes the least significant bits of a short
				                    // and can therefore be used for all Source engine formats, Portal 2 and all.
			case TYPE_SIN:
			case TYPE_QUAKE2:
				plane=DataReader.readUShort(data[0], data[1]);
				texture=(int)DataReader.readShort(data[2], data[3]);
				break;
			case TYPE_NIGHTFIRE:
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