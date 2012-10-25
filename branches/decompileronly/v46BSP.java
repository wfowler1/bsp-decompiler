// v46BSP class
// This class gathers all relevant information from the lumps of a BSP version 46.

public class v46BSP {

	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	// This is the version of the BSP. This will determine the lumps order and aid in
	// decompilation.
	public static final int VERSION=46;
	
	private String filepath;
	
	// Each lump has its own class for handling its specific data structures.
	// These are the only lumps we need for decompilation.
	private Entities entities;
	private PlaneList planes;
	private Textures textures;
	private Models models;
	private v46Brushes brushes;
	private v46BrushSides brushSides;
	private v46Vertices vertices; // Probably the only BSP version to use a different vertex format
	private v46Faces faces;
	
	// CONSTRUCTORS
	// This accepts a folder path and looks for the BSP there.
	public v46BSP(String in) {
		filepath=in;
	}

	public void printBSPReport() {
		try {
			Window.println("Entities lump: "+entities.getLength()+" bytes, "+entities.length()+" items",Window.VERBOSITY_MAPSTATS);
		} catch(java.lang.NullPointerException e) {
			Window.println("Entities not yet parsed!",Window.VERBOSITY_MAPSTATS);
		}
		try {
			Window.println("Textures lump: "+textures.getLength()+" bytes, "+textures.length()+" items",Window.VERBOSITY_MAPSTATS);
		} catch(java.lang.NullPointerException e) {
			Window.println("Textures not yet parsed!",Window.VERBOSITY_MAPSTATS);
		}
		try {
			Window.println("Planes lump: "+planes.getLength()+" bytes, "+planes.getNumElements()+" items",Window.VERBOSITY_MAPSTATS);
		} catch(java.lang.NullPointerException e) {
			Window.println("Planes not yet parsed!",Window.VERBOSITY_MAPSTATS);
		}
		try {
			Window.println("Models lump: "+models.getLength()+" bytes, "+models.length()+" items",Window.VERBOSITY_MAPSTATS);
		} catch(java.lang.NullPointerException e) {
			Window.println("Models not yet parsed!",Window.VERBOSITY_MAPSTATS);
		}
		try {
			Window.println("Brushes lump: "+brushes.getLength()+" bytes, "+brushes.getNumElements()+" items",Window.VERBOSITY_MAPSTATS);
		} catch(java.lang.NullPointerException e) {
			Window.println("Brushes not yet parsed!",Window.VERBOSITY_MAPSTATS);
		}
		try {
			Window.println("Brush sides lump: "+brushSides.getLength()+" bytes, "+brushSides.length()+" items",Window.VERBOSITY_MAPSTATS);
		} catch(java.lang.NullPointerException e) {
			Window.println("Brush sides not yet parsed!",Window.VERBOSITY_MAPSTATS);
		}
		try {
			Window.println("Vertices lump: "+vertices.getLength()+" bytes, "+vertices.getNumElements()+" items",Window.VERBOSITY_MAPSTATS);
		} catch(java.lang.NullPointerException e) {
			Window.println("Vertices not yet parsed!",Window.VERBOSITY_MAPSTATS);
		}
		try {
			Window.println("Faces lump: "+faces.getLength()+" bytes, "+faces.getNumElements()+" items",Window.VERBOSITY_MAPSTATS);
		} catch(java.lang.NullPointerException e) {
			Window.println("Faces not yet parsed!",Window.VERBOSITY_MAPSTATS);
		}
	}
	
	// ACCESSORS/MUTATORS
	
	public String getPath() {
		return filepath;
	}
	
	public String getMapName() {
		int i;
		for(i=0;i<filepath.length();i++) {
			if(filepath.charAt(filepath.length()-1-i)=='\\') {
				break;
			}
			if(filepath.charAt(filepath.length()-1-i)=='/') {
				break;
			}
		}
		return filepath.substring(filepath.length()-i,filepath.length());
	}
	
	public String getMapNameNoExtension() {
		String name=getMapName();
		int i;
		for(i=0;i<name.length();i++) {
			if(name.charAt(name.length()-1-i)=='.') {
				break;
			}
		}
		return name.substring(0,name.length()-1-i);
	}
	
	public String getFolder() {
		int i;
		for(i=0;i<filepath.length();i++) {
			if(filepath.charAt(filepath.length()-1-i)=='\\') {
				break;
			}
			if(filepath.charAt(filepath.length()-1-i)=='/') {
				break;
			}
		}
		return filepath.substring(0,filepath.length()-i);
	}
	
	public void setEntities(byte[] data) {
		entities=new Entities(data);
	}
	
	public Entities getEntities() {
		return entities;
	}
	
	public void setPlanes(byte[] data) {
		planes=new PlaneList(data);
	}
	
	public PlaneList getPlanes() {
		return planes;
	}
	
	public void setTextures(byte[] data) {
		textures=new Textures(data, Texture.TYPE_QUAKE3);
	}
	
	public Textures getTextures() {
		return textures;
	}
	
	public void setModels(byte[] data) {
		models=new Models(data, Model.TYPE_QUAKE3);
	}
	
	public Models getModels() {
		return models;
	}
	
	public void setBrushes(byte[] data) {
		brushes=new v46Brushes(data);
	}
	
	public v46Brushes getBrushes() {
		return brushes;
	}
	
	public void setBrushSides(byte[] data) {
		brushSides=new v46BrushSides(data);
	}
	
	public v46BrushSides getBrushSides() {
		return brushSides;
	}
	
	public void setVertices(byte[] data) {
		vertices=new v46Vertices(data);
	}
	
	public v46Vertices getVertices() {
		return vertices;
	}
	
	public void setFaces(byte[] data) {
		faces=new v46Faces(data);
	}
	
	public v46Faces getFaces() {
		return faces;
	}
}
