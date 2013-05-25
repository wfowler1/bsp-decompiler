// Vertex class
// Constains all data necessary to handle a vertex in any BSP format.

public class Vertex extends LumpObject {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	public static final int X=0;
	public static final int Y=1;
	public static final int Z=2;
	
	private Vector3D vertex;
	private float[] texCoord=new float[] { Float.NaN, Float.NaN };
	
	// CONSTRUCTORS
	public Vertex(LumpObject in, int type) {
		super(in.getData());
		new Vertex(in.getData(), type);
	}
	
	public Vertex(byte[] data, int type) {
		super(data);
		switch(type) {
			case DoomMap.TYPE_DOOM:
				vertex=new Vector3D(DataReader.readShort(data[0], data[1]), DataReader.readShort(data[2], data[3]));
				break;
			case BSP.TYPE_STEF2:
			case BSP.TYPE_MOHAA:
			case BSP.TYPE_STEF2DEMO:
			case BSP.TYPE_RAVEN:
			case BSP.TYPE_QUAKE3:
			case BSP.TYPE_COD:
			case BSP.TYPE_FAKK:
				texCoord[X]=DataReader.readFloat(data[12], data[13], data[14], data[15]);
				texCoord[Y]=DataReader.readFloat(data[16], data[17], data[18], data[19]);
			case BSP.TYPE_QUAKE:
			case BSP.TYPE_NIGHTFIRE:
			case BSP.TYPE_SIN:
			case BSP.TYPE_SOF:
			case BSP.TYPE_SOURCE17:
			case BSP.TYPE_SOURCE18:
			case BSP.TYPE_SOURCE19:
			case BSP.TYPE_SOURCE20:
			case BSP.TYPE_SOURCE21:
			case BSP.TYPE_SOURCE22:
			case BSP.TYPE_SOURCE23:
			case BSP.TYPE_TACTICALINTERVENTION:
			case BSP.TYPE_QUAKE2:
			case BSP.TYPE_DAIKATANA:
			case BSP.TYPE_VINDICTUS:
			case BSP.TYPE_DMOMAM:
				vertex=DataReader.readPoint3F(data[0], data[1], data[2], data[3], data[4], data[5], data[6], data[7], data[8], data[9], data[10], data[11]);
				break;
		}
	}
	
	// METHODS
	public static Lump<Vertex> createLump(byte[] in, int type) throws java.lang.InterruptedException {
		int structLength=0;
		switch(type) {
			case DoomMap.TYPE_DOOM:
				structLength=4;
				break;
			case BSP.TYPE_QUAKE:
			case BSP.TYPE_NIGHTFIRE:
			case BSP.TYPE_SIN:
			case BSP.TYPE_SOF:
			case BSP.TYPE_SOURCE17:
			case BSP.TYPE_SOURCE18:
			case BSP.TYPE_SOURCE19:
			case BSP.TYPE_SOURCE20:
			case BSP.TYPE_SOURCE21:
			case BSP.TYPE_SOURCE22:
			case BSP.TYPE_SOURCE23:
			case BSP.TYPE_TACTICALINTERVENTION:
			case BSP.TYPE_QUAKE2:
			case BSP.TYPE_DAIKATANA:
			case BSP.TYPE_VINDICTUS:
			case BSP.TYPE_DMOMAM:
				structLength=12;
				break;
			case BSP.TYPE_STEF2:
			case BSP.TYPE_MOHAA:
			case BSP.TYPE_STEF2DEMO:
			case BSP.TYPE_QUAKE3:
			case BSP.TYPE_COD:
			case BSP.TYPE_FAKK:
				structLength=44;
				break;
			case BSP.TYPE_RAVEN:
				structLength=80;
				break;
			default:
				structLength=0; // This will cause the shit to hit the fan.
		}
		int offset=0;
		Vertex[] elements=new Vertex[in.length/structLength];
		byte[] bytes=new byte[structLength];
		for(int i=0;i<elements.length;i++) {
			if(Thread.currentThread().interrupted()) {
				throw new java.lang.InterruptedException("while populating Vertex array");
			}
			for(int j=0;j<structLength;j++) {
				bytes[j]=in[offset+j];
			}
			elements[i]=new Vertex(bytes, type);
			offset+=structLength;
		}
		return new Lump<Vertex>(elements, in.length, structLength);
	}
	
	// ACCESSORS/MUTATORS
	public Vector3D getVertex() {
		return vertex;
	}
	
	public float getTexCoordX() {
		return texCoord[X];
	}
	
	public float getTexCoordY() {
		return texCoord[Y];
	}
}