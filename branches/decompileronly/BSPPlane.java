// BSPPlane class

// This class holds data on ONE plane. It's only really useful when
// used in an array along with many others. Each piece of data has
// its own variable.

public class BSPPlane {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	public final int A=0;
	public final int B=1;
	public final int C=2;
	
	private Plane plane;
	private int type; // Still not sure what the fuck this is for. Thought it was an axis, but that'd
	                  // be pointless since Ax+By+Cz=D is sufficient for any plane ever, as long as A.B.C=1,
	                  // where . is the dot product
	
	// CONSTRUCTORS
	
	// This one takes the components separate and in the correct data type
	public BSPPlane(float inA, float inB, float inC, float inDist, int inType) {
		plane=new Plane(inA, inB, inC, inDist);
		type=inType;
	}
	
	public BSPPlane(double inA, double inB, double inC, float inDist, int inType) {
		plane=new Plane(inA, inB, inC, inDist);
		type=inType;
	}
	
	public BSPPlane(double inA, double inB, double inC, double inDist, int inType) {
		plane=new Plane(inA, inB, inC, inDist);
		type=inType;
	}
	
	public BSPPlane(float[] inNormal, float inDist, int inType) {
		plane=new Plane(inNormal[A], inNormal[B], inNormal[C], inDist);
		type=inType;
	}

	// This one takes an array of bytes (as if read directly from a file) and reads them
	// directly into the proper data types.
	// Another reason to port this to a different language: JAVA USES BIG ENDIAN BYTE ORDER
	// Maybe there's some property of the runtime you can set to use little endian. Maybe
	// Java is just retarded.
	public BSPPlane(byte[] in) {
		Vector3D normal=DataReader.readPoint3F(in[0], in[1], in[2], in[3], in[4], in[5], in[6], in[7], in[8], in[9], in[10], in[11]);
		float dist=DataReader.readFloat(in[12], in[13], in[14], in[15]);
		plane=new Plane(normal, dist);
		type=(in[19] << 24) | ((in[18] & 0xff) << 16) | ((in[17] & 0xff) << 8) | (in[16] & 0xff);
	}
	
	// METHODS
	
	// +distance(Vector3D)
	// Gets signed distance from plane to point
	public double distance(Vector3D in) {
		return (plane.getA()*in.getX() + plane.getB()*in.getY() + plane.getC()*in.getZ() - plane.getDist())/(Math.sqrt(Math.pow(plane.getA(),2) + Math.pow(plane.getB(),2) + Math.pow(plane.getC(),2)));
	}
	
	// +intersect
	// Defines the line where this plane intersects with another
	// public 
	
	// ACCESSORS/MUTATORS
	
	// returns the coordinates as a float3
	public Vector3D getNormal() {
		return plane.getNormal();
	}
	
	public double getA() {
		return plane.getA();
	}
	
	public double getB() {
		return plane.getB();
	}
	
	public double getC() {
		return plane.getC();
	}
	
	public double getDist() {
		return plane.getDist();
	}
	
	public float getDistF() {
		return (float)plane.getDist();
	}
	
	public int getType() {
		return type;
	}
	
	public void setA(float in) {
		plane.setA(in);
	}
	
	public void setB(float in) {
		plane.setB(in);
	}
	
	public void setC(float in) {
		plane.setC(in);
	}
	
	public void setDist(float in) {
		plane.setDist(in);
	}
	
	public void setType(int in) {
		type=in;
	}
}
