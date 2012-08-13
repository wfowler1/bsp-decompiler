// v42BSP class
// This class gathers all relevant information from the lumps of a BSP version 42.

public class v42BSP {

	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	// This is the version of the BSP. This will determine the lumps order and aid in
	// decompilation.
	public static final int version=42;
	
	private String filepath;
	
	// Each lump has its own class for handling its specific data structures.
	// These are the only lumps we need for decompilation.
	private Entities entities;
	private BSPPlanes planes;
	private v42Strings64 textures;
	private v42Strings64 materials;
	private Vertices vertices;
	private v42Faces faces;
	private v42Leaves leaves;
	private IntList markbrushes;
	private v42Models models;
	private v42Brushes brushes;
	private v42BrushSides brushSides;
	private v42TextureMatrices textureMatrices;
	
	// CONSTRUCTORS
	// This accepts a folder path and looks for the BSP there.
	public v42BSP(String in) {
		filepath=in;
	}

	public void printBSPReport() {
		try {
			Window.println("Entities lump: "+entities.getLength()+" bytes, "+entities.getNumElements()+" items",1);
		} catch(java.lang.NullPointerException e) {
			Window.println("Entities not yet parsed!",1);
		}
		try {
			Window.println("Planes lump: "+planes.getLength()+" bytes, "+planes.getNumElements()+" items",1);
		} catch(java.lang.NullPointerException e) {
			Window.println("Planes not yet parsed!",1);
		}
		try {
			Window.println("Textures lump: "+textures.getLength()+" bytes, "+textures.getNumElements()+" items",1);
		} catch(java.lang.NullPointerException e) {
			Window.println("Textures not yet parsed!",1);
		}
		try {
			Window.println("Materials lump: "+materials.getLength()+" bytes, "+materials.getNumElements()+" items",1);
		} catch(java.lang.NullPointerException e) {
			Window.println("Materials not yet parsed!",1);
		}
		try {
			Window.println("Vertices lump: "+vertices.getLength()+" bytes, "+vertices.getNumElements()+" items",1);
		} catch(java.lang.NullPointerException e) {
			Window.println("Vertices not yet parsed!",1);
		}
		try {
			Window.println("Faces lump: "+faces.getLength()+" bytes, "+faces.getNumElements()+" items",1);
		} catch(java.lang.NullPointerException e) {
			Window.println("Faces not yet parsed!",1);
		}
		try {
			Window.println("Leaves lump: "+leaves.getLength()+" bytes, "+leaves.getNumElements()+" items",1);
		} catch(java.lang.NullPointerException e) {
			Window.println("Leaves not yet parsed!",1);
		}
		try {
			Window.println("Leaf brushes lump: "+markbrushes.getLength()+" bytes, "+markbrushes.getNumElements()+" items",1);
		} catch(java.lang.NullPointerException e) {
			Window.println("Leaf brushes not yet parsed!",1);
		}
		try {
			Window.println("Models lump: "+models.getLength()+" bytes, "+models.getNumElements()+" items",1);
		} catch(java.lang.NullPointerException e) {
			Window.println("Models not yet parsed!",1);
		}
		try {
			Window.println("Brushes lump: "+brushes.getLength()+" bytes, "+brushes.getNumElements()+" items",1);
		} catch(java.lang.NullPointerException e) {
			Window.println("Brushes not yet parsed!",1);
		}
		try {
			Window.println("Brush sides lump: "+brushSides.getLength()+" bytes, "+brushSides.getNumElements()+" items",1);
		} catch(java.lang.NullPointerException e) {
			Window.println("Brush sides not yet parsed!",1);
		}
		try {
			Window.println("Texture scales lump: "+textureMatrices.getLength()+" bytes, "+textureMatrices.getNumElements()+" items",1);
		} catch(java.lang.NullPointerException e) {
			Window.println("Texture scales not yet parsed!",1);
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
		planes=new BSPPlanes(data);
	}
	
	public BSPPlanes getPlanes() {
		return planes;
	}
	
	public void setTextures(byte[] data) {
		textures=new v42Strings64(data);
	}
	
	public v42Strings64 getTextures() {
		return textures;
	}
	
	public void setMaterials(byte[] data) {
		materials=new v42Strings64(data);
	}
	
	public v42Strings64 getMaterials() {
		return materials;
	}
	
	public void setVertices(byte[] data) {
		vertices=new Vertices(data);
	}
	
	public Vertices getVertices() {
		return vertices;
	}
	
	public void setFaces(byte[] data) {
		faces=new v42Faces(data);
	}
	
	public v42Faces getFaces() {
		return faces;
	}
	
	public void setLeaves(byte[] data) {
		leaves=new v42Leaves(data);
	}
	
	public v42Leaves getLeaves() {
		return leaves;
	}
	
	public void setMarkBrushes(byte[] data) {
		markbrushes=new IntList(data);
	}
	
	public IntList getMarkBrushes() {
		return markbrushes;
	}
	
	public void setModels(byte[] data) {
		models=new v42Models(data);
	}
	
	public v42Models getModels() {
		return models;
	}
	
	public void setBrushes(byte[] data) {
		brushes=new v42Brushes(data);
	}
	
	public v42Brushes getBrushes() {
		return brushes;
	}
	
	public void setBrushSides(byte[] data) {
		brushSides=new v42BrushSides(data);
	}
	
	public v42BrushSides getBrushSides() {
		return brushSides;
	}
	
	public void setTextureMatrices(byte[] data) {
		textureMatrices=new v42TextureMatrices(data);
	}
	
	public v42TextureMatrices getTextureMatrices() {
		return textureMatrices;
	}
}
