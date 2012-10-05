// MoHAATexture class

// Holds all necessary data for a texture in a MoHAA BSP

public class MoHAATexture extends v46Texture {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	private int unknown;
	private String mask="";
	
	// CONSTRUCTORS
	
	public MoHAATexture(String inTexture, int inFlags, byte[] inContents, int unknown, String mask) {
		super(inTexture, inFlags, inContents);
		this.unknown=unknown;
		this.mask=mask;
	}
	
	public MoHAATexture(byte[] in) {
		super(MoHAATexture.extractV46Texture(in)); // I had to do this because of Java's ridiculous requirement that super() be the first statement in a constructor.
		unknown=DataReader.readInt(in[75], in[74], in[73], in[72]);
		for(int i=0;i<64;i++) {
			if(in[i+72]==0x00) {
				break;
			}
			mask+=(char)in[i];
		}
	}
	
	// METHODS
	private static byte[] extractV46Texture(byte[] in) {
		byte[] out=new byte[72];
		for(int i=0;i<out.length;i++) {
			out[i]=in[i];
		}
		return out;
	}
	
	// ACCESSORS/MUTATORS
	
	public int getUnknown() {
		return unknown;
	}
	
	public void setUnknown(int in) {
		unknown=in;
	}
	
	public String getMask() {
		return mask;
	}
	
	public void setMask(String in) {
		mask=in;
	}
}
