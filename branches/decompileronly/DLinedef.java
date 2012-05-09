// DLinedef class
// Contains all necessary information for a Doom LINEDEF object.
// The linedef is a strange animal. It roughly equates to the Planes of other
// BSP formats, but also defines what sectors are on what side.

public class DLinedef {

	// INITIAL DATA DEFINITION AND DECLARATION OF CONSTANTS

	private short start;
	private short end;
	private short flags;
	private short type;
	private short tag;
	private short right;
	private short left;
	
	// CONSTRUCTORS
	
	public DLinedef(short start, short end, short flags, short type, short tag, short right, short left) {
		this.start=start;
		this.end=end;
		this.flags=flags;
		this.type=type;
		this.tag=tag;
		this.right=right;
		this.left=left;
	}
	
	public DLinedef(byte[] in) {
		start=DataReader.readShort(in[0], in[1]);
		end=DataReader.readShort(in[2], in[3]);
		flags=DataReader.readShort(in[4], in[5]);
		type=DataReader.readShort(in[6], in[7]);
		tag=DataReader.readShort(in[8], in[9]);
		right=DataReader.readShort(in[10], in[11]);
		left=DataReader.readShort(in[12], in[13]);
	}
	
	// METHODS
	
	public boolean isOneSided() {
		return (right==-1 || left==-1);
	}
	
	// ACCESSORS AND MUTATORS
	
	public short getStart() {
		return start;
	}
	
	public void setStart(short in) {
		start=in;
	}
	
	public short getEnd() {
		return end;
	}
	
	public void setEnd(short in) {
		end=in;
	}
	
	public short getFlags() {
		return flags;
	}
	
	public void setFlags(short in) {
		flags=in;
	}
	
	public short getType() {
		return type;
	}
	
	public void setType(short in) {
		type=in;
	}
	
	public short getTag() {
		return tag;
	}
	
	public void setTag(short in) {
		tag=in;
	}
	
	public short getRight() {
		return right;
	}
	
	public void setRight(short in) {
		right=in;
	}
	
	public short getLeft() {
		return left;
	}
	
	public void setLeft(short in) {
		left=in;
	}
}
