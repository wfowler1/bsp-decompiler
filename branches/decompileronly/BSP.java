// BSP class
// Holds data for any and all BSP formats. Any unused lumps in a given format
// will be left as null. Then it will be fed into a universal decompile method
// which should be able to perform its job based on what data is stored.

public class BSP {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	// Bunch of different versions. Can be used to differentiate maps or strucures.
	public static final int TYPE_QUAKE=29;
	// public static final int TYPE_GOLDSRC=30; // Uses same algorithm and structures as Quake
	public static final int TYPE_NIGHTFIRE=42;
	public static final int TYPE_STEF2=556942937;
	public static final int TYPE_MOHAA=892416069;
	// public static final int TYPE_MOHBT=1095516506; // Similar enough to MOHAA to use the same structures and algorithm
	public static final int TYPE_STEF2DEMO=1263223129;
	public static final int TYPE_FAKK=1263223152;
	public static final int TYPE_COD2=1347633741; // Uses same algorithm and structures as COD1 Read differently.
	public static final int TYPE_SIN=1347633747; // The headers for SiN and Jedi Outcast are exactly the same
	public static final int TYPE_RAVEN=1347633748;
	public static final int TYPE_COD4=1347633759; // Uses same algorithm and structures as COD1. Read differently.
	public static final int TYPE_SOURCE17=1347633767;
	public static final int TYPE_SOURCE18=1347633768;
	public static final int TYPE_SOURCE19=1347633769;
	public static final int TYPE_SOURCE20=1347633770;
	public static final int TYPE_SOURCE21=1347633771;
	public static final int TYPE_SOURCE22=1347633772;
	public static final int TYPE_SOURCE23=1347633773;
	public static final int TYPE_QUAKE2=1347633775;
	public static final int TYPE_SOF=1347633782; // Uses the same header as Q3.
	public static final int TYPE_QUAKE3=1347633783;
	// public static final int TYPE_RTCW=1347633784; // Uses same algorithm and structures as Quake 3
	public static final int TYPE_COD=1347633796;
	
	// What kind is this map?
	private int version;
	
	private String filePath;
	
	// Map structures
	// Quake 1/GoldSrc
	private Entities entities;
	private Planes planes;
	private Textures textures;
	private Vertices vertices;
	private Nodes nodes;
	private TexInfos texInfo;
	private Faces faces;
	private Leaves leaves;
	private NumList markSurfaces;
	private Edges edges;
	private NumList surfEdges;
	private Models models;
	// Quake 2
	private Brushes brushes;
	private BrushSides brushSides;
	private NumList markBrushes;
	// MOHAA
	//private MoHAAStaticProps staticProps;
	// Nightfire
	private Textures materials;
	// Source
	private Faces originalFaces;
	private NumList texTable;
	private SourceTexDatas texDatas;
	private SourceDispInfos dispInfos;
	private SourceDispVertices dispVerts;
	private NumList displacementTriangles;
	private SourceStaticProps staticProps;
	private SourceCubemaps cubemaps;
	
	// CONSTRUCTORS
	public BSP(String filePath, int version) {
		this.filePath=filePath;
		this.version=version;
	}
	
	// METHODS

	public void printBSPReport() {
		// If there's a NullPointerException here, the BSPReader class didn't initialize the object and therefore
		// this is either a BSP format which doesn't use that lump, or there's an error which will become apparent.
		try {
			Window.println("Entities lump: "+entities.getLength()+" bytes, "+entities.length()+" items",Window.VERBOSITY_MAPSTATS);
		} catch(java.lang.NullPointerException e) {
		}
		try {
			Window.println("Planes lump: "+planes.getLength()+" bytes, "+planes.length()+" items",Window.VERBOSITY_MAPSTATS);
		} catch(java.lang.NullPointerException e) {
		}
		try {
			Window.println("Texture lump: "+textures.getLength()+" bytes, "+textures.length()+" items",Window.VERBOSITY_MAPSTATS);
		} catch(java.lang.NullPointerException e) {
		}
		try {
			Window.println("Vertices lump: "+vertices.getLength()+" bytes, "+vertices.length()+" items",Window.VERBOSITY_MAPSTATS);
		} catch(java.lang.NullPointerException e) {
		}
		try {
			Window.println("Nodes lump: "+nodes.getLength()+" bytes, "+nodes.length()+" items",Window.VERBOSITY_MAPSTATS);
		} catch(java.lang.NullPointerException e) {
		}
		try {
			Window.println("Texture info lump: "+texInfo.getLength()+" bytes, "+texInfo.length()+" items",Window.VERBOSITY_MAPSTATS);
		} catch(java.lang.NullPointerException e) {
		}
		try {
			Window.println("Faces lump: "+faces.getLength()+" bytes, "+faces.length()+" items",Window.VERBOSITY_MAPSTATS);
		} catch(java.lang.NullPointerException e) {
		}
		try {
			Window.println("Leaves lump: "+leaves.getLength()+" bytes, "+leaves.length()+" items",Window.VERBOSITY_MAPSTATS);
		} catch(java.lang.NullPointerException e) {
		}
		try {
			Window.println("Mark surfaces lump: "+markSurfaces.getLength()+" bytes, "+markSurfaces.length()+" items",Window.VERBOSITY_MAPSTATS);
		} catch(java.lang.NullPointerException e) {
		}
		try {
			Window.println("Edges lump: "+edges.getLength()+" bytes, "+edges.length()+" items",Window.VERBOSITY_MAPSTATS);
		} catch(java.lang.NullPointerException e) {
		}
		try {
			Window.println("Surface Edges lump: "+surfEdges.getLength()+" bytes, "+surfEdges.length()+" items",Window.VERBOSITY_MAPSTATS);
		} catch(java.lang.NullPointerException e) {
		}
		try {
			Window.println("Models lump: "+models.getLength()+" bytes, "+models.length()+" items",Window.VERBOSITY_MAPSTATS);
		} catch(java.lang.NullPointerException e) {
		}
		try {
			Window.println("Brushes lump: "+brushes.getLength()+" bytes, "+brushes.length()+" items",Window.VERBOSITY_MAPSTATS);
		} catch(java.lang.NullPointerException e) {
		}
		try {
			Window.println("Brush sides lump: "+brushSides.getLength()+" bytes, "+brushSides.length()+" items",Window.VERBOSITY_MAPSTATS);
		} catch(java.lang.NullPointerException e) {
		}
		try {
			Window.println("Mark brushes lump: "+markBrushes.getLength()+" bytes, "+markBrushes.length()+" items",Window.VERBOSITY_MAPSTATS);
		} catch(java.lang.NullPointerException e) {
		}
		/*try {
			Window.println("Static prop lump: "+staticProps.getLength()+" bytes, "+staticProps.length()+" items",Window.VERBOSITY_MAPSTATS);
		} catch(java.lang.NullPointerException e) {
		}*/
		try {
			Window.println("Original Faces lump: "+originalFaces.getLength()+" bytes, "+originalFaces.length()+" items",Window.VERBOSITY_MAPSTATS);
		} catch(java.lang.NullPointerException e) {
		}
		try {
			Window.println("Texture index table lump: "+texTable.getLength()+" bytes, "+texTable.length()+" items",Window.VERBOSITY_MAPSTATS);
		} catch(java.lang.NullPointerException e) {
		}
		try {
			Window.println("Textures lump: "+texDatas.getLength()+" bytes, "+texDatas.length()+" items",Window.VERBOSITY_MAPSTATS);
		} catch(java.lang.NullPointerException e) {
		}
		try {
			Window.println("Displacement info lump: "+dispInfos.getLength()+" bytes, "+dispInfos.length()+" items",Window.VERBOSITY_MAPSTATS);
		} catch(java.lang.NullPointerException e) {
		}
		try {
			Window.println("Displacement Vertices lump: "+dispVerts.getLength()+" bytes, "+dispVerts.length()+" items",Window.VERBOSITY_MAPSTATS);
		} catch(java.lang.NullPointerException e) {
		}
		try {
			Window.println("Displacement Triangle Tags lump: "+displacementTriangles.getLength()+" bytes, "+displacementTriangles.length()+" items",Window.VERBOSITY_MAPSTATS);
		} catch(java.lang.NullPointerException e) {
		}
		try {
			Window.println("Static Props lump: "+staticProps.getLength()+" bytes, "+staticProps.length()+" items, "+staticProps.getDictionary().length+" unique models",Window.VERBOSITY_MAPSTATS);
		} catch(java.lang.NullPointerException e) {
		}
		try {
			Window.println("Cubemaps lump: "+cubemaps.getLength()+" bytes, "+cubemaps.length()+" items",Window.VERBOSITY_MAPSTATS);
		} catch(java.lang.NullPointerException e) {
		}
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
	
	// Only for Source engine.
	public int findTexDataWithTexture(String texture) {
		for(int i=0;i<texDatas.length();i++) {
			String temp=textures.getTextureAtOffset((int)texTable.getElement(texDatas.getElement(i).getStringTableIndex()));
			if(temp==texture) {
				return i;
			}
		}
		return -1;
	}
	
	// ACCESSORS/MUTATORS
	
	public String getPath() {
		return filePath;
	}
	
	public String getMapName() {
		int i;
		for(i=0;i<filePath.length();i++) {
			if(filePath.charAt(filePath.length()-1-i)=='\\') {
				break;
			}
			if(filePath.charAt(filePath.length()-1-i)=='/') {
				break;
			}
		}
		return filePath.substring(filePath.length()-i,filePath.length());
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
		for(i=0;i<filePath.length();i++) {
			if(filePath.charAt(filePath.length()-1-i)=='\\') {
				break;
			}
			if(filePath.charAt(filePath.length()-1-i)=='/') {
				break;
			}
		}
		return filePath.substring(0,filePath.length()-i);
	}
	
	public int getVersion() {
		return version;
	}
	
	public Entities getEntities() {
		return entities;
	}
	
	public void setEntities(byte[] data) throws java.lang.InterruptedException {
		entities=new Entities(data);
	}
	
	public Planes getPlanes() {
		return planes;
	}
	
	public void setPlanes(byte[] data) throws java.lang.InterruptedException {
		planes=new Planes(data, version);
	}
	
	public Textures getTextures() {
		return textures;
	}
	
	public void setTextures(byte[] data) throws java.lang.InterruptedException {
		textures=new Textures(data, version);
	}
	
	public Textures getMaterials() {
		return materials;
	}
	
	public void setMaterials(byte[] data) throws java.lang.InterruptedException {
		materials=new Textures(data, version);
	}
	
	public Vertices getVertices() {
		return vertices;
	}
	
	public void setVertices(byte[] data) throws java.lang.InterruptedException {
		vertices=new Vertices(data, version);
	}
	
	public Nodes getNodes() {
		return nodes;
	}
	
	public void setNodes(byte[] data) throws java.lang.InterruptedException {
		nodes=new Nodes(data, version);
	}
	
	public TexInfos getTexInfo() {
		return texInfo;
	}
	
	public void setTexInfo(byte[] data) throws java.lang.InterruptedException {
		texInfo=new TexInfos(data, version);
	}
	
	public Faces getFaces() {
		return faces;
	}
	
	public void setFaces(byte[] data) throws java.lang.InterruptedException {
		faces=new Faces(data, version);
	}
	
	public Leaves getLeaves() {
		return leaves;
	}
	
	public void setLeaves(byte[] data) throws java.lang.InterruptedException {
		leaves=new Leaves(data, version);
	}
	
	public NumList getMarkSurfaces() {
		return markSurfaces;
	}
	
	public void setMarkSurfaces(byte[] data) throws java.lang.InterruptedException {
		switch(version) {
			case TYPE_QUAKE:
				markSurfaces=new NumList(data, NumList.TYPE_USHORT);
				break;
		}
	}
	
	public Edges getEdges() {
		return edges;
	}
	
	public void setEdges(byte[] data) throws java.lang.InterruptedException {
		edges=new Edges(data);
	}
	
	public NumList getSurfEdges() {
		return surfEdges;
	}
	
	public void setSurfEdges(byte[] data) throws java.lang.InterruptedException {
		switch(version) {
			case TYPE_QUAKE:
			case TYPE_QUAKE2:
			case TYPE_SIN:
			case TYPE_SOF:
			case TYPE_SOURCE17:
			case TYPE_SOURCE18:
			case TYPE_SOURCE19:
			case TYPE_SOURCE20:
			case TYPE_SOURCE21:
			case TYPE_SOURCE22:
			case TYPE_SOURCE23:
				surfEdges=new NumList(data, NumList.TYPE_INT);
				break;
		}
	}
	
	public Models getModels() {
		return models;
	}
	
	public void setModels(byte[] data) throws java.lang.InterruptedException {
		models=new Models(data, version);
	}
	
	public Brushes getBrushes() {
		return brushes;
	}
	
	public void setBrushes(byte[] data) throws java.lang.InterruptedException {
		brushes=new Brushes(data, version);
	}
	
	public BrushSides getBrushSides() {
		return brushSides;
	}
	
	public void setBrushSides(byte[] data) throws java.lang.InterruptedException {
		brushSides=new BrushSides(data, version);
	}
	
	public void setMarkBrushes(byte[] data) throws java.lang.InterruptedException {
		switch(version) {
			case TYPE_QUAKE2:
			case TYPE_SIN:
			case TYPE_SOF:
			case TYPE_SOURCE17:
			case TYPE_SOURCE18:
			case TYPE_SOURCE19:
			case TYPE_SOURCE20:
			case TYPE_SOURCE21:
			case TYPE_SOURCE22:
			case TYPE_SOURCE23:
				markBrushes=new NumList(data, NumList.TYPE_USHORT);
				break;
			case TYPE_NIGHTFIRE:
				markBrushes=new NumList(data, NumList.TYPE_UINT);
				break;
		}
	}
	
	public NumList getMarkBrushes() {
		return markBrushes;
	}
	
	public void setTexDatas(byte[] data) throws java.lang.InterruptedException {
		texDatas=new SourceTexDatas(data);
	}
	
	public SourceTexDatas getTexDatas() {
		return texDatas;
	}
	
	public void setDispInfos(byte[] data) throws java.lang.InterruptedException {
		dispInfos=new SourceDispInfos(data, version);
	}
	
	public SourceDispInfos getDispInfos() {
		return dispInfos;
	}
	
	public Faces getOriginalFaces() {
		return originalFaces;
	}
	
	public void setOriginalFaces(byte[] data) throws java.lang.InterruptedException {
		originalFaces=new Faces(data, version);
	}
	
	public void setDispVerts(byte[] data) throws java.lang.InterruptedException {
		dispVerts=new SourceDispVertices(data);
	}
	
	public SourceDispVertices getDispVerts() {
		return dispVerts;
	}
	
	public void setTexTable(byte[] data) throws java.lang.InterruptedException {
		texTable=new NumList(data, NumList.TYPE_UINT);
	}
	
	public NumList getTexTable() {
		return texTable;
	}
	
	public void setDispTris(byte[] data) throws java.lang.InterruptedException {
		displacementTriangles=new NumList(data, NumList.TYPE_USHORT);
	}
	
	public NumList getDispTris() {
		return displacementTriangles;
	}
	
	public void setStaticProps(byte[] data, int lumpVersion) throws java.lang.InterruptedException {
		staticProps=new SourceStaticProps(data, version, lumpVersion);
	}
	
	public SourceStaticProps getStaticProps() {
		return staticProps;
	}
	
	public void setCubemaps(byte[] data) throws java.lang.InterruptedException {
		cubemaps=new SourceCubemaps(data, version);
	}

	public SourceCubemaps getCubemaps() {
		return cubemaps;
	}
	
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