// v38Texture class

// Contains all the information for a single v38Texture object

public class v38Texture {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	Vector3D u;
	float uShift;
	Vector3D v;
	float vShift;
	int flags;
	int unknown;
	String texture;
	int next;
	
	// CONSTRUCTORS
	
	// Takes everything exactly as it is stored
	public v38Texture(Vector3D inU, float inUShift, Vector3D inV, float inVShift, int inFlags, int inUnk, String inName, int inNext) {
		u=inU;
		uShift=inUShift;
		v=inV;
		vShift=inVShift;
		flags=inFlags;
		unknown=inUnk;
		texture=inName;
		next=inNext;
	}
	
	// This constructor takes 76 bytes in a byte array, as though
	// it had just been read by a FileInputStream.
	public v38Texture(byte[] in) {
		int datain=(in[3] << 24) | ((in[2] & 0xff) << 16) | ((in[1] & 0xff) << 8) | (in[0] & 0xff);
		float ua=Float.intBitsToFloat(datain);
		datain=(in[7] << 24) | ((in[6] & 0xff) << 16) | ((in[5] & 0xff) << 8) | (in[4] & 0xff);
		float ub=Float.intBitsToFloat(datain);
		datain=(in[11] << 24) | ((in[10] & 0xff) << 16) | ((in[9] & 0xff) << 8) | (in[8] & 0xff);
		float uc=Float.intBitsToFloat(datain);
		u=new Vector3D(ua, ub, uc);
		datain=(in[15] << 24) | ((in[14] & 0xff) << 16) | ((in[13] & 0xff) << 8) | (in[12] & 0xff);
		uShift=Float.intBitsToFloat(datain);
		datain=(in[19] << 24) | ((in[18] & 0xff) << 16) | ((in[17] & 0xff) << 8) | (in[16] & 0xff);
		float va=Float.intBitsToFloat(datain);
		datain=(in[23] << 24) | ((in[22] & 0xff) << 16) | ((in[21] & 0xff) << 8) | (in[20] & 0xff);
		float vb=Float.intBitsToFloat(datain);
		datain=(in[27] << 24) | ((in[26] & 0xff) << 16) | ((in[25] & 0xff) << 8) | (in[24] & 0xff);
		float vc=Float.intBitsToFloat(datain);
		u=new Vector3D(va, vb, vc);
		datain=(in[31] << 24) | ((in[30] & 0xff) << 16) | ((in[29] & 0xff) << 8) | (in[28] & 0xff);
		vShift=Float.intBitsToFloat(datain);
		flags=(in[35] << 24) | ((in[34] & 0xff) << 16) | ((in[33] & 0xff) << 8) | (in[32] & 0xff);
		unknown=(in[39] << 24) | ((in[38] & 0xff) << 16) | ((in[37] & 0xff) << 8) | (in[36] & 0xff);
		int offset=40;
		texture=(char)in[offset]+""; // must do this first. Doing += right away adds "null" to the beginning
		for(int i=1;i<32;i++) {
			if(in[offset+i]==0x00) {
				break;
			} // else
			texture+=(char)in[offset+i]+"";
		}
		next=(in[75] << 24) | ((in[74] & 0xff) << 16) | ((in[73] & 0xff) << 8) | (in[72] & 0xff);
	}
	
	// METHODS
	
	// ACCESSORS/MUTATORS
	public void setU(Vector3D in) {
		u=in;
	}
	
	public Vector3D getU() {
		return u;
	}
	
	public void setUShift(float in) {
		uShift=in;
	}
	
	public float getUShift() {
		return uShift;
	}
	
	public void setV(Vector3D in) {
		v=in;
	}
	
	public Vector3D getV() {
		return v;
	}
	
	public void setVShift(float in) {
		vShift=in;
	}
	
	public float getVShift() {
		return vShift;
	}
	
	public void setFlags(int in) {
		flags=in;
	}
	
	public int getFlags() {
		return flags;
	}
	
	public void setUnknown(int in) {
		unknown=in;
	}
	
	public int getUnknown() {
		return unknown;
	}
	
	public void setTexture(String in) {
		texture=in;
	}
	
	public String getTexture() {
		return texture;
	}
	
	public void setNext(int in) {
		next=in;
	}
	
	public int getNext() {
		return next;
	}
}