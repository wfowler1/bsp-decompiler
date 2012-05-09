// DThing class
// Contains all necessary information for a Doom THING object.
// Essentially is Doom's entities. You could make the argument that
// this takes less space than the conventional entities lump. And you
// would be correct. But it is incredibly esoteric and non-expandable.
// Some games DID expand it (such as Hexen or Strife), but they had to
// increase the data size to do it. And it was a pain in their ass.

public class DThing {

	// INITIAL DATA DEFINITION AND DECLARATION OF CONSTANTS

	private Vector3D origin;
	private short angle;
	private short classNum;
	private short flags;
	
	// CONSTRUCTORS
	
	public DThing(short originX, short originY, short angle, short classNum, short flags) {
		origin=new Vector3D(originX, originY);
		this.angle=angle;
		this.classNum=classNum;
		this.flags=flags;
	}
	
	public DThing(byte[] in) {
		origin=new Vector3D(DataReader.readShort(in[0], in[1]), DataReader.readShort(in[2], in[3]));
		this.angle=DataReader.readShort(in[4], in[5]);
		this.classNum=DataReader.readShort(in[6], in[7]);
		this.flags=DataReader.readShort(in[8], in[9]);
	}
	
	// METHODS
	
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
}

