// MoHAATexture class

// Holds all necessary data for a texture in a MoHAA BSP

public class MoHAATexture extends EF2Texture {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	private String mask="";
	
	// CONSTRUCTORS
	
	public MoHAATexture(String inTexture, int inFlags, byte[] inContents, int unknown, String mask) {
		super(inTexture, inFlags, inContents, unknown);
		this.mask=mask;
	}
	
	public MoHAATexture(byte[] in) {
		super(MoHAATexture.extractEF2Texture(in)); // I had to do this because of Java's ridiculous requirement that super() be the first statement in a constructor.
		for(int i=0;i<64;i++) {
			if(in[i+76]==0x00) {
				break;
			}
			mask+=(char)in[i];
		}
	}
	
	// METHODS
	private static byte[] extractEF2Texture(byte[] in) {
		byte[] out=new byte[76];
		for(int i=0;i<out.length;i++) {
			out[i]=in[i];
		}
		return out;
	}
	
	// ACCESSORS/MUTATORS
	
	public String getMask() {
		return mask;
	}
	
	public void setMask(String in) {
		mask=in;
	}
}
