// v42BSP class
// This class gathers all relevant information from the lumps of a BSP version 42.

public class v42BSP {

	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	// This is the version of the BSP. This will determine the lumps order and aid in
	// decompilation.
	public static final int VERSION=42;
	
	private String filepath;
	
	// Each lump has its own class for handling its specific data structures.
	// These are the only lumps we need for decompilation.
	private Entities entities;
	private Planes planes;
	private Textures textures;
	private Textures materials;
	private Vertices vertices;
	private Faces faces;
	private Leaves leaves;
	private IntList markbrushes;
	private Models models;
	private Brushes brushes;
	private BrushSides brushSides;
	private TexInfos texInfo;
	
	// CONSTRUCTORS
	// This accepts a folder path and looks for the BSP there.
	public v42BSP(String in) {
		filepath=in;
	}

	public void printBSPReport() {
		try {
			Window.println("Entities lump: "+entities.getLength()+" bytes, "+entities.length()+" items",Window.VERBOSITY_MAPSTATS);
		} catch(java.lang.NullPointerException e) {
			Window.println("Entities not yet parsed!",Window.VERBOSITY_MAPSTATS);
		}
		try {
			Window.println("Planes lump: "+planes.getLength()+" bytes, "+planes.length()+" items",Window.VERBOSITY_MAPSTATS);
		} catch(java.lang.NullPointerException e) {
			Window.println("Planes not yet parsed!",Window.VERBOSITY_MAPSTATS);
		}
		try {
			Window.println("Textures lump: "+textures.getLength()+" bytes, "+textures.length()+" items",Window.VERBOSITY_MAPSTATS);
		} catch(java.lang.NullPointerException e) {
			Window.println("Textures not yet parsed!",Window.VERBOSITY_MAPSTATS);
		}
		try {
			Window.println("Materials lump: "+materials.getLength()+" bytes, "+materials.length()+" items",Window.VERBOSITY_MAPSTATS);
		} catch(java.lang.NullPointerException e) {
			Window.println("Materials not yet parsed!",Window.VERBOSITY_MAPSTATS);
		}
		try {
			Window.println("Vertices lump: "+vertices.getLength()+" bytes, "+vertices.length()+" items",Window.VERBOSITY_MAPSTATS);
		} catch(java.lang.NullPointerException e) {
			Window.println("Vertices not yet parsed!",Window.VERBOSITY_MAPSTATS);
		}
		try {
			Window.println("Faces lump: "+faces.getLength()+" bytes, "+faces.length()+" items",Window.VERBOSITY_MAPSTATS);
		} catch(java.lang.NullPointerException e) {
			Window.println("Faces not yet parsed!",Window.VERBOSITY_MAPSTATS);
		}
		try {
			Window.println("Leaves lump: "+leaves.getLength()+" bytes, "+leaves.length()+" items",Window.VERBOSITY_MAPSTATS);
		} catch(java.lang.NullPointerException e) {
			Window.println("Leaves not yet parsed!",Window.VERBOSITY_MAPSTATS);
		}
		try {
			Window.println("Leaf brushes lump: "+markbrushes.getLength()+" bytes, "+markbrushes.getNumElements()+" items",Window.VERBOSITY_MAPSTATS);
		} catch(java.lang.NullPointerException e) {
			Window.println("Leaf brushes not yet parsed!",Window.VERBOSITY_MAPSTATS);
		}
		try {
			Window.println("Models lump: "+models.getLength()+" bytes, "+models.length()+" items",Window.VERBOSITY_MAPSTATS);
		} catch(java.lang.NullPointerException e) {
			Window.println("Models not yet parsed!",Window.VERBOSITY_MAPSTATS);
		}
		try {
			Window.println("Brushes lump: "+brushes.getLength()+" bytes, "+brushes.length()+" items",Window.VERBOSITY_MAPSTATS);
		} catch(java.lang.NullPointerException e) {
			Window.println("Brushes not yet parsed!",Window.VERBOSITY_MAPSTATS);
		}
		try {
			Window.println("Brush sides lump: "+brushSides.getLength()+" bytes, "+brushSides.length()+" items",Window.VERBOSITY_MAPSTATS);
		} catch(java.lang.NullPointerException e) {
			Window.println("Brush sides not yet parsed!",Window.VERBOSITY_MAPSTATS);
		}
		try {
			Window.println("Texture scales lump: "+texInfo.getLength()+" bytes, "+texInfo.length()+" items",Window.VERBOSITY_MAPSTATS);
		} catch(java.lang.NullPointerException e) {
			Window.println("Texture scales not yet parsed!",Window.VERBOSITY_MAPSTATS);
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
		planes=new Planes(data, BSP.TYPE_NIGHTFIRE);
	}
	
	public Planes getPlanes() {
		return planes;
	}
	
	public void setTextures(byte[] data) {
		textures=new Textures(data, BSP.TYPE_NIGHTFIRE);
	}
	
	public Textures getTextures() {
		return textures;
	}
	
	public void setMaterials(byte[] data) {
		materials=new Textures(data, BSP.TYPE_NIGHTFIRE);
	}
	
	public Textures getMaterials() {
		return materials;
	}
	
	public void setVertices(byte[] data) {
		vertices=new Vertices(data, BSP.TYPE_NIGHTFIRE);
	}
	
	public Vertices getVertices() {
		return vertices;
	}
	
	public void setFaces(byte[] data) {
		faces=new Faces(data, Face.TYPE_NIGHTFIRE);
	}
	
	public Faces getFaces() {
		return faces;
	}
	
	public void setLeaves(byte[] data) {
		leaves=new Leaves(data, Leaf.TYPE_NIGHTFIRE);
	}
	
	public Leaves getLeaves() {
		return leaves;
	}
	
	public void setMarkBrushes(byte[] data) {
		markbrushes=new IntList(data);
	}
	
	public IntList getMarkBrushes() {
		return markbrushes;
	}
	
	public void setModels(byte[] data) {
		models=new Models(data, Model.TYPE_NIGHTFIRE);
	}
	
	public Models getModels() {
		return models;
	}
	
	public void setBrushes(byte[] data) {
		brushes=new Brushes(data, BSP.TYPE_NIGHTFIRE);
	}
	
	public Brushes getBrushes() {
		return brushes;
	}
	
	public void setBrushSides(byte[] data) {
		brushSides=new BrushSides(data, BrushSide.TYPE_NIGHTFIRE);
	}
	
	public BrushSides getBrushSides() {
		return brushSides;
	}
	
	public void setTexInfo(byte[] data) {
		texInfo=new TexInfos(data, BSP.TYPE_NIGHTFIRE);
	}
	
	public TexInfos getTexInfo() {
		return texInfo;
	}
}
