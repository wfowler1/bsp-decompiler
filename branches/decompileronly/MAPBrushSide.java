// MAPBrushSide class
// Holds all the data for a brush side in the format for a .MAP file version 510.

public class MAPBrushSide {

	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	private Point3D[] plane; // Plane defined as three points
	private String texture;
	private double[] textureS;
	private double textureShiftS;
	private double[] textureT;
	private double textureShiftT;
	private float texRot=0;
	private double texScaleX;
	private double texScaleY;
	private int flags;
	private String material;
	private double lgtScale;
	private double lgtRot;
	
	public static final int X=0;
	public static final int Y=1;
	public static final int Z=2;
	
	// CONSTRUCTORS
	public MAPBrushSide(Point3D[] inPlane, String inTexture, double[] inTextureS, double inTextureShiftS, double[] inTextureT, double inTextureShiftT, float inTexRot,
	                    double inTexScaleX, double inTexScaleY, int inFlags, String inMaterial, double inLgtScale, double inLgtRot) {
		plane=inPlane;
		texture=inTexture;
		textureS=inTextureS;
		textureShiftS=inTextureShiftS;
		textureT=inTextureT;
		textureShiftT=inTextureShiftT;
		texRot=inTexRot;
		texScaleX=inTexScaleX;
		texScaleY=inTexScaleY;
		flags=inFlags;
		material=inMaterial;
		lgtScale=inLgtScale;
		lgtRot=inLgtRot;
	}
	
	// METHODS
	
	// toString()
	// Returns the brush side exactly as it would look in a .MAP file.
	// This is on multiple lines simply for readability. the returned
	// String will have no line breaks.
	public String toString() {
		return "( "+plane[0].getX()+" "+plane[0].getY()+" "+plane[0].getZ()+" ) "+
		       "( "+plane[1].getX()+" "+plane[1].getY()+" "+plane[1].getZ()+" ) "+
		       "( "+plane[2].getX()+" "+plane[2].getY()+" "+plane[2].getZ()+" ) "+
		       texture + 
		       " [ "+textureS[X]+" "+textureS[Y]+" "+textureS[Z]+" "+textureShiftS+" ]"+
		       " [ "+textureT[X]+" "+textureT[Y]+" "+textureT[Z]+" "+textureShiftT+" ] "+
		       texRot+" "+texScaleX+" "+texScaleY+" "+flags+" "+
		       material +
		       " [ "+lgtScale+" "+lgtRot+" ]";
	}
	
	// flipPlane()
	// Negate the plane
	public void flipPlane() {
		Point3D temp=plane[2];
		plane[2]=plane[1];
		plane[1]=temp;
	}
	
	// ACCESSORS/MUTATORS
	public Point3D[] getTriangle() {
		return plane;
	}
}
