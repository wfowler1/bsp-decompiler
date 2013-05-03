// TIDecryptor class

// Small quick class to decrypt the 256-bit XOR encryption of Tactical Intervention maps.

public class TIDecryptor {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	private static byte[] key = new byte[32];
	
	// CONSTRUCTORS
	public TIDecryptor(byte[] key) {
		this.key=key;
	}
	
	// METHODS
	public byte[] decrypt(byte[] in) {
		return decrypt(in, 0);
	}
	
	public byte[] decrypt(byte[] in, int offset) {
		byte[] out = new byte[in.length];
		for(int i=0;i<in.length;i++) {
			out[i] = (byte)(in[i] ^ key[(i+offset)%key.length]);
		}
		return out;
	}
	
	// ACCESSORS/MUTATORS
}
