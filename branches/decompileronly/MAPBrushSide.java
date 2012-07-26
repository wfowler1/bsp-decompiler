// MAPBrushSide class
// Holds all the data for a brush side in the format for a .MAP file version 510.

import java.text.DecimalFormat;

public class MAPBrushSide {

	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	private Vector3D[] triangle=new Vector3D[3]; // Plane defined as three points
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
	
	private boolean planeDefined=false;
	private boolean triangleDefined=false;
	
	public static final int X=0;
	public static final int Y=1;
	public static final int Z=2;
	
	// CONSTRUCTORS
	// Takes a triangle of points, and calculates a new standard equation for a plane with it. Not recommended.
	public MAPBrushSide(Vector3D[] inTriangle, String inTexture, double[] inTextureS, double inTextureShiftS, double[] inTextureT, double inTextureShiftT, float inTexRot,
	                    double inTexScaleX, double inTexScaleY, int inFlags, String inMaterial, double inLgtScale, double inLgtRot) {
		triangle[0]=inTriangle[0];
		triangle[1]=inTriangle[1];
		triangle[2]=inTriangle[2];
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
		planeDefined=false;
		triangleDefined=true;
	}
	
	// Takes both a plane and triangle. Recommended if at all possible.
	public MAPBrushSide(Plane plane, Vector3D[] inTriangle, String inTexture, double[] inTextureS, double inTextureShiftS, double[] inTextureT, double inTextureShiftT, float inTexRot,
	                    double inTexScaleX, double inTexScaleY, int inFlags, String inMaterial, double inLgtScale, double inLgtRot) {
		triangle[0]=inTriangle[0];
		triangle[1]=inTriangle[1];
		triangle[2]=inTriangle[2];
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
		planeDefined=true;
		triangleDefined=true;
	}

	// Takes only a plane and finds three arbitrary points on it. Recommend only if triangle is not available.
	public MAPBrushSide(Plane plane, String inTexture, double[] inTextureS, double inTextureShiftS, double[] inTextureT, double inTextureShiftT, float inTexRot,
	                    double inTexScaleX, double inTexScaleY, int inFlags, String inMaterial, double inLgtScale, double inLgtRot) {
		this.plane=plane;
		triangle=GenericMethods.extrapPlanePoints(plane);
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
		planeDefined=true;
		triangleDefined=false;
	}

	// METHODS

	// toString()
	// Returns the brush side exactly as it would look in a .MAP file.
	// This is on multiple lines simply for readability. the returned
	// String will have no line breaks. This isn't used anymore for
	// file output, this would be slower.
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
	// Negate all definitions of the plane in the side.
	// Don't need to change the indicators of whether or not this side was defined by
	// triangle or plane, since both definitions are still valid. I'm using the same
	// information, I'm just reversing the direction.
	public void flipSide() {
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
	
	public boolean isDefinedByPlane() {
		return planeDefined;
	}
	
	public boolean isDefinedByTriangle() {
		return triangleDefined;
	}
	
	// ACCESSORS/MUTATORS
	public void setTriangle(Vector3D[] in) {
		System.out.println("SETTING A FUCKING TRIANGLE");
		triangle[0]=in[0];
		triangle[1]=in[1];
		triangle[2]=in[2];
		plane=new Plane(triangle);
		planeDefined=false;
		triangleDefined=true;
	}
	
	public void setSide(Plane plane, Vector3D[] triangle) {
		if(triangle.length>=3) {
			this.triangle[0]=triangle[0];
			this.triangle[1]=triangle[1];
			this.triangle[2]=triangle[2];
			triangleDefined=true;
		} else {
			Window.window.println("Tried to define side with "+triangle.length+" points!");
		}
		this.plane=plane;
		planeDefined=true;
	}
	
	public void setPlane(Plane in) {
		plane=in;
		triangle=GenericMethods.extrapPlanePoints(plane);
		planeDefined=true;
		triangleDefined=false;
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
