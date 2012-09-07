// v38BSP class
// This class gathers all relevant information from the lumps of a BSP version 38.

public class v38BSP {

	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	// This is the version of the BSP. This will determine the lumps order and aid in
	// decompilation.
	public static final int VERSION=38;
	
	private String filepath;
	
	// Each lump has its own class for handling its specific data structures.
	// These are the only lumps we need for decompilation.
	private Entities entities;
	private BSPPlanes planes;
	private v38Textures textures;
	private Vertices vertices;
	private v38Nodes nodes;
	private v38Faces faces;
	private v38Leaves leaves;
	private ShortList markbrushes;
	private Edges edges;
	private IntList markedges;
	private Models models;
	private Brushes brushes;
	private v38BrushSides brushSides;
	//private IntList areaPortals;
	
	// CONSTRUCTORS
	// This accepts a folder path and looks for the BSP there.
	public v38BSP(String in) {
		filepath=in;
	}

	public void printBSPReport() {
		try {
			Window.println("Entities lump: "+entities.getLength()+" bytes, "+entities.length()+" items",Window.VERBOSITY_MAPSTATS);
		} catch(java.lang.NullPointerException e) {
			Window.println("Entities not yet parsed!",Window.VERBOSITY_MAPSTATS);
		}
		try {
			Window.println("Planes lump: "+planes.getLength()+" bytes, "+planes.getNumElements()+" items",Window.VERBOSITY_MAPSTATS);
		} catch(java.lang.NullPointerException e) {
			Window.println("Planes not yet parsed!",Window.VERBOSITY_MAPSTATS);
		}
		try {
			Window.println("Textures lump: "+textures.getLength()+" bytes, "+textures.getNumElements()+" items",Window.VERBOSITY_MAPSTATS);
		} catch(java.lang.NullPointerException e) {
			Window.println("Textures not yet parsed!",Window.VERBOSITY_MAPSTATS);
		}
		try {
			Window.println("Vertices lump: "+vertices.getLength()+" bytes, "+vertices.getNumElements()+" items",Window.VERBOSITY_MAPSTATS);
		} catch(java.lang.NullPointerException e) {
			Window.println("Vertices not yet parsed!",Window.VERBOSITY_MAPSTATS);
		}
		try {
			Window.println("Nodes lump: "+nodes.getLength()+" bytes, "+nodes.getNumElements()+" items",Window.VERBOSITY_MAPSTATS);
		} catch(java.lang.NullPointerException e) {
			Window.println("Nodes not yet parsed!",Window.VERBOSITY_MAPSTATS);
		}
		try {
			Window.println("Faces lump: "+faces.getLength()+" bytes, "+faces.getNumElements()+" items",Window.VERBOSITY_MAPSTATS);
		} catch(java.lang.NullPointerException e) {
			Window.println("Faces not yet parsed!",Window.VERBOSITY_MAPSTATS);
		}
		try {
			Window.println("Leaves lump: "+leaves.getLength()+" bytes, "+leaves.getNumElements()+" items",Window.VERBOSITY_MAPSTATS);
		} catch(java.lang.NullPointerException e) {
			Window.println("Leaves not yet parsed!",Window.VERBOSITY_MAPSTATS);
		}
		try {
			Window.println("Leaf brushes lump: "+markbrushes.getLength()+" bytes, "+markbrushes.getNumElements()+" items",Window.VERBOSITY_MAPSTATS);
		} catch(java.lang.NullPointerException e) {
			Window.println("Leaf brushes not yet parsed!",Window.VERBOSITY_MAPSTATS);
		}
		try {
			Window.println("Edges lump: "+edges.getLength()+" bytes, "+edges.getNumElements()+" items",Window.VERBOSITY_MAPSTATS);
		} catch(java.lang.NullPointerException e) {
			Window.println("Edges not yet parsed!",Window.VERBOSITY_MAPSTATS);
		}
		try {
			Window.println("Surface Edges lump: "+markedges.getLength()+" bytes, "+markedges.getNumElements()+" items",Window.VERBOSITY_MAPSTATS);
		} catch(java.lang.NullPointerException e) {
			Window.println("Surface Edges not yet parsed!",Window.VERBOSITY_MAPSTATS);
		}
		try {
			Window.println("Models lump: "+models.getLength()+" bytes, "+models.getNumElements()+" items",Window.VERBOSITY_MAPSTATS);
		} catch(java.lang.NullPointerException e) {
			Window.println("Models not yet parsed!",Window.VERBOSITY_MAPSTATS);
		}
		try {
			Window.println("Brushes lump: "+brushes.getLength()+" bytes, "+brushes.length()+" items",Window.VERBOSITY_MAPSTATS);
		} catch(java.lang.NullPointerException e) {
			Window.println("Brushes not yet parsed!",Window.VERBOSITY_MAPSTATS);
		}
		try {
			Window.println("Brush sides lump: "+brushSides.getLength()+" bytes, "+brushSides.getNumElements()+" items",Window.VERBOSITY_MAPSTATS);
		} catch(java.lang.NullPointerException e) {
			Window.println("Brush sides not yet parsed!",Window.VERBOSITY_MAPSTATS);
		}
		/*try {
			Window.println("Area Portals lump: "+areaPortals.getLength()+" bytes, "+areaPortals.getNumElements()+" items",Window.VERBOSITY_MAPSTATS);
		} catch(java.lang.NullPointerException e) {
			Window.println("Area Portals not yet parsed!",Window.VERBOSITY_MAPSTATS);
		}*/
	}
	
	// +getLeavesInModel(int)
	// Returns an array of v38Leaf containing all the leaves referenced from
	// this model's head node. This array cannot be referenced by index numbers
	// from other lumps, but if simply iterating through, getting information
	// it'll be just fine.
	public v38Leaf[] getLeavesInModel(int model) {
		return getLeavesInNode(models.getModel(model).getHead());
	}
	
	// +getLeavesInNode(int)
	// Returns an array of v38Leaf containing all the leaves referenced from
	// this node. Since nodes reference other nodes, this may recurse quite
	// some ways. Eventually every node will boil down to a set of leaves,
	// which is what this method returns.
	
	// This is an iterative preorder traversal algorithm modified from the Wikipedia page at:
	// http://en.wikipedia.org/wiki/Tree_traversal#Iterative_Traversal
	// I needed an iterative algorithm because recursive ones commonly gave stack overflows.
	public v38Leaf[] getLeavesInNode(int head) {
		v38Node headNode;
		v38Leaf[] nodeLeaves=new v38Leaf[0];
		try {
			headNode=nodes.getNode(head);
		} catch(java.lang.ArrayIndexOutOfBoundsException e) {
			return nodeLeaves;
		}
		v38NodeStack nodestack = new v38NodeStack();
		nodestack.push(headNode);
 
		v38Node currentNode;

		while (!nodestack.isEmpty()) {
			currentNode = nodestack.pop();
			int right = currentNode.getChild2();
			if (right >= 0) {
				nodestack.push(nodes.getNode(right));
			} else {
				v38Leaf[] newList=new v38Leaf[nodeLeaves.length+1];
				for(int i=0;i<nodeLeaves.length;i++) {
					newList[i]=nodeLeaves[i];
				}
				newList[nodeLeaves.length]=leaves.getLeaf((right*(-1))-1); // Quake 2 subtracts 1 from the index
				nodeLeaves=newList;
			}
			int left = currentNode.getChild1();
			if (left >= 0) {
				nodestack.push(nodes.getNode(left));
			} else {
				v38Leaf[] newList=new v38Leaf[nodeLeaves.length+1];
				for(int i=0;i<nodeLeaves.length;i++) {
					newList[i]=nodeLeaves[i];
				}
				newList[nodeLeaves.length]=leaves.getLeaf((left*(-1))-1); // Quake 2 subtracts 1 from the index
				nodeLeaves=newList;
			}
		}
		return nodeLeaves;
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
		textures=new v38Textures(data);
	}
	
	public v38Textures getTextures() {
		return textures;
	}
	
	public void setVertices(byte[] data) {
		vertices=new Vertices(data);
	}
	
	public Vertices getVertices() {
		return vertices;
	}
	
	public void setNodes(byte[] data) {
		nodes=new v38Nodes(data);
	}
	
	public v38Nodes getNodes() {
		return nodes;
	}
	
	public void setFaces(byte[] data) {
		faces=new v38Faces(data);
	}
	
	public v38Faces getFaces() {
		return faces;
	}
	
	public void setLeaves(byte[] data) {
		leaves=new v38Leaves(data);
	}
	
	public v38Leaves getLeaves() {
		return leaves;
	}
	
	public void setMarkBrushes(byte[] data) {
		markbrushes=new ShortList(data);
	}
	
	public ShortList getMarkBrushes() {
		return markbrushes;
	}
	
	public void setEdges(byte[] data) {
		edges=new Edges(data);
	}
	
	public Edges getEdges() {
		return edges;
	}
	
	public void setMarkEdges(byte[] data) {
		markedges=new IntList(data);
	}
	
	public IntList getMarkEdges() {
		return markedges;
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
		brushSides=new v38BrushSides(data);
	}
	
	public v38BrushSides getBrushSides() {
		return brushSides;
	}
	
	/*public void setAreaPortals(byte[] data) {
		areaPortals=new IntList(data);
	}
	
	public IntList getAreaPortals() {
		return areaPortals;
	}*/
	
	
	
	// INTERNAL CLASSES
	
	// v38NodeStack class

	// Contains a "stack" of v38Nodes. This aids greatly in the
	// traversal of a BSP tree without use of recursion.
	
	private class v38NodeStack {
		
		// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
		
		v38Node[] stack;
		
		// CONSTRUCTORS
		
		public v38NodeStack() {
			stack=new v38Node[0];
		}
		
		// METHODS
		
		public void push(v38Node in) {
			v38Node[] newStack = new v38Node[stack.length+1];
			for(int i=0;i<stack.length;i++) {
				newStack[i]=stack[i];
			}
			newStack[newStack.length-1]=in;
			stack=newStack;
		}
		
		public v38Node pop() {
			v38Node returnme=stack[stack.length-1];
			v38Node[] newStack=new v38Node[stack.length-1];
			for(int i=0;i<stack.length-1;i++) {
				newStack[i]=stack[i];
			}
			stack=newStack;
			return returnme;
		}
		
		public v38Node read() {
			return stack[stack.length-1];
		}
		
		// ACCESSORS AND MUTATORS
		
		public boolean isEmpty() {
			return stack.length==0;
		}
		
		public int getSize() {
			return stack.length;
		}
	}
}
