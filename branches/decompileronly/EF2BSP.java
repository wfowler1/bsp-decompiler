// EF2BSP class
// BSP class for Star Trek Elite Force 2 Demo
// It's likely that the EF2! format is similar to this.

public class EF2BSP extends v46BSP {

	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	// This is the version of the BSP. This will determine the lumps order and aid in
	// decompilation.
	public static final int VERSION=1178684254; // There's too many BSP types and too many different versioning schemes.
	
	// Each lump has its own class for handling its specific data structures.
	// These are the only lumps we need for decompilation.
	// Many lumps are inherited from v46BSP
	private Textures textures;
	private boolean isDemo=false;
	
	// CONSTRUCTORS
	public EF2BSP(String path, boolean isDemo) {
		super(path);
		this.isDemo=isDemo;
	}
	
	// METHODS
	public void printBSPReport() {
		super.printBSPReport();
		try {
			Window.println("Textures lump: "+textures.getLength()+" bytes, "+textures.length()+" items",Window.VERBOSITY_MAPSTATS);
		} catch(java.lang.NullPointerException e) {
			Window.println("Textures not yet parsed!",Window.VERBOSITY_MAPSTATS);
		}
	}
	
	// ACCESSORS/MUTATORS
	public void setTextures(byte[] data) {
		this.textures=new Textures(data, BSP.TYPE_STEF2);
	}
	
	public Textures getTextures() {
		return this.textures;
	}
	
	public boolean isDemo() {
		return isDemo;
	}
}
