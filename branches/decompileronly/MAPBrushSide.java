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
	
	private int id;
	
	public static final int X=0;
	public static final int Y=1;
	public static final int Z=2;
	
	DecimalFormat fmt = new DecimalFormat("0.000000");
	DecimalFormat fmtScales = new DecimalFormat("0.####");
	
	// CONSTRUCTORS
	public MAPBrushSide(Vector3D[] inTriangle, String inTexture, double[] inTextureS, double inTextureShiftS, double[] inTextureT, double inTextureShiftT, float inTexRot,
	                    double inTexScaleX, double inTexScaleY, int inFlags, String inMaterial, double inLgtScale, double inLgtRot, int id) {
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
		this.id=id;
	}
	
	public MAPBrushSide(Plane plane, Vector3D[] inTriangle, String inTexture, double[] inTextureS, double inTextureShiftS, double[] inTextureT, double inTextureShiftT, float inTexRot,
	                    double inTexScaleX, double inTexScaleY, int inFlags, String inMaterial, double inLgtScale, double inLgtRot, int id) {
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
		this.id=id;
	}

	public MAPBrushSide(Plane plane, String inTexture, double[] inTextureS, double inTextureShiftS, double[] inTextureT, double inTextureShiftT, float inTexRot,
	                    double inTexScaleX, double inTexScaleY, int inFlags, String inMaterial, double inLgtScale, double inLgtRot, int id) {
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
		this.id=id;
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
	
	// toVMFSide()
	// Returns the brush side exactly as it would look in a .VMF file.
	public String toVMFSide() {
		try {
			String out="		side"+(char)0x0D+(char)0x0A+"		{"+(char)0x0D+(char)0x0A;
			out+="			\"id\" \""+id+"\""+(char)0x0D+(char)0x0A;
			out+="			\"plane\" \"("+triangle[0].getX()+" "+triangle[0].getY()+" "+triangle[0].getZ()+") ";
			out+="("+triangle[1].getX()+" "+triangle[1].getY()+" "+triangle[1].getZ()+") ";
			out+="("+triangle[2].getX()+" "+triangle[2].getY()+" "+triangle[2].getZ()+")\""+(char)0x0D+(char)0x0A;
			out+="			\"material\" \"" + texture + "\""+(char)0x0D+(char)0x0A;
			out+="			\"uaxis\" \"["+textureS.getX()+" "+textureS.getY()+" "+textureS.getZ()+" "+textureShiftS+"] "+texScaleX+"\""+(char)0x0D+(char)0x0A;
			out+="			\"vaxis\" \"["+textureT.getX()+" "+textureT.getY()+" "+textureT.getZ()+" "+textureShiftT+"] "+texScaleY+"\""+(char)0x0D+(char)0x0A;
			out+="			\"rotation\" \""+texRot+"\""+(char)0x0D+(char)0x0A;
			out+="			\"lightmapscale\" \""+lgtScale+"\""+(char)0x0D+(char)0x0A;
			out+="			\"smoothing_groups\" \"0\""+(char)0x0D+(char)0x0A+"		}";
			return out;
		} catch(java.lang.NullPointerException e) {
			Window.window.println("Side with bad data! Not exported!");
			return null;
		}
	}
	
	// toRoundString()
	// Same as toString but rounds numbers within .01 of a whole number to that whole number
	public String toRoundString() {
		try {
			return "( "+fmt.format((double)Math.round(triangle[0].getX()*1000000.0)/1000000.0)+" "+fmt.format((double)Math.round(triangle[0].getY()*1000000.0)/1000000.0)+" "+fmt.format((double)Math.round(triangle[0].getZ()*1000000.0)/1000000.0)+" ) "+
			       "( "+fmt.format((double)Math.round(triangle[1].getX()*1000000.0)/1000000.0)+" "+fmt.format((double)Math.round(triangle[1].getY()*1000000.0)/1000000.0)+" "+fmt.format((double)Math.round(triangle[1].getZ()*1000000.0)/1000000.0)+" ) "+
			       "( "+fmt.format((double)Math.round(triangle[2].getX()*1000000.0)/1000000.0)+" "+fmt.format((double)Math.round(triangle[2].getY()*1000000.0)/1000000.0)+" "+fmt.format((double)Math.round(triangle[2].getZ()*1000000.0)/1000000.0)+" ) "+
			       texture + 
			       " [ "+fmt.format((double)Math.round(textureS.getX()*1000000.0)/1000000.0)+" "+fmt.format((double)Math.round(textureS.getY()*1000000.0)/1000000.0)+" "+fmt.format((double)Math.round(textureS.getZ()*1000000.0)/1000000.0)+" "+Math.round(textureShiftS)+" ]"+
			       " [ "+fmt.format((double)Math.round(textureT.getX()*1000000.0)/1000000.0)+" "+fmt.format((double)Math.round(textureT.getY()*1000000.0)/1000000.0)+" "+fmt.format((double)Math.round(textureT.getZ()*1000000.0)/1000000.0)+" "+Math.round(textureShiftT)+" ] "+
			       fmtScales.format((double)Math.round(texRot*10000.0)/10000.0)+" "+fmtScales.format((double)Math.round(texScaleX*10000.0)/10000.0)+" "+fmtScales.format((double)Math.round(texScaleY*10000.0)/10000.0)+" "+flags+" "+
			       material +
			       " [ "+fmtScales.format((double)Math.round(lgtScale*1000000.0)/1000000.0)+" "+fmtScales.format((double)Math.round(lgtRot*1000000.0)/1000000.0)+" ]";
		} catch(java.lang.NullPointerException e) {
			Window.window.println("Side with bad data! Not exported!");
			return null;
		}
	}
	
	// toRoundVMFSide()
	// Same as toVMFSide but rounds numbers within .01 of a whole number to that whole number
	public String toRoundVMFSide() {
		try {
			String out="		side"+(char)0x0D+(char)0x0A+"		{"+(char)0x0D+(char)0x0A;
			out+="			\"id\" \""+id+"\""+(char)0x0D+(char)0x0A;
			out+="			\"plane\" \"("+fmt.format((double)Math.round(triangle[0].getX()*1000000.0)/1000000.0)+" "+fmt.format((double)Math.round(triangle[0].getY()*1000000.0)/1000000.0)+" "+fmt.format((double)Math.round(triangle[0].getZ()*1000000.0)/1000000.0)+") ";
			out+="("+fmt.format((double)Math.round(triangle[1].getX()*1000000.0)/1000000.0)+" "+fmt.format((double)Math.round(triangle[1].getY()*1000000.0)/1000000.0)+" "+fmt.format((double)Math.round(triangle[1].getZ()*1000000.0)/1000000.0)+") ";
			out+="("+fmt.format((double)Math.round(triangle[2].getX()*1000000.0)/1000000.0)+" "+fmt.format((double)Math.round(triangle[2].getY()*1000000.0)/1000000.0)+" "+fmt.format((double)Math.round(triangle[2].getZ()*1000000.0)/1000000.0)+")\""+(char)0x0D+(char)0x0A;
			out+="			\"material\" \"" + texture + "\""+(char)0x0D+(char)0x0A;
			out+="			\"uaxis\" \"["+fmt.format((double)Math.round(textureS.getX()*1000000.0)/1000000.0)+" "+fmt.format((double)Math.round(textureS.getY()*1000000.0)/1000000.0)+" "+fmt.format((double)Math.round(textureS.getZ()*1000000.0)/1000000.0)+" "+Math.round(textureShiftS)+"] "+fmtScales.format((double)Math.round(texScaleX*10000.0)/10000.0)+"\""+(char)0x0D+(char)0x0A;
			out+="			\"vaxis\" \"["+fmt.format((double)Math.round(textureT.getX()*1000000.0)/1000000.0)+" "+fmt.format((double)Math.round(textureT.getY()*1000000.0)/1000000.0)+" "+fmt.format((double)Math.round(textureT.getZ()*1000000.0)/1000000.0)+" "+Math.round(textureShiftT)+"] "+fmtScales.format((double)Math.round(texScaleY*10000.0)/10000.0)+"\""+(char)0x0D+(char)0x0A;
			out+="			\"rotation\" \""+fmtScales.format((double)Math.round(texRot*10000.0)/10000.0)+"\""+(char)0x0D+(char)0x0A;
			out+="			\"lightmapscale\" \""+fmtScales.format((double)Math.round(lgtScale*10000.0)/10000.0)+"\""+(char)0x0D+(char)0x0A;
			out+="			\"smoothing_groups\" \"0\""+(char)0x0D+(char)0x0A+"		}";
			return out;
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
	}
	
	// ACCESSORS/MUTATORS
	public Vector3D[] getTriangle() {
		return triangle;
	}
	
	public String getTexture() {
		return texture;
	}
	
	public void setTriangle(Vector3D[] in) {
		triangle[0]=in[0];
		triangle[1]=in[1];
		triangle[2]=in[2];
	}
	
	public Plane getPlane() {
		return plane;
	}
}
