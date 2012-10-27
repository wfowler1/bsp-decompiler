// SiNBSP class

public class SiNBSP extends v38BSP {

	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	// This is the version of the BSP. This will determine the lumps order and aid in
	// decompilation.
	public static final int VERSION=1347633747; // "RBSP" as int32 + 1
	
	// Each lump has its own class for handling its specific data structures.
	// These are the only lumps we need for decompilation.
	// Many lumps are inherited from v38BSP
	private BrushSides brushSides;
	private Textures textures;
	private SiNFaces sfaces;
	
	// CONSTRUCTORS
	public SiNBSP(String path) {
		super(path);
	}
	
	// METHODS
	
	public void printBSPReport() {
		super.printBSPReport();
		try {
			Window.println("Brush sides lump: "+brushSides.getLength()+" bytes, "+brushSides.length()+" items",Window.VERBOSITY_MAPSTATS);
		} catch(java.lang.NullPointerException e) {
			Window.println("Brush sides not yet parsed!",Window.VERBOSITY_MAPSTATS);
		}
		try {
			Window.println("Textures lump: "+textures.getLength()+" bytes, "+textures.length()+" items",Window.VERBOSITY_MAPSTATS);
		} catch(java.lang.NullPointerException e) {
			Window.println("Textures not yet parsed!",Window.VERBOSITY_MAPSTATS);
		}
		try {
			Window.println("Faces lump: "+sfaces.getLength()+" bytes, "+sfaces.length()+" items",Window.VERBOSITY_MAPSTATS);
		} catch(java.lang.NullPointerException e) {
			Window.println("Faces not yet parsed!",Window.VERBOSITY_MAPSTATS);
		}
	}
	
	// ACCESSORS/MUTATORS
	public void setBrushSides(byte[] data) {
		this.brushSides=new BrushSides(data, BrushSide.TYPE_SIN);
	}
	
	public BrushSides getBrushSides() {
		return this.brushSides;
	}
	
	public void setTextures(byte[] data) {
		this.textures=new Textures(data, Texture.TYPE_SIN);
	}
	
	public Textures getTextures() {
		return this.textures;
	}
	
	public void setSFaces(byte[] data) {
		this.sfaces=new SiNFaces(data);
	}
	
	public SiNFaces getSFaces() {
		return this.sfaces;
	}
}