// SourceBSP class
// This class gathers all relevant information of a Source engine BSP (VBSP).
// Since all Source engine BSP formats are roughly equivalent, I can deal with differences
// between versions here in this class, rather than making a separate class for each version.
// I may have to make different versions of the lump classes to handle differences in format
// in those, but that's okay.

// Even though Source is a much newer map format than older IBSP versions, it still has some
// very similar structures. I can still use the old lump classes for planes and entities, for
// example. I think even the edges and surfedges lumps are the same as Quake 2.

public class SourceBSP {

	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	// This is the version of the BSP.
	public int version;
	
	private String filepath;
	
	// Each lump has its own class for handling its specific data structures.
	// These are the only lumps we need for decompilation.
	private Entities entities; // Entities format hasn't changed since the original Quake! :D
	private Planes planes; // Neither has plane format (in Valve games anyway)
	private SourceTexDatas texDatas;
	private Vertices vertices;
	private Nodes nodes;
	private SourceTexInfos texInfos;
	// private SourceFaces faces;
	private Leaves leaves;
	private Edges edges;
	private IntList surfedges;
	private Models models;
	private ShortList markbrushes;
	private Brushes brushes;
	private BrushSides brushSides;
	private SourceDispInfos dispInfos;
	// private SourceFaces originalFaces;
	private SourceDispVertices dispVerts;
	private SourceTextures textures;
	private IntList texTable;
	private ShortList displacementTriangles;
	
	// CONSTRUCTORS
	// This accepts a folder path and looks for the BSP there.
	public SourceBSP(String in, int version) {
		filepath=in;
		this.version=version;
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
			Window.println("Texture data lump: "+texDatas.getLength()+" bytes, "+texDatas.length()+" items",Window.VERBOSITY_MAPSTATS);
		} catch(java.lang.NullPointerException e) {
			Window.println("Texture data not yet parsed!",Window.VERBOSITY_MAPSTATS);
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
			Window.println("Texture info lump: "+texInfos.getLength()+" bytes, "+texInfos.length()+" items",Window.VERBOSITY_MAPSTATS);
		} catch(java.lang.NullPointerException e) {
			Window.println("Texture info not yet parsed!",Window.VERBOSITY_MAPSTATS);
		}
		/*try {
			Window.println("Faces lump: "+faces.getLength()+" bytes, "+faces.length()+" items",Window.VERBOSITY_MAPSTATS);
		} catch(java.lang.NullPointerException e) {
			Window.println("Faces not yet parsed!",Window.VERBOSITY_MAPSTATS);
		}*/
		try {
			Window.println("Leaves lump: "+leaves.getLength()+" bytes, "+leaves.length()+" items",Window.VERBOSITY_MAPSTATS);
		} catch(java.lang.NullPointerException e) {
			Window.println("Leaves not yet parsed!",Window.VERBOSITY_MAPSTATS);
		}
		try {
			Window.println("Edges lump: "+edges.getLength()+" bytes, "+edges.getNumElements()+" items",Window.VERBOSITY_MAPSTATS);
		} catch(java.lang.NullPointerException e) {
			Window.println("Edges not yet parsed!",Window.VERBOSITY_MAPSTATS);
		}
		try {
			Window.println("Surface Edges lump: "+surfedges.getLength()+" bytes, "+surfedges.getNumElements()+" items",Window.VERBOSITY_MAPSTATS);
		} catch(java.lang.NullPointerException e) {
			Window.println("Surface Edges not yet parsed!",Window.VERBOSITY_MAPSTATS);
		}
		try {
			Window.println("Models lump: "+models.getLength()+" bytes, "+models.length()+" items",Window.VERBOSITY_MAPSTATS);
		} catch(java.lang.NullPointerException e) {
			Window.println("Texture scales not yet parsed!",Window.VERBOSITY_MAPSTATS);
		}
		try {
			Window.println("Leaf brushes lump: "+markbrushes.getLength()+" bytes, "+markbrushes.getNumElements()+" items",Window.VERBOSITY_MAPSTATS);
		} catch(java.lang.NullPointerException e) {
			Window.println("Leaf brushes not yet parsed!",Window.VERBOSITY_MAPSTATS);
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
			Window.println("Displacement info lump: "+dispInfos.getLength()+" bytes, "+dispInfos.length()+" items",Window.VERBOSITY_MAPSTATS);
		} catch(java.lang.NullPointerException e) {
			Window.println("Displacement info not yet parsed!",Window.VERBOSITY_MAPSTATS);
		}
		/*try {
			Window.println("Original Faces lump: "+originalFaces.getLength()+" bytes, "+originalFaces.length()+" items",Window.VERBOSITY_MAPSTATS);
		} catch(java.lang.NullPointerException e) {
			Window.println("Original Faces not yet parsed!",Window.VERBOSITY_MAPSTATS);
		}*/
		try {
			Window.println("Displacement Vertices lump: "+dispVerts.getLength()+" bytes, "+dispVerts.length()+" items",Window.VERBOSITY_MAPSTATS);
		} catch(java.lang.NullPointerException e) {
			Window.println("Displacement Vertices not yet parsed!",Window.VERBOSITY_MAPSTATS);
		}
		try {
			Window.println("Textures lump: "+textures.getLength()+" bytes, "+textures.length()+" items",Window.VERBOSITY_MAPSTATS);
		} catch(java.lang.NullPointerException e) {
			Window.println("Textures not yet parsed!",Window.VERBOSITY_MAPSTATS);
		}
		try {
			Window.println("Texture index table lump: "+texTable.getLength()+" bytes, "+texTable.getNumElements()+" items",Window.VERBOSITY_MAPSTATS);
		} catch(java.lang.NullPointerException e) {
			Window.println("Texture index table not yet parsed!",Window.VERBOSITY_MAPSTATS);
		}
		try {
			Window.println("Displacement Triangle Tags lump: "+displacementTriangles.getLength()+" bytes, "+displacementTriangles.getNumElements()+" items",Window.VERBOSITY_MAPSTATS);
		} catch(java.lang.NullPointerException e) {
			Window.println("Displacement Triangle Tags not yet parsed!",Window.VERBOSITY_MAPSTATS);
		}
	}
	
	// Some code from my Quake 2 BSP format, allowing iteration through the BSP tree without
	// using recursion. Recursive algorithms for this many nodes will give stack overflows in
	// Java, even on the comparatively smaller Quake 2 maps.
	
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
				newList[nodeLeaves.length]=leaves.getElement((right*(-1))-1); // Source subtracts 1 from the index, like Quake 2
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
				newList[nodeLeaves.length]=leaves.getElement((left*(-1))-1); // Source subtracts 1 from the index, like Quake 2
				nodeLeaves=newList;
			}
		}
		return nodeLeaves;
	}
	
	public int findTexDataWithTexture(String texture) {
		for(int i=0;i<texDatas.length();i++) {
			String temp=textures.getTextureAtOffset(texTable.getInt(texDatas.getElement(i).getStringTableIndex()));
			if(temp==texture) {
				return i;
			}
		}
		return -1;
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
	
	public void setTexDatas(byte[] data) {
		texDatas=new SourceTexDatas(data);
	}
	
	public SourceTexDatas getTexDatas() {
		return texDatas;
	}
	
	public void setVertices(byte[] data) {
		vertices=new Vertices(data);
	}
	
	public Vertices getVertices() {
		return vertices;
	}
	
	public void setNodes(byte[] data) {
		nodes=new Nodes(data, Node.TYPE_SOURCE);
	}
	
	public Nodes getNodes() {
		return nodes;
	}
	
	public void setTexInfos(byte[] data) {
		texInfos=new SourceTexInfos(data);
	}
	
	public SourceTexInfos getTexInfos() {
		return texInfos;
	}
	/*
	public void setFaces(byte[] data) {
		faces=new SourceFaces(data);
	}
	
	public SourceFaces getFaces() {
		return faces;
	}
	*/
	public void setLeaves(byte[] data) {
		if(version > 17 && version < 20) {
			leaves=new Leaves(data, Leaf.TYPE_SOURCE19);
		} else {
			leaves=new Leaves(data, Leaf.TYPE_SOURCE20);
		}
	}

	public Leaves getLeaves() {
		return leaves;
	}
	
	public void setEdges(byte[] data) {
		edges=new Edges(data);
	}
	
	public Edges getEdges() {
		return edges;
	}
	
	public void setSurfEdges(byte[] data) {
		surfedges=new IntList(data);
	}
	
	public IntList getSurfEdges() {
		return surfedges;
	}
	
	public void setModels(byte[] data) {
		models=new Models(data, Model.TYPE_QUAKE2);
	}
	
	public Models getModels() {
		return models;
	}
	
	public void setMarkBrushes(byte[] data) {
		markbrushes=new ShortList(data);
	}
	
	public ShortList getMarkBrushes() {
		return markbrushes;
	}
	
	public void setBrushes(byte[] data) {
		brushes=new Brushes(data, Brush.TYPE_QUAKE2);
	}
	
	public Brushes getBrushes() {
		return brushes;
	}
	
	public void setBrushSides(byte[] data) {
		brushSides=new BrushSides(data, BrushSide.TYPE_SOURCE);
	}
	
	public BrushSides getBrushSides() {
		return brushSides;
	}
	
	public void setDispInfos(byte[] data) {
		dispInfos=new SourceDispInfos(data);
	}
	
	public SourceDispInfos getDistInfos() {
		return dispInfos;
	}
	// TODO: more accessors/mutators
	// private SourceFaces originalFaces;
	
	public void setDispVerts(byte[] data) {
		dispVerts=new SourceDispVertices(data);
	}
	
	public SourceDispVertices getDispVerts() {
		return dispVerts;
	}
	
	public void setTextures(byte[] data) {
		textures=new SourceTextures(data);
	}
	
	public SourceTextures getTextures() {
		return textures;
	}
	
	public void setTexTable(byte[] data) {
		texTable=new IntList(data);
	}
	
	public IntList getTexTable() {
		return texTable;
	}
	
	public void setDispTris(byte[] data) {
		displacementTriangles=new ShortList(data);
	}
	
	public ShortList getDispTris() {
		return displacementTriangles;
	}
	
	// INTERNAL CLASSES
	
	// NodeStack class

	// Contains a "stack" of v38Nodes. This aids greatly in the
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
