// BSP class
// Holds data for any and all BSP formats. Any unused lumps in a given format
// will be left as null. Then it will be fed into a universal decompile method
// which should be able to perform its job based on what data is stored.

import java.util.Stack;

public class BSP {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	// Bunch of different versions. Can be used to differentiate maps or strucures.
	public static final int TYPE_QUAKE=29;
	// public static final int TYPE_GOLDSRC=30; // Uses same algorithm and structures as Quake
	public static final int TYPE_NIGHTFIRE=42;
	public static final int TYPE_VINDICTUS=346131372;
	public static final int TYPE_STEF2=556942937;
	public static final int TYPE_MOHAA=892416069;
	// public static final int TYPE_MOHBT=1095516506; // Similar enough to MOHAA to use the same structures and algorithm
	public static final int TYPE_STEF2DEMO=1263223129;
	public static final int TYPE_FAKK=1263223152;
	public static final int TYPE_TACTICALINTERVENTION=1268885814;
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
	public static final int TYPE_DAIKATANA=1347633778;
	public static final int TYPE_SOF=1347633782; // Uses the same header as Q3.
	public static final int TYPE_QUAKE3=1347633783;
	// public static final int TYPE_RTCW=1347633784; // Uses same algorithm and structures as Quake 3
	public static final int TYPE_COD=1347633796;
	public static final int TYPE_DMOMAM=1347895914;
	
	// What kind is this map?
	private int version;
	
	private String filePath;
	
	// Map structures
	// Quake 1/GoldSrc
	private Entities entities;
	private Lump<Plane> planes;
	private Textures textures;
	private Lump<Vertex> vertices;
	private Lump<Node> nodes;
	private Lump<TexInfo> texInfo;
	private Lump<Face> faces;
	private Lump<Leaf> leaves;
	private NumList markSurfaces;
	private Lump<Edge> edges;
	private NumList surfEdges;
	private Lump<Model> models;
	// Quake 2
	private Lump<Brush> brushes;
	private Lump<BrushSide> brushSides;
	private NumList markBrushes;
	// MOHAA
	//private MoHAAStaticProps staticProps;
	// Nightfire
	private Textures materials;
	// Source
	private Lump<Face> originalFaces;
	private NumList texTable;
	private Lump<SourceTexData> texDatas;
	private Lump<SourceDispInfo> dispInfos;
	private SourceDispVertices dispVerts;
	private NumList displacementTriangles;
	private SourceStaticProps staticProps;
	private Lump<SourceCubemap> cubemaps;
	//private SourceOverlays overlays;
	
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
			Window.println("Entities lump: "+entities.length()+" bytes, "+entities.size()+" items",Window.VERBOSITY_MAPSTATS);
		} catch(java.lang.NullPointerException e) {
		}
		try {
			Window.println("Planes lump: "+planes.length()+" bytes, "+planes.size()+" items",Window.VERBOSITY_MAPSTATS);
			if(planes.hasFunnySize()) {
				Window.println("WARNING: Funny lump size in Planes",Window.VERBOSITY_WARNINGS);
			}
		} catch(java.lang.NullPointerException e) {
		}
		try {
			Window.println("Texture lump: "+textures.length()+" bytes, "+textures.size()+" items",Window.VERBOSITY_MAPSTATS);
			if(textures.hasFunnySize()) {
				Window.println("WARNING: Funny lump size in Texture",Window.VERBOSITY_WARNINGS);
			}
		} catch(java.lang.NullPointerException e) {
		}
		try {
			Window.println("Materials lump: "+materials.length()+" bytes, "+materials.size()+" items",Window.VERBOSITY_MAPSTATS);
			if(materials.hasFunnySize()) {
				Window.println("WARNING: Funny lump size in Materials",Window.VERBOSITY_WARNINGS);
			}
		} catch(java.lang.NullPointerException e) {
		}
		try {
			Window.println("Vertices lump: "+vertices.length()+" bytes, "+vertices.size()+" items",Window.VERBOSITY_MAPSTATS);
			if(vertices.hasFunnySize()) {
				Window.println("WARNING: Funny lump size in Vertices",Window.VERBOSITY_WARNINGS);
			}
		} catch(java.lang.NullPointerException e) {
		}
		try {
			Window.println("Nodes lump: "+nodes.length()+" bytes, "+nodes.size()+" items",Window.VERBOSITY_MAPSTATS);
			if(nodes.hasFunnySize()) {
				Window.println("WARNING: Funny lump size in Nodes",Window.VERBOSITY_WARNINGS);
			}
		} catch(java.lang.NullPointerException e) {
		}
		try {
			Window.println("Texture info lump: "+texInfo.length()+" bytes, "+texInfo.size()+" items",Window.VERBOSITY_MAPSTATS);
			if(texInfo.hasFunnySize()) {
				Window.println("WARNING: Funny lump size in Texture info",Window.VERBOSITY_WARNINGS);
			}
		} catch(java.lang.NullPointerException e) {
		}
		try {
			Window.println("Faces lump: "+faces.length()+" bytes, "+faces.size()+" items",Window.VERBOSITY_MAPSTATS);
			if(faces.hasFunnySize()) {
				Window.println("WARNING: Funny lump size in Faces",Window.VERBOSITY_WARNINGS);
			}
		} catch(java.lang.NullPointerException e) {
		}
		try {
			Window.println("Leaves lump: "+leaves.length()+" bytes, "+leaves.size()+" items",Window.VERBOSITY_MAPSTATS);
			if(leaves.hasFunnySize()) {
				Window.println("WARNING: Funny lump size in Leaves",Window.VERBOSITY_WARNINGS);
			}
		} catch(java.lang.NullPointerException e) {
		}
		try {
			Window.println("Mark surfaces lump: "+markSurfaces.length()+" bytes, "+markSurfaces.size()+" items",Window.VERBOSITY_MAPSTATS);
			if(markSurfaces.hasFunnySize()) {
				Window.println("WARNING: Funny lump size in Mark surfaces",Window.VERBOSITY_WARNINGS);
			}
		} catch(java.lang.NullPointerException e) {
		}
		try {
			Window.println("Edges lump: "+edges.length()+" bytes, "+edges.size()+" items",Window.VERBOSITY_MAPSTATS);
			if(edges.hasFunnySize()) {
				Window.println("WARNING: Funny lump size in Edges",Window.VERBOSITY_WARNINGS);
			}
		} catch(java.lang.NullPointerException e) {
		}
		try {
			Window.println("Surface Edges lump: "+surfEdges.length()+" bytes, "+surfEdges.size()+" items",Window.VERBOSITY_MAPSTATS);
			if(surfEdges.hasFunnySize()) {
				Window.println("WARNING: Funny lump size in Surface Edges",Window.VERBOSITY_WARNINGS);
			}
		} catch(java.lang.NullPointerException e) {
		}
		try {
			Window.println("Models lump: "+models.length()+" bytes, "+models.size()+" items",Window.VERBOSITY_MAPSTATS);
			if(models.hasFunnySize()) {
				Window.println("WARNING: Funny lump size in Models",Window.VERBOSITY_WARNINGS);
			}
		} catch(java.lang.NullPointerException e) {
		}
		try {
			Window.println("Brushes lump: "+brushes.length()+" bytes, "+brushes.size()+" items",Window.VERBOSITY_MAPSTATS);
			if(brushes.hasFunnySize()) {
				Window.println("WARNING: Funny lump size in Brushes",Window.VERBOSITY_WARNINGS);
			}
		} catch(java.lang.NullPointerException e) {
		}
		try {
			Window.println("Brush sides lump: "+brushSides.length()+" bytes, "+brushSides.size()+" items",Window.VERBOSITY_MAPSTATS);
			if(brushSides.hasFunnySize()) {
				Window.println("WARNING: Funny lump size in Brush sides",Window.VERBOSITY_WARNINGS);
			}
		} catch(java.lang.NullPointerException e) {
		}
		try {
			Window.println("Mark brushes lump: "+markBrushes.length()+" bytes, "+markBrushes.size()+" items",Window.VERBOSITY_MAPSTATS);
			if(markBrushes.hasFunnySize()) {
				Window.println("WARNING: Funny lump size in Mark brushes",Window.VERBOSITY_WARNINGS);
			}
		} catch(java.lang.NullPointerException e) {
		}
		try {
			Window.println("Original Faces lump: "+originalFaces.length()+" bytes, "+originalFaces.size()+" items",Window.VERBOSITY_MAPSTATS);
			if(originalFaces.hasFunnySize()) {
				Window.println("WARNING: Funny lump size in Original Faces",Window.VERBOSITY_WARNINGS);
			}
		} catch(java.lang.NullPointerException e) {
		}
		try {
			Window.println("Texture index table lump: "+texTable.length()+" bytes, "+texTable.size()+" items",Window.VERBOSITY_MAPSTATS);
			if(texTable.hasFunnySize()) {
				Window.println("WARNING: Funny lump size in Texture index table",Window.VERBOSITY_WARNINGS);
			}
		} catch(java.lang.NullPointerException e) {
		}
		try {
			Window.println("Texture data lump: "+texDatas.length()+" bytes, "+texDatas.size()+" items",Window.VERBOSITY_MAPSTATS);
			if(texDatas.hasFunnySize()) {
				Window.println("WARNING: Funny lump size in Texture data",Window.VERBOSITY_WARNINGS);
			}
		} catch(java.lang.NullPointerException e) {
		}
		try {
			Window.println("Displacement info lump: "+dispInfos.length()+" bytes, "+dispInfos.size()+" items",Window.VERBOSITY_MAPSTATS);
			if(dispInfos.hasFunnySize()) {
				Window.println("WARNING: Funny lump size in Displacement info",Window.VERBOSITY_WARNINGS);
			}
		} catch(java.lang.NullPointerException e) {
		}
		try {
			Window.println("Displacement Vertices lump: "+dispVerts.length()+" bytes, "+dispVerts.size()+" items",Window.VERBOSITY_MAPSTATS);
			if(dispVerts.hasFunnySize()) {
				Window.println("WARNING: Funny lump size in Displacement Vertices",Window.VERBOSITY_WARNINGS);
			}
		} catch(java.lang.NullPointerException e) {
		}
		try {
			Window.println("Displacement Triangle Tags lump: "+displacementTriangles.length()+" bytes, "+displacementTriangles.size()+" items",Window.VERBOSITY_MAPSTATS);
			if(displacementTriangles.hasFunnySize()) {
				Window.println("WARNING: Funny lump size in Displacement Triangle Tags",Window.VERBOSITY_WARNINGS);
			}
		} catch(java.lang.NullPointerException e) {
		}
		try {
			Window.println("Static Props lump: "+staticProps.length()+" bytes, "+staticProps.size()+" items, "+staticProps.getDictionary().length+" unique models",Window.VERBOSITY_MAPSTATS);
		} catch(java.lang.NullPointerException e) {
		}
		try {
			Window.println("Cubemaps lump: "+cubemaps.length()+" bytes, "+cubemaps.size()+" items",Window.VERBOSITY_MAPSTATS);
			if(cubemaps.hasFunnySize()) {
				Window.println("WARNING: Funny lump size in Cubemaps",Window.VERBOSITY_WARNINGS);
			}
		} catch(java.lang.NullPointerException e) {
		}/*
		try {
			Window.println("Overlays lump: "+overlays.length()+" bytes, "+overlays.size()+" items",Window.VERBOSITY_MAPSTATS);
			if(overlays.hasFunnySize()) {
				Window.println("WARNING: Funny lump size in Overlays",Window.VERBOSITY_WARNINGS);
			}
		} catch(java.lang.NullPointerException e) {
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
		Stack nodestack = new Stack<Node>();
		nodestack.push(headNode);
 
		Node currentNode;

		while (!nodestack.empty()) {
			currentNode = (Node)nodestack.pop();
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
		for(int i=0;i<texDatas.size();i++) {
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
	
	public void setVersion(int in) {
		version=in;
	}
	
	public Entities getEntities() {
		return entities;
	}
	
	public void setEntities(byte[] data) throws java.lang.InterruptedException {
		entities=Entity.createLump(data);
	}
	
	public Lump<Plane> getPlanes() {
		return planes;
	}
	
	public void setPlanes(byte[] data) throws java.lang.InterruptedException {
		planes=Plane.createLump(data, version);
	}
	
	public Textures getTextures() {
		return textures;
	}
	
	public void setTextures(byte[] data) throws java.lang.InterruptedException {
		textures=Texture.createLump(data, version);
	}
	
	public Textures getMaterials() {
		return materials;
	}
	
	public void setMaterials(byte[] data) throws java.lang.InterruptedException {
		materials=Texture.createLump(data, version);
	}
	
	public Lump<Vertex> getVertices() {
		return vertices;
	}
	
	public void setVertices(byte[] data) throws java.lang.InterruptedException {
		vertices=Vertex.createLump(data, version);
	}
	
	public Lump<Node> getNodes() {
		return nodes;
	}
	
	public void setNodes(byte[] data) throws java.lang.InterruptedException {
		nodes=Node.createLump(data, version);
	}
	
	public Lump<TexInfo> getTexInfo() {
		return texInfo;
	}
	
	public void setTexInfo(byte[] data) throws java.lang.InterruptedException {
		texInfo=TexInfo.createLump(data, version);
	}
	
	public Lump<Face> getFaces() {
		return faces;
	}
	
	public void setFaces(byte[] data) throws java.lang.InterruptedException {
		faces=Face.createLump(data, version);
	}
	
	public Lump<Leaf> getLeaves() {
		return leaves;
	}
	
	public void setLeaves(byte[] data) throws java.lang.InterruptedException {
		leaves=Leaf.createLump(data, version);
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
	
	public Lump<Edge> getEdges() {
		return edges;
	}
	
	public void setEdges(byte[] data, int type) throws java.lang.InterruptedException {
		edges=Edge.createLump(data, type);
	}
	
	public NumList getSurfEdges() {
		return surfEdges;
	}
	
	public void setSurfEdges(byte[] data) throws java.lang.InterruptedException {
		switch(version) {
			case TYPE_QUAKE:
			case TYPE_QUAKE2:
			case TYPE_DAIKATANA:
			case TYPE_SIN:
			case TYPE_SOF:
			case TYPE_SOURCE17:
			case TYPE_SOURCE18:
			case TYPE_SOURCE19:
			case TYPE_SOURCE20:
			case TYPE_SOURCE21:
			case TYPE_SOURCE22:
			case TYPE_SOURCE23:
			case TYPE_TACTICALINTERVENTION:
			case TYPE_DMOMAM:
			case TYPE_VINDICTUS:
				surfEdges=new NumList(data, NumList.TYPE_INT);
				break;
		}
	}
	
	public Lump<Model> getModels() {
		return models;
	}
	
	public void setModels(byte[] data) throws java.lang.InterruptedException {
		models=Model.createLump(data, version);
	}
	
	public Lump<Brush> getBrushes() {
		return brushes;
	}
	
	public void setBrushes(byte[] data) throws java.lang.InterruptedException {
		brushes=Brush.createLump(data, version);
	}
	
	public Lump<BrushSide> getBrushSides() {
		return brushSides;
	}
	
	public void setBrushSides(byte[] data) throws java.lang.InterruptedException {
		brushSides=BrushSide.createLump(data, version);
	}
	
	public void setMarkBrushes(byte[] data) throws java.lang.InterruptedException {
		switch(version) {
			case TYPE_QUAKE2:
			case TYPE_SIN:
			case TYPE_SOF:
			case TYPE_DAIKATANA:
			case TYPE_SOURCE17:
			case TYPE_SOURCE18:
			case TYPE_SOURCE19:
			case TYPE_SOURCE20:
			case TYPE_SOURCE21:
			case TYPE_SOURCE22:
			case TYPE_SOURCE23:
			case TYPE_TACTICALINTERVENTION:
			case TYPE_DMOMAM:
				markBrushes=new NumList(data, NumList.TYPE_USHORT);
				break;
			case TYPE_NIGHTFIRE:
			case TYPE_VINDICTUS:
				markBrushes=new NumList(data, NumList.TYPE_UINT);
				break;
		}
	}
	
	public NumList getMarkBrushes() {
		return markBrushes;
	}
	
	public void setTexDatas(byte[] data) throws java.lang.InterruptedException {
		texDatas=SourceTexData.createLump(data);
	}
	
	public Lump<SourceTexData> getTexDatas() {
		return texDatas;
	}
	
	public void setDispInfos(byte[] data) throws java.lang.InterruptedException {
		dispInfos=SourceDispInfo.createLump(data, version);
	}
	
	public Lump<SourceDispInfo> getDispInfos() {
		return dispInfos;
	}
	
	public Lump<Face> getOriginalFaces() {
		return originalFaces;
	}
	
	public void setOriginalFaces(byte[] data) throws java.lang.InterruptedException {
		originalFaces=Face.createLump(data, version);
	}
	
	public void setDispVerts(byte[] data) throws java.lang.InterruptedException {
		dispVerts=SourceDispVertex.createLump(data);
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
		staticProps=SourceStaticProp.createLump(data, version, lumpVersion);
	}
	
	public SourceStaticProps getStaticProps() {
		return staticProps;
	}
	
	public void setCubemaps(byte[] data) throws java.lang.InterruptedException {
		cubemaps=SourceCubemap.createLump(data, version);
	}

	public Lump<SourceCubemap> getCubemaps() {
		return cubemaps;
	}
	/*
	public void setOverlays(byte[] data) throws java.lang.InterruptedException {
		overlays=new SourceOverlays(data, version);
	}
	
	public SourceOverlays getOverlays() {
		return overlays;
	}*/
	
	// INTERNAL CLASSES
	
	// NodeStack class

	// Contains a "stack" of Nodes. This aids greatly in the
	// traversal of a BSP tree without use of recursion.
	
	/*private class NodeStack {
		
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
	}*/
}