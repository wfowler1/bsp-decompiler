// SourceStaticProp class
// Handles the data needed for one static prop.
// This is the lump object with the most wild changes between different versions
// and different game implementations. More research needed

public class SourceStaticProp extends LumpObject {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	private Vector3D origin;
	private Vector3D angles;
	private short dictionaryEntry;
	private byte solidity;
	private byte flags;
	private int skin;
	private float minFadeDist;
	private float maxFadeDist;
	private float forcedFadeScale=1;
	
	// CONSTRUCTORS
	public SourceStaticProp(LumpObject in, int type, int version) {
		super(in.getData());
		new SourceStaticProp(in.getData(), type, version);
	}

	public SourceStaticProp(byte[] data, int type, int version) {
		super(data);
		switch(type) {
			case BSP.TYPE_SOURCE17:
			case BSP.TYPE_SOURCE18:
			case BSP.TYPE_SOURCE19:
			case BSP.TYPE_SOURCE20:
			case BSP.TYPE_SOURCE21:
			case BSP.TYPE_SOURCE22:
			case BSP.TYPE_SOURCE23:
				switch(version) {
					case 5:
					case 6:
					case 7:
					case 8:
					case 9:
						forcedFadeScale=DataReader.readFloat(data[56], data[57], data[58], data[59]);
					case 4:
						origin=DataReader.readPoint3F(data[0], data[1], data[2], data[3], data[4], data[5], data[6], data[7], data[8], data[9], data[10], data[11]);
						angles=DataReader.readPoint3F(data[12], data[13], data[14], data[15], data[16], data[17], data[18], data[19], data[20], data[21], data[22], data[23]);
						dictionaryEntry=DataReader.readShort(data[24], data[25]);
						solidity=data[30];
						flags=data[31];
						skin=DataReader.readInt(data[32], data[33], data[34], data[35]);
						minFadeDist=DataReader.readFloat(data[36], data[37], data[38], data[39]);
						maxFadeDist=DataReader.readFloat(data[40], data[41], data[42], data[43]);
					break;
				}
		}
	}
	
	// ACCESSORS/MUTATORS
	public Vector3D getOrigin() {
		return origin;
	}
	
	public Vector3D getAngles() {
		return angles;
	}
	
	public int getDictionaryEntry() {
		return dictionaryEntry;
	}
	
	public byte getSolidity() {
		return solidity;
	}
	
	public byte getFlags() {
		return flags;
	}
	
	public int getSkin() {
		return skin;
	}
	
	public float getMinFadeDist() {
		return minFadeDist;
	}
	
	public float getMaxFadeDist() {
		return maxFadeDist;
	}
	
	public float getForcedFadeScale() {
		return forcedFadeScale;
	}
}