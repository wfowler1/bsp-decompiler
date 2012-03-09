// v46BSP class
// This class gathers all relevant information from the lumps of a BSP version 46.

public class v46BSP {

	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	// This is the version of the BSP. This will determine the lumps order and aid in
	// decompilation.
	public static final int version=46;
	
	private String filepath;
	
	// Each lump has its own class for handling its specific data structures.
	// These are the only lumps we need for decompilation.
	private Entities entities;
	private PlaneList planes;
	private v46Textures textures;
	private v46Models models;
	private Brushes brushes;
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
			Window.window.println("Entities lump: "+entities.getLength()+" bytes, "+entities.getNumElements()+" items");
		} catch(java.lang.NullPointerException e) {
			Window.window.println("Entities not yet parsed!");
		}
		try {
			Window.window.println("Textures lump: "+textures.getLength()+" bytes, "+textures.getNumElements()+" items");
		} catch(java.lang.NullPointerException e) {
			Window.window.println("Textures not yet parsed!");
		}
		try {
			Window.window.println("Planes lump: "+planes.getLength()+" bytes, "+planes.getNumElements()+" items");
		} catch(java.lang.NullPointerException e) {
			Window.window.println("Planes not yet parsed!");
		}
		try {
			Window.window.println("Models lump: "+models.getLength()+" bytes, "+models.getNumElements()+" items");
		} catch(java.lang.NullPointerException e) {
			Window.window.println("Models not yet parsed!");
		}
		try {
			Window.window.println("Brushes lump: "+brushes.getLength()+" bytes, "+brushes.getNumElements()+" items");
		} catch(java.lang.NullPointerException e) {
			Window.window.println("Brushes not yet parsed!");
		}
		try {
			Window.window.println("Brush sides lump: "+brushSides.getLength()+" bytes, "+brushSides.getNumElements()+" items");
		} catch(java.lang.NullPointerException e) {
			Window.window.println("Brush sides not yet parsed!");
		}
		try {
			Window.window.println("Vertices lump: "+vertices.getLength()+" bytes, "+vertices.getNumElements()+" items");
		} catch(java.lang.NullPointerException e) {
			Window.window.println("Vertices not yet parsed!");
		}
		try {
			Window.window.println("Faces lump: "+faces.getLength()+" bytes, "+faces.getNumElements()+" items");
		} catch(java.lang.NullPointerException e) {
			Window.window.println("Faces not yet parsed!");
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
		textures=new v46Textures(data);
	}
	
	public v46Textures getTextures() {
		return textures;
	}
	
	public void setModels(byte[] data) {
		models=new v46Models(data);
	}
	
	public v46Models getModels() {
		return models;
	}
	
	public void setBrushes(byte[] data) {
		brushes=new Brushes(data);
	}
	
	public Brushes getBrushes() {
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
