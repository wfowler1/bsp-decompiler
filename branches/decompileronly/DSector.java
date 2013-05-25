// DSector class
// Contains all necessary information for a Doom SECTOR object.
// The sector defines an area, the heights of the floor and cieling
// of the area, the floor and cieling textures, the light level, the
// type of sector and a tag number.

public class DSector extends LumpObject {

	// INITIAL DATA DEFINITION AND DECLARATION OF CONSTANTS

	private short floor;
	private short cieling;
	
	private String floorTexture;
	private String cielingTexture;
	
	private short light;
	private short type;
	private short tag;
	
	// CONSTRUCTORS
	
	/*public DSector(short floor, short cieling, String floorTexture, String cielingTexture, short light, short type, short tag) {
		this.floor=floor;
		this.cieling=cieling;
		this.floorTexture=floorTexture;
		this.cielingTexture=cielingTexture;
		this.light=light;
		this.type=type;
		this.tag=tag;
	}*/
	
	public DSector(byte[] in) {
		super(in);
		floor=DataReader.readShort(in[0], in[1]);
		cieling=DataReader.readShort(in[2], in[3]);
		floorTexture="";
		for(int i=0;i<8;i++) {
			if(in[i+4] != (byte)0x00) {
				floorTexture+=(char)in[i+4];
			} else {
				break;
			}
		}
		cielingTexture="";
		for(int i=0;i<8;i++) {
			if(in[i+12] != (byte)0x00) {
				cielingTexture+=(char)in[i+12];
			} else {
				break;
			}
		}
		light=DataReader.readShort(in[20], in[21]);
		type=DataReader.readShort(in[22], in[23]);
		tag=DataReader.readShort(in[24], in[25]);
	}
	
	// METHODS
	public static Lump<DSector> createLump(byte[] in) throws java.lang.InterruptedException {
		int offset=0;
		int structLength=26;
		DSector[] elements=new DSector[in.length/structLength];
		byte[] bytes=new byte[structLength];
		for(int i=0;i<elements.length;i++) {
			if(Thread.currentThread().interrupted()) {
				throw new java.lang.InterruptedException("while populating Sector array");
			}
			for(int j=0;j<structLength;j++) {
				bytes[j]=in[offset+j];
			}
			elements[i]=new DSector(bytes);
			offset+=structLength;
		}
		return new Lump<DSector>(elements, in.length, structLength);
	}
	
	// ACCESSORS AND MUTATORS
	
	public void setFloorHeight(short in) {
		floor=in;
	}
	
	public short getFloorHeight() {
		return floor;
	}
	
	public void setCielingHeight(short in) {
		cieling=in;
	}
	
	public short getCielingHeight() {
		return cieling;
	}
	
	public void setFloorTexture(String in) {
		floorTexture=in;
	}
	
	public String getFloorTexture() {
		return floorTexture;
	}
	
	public void setCielingTexture(String in) {
		cielingTexture=in;
	}
	
	public String getCielingTexture() {
		return cielingTexture;
	}
	
	public void setLightLevel(short in) {
		light=in;
	}
	
	public short getLightLevel() {
		return light;
	}
	
	public void setType(short in) {
		type=in;
	}
	
	public short getType() {
		return type;
	}
	
	public void setTag(short in) {
		tag=in;
	}
	
	public short getTag() {
		return tag;
	}
}