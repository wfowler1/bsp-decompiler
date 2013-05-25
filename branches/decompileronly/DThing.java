// DThing class
// Contains all necessary information for a Doom THING object.
// Essentially is Doom's entities. You could make the argument that
// this takes less space than the conventional entities lump. And you
// would be correct. But it is incredibly esoteric and non-expandable.
// Some games DID expand it (such as Hexen or Strife), but they had to
// increase the data size to do it. And it was a pain in their ass.

public class DThing extends LumpObject {

	// INITIAL DATA DEFINITION AND DECLARATION OF CONSTANTS

	private Vector3D origin;
	private short angle;
	private short classNum;
	private short flags;
	
	private short id;
	private short action;
	private short[] arguments=new short[5];
	
	// CONSTRUCTORS
	
	public DThing(byte[] in, int type) {
		super(in);
		switch(type) {
			case DoomMap.TYPE_DOOM:
				origin=new Vector3D(DataReader.readShort(in[0], in[1]), DataReader.readShort(in[2], in[3]));
				this.angle=DataReader.readShort(in[4], in[5]);
				this.classNum=DataReader.readShort(in[6], in[7]);
				this.flags=DataReader.readShort(in[8], in[9]);
				break;
			case DoomMap.TYPE_HEXEN:
				id=DataReader.readShort(in[0], in[1]);
				origin=new Vector3D(DataReader.readShort(in[2], in[3]), DataReader.readShort(in[4], in[5]), DataReader.readShort(in[6], in[7]));
				this.angle=DataReader.readShort(in[8], in[9]);
				this.classNum=DataReader.readShort(in[10], in[11]);
				this.flags=DataReader.readShort(in[12], in[13]);
				action=DataReader.readUByte(in[14]);
				arguments[0]=DataReader.readUByte(in[15]);
				arguments[1]=DataReader.readUByte(in[16]);
				arguments[2]=DataReader.readUByte(in[17]);
				arguments[3]=DataReader.readUByte(in[18]);
				arguments[4]=DataReader.readUByte(in[19]);
				break;
		}
	}
	
	// METHODS
	public static Lump<DThing> createLump(byte[] in, int type) throws java.lang.InterruptedException {
		int structLength=0;
		switch(type) {
			case DoomMap.TYPE_DOOM:
				structLength=10;
				break;
			case DoomMap.TYPE_HEXEN:
				structLength=20;
				break;
		}
		int offset=0;
		DThing[] elements=new DThing[in.length/structLength];
		byte[] bytes=new byte[structLength];
		for(int i=0;i<elements.length;i++) {
			if(Thread.currentThread().interrupted()) {
				throw new java.lang.InterruptedException("while populating Thing array");
			}
			for(int j=0;j<structLength;j++) {
				bytes[j]=in[offset+j];
			}
			elements[i]=new DThing(bytes, type);
			offset+=structLength;
		}
		return new Lump<DThing>(elements, in.length, structLength);
	}
	
	// ACCESSORS AND MUTATORS
	
	public short getOriginX() {
		return (short)origin.getX();
	}
	
	public void setOriginX(short in) {
		origin.setX((double)in);
	}
	
	public short getOriginY() {
		return (short)origin.getY();
	}
	
	public void setOriginY(short in) {
		origin.setY((double)in);
	}
	
	// We're gonna need to worry about the Z, particularly in a mutator, since
	// we'll have to set a Z coordinate later on.
	public short getOriginZ() {
		return (short)origin.getZ();
	}
	
	public void setOriginZ(short in) {
		origin.setZ((double)in);
	}
	
	public Vector3D getOrigin() {
		return origin;
	}
	
	public void setOrigin(Vector3D in) {
		origin=in;
	}
	
	public short getAngle() {
		return angle;
	}
	
	public void setAngle(short in) {
		angle=in;
	}
	
	public short getClassNum() {
		return classNum;
	}
	
	public void setClassNum(short in) {
		classNum=in;
	}
	
	public short getFlags() {
		return flags;
	}
	
	public void setFlags(short in) {
		flags=in;
	}
	
	public short getID() {
		return id;
	}
	
	public short getAction() {
		return action;
	}
	
	public short[] getArguments() {
		return arguments;
	}
}

