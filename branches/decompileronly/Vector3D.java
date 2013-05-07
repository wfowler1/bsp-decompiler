// Vector3D class

// Holds a double3, for a point.
// Incidentally, I'd LOVE to use 128-bit quads for this, but no such thing exists in Java.
// Would take waaaayy too much time to process decimals...
// With help from Alex "UltimateSniper" Harrod
public class Vector3D {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	public final int X=0;
	public final int Y=1;
	public final int Z=2;
	
	public static final Vector3D UNDEFINED = new Vector3D(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
	public static final Vector3D UP = new Vector3D(0, 0, 1);
	public static final Vector3D FORWARD = new Vector3D(1, 0, 0);
	public static final Vector3D RIGHT = new Vector3D(0, 1, 0);
	
	private double[] point=new double[3];
	
	// CONSTRUCTORS
	
	// Takes X, Y and Z separate.
	public Vector3D(float inX, float inY, float inZ) {
		point = new double[] { (double)inX , (double)inY , (double)inZ };
	}
	public Vector3D(double inX, double inY, double inZ) {
		point = new double[] { inX , inY , inZ };
	}
	// Takes one array of length 3, containing X, Y and Z.
	public Vector3D(float[] in) {
		try {
			point = new double[] { (double)in[0] , (double)in[1] , (double)in[2] };
		} catch(java.lang.ArrayIndexOutOfBoundsException e) {
			;
		}
	}
	public Vector3D(double[] in) {
		try {
			point[X] = in[X];
			point[Y] = in[Y];
			point[Z] = in[Z];
		} catch(java.lang.ArrayIndexOutOfBoundsException e) {
			;
		}
	}
	// Takes a Vector3D.
	public Vector3D(Vector3D in) {
		point = new double[] { in.getX() , in.getY() , in.getZ() };
	}
	// Takes bytes in a byte array, as though it had just been read by a FileInputStream.
	public Vector3D(byte[] in) throws java.lang.ArrayIndexOutOfBoundsException {
		if(in.length >= 12 && in.length < 24) {
			point[X]=(double)DataReader.readFloat(in[0], in[1], in[2], in[3]);
			point[Y]=(double)DataReader.readFloat(in[4], in[5], in[6], in[7]);
			point[Z]=(double)DataReader.readFloat(in[8], in[9], in[10], in[11]);
		} else if (in.length >= 24) {
			point[X] = DataReader.readDouble(in[0], in[1], in[2], in[3], in[4], in[5], in[6], in[7]);
			point[Y] = DataReader.readDouble(in[8], in[9], in[10], in[11], in[12], in[13], in[14], in[15]);
			point[Z] = DataReader.readDouble(in[16], in[17], in[18], in[19], in[20], in[21], in[22], in[23]);
		} else {
			throw new java.lang.ArrayIndexOutOfBoundsException();
		}
	}
	
	// Takes two shorts, and assumes they are X and Y.
	// Useful for turning 2D data from a Doom Map into 3D coordinates
	public Vector3D(short x, short y) {
		point[X]=(double)x;
		point[Y]=(double)y;
		point[Z]=0.0;
	}
	
	// Takes three shorts.
	public Vector3D(short x, short y, short z) {
		point[X]=(double)x;
		point[Y]=(double)y;
		point[Z]=(double)z;
	}
	public Vector3D(short[] in) {
		point[X]=(double)in[X];
		point[Y]=(double)in[Y];
		point[Z]=(double)in[Z];
	}
	
	// METHODS
	
	// Operators
	/// Returns:	Vector3D of the components of this vertex, plus the respective components of the input vertex.
	public Vector3D add(Vector3D in) {
		return new Vector3D(point[0]+in.getX(), point[1]+in.getY(), point[2]+in.getZ());
	}
	
	/// Returns:	Vector3D of the components of this vertex, minus the respective components of the input vertex.
	public Vector3D subtract(Vector3D in) {
		return new Vector3D(point[0]-in.getX(), point[1]-in.getY(), point[2]-in.getZ());
	}
	
	/// Returns:	Negative of this vertex.
	public Vector3D negate() {
		return new Vector3D(-point[0], -point[1], -point[2]);
	}
	
	/// Returns:	Whether or not the vertex is identical to this one.
	public boolean equals(Vector3D in) {
		return (point[0]+Window.getPrecision() >= in.getX() && point[0]-Window.getPrecision() <= in.getX() && point[1]+Window.getPrecision() >= in.getY() && point[1]-Window.getPrecision() <= in.getY() && point[2]+Window.getPrecision() >= in.getZ() && point[2]-Window.getPrecision() <= in.getZ());
	}

	// Scalar product
	/// Returns:	Vector3D of the components of this vertex, multiplied by the scalar value.
	public Vector3D scale(double scalar) {
		return new Vector3D(point[0]*scalar, point[1]*scalar, point[2]*scalar);
	}
	
	// Vector Products
	// Dot
	public static double dotProduct(Vector3D vec1, Vector3D vec2) {
		return dotProduct(vec1.getPoint(), vec2.getPoint());
	}
	public static double dotProduct(double[] vec1, Vector3D vec2) {
		return dotProduct(vec1, vec2.getPoint());
	}
	public static double dotProduct(Vector3D vec1, double[] vec2) {
		return dotProduct(vec1.getPoint(), vec2);
	}
	public static double dotProduct(double[] vec1, double[] vec2) {
		return vec1[0] * vec2[0] + vec1[1] * vec2[1] + vec1[2] * vec2[2];
	}
	public double dot(Vector3D vec) {
		return dot(vec.getPoint());
	}
	public double dot(double[] vec) {
		return point[0] * vec[0] + point[1] * vec[1] + point[2] * vec[2];
	}
	// Cross
	public static Vector3D crossProduct(Vector3D vec1, Vector3D vec2) {
		return crossProduct(vec1.getPoint(), vec2.getPoint());
	}
	public static Vector3D crossProduct(double[] vec1, Vector3D vec2) {
		return crossProduct(vec1, vec2.getPoint());
	}
	public static Vector3D crossProduct(Vector3D vec1, double[] vec2) {
		return crossProduct(vec1.getPoint(), vec2);
	}
	public static Vector3D crossProduct(double[] vec1, double[] vec2) {
		return new Vector3D(vec1[1]*vec2[2] - vec2[1]*vec1[2], vec2[0]*vec1[2] - vec1[0]*vec2[2], vec1[0]*vec2[1] - vec2[0]*vec1[1]);
	}
	public Vector3D cross(Vector3D vec) {
		return cross(vec.getPoint());
	}
	public Vector3D cross(double[] vec) {
		return new Vector3D(point[1]*vec[2] - vec[1]*point[2], vec[0]*point[2] - point[0]*vec[2], point[0]*vec[1] - vec[0]*point[1]);
	}
	
	// Generic
	/// Returns:	Distance from the vertex to the origin.
	public double length() {
		return Math.sqrt(Math.pow(point[0], 2) + Math.pow(point[1], 2) + Math.pow(point[2], 2));
	}
	
	/// Returns:	Distance from this point to another one.
	public double distance(Vector3D in) {
		return Math.sqrt(Math.pow((point[0]-in.getX()),2) + Math.pow((point[1]-in.getY()),2) + Math.pow((point[2]-in.getZ()),2));
	}
	
	/// Returns:	String describing the vertex in the form x , y , x.
	public String toString() {
		return Double.toString(point[0]) + " , " + Double.toString(point[1]) + " , " + Double.toString(point[2]);
	}
	public String toStringF() {
		return Float.toString((float)point[0]) + " , " + Float.toString((float)point[1]) + " , " + Float.toString((float)point[2]);
	}
	
	// Modifies this vector to have length 1, with same direction
	public void normalize() {
		double length=length();
		Vector3D newVector=scale(1/length);
		point[X]=newVector.getX();
		point[Y]=newVector.getY();
		point[Z]=newVector.getZ();
	}
	
	public Vector3D normalized() {
		double length=length();
		return scale(1/length);
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
	
	public float[] getPointF() {
		return new float[] { (float)point[0] , (float)point[1] , (float)point[2] };
	}
	
	public void setX(double in) {
		if(in+Window.getPrecision() >= Math.round(in) && in-Window.getPrecision() <= Math.round(in)) {
			point[X]=(double)Math.round(in);
		} else {
			point[X]=in;
		}
	}
	
	public void setY(double in) {
		if(in+Window.getPrecision() >= Math.round(in) && in-Window.getPrecision() <= Math.round(in)) {
			point[Y]=(double)Math.round(in);
		} else {
			point[Y]=in;
		}
	}
	
	public void setZ(double in) {
		if(in+Window.getPrecision() >= Math.round(in) && in-Window.getPrecision() <= Math.round(in)) {
			point[Z]=(double)Math.round(in);
		} else {
			point[Z]=in;
		}
	}
	
	public void setPoint(double[] in) {
		try {
			point = new double[] { in[0] , in[1] , in[2] };
		} catch(java.lang.ArrayIndexOutOfBoundsException e) {
			;
		}
	}
}
