// BSPReader class

// Does the actual reading of the BSP file and takes appropriate
// action based primarily on BSP version number. It also feeds all
// appropriate data to the different BSP version classes. This
// does not actually do any data processing or analysis, it simply
// reads from the hard drive and sends the data where it needs to go.
// Deprecates the LS class, and doesn't create a file for every lump!

import java.io.File;
import java.io.FileInputStream;

public class BSPReader {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	private File BSP; // Where's my BSP?
	private String folder;
	// Both MoHAA and Source engines use different version numbering systems than the standard.
	private boolean source=false;
	private boolean mohaa=false;
	
	private boolean wad=false;
	
	private boolean toVMF;
	
	public final int OFFSET=0;
	public final int LENGTH=1;
	
	private int version=0;
	
	// Declare all kinds of BSPs here, the one actually used will be determined by constructor
	// protected BSPv29n30
	protected DoomMap[] doomMaps;
	protected v38BSP BSP38;
	protected v42BSP BSP42;
	protected v46BSP BSP46;
	// protected BSPv47
	// protected MOHAABSP
	// protected SourceBSPv20
	
	// CONSTRUCTORS
	
	// Takes a String in and assumes it is a path. That path is the path to the file
	// that is the BSP and its name minus the .BSP extension is assumed to be the folder.
	// See comments below for clarification. Case does not matter on the extension, so it
	// could be .BSP, .bsp, etc.
	public BSPReader(String in) {
		BSP=new File(in); // The read String points directly to the BSP file.
		if(!BSP.exists()) {
			Window.window.println("Unable to open source BSP file, please ensure the BSP exists.");
		} else {
			folder=BSP.getParent(); // The read string minus the .BSP is the lumps folder
			if(folder==null) {
				folder="";
			}
		}
	}
	
	public BSPReader(File in) {
		BSP=in;
		if(!BSP.exists()) {
			Window.window.println("Unable to open source BSP file, please ensure the BSP exists.");
		} else {
			folder=BSP.getParent(); // The read string minus the .BSP is the lumps folder
			if(folder==null) {
				folder="";
			}
		}
	}
	
	public BSPReader(File in, boolean toVMF) {
		BSP=in;
		this.toVMF=toVMF;
		if(!BSP.exists()) {
			Window.window.println("Unable to open source BSP file, please ensure the BSP exists.");
		} else {
			folder=BSP.getParent(); // The read string minus the .BSP is the lumps folder
			if(folder==null) {
				folder="";
			}
		}
	}
	
	// METHODS
	
	public void readBSP() {
		try {
			// Don't forget, Java uses BIG ENDIAN BYTE ORDER, so all numbers have to be read and written backwards.
			int version=getVersion();
			if(mohaa) {
				Window.window.println("Sorry, no MOHAA support (yet)!");
			} else {
				if(source) {
					Window.window.println("Sorry, no source map support (yet)!");
				} else {
					FileInputStream offsetReader;
					byte[] read=new byte[4];
					int offset;
					int length;
					switch(version) {
						case 1: // WAD file
							Window.window.println("WAD file found");
							offsetReader = new FileInputStream(BSP);
							offsetReader.skip(4); // Skip the file header, putting the reader into the length and offset of the directory
							
							doomMaps=new DoomMap[0];
							
							// Find the directory
							offsetReader.read(read); // Read 4 bytes
							int numLumps=DataReader.readInt(read[0], read[1], read[2], read[3]);
							offsetReader.read(read); // Read 4 more bytes
							int directoryOffset=DataReader.readInt(read[0], read[1], read[2], read[3]);
							
							FileInputStream directoryReader = new FileInputStream(BSP);
							directoryReader.skip(directoryOffset);
							
							byte[] readDirectory=new byte[16];
							
							// Read through the directory to find maps
							for(int i=0;i<numLumps;i++) {
								directoryReader.read(readDirectory);
								offset=DataReader.readInt(readDirectory[0], readDirectory[1], readDirectory[2], readDirectory[3]);
								length=DataReader.readInt(readDirectory[4], readDirectory[5], readDirectory[6], readDirectory[7]);
								String lumpName=new String(new byte[] { readDirectory[8], readDirectory[9], readDirectory[10], readDirectory[11], readDirectory[12], readDirectory[13], readDirectory[14], readDirectory[15] });
								if( length==0 && ( lumpName.substring(0,3).equalsIgnoreCase("MAP") || ( lumpName.charAt(0)=='E' && lumpName.charAt(2)=='M' ) ) ) {
									String mapName=lumpName.substring(0,5); // Map names are always ExMy or MAPxx. Never more than five chars.
									Window.window.println("Map: "+mapName);
									// All of this code updates the maplist with a new entry
									DoomMap[] newList=new DoomMap[doomMaps.length+1];
									for(int j=0;j<doomMaps.length;j++) {
										newList[j] = doomMaps[j];
									}
									newList[doomMaps.length]=new DoomMap(BSP.getPath(), mapName);
									doomMaps=newList;
									
									FileInputStream lumpReader = new FileInputStream(BSP);
									lumpReader.skip(directoryOffset+((i+1)*16));
									
									lumpReader.read(readDirectory);
									
									offset=DataReader.readInt(readDirectory[0], readDirectory[1], readDirectory[2], readDirectory[3]);
									length=DataReader.readInt(readDirectory[4], readDirectory[5], readDirectory[6], readDirectory[7]);
									lumpName=new String(new byte[] { readDirectory[8], readDirectory[9], readDirectory[10], readDirectory[11], readDirectory[12], readDirectory[13], readDirectory[14], readDirectory[15] });
									if(lumpName.substring(0,6).equalsIgnoreCase("THINGS")) {
										doomMaps[doomMaps.length-1].setThings(readLump(offset, length));
									}
									
									lumpReader.read(readDirectory);
									
									offset=DataReader.readInt(readDirectory[0], readDirectory[1], readDirectory[2], readDirectory[3]);
									length=DataReader.readInt(readDirectory[4], readDirectory[5], readDirectory[6], readDirectory[7]);
									lumpName=new String(new byte[] { readDirectory[8], readDirectory[9], readDirectory[10], readDirectory[11], readDirectory[12], readDirectory[13], readDirectory[14], readDirectory[15] });
									if(lumpName.equalsIgnoreCase("LINEDEFS")) {
										doomMaps[doomMaps.length-1].setLinedefs(readLump(offset, length));
									}
									
									lumpReader.read(readDirectory);
									
									offset=DataReader.readInt(readDirectory[0], readDirectory[1], readDirectory[2], readDirectory[3]);
									length=DataReader.readInt(readDirectory[4], readDirectory[5], readDirectory[6], readDirectory[7]);
									lumpName=new String(new byte[] { readDirectory[8], readDirectory[9], readDirectory[10], readDirectory[11], readDirectory[12], readDirectory[13], readDirectory[14], readDirectory[15] });
									if(lumpName.equalsIgnoreCase("SIDEDEFS")) {
										doomMaps[doomMaps.length-1].setSidedefs(readLump(offset, length));
									}
									
									lumpReader.read(readDirectory);
									
									offset=DataReader.readInt(readDirectory[0], readDirectory[1], readDirectory[2], readDirectory[3]);
									length=DataReader.readInt(readDirectory[4], readDirectory[5], readDirectory[6], readDirectory[7]);
									lumpName=new String(new byte[] { readDirectory[8], readDirectory[9], readDirectory[10], readDirectory[11], readDirectory[12], readDirectory[13], readDirectory[14], readDirectory[15] });
									if(lumpName.equalsIgnoreCase("VERTEXES")) {
										doomMaps[doomMaps.length-1].setVertices(readLump(offset, length));
									}
									
									lumpReader.read(readDirectory);
									
									offset=DataReader.readInt(readDirectory[0], readDirectory[1], readDirectory[2], readDirectory[3]);
									length=DataReader.readInt(readDirectory[4], readDirectory[5], readDirectory[6], readDirectory[7]);
									lumpName=new String(new byte[] { readDirectory[8], readDirectory[9], readDirectory[10], readDirectory[11], readDirectory[12], readDirectory[13], readDirectory[14], readDirectory[15] });
									if(lumpName.substring(0,4).equalsIgnoreCase("SEGS")) {
										doomMaps[doomMaps.length-1].setSegments(readLump(offset, length));
									}
									
									lumpReader.read(readDirectory);
									
									offset=DataReader.readInt(readDirectory[0], readDirectory[1], readDirectory[2], readDirectory[3]);
									length=DataReader.readInt(readDirectory[4], readDirectory[5], readDirectory[6], readDirectory[7]);
									lumpName=new String(new byte[] { readDirectory[8], readDirectory[9], readDirectory[10], readDirectory[11], readDirectory[12], readDirectory[13], readDirectory[14], readDirectory[15] });
									if(lumpName.equalsIgnoreCase("SSECTORS")) {
										doomMaps[doomMaps.length-1].setSubSectors(readLump(offset, length));
									}
									
									lumpReader.read(readDirectory);
									
									offset=DataReader.readInt(readDirectory[0], readDirectory[1], readDirectory[2], readDirectory[3]);
									length=DataReader.readInt(readDirectory[4], readDirectory[5], readDirectory[6], readDirectory[7]);
									lumpName=new String(new byte[] { readDirectory[8], readDirectory[9], readDirectory[10], readDirectory[11], readDirectory[12], readDirectory[13], readDirectory[14], readDirectory[15] });
									if(lumpName.substring(0,5).equalsIgnoreCase("NODES")) {
										doomMaps[doomMaps.length-1].setNodes(readLump(offset, length));
									}
									
									lumpReader.read(readDirectory);
									
									offset=DataReader.readInt(readDirectory[0], readDirectory[1], readDirectory[2], readDirectory[3]);
									length=DataReader.readInt(readDirectory[4], readDirectory[5], readDirectory[6], readDirectory[7]);
									lumpName=new String(new byte[] { readDirectory[8], readDirectory[9], readDirectory[10], readDirectory[11], readDirectory[12], readDirectory[13], readDirectory[14], readDirectory[15] });
									if(lumpName.substring(0,7).equalsIgnoreCase("SECTORS")) {
										doomMaps[doomMaps.length-1].setSectors(readLump(offset, length));
									}
							
									lumpReader.close();
							
									doomMaps[doomMaps.length-1].printBSPReport();
									
									Window.window.addJob(null, doomMaps[doomMaps.length-1], toVMF);
								}
							}
							
							directoryReader.close();
							offsetReader.close();
						break;
						case 29: // Quake
						case 30: // Half-life
							Window.window.println("Sorry, no Quake/Half-life support (yet)!");
							break;
						case 38: // Quake 2
							Window.window.println("BSP v38 found (Quake 2)");
							offsetReader = new FileInputStream(BSP);
							BSP38 = new v38BSP(BSP.getPath());
							offsetReader.skip(8); // Skip the file header, putting the reader into the offset/length pairs
							
							// Lump 00
							offsetReader.read(read); // Read 4 bytes
							offset=(read[3] << 24) | ((read[2] & 0xff) << 16) | ((read[1] & 0xff) << 8) | (read[0] & 0xff);
							offsetReader.read(read); // Read 4 more bytes
							length=(read[3] << 24) | ((read[2] & 0xff) << 16) | ((read[1] & 0xff) << 8) | (read[0] & 0xff);
							BSP38.setEntities(readLump(offset, length));
							
							// Lump 01
							offsetReader.read(read); // Read 4 bytes
							offset=(read[3] << 24) | ((read[2] & 0xff) << 16) | ((read[1] & 0xff) << 8) | (read[0] & 0xff);
							offsetReader.read(read); // Read 4 more bytes
							length=(read[3] << 24) | ((read[2] & 0xff) << 16) | ((read[1] & 0xff) << 8) | (read[0] & 0xff);
							BSP38.setPlanes(readLump(offset, length));
							
							// Lump 02
							offsetReader.read(read); // Read 4 bytes
							offset=(read[3] << 24) | ((read[2] & 0xff) << 16) | ((read[1] & 0xff) << 8) | (read[0] & 0xff);
							offsetReader.read(read); // Read 4 more bytes
							length=(read[3] << 24) | ((read[2] & 0xff) << 16) | ((read[1] & 0xff) << 8) | (read[0] & 0xff);
							BSP38.setVertices(readLump(offset, length));
							
							offsetReader.skip(8); // Do not need offset/length for lump 3
							
							// Lump 04
							offsetReader.read(read); // Read 4 bytes
							offset=(read[3] << 24) | ((read[2] & 0xff) << 16) | ((read[1] & 0xff) << 8) | (read[0] & 0xff);
							offsetReader.read(read); // Read 4 more bytes
							length=(read[3] << 24) | ((read[2] & 0xff) << 16) | ((read[1] & 0xff) << 8) | (read[0] & 0xff);
							BSP38.setNodes(readLump(offset, length));
							
							// Lump 05
							offsetReader.read(read); // Read 4 bytes
							offset=(read[3] << 24) | ((read[2] & 0xff) << 16) | ((read[1] & 0xff) << 8) | (read[0] & 0xff);
							offsetReader.read(read); // Read 4 more bytes
							length=(read[3] << 24) | ((read[2] & 0xff) << 16) | ((read[1] & 0xff) << 8) | (read[0] & 0xff);
							BSP38.setTextures(readLump(offset, length));
							
							// Lump 06
							offsetReader.read(read); // Read 4 bytes
							offset=(read[3] << 24) | ((read[2] & 0xff) << 16) | ((read[1] & 0xff) << 8) | (read[0] & 0xff);
							offsetReader.read(read); // Read 4 more bytes
							length=(read[3] << 24) | ((read[2] & 0xff) << 16) | ((read[1] & 0xff) << 8) | (read[0] & 0xff);
							BSP38.setFaces(readLump(offset, length));
							
							offsetReader.skip(8); // Do not need offset/length for lumps 7
							
							// Lump 08
							offsetReader.read(read); // Read 4 bytes
							offset=(read[3] << 24) | ((read[2] & 0xff) << 16) | ((read[1] & 0xff) << 8) | (read[0] & 0xff);
							offsetReader.read(read); // Read 4 more bytes
							length=(read[3] << 24) | ((read[2] & 0xff) << 16) | ((read[1] & 0xff) << 8) | (read[0] & 0xff);
							BSP38.setLeaves(readLump(offset, length));
							
							offsetReader.skip(8); // Do not need offset/length for lumps 9
							
							// Lump 10
							offsetReader.read(read); // Read 4 bytes
							offset=(read[3] << 24) | ((read[2] & 0xff) << 16) | ((read[1] & 0xff) << 8) | (read[0] & 0xff);
							offsetReader.read(read); // Read 4 more bytes
							length=(read[3] << 24) | ((read[2] & 0xff) << 16) | ((read[1] & 0xff) << 8) | (read[0] & 0xff);
							BSP38.setMarkBrushes(readLump(offset, length));
							
							// Lump 11
							offsetReader.read(read); // Read 4 bytes
							offset=(read[3] << 24) | ((read[2] & 0xff) << 16) | ((read[1] & 0xff) << 8) | (read[0] & 0xff);
							offsetReader.read(read); // Read 4 more bytes
							length=(read[3] << 24) | ((read[2] & 0xff) << 16) | ((read[1] & 0xff) << 8) | (read[0] & 0xff);
							BSP38.setEdges(readLump(offset, length));
							
							// Lump 12
							offsetReader.read(read); // Read 4 bytes
							offset=(read[3] << 24) | ((read[2] & 0xff) << 16) | ((read[1] & 0xff) << 8) | (read[0] & 0xff);
							offsetReader.read(read); // Read 4 more bytes
							length=(read[3] << 24) | ((read[2] & 0xff) << 16) | ((read[1] & 0xff) << 8) | (read[0] & 0xff);
							BSP38.setMarkEdges(readLump(offset, length));
							
							// Lump 13
							offsetReader.read(read); // Read 4 bytes
							offset=(read[3] << 24) | ((read[2] & 0xff) << 16) | ((read[1] & 0xff) << 8) | (read[0] & 0xff);
							offsetReader.read(read); // Read 4 more bytes
							length=(read[3] << 24) | ((read[2] & 0xff) << 16) | ((read[1] & 0xff) << 8) | (read[0] & 0xff);
							BSP38.setModels(readLump(offset, length));
							
							// Lump 14
							offsetReader.read(read); // Read 4 bytes
							offset=(read[3] << 24) | ((read[2] & 0xff) << 16) | ((read[1] & 0xff) << 8) | (read[0] & 0xff);
							offsetReader.read(read); // Read 4 more bytes
							length=(read[3] << 24) | ((read[2] & 0xff) << 16) | ((read[1] & 0xff) << 8) | (read[0] & 0xff);
							BSP38.setBrushes(readLump(offset, length));
							
							// Lump 15
							offsetReader.read(read); // Read 4 bytes
							offset=(read[3] << 24) | ((read[2] & 0xff) << 16) | ((read[1] & 0xff) << 8) | (read[0] & 0xff);
							offsetReader.read(read); // Read 4 more bytes
							length=(read[3] << 24) | ((read[2] & 0xff) << 16) | ((read[1] & 0xff) << 8) | (read[0] & 0xff);
							BSP38.setBrushSides(readLump(offset, length));
							/*
							offsetReader.skip(16); // Do not need offset/length for lumps 16 or 17
							
							// Lump 18
							offsetReader.read(read); // Read 4 bytes
							offset=(read[3] << 24) | ((read[2] & 0xff) << 16) | ((read[1] & 0xff) << 8) | (read[0] & 0xff);
							offsetReader.read(read); // Read 4 more bytes
							length=(read[3] << 24) | ((read[2] & 0xff) << 16) | ((read[1] & 0xff) << 8) | (read[0] & 0xff);
							BSP38.setAreaPortals(readLump(offset, length));*/
							
							offsetReader.close();
							
							BSP38.printBSPReport();
							break;
						case 42: // JBN
							Window.window.println("BSP v42 found (Nightfire)");
							offsetReader = new FileInputStream(BSP);
							BSP42 = new v42BSP(BSP.getPath());
							offsetReader.skip(4); // Skip the file header, putting the reader into the offset/length pairs
							
							// Lump 00
							offsetReader.read(read); // Read 4 bytes
							offset=(read[3] << 24) | ((read[2] & 0xff) << 16) | ((read[1] & 0xff) << 8) | (read[0] & 0xff);
							offsetReader.read(read); // Read 4 more bytes
							length=(read[3] << 24) | ((read[2] & 0xff) << 16) | ((read[1] & 0xff) << 8) | (read[0] & 0xff);
							BSP42.setEntities(readLump(offset, length));
							
							// Lump 01
							offsetReader.read(read); // Read 4 bytes
							offset=(read[3] << 24) | ((read[2] & 0xff) << 16) | ((read[1] & 0xff) << 8) | (read[0] & 0xff);
							offsetReader.read(read); // Read 4 more bytes
							length=(read[3] << 24) | ((read[2] & 0xff) << 16) | ((read[1] & 0xff) << 8) | (read[0] & 0xff);
							BSP42.setPlanes(readLump(offset, length));
							
							// Lump 02
							offsetReader.read(read); // Read 4 bytes
							offset=(read[3] << 24) | ((read[2] & 0xff) << 16) | ((read[1] & 0xff) << 8) | (read[0] & 0xff);
							offsetReader.read(read); // Read 4 more bytes
							length=(read[3] << 24) | ((read[2] & 0xff) << 16) | ((read[1] & 0xff) << 8) | (read[0] & 0xff);
							BSP42.setTextures(readLump(offset, length));
							
							// Lump 03
							offsetReader.read(read); // Read 4 bytes
							offset=(read[3] << 24) | ((read[2] & 0xff) << 16) | ((read[1] & 0xff) << 8) | (read[0] & 0xff);
							offsetReader.read(read); // Read 4 more bytes
							length=(read[3] << 24) | ((read[2] & 0xff) << 16) | ((read[1] & 0xff) << 8) | (read[0] & 0xff);
							BSP42.setMaterials(readLump(offset, length));
							
							// Lump 04
							offsetReader.read(read); // Read 4 bytes
							offset=(read[3] << 24) | ((read[2] & 0xff) << 16) | ((read[1] & 0xff) << 8) | (read[0] & 0xff);
							offsetReader.read(read); // Read 4 more bytes
							length=(read[3] << 24) | ((read[2] & 0xff) << 16) | ((read[1] & 0xff) << 8) | (read[0] & 0xff);
							BSP42.setVertices(readLump(offset, length));
							
							offsetReader.skip(32); // Do not need offset/length for lumps 5-8
							
							// Lump 09
							offsetReader.read(read); // Read 4 bytes
							offset=(read[3] << 24) | ((read[2] & 0xff) << 16) | ((read[1] & 0xff) << 8) | (read[0] & 0xff);
							offsetReader.read(read); // Read 4 more bytes
							length=(read[3] << 24) | ((read[2] & 0xff) << 16) | ((read[1] & 0xff) << 8) | (read[0] & 0xff);
							BSP42.setFaces(readLump(offset, length));
							
							offsetReader.skip(8); // Do not need offset/length for lump 10
							
							// Lump 11
							offsetReader.read(read); // Read 4 bytes
							offset=(read[3] << 24) | ((read[2] & 0xff) << 16) | ((read[1] & 0xff) << 8) | (read[0] & 0xff);
							offsetReader.read(read); // Read 4 more bytes
							length=(read[3] << 24) | ((read[2] & 0xff) << 16) | ((read[1] & 0xff) << 8) | (read[0] & 0xff);
							BSP42.setLeaves(readLump(offset, length));
							
							offsetReader.skip(8); // Do not need offset/length for lump 12
							
							// Lump 13
							offsetReader.read(read); // Read 4 bytes
							offset=(read[3] << 24) | ((read[2] & 0xff) << 16) | ((read[1] & 0xff) << 8) | (read[0] & 0xff);
							offsetReader.read(read); // Read 4 more bytes
							length=(read[3] << 24) | ((read[2] & 0xff) << 16) | ((read[1] & 0xff) << 8) | (read[0] & 0xff);
							BSP42.setMarkBrushes(readLump(offset, length));
							
							// Lump 14
							offsetReader.read(read); // Read 4 bytes
							offset=(read[3] << 24) | ((read[2] & 0xff) << 16) | ((read[1] & 0xff) << 8) | (read[0] & 0xff);
							offsetReader.read(read); // Read 4 more bytes
							length=(read[3] << 24) | ((read[2] & 0xff) << 16) | ((read[1] & 0xff) << 8) | (read[0] & 0xff);
							BSP42.setModels(readLump(offset, length));
							
							// Lump 15
							offsetReader.read(read); // Read 4 bytes
							offset=(read[3] << 24) | ((read[2] & 0xff) << 16) | ((read[1] & 0xff) << 8) | (read[0] & 0xff);
							offsetReader.read(read); // Read 4 more bytes
							length=(read[3] << 24) | ((read[2] & 0xff) << 16) | ((read[1] & 0xff) << 8) | (read[0] & 0xff);
							BSP42.setBrushes(readLump(offset, length));
							
							// Lump 16
							offsetReader.read(read); // Read 4 bytes
							offset=(read[3] << 24) | ((read[2] & 0xff) << 16) | ((read[1] & 0xff) << 8) | (read[0] & 0xff);
							offsetReader.read(read); // Read 4 more bytes
							length=(read[3] << 24) | ((read[2] & 0xff) << 16) | ((read[1] & 0xff) << 8) | (read[0] & 0xff);
							BSP42.setBrushSides(readLump(offset, length));
							
							// Lump 17
							offsetReader.read(read); // Read 4 bytes
							offset=(read[3] << 24) | ((read[2] & 0xff) << 16) | ((read[1] & 0xff) << 8) | (read[0] & 0xff);
							offsetReader.read(read); // Read 4 more bytes
							length=(read[3] << 24) | ((read[2] & 0xff) << 16) | ((read[1] & 0xff) << 8) | (read[0] & 0xff);
							BSP42.setTextureMatrices(readLump(offset, length));
							
							offsetReader.close();
							
							BSP42.printBSPReport();
							break;
						case 46: // Quake 3/close derivative
							Window.window.println("BSP v46 found (id Tech 3)");
							offsetReader = new FileInputStream(BSP);
							BSP46 = new v46BSP(BSP.getPath());
							offsetReader.skip(8); // Skip the file header, putting the reader into the offset/length pairs
							
							// Lump 00
							offsetReader.read(read); // Read 4 bytes
							offset=(read[3] << 24) | ((read[2] & 0xff) << 16) | ((read[1] & 0xff) << 8) | (read[0] & 0xff);
							offsetReader.read(read); // Read 4 more bytes
							length=(read[3] << 24) | ((read[2] & 0xff) << 16) | ((read[1] & 0xff) << 8) | (read[0] & 0xff);
							BSP46.setEntities(readLump(offset, length));
							
							// Lump 01
							offsetReader.read(read); // Read 4 bytes
							offset=(read[3] << 24) | ((read[2] & 0xff) << 16) | ((read[1] & 0xff) << 8) | (read[0] & 0xff);
							offsetReader.read(read); // Read 4 more bytes
							length=(read[3] << 24) | ((read[2] & 0xff) << 16) | ((read[1] & 0xff) << 8) | (read[0] & 0xff);
							BSP46.setTextures(readLump(offset, length));
							
							// Lump 02
							offsetReader.read(read); // Read 4 bytes
							offset=(read[3] << 24) | ((read[2] & 0xff) << 16) | ((read[1] & 0xff) << 8) | (read[0] & 0xff);
							offsetReader.read(read); // Read 4 more bytes
							length=(read[3] << 24) | ((read[2] & 0xff) << 16) | ((read[1] & 0xff) << 8) | (read[0] & 0xff);
							BSP46.setPlanes(readLump(offset, length));
							
							offsetReader.skip(32); // Do not need offset/length for lumps 3-6
							
							// Lump 07
							offsetReader.read(read); // Read 4 bytes
							offset=(read[3] << 24) | ((read[2] & 0xff) << 16) | ((read[1] & 0xff) << 8) | (read[0] & 0xff);
							offsetReader.read(read); // Read 4 more bytes
							length=(read[3] << 24) | ((read[2] & 0xff) << 16) | ((read[1] & 0xff) << 8) | (read[0] & 0xff);
							BSP46.setModels(readLump(offset, length));
							
							// Lump 08
							offsetReader.read(read); // Read 4 bytes
							offset=(read[3] << 24) | ((read[2] & 0xff) << 16) | ((read[1] & 0xff) << 8) | (read[0] & 0xff);
							offsetReader.read(read); // Read 4 more bytes
							length=(read[3] << 24) | ((read[2] & 0xff) << 16) | ((read[1] & 0xff) << 8) | (read[0] & 0xff);
							BSP46.setBrushes(readLump(offset, length));
							
							// Lump 09
							offsetReader.read(read); // Read 4 bytes
							offset=(read[3] << 24) | ((read[2] & 0xff) << 16) | ((read[1] & 0xff) << 8) | (read[0] & 0xff);
							offsetReader.read(read); // Read 4 more bytes
							length=(read[3] << 24) | ((read[2] & 0xff) << 16) | ((read[1] & 0xff) << 8) | (read[0] & 0xff);
							BSP46.setBrushSides(readLump(offset, length));
							
							// Lump 10
							offsetReader.read(read); // Read 4 bytes
							offset=(read[3] << 24) | ((read[2] & 0xff) << 16) | ((read[1] & 0xff) << 8) | (read[0] & 0xff);
							offsetReader.read(read); // Read 4 more bytes
							length=(read[3] << 24) | ((read[2] & 0xff) << 16) | ((read[1] & 0xff) << 8) | (read[0] & 0xff);
							BSP46.setVertices(readLump(offset, length));
							
							offsetReader.skip(16); // Do not need offset/length for lumps 11 and 12
							
							// Lump 13
							offsetReader.read(read); // Read 4 bytes
							offset=(read[3] << 24) | ((read[2] & 0xff) << 16) | ((read[1] & 0xff) << 8) | (read[0] & 0xff);
							offsetReader.read(read); // Read 4 more bytes
							length=(read[3] << 24) | ((read[2] & 0xff) << 16) | ((read[1] & 0xff) << 8) | (read[0] & 0xff);
							BSP46.setFaces(readLump(offset, length));
							
							offsetReader.close();
							
							BSP46.printBSPReport();
							
							break;
						case 47: // RTC Wolfenstein, I believe it's almost identical to Q3
							Window.window.println("Sorry, no Wolfenstein support (yet)!");
							break;
						default:
							Window.window.println("I don't know what kind of BSP this is! Please post an issue on the bug tracker!");
					}
				}
			}
		} catch(java.io.IOException e) {
			Window.window.println("Unable to access BSP file! Is it open in another program?");
		}
	}
	
	// +readLump(int, int)
	// Reads the lump length bytes long at offset in the file
	public byte[] readLump(int offset, int length) {
		byte[] input=new byte[length];
		try {
			FileInputStream fileReader=new FileInputStream(BSP);
			fileReader.skip(offset);
			fileReader.read(input);
			fileReader.close();
		} catch(java.io.IOException e) {
			Window.window.println("Unknown error reading BSP, it was working before!");
		}
		return input;
	}
				
	// ACCESSORS/MUTATORS
	public int getVersion() throws java.io.IOException {
		if(version==0) {
			byte[] read=new byte[4];
			FileInputStream versionNumberReader=new FileInputStream(BSP); // This filestream will be used to read version number only
			versionNumberReader.read(read);
			int in=(read[3] << 24) | ((read[2] & 0xff) << 16) | ((read[1] & 0xff) << 8) | (read[0] & 0xff);
			if(in == 1347633737) { // 1347633737 reads in ASCII as "IBSP"
				versionNumberReader.read(read);
				version=(read[3] << 24) | ((read[2] & 0xff) << 16) | ((read[1] & 0xff) << 8) | (read[0] & 0xff);
			} else {
				if(in == 892416050) { // 892416050 reads in ASCII as "2015," the game studio which developed MoHAA
					mohaa=true;
					versionNumberReader.read(read);
					version=(read[3] << 24) | ((read[2] & 0xff) << 16) | ((read[1] & 0xff) << 8) | (read[0] & 0xff); // Should be 19
				} else {
				if(in == 1095516485) { // 1095516485 reads in ASCII as "EALA," the ones who developed MoHAA Spearhead and Breakthrough
						mohaa=true;
						versionNumberReader.read(read);
						version=(read[3] << 24) | ((read[2] & 0xff) << 16) | ((read[1] & 0xff) << 8) | (read[0] & 0xff); // Should be 21
					} else {
						if(in == 1347633750) { // 1347633750 reads in ASCII as "VBSP." Indicates Source engine.
							source=true;
							versionNumberReader.read(read);
							version=(read[3] << 24) | ((read[2] & 0xff) << 16) | ((read[1] & 0xff) << 8) | (read[0] & 0xff);
						} else {
							if(in == 1145132873 || in == 1145132880) { // "IWAD" or "PWAD"
								wad=true;
								version=1;
							} else {
								version=in;
							}
						}
					}
				}
			}
		}
		return version;
	}
}
