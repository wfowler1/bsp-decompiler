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
	private double[] origin;
	private double planePointCoef;
	private int entnum;
	private boolean isDetailBrush;
	
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
	
	public MAPBrush(int num, int entnum, double[] origin, double planePointCoef, boolean isDetailBrush) {
		sides=new MAPBrushSide[0];
		goodSides=new boolean[0];
		planes=new Plane[0];
		triangles=new Vector3D[0][];
		brushNum=num;
		this.entnum=entnum;
		this.origin=origin;
		this.planePointCoef=planePointCoef;
		this.isDetailBrush=isDetailBrush;
	}
	
	// METHODS
	
	public void add(MAPBrushSide in, Vector3D[] triangle, Plane plane, boolean pointsWorked) {
		boolean duplicate=false;
		for(int i=0;i<sides.length;i++) {
			if(in.getPlane().equals(sides[i].getPlane())) {
				duplicate=true;
				break;
			}
		}
		if(!duplicate) {
			Vector3D originVector=new Vector3D(origin);
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
			newList[sides.length].shift(originVector);
			newGoodSides[goodSides.length]=pointsWorked;
			newPlaneList[planes.length] = plane;
			newTriangles[sides.length] = triangle;
			sides=newList;
			planes=newPlaneList;
			triangles=newTriangles;
			goodSides=newGoodSides;
		}
	}
	
	public void add(MAPBrushSide in) {
		boolean duplicate=false;
		for(int i=0;i<sides.length;i++) {
			if(in.getPlane().equals(sides[i].getPlane())) {
				duplicate=true;
				break;
			}
		}
		if(!duplicate) {
			Vector3D originVector=new Vector3D(origin);
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
			newList[sides.length].shift(originVector);
			newPlaneList[planes.length] = new Plane(in.getPlane());
			newTriangles[sides.length] = in.getTriangle();
			sides=newList;
			planes=newPlaneList;
			triangles=newTriangles;
			goodSides=newGoodSides;
		}
	}
	
	public void delete(int side) {
		MAPBrushSide[] newList=new MAPBrushSide[sides.length-1];
		Plane[] newPlaneList=new Plane[sides.length-1];
		Vector3D[][] newTriangles=new Vector3D[sides.length-1][];
		boolean[] newGoodSides=new boolean[sides.length-1];
		for(int i=0;i<side;i++) {
			newList[i]=sides[i];
			newPlaneList[i]=planes[i];
			newTriangles[i]=triangles[i];
			newGoodSides[i]=goodSides[i];
		}
		for(int i=side+1;i<sides.length;i++) {
			newList[i-1]=sides[i];
			newPlaneList[i-1]=planes[i];
			newTriangles[i-1]=triangles[i];
			newGoodSides[i-1]=goodSides[i];
		}
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
					sides[i].setTriangle(triangles[i]);
				}
			}
		} else {
			if(hasBadSide()) {
				triangles=GenericMethods.AdvancedCorrectPlanes(planes, PRECISION);
				if(triangles.length!=sides.length) {
					Window.window.println("WARNING: Produced "+triangles.length+" triangles from Entity "+entnum+" Brush "+brushNum+"! Expected "+planes.length+"!");
					triangles=new Vector3D[sides.length][3];
					for(int i=0;i<sides.length;i++) {
						triangles[i]=GenericMethods.extrapPlanePoints(planes[i], planePointCoef);
						sides[i].setTriangle(triangles[i]);
					}
				} else {
					for(int i=0;i<sides.length;i++) {
						sides[i].setTriangle(triangles[i]);
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
					try {
						triangles[i]=newTriangles[i];
						sides[i].setTriangle(triangles[i]);
					} catch(java.lang.NullPointerException e) {
						Window.window.println("WARNING: Recalculating brush corners failed on Entity "+entnum+" Brush "+brushNum+" Side "+i+"!");
					}
				}
			}
		}
	}
	
	public String toString() {
		String out="{ // Brush "+brushNum+(char)0x0D+(char)0x0A;
		if(isDetailBrush) {
			out+="\"BRUSHFLAGS\" \"DETAIL\""+(char)0x0D+(char)0x0A;
		}
		for(int i=0;i<sides.length;i++) {
			out+=sides[i].toString()+(char)0x0D+(char)0x0A;
		}
		out+="}";
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
	
	public Plane[] getPlanes() {
		Plane[] planes=new Plane[sides.length];
		for(int i=0;i<sides.length;i++) {
			planes[i]=sides[i].getPlane();
		}
		return planes;
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
	
	public boolean isDetailBrush() {
		return isDetailBrush;
	}
	
	public void setDetail(boolean in) {
		isDetailBrush=in;
	}
}
