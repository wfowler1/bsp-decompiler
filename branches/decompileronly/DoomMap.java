// DoomMap class
// This class gathers all relevant information from the lumps of a Doom Map.
// I don't know if I can call this a BSP, though. It's more of a Binary Area
// Partition, a BAP.
// Anyhow, it never had a formal BSP version number, nor was it ever referred
// to as a BSP, so it's DoomMap.

import java.io.File;

public class DoomMap {

	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	// This is the version of the BSP. This will determine the lumps order and aid in
	// decompilation. Since this one never had a formal version, I'll make one up.
	public static final int TYPE_DOOM=1145132868; // "DWAD"
	public static final int TYPE_HEXEN=1145132872; // "HWAD"
	
	// Since all Doom engine maps were incorporated into the WAD, we need to keep
	// track of both the location of the WAD file and the internal name of the map.
	private String wadpath;
	private String mapName;
	private int version;
	
	// Each lump has its own class for handling its specific data structures.
	// These are the only lumps we need for decompilation.
	private Lump<DThing> things;
	private Lump<DLinedef> linedefs;
	private Lump<DSidedef> sidedefs;
	private Lump<Vertex> vertices;
	private Lump<DSegment> segs;
	private Lump<DSubSector> subsectors;
	private Lump<DNode> nodes;
	private Lump<DSector> sectors;
	
	// CONSTRUCTORS
	// This accepts a folder path and looks for the BSP there.
	public DoomMap(String wadpath, String map) {
		this.wadpath=wadpath;
		this.mapName=map;
	}

	// METHODS

	public void printBSPReport() {
		try {
			Window.println("Things lump: "+things.length()+" bytes, "+things.size()+" items",Window.VERBOSITY_MAPSTATS);
			if(things.hasFunnySize()) {
				Window.println("WARNING: Funny lump size in Things",Window.VERBOSITY_WARNINGS);
			}
		} catch(java.lang.NullPointerException e) {
		}
		try {
			Window.println("Linedefs lump: "+linedefs.length()+" bytes, "+linedefs.size()+" items",Window.VERBOSITY_MAPSTATS);
			if(linedefs.hasFunnySize()) {
				Window.println("WARNING: Funny lump size in Linedefs",Window.VERBOSITY_WARNINGS);
			}
		} catch(java.lang.NullPointerException e) {
		}
		try {
			Window.println("Sizedefs lump: "+sidedefs.length()+" bytes, "+sidedefs.size()+" items",Window.VERBOSITY_MAPSTATS);
			if(sidedefs.hasFunnySize()) {
				Window.println("WARNING: Funny lump size in Sidedefs",Window.VERBOSITY_WARNINGS);
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
			Window.println("Segments lump: "+segs.length()+" bytes, "+segs.size()+" items",Window.VERBOSITY_MAPSTATS);
			if(segs.hasFunnySize()) {
				Window.println("WARNING: Funny lump size in Segments",Window.VERBOSITY_WARNINGS);
			}
		} catch(java.lang.NullPointerException e) {
		}
		try {
			Window.println("Subsectors lump: "+subsectors.length()+" bytes, "+subsectors.size()+" items",Window.VERBOSITY_MAPSTATS);
			if(subsectors.hasFunnySize()) {
				Window.println("WARNING: Funny lump size in Subsectors",Window.VERBOSITY_WARNINGS);
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
			Window.println("Sectors lump: "+sectors.length()+" bytes, "+sectors.size()+" items",Window.VERBOSITY_MAPSTATS);
			if(sectors.hasFunnySize()) {
				Window.println("WARNING: Funny lump size in Sectors",Window.VERBOSITY_WARNINGS);
			}
		} catch(java.lang.NullPointerException e) {
		}
	}
	
	// ACCESSORS/MUTATORS
	
	public int getVersion() {
		return version;
	}
	
	public String getPath() {
		return wadpath;
	}
	
	public String getMapName() {
		return mapName;
	}
	
	public String getFolder() {
		int i;
		for(i=0;i<wadpath.length();i++) {
			if(wadpath.charAt(wadpath.length()-1-i)=='\\') {
				break;
			}
			if(wadpath.charAt(wadpath.length()-1-i)=='/') {
				break;
			}
		}
		return wadpath.substring(0,wadpath.length()-i);
	}
	
	public String getWadName() {
		File newFile=new File(wadpath);
		return newFile.getName().substring(0, newFile.getName().length()-4);
	}
	
	public void setThings(byte[] data, int type) throws java.lang.InterruptedException {
		if(version==0) {
			version=type;
		}
		things=DThing.createLump(data, type);
	}
	
	public Lump<DThing> getThings() {
		return things;
	}
	
	public void setLinedefs(byte[] data, int type) throws java.lang.InterruptedException {
		if(version==0) {
			version=type;
		}
		linedefs=DLinedef.createLump(data, type);
	}
	
	public Lump<DLinedef> getLinedefs() {
		return linedefs;
	}
	
	public void setSidedefs(byte[] data) throws java.lang.InterruptedException {
		sidedefs=DSidedef.createLump(data);
	}
	
	public Lump<DSidedef> getSidedefs() {
		return sidedefs;
	}
	
	public void setVertices(byte[] data) throws java.lang.InterruptedException {
		vertices=Vertex.createLump(data, TYPE_DOOM);
	}
	
	public Lump<Vertex> getVertices() {
		return vertices;
	}
	
	public void setSegments(byte[] data) throws java.lang.InterruptedException {
		segs=DSegment.createLump(data);
	}
	
	public Lump<DSegment> getSegments() {
		return segs;
	}
	
	public void setSubSectors(byte[] data) throws java.lang.InterruptedException {
		subsectors=DSubSector.createLump(data);
	}
	
	public Lump<DSubSector> getSubSectors() {
		return subsectors;
	}
	
	public void setNodes(byte[] data) throws java.lang.InterruptedException {
		nodes=DNode.createLump(data);
	}
	
	public Lump<DNode> getNodes() {
		return nodes;
	}
	
	public void setSectors(byte[] data) throws java.lang.InterruptedException {
		sectors=DSector.createLump(data);
	}
	
	public Lump<DSector> getSectors() {
		return sectors;
	}
}
