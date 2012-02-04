// Plane class

// This class holds data on ONE plane. It's only really useful when
// used in an array along with many others. Each piece of data has
// its own variable.

public class Plane {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	public final int A=0;
	public final int B=1;
	public final int C=2;
	
	private float[] normal=new float[3]; // Coefficients in the equation Ax+By+Cz=D
	private float dist; // D in the equation
	private int type; // Still not sure what the fuck this is for. Thought it was an axis, but that'd
	                  // be pointless since Ax+By+Cz=D is sufficient for any plane ever, as long as A.B.C=1,
	                  // where . is the dot product
	
	// CONSTRUCTORS
	
	// This one takes the components separate and in the correct data type
	public Plane(float inA, float inB, float inC, float inDist, int inType) {
		normal[A]=inA;
		normal[B]=inB;
		normal[C]=inC;
		dist=inDist;
		type=inType;
	}
	
	public Plane(float[] inNormal, float inDist, int inType) {
		normal=inNormal;
		dist=inDist;
		type=inType;
	}

	// This one takes an array of bytes (as if read directly from a file) and reads them
	// directly into the proper data types.
	// Another reason to port this to a different language: JAVA USES BIG ENDIAN BYTE ORDER
	// Maybe there's some property of the runtime you can set to use little endian. Maybe
	// Java is just retarded.
	public Plane(byte[] in) {
		int myInt=(in[3] << 24) | ((in[2] & 0xff) << 16) | ((in[1] & 0xff) << 8) | (in[0] & 0xff);
		normal[A]=Float.intBitsToFloat(myInt);
		myInt=(in[7] << 24) | ((in[6] & 0xff) << 16) | ((in[5] & 0xff) << 8) | (in[4] & 0xff);
		normal[B]=Float.intBitsToFloat(myInt);
		myInt=(in[11] << 24) | ((in[10] & 0xff) << 16) | ((in[9] & 0xff) << 8) | (in[8] & 0xff);
		normal[C]=Float.intBitsToFloat(myInt);
		myInt=(in[15] << 24) | ((in[14] & 0xff) << 16) | ((in[13] & 0xff) << 8) | (in[12] & 0xff);
		dist=Float.intBitsToFloat(myInt);
		type=(in[19] << 24) | ((in[18] & 0xff) << 16) | ((in[17] & 0xff) << 8) | (in[16] & 0xff);
	}
	
	// METHODS
	
	// +distance(Point3D)
	// Gets signed distance from plane to point
	public double distance(Point3D in) {
		return (normal[A]*in.getX() + normal[B]*in.getY() + normal[C]*in.getZ() - dist)/(Math.sqrt(Math.pow(normal[A],2) + Math.pow(normal[B],2) + Math.pow(normal[C],2)));
	}
	
	// +intersect
	// Defines the line where this plane intersects with another
	// public 
	
	// ACCESSORS/MUTATORS
	
	// returns the coordinates as a float3
	public float[] getNormal() {
		return normal;
	}
	
	public float getA() {
		return normal[A];
	}
	
	public float getB() {
		return normal[B];
	}
	
	public float getC() {
		return normal[C];
	}
	
	public float getDist() {
		return dist;
	}
	
	public int getType() {
		return type;
	}
	
	public void setA(float in) {
		normal[A]=in;
	}
	
	public void setB(float in) {
		normal[B]=in;
	}
	
	public void setC(float in) {
		normal[C]=in;
	}
	
	public void setDist(float in) {
		dist=in;
	}
	
	public void setType(int in) {
		type=in;
	}
}
