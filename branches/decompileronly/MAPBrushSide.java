// MAPBrushSide class
// Holds all the data for a brush side in the format for a .MAP file version 510.

import java.text.DecimalFormat;

public class MAPBrushSide {

	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	private Vector3D[] triangle; // Plane defined as three points
	private Plane plane;
	private String texture;
	private Vector3D textureS;
	private double textureShiftS;
	private Vector3D textureT;
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
	
	DecimalFormat fmt = new DecimalFormat("0.000000");
	DecimalFormat fmtScales = new DecimalFormat("0.####");
	
	// CONSTRUCTORS
	public MAPBrushSide(Vector3D[] inTriangle, String inTexture, double[] inTextureS, double inTextureShiftS, double[] inTextureT, double inTextureShiftT, float inTexRot,
	                    double inTexScaleX, double inTexScaleY, int inFlags, String inMaterial, double inLgtScale, double inLgtRot) {
	//	triangle[0]=inTriangle[0];
	//	triangle[1]=inTriangle[1];
	//	triangle[2]=inTriangle[2];
		triangle=inTriangle;
		this.plane=new Plane(triangle);
		texture=inTexture;
		textureS=new Vector3D(inTextureS);
		textureShiftS=inTextureShiftS;
		textureT=new Vector3D(inTextureT);
		textureShiftT=inTextureShiftT;
		texRot=inTexRot;
		texScaleX=inTexScaleX;
		texScaleY=inTexScaleY;
		flags=inFlags;
		material=inMaterial;
		lgtScale=inLgtScale;
		lgtRot=inLgtRot;
	}
	
	public MAPBrushSide(Plane plane, Vector3D[] inTriangle, String inTexture, double[] inTextureS, double inTextureShiftS, double[] inTextureT, double inTextureShiftT, float inTexRot,
	                    double inTexScaleX, double inTexScaleY, int inFlags, String inMaterial, double inLgtScale, double inLgtRot) {
	//	triangle[0]=inTriangle[0];
	//	triangle[1]=inTriangle[1];
	//	triangle[2]=inTriangle[2];
		this.plane=plane;
		triangle=inTriangle;
		texture=inTexture;
		textureS=new Vector3D(inTextureS);
		textureShiftS=inTextureShiftS;
		textureT=new Vector3D(inTextureT);
		textureShiftT=inTextureShiftT;
		texRot=inTexRot;
		texScaleX=inTexScaleX;
		texScaleY=inTexScaleY;
		flags=inFlags;
		material=inMaterial;
		lgtScale=inLgtScale;
		lgtRot=inLgtRot;
	}

	public MAPBrushSide(Plane plane, String inTexture, double[] inTextureS, double inTextureShiftS, double[] inTextureT, double inTextureShiftT, float inTexRot,
	                    double inTexScaleX, double inTexScaleY, int inFlags, String inMaterial, double inLgtScale, double inLgtRot) {
		this.plane=plane;
		triangle=GenericMethods.extrapPlanePoints(plane, 100);
		texture=inTexture;
		textureS=new Vector3D(inTextureS);
		textureShiftS=inTextureShiftS;
		textureT=new Vector3D(inTextureT);
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
		try {
			return "( "+triangle[0].getX()+" "+triangle[0].getY()+" "+triangle[0].getZ()+" ) "+
			       "( "+triangle[1].getX()+" "+triangle[1].getY()+" "+triangle[1].getZ()+" ) "+
			       "( "+triangle[2].getX()+" "+triangle[2].getY()+" "+triangle[2].getZ()+" ) "+
			       texture + 
			       " [ "+textureS.getX()+" "+textureS.getY()+" "+textureS.getZ()+" "+textureShiftS+" ]"+
			       " [ "+textureT.getX()+" "+textureT.getY()+" "+textureT.getZ()+" "+textureShiftT+" ] "+
			       texRot+" "+texScaleX+" "+texScaleY+" "+flags+" "+
			       material +
			       " [ "+lgtScale+" "+lgtRot+" ]";
		} catch(java.lang.NullPointerException e) {
			Window.window.println("Side with bad data! Not exported!");
			return null;
		}
	}
	
	// flipPlane()
	// Negate the plane
	public void flipPlane() {
		Vector3D temp=triangle[2];
		triangle[2]=triangle[1];
		triangle[1]=temp;
		plane.flip();
	}
	
	// shift(Vector3D)
	// Shifts the brush side and its points by the amounts in the input Vector
	public void shift(Vector3D shift) {
		triangle[0]=triangle[0].add(shift);
		triangle[1]=triangle[1].add(shift);
		triangle[2]=triangle[2].add(shift);
		plane=new Plane(triangle);
	}
	
	// ACCESSORS/MUTATORS
	public void setTriangle(Vector3D[] in) {
		triangle[0]=in[0];
		triangle[1]=in[1];
		triangle[2]=in[2];
		plane=new Plane(triangle);
	}
	
	public double getLgtScale() {
		return lgtScale;
	}
	
	public double getLgtRot() {
		return lgtRot;
	}
	
	public String getMaterial() {
		return material;
	}
	
	public int getFlags() {
		return flags;
	}
	
	public double getTexScaleX() {
		return texScaleX;
	}
	
	public double getTexScaleY() {
		return texScaleY;
	}
	
	public double getTextureShiftS() {
		return textureShiftS;
	}
	
	public double getTextureShiftT() {
		return textureShiftT;
	}
	
	public Vector3D[] getTriangle() {
		return triangle;
	}
	
	public Plane getPlane() {
		return plane;
	}
	
	public String getTexture() {
		return texture;
	}
	
	public Vector3D getTextureS() {
		return textureS;
	}
	
	public Vector3D getTextureT() {
		return textureT;
	}
	
	public float getTexRot() {
		return texRot;
	}
}
