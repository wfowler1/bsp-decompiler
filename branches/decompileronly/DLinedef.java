// DLinedef class
// Contains all necessary information for a Doom LINEDEF object.
// The linedef is a strange animal. It roughly equates to the Planes of other
// BSP formats, but also defines what sectors are on what side.

public class DLinedef extends LumpObject {

	// INITIAL DATA DEFINITION AND DECLARATION OF CONSTANTS

	private short start;
	private short end;
	private byte[] flags;
	private short action;
	private short tag;
	private short right;
	private short left;
	private short[] arguments=new short[5];
	
	// CONSTRUCTORS
	
	public DLinedef(byte[] in, int type) {
		super(in);
		start=DataReader.readShort(in[0], in[1]);
		end=DataReader.readShort(in[2], in[3]);
		flags=new byte[] { in[4], in[5] };
		switch(type) {
			case DoomMap.TYPE_DOOM:
				action=DataReader.readShort(in[6], in[7]);
				tag=DataReader.readShort(in[8], in[9]);
				right=DataReader.readShort(in[10], in[11]);
				left=DataReader.readShort(in[12], in[13]);
				break;
			case DoomMap.TYPE_HEXEN:
				action=DataReader.readUByte(in[6]);
				arguments[0]=DataReader.readUByte(in[7]);
				arguments[1]=DataReader.readUByte(in[8]);
				arguments[2]=DataReader.readUByte(in[9]);
				arguments[3]=DataReader.readUByte(in[10]);
				arguments[4]=DataReader.readUByte(in[11]);
				right=DataReader.readShort(in[12], in[13]);
				left=DataReader.readShort(in[14], in[15]);
				break;
		}
	}
	
	// METHODS
	
	public boolean isOneSided() {
		return (right==-1 || left==-1);
	}
	
	public static Lump<DLinedef> createLump(byte[] in, int type) throws java.lang.InterruptedException {
		int structLength=0;
		switch(type) {
			case DoomMap.TYPE_DOOM:
				structLength=14;
				break;
			case DoomMap.TYPE_HEXEN:
				structLength=16;
				break;
		}
		int offset=0;
		DLinedef[] elements=new DLinedef[in.length/structLength];
		byte[] bytes=new byte[structLength];
		for(int i=0;i<elements.length;i++) {
			if(Thread.currentThread().interrupted()) {
				throw new java.lang.InterruptedException("while populating Linedef array");
			}
			for(int j=0;j<structLength;j++) {
				bytes[j]=in[offset+j];
			}
			elements[i]=new DLinedef(bytes, type);
			offset+=structLength;
		}
		return new Lump<DLinedef>(elements, in.length, structLength);
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
	
	public byte[] getFlags() {
		return flags;
	}
	
	public void setFlags(byte[] in) {
		flags=in;
	}
	
	public short getAction() {
		return action;
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
	
	public short[] getArguments() {
		return arguments;
	}
}
