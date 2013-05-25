// DSegment class

// This class holds data of a single Doom line Segment
// Not entirely sure why this structure exists. It's quite superfluous.

public class DSegment extends LumpObject {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	private short startVertex;
	private short endVertex;
	private short angle;
	private short lineDef;
	private short direction; // 0 for right side of linedef, 1 for left
	private short offset;
	
	// CONSTRUCTORS
	
	// This one takes the components separate and in the correct data type
	/*public DSegment(short startVertex, short endVertex, short angle, short lineDef, short direction, short offset) {
		this.startVertex=startVertex;
		this.endVertex=endVertex;
		this.angle=angle;
		this.lineDef=lineDef;
		this.direction=direction;
		this.offset=offset;
	}*/

	// This one takes an array of bytes (as if read directly from a file) and reads them
	// directly into the proper data types.
	public DSegment(byte[] in) {
		super(in);
		this.startVertex=DataReader.readShort(in[0], in[1]);
		this.endVertex=DataReader.readShort(in[2], in[3]);
		this.angle=DataReader.readShort(in[4], in[5]);
		this.lineDef=DataReader.readShort(in[6], in[7]);
		this.direction=DataReader.readShort(in[8], in[9]);
		this.offset=DataReader.readShort(in[10], in[11]);
	}
	
	// METHODS
	public static Lump<DSegment> createLump(byte[] in) throws java.lang.InterruptedException {
		int structLength=12;
		int offset=0;
		DSegment[] elements=new DSegment[in.length/structLength];
		byte[] bytes=new byte[structLength];
		for(int i=0;i<elements.length;i++) {
			if(Thread.currentThread().interrupted()) {
				throw new java.lang.InterruptedException("while populating Segment array");
			}
			for(int j=0;j<structLength;j++) {
				bytes[j]=in[offset+j];
			}
			elements[i]=new DSegment(bytes);
			offset+=structLength;
		}
		return new Lump<DSegment>(elements, in.length, structLength);
	}
	
	// ACCESSORS/MUTATORS
	public short getStartVertex() {
		return startVertex;
	}
	
	public void setStartVertex(short in) {
		startVertex=in;
	}
	
	public short getEndVertex() {
		return endVertex;
	}
	
	public void setEndVertex(short in) {
		endVertex=in;
	}
	
	public short getAngle() {
		return angle;
	}
	
	public void setAngle(short in) {
		angle=in;
	}
	
	public short getLinedef() {
		return lineDef;
	}
	
	public void setLinedef(short in) {
		lineDef=in;
	}
	
	public short getDirection() {
		return direction;
	}
	
	public void setDirection(short in) {
		direction=in;
	}
	
	public short getOffset() {
		return offset;
	}
	
	public void setOffset(short in) {
		offset=in;
	}
}