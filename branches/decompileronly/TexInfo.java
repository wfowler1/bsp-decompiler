// TexInfo class
// This class contains the texture scaling information for certain formats.
// Some BSP formats lack this lump (or it is contained in a different one)
// so their cases will be left out.

public class TexInfo extends LumpObject {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	public static final int S=0;
	public static final int T=1;
	
	private Vector3D[] axes;
	private float[] shifts=new float[] { Float.NaN, Float.NaN };
	private int flags=-1;
	private int texture=-1;
	
	// CONSTRUCTORS
	public TexInfo(LumpObject in, int type) {
		super(in.getData());
		new TexInfo(in.getData(), type);
	}
	
	public TexInfo(byte[] data, int type) {
		super(data);
		axes=new Vector3D[2];
		axes[S]=DataReader.readPoint3F(data[0], data[1], data[2], data[3], data[4], data[5], data[6], data[7], data[8], data[9], data[10], data[11]);
		shifts[S]=DataReader.readFloat(data[12], data[13], data[14], data[15]);
		axes[T]=DataReader.readPoint3F(data[16], data[17], data[18], data[19], data[20], data[21], data[22], data[23], data[24], data[25], data[26], data[27]);
		shifts[T]=DataReader.readFloat(data[28], data[29], data[30], data[31]);
		switch(type) {
			// Excluded engines: Quake 2-based, Quake 3-based
			case BSP.TYPE_SOURCE17:
			case BSP.TYPE_SOURCE18:
			case BSP.TYPE_SOURCE19:
			case BSP.TYPE_SOURCE20:
			case BSP.TYPE_SOURCE21:
			case BSP.TYPE_SOURCE22:
			case BSP.TYPE_SOURCE23:
			case BSP.TYPE_TACTICALINTERVENTION:
			case BSP.TYPE_VINDICTUS:
				texture=DataReader.readInt(data[68], data[69], data[70], data[71]);
				flags=DataReader.readInt(data[64], data[65], data[66], data[67]);
				break;
			case BSP.TYPE_DMOMAM:
				texture=DataReader.readInt(data[92], data[93], data[94], data[95]);
				flags=DataReader.readInt(data[88], data[89], data[90], data[91]);
				break;
			case BSP.TYPE_QUAKE:
				texture=DataReader.readInt(data[32], data[33], data[34], data[35]);
				flags=DataReader.readInt(data[36], data[37], data[38], data[39]);
				break;
			case BSP.TYPE_NIGHTFIRE:
				break;
		}
	}
	
	// Not for use in a group
	public TexInfo(Vector3D s, float SShift, Vector3D t, float TShift, int flags, int texture) {
		super(new byte[0]);
		axes=new Vector3D[2];
		axes[S]=s;
		axes[T]=t;
		shifts[S]=SShift;
		shifts[T]=TShift;
		this.flags=flags;
		this.texture=texture;
	}
	
	// METHODS
	public static Lump<TexInfo> createLump(byte[] in, int type) throws java.lang.InterruptedException {
		int structLength=0;
		switch(type) {
			case BSP.TYPE_NIGHTFIRE:
				structLength=32;
				break;
			case BSP.TYPE_QUAKE:
				structLength=40;
				break;
			case BSP.TYPE_SOURCE17:
			case BSP.TYPE_SOURCE18:
			case BSP.TYPE_SOURCE19:
			case BSP.TYPE_SOURCE20:
			case BSP.TYPE_SOURCE21:
			case BSP.TYPE_SOURCE22:
			case BSP.TYPE_SOURCE23:
			case BSP.TYPE_TACTICALINTERVENTION:
			case BSP.TYPE_VINDICTUS:
				structLength=72;
				break;
			case BSP.TYPE_DMOMAM:
				structLength=96;
				break;
		}
		int offset=0;
		TexInfo[] elements=new TexInfo[in.length/structLength];
		byte[] bytes=new byte[structLength];
		for(int i=0;i<elements.length;i++) {
			if(Thread.currentThread().interrupted()) {
				throw new java.lang.InterruptedException("while populating TexInfo array");
			}
			for(int j=0;j<structLength;j++) {
				bytes[j]=in[offset+j];
			}
			elements[i]=new TexInfo(bytes, type);
			offset+=structLength;
		}
		return new Lump<TexInfo>(elements, in.length, structLength);
	}
	
	// ACCESSORS/MUTATORS
	public Vector3D getSAxis() {
		return axes[S];
	}
	
	public Vector3D getTAxis() {
		return axes[T];
	}
	
	public float getSShift() {
		return shifts[S];
	}
	
	public float getTShift() {
		return shifts[T];
	}
	
	public int getFlags() {
		return flags;
	}
	
	public int getTexture() {
		return texture;
	}
}