// v46Vertex class

// Contains all data on a single Vertex object in the v46 BSP.
// Don't know why all this information is contained in vertices
// of all places. Doesn't seem quite appropriate for texture
// scaling information.

public class v46Vertex {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	private Point3D vertex;
	private float surf_texCoordX;
	private float surf_texCoordY;
	private float lm_texCoordX;
	private float lm_texCoordY;
	private Point3D normal; // ???????????
	private byte[] color=new byte[4]; // RGBA
	
	// CONSTRUCTORS
	
	// This constructor takes all data in their proper data types
	public v46Vertex(Point3D vertex, float surf_texCoordX, float surf_texCoordY, float lm_texCoordX, float lm_texCoordY, Point3D normal, byte[] color) {
		this.vertex=vertex;
		this.surf_texCoordX=surf_texCoordX;
		this.surf_texCoordY=surf_texCoordY;
		this.lm_texCoordX=lm_texCoordX;
		this.lm_texCoordY=lm_texCoordY;
		this.normal=normal;
		this.color[0]=color[0];
		this.color[1]=color[1];
		this.color[2]=color[2];
		this.color[3]=color[3];
	}
	
	// This constructor takes 20 bytes in a byte array, as though
	// it had just been read by a FileInputStream.
	public v46Vertex(byte[] in) {
		vertex=DataReader.readPoint3F(in[0], in[1], in[2], in[3], in[4], in[5], in[6], in[7], in[8], in[9], in[10], in[11]);
		surf_texCoordX=DataReader.readFloat(in[12], in[13], in[14], in[15]);
		surf_texCoordY=DataReader.readFloat(in[16], in[17], in[18], in[19]);
		lm_texCoordX=DataReader.readFloat(in[20], in[21], in[22], in[23]);
		lm_texCoordY=DataReader.readFloat(in[24], in[25], in[26], in[27]);
		normal=DataReader.readPoint3F(in[28], in[29], in[30], in[31], in[32], in[33], in[34], in[35], in[36], in[37], in[38], in[39]);
		color[0]=in[40];
		color[1]=in[41];
		color[2]=in[42];
		color[3]=in[43];
	}
	
	// METHODS
	
	// ACCESSORS/MUTATORS
	
	public Point3D getVertex() {
		return vertex;
	}
	
	public void setVertex(Point3D vertex) {
		this.vertex=vertex;
	}
	
	public float getSurfaceTexCoordX() {
		return surf_texCoordX;
	}
	
	public void setSurfaceTexCoordX(float surf_texCoordX) {
		this.surf_texCoordX=surf_texCoordX;
	}
	
	public float getSurfaceTexCoordY() {
		return surf_texCoordY;
	}
	
	public void setSurfaceTexCoordY(float surf_texCoordY) {
		this.surf_texCoordY=surf_texCoordY;
	}
	
	public float getLightmapTexCoordX() {
		return lm_texCoordX;
	}
	
	public void setLightmapTexCoordX(float lm_texCoordX) {
		this.lm_texCoordX=lm_texCoordX;
	}
	
	public float getLightmapTexCoordY() {
		return lm_texCoordY;
	}
	
	public void setLightmapTexCoordY(float lm_texCoordY) {
		this.lm_texCoordY=lm_texCoordY;
	}
	
	public Point3D getNormal() {
		return normal;
	}
	
	public void setNormal(Point3D normal) {
		this.normal=normal;
	}
	
	public byte[] getColor() {
		return color;
	}
	
	public void setColor(byte[] color) {
		this.color[0]=color[0];
		this.color[1]=color[1];
		this.color[2]=color[2];
		this.color[3]=color[3];
	}
	
	// Directly reference the vertex, to make life easier later on
	public float getX() {
		return (float)vertex.getX();
	}
	
	public void setX(float inX) {
		vertex.setX((double)inX);
	}
	
	public float getY() {
		return (float)vertex.getY();
	}
	
	public void setY(float inY) {
		vertex.setY((double)inY);
	}
	
	public float getZ() {
		return (float)vertex.getZ();
	}
	
	public void setZ(float inZ) {
		vertex.setZ((double)inZ);
	}
}