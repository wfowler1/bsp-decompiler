// SourceCubemap class

public class SourceCubemap extends LumpObject {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	private Vector3D origin;
	private int size;
	
	// CONSTRUCTORS
	public SourceCubemap(LumpObject in, int type) {
		super(in.getData());
		new SourceCubemap(in.getData(), type);
	}
	
	public SourceCubemap(byte[] data, int type) {
		super(data);
		switch(type) {
			case BSP.TYPE_SOURCE17:
			case BSP.TYPE_SOURCE18:
			case BSP.TYPE_SOURCE19:
			case BSP.TYPE_SOURCE20:
			case BSP.TYPE_SOURCE21:
			case BSP.TYPE_SOURCE22:
			case BSP.TYPE_SOURCE23:
			case BSP.TYPE_TACTICALINTERVENTION:
			case BSP.TYPE_VINDICTUS:
			case BSP.TYPE_DMOMAM:
				origin=new Vector3D(DataReader.readInt(data[0], data[1], data[2], data[3]), DataReader.readInt(data[4], data[5], data[6], data[7]), DataReader.readInt(data[8], data[9], data[10], data[11]));
				size=DataReader.readInt(data[12], data[13], data[14], data[15]);
				break;
		}
	}
	
	// METHODS
	public static Lump<SourceCubemap> createLump(byte[] in, int type) throws java.lang.InterruptedException {
		int structLength=0;
		switch(type) {
			case BSP.TYPE_SOURCE17:
			case BSP.TYPE_SOURCE18:
			case BSP.TYPE_SOURCE19:
			case BSP.TYPE_SOURCE20:
			case BSP.TYPE_SOURCE21:
			case BSP.TYPE_SOURCE22:
			case BSP.TYPE_SOURCE23:
			case BSP.TYPE_TACTICALINTERVENTION:
			case BSP.TYPE_VINDICTUS:
			case BSP.TYPE_DMOMAM:
				structLength=16;
				break;
			default:
				structLength=0; // This will cause the shit to hit the fan.
		}
		int offset=0;
		SourceCubemap[] elements=new SourceCubemap[in.length/structLength];
		byte[] bytes=new byte[structLength];
		for(int i=0;i<elements.length;i++) {
			if(Thread.currentThread().interrupted()) {
				throw new java.lang.InterruptedException("while populating Cubemap array");
			}
			for(int j=0;j<structLength;j++) {
				bytes[j]=in[offset+j];
			}
			elements[i]=new SourceCubemap(bytes, type);
			offset+=structLength;
		}
		return new Lump<SourceCubemap>(elements, in.length, structLength);
	}
	
	// ACCESSORS/MUTATORS
	public Vector3D getOrigin() {
		return origin;
	}
	
	public int getSize() {
		return size;
	}
}