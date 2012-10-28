// Face class
// Replaces all the separate face classes for different versions of BSP.
// Or, at least the ones I need.

public class Face extends LumpObject {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	public static final int TYPE_QUAKE=0;
	public static final int TYPE_SIN=1;
	public static final int TYPE_QUAKE3=2;
	public static final int TYPE_SOURCE=3;
	public static final int TYPE_NIGHTFIRE=4;
	public static final int TYPE_RAVEN=5;
	
	// Faces is one of the more different lumps between versions. Some of these fields
	// are only used by one format. However, there are some commonalities which make
	// it worthwhile to unify these. All formats use a plane, a texture, and vertices
	// in some way. Also (unused for the decompiler) they all use lightmaps.
	private int plane=-1;
	private int side=-1;
	private int firstEdge=-1;
	private int numEdges=-1;
	private int texture=-1;
	private int firstVertex=-1;
	private int numVertices=-1;
	private int material=-1;
	private int textureScale=-1;
	private int displacement=-1;
	private int original=-1;
	private byte[] flags;
	
	// CONSTRUCTORS
	
	public Face(LumpObject in, int type) {
		super(in.getData());
		new Face(in.getData(), type);
	}
	
	public Face(byte[] data, int type) {
		super(data);
		switch(type) {
			case TYPE_QUAKE:
			case TYPE_SIN:
				plane=DataReader.readUShort(data[0], data[1]);
				side=DataReader.readUShort(data[2], data[3]);
				firstEdge=DataReader.readInt(data[4], data[5], data[6], data[7]);
				numEdges=DataReader.readUShort(data[8], data[9]);
				texture=DataReader.readUShort(data[10], data[11]);
				break;
			case TYPE_QUAKE3:
			case TYPE_RAVEN:
				texture=DataReader.readInt(data[0], data[1], data[2], data[3]);
				flags=new byte[] { data[8], data[9], data[10], data[11] };
				firstVertex=DataReader.readInt(data[12], data[13], data[14], data[15]);
				numVertices=DataReader.readInt(data[16], data[17], data[18], data[19]);
				break;
			case TYPE_SOURCE:
				plane=DataReader.readUShort(data[0], data[1]);
				side=(int)data[2];
				firstEdge=DataReader.readInt(data[4], data[5], data[6], data[7]);
				numEdges=DataReader.readUShort(data[8], data[9]);
				texture=DataReader.readUShort(data[10], data[11]);
				displacement=DataReader.readUShort(data[12], data[13]);
				original=DataReader.readInt(data[44], data[45], data[46], data[47]);
				break;
			case TYPE_NIGHTFIRE:
				plane=DataReader.readInt(data[0], data[1], data[2], data[3]);
				firstVertex=DataReader.readInt(data[4], data[5], data[6], data[7]);
				numVertices=DataReader.readInt(data[8], data[9], data[10], data[11]);
				flags=new byte[] { data[20], data[21], data[22], data[23] };
				texture=DataReader.readInt(data[24], data[25], data[26], data[27]);
				material=DataReader.readInt(data[28], data[29], data[30], data[31]);
				textureScale=DataReader.readInt(data[32], data[33], data[34], data[35]);
				break;
		}
	}
	
	// METHODS
	
	// ACCESSORS/MUTATORS
	
	public int getPlane() {
		return plane;
	}
	
	public int getSide() {
		return side;
	}
	
	public int getFirstEdge() {
		return firstEdge;
	}
	
	public int getNumEdges() {
		return numEdges;
	}
	
	public int getTexture() {
		return texture;
	}
	
	public int getFirstVertex() {
		return firstVertex;
	}
	
	public int getNumVertices() {
		return numVertices;
	}
	
	public int getMaterial() {
		return material;
	}
	
	public int getTextureScale() {
		return textureScale;
	}
	
	public int getDisplacement() {
		return displacement;
	}
	
	public int getOriginal() {
		return original;
	}
	
	public byte[] getFlags() {
		return flags;
	}
	
	public void setFlags(byte[] in) {
		flags=in;
	}
}