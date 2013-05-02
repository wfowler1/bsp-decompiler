// TIDecryptor class

// Small quick class to decrypt the 256-bit XOR encryption of Tactical Intervention maps.

public class TIDecryptor {
	
	/*
	private static final long key1 = 0x4334334334334138;
	private static final long key2 = 0x3534314631303243;
	private static final long key3 = 0x4538463639303833;
	private static final long key4 = 0x3136374337373536;
	*/
	private static final byte[] key = { 0x43, 0x34, 0x33, 0x43, 0x34, 0x33, 0x41, 0x38, 
	                                    0x35, 0x34, 0x31, 0x46, 0x31, 0x30, 0x32, 0x43,
	                                    0x45, 0x38, 0x46, 0x36, 0x39, 0x30, 0x38, 0x33,
	                                    0x31, 0x36, 0x37, 0x43, 0x37, 0x37, 0x35, 0x36 };
	
	public static byte[] decrypt(byte[] in) {
		return decrypt(in, 0);
	}
	
	public static byte[] decrypt(byte[] in, int offset) {
		byte[] out = new byte[in.length];
		for(int i=0;i<in.length;i++) {
			out[i] = (byte)(in[i] ^ key[(i+offset)%key.length]);
		}
		return out;
	}
}
