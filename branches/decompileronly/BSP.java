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
	// public static final int TYPE_COD2=1347633741; // Uses same algorithm and structures as COD1
	public static final int TYPE_SIN=1347633747; // The headers for SiN and Jedi Outcast are exactly the same
	public static final int TYPE_RAVEN=1347633748;
	// public static final int TYPE_COD4=1347633759; // Uses same algorithm and structures as COD1
	public static final int TYPE_SOURCE17=1347633767;
	public static final int TYPE_SOURCE18=1347633768;
	public static final int TYPE_SOURCE19=1347633769;
	public static final int TYPE_SOURCE20=1347633770;
	public static final int TYPE_SOURCE21=1347633771;
	public static final int TYPE_SOURCE22=1347633772;
	public static final int TYPE_SOURCE23=1347633773;
	public static final int TYPE_QUAKE2=1347633775;
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
	private ShortList markSurfaces;
	private Edges edges;
	private IntList surfEdges;
	private Models models;
	// Quake 3
	private Brushes brushes;
	private BrushSides brushSides;
	// MOHAA
	//private MoHAAStaticProps staticProps;
	
	// CONSTRUCTORS
	public BSP(String filePath, int version) {
		this.filePath=filePath;
		this.version=version;
	}
	
	// METHODS
	
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
	
	public void setEntities(byte[] data) {
		entities=new Entities(data);
	}
	
	public Planes getPlanes() {
		return planes;
	}
	
	public void setPlanes(byte[] data) {
		planes=new Planes(data, version);
	}
	
	public Textures getTextures() {
		return textures;
	}
	
	public void setTextures(byte[] data) {
		textures=new Textures(data, version);
	}
	
	public Vertices getVertices() {
		return vertices;
	}
	
	public void setVertices(byte[] data) {
		vertices=new Vertices(data, version);
	}
	
	public Nodes getNodes() {
		return nodes;
	}
	
	public void setNodes(byte[] data) {
		nodes=new Nodes(data, version);
	}
	
	public TexInfos getTexInfo() {
		return texInfo;
	}
	
	public void setTexInfo(byte[] data) {
		texInfo=new TexInfos(data, version);
	}
	
	public Faces getFaces() {
		return faces;
	}
	
	public void setFaces(byte[] data) {
		faces=new Faces(data, version);
	}
	
	public Leaves getLeaves() {
		return leaves;
	}
	
	public void setLeaves(byte[] data) {
		leaves=new Leaves(data, version);
	}
	
	public ShortList getMarkSurfaces() {
		return markSurfaces;
	}
	
	public void setMarkSurfaces(byte[] data) {
		markSurfaces=new ShortList(data);
	}
	
	public Edges getEdges() {
		return edges;
	}
	
	public void setEdges(byte[] data) {
		edges=new Edges(data);
	}
	
	public IntList getSurfEdges() {
		return surfEdges;
	}
	
	public void setSurfEdges(byte[] data) {
		surfEdges=new IntList(data);
	}
	
	public Models getModels() {
		return models;
	}
	
	public void setModels(byte[] data) {
		models=new Models(data, version);
	}
	
	public Brushes getBrushes() {
		return brushes;
	}
	
	public void setBrushes(byte[] data) {
		brushes=new Brushes(data, version);
	}
	
	public BrushSides getBrushSides() {
		return brushSides;
	}
	
	public void setBrushSides(byte[] data) {
		brushSides=new BrushSides(data, version);
	}
}