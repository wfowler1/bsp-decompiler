// LumpObject class
// A base class for any given lump object. Holds the data as a byte array.

public class LumpObject {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	private byte[] data;
	
	// CONSTRUCTORS
	public LumpObject(byte[] data) {
		this.data=data;
	}
	
	// METHODS
	
	// ACCESSORS/MUTATORS
	public byte[] getData() {
		return data;
	}
	
	public void setData(byte[] in) {
		data=in;
	}
	
	public int length() {
		return data.length;
	}
}