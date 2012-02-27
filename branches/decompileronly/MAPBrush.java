// MAPBrush class
// Maintains a list of MAPBrushSides, to be written to a .MAP format file.

public class MAPBrush {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	private MAPBrushSide[] sides;
	private boolean[] goodSides;
	private Plane[] planes; // Keep a record of which side uses which plane
	                        // This isn't 100% necessary unless you want to use the original normal+dist
	                        // info AFTER the MAPBrushSide object has been created. Like when fixing the plane direction.
	private Vector3D[][] triangles; // This will be the index of a side which is already defined by a triangle from vertices.
	                        // Every side will have this. However, only sides defined by triangles will have data
	private int id;
	private double[] origin;
	private double planePointCoef;
	int entnum;
	
	private static final float PRECISION=(float)0.01;
		
	private int brushNum; // Set this to which brush this is; not really 100% necessary either
	
	// CONSTRUCTORS
	
	public MAPBrush() {
		sides=new MAPBrushSide[0];
		goodSides=new boolean[0];
		planes=new Plane[0];
		triangles=new Vector3D[0][];
	}
	
	public MAPBrush(int num) {
		sides=new MAPBrushSide[0];
		goodSides=new boolean[0];
		planes=new Plane[0];
		triangles=new Vector3D[0][];
		brushNum=num;
	}
	
	public MAPBrush(int num, int id) {
		sides=new MAPBrushSide[0];
		goodSides=new boolean[0];
		planes=new Plane[0];
		triangles=new Vector3D[0][];
		brushNum=num;
		this.id=id;
	}
	
	public MAPBrush(int num, int id, int entnum, double[] origin, double planePointCoef) {
		sides=new MAPBrushSide[0];
		goodSides=new boolean[0];
		planes=new Plane[0];
		triangles=new Vector3D[0][];
		brushNum=num;
		this.id=id;
		this.entnum=entnum;
		this.origin=origin;
		this.planePointCoef=planePointCoef;
	}
	
	// METHODS
	
	public void add(MAPBrushSide in, Vector3D[] triangle, Plane plane, boolean pointsWorked) {
		MAPBrushSide[] newList=new MAPBrushSide[sides.length+1];
		boolean[] newGoodSides=new boolean[sides.length+1];
		Plane[] newPlaneList=new Plane[sides.length+1];
		Vector3D[][] newTriangles=new Vector3D[sides.length+1][];
		for(int i=0;i<sides.length;i++) {
			newList[i]=sides[i];
			newGoodSides[i]=goodSides[i];
			newPlaneList[i]=planes[i];
			newTriangles[i]=triangles[i];
		}
		newList[sides.length] = in;
		newGoodSides[goodSides.length]=pointsWorked;
		newPlaneList[planes.length] = plane;
		newTriangles[sides.length] = triangle;
		sides=newList;
		planes=newPlaneList;
		triangles=newTriangles;
		goodSides=newGoodSides;
	}
	
	public void add(MAPBrushSide in) {
		MAPBrushSide[] newList=new MAPBrushSide[sides.length+1];
		Plane[] newPlaneList=new Plane[sides.length+1];
		Vector3D[][] newTriangles=new Vector3D[sides.length+1][];
		boolean[] newGoodSides=new boolean[sides.length+1];
		for(int i=0;i<sides.length;i++) {
			newList[i]=sides[i];
			newPlaneList[i]=planes[i];
			newTriangles[i]=triangles[i];
			newGoodSides[i]=goodSides[i];
		}
		newGoodSides[goodSides.length]=false;
		newList[sides.length] = in;
		newPlaneList[planes.length] = new Plane(in.getPlane());
		newTriangles[sides.length] = in.getPlane();
		sides=newList;
		planes=newPlaneList;
		triangles=newTriangles;
		goodSides=newGoodSides;
	}

	public void correctPlanes() {
		if(hasGoodSide() && hasBadSide()) {
			Vector3D[] goodSide=triangles[getATriangle()];
			planes=GenericMethods.SimpleCorrectPlanes(planes, goodSide, PRECISION);
			for(int i=0;i<sides.length;i++) {
				if(!goodSides[i]) {
					triangles[i]=GenericMethods.extrapPlanePoints(planes[i], planePointCoef);
					sides[i].setPlane(triangles[i]);
				}
			}
		} else {
			if(hasBadSide()) {
				triangles=GenericMethods.AdvancedCorrectPlanes(planes, PRECISION);
				if(triangles.length<sides.length) {
					Window.window.println("Entity "+entnum+" Brush "+brushNum+" with "+planes.length+" planes produced "+triangles.length+" triangles!");
					triangles=new Vector3D[sides.length][3];
					for(int i=0;i<sides.length;i++) {
						triangles[i]=GenericMethods.extrapPlanePoints(planes[i], planePointCoef);
						sides[i].setPlane(triangles[i]);
					}
				} else {
					for(int i=0;i<sides.length;i++) {
						sides[i].setPlane(triangles[i]);
					}
				}
			}
		}
	}
	
	// Only run this AFTER ensuring plane flips!
	public void recalcCorners() {
		if(hasGoodSide() && hasBadSide()) { // Need to check for both of these, otherwise advanced plane flip was run and corners are already known!
			Vector3D[][] newTriangles=GenericMethods.CalcPlanePoints(planes, PRECISION); // Either that or every side was already defined by vertices.
			for(int i=0;i<triangles.length;i++) {
				if(!goodSides[i]) {
					triangles[i]=newTriangles[i];
					sides[i].setPlane(triangles[i]);
				}
			}
		}
	}
	
	public String toString() {
		Vector3D originVector=new Vector3D(origin);
		String out="{ // Brush "+brushNum+(char)0x0D+(char)0x0A;
		for(int i=0;i<sides.length;i++) {
			sides[i].shift(originVector);
			out+=sides[i].toString()+(char)0x0D+(char)0x0A;
		}
		out+="}";
		return out;
	}

	public String toVMFBrush() {
		Vector3D originVector=new Vector3D(origin);
		String out="solid"+(char)0x0D+(char)0x0A+"	{"+(char)0x0D+(char)0x0A+"		\"id\" \""+id+"\""+(char)0x0D+(char)0x0A;
		for(int i=0;i<sides.length;i++) {
			try {
				sides[i].shift(originVector);
				out+=sides[i].toVMFSide()+(char)0x0D+(char)0x0A;
			} catch(java.lang.NullPointerException e) { // If the object was never created, because the face was special/bevel
				; // Do nothing, but this should never happen anyway
			}
		}
		out+="	}";
		return out;
	}
	
	public String toRoundString() {
		Vector3D originVector=new Vector3D(origin);
		String out="{ // Brush "+brushNum+(char)0x0D+(char)0x0A;
		for(int i=0;i<sides.length;i++) {
			sides[i].shift(originVector);
			out+=sides[i].toRoundString()+(char)0x0D+(char)0x0A;
		}
		out+="}";
		return out;
	}

	public String toRoundVMFBrush() {
		Vector3D originVector=new Vector3D(origin);
		String out="solid"+(char)0x0D+(char)0x0A+"	{"+(char)0x0D+(char)0x0A+"		\"id\" \""+id+"\""+(char)0x0D+(char)0x0A;
		for(int i=0;i<sides.length;i++) {
			try {
				sides[i].shift(originVector);
				out+=sides[i].toRoundVMFSide()+(char)0x0D+(char)0x0A;
			} catch(java.lang.NullPointerException e) { // If the object was never created, because the face was special/bevel
				; // Do nothing, but this should never happen anyway
			}
		}
		out+="	}";
		return out;
	}
	
	public boolean hasGoodSide() {
		for(int i=0;i<triangles.length;i++) {
			if(goodSides[i]) {
				return true;
			}
		}
		return false;
	}
	
	public boolean hasBadSide() {
		for(int i=0;i<triangles.length;i++) {
			if(!goodSides[i]) {
				return true;
			}
		}
		return false;
	}
	
	// ACCESSORS/MUTATORS
	
	public MAPBrushSide getSide(int i) {
		return sides[i];
	}
	
	public Plane getPlane(int i) {
		return planes[i];
	}
	
	public int getATriangle() {
		for(int i=0;i<triangles.length;i++) {
			if(goodSides[i]) {
				return i;
			}
		}
		return -1;
	}
	
	public int getNumSides() {
		return sides.length;
	}
	
	public boolean sideIsGood(int i) {
		return goodSides[i];
	}
}
