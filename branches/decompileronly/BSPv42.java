// BSPv42 class
// This class gathers all relevant information from the lumps of a BSP version 42.

import java.io.File;

public class BSPv42 {

	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	private static Runtime r = Runtime.getRuntime(); // Get a runtime object. This is for calling
	                                                 // Java's garbage collector and does not need
	                                                 // to be ported. I try not to leave memory leaks
	                                                 // but since Java has no way explicitly reallocate
	                                                 // unused memory I have to tell it when a good
	                                                 // time is to run the garbage collector, by
	                                                 // calling gc(). Also, it is used to execute EXEs
	                                                 // from within the program by calling .exec(path).
	
	// This is the version of the BSP. This will determine the lumps order and aid in
	// decompilation.
	public static final int version=42;
	
	private String filepath;
	
	// Each lump has its own class for handling its specific data structures.
	// These are the only lumps we need for decompilation.
	private Entities entities;
	private Planes planes;
	private Texture64 textures;
	private Materials64 materials;
	private Vertices vertices;
	private Faces faces;
	private Leaves leaves;
	private IntList markbrushes;
	private Models models;
	private Brushes brushes;
	private BrushSides brushSides;
	private TextureMatrices textureMatrices;
	
	// CONSTRUCTORS
	// This accepts a folder path and looks for the lump files there. If the folder is empty (or not found)
	// the program fails nicely.
	public BSPv42(String in) {
		filepath=in;
	}

	public void printBSPReport() {
		try {
			Window.window.println("Entities lump: "+entities.getLength()+" bytes, "+entities.getNumElements()+" items");
		} catch(java.lang.NullPointerException e) {
			Window.window.println("Entities not yet parsed!");
		}
		try {
			Window.window.println("Planes lump: "+planes.getLength()+" bytes, "+planes.getNumElements()+" items");
		} catch(java.lang.NullPointerException e) {
			Window.window.println("Planes not yet parsed!");
		}
		try {
			Window.window.println("Textures lump: "+textures.getLength()+" bytes, "+textures.getNumElements()+" items");
		} catch(java.lang.NullPointerException e) {
			Window.window.println("Textures not yet parsed!");
		}
		try {
			Window.window.println("Materials lump: "+materials.getLength()+" bytes, "+materials.getNumElements()+" items");
		} catch(java.lang.NullPointerException e) {
			Window.window.println("Materials not yet parsed!");
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
		try {
			Window.window.println("Leaves lump: "+leaves.getLength()+" bytes, "+leaves.getNumElements()+" items");
		} catch(java.lang.NullPointerException e) {
			Window.window.println("Leaves not yet parsed!");
		}
		try {
			Window.window.println("Leaf brushes lump: "+markbrushes.getLength()+" bytes, "+markbrushes.getNumElements()+" items");
		} catch(java.lang.NullPointerException e) {
			Window.window.println("Leaf brushes not yet parsed!");
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
			Window.window.println("Texture scales lump: "+textureMatrices.getLength()+" bytes, "+textureMatrices.getNumElements()+" items");
		} catch(java.lang.NullPointerException e) {
			Window.window.println("Texture scales not yet parsed!");
		}
	}
	
	// ACCESSORS/MUTATORS
	
	public String getPath() {
		return filepath;
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
		planes=new Planes(data);
	}
	
	public Planes getPlanes() {
		return planes;
	}
	
	public void setTextures(byte[] data) {
		textures=new Texture64(data);
	}
	
	public Texture64 getTextures() {
		return textures;
	}
	
	public void setMaterials(byte[] data) {
		materials=new Materials64(data);
	}
	
	public Materials64 getMaterials() {
		return materials;
	}
	
	public void setVertices(byte[] data) {
		vertices=new Vertices(data);
	}
	
	public Vertices getVertices() {
		return vertices;
	}
	
	public void setFaces(byte[] data) {
		faces=new Faces(data);
	}
	
	public Faces getFaces() {
		return faces;
	}
	
	public void setLeaves(byte[] data) {
		leaves=new Leaves(data);
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
		models=new Models(data);
	}
	
	public Models getModels() {
		return models;
	}
	
	public void setBrushes(byte[] data) {
		brushes=new Brushes(data);
	}
	
	public Brushes getBrushes() {
		return brushes;
	}
	
	public void setBrushSides(byte[] data) {
		brushSides=new BrushSides(data);
	}
	
	public BrushSides getBrushSides() {
		return brushSides;
	}
	
	public void setTextureMatrices(byte[] data) {
		textureMatrices=new TextureMatrices(data);
	}
	
	public TextureMatrices getTextureMatrices() {
		return textureMatrices;
	}
}
