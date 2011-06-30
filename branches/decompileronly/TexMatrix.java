// TexMatrix class

// This class holds the data of the scaling applied to a texture by
// faces.

public class TexMatrix {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	public final int X=0;
	public final int Y=1;
	public final int Z=2;
	
	private float[] uAxis=new float[3];
	private float uShift;
	private float[] vAxis=new float[3];
	private float vShift;
	
	// CONSTRUCTORS
	
	// This one takes the components separate and in the correct data type
	public TexMatrix(float inUX, float inUY, float inUZ, float inUS,
                    float inVX, float inVY, float inVZ, float inVS) {
		uAxis[X]=inUX;
		uAxis[Y]=inUY;
		uAxis[Z]=inUZ;
		uShift=inUS;
		vAxis[X]=inVX;
		vAxis[Y]=inVY;
		vAxis[Z]=inVZ;
		vShift=inVS;
	}	
	
	// This one takes the components separate and in the correct data type
	// if the axes are passed as float3s
	public TexMatrix(float[] inUAxis, float inUS, float[] inVAxis, float inVS) throws InvalidTextureMatrixException {
		if(inUAxis.length!=3 || inVAxis.length!=3) {
			throw new InvalidTextureMatrixException();
		}
		uAxis=inUAxis;
		uShift=inUS;
		vAxis=inVAxis;
		vShift=inVS;
	}

	// This one takes an array of bytes (as if read directly from a file) and reads them
	// directly into the proper data types.
	public TexMatrix(byte[] in) throws InvalidTextureMatrixException {
		if(in.length!=32) {
			throw new InvalidTextureMatrixException();
		}
		int myInt=(in[3] << 24) | ((in[2] & 0xff) << 16) | ((in[1] & 0xff) << 8) | (in[0] & 0xff);
		uAxis[X]=Float.intBitsToFloat(myInt);
		myInt=(in[7] << 24) | ((in[6] & 0xff) << 16) | ((in[5] & 0xff) << 8) | (in[4] & 0xff);
		uAxis[Y]=Float.intBitsToFloat(myInt);
		myInt=(in[11] << 24) | ((in[10] & 0xff) << 16) | ((in[9] & 0xff) << 8) | (in[8] & 0xff);
		uAxis[Z]=Float.intBitsToFloat(myInt);
		myInt=(in[15] << 24) | ((in[14] & 0xff) << 16) | ((in[13] & 0xff) << 8) | (in[12] & 0xff);
		uShift=Float.intBitsToFloat(myInt);
		myInt=(in[19] << 24) | ((in[18] & 0xff) << 16) | ((in[17] & 0xff) << 8) | (in[16] & 0xff);
		vAxis[X]=Float.intBitsToFloat(myInt);
		myInt=(in[23] << 24) | ((in[22] & 0xff) << 16) | ((in[21] & 0xff) << 8) | (in[20] & 0xff);
		vAxis[Y]=Float.intBitsToFloat(myInt);
		myInt=(in[27] << 24) | ((in[26] & 0xff) << 16) | ((in[25] & 0xff) << 8) | (in[24] & 0xff);
		vAxis[Z]=Float.intBitsToFloat(myInt);
		myInt=(in[31] << 24) | ((in[30] & 0xff) << 16) | ((in[29] & 0xff) << 8) | (in[28] & 0xff);
		vShift=Float.intBitsToFloat(myInt);
	}
	
	// METHODS
	
	// ACCESSORS/MUTATORS
	public float getUAxisX() {
		return uAxis[X];
	}
	
	public void setUAxisX(float in) {
		uAxis[X]=in;
	}
	
	public float getUAxisY() {
		return uAxis[Y];
	}
	
	public void setUAxisY(float in) {
		uAxis[Y]=in;
	}
	
	public float getUAxisZ() {
		return uAxis[Z];
	}
	
	public void setUAxisZ(float in) {
		uAxis[Z]=in;
	}
	
	public float getUShift() {
		return uShift;
	}
	
	public void setUShift(float in) {
		uShift=in;
	}
	
	public float getVAxisX() {
		return vAxis[X];
	}
	
	public void setVAxisX(float in) {
		vAxis[X]=in;
	}
	
	public float getVAxisY() {
		return vAxis[Y];
	}
	
	public void setVAxisY(float in) {
		vAxis[Y]=in;
	}
	
	public float getVAxisZ() {
		return vAxis[Z];
	}
	
	public void setVAxisZ(float in) {
		vAxis[Z]=in;
	}
	
	public float getVShift() {
		return vShift;
	}
	
	public void setVShift(float in) {
		vShift=in;
	}
}