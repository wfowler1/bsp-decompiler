// BSPData class
// This class gathers all relevant information from the lumps, and attempts
// to recreate the source MAP file of the BSP as accurately as possible.

// Also sets up and runs the GUI through which the actions of this class are
// controlled. It doesn't need to be very complicated.

import java.io.File;

public class BSPData {

	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	private static Runtime r = Runtime.getRuntime(); // Get a runtime object. This is for calling
	                                                 // Java's garbage collector and does not need
												                // to be ported. I try not to leave memory leaks
												                // but since Java has no way explicitly reallocate
												                // unused memory I have to tell it when a good
												                // time is to run the garbage collector, by
												                // calling gc(). Also, it is used to execute EXEs
												                // from within the program by calling .exec(path).

	// All lumps will be in the same folder. This String IS that folder.
	private String filepath;
	
	// This is the version of the BSP. This will determine the lumps order and aid in
	// decompilation.
	private int version;
	
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
	
	// Allows us to reference the X Y or Z components of a vector by their letter
	public final int X=0;
	public final int Y=1;
	public final int Z=2;
	
	// Declare this here since the lumps path of the BSP probably will not change
	private LS ls;
	
	// CONSTRUCTORS
	// This accepts a file path and parses it into the form needed. If the folder is empty (or not found)
	// the program fails nicely.
	public BSPData(String in) {
		try {
			filepath=in.substring(0,in.length()-4)+"\\";
			ls=new LS(in);
			version = ls.separateLumps();
			
			entities = new Entities(filepath+"Entities.txt");
			planes = new Planes(filepath+"Planes.hex");
			textures = new Texture64(filepath+"Textures.hex");
			materials = new Materials64(filepath+"Materials.hex");
			vertices = new Vertices(filepath+"Vertices.hex");
			faces = new Faces(filepath+"Faces.hex");
			leaves = new Leaves(filepath+"Leaves.hex");
			markbrushes = new IntList(filepath+"Mark Brushes.hex");
			models = new Models(filepath+"Models.hex");
			brushes = new Brushes(filepath+"Brushes.hex");
			brushSides = new BrushSides(filepath+"Brushsides.hex");
			textureMatrices = new TextureMatrices(filepath+"Texmatrix.hex");
			
			r.gc(); // Take a minute to collect garbage, all the file parsing can leave a lot of crap data.
			
			Window.window.println("Entities lump: "+entities.getLength()+" bytes, "+entities.getNumElements()+" items");
			Window.window.println("Planes lump: "+planes.getLength()+" bytes, "+planes.getNumElements()+" items");
			Window.window.println("Textures lump: "+textures.getLength()+" bytes, "+textures.getNumElements()+" items");
			Window.window.println("Materials lump: "+materials.getLength()+" bytes, "+materials.getNumElements()+" items");
			Window.window.println("Vertices lump: "+vertices.getLength()+" bytes, "+vertices.getNumElements()+" items");
			Window.window.println("Faces lump: "+faces.getLength()+" bytes, "+faces.getNumElements()+" items");
			Window.window.println("Leaves lump: "+leaves.getLength()+" bytes, "+leaves.getNumElements()+" items");
			Window.window.println("Leaf brushes lump: "+markbrushes.getLength()+" bytes, "+markbrushes.getNumElements()+" items");
			Window.window.println("Models lump: "+models.getLength()+" bytes, "+models.getNumElements()+" items");
			Window.window.println("Brushes lump: "+brushes.getLength()+" bytes, "+brushes.getNumElements()+" items");
			Window.window.println("Brush sides lump: "+brushSides.getLength()+" bytes, "+brushSides.getNumElements()+" items");
			Window.window.println("Texture scales lump: "+textureMatrices.getLength()+" bytes, "+textureMatrices.getNumElements()+" items");
			
		} catch(java.lang.StringIndexOutOfBoundsException e) {
			Window.window.println("Error: invalid path");
		}
	}
	
	protected void close() {
		Window.window.println("Deleting lump files");
		ls.deleteLumps();
	}
	
	// ACCESSORS/MUTATORS
	
	public String getPath() {
		return filepath;
	}
	
	public Entities getEntities() {
		return entities;
	}
	
	public Planes getPlanes() {
		return planes;
	}
	
	public Texture64 getTextures() {
		return textures;
	}
	
	public Materials64 getMaterials() {
		return materials;
	}
	
	public Vertices getVertices() {
		return vertices;
	}
	
	public Faces getFaces() {
		return faces;
	}
	
	public Leaves getLeaves() {
		return leaves;
	}
	
	public IntList getMarkBrushes() {
		return markbrushes;
	}
	
	public Models getModels() {
		return models;
	}
	
	public Brushes getBrushes() {
		return brushes;
	}
	
	public BrushSides getBrushSides() {
		return brushSides;
	}
	
	public TextureMatrices getTextureMatrices() {
		return textureMatrices;
	}
}
