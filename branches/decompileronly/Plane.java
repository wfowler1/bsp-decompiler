// Plane class

// This class holds data on ONE plane. It's only really useful when
// used in an array along with many others. Each piece of data has
// its own variable.

public class Plane {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	public final int A=0;
	public final int B=1;
	public final int C=2;
	
	private Point3D normal; // Coefficients in the equation Ax+By+Cz=D
	private float dist; // D in the equation
	
	// CONSTRUCTORS
	
	// This one takes the components separate and in the correct data type
	public Plane(float inA, float inB, float inC, float inDist) {
		normal=new Point3D(inA, inB, inC);
		dist=inDist;
	}
	
	public Plane(double inA, double inB, double inC, float inDist) {
		normal=new Point3D(inA, inB, inC);
		dist=inDist;
	}
	
	public Plane(float[] inNormal, float inDist) {
		normal=new Point3D(inNormal[A], inNormal[B], inNormal[C]);
		dist=inDist;
	}
	
	public Plane(Point3D normal, float dist) {
		this.normal=normal;
		this.dist=dist;
	}

	// This one takes an array of bytes (as if read directly from a file) and reads them
	// directly into the proper data types.
	public Plane(byte[] in) {
		normal=DataReader.readPoint3F(in[0], in[1], in[2], in[3], in[4], in[5], in[6], in[7], in[8], in[9], in[10], in[11]);
		dist=DataReader.readFloat(in[12], in[13], in[14], in[15]);
	}
	
	// METHODS
	
	// +distance(Point3D)
	// Gets signed distance from plane to point
	public double distance(Point3D in) {
		return (normal.getX()*in.getX() + normal.getY()*in.getY() + normal.getZ()*in.getZ() - dist)/(Math.sqrt(Math.pow(normal.getX(),2) + Math.pow(normal.getY(),2) + Math.pow(normal.getZ(),2)));
	}
	
	// ACCESSORS/MUTATORS
	
	// returns the coordinates as a float3
	public Point3D getNormal() {
		return normal;
	}
	
	public double getA() {
		return normal.getX();
	}
	
	public double getB() {
		return normal.getY();
	}
	
	public double getC() {
		return normal.getZ();
	}
	
	public float getDist() {
		return dist;
	}
	
	public void setA(float in) {
		normal.setX(in);
	}
	
	public void setB(float in) {
		normal.setY(in);
	}
	
	public void setC(float in) {
		normal.setZ(in);
	}
	
	public void setDist(float in) {
		dist=in;
	}
}
