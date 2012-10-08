// EF2Texture class

// Holds all necessary data for a texture in a MoHAA BSP

public class EF2Texture extends v46Texture {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	private int unknown;
	
	// CONSTRUCTORS
	
	public EF2Texture(String inTexture, int inFlags, byte[] inContents, int unknown) {
		super(inTexture, inFlags, inContents);
		this.unknown=unknown;
	}
	
	public EF2Texture(byte[] in) {
		super(EF2Texture.extractV46Texture(in)); // I had to do this because of Java's ridiculous requirement that super() be the first statement in a constructor.
		unknown=DataReader.readInt(in[75], in[74], in[73], in[72]);
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
}
