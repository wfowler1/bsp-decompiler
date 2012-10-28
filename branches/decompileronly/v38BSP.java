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
	private Planes planes;
	private Textures textures;
	private Vertices vertices;
	private Nodes nodes;
	private Faces faces;
	private Leaves leaves;
	private ShortList markbrushes;
	private Edges edges;
	private IntList markedges;
	private Models models;
	private Brushes brushes;
	private BrushSides brushSides;
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
			Window.println("Vertices lump: "+vertices.getLength()+" bytes, "+vertices.getNumElements()+" items",Window.VERBOSITY_MAPSTATS);
		} catch(java.lang.NullPointerException e) {
			Window.println("Vertices not yet parsed!",Window.VERBOSITY_MAPSTATS);
		}
		try {
			Window.println("Nodes lump: "+nodes.getLength()+" bytes, "+nodes.length()+" items",Window.VERBOSITY_MAPSTATS);
		} catch(java.lang.NullPointerException e) {
			Window.println("Nodes not yet parsed!",Window.VERBOSITY_MAPSTATS);
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
		/*try {
			Window.println("Area Portals lump: "+areaPortals.getLength()+" bytes, "+areaPortals.getNumElements()+" items",Window.VERBOSITY_MAPSTATS);
		} catch(java.lang.NullPointerException e) {
			Window.println("Area Portals not yet parsed!",Window.VERBOSITY_MAPSTATS);
		}*/
	}
	
	// +getLeavesInModel(int)
	// Returns an array of Leaf containing all the leaves referenced from
	// this model's head node. This array cannot be referenced by index numbers
	// from other lumps, but if simply iterating through, getting information
	// it'll be just fine.
	public Leaf[] getLeavesInModel(int model) {
		return getLeavesInNode(models.getElement(model).getHeadNode());
	}
	
	// +getLeavesInNode(int)
	// Returns an array of Leaf containing all the leaves referenced from
	// this node. Since nodes reference other nodes, this may recurse quite
	// some ways. Eventually every node will boil down to a set of leaves,
	// which is what this method returns.
	
	// This is an iterative preorder traversal algorithm modified from the Wikipedia page at:
	// http://en.wikipedia.org/wiki/Tree_traversal#Iterative_Traversal
	// I needed an iterative algorithm because recursive ones commonly gave stack overflows.
	public Leaf[] getLeavesInNode(int head) {
		Node headNode;
		Leaf[] nodeLeaves=new Leaf[0];
		try {
			headNode=nodes.getElement(head);
		} catch(java.lang.ArrayIndexOutOfBoundsException e) {
			return nodeLeaves;
		}
		NodeStack nodestack = new NodeStack();
		nodestack.push(headNode);
 
		Node currentNode;

		while (!nodestack.isEmpty()) {
			currentNode = nodestack.pop();
			int right = currentNode.getChild2();
			if (right >= 0) {
				nodestack.push(nodes.getElement(right));
			} else {
				Leaf[] newList=new Leaf[nodeLeaves.length+1];
				for(int i=0;i<nodeLeaves.length;i++) {
					newList[i]=nodeLeaves[i];
				}
				newList[nodeLeaves.length]=leaves.getElement((right*(-1))-1); // Quake 2 subtracts 1 from the index
				nodeLeaves=newList;
			}
			int left = currentNode.getChild1();
			if (left >= 0) {
				nodestack.push(nodes.getElement(left));
			} else {
				Leaf[] newList=new Leaf[nodeLeaves.length+1];
				for(int i=0;i<nodeLeaves.length;i++) {
					newList[i]=nodeLeaves[i];
				}
				newList[nodeLeaves.length]=leaves.getElement((left*(-1))-1); // Quake 2 subtracts 1 from the index
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
		planes=new Planes(data, Plane.TYPE_QUAKE);
	}
	
	public Planes getPlanes() {
		return planes;
	}
	
	public void setTextures(byte[] data) {
		textures=new Textures(data, Texture.TYPE_QUAKE2);
	}
	
	public Textures getTextures() {
		return textures;
	}
	
	public void setVertices(byte[] data) {
		vertices=new Vertices(data);
	}
	
	public Vertices getVertices() {
		return vertices;
	}
	
	public void setNodes(byte[] data) {
		nodes=new Nodes(data, Node.TYPE_QUAKE2);
	}
	
	public Nodes getNodes() {
		return nodes;
	}
	
	public void setFaces(byte[] data) {
		faces=new Faces(data, Face.TYPE_QUAKE);
	}
	
	public Faces getFaces() {
		return faces;
	}
	
	public void setLeaves(byte[] data) {
		leaves=new Leaves(data, Leaf.TYPE_QUAKE2);
	}
	
	public Leaves getLeaves() {
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
		models=new Models(data, Model.TYPE_QUAKE2);
	}
	
	public Models getModels() {
		return models;
	}
	
	public void setBrushes(byte[] data) {
		brushes=new Brushes(data, Brush.TYPE_QUAKE2);
	}
	
	public Brushes getBrushes() {
		return brushes;
	}
	
	public void setBrushSides(byte[] data) {
		brushSides=new BrushSides(data, BrushSide.TYPE_QUAKE2);
	}
	
	public BrushSides getBrushSides() {
		return brushSides;
	}
	
	/*public void setAreaPortals(byte[] data) {
		areaPortals=new IntList(data);
	}
	
	public IntList getAreaPortals() {
		return areaPortals;
	}*/
	
	
	
	// INTERNAL CLASSES
	
	// NodeStack class

	// Contains a "stack" of Nodes. This aids greatly in the
	// traversal of a BSP tree without use of recursion.
	
	private class NodeStack {
		
		// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
		
		Node[] stack;
		
		// CONSTRUCTORS
		
		public NodeStack() {
			stack=new Node[0];
		}
		
		// METHODS
		
		public void push(Node in) {
			Node[] newStack = new Node[stack.length+1];
			for(int i=0;i<stack.length;i++) {
				newStack[i]=stack[i];
			}
			newStack[newStack.length-1]=in;
			stack=newStack;
		}
		
		public Node pop() {
			Node returnme=stack[stack.length-1];
			Node[] newStack=new Node[stack.length-1];
			for(int i=0;i<stack.length-1;i++) {
				newStack[i]=stack[i];
			}
			stack=newStack;
			return returnme;
		}
		
		public Node read() {
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
