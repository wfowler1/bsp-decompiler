// MAPBrush class
// Maintains a list of MAPBrushSides, to be written to a .MAP format file.

public class MAPBrush {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	private MAPBrushSide[] sides;
	private boolean isDetailBrush=false;
	private boolean isWaterBrush=false; // Both Source and Quake 2 compile water brushes into the world. Hammer doesn't use func_water.
	// So I need to keep track of whether a brush is water or not, so I can deal with it on a per-mapformat basis.
	
	// For debugging purposes only, if there's a problem these will be in the error message
	private int entnum;
	private int brushnum;
	
	// CONSTRUCTORS
	
	public MAPBrush(int num, int entnum, boolean isDetailBrush) {
		sides=new MAPBrushSide[0];
		brushnum=num;
		this.entnum=entnum;
		this.isDetailBrush=isDetailBrush;
	}
	
	public MAPBrush(MAPBrushSide[] in, int num, int entnum, boolean isDetail) {
		sides=new MAPBrushSide[0];
		for(int i=0;i<in.length;i++) {
			add(in[i]);
		}
		brushnum=num;
		this.entnum=entnum;
		this.isDetailBrush=isDetail;
	}
	
	public MAPBrush(MAPBrush in) {
		brushnum=in.getBrushnum();
		entnum=in.getEntnum();
		this.isDetailBrush=in.isDetailBrush();
		this.isWaterBrush=in.isWaterBrush();
		sides=new MAPBrushSide[in.getNumSides()];
		for(int i=0;i<in.getNumSides();i++) {
			sides[i]=new MAPBrushSide(in.getSide(i));
		}
	}
	
	// METHODS
	
	// Adds the new brush side to the brush, but only if it's not an already defined
	// side. Otherwise you might get "brush with coplanar planes" error.
	public void add(MAPBrushSide in) {
		if(in!=null) {
			boolean duplicate=false;
			int i=0;
			for(i=0;i<sides.length;i++) {
				if(in.getPlane().equals(sides[i].getPlane())) {
					duplicate=true;
					break;
				}
			}
			if(!duplicate) {
				MAPBrushSide[] newList=new MAPBrushSide[sides.length+1];
				for(int j=0;j<sides.length;j++) {
					newList[j]=sides[j];
				}
				newList[sides.length] = in;
				sides=newList;
			} else { // If it is a duplicate, one of them probably came form a Doom node subdivision which will always be textured "special/nodraw"
				      // but the other one (from a segment) will have a texture which needs to be visible. Select for that one.
				if(sides[i].getTexture().equalsIgnoreCase("special/nodraw") && !in.getTexture().equalsIgnoreCase("special/nodraw")) {
					sides[i].setTexture(in.getTexture());
				}
			}
		}
	}
	
	public void delete(int side) {
		MAPBrushSide[] newList=new MAPBrushSide[sides.length-1];
		for(int i=0;i<side;i++) {
			newList[i]=sides[i];
		}
		for(int i=side+1;i<sides.length;i++) {
			newList[i-1]=sides[i];
		}
		sides=newList;
	}
	
	// hasGoodSide()
	// Returns true if any side in the brush did have a triangle defined in its constructor,
	// or had one defined for it elsewhere.
	public boolean hasGoodSide() {
		for(int i=0;i<sides.length;i++) {
			if(sides[i].isDefinedByTriangle()) {
				return true;
			}
		}
		return false;
	}
	
	// hasBadSide()
	// Returns true if any side in the brush didn't have a triangle defined.
	// Even if it didn't, that's not necessarily a bad thing. MAPBrushSides
	// will generate a triangle from the plane passed to them instead, but
	// the triangle might be very esoteric, and certainly won't be brush corners.
	public boolean hasBadSide() {
		for(int i=0;i<sides.length;i++) {
			if(!sides[i].isDefinedByTriangle()) {
				return true;
			}
		}
		return false;
	}
	
	// Moves the brush by the Vector.
	public void shift(Vector3D shiftVector) {
		for(int i=0;i<sides.length;i++) {
			sides[i].shift(shiftVector);
		}
	}
	
	// toString()
	// DEPRECATED for use in generating mapfiles.
	@Deprecated
	public String toString() {
		String out="{ // Brush "+brushnum+(char)0x0D+(char)0x0A;
		if(isDetailBrush) {
			out+="\"BRUSHFLAGS\" \"DETAIL\""+(char)0x0D+(char)0x0A;
		}
		for(int i=0;i<sides.length;i++) {
			out+=sides[i].toString()+(char)0x0D+(char)0x0A;
		}
		out+="}";
		return out;
	}
	
	// ACCESSORS/MUTATORS
	
	public MAPBrushSide getSide(int i) {
		return sides[i];
	}
	
	public Plane getPlane(int i) {
		return sides[i].getPlane();
	}
	
	public Plane[] getPlanes() {
		Plane[] planes=new Plane[sides.length];
		for(int i=0;i<sides.length;i++) {
			planes[i]=sides[i].getPlane();
		}
		return planes;
	}
	
	public int getNumSides() {
		return sides.length;
	}
	
	public boolean isDetailBrush() {
		return isDetailBrush;
	}
	
	public void setDetail(boolean in) {
		isDetailBrush=in;
	}
	
	public boolean isWaterBrush() {
		return isWaterBrush;
	}
	
	// These aren't common enough to warrent setting this in the constructor.
	public void setWater(boolean in) {
		isWaterBrush=in;
	}
	
	public int getBrushnum() {
		return brushnum;
	}
	
	public int getEntnum() {
		return entnum;
	}
}
