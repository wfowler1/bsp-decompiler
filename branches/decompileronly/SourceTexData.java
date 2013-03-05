// SourceTexData class

// Contains all the information for a single SourceTexData object

public class SourceTexData extends LumpObject {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	private Vector3D reflectivity;
	private int stringTableIndex;
	private int width;
	private int height;
	private int view_width;
	private int view_height;
	
	// CONSTRUCTORS
	
	// Takes everything exactly as it is stored
	public SourceTexData(Vector3D reflectivity, int stringTableIndex, int width, int height, int view_width, int view_height) {
		super(new byte[0]);
		this.reflectivity=reflectivity;
		this.stringTableIndex=stringTableIndex;
		this.width=width;
		this.height=height;
		this.view_width=view_width;
		this.view_height=view_height;
	}
	
	// This constructor takes bytes in a byte array, as though
	// it had just been read by a FileInputStream.
	public SourceTexData(byte[] in) {
		super(in);
		this.reflectivity=DataReader.readPoint3F(in[0], in[1], in[2], in[3], in[4], in[5], in[6], in[7], in[8], in[9], in[10], in[11]);
		this.stringTableIndex=DataReader.readInt(in[12], in[13], in[14], in[15]);
		this.width=DataReader.readInt(in[16], in[17], in[18], in[19]);
		this.height=DataReader.readInt(in[20], in[21], in[22], in[23]);
		this.view_width=DataReader.readInt(in[24], in[25], in[26], in[27]);
		this.view_height=DataReader.readInt(in[28], in[29], in[30], in[31]);
	}
	
	// METHODS
	
	// ACCESSORS/MUTATORS
	public void setReflectivity(Vector3D in) {
		reflectivity=in;
	}
	
	public Vector3D getReflectivity() {
		return reflectivity;
	}
	
	public void setStringTableIndex(int in) {
		stringTableIndex=in;
	}
	
	public int getStringTableIndex() {
		return stringTableIndex;
	}
	
	public void setWidth(int in) {
		width=in;
	}
	
	public int getWidth() {
		return width;
	}
	
	public void setHeight(int in) {
		height=in;
	}
	
	public int getHeight() {
		return height;
	}
	
	public void setViewWidth(int in) {
		view_width=in;
	}
	
	public int getViewWidth() {
		return view_width;
	}
	
	public void setViewHeight(int in) {
		view_height=in;
	}
	
	public int getViewHeight() {
		return view_height;
	}
}