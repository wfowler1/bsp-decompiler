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
	// decompilation. Since this one never had a formal version, I'll make one up. 1
	// is the most correct version since it really was the first version...
	public static final int version=1;
	
	// Since all Doom engine maps were incorporated into the WAD, we need to keep
	// track of both the location of the WAD file and the internal name of the map.
	private String wadpath;
	private String mapName;
	
	// Each lump has its own class for handling its specific data structures.
	// These are the only lumps we need for decompilation.
	private DThings things;
	private DLinedefs linedefs;
	private DSidedefs sidedefs;
	private DVertices vertices;
	private DSegments segs;
	private DSubSectors subsectors;
	private DNodes nodes;
	private DSectors sectors;
	
	// CONSTRUCTORS
	// This accepts a folder path and looks for the BSP there.
	public DoomMap(String wadpath, String map) {
		this.wadpath=wadpath;
		if(map.charAt(0)=='E') { // ExMy format - 4 chars
			this.mapName=map.substring(0,4);
		} else { // MAPxx format - 5 chars
			this.mapName=map;
		}
	}

	// METHODS

	public void printBSPReport() {
		try {
			Window.println("Things lump: "+things.getLength()+" bytes, "+things.getNumElements()+" items",1);
		} catch(java.lang.NullPointerException e) {
			Window.println("Things not yet parsed!",1);
		}
		try {
			Window.println("Linedefs lump: "+linedefs.getLength()+" bytes, "+linedefs.getNumElements()+" items",1);
		} catch(java.lang.NullPointerException e) {
			Window.println("Linedefs not yet parsed!",1);
		}
		try {
			Window.println("Sidedefs lump: "+sidedefs.getLength()+" bytes, "+sidedefs.getNumElements()+" items",1);
		} catch(java.lang.NullPointerException e) {
			Window.println("Sidedefs not yet parsed!",1);
		}
		try {
			Window.println("Vertices lump: "+vertices.getLength()+" bytes, "+vertices.getNumElements()+" items",1);
		} catch(java.lang.NullPointerException e) {
			Window.println("Vertices not yet parsed!",1);
		}
		try {
			Window.println("Segments lump: "+segs.getLength()+" bytes, "+segs.getNumElements()+" items",1);
		} catch(java.lang.NullPointerException e) {
			Window.println("Segments not yet parsed!",1);
		}
		try {
			Window.println("Subsectors lump: "+subsectors.getLength()+" bytes, "+subsectors.getNumElements()+" items",1);
		} catch(java.lang.NullPointerException e) {
			Window.println("Subsectors not yet parsed!",1);
		}
		try {
			Window.println("Nodes lump: "+nodes.getLength()+" bytes, "+nodes.getNumElements()+" items",1);
		} catch(java.lang.NullPointerException e) {
			Window.println("Nodes not yet parsed!",1);
		}
		try {
			Window.println("Sectors lump: "+sectors.getLength()+" bytes, "+sectors.getNumElements()+" items",1);
		} catch(java.lang.NullPointerException e) {
			Window.println("Sectors not yet parsed!",1);
		}
	}
	
	// ACCESSORS/MUTATORS
	
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
	
	public void setThings(byte[] data) {
		things=new DThings(data);
	}
	
	public DThings getThings() {
		return things;
	}
	
	public void setLinedefs(byte[] data) {
		linedefs=new DLinedefs(data);
	}
	
	public DLinedefs getLinedefs() {
		return linedefs;
	}
	
	public void setSidedefs(byte[] data) {
		sidedefs=new DSidedefs(data);
	}
	
	public DSidedefs getSidedefs() {
		return sidedefs;
	}
	
	public void setVertices(byte[] data) {
		vertices=new DVertices(data);
	}
	
	public DVertices getVertices() {
		return vertices;
	}
	
	public void setSegments(byte[] data) {
		segs=new DSegments(data);
	}
	
	public DSegments getSegments() {
		return segs;
	}
	
	public void setSubSectors(byte[] data) {
		subsectors=new DSubSectors(data);
	}
	
	public DSubSectors getSubSectors() {
		return subsectors;
	}
	
	public void setNodes(byte[] data) {
		nodes=new DNodes(data);
	}
	
	public DNodes getNodes() {
		return nodes;
	}
	
	public void setSectors(byte[] data) {
		sectors=new DSectors(data);
	}
	
	public DSectors getSectors() {
		return sectors;
	}
}
