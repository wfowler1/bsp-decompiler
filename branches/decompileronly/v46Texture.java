// v46Texture class

// Holds all necessary data for a texture in a BSP v46

public class v46Texture {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	private String texture="";
	private int flags;
	private int contents; // Q3 holds contents here?!?
	
	// CONSTRUCTORS
	
	public v46Texture(String inTexture, int inFlags, int inContents) {
		texture=inTexture;
		flags=inFlags;
		contents=inContents;
	}
	
	public v46Texture(byte[] in) {
		for(int i=0;i<64;i++) {
			if(in[i]==0x00) {
				break;
			}
			texture+=in[i];
		}
		flags=(((in[67] & 0xff) << 24) | ((in[66] & 0xff) << 16) | ((in[65] & 0xff) << 8) | (in[64] & 0xff));
		contents=(((in[71] & 0xff) << 24) | ((in[70] & 0xff) << 16) | ((in[69] & 0xff) << 8) | (in[68] & 0xff));
	}
	
	// METHODS

	// ACCESSORS/MUTATORS
	
	public String getTexture() {
		return texture;
	}
	
	public void setTexture(String in) {
		texture=in;
	}
	
	public int getFlags() {
		return flags;
	}
	
	public void setFlags(int in) {
		flags=in;
	}
	
	public int getContents() {
		return contents;
	}
	
	public void setContents(int in) {
		contents=in;
	}
}
