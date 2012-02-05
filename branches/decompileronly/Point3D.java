// Point3D class

// Holds a double3, for a point. Identical to the Vertex class except uses Doubles
// Incidentally, I'd LOVE to use 128-bit quads for this, but no such thing exists in Java.

public class Point3D {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	public final int X=0;
	public final int Y=1;
	public final int Z=2;
	
	private double[] point=new double[3];
	
	// CONSTRUCTORS
	
	// This constructor takes three floats as X Y and Z
	public Point3D(float inX, float inY, float inZ) {
		point[X]=(double)inX;
		point[Y]=(double)inY;
		point[Z]=(double)inZ;
	}
	
	// This constructor takes three doubles as X Y and Z
	public Point3D(double inX, double inY, double inZ) {
		point[X]=inX;
		point[Y]=inY;
		point[Z]=inZ;
	}
	
	// This constructor takes one float[3]
	public Point3D(float[] in) {
		try {
			point[X]=(double)in[X];
			point[Y]=(double)in[Y];
			point[Z]=(double)in[Z];
		} catch(java.lang.ArrayIndexOutOfBoundsException e) {
			;
		}
	}
	
	// This constructor takes one double[3]
	public Point3D(double[] in) {
		try {
			point[X]=in[X];
			point[Y]=in[Y];
			point[Z]=in[Z];
		} catch(java.lang.ArrayIndexOutOfBoundsException e) {
			;
		}
	}
	
	// This constructor takes a Vertex
	public Point3D(Point3D in) {
		point[X]=in.getX();
		point[Y]=in.getY();
		point[Z]=in.getZ();
	}
	
	// This constructor takes twelve bytes in a byte array, as though
	// it had just been read by a FileInputStream.
	public Point3D(byte[] in) {
		point[X]=(double)DataReader.readFloat(in[0], in[1], in[2], in[3]);
		point[Y]=(double)DataReader.readFloat(in[4], in[5], in[6], in[7]);
		point[Z]=(double)DataReader.readFloat(in[8], in[9], in[10], in[11]);
	}
	
	// METHODS
	// +subtract(Point3D)
	// Subtracts all the components of another point from this one and returns the result
	public Point3D subtract(Point3D in) {
		return new Point3D(point[X]-in.getX(), point[Y]-in.getY(), point[Z]-in.getZ());
	}
	
	// +add(Point3D)
	// Subtracts all the components of another point from this one and returns the result
	public Point3D add(Point3D in) {
		return new Point3D(point[X]+in.getX(), point[Y]+in.getY(), point[Z]+in.getZ());
	}
	
	// +scale(double)
	// Multiplies all components by the input and returns the result
	public Point3D scale(double scalar) {
		return new Point3D(point[X]*scalar, point[Y]*scalar, point[Z]*scalar);
	}
	
	// +equals(Point3D)
	// Return true if the components of the input Vertex are equivalent to those of this one
	public boolean equals(Point3D in) {
		return (point[X]==in.getX() && point[Y]==in.getY() && point[Z]==in.getZ());
	}
	
	// +distance(Vertex)
	// Returns the distance from this point to another one
	public double distance(Point3D in) {
		return Math.sqrt(Math.pow((point[X]-in.getX()),2) + Math.pow((point[Y]-in.getY()),2) + Math.pow((point[Z]-in.getZ()),2));
	}
	
	// ACCESSORS/MUTATORS
	
	public double getX() {
		return point[X];
	}
	
	public double getY() {
		return point[Y];
	}
	
	public double getZ() {
		return point[Z];
	}
	
	public float getXF() {
		return (float)point[X];
	}
	
	public float getYF() {
		return (float)point[Y];
	}
	
	public float getZF() {
		return (float)point[Z];
	}
	
	public double[] getPoint() {
		return point;
	}
	
	public void setX(double in) {
		point[X]=in;
	}
	
	public void setY(double in) {
		point[Y]=in;
	}
	
	public void setZ(double in) {
		point[Z]=in;
	}
	
	public void setPoint(double[] in) {
		try {
			point[X]=in[X];
			point[Y]=in[Y];
			point[Z]=in[Z];
		} catch(java.lang.ArrayIndexOutOfBoundsException e) {
			;
		}
	}
	
	public double getLength() {
		return Math.sqrt(Math.pow(point[X], 2) + Math.pow(point[Y], 2) + Math.pow(point[Z], 2));
	}
}