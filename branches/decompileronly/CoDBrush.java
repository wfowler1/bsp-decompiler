// CoDBrushSide class

// Contains all info for a brush side in a v46 map

public class CoDBrush {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	// This lump uses an implicit index into brush sides. The first brush references side 0, the 
	// second brush uses the next unused side, and so forth.
	private short numSides;
	private short texture;
	
	// CONSTRUCTORS
	
	public CoDBrush(short numSides, short texture) {
		this.numSides=numSides;
		this.texture=texture;
	}
	
	public CoDBrush(byte[] in) {
		this.numSides=DataReader.readShort(in[0], in[1]);
		this.texture=DataReader.readShort(in[2], in[3]);
	}
	
	// METHODS

	// ACCESSORS/MUTATORS
	
	public short getNumSides() {
		return numSides;
	}
	
	public void setNumSides(short in) {
		numSides=in;
	}
	
	public short getTexture() {
		return texture;
	}
	
	public void setTexture(short in) {
		texture=in;
	}
}