// SourceBrushSide class

// This class holds data of a single brush side.

public class SourceBrushSide {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	private int plane; // Stupid Java doesn't support unsigned data. There are BSPs with over 32,767 planes, so I must use an int.
	private short texInfo;
	private short displacementInfo;
	// Technically, this is the Portal 2 brush side format, it isn't used this way in most
	// forks of the engine. However, this is usually a short rather than two bytes, indicating
	// isBevel. I can easily determine whether it's a bevel from either format regardless.
	// I just don't know what the hell connotations "thin" has.
	private byte isBevel;
	private byte isThin;
	
	// CONSTRUCTORS
	
	// This one takes the components separate and in the correct data type
	public SourceBrushSide(short plane, short texInfo, short displacementInfo, byte isBevel, byte isThin) {
		this.plane=(int)plane;
		this.texInfo=texInfo;
		this.displacementInfo=displacementInfo;
		this.isBevel=isBevel;
		this.isThin=isThin;
	}
	
	// Takes all components as the rest of the engine would pass it
	public SourceBrushSide(short plane, short texInfo, short displacementInfo, short isBevel) {
		this.plane=(int)plane;
		this.texInfo=texInfo;
		this.displacementInfo=displacementInfo;
		this.isBevel=(byte)isBevel; // This is probably a boolean value anyway
		this.isThin=0;
	}

	// This one takes an array of bytes (as if read directly from a file) and reads them
	// directly into the proper data types.
	public SourceBrushSide(byte[] in) {
		this.plane=DataReader.readUShort(in[0], in[1]);
		this.texInfo=DataReader.readShort(in[2], in[3]);
		this.displacementInfo=DataReader.readShort(in[4], in[5]);
		this.isBevel=in[6]; // In little endian format, this byte takes the least significant bits of a short
		this.isThin=in[7];
	}
	
	// METHODS
	
	// ACCESSORS/MUTATORS
	public int getPlane() {
		return plane;
	}
	
	public void setPlane(int in) {
		plane=in;
	}
	
	public short getTexInfo() {
		return texInfo;
	}
	
	public void setTexInfo(short in) {
		texInfo=in;
	}
	
	public short getDispInfo() {
		return displacementInfo;
	}
	
	public void setDispInfo(short in) {
		displacementInfo=in;
	}
	
	public byte isBevel() {
		return isBevel;
	}
	
	public void setIsBevel(byte in) {
		isBevel=in;
	}
	
	public byte isThin() {
		return isThin;
	}
	
	public void setIsThin(byte in) {
		isThin=in;
	}
}