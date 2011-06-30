// MAPBrushSide class
// Holds all the data for a brush side in the format for a .MAP file version 510.

public class MAPBrushSide {

	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	private Vertex[] plane;
	private String texture;
	private float[] textureS;
	private float textureShiftS;
	private float[] textureT;
	private float textureShiftT;
	private float texRot=0;
	private float texScaleX;
	private float texScaleY;
	private int flags;
	private String material;
	private float lgtScale;
	private float lgtRot;
	
	public static final int X=0;
	public static final int Y=1;
	public static final int Z=2;
	
	// CONSTRUCTORS
	public MAPBrushSide(Vertex[] inPlane, String inTexture, float[] inTextureS, float inTextureShiftS, float[] inTextureT, float inTextureShiftT, float inTexRot,
	                    float inTexScaleX, float inTexScaleY, int inFlags, String inMaterial, float inLgtScale, float inLgtRot) throws InvalidMAPBrushSideException {
		if(inPlane.length!=3 || inTextureS.length!=3 || inTextureT.length!=3) {
			throw new InvalidMAPBrushSideException();
		}
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
	
	// ACCESSORS/MUTATORS
}
