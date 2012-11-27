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
		if(inTriangle.length>=3 && inTriangle[0]!=null && inTriangle[1]!=null && inTriangle[2]!=null) {
			triangle[0]=inTriangle[0];
			triangle[1]=inTriangle[1];
			triangle[2]=inTriangle[2];
		} else {
			throw new java.lang.ArithmeticException("Invalid point definition for a plane: \n"+inTriangle[0]+"\n"+inTriangle[1]+"\n"+inTriangle[2]);
		}
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
		//Window.println(triangle[0]+"\n"+triangle[1]+"\n"+triangle[2]+"\n\n");
	}
	
	// Takes both a plane and triangle. Recommended if at all possible.
	public MAPBrushSide(Plane plane, Vector3D[] inTriangle, String inTexture, double[] inTextureS, double inTextureShiftS, double[] inTextureT, double inTextureShiftT, float inTexRot,
	                    double inTexScaleX, double inTexScaleY, int inFlags, String inMaterial, double inLgtScale, double inLgtRot) {
		if(inTriangle.length>=3 && inTriangle[0]!=null && inTriangle[1]!=null && inTriangle[2]!=null) {
			triangle[0]=inTriangle[0];
			triangle[1]=inTriangle[1];
			triangle[2]=inTriangle[2];
		} else {
			throw new java.lang.ArithmeticException("Invalid point definition for a plane: \n"+inTriangle[0]+"\n"+inTriangle[1]+"\n"+inTriangle[2]);
		}
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
		//Window.println(triangle[0]+"\n"+triangle[1]+"\n"+triangle[2]+"\n\n");
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
	
	public MAPBrushSide(MAPBrushSide copy) {
		plane=new Plane(copy.getPlane());
		triangle[0]=new Vector3D(copy.getTriangle()[0]);
		triangle[1]=new Vector3D(copy.getTriangle()[1]);
		triangle[2]=new Vector3D(copy.getTriangle()[2]);
		texture=copy.getTexture();
		textureS=new Vector3D(copy.getTextureS());
		textureT=new Vector3D(copy.getTextureT());
		textureShiftS=copy.getTextureShiftS();
		textureShiftT=copy.getTextureShiftT();
		texRot=copy.getTexRot();
		texScaleX=copy.getTexScaleX();
		texScaleY=copy.getTexScaleY();
		flags=copy.getFlags();
		material=copy.getMaterial();
		lgtScale=copy.getLgtScale();
		lgtRot=copy.getLgtRot();
		planeDefined=copy.isDefinedByPlane();
		triangleDefined=copy.isDefinedByTriangle();
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
			Window.println("WARNING: Side with bad data! Not exported!",Window.VERBOSITY_WARNINGS);
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
		try {
			if(shift.getX()!=0 || shift.getY()!=0 || shift.getZ()!=0) {
				triangle[0]=triangle[0].add(shift);
				triangle[1]=triangle[1].add(shift);
				triangle[2]=triangle[2].add(shift);
				plane=new Plane(triangle);
			}
		} catch(java.lang.Exception e) {
			Window.println("WARNING: Failed to shift triangle:"+e+(char)0x0D+(char)0x0A+triangle[0]+(char)0x0D+(char)0x0A+triangle[1]+(char)0x0D+(char)0x0A+triangle[2]+(char)0x0D+(char)0x0A+"Adding: "+shift,Window.VERBOSITY_ALWAYS);
		}
	}
	
	public boolean isDefinedByPlane() {
		return planeDefined;
	}
	
	public boolean isDefinedByTriangle() {
		return triangleDefined;
	}
	
	// ACCESSORS/MUTATORS
	public void setTriangle(Vector3D[] in) {
		if(in.length>=3) {
			if(in[0]==null) {
				Window.println("WARNING: Tried to set triangle but point 0 was null!",Window.VERBOSITY_WARNINGS);
			} else {
				if(in[1]==null) {
					Window.println("WARNING: Tried to set triangle but point 1 was null!",Window.VERBOSITY_WARNINGS);
				} else {
					if(in[2]==null) {
						Window.println("WARNING: Tried to set triangle but point 2 was null!",Window.VERBOSITY_WARNINGS);
					} else {
						triangle[0]=in[0];
						triangle[1]=in[1];
						triangle[2]=in[2];
						plane=new Plane(triangle);
						planeDefined=false;
						triangleDefined=true;
					}
				}
			}
		} else {
			Window.println("WARNING: Tried to define side with "+triangle.length+" points!",Window.VERBOSITY_WARNINGS);
		}
	}
	
	public void setSide(Plane plane, Vector3D[] triangle) {
		if(triangle.length>=3) {
			if(triangle[0]==null) {
				Window.println("WARNING: Tried to set triangle but point 0 was null!",Window.VERBOSITY_WARNINGS);
			} else {
				if(triangle[1]==null) {
					Window.println("WARNING: Tried to set triangle but point 1 was null!",Window.VERBOSITY_WARNINGS);
				} else {
					if(triangle[2]==null) {
						Window.println("WARNING: Tried to set triangle but point 2 was null!",Window.VERBOSITY_WARNINGS);
					} else {
						this.triangle[0]=triangle[0];
						this.triangle[1]=triangle[1];
						this.triangle[2]=triangle[2];
						triangleDefined=true;
						this.plane=plane;
						planeDefined=true;
					}
				}
			}
		} else {
			Window.println("WARNING: Tried to define side with "+triangle.length+" points!",Window.VERBOSITY_WARNINGS);
		}

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
	
	public void setTexture(String in) {
		texture=in;
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
