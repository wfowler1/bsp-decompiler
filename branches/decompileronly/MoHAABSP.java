// MoHAABSP class
// Oh, you know what these are by now.

public class MoHAABSP extends v46BSP {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	// This is the version of the BSP. This will determine the lumps order and aid in
	// decompilation.
	// Since MoHAA use a different versioning scheme, I can't use the typical range of numbers.
	// But, it only matters in the context of my program anyway, so make it arbitrary
	public static final int VERSION=275;
	
	// Each lump has its own class for handling its specific data structures.
	// These are the only lumps we need for decompilation.
	MoHAATextures mtextures;
	v42TextureMatrices textureScales; // Yes, MoHAA actually uses the same matrix format as Nightfire.
	MoHAABrushSides mBrushSides;
	MoHAAStaticProps staticProps;
	
	// CONSTRUCTORS
	// This accepts a folder path and looks for the BSP there.
	public MoHAABSP(String in) {
		super(in);
	}

	public void printBSPReport() {
		super.printBSPReport();
		try {
			Window.println("Textures lump: "+mtextures.getLength()+" bytes, "+mtextures.length()+" items",Window.VERBOSITY_MAPSTATS);
		} catch(java.lang.NullPointerException e) {
			Window.println("Textures not yet parsed!",Window.VERBOSITY_MAPSTATS);
		}
		try {
			Window.println("Texture scale lump: "+textureScales.getLength()+" bytes, "+textureScales.getNumElements()+" items",Window.VERBOSITY_MAPSTATS);
		} catch(java.lang.NullPointerException e) {
			Window.println("Texture scales not yet parsed!",Window.VERBOSITY_MAPSTATS);
		}
		try {
			Window.println("Brush sides lump: "+mBrushSides.getLength()+" bytes, "+mBrushSides.length()+" items",Window.VERBOSITY_MAPSTATS);
		} catch(java.lang.NullPointerException e) {
			Window.println("Brush sides not yet parsed!",Window.VERBOSITY_MAPSTATS);
		}
		try {
			Window.println("Static prop lump: "+staticProps.getLength()+" bytes, "+staticProps.length()+" items",Window.VERBOSITY_MAPSTATS);
		} catch(java.lang.NullPointerException e) {
			Window.println("Static props not yet parsed!",Window.VERBOSITY_MAPSTATS);
		}
	}
	
	public void setMTextures(byte[] in) {
		mtextures=new MoHAATextures(in);
	}
	
	public MoHAATextures getMTextures() {
		return mtextures;
	}
	
	public void setTexScales(byte[] in) {
		textureScales=new v42TextureMatrices(in);
	}
	
	public v42TextureMatrices getTexScales() {
		return textureScales;
	}
	
	public void setMBrushSides(byte[] in) {
		mBrushSides=new MoHAABrushSides(in);
	}
	
	public MoHAABrushSides getMBrushSides() {
		return mBrushSides;
	}
	
	public void setStaticProps(byte[] in) {
		staticProps=new MoHAAStaticProps(in);
	}
	
	public MoHAAStaticProps getStaticProps() {
		return staticProps;
	}
}