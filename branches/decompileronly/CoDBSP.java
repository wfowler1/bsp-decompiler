// CoDBSP class
// This class gathers all relevant information from the lumps of a BSP from CoD.

public class CoDBSP extends v46BSP {

	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	// This is the version of the BSP. This will determine the lumps order and aid in
	// decompilation.
	public static final int VERSION=59;
	
	// Each lump has its own class for handling its specific data structures.
	// These are the only lumps we need for decompilation.
	private CoDModels cmodels;
	private CoDBrushes cbrushes;
	private CoDBrushSides cbrushSides;
	
	// CONSTRUCTORS
	// This accepts a folder path and looks for the BSP there.
	public CoDBSP(String in) {
		super(in);
	}

	public void printBSPReport() {
		super.printBSPReport();
		try {
			Window.println("Models lump: "+cmodels.getLength()+" bytes, "+cmodels.length()+" items",Window.VERBOSITY_MAPSTATS);
		} catch(java.lang.NullPointerException e) {
			Window.println("Models not yet parsed!",Window.VERBOSITY_MAPSTATS);
		}
		try {
			Window.println("Brushes lump: "+cbrushes.getLength()+" bytes, "+cbrushes.length()+" items",Window.VERBOSITY_MAPSTATS);
		} catch(java.lang.NullPointerException e) {
			Window.println("Brushes not yet parsed!",Window.VERBOSITY_MAPSTATS);
		}
		try {
			Window.println("Brush sides lump: "+cbrushSides.getLength()+" bytes, "+cbrushSides.length()+" items",Window.VERBOSITY_MAPSTATS);
		} catch(java.lang.NullPointerException e) {
			Window.println("Brush sides not yet parsed!",Window.VERBOSITY_MAPSTATS);
		}
	}
	
	// ACCESSORS/MUTATORS
	
	public void setCModels(byte[] data) {
		cmodels=new CoDModels(data);
	}
	
	public CoDModels getCModels() {
		return cmodels;
	}
	
	public void setCBrushes(byte[] data) {
		cbrushes=new CoDBrushes(data);
	}
	
	public CoDBrushes getCBrushes() {
		return cbrushes;
	}
	
	public void setCBrushSides(byte[] data) {
		cbrushSides=new CoDBrushSides(data);
	}
	
	public CoDBrushSides getCBrushSides() {
		return cbrushSides;
	}
}