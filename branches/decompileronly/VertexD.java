// VertexD class

// Holds a doubke3, for a vertex. Identical to the Vertex class except uses Doubles

public class VertexD {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	public final int X=0;
	public final int Y=1;
	public final int Z=2;
	
	private double[] vertex=new double[3];
	
	// CONSTRUCTORS
	
	// This constructor takes three floats as X Y and Z
	public VertexD(float inX, float inY, float inZ) {
		vertex[X]=(double)inX;
		vertex[Y]=(double)inY;
		vertex[Z]=(double)inZ;
	}
	
	// This constructor takes three doubles as X Y and Z
	public VertexD(double inX, double inY, double inZ) {
		vertex[X]=inX;
		vertex[Y]=inY;
		vertex[Z]=inZ;
	}
	
	// This constructor takes one float[3]
	public VertexD(float[] in) throws InvalidVertexException {
		if(in.length!=3) {
			throw new InvalidVertexException();
		}
		vertex[X]=(double)in[X];
		vertex[Y]=(double)in[Y];
		vertex[Z]=(double)in[Z];
	}
	
	// This constructor takes one double[3]
	public VertexD(double[] in) throws InvalidVertexException {
		if(in.length!=3) {
			throw new InvalidVertexException();
		}
		vertex[X]=in[X];
		vertex[Y]=in[Y];
		vertex[Z]=in[Z];
	}
	
	// This constructor takes a Vertex
	public VertexD(Vertex in) {
		vertex[X]=(double)in.getX();
		vertex[Y]=(double)in.getY();
		vertex[Z]=(double)in.getZ();
	}
	
	// This constructor takes twelve bytes in a byte array, as though
	// it had just been read by a FileInputStream.
	public VertexD(byte[] in) throws InvalidVertexException {
		if(in.length!=12) {
			throw new InvalidVertexException();
		}
		int myInt=(in[3] << 24) | ((in[2] & 0xff) << 16) | ((in[1] & 0xff) << 8) | (in[0] & 0xff);
		vertex[X]=(double)Float.intBitsToFloat(myInt);
		myInt=(in[7] << 24) | ((in[6] & 0xff) << 16) | ((in[5] & 0xff) << 8) | (in[4] & 0xff);
		vertex[Y]=(double)Float.intBitsToFloat(myInt);
		myInt=(in[11] << 24) | ((in[10] & 0xff) << 16) | ((in[9] & 0xff) << 8) | (in[8] & 0xff);
		vertex[Z]=(double)Float.intBitsToFloat(myInt);
	}
	
	// METHODS
	// +subtract(VertexD)
	// Subtracts all the components of another vertex from this one and returns the result
	public VertexD subtract(VertexD in) {
		return new VertexD(vertex[X]-in.getX(), vertex[Y]-in.getY(), vertex[Z]-in.getZ());
	}
	
	// +add(VertexD)
	// Subtracts all the components of another vertex from this one and returns the result
	public VertexD add(VertexD in) {
		return new VertexD(vertex[X]+in.getX(), vertex[Y]+in.getY(), vertex[Z]+in.getZ());
	}
	
	// +scale(double)
	// Multiplies all components by the input and returns the result
	public VertexD scale(double scalar) {
		return new VertexD(vertex[X]*scalar, vertex[Y]*scalar, vertex[Z]*scalar);
	}
	
	// +equals(VertexD)
	// Return true if the components of the input Vertex are equivalent to those of this one
	public boolean equals(VertexD in) {
		return (vertex[X]==in.getX() && vertex[Y]==in.getY() && vertex[Z]==in.getZ());
	}
	
	// ACCESSORS/MUTATORS
	
	public double getX() {
		return vertex[X];
	}
	
	public double getY() {
		return vertex[Y];
	}
	
	public double getZ() {
		return vertex[Z];
	}
	
	public double[] getVertex() {
		return vertex;
	}
	
	public void setX(double in) {
		vertex[X]=in;
	}
	
	public void setY(double in) {
		vertex[Y]=in;
	}
	
	public void setZ(double in) {
		vertex[Z]=in;
	}
	
	public void setVertex(double[] in) throws InvalidVertexException {
		if(in.length!=3) {
			throw new InvalidVertexException();
		} else {
			vertex=in;
		}
	}
	
	public double getLength() {
		return Math.sqrt(Math.pow(vertex[X], 2) + Math.pow(vertex[Y], 2) + Math.pow(vertex[Z], 2));
	}
}