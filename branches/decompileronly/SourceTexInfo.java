// SourceTexInfo class

// Contains all the information for a single SourceTexInfo object

public class SourceTexInfo {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	private Vector3D textureS;
	private float textureSShift;
	private Vector3D textureT;
	private float textureTShift;
	private Vector3D lightmapS;
	private float lightmapSShift;
	private Vector3D lightmapT;
	private float lightmapTShift;
	private int flags;
	private int texdata;
	
	// CONSTRUCTORS
	
	// Takes everything exactly as it is stored
	public SourceTexInfo(Vector3D textureS, float textureSShift, Vector3D textureT, float textureTShift, 
	                     Vector3D lightmapS, float lightmapSShift, Vector3D lightmapT, float lightmapTShift,
	                     int flags, int texdata) {
		this.textureS=textureS;
		this.textureSShift=textureSShift;
		this.textureT=textureT;
		this.textureTShift=textureTShift;
		this.lightmapS=lightmapS;
		this.lightmapSShift=lightmapSShift;
		this.lightmapT=lightmapT;
		this.lightmapTShift=lightmapTShift;
		this.flags=flags;
		this.texdata=texdata;
	}
	
	// This constructor takes bytes in a byte array, as though
	// it had just been read by a FileInputStream.
	public SourceTexInfo(byte[] in) {
		this.textureS=DataReader.readPoint3F(in[0], in[1], in[2], in[3], in[4], in[5], in[6], in[7], in[8], in[9], in[10], in[11]);
		this.textureSShift=DataReader.readFloat(in[12], in[13], in[14], in[15]);
		this.textureT=DataReader.readPoint3F(in[16], in[17], in[18], in[19], in[20], in[21], in[22], in[23], in[24], in[25], in[26], in[27]);
		this.textureTShift=DataReader.readFloat(in[28], in[29], in[30], in[31]);
		this.lightmapS=DataReader.readPoint3F(in[32], in[33], in[34], in[35], in[36], in[37], in[38], in[39], in[40], in[41], in[42], in[43]);
		this.lightmapSShift=DataReader.readFloat(in[44], in[45], in[46], in[47]);
		this.lightmapT=DataReader.readPoint3F(in[48], in[49], in[50], in[51], in[52], in[53], in[54], in[55], in[56], in[57], in[58], in[59]);
		this.lightmapTShift=DataReader.readFloat(in[60], in[61], in[62], in[63]);
		this.flags=DataReader.readInt(in[64], in[65], in[66], in[67]);
		this.texdata=DataReader.readInt(in[68], in[69], in[70], in[71]);
	}
	
	// METHODS
	
	// ACCESSORS/MUTATORS
	public void setTextureS(Vector3D in) {
		textureS=in;
	}
	
	public Vector3D getTextureS() {
		return textureS;
	}
	
	public void setTextureSShift(float in) {
		textureSShift=in;
	}
	
	public float getTextureSShift() {
		return textureSShift;
	}
	
	public void setTextureT(Vector3D in) {
		textureT=in;
	}
	
	public Vector3D getTextureT() {
		return textureT;
	}
	
	public void setTextureTShift(float in) {
		textureTShift=in;
	}
	
	public float getTextureTShift() {
		return textureTShift;
	}
	
	public void setLightmapS(Vector3D in) {
		lightmapS=in;
	}
	
	public Vector3D getLightmapS() {
		return lightmapS;
	}
	
	public void setLightmapSShift(float in) {
		lightmapSShift=in;
	}
	
	public float getLightmapSShift() {
		return lightmapSShift;
	}
	
	public void setLightmapT(Vector3D in) {
		lightmapT=in;
	}
	
	public Vector3D getLightmapT() {
		return lightmapT;
	}
	
	public void setLightmapTShift(float in) {
		lightmapTShift=in;
	}
	
	public float getLightmapTShift() {
		return lightmapTShift;
	}
	
	public void setFlags(int in) {
		flags=in;
	}
	
	public int getFlags() {
		return flags;
	}
	
	public void setTexData(int in) {
		texdata=in;
	}
	
	public int getTexData() {
		return texdata;
	}
}