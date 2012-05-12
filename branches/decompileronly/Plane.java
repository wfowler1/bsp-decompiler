// Plane class

// This class holds data on ONE plane. It's only really useful when
// used in an array along with many others. Each piece of data has
// its own variable.
// With help from Alex "UltimateSniper" Harrod
public class Plane {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	public final int A=0;
	public final int B=1;
	public final int C=2;
	
	private Vector3D normal; // Coefficients in the equation Ax+By+Cz=D
	private double dist; // D in the equation
	
	// CONSTRUCTORS
	
	// This one takes the components separate and in the correct data type
	public Plane(float inA, float inB, float inC, float inDist) {
		new Plane((double)inA, (double)inB, (double)inC, (double)inDist);
	}
	
	public Plane(double inA, double inB, double inC, double inDist) {
		normal=new Vector3D(inA, inB, inC);
		dist=inDist;
	}
	
	public Plane(Vector3D normal, double dist) {
		normal=new Vector3D(normal);
		this.dist=dist;
	}
	
	public Plane(Plane in) {
		normal = new Vector3D(in.getNormal());
		dist = in.getDist();
	}
	// Takes 3 vertices, which define the plane.
	public Plane(Vector3D a, Vector3D b, Vector3D c) {
		normal = a.subtract(c).cross(a.subtract(b));
		normal = normal.scale(1 / normal.getLength());
		dist = a.dot(normal);
	}
	public Plane(Vector3D[] in) {
		normal = in[A].subtract(in[C]).cross(in[A].subtract(in[B]));
		normal = normal.scale(1 / normal.getLength());
		dist = in[A].dot(normal);
	}
	public Plane(float[] inNormal, float inDist) {
		normal=new Vector3D(inNormal[A], inNormal[B], inNormal[C]);
		dist=(double)inDist;
	}
	
	public Plane(Vector3D normal, float dist) {
		this.normal=normal;
		this.dist=(double)dist;
	}
	
	// Takes an array of bytes (as if read directly from a file) and reads them directly into values.
	//   Another reason to port this to a different language: JAVA USES BIG ENDIAN BYTE ORDER
	//   Maybe there's some property of the runtime you can set to use little endian.
	public Plane(byte[] in) throws java.lang.ArrayIndexOutOfBoundsException {
		if (in.length >= 16 && in.length < 32) {
			byte[] norm = new byte[12];
			System.arraycopy(in, 0, norm, 0, 12);
			normal = DataReader.readPoint3F(norm);
			dist = (double)DataReader.readFloat(in[12], in[13], in[14], in[15]);
		} else if (in.length >= 32) {
			byte[] norm = new byte[24];
			System.arraycopy(in, 0, norm, 0, 24);
			normal = DataReader.readPoint3D(norm);
			dist = DataReader.readDouble(in[24], in[25], in[26], in[27], in[28], in[29], in[30], in[31]);
		} else {
			throw new java.lang.ArrayIndexOutOfBoundsException();
		}
	}
	
	// METHODS
	
	/// Returns:	Whether this plane is parallel to, faces the same direction, and has the same distance as, the given plane.
	public boolean equals(Plane in) {
		// Use Cross-Product; if 0, parallel. Must face same direction, have parallel normals, and identical distances.
		Vector3D inNorm = in.getNormal();
		return (normal.dot(inNorm) > 0 && Math.abs((normal.getY()*inNorm.getZ()) - (normal.getZ()*inNorm.getY())) < 0.01 && Math.abs((normal.getX()*inNorm.getZ()) - (normal.getZ()*inNorm.getX())) < 0.01 && Math.abs((normal.getX()*inNorm.getY()) - (normal.getY()*inNorm.getX())) < 0.01 && Math.abs(dist - in.getDist()) < 0.01);
	}
	
	/// Returns:	Signed distance from this plane to given vertex.
	public double distance(Vector3D to) {
		return distance(to.getPoint());
	}
	public double distance(double[] to) {
		// Ax + By + Cz - d = DISTANCE = normDOTpoint - d
		double normLength = Math.pow(normal.getX(),2) + Math.pow(normal.getY(),2) + Math.pow(normal.getZ(),2);
		if (normLength != 1.00)
			normLength = Math.sqrt(normLength);
		return (normal.getX()*to[0] + normal.getY()*to[1] + normal.getZ()*to[2] - dist)/normLength;
	}
	
	/// Returns:	Point where this plane intersects 2 planes given. Gives Vector3D.Undefined if any planes are parallel.
	public Vector3D trisect(Plane p2, Plane p3) {
		// Normals
		double[] n2 = p2.getNormal().getPoint();
		double[] n3 = p3.getNormal().getPoint();
		// Distance
		double d2 = p2.getDist();
		double d3 = p3.getDist();
		// Cross Products
		double[] cr1 = Vector3D.crossProduct(n2, n3).getPoint();
		double denominator = Vector3D.dotProduct(normal, cr1);
		if (denominator == 0) {
			return Vector3D.undefined;
		}
		double[] cr2 = Vector3D.crossProduct(n3, normal).getPoint();
		double[] cr3 = Vector3D.crossProduct(normal, n2).getPoint();
		// Calculate
		return new Vector3D((dist*cr1[0] + d2*cr2[0] + d3*cr3[0])/denominator, (dist*cr1[1] + d2*cr2[1] + d3*cr3[1])/denominator, (dist*cr1[2] + d2*cr2[2] + d3*cr3[2])/denominator);
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
	
	// ACCESSORS/MUTATORS
	
	// Gets
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
	
	public float getAF() {
		return (float)normal.getX();
	}
	public float getBF() {
		return (float)normal.getY();
	}
	public float getCF() {
		return (float)normal.getZ();
	}
	public float getDistF() {
		return (float)dist;
	}
	
	// Sets
	public void setNormal(Vector3D in) {
		normal = in;
	}
	public void setA(double in) {
		normal.setX(in);
	}
	public void setB(double in) {
		normal.setY(in);
	}
	public void setC(double in) {
		normal.setZ(in);
	}
	public void setDist(double in) {
		dist = in;
	}
	
	public void setA(float in) {
		normal.setX((double)in);
	}
	public void setB(float in) {
		normal.setY((double)in);
	}
	public void setC(float in) {
		normal.setZ((double)in);
	}
	public void setDist(float in) {
		dist = (double)in;
	}
}
