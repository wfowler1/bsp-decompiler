// Vertex class

// Holds a float3, for a vertex.

public class Vertex {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	public final int X=0;
	public final int Y=1;
	public final int Z=2;
	
	private float[] vertex=new float[3];
	
	// CONSTRUCTORS
	
	// This constructor takes three floats as X Y and Z
	public Vertex(float inX, float inY, float inZ) {
		vertex[X]=inX;
		vertex[Y]=inY;
		vertex[Z]=inZ;
	}
	// This constructor takes one float[3]
	public Vertex(float[] in) throws InvalidVertexException {
		if(in.length!=3) {
			throw new InvalidVertexException();
		}
		vertex=in;
	}
	
	// This constructor takes twelve bytes in a byte array, as though
	// it had just been read by a FileInputStream.
	public Vertex(byte[] in) throws InvalidVertexException {
		if(in.length!=12) {
			throw new InvalidVertexException();
		}
		int myInt=(in[3] << 24) | ((in[2] & 0xff) << 16) | ((in[1] & 0xff) << 8) | (in[0] & 0xff);
		vertex[X]=Float.intBitsToFloat(myInt);
		myInt=(in[7] << 24) | ((in[6] & 0xff) << 16) | ((in[5] & 0xff) << 8) | (in[4] & 0xff);
		vertex[Y]=Float.intBitsToFloat(myInt);
		myInt=(in[11] << 24) | ((in[10] & 0xff) << 16) | ((in[9] & 0xff) << 8) | (in[8] & 0xff);
		vertex[Z]=Float.intBitsToFloat(myInt);
	}
	
	// METHODS
	// +subtract(Vertex)
	// Subtracts all the components of another vertex from this one and returns the result
	public Vertex subtract(Vertex in) {
		return new Vertex(vertex[X]-in.getX(), vertex[Y]-in.getY(), vertex[Z]-in.getZ());
	}
	
	// ACCESSORS/MUTATORS
	
	public float getX() {
		return vertex[X];
	}
	
	public float getY() {
		return vertex[Y];
	}
	
	public float getZ() {
		return vertex[Z];
	}
	
	public float[] getVertex() {
		return vertex;
	}
	
	public void setX(float in) {
		vertex[X]=in;
	}
	
	public void setY(float in) {
		vertex[Y]=in;
	}
	
	public void setZ(float in) {
		vertex[Z]=in;
	}
	
	public void setVertex(float[] in) throws InvalidVertexException {
		if(in.length!=3) {
			throw new InvalidVertexException();
		} else {
			vertex=in;
		}
	}
}