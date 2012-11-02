// Plane class
// Holds the A, B, C and D components for a plane, which can be read from any given BSP.

public class Plane extends LumpObject {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	public static final int A=0;
	public static final int B=1;
	public static final int C=2;
	
	private Vector3D normal;
	private double dist=Double.NaN;
	
	// CONSTRUCTORS
	
	// This one takes the components separate and in the correct data type
	public Plane(float inA, float inB, float inC, float inDist) {
		super(new byte[0]);
		new Plane((double)inA, (double)inB, (double)inC, (double)inDist);
	}
	
	public Plane(double inA, double inB, double inC, double inDist) {
		super(new byte[0]);
		normal=new Vector3D(inA, inB, inC);
		dist=inDist;
	}
	
	public Plane(Vector3D normal, double dist) {
		super(new byte[0]);
		this.normal=new Vector3D(normal);
		this.dist=dist;
	}
	
	public Plane(Plane in) {
		super(new byte[0]);
		normal = new Vector3D(in.getNormal());
		dist = in.getDist();
	}
	// Takes 3 vertices, which define the plane.
	public Plane(Vector3D a, Vector3D b, Vector3D c) {
		super(new byte[0]);
		normal = a.subtract(c).cross(a.subtract(b));
		normal = normal.scale(1.0 / normal.length());
		dist = a.dot(normal);
	}
	public Plane(Vector3D[] in) {
		super(new byte[0]);
		normal = in[A].subtract(in[C]).cross(in[A].subtract(in[B]));
		normal = normal.scale(1.0 / normal.length());
		dist = in[A].dot(normal);
	}
	public Plane(float[] inNormal, float inDist) {
		super(new byte[0]);
		normal=new Vector3D(inNormal[A], inNormal[B], inNormal[C]);
		dist=(double)inDist;
	}
	
	public Plane(Vector3D normal, float dist) {
		super(new byte[0]);
		this.normal=normal;
		this.dist=(double)dist;
	}
	
	public Plane(LumpObject in, int type) {
		super(in.getData());
		new Plane(in.getData(), type);
	}

	public Plane(byte[] data, int type) {
		super(data);
		normal=DataReader.readPoint3F(data[0], data[1], data[2], data[3], data[4], data[5], data[6], data[7], data[8], data[9], data[10], data[11]);
		dist=(double)DataReader.readFloat(data[12], data[13], data[14], data[15]);
	}
	
	// METHODS
	
	/// Returns:	Whether this plane is parallel to, faces the same direction, and has the same distance as, the given plane.
	public boolean equals(Plane in) {
		// Use Cross-Product; if 0, parallel. Must face same direction, have parallel normals, and identical distances.
		Vector3D inNorm = in.getNormal();
		return (normal.equals(inNorm) && dist+Window.getPrecision() >= in.getDist() && dist-Window.getPrecision() <= in.getDist());
	}
	
	/// Returns:	Signed distance from this plane to given vertex.
	public double distance(Vector3D to) {
		return distance(to.getPoint());
	}
	public double distance(double[] to) {
		// Ax + By + Cz - d = DISTANCE = normDOTpoint - d
		double normLength = Math.pow(normal.getX(),2) + Math.pow(normal.getY(),2) + Math.pow(normal.getZ(),2);
		if (Math.abs(normLength-1.00) > 0.01) {
			normLength = Math.sqrt(normLength);
		}
		return (normal.getX()*to[0] + normal.getY()*to[1] + normal.getZ()*to[2] - dist)/normLength;
	}
	
	/// Returns:	Point where this plane intersects 2 planes given. Gives Vector3D.Undefined if any planes are parallel.
	public Vector3D trisect(Plane p2, Plane p3) {
		Vector3D bN = p2.getNormal();
		Vector3D cN = p3.getNormal();
		/* Math:
		 *  x1*x y1*y z1*z     d1        x1 y1 z1  *  x     d1        x     x1 y1 z1 ^-1  *  d1     (d1*(y2*z3-z2*y3) + d2*(y3*z1-z3*y1) + d3*(y1*z2-z1*y2)) / (x1*(y2*z3-z2*y3) + y1*(x3*z2-z3*x2) + z1*(x2*y3-y2*x3))
		 *  x2*x y2*y z2*z  =  d2   =>   x2 y2 z2     y  =  d2   =>   y  =  x2 y2 z2         d2  =  (d1*(x3*z2-z3*x2) + d2*(x1*z3-z1*x3) + d3*(x2*z1-z2*x1)) / (x1*(y2*z3-z2*y3) + y1*(x3*z2-z3*x2) + z1*(x2*y3-y2*x3))
		 *  x3*x y3*y z3*z     d3        x3 y3 z3     z     d3        z     x3 y3 z3         d3     (d1*(x2*y3-y2*x3) + d2*(x3*y1-y3*x1) + d3*(x1*y2-y1*x2)) / (x1*(y2*z3-z2*y3) + y1*(x3*z2-z3*x2) + z1*(x2*y3-y2*x3))
		 *  -> Note that the 3 sets of brackets used in the determinant (the denominator) are also used in some cases before the division (these are the first row of the inverted matrix).
		 *  --> Fastest method: calc once and use twice.
		*/
		double PartSolx1 = bN.getY() * cN.getZ() - bN.getZ() * cN.getY();
		double PartSoly1 = bN.getZ() * cN.getX() - bN.getX() * cN.getZ();
		double PartSolz1 = bN.getX() * cN.getY() - bN.getY() * cN.getX();
		double det = normal.getX() * PartSolx1 + normal.getY() * PartSoly1 + normal.getZ() * PartSolz1; // Determinant
		if (det == 0) { // If 0, 2 or more planes are parallel.
			return Vector3D.undefined;
		}
		// Divide by determinant to get final matrix solution, and multiply by matrix of distances to get final position.
		return new Vector3D(
			(dist * PartSolx1 + p2.getDist() * (cN.getY() * normal.getZ() - cN.getZ() * normal.getY()) + p3.getDist() * (normal.getY() * bN.getZ() - normal.getZ() * bN.getY())) / det,
			(dist * PartSoly1 + p2.getDist() * (normal.getX() * cN.getZ() - normal.getZ() * cN.getX()) + p3.getDist() * (bN.getX() * normal.getZ() - bN.getZ() * normal.getX())) / det,
			(dist * PartSolz1 + p2.getDist() * (cN.getX() * normal.getY() - cN.getY() * normal.getX()) + p3.getDist() * (normal.getX() * bN.getY() - normal.getY() * bN.getX())) / det);
	}
	
	// Flips plane to face opposite direction.
	public void flip() {
		normal = normal.negate();
		dist = -dist;
	}
	
	// Takes a plane as an array of vertices and flips it over.
	public static Vector3D[] flip(Vector3D[] in) {
		return new Vector3D[] {in[0], in[2], in[1]};
	}
	
	// Takes a Plane and flips it (static method)
	public static Plane flip(Plane in) {
		return new Plane(in.getNormal().negate(), -in.getDist());
	}
	
	// Returns this plane, flipped
	public Plane negate() {
		return new Plane(normal.negate(), -dist);
	}
	
	public String toString() {
		return "("+normal.toString()+") "+dist;
	}
	
	// ACCESSORS/MUTATORS
	
	public Vector3D getNormal() {
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
	
	public double getDist() {
		return dist;
	}
}