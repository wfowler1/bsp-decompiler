// Edge class

// Holds all the data for an edge in a Quake, Half-Life, or Quake 2 map.
// Doubles as a subsector class for Doom maps, has accessors for them.

public class Edge extends LumpObject {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	private int firstVertex;
	private int secondVertex;
	
	// CONSTRUCTORS
	
	// This constructor takes all data in their proper data types
	public Edge(short inFirstVertex, short inSecondVertex) {
		super(new byte[0]);
		firstVertex=(int)inFirstVertex;
		secondVertex=(int)inSecondVertex;
	}

	public Edge(int inFirstVertex, int inSecondVertex) {
		super(new byte[0]);
		firstVertex=inFirstVertex;
		secondVertex=inSecondVertex;
	}
	
	// This constructor takes bytes in a byte array, as though
	// it had just been read by a FileInputStream.
	public Edge(byte[] in, int type) {
		super(in);
		switch(type) {
			case BSP.TYPE_QUAKE:
			case BSP.TYPE_SIN:
			case BSP.TYPE_DAIKATANA:
			case BSP.TYPE_SOURCE17:
			case BSP.TYPE_SOURCE18:
			case BSP.TYPE_SOURCE19:
			case BSP.TYPE_SOURCE20:
			case BSP.TYPE_SOURCE21:
			case BSP.TYPE_SOURCE22:
			case BSP.TYPE_SOURCE23:
			case BSP.TYPE_TACTICALINTERVENTION:
			case BSP.TYPE_DMOMAM:
			case BSP.TYPE_QUAKE2:
			case BSP.TYPE_SOF:
			case DoomMap.TYPE_DOOM:
			case DoomMap.TYPE_HEXEN:
				firstVertex=DataReader.readUShort(in[0], in[1]);
				secondVertex=DataReader.readUShort(in[2], in[3]);
				break;
			case BSP.TYPE_VINDICTUS:
				firstVertex=DataReader.readInt(in[0], in[1], in[2], in[3]);
				secondVertex=DataReader.readInt(in[4], in[5], in[6], in[7]);
				break;
		}
	}
	
	// METHODS
	public static Lump<Edge> createLump(byte[] in, int type) throws java.lang.InterruptedException {
		int structLength=0;
		switch(type) {
			case BSP.TYPE_QUAKE:
			case BSP.TYPE_SIN:
			case BSP.TYPE_DAIKATANA:
			case BSP.TYPE_SOURCE17:
			case BSP.TYPE_SOURCE18:
			case BSP.TYPE_SOURCE19:
			case BSP.TYPE_SOURCE20:
			case BSP.TYPE_SOURCE21:
			case BSP.TYPE_SOURCE22:
			case BSP.TYPE_SOURCE23:
			case BSP.TYPE_TACTICALINTERVENTION:
			case BSP.TYPE_DMOMAM:
			case BSP.TYPE_QUAKE2:
			case BSP.TYPE_SOF:
			case DoomMap.TYPE_DOOM:
			case DoomMap.TYPE_HEXEN:
				structLength=4;
				break;
			case BSP.TYPE_VINDICTUS:
				structLength=8;
				break;
		}
		int offset=0;
		Edge[] elements=new Edge[in.length/structLength];
		byte[] bytes=new byte[structLength];
		for(int i=0;i<elements.length;i++) {
			if(Thread.currentThread().interrupted()) {
				throw new java.lang.InterruptedException("while populating Edge array");
			}
			for(int j=0;j<structLength;j++) {
				bytes[j]=in[offset+j];
			}
			elements[i]=new Edge(bytes, type);
			offset+=structLength;
		}
		return new Lump<Edge>(elements, in.length, structLength);
	}
	
	// ACCESSORS/MUTATORS
	
	public int getFirstVertex() {
		return firstVertex;
	}
	
	public void setFirstVertex(int in) {
		firstVertex=in;
	}
	
	public int getSecondVertex() {
		return secondVertex;
	}
	
	public void setSecondVertex(int in) {
		secondVertex=in;
	}
	
	public int getNumSegs() {
		return firstVertex;
	}
	
	public int getFirstSeg() {
		return secondVertex;
	}
}
