// MoHAAStaticProp class

// Holds all necessary data for a static prop in a MoHAA BSP

public class MoHAAStaticProp {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	private String model;
	private float[] origin=new float[3];
	private float[] angles=new float[3];
	private float unknown0; // Might be a scale or size?
	private int unknown1;
	private int unknown2; // Spawnflags?
	
	// CONSTRUCTORS
	
	public MoHAAStaticProp(String model, float[] origin, float[] angles, float unknown0, int unknown1, int unknown2) {
		this.model=model;
		this.origin=origin;
		this.angles=angles;
		this.unknown0=unknown0;
		this.unknown1=unknown1;
		this.unknown2=unknown2;
	}
	
	public MoHAAStaticProp(byte[] in) {
		for(int i=0;i<128;i++) {
			if(in[i]==0x00) {
				break;
			}
			model+=(char)in[i];
		}
		origin[0]=DataReader.readFloat(in[128], in[129], in[130], in[131]);
		origin[1]=DataReader.readFloat(in[132], in[133], in[134], in[135]);
		origin[2]=DataReader.readFloat(in[136], in[137], in[138], in[139]);
		angles[0]=DataReader.readFloat(in[140], in[141], in[142], in[143]);
		angles[1]=DataReader.readFloat(in[144], in[145], in[146], in[147]);
		angles[2]=DataReader.readFloat(in[148], in[149], in[150], in[151]);
		unknown0=DataReader.readFloat(in[152], in[153], in[154], in[155]);
		unknown1=DataReader.readInt(in[156], in[157], in[158], in[159]);
		unknown2=DataReader.readInt(in[160], in[161], in[162], in[163]);
	}
	
	// METHODS

	// ACCESSORS/MUTATORS
	
	public String getModel() {
		return model;
	}
	
	public void setModel(String in) {
		model=in;
	}
	
	public float[] getOrigin() {
		return origin;
	}

	public void setOrigin(float[] in) {
		origin=in;
	}
	
	public float[] getAngles() {
		return angles;
	}

	public void setAngles(float[] in) {
		angles=in;
	}
	
	public float getUnknown0() {
		return unknown0;
	}
	
	public void setUnknown0(float in) {
		unknown0=in;
	}
	
	public int getUnknown1() {
		return unknown1;
	}
	
	public void setUnknown1(int in) {
		unknown1=in;
	}
	
	public int getUnknown2() {
		return unknown2;
	}
	
	public void setUnknown2(int in) {
		unknown2=in;
	}
}
