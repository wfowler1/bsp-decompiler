// v46Texture class

// Holds all necessary data for a texture in a BSP v46

public class v46Texture {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	private String texture="";
	private int flags;
	private byte[] contents; // Q3 holds contents here?!?
	
	// CONSTRUCTORS
	
	public v46Texture(String inTexture, int inFlags, byte[] inContents) {
		texture=inTexture;
		flags=inFlags;
		contents=inContents;
	}
	
	public v46Texture(byte[] in) {
		for(int i=0;i<64;i++) {
			if(in[i]==0x00) {
				break;
			}
			texture+=(char)in[i];
		}
		flags=(((in[67] & 0xff) << 24) | ((in[66] & 0xff) << 16) | ((in[65] & 0xff) << 8) | (in[64] & 0xff));
		contents=new byte[] { in[68], in[69], in[70], in[71] };
	}
	
	// METHODS
	public void printIsDetail() {
		Window.println(""+((contents[3] & ((byte)1 << 3)) != 0),Window.VERBOSITY_ALWAYS);
	}
	
	public void printContents() {
		Window.println(contents[3]+" "+contents[2]+" "+contents[1]+" "+contents[0]+" "+texture,Window.VERBOSITY_ALWAYS);
	}

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
	
	public byte[] getContents() {
		return contents;
	}
	
	public void setContents(byte[] in) {
		contents=in;
	}
}
