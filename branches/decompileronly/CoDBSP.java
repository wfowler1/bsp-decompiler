// CoDBSP class
// This class gathers all relevant information from the lumps of a BSP from CoD.

public class CoDBSP extends v46BSP {

	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	// This is the version of the BSP. This will determine the lumps order and aid in
	// decompilation.
	public int version=59;
	
	// Each lump has its own class for handling its specific data structures.
	// These are the only lumps we need for decompilation.
	private Models models;
	private CoDBrushes cbrushes;
	private CoDBrushSides cbrushSides;
	
	// CONSTRUCTORS
	// This accepts a folder path and looks for the BSP there.
	public CoDBSP(String in) {
		super(in);
	}
	
	public CoDBSP(String in, int version) {
		super(in);
		this.version=version;
	}

	public void printBSPReport() {
		super.printBSPReport();
		try {
			Window.println("Models lump: "+models.getLength()+" bytes, "+models.length()+" items",Window.VERBOSITY_MAPSTATS);
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
	
	public void setModels(byte[] data) {
		models=new Models(data, Model.TYPE_COD);
	}
	
	public Models getModels() {
		return models;
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
