// MAPBrush class
// Maintains a list of MAPBrushSides, to be written to a .MAP format file.

public class MAPBrush {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	MAPBrushSide[] sides;
	Plane[] planes; // Keep a record of which side uses which plane
	                // This isn't 100% necessary unless you want to use the original normal+dist
	                // info AFTER the MAPBrushSide object has been created.
	boolean[] goodSides; // This will be the index of a side which is definitely facing the right direction.
	                 // Heavy reliance on the user of this class knowing wtf he's doing XD
	int brushNum; // Set this to which brush this is; not really 100% necessary either
	
	// CONSTRUCTORS
	
	public MAPBrush() {
		sides=new MAPBrushSide[0];
		planes=new Plane[0];
		goodSides=new boolean[0];
	}
	
	public MAPBrush(int num) {
		sides=new MAPBrushSide[0];
		planes=new Plane[0];
		goodSides=new boolean[0];
		brushNum=num;
	}
	
	// METHODS
	
	public void add(MAPBrushSide in, boolean isGoodSide, Plane plane) {
		MAPBrushSide[] newList=new MAPBrushSide[sides.length+1];
		Plane[] newPlaneList=new Plane[sides.length+1];
		boolean[] newGoodSideList=new boolean[sides.length+1];
		for(int i=0;i<sides.length;i++) {
			newList[i]=sides[i];
			newPlaneList[i]=planes[i];
			newGoodSideList[i]=goodSides[i];
		}
		newList[sides.length] = in;
		newPlaneList[planes.length] = plane;
		newGoodSideList[sides.length] = isGoodSide;
		sides=newList;
		planes=newPlaneList;
		goodSides=newGoodSideList;
	}
	
	public String toString() {
		String out="{ // Brush "+brushNum+(char)0x0D+(char)0x0A;
		for(int i=0;i<sides.length;i++) {
			try {
				out+=sides[i].toString()+(char)0x0D+(char)0x0A;
			} catch(java.lang.NullPointerException e) { // If the object was never created, because the face was special/bevel
				; // Do nothing, but this should never happen anyway
			}
		}
		out+="}";
		return out;
	}
	
	public boolean hasGoodSide() {
		for(int i=0;i<goodSides.length;i++) {
			if(goodSides[i]) {
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
	
	public int getGoodSide() {
		for(int i=0;i<goodSides.length;i++) {
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
