// BSPReader class

// Does the actual reading of the BSP file and takes appropriate
// action based primarily on BSP version number. It also feeds all
// appropriate data to the different BSP version classes. This
// does not actually do any data processing or analysis, it simply
// reads from the hard drive and sends the data where it needs to go.
// Deprecates the LS class, and doesn't create a file for every lump!

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Date;

public class BSPReader {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	private File BSPFile; // Where's my BSP?
	private String folder;
	
	public static final int OFFSET=0;
	public static final int LENGTH=1;
	// These are only used in Source BSPs, which have a lot of different structures
	public static final int LUMPVERSION=2;
	public static final int FOURCC=3;
	
	private int version=0;
	private int version2=0;
	private int readAs=-1; // An override for version number detection, in case the user wishes to use a specific algorithm
	
	private boolean wad=false;
	
	// Declare all kinds of BSPs here, the one actually used will be determined by constructor
	private DoomMap[] doomMaps;
	private BSP BSPObject;
	
	// CONSTRUCTORS
	
	// Takes a String in and assumes it is a path. That path is the path to the file
	// that is the BSP and its name minus the .BSP extension is assumed to be the folder.
	// See comments below for clarification. Case does not matter on the extension, so it
	// could be .BSP, .bsp, etc.
	
	public BSPReader(String in, int readAs) {
		new BSPReader(new File(in), readAs);
	}
	
	public BSPReader(File in, int readAs) {
		this.readAs=readAs;
		BSPFile=in;
		if(!BSPFile.exists()) {
			Window.println("Unable to open BSP file; file not found.",Window.VERBOSITY_ALWAYS);
		} else {
			folder=BSPFile.getParent(); // The read string minus the .BSP is the lumps folder
			if(folder==null) {
				folder="";
			}
		}
	}
	
	// METHODS
	
	public void readBSP() throws java.lang.InterruptedException {
		try {
			version=getVersion();
			byte[] read=new byte[4];
			int offset;
			int length;
			FileInputStream offsetReader = new FileInputStream(BSPFile);
			if(wad) {
				Window.println("WAD file found",Window.VERBOSITY_ALWAYS);
				offsetReader.skip(4); // Skip the file header, putting the reader into the length and offset of the directory
				
				doomMaps=new DoomMap[0];
				
				// Find the directory
				offsetReader.read(read); // Read 4 bytes
				int numLumps=DataReader.readInt(read[0], read[1], read[2], read[3]);
				offsetReader.read(read); // Read 4 more bytes
				int directoryOffset=DataReader.readInt(read[0], read[1], read[2], read[3]);
				
				FileInputStream directoryReader = new FileInputStream(BSPFile);
				directoryReader.skip(directoryOffset);
				
				byte[] readDirectory=new byte[16];
				
				// Read through the directory to find maps
				for(int i=0;i<numLumps;i++) {
					directoryReader.read(readDirectory);
					offset=DataReader.readInt(readDirectory[0], readDirectory[1], readDirectory[2], readDirectory[3]);
					length=DataReader.readInt(readDirectory[4], readDirectory[5], readDirectory[6], readDirectory[7]);
					String lumpName=new String(new byte[] { readDirectory[8], readDirectory[9], readDirectory[10], readDirectory[11], readDirectory[12], readDirectory[13], readDirectory[14], readDirectory[15] });
					if(Thread.currentThread().interrupted()) {
						throw new java.lang.InterruptedException("on lump "+i+" of WAD, "+lumpName+".");
					}
					if( ( lumpName.substring(0,3).equals("MAP") && lumpName.charAt(3)>='0' && lumpName.charAt(3)<='9' && lumpName.charAt(4)>='0' && lumpName.charAt(4)<='9' ) || ( lumpName.charAt(0)=='E' && lumpName.charAt(2)=='M' && lumpName.charAt(1)>='0' && lumpName.charAt(1)<='9' && lumpName.charAt(3)>='0' && lumpName.charAt(3)<='9' ) ) {
						String mapName=lumpName.substring(0,5); // Map names are always ExMy or MAPxx. Never more than five chars.
						Window.println("Map: "+mapName,Window.VERBOSITY_ALWAYS);
						// All of this code updates the maplist with a new entry
						DoomMap[] newList=new DoomMap[doomMaps.length+1];
						for(int j=0;j<doomMaps.length;j++) {
							newList[j] = doomMaps[j];
						}
						newList[doomMaps.length]=new DoomMap(BSPFile.getPath(), mapName);
						doomMaps=newList;
						
						if(length>0 && Window.extractZipIsSelected()) {
							try {
								Window.print("Extracting Map Header ",Window.VERBOSITY_ALWAYS);
								Date begin=new Date();
								FileOutputStream ScriptWriter;
								if(Window.getOutputFolder().equals("default")) {
									File newDir = new File(doomMaps[doomMaps.length-1].getFolder()+doomMaps[doomMaps.length-1].getWadName()+"\\");
									if(!newDir.exists()) {
										newDir.mkdir();
									}
									Window.println(doomMaps[doomMaps.length-1].getFolder()+doomMaps[doomMaps.length-1].getWadName()+"\\"+doomMaps[doomMaps.length-1].getMapName()+".hdr",Window.VERBOSITY_ALWAYS);
									ScriptWriter=new FileOutputStream(new File(doomMaps[doomMaps.length-1].getFolder()+doomMaps[doomMaps.length-1].getWadName()+"\\"+doomMaps[doomMaps.length-1].getMapName()+".hdr"));
								} else {
									File newDir = new File(Window.getOutputFolder()+"\\"+doomMaps[doomMaps.length-1].getWadName()+"\\");
									if(!newDir.exists()) {
										newDir.mkdir();
									}
									Window.println(Window.getOutputFolder()+"\\"+doomMaps[doomMaps.length-1].getWadName()+"\\"+doomMaps[doomMaps.length-1].getMapName()+".hdr",Window.VERBOSITY_ALWAYS);
									ScriptWriter=new FileOutputStream(new File(Window.getOutputFolder()+"\\"+doomMaps[doomMaps.length-1].getWadName()+"\\"+doomMaps[doomMaps.length-1].getMapName()+".hdr"));
								}
								ScriptWriter.write(readLump(offset, length));
								ScriptWriter.close();
								Date end=new Date();
								Window.println(end.getTime()-begin.getTime()+"ms",Window.VERBOSITY_ALWAYS);
							} catch(java.lang.Exception e) {
								Window.println("WARNING: Unable to write header file!",Window.VERBOSITY_WARNINGS);
							}
						}
						
						FileInputStream lumpReader = new FileInputStream(BSPFile);
						lumpReader.skip(directoryOffset+((i+1)*16));
						
						lumpReader.read(readDirectory);
						
						offset=DataReader.readInt(readDirectory[0], readDirectory[1], readDirectory[2], readDirectory[3]);
						length=DataReader.readInt(readDirectory[4], readDirectory[5], readDirectory[6], readDirectory[7]);
						lumpName=new String(new byte[] { readDirectory[8], readDirectory[9], readDirectory[10], readDirectory[11], readDirectory[12], readDirectory[13], readDirectory[14], readDirectory[15] });
						int thingsoffset=0;
						int thingslength=0;
						if(lumpName.substring(0,6).equalsIgnoreCase("THINGS")) {
							thingsoffset=offset;
							thingslength=length;
						}
						
						lumpReader.read(readDirectory);
						
						offset=DataReader.readInt(readDirectory[0], readDirectory[1], readDirectory[2], readDirectory[3]);
						length=DataReader.readInt(readDirectory[4], readDirectory[5], readDirectory[6], readDirectory[7]);
						lumpName=new String(new byte[] { readDirectory[8], readDirectory[9], readDirectory[10], readDirectory[11], readDirectory[12], readDirectory[13], readDirectory[14], readDirectory[15] });
						int linesoffset=0;
						int lineslength=0;
						if(lumpName.equalsIgnoreCase("LINEDEFS")) {
							linesoffset=offset;
							lineslength=length;
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
						
						lumpReader.skip(32);
						lumpReader.read(readDirectory);
						
						lumpName=new String(new byte[] { readDirectory[8], readDirectory[9], readDirectory[10], readDirectory[11], readDirectory[12], readDirectory[13], readDirectory[14], readDirectory[15] });
						if(lumpName.equalsIgnoreCase("BEHAVIOR")) {
							doomMaps[doomMaps.length-1].setThings(readLump(thingsoffset, thingslength), DoomMap.TYPE_HEXEN);
							doomMaps[doomMaps.length-1].setLinedefs(readLump(linesoffset, lineslength), DoomMap.TYPE_HEXEN);
						} else {
							doomMaps[doomMaps.length-1].setThings(readLump(thingsoffset, thingslength), DoomMap.TYPE_DOOM);
							doomMaps[doomMaps.length-1].setLinedefs(readLump(linesoffset, lineslength), DoomMap.TYPE_DOOM);
						}
						
						lumpReader.close();

						doomMaps[doomMaps.length-1].printBSPReport();

						DecompilerDriver.window.addJob(null, doomMaps[doomMaps.length-1]);
					}
				}
				directoryReader.close();
			} else {
				BSPObject = new BSP(BSPFile.getPath(), version);
				switch(version) {
					case BSP.TYPE_QUAKE:
						Window.println("Quake 1/Half-life BSP",Window.VERBOSITY_ALWAYS);
						offsetReader.skip(4); // Skip the file header, putting the reader into the offset/length pairs
						
						// Lump 00
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						BSPObject.setEntities(readLump(offset, length));
						
						// Lump 01
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						BSPObject.setPlanes(readLump(offset, length));
						
						// Lump 02
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						BSPObject.setTextures(readLump(offset, length));
						
						// Lump 03
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						BSPObject.setVertices(readLump(offset, length));
						
						offsetReader.skip(8);
						
						// Lump 05
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						BSPObject.setNodes(readLump(offset, length));
						
						// Lump 06
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						BSPObject.setTexInfo(readLump(offset, length));
						
						// Lump 07
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						BSPObject.setFaces(readLump(offset, length));
						
						offsetReader.skip(16);
						
						// Lump 10
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						BSPObject.setLeaves(readLump(offset, length));
						
						// Lump 11
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						BSPObject.setMarkSurfaces(readLump(offset, length));
						
						// Lump 12
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						BSPObject.setEdges(readLump(offset, length));
						
						// Lump 13
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						BSPObject.setSurfEdges(readLump(offset, length));
						
						// Lump 14
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						BSPObject.setModels(readLump(offset, length));
						break;
					case BSP.TYPE_NIGHTFIRE:
						Window.println("BSP v42 (Nightfire)",Window.VERBOSITY_ALWAYS);
						offsetReader.skip(4); // Skip the file header, putting the reader into the offset/length pairs
						
						// Lump 00
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						BSPObject.setEntities(readLump(offset, length));
						
						// Lump 01
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						BSPObject.setPlanes(readLump(offset, length));
						
						// Lump 02
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						BSPObject.setTextures(readLump(offset, length));
						
						// Lump 03
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						BSPObject.setMaterials(readLump(offset, length));
						
						// Lump 04
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						BSPObject.setVertices(readLump(offset, length));
						
						offsetReader.skip(32); // Do not need offset/length for lumps 5-8
						
						// Lump 09
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						BSPObject.setFaces(readLump(offset, length));
						
						offsetReader.skip(8); // Do not need offset/length for lump 10
						
						// Lump 11
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						BSPObject.setLeaves(readLump(offset, length));
						
						offsetReader.skip(8); // Do not need offset/length for lump 12
						
						// Lump 13
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						BSPObject.setMarkBrushes(readLump(offset, length));
						
						// Lump 14
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						BSPObject.setModels(readLump(offset, length));
						
						// Lump 15
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						BSPObject.setBrushes(readLump(offset, length));
						
						// Lump 16
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						BSPObject.setBrushSides(readLump(offset, length));
						
						// Lump 17
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						BSPObject.setTexInfo(readLump(offset, length));
						break;
					case BSP.TYPE_STEF2:
					case BSP.TYPE_STEF2DEMO:
						Window.println("Star Trek Elite Force 2 BSP",Window.VERBOSITY_ALWAYS);
						offsetReader.skip(12); // Skip the file header, putting the reader into the lump directory
						
						// Lump 00
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						BSPObject.setTextures(readLump(offset, length));
						
						// Lump 01
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						BSPObject.setPlanes(readLump(offset, length));
						
						offsetReader.skip(80); // Lumps 02-11 aren't needed
						
						// Lump 12
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						BSPObject.setBrushSides(readLump(offset, length));
						
						// Lump 13
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						BSPObject.setBrushes(readLump(offset, length));
						
						offsetReader.skip(8);
						
						// Lump 15
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						BSPObject.setModels(readLump(offset, length));
						
						// Lump 16
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						BSPObject.setEntities(readLump(offset, length));
						break;
					case BSP.TYPE_MOHAA:
						Window.println("MOHAA BSP (modified id Tech 3)",Window.VERBOSITY_ALWAYS);
						offsetReader.skip(12); // Skip the file header, putting the reader into the offset/length pairs
						
						// Lump 00
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						BSPObject.setTextures(readLump(offset, length));
						
						// Lump 01
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						BSPObject.setPlanes(readLump(offset, length));
						
						offsetReader.skip(72); // Do not need offset/length for lumps 2-10
						
						// Lump 11
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						BSPObject.setBrushSides(readLump(offset, length));
						
						// Lump 12
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						BSPObject.setBrushes(readLump(offset, length));
						
						// Lump 13
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						BSPObject.setModels(readLump(offset, length));
						
						// Lump 14
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						BSPObject.setEntities(readLump(offset, length));
						
						/*TODO
						offsetReader.skip(80); // Do not need offset/length for lumps 15-24
						
						// Lump 24
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						BSPObject.setStaticProps(readLump(offset, length));*/
						break;
					case BSP.TYPE_FAKK:
						Window.println("Heavy Metal FAKK² BSP",Window.VERBOSITY_ALWAYS);
						offsetReader.skip(12); // Skip the file header, putting the reader into the lump directory
						
						// Lump 00
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						BSPObject.setTextures(readLump(offset, length));
						
						// Lump 01
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						BSPObject.setPlanes(readLump(offset, length));
						
						offsetReader.skip(64);
						
						// Lump 10
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						BSPObject.setBrushSides(readLump(offset, length));
						
						// Lump 11
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						BSPObject.setBrushes(readLump(offset, length));
						
						offsetReader.skip(8);
						
						// Lump 13
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						BSPObject.setModels(readLump(offset, length));
						
						// Lump 14
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						BSPObject.setEntities(readLump(offset, length));
						break;
					case BSP.TYPE_SIN:
						Window.println("SiN BSP (Modified Quake 2)",Window.VERBOSITY_ALWAYS);
						offsetReader.skip(8); // Skip the file header, putting the reader into the offset/length pairs
						
						// Lump 00
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						BSPObject.setEntities(readLump(offset, length));
						
						// Lump 01
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						BSPObject.setPlanes(readLump(offset, length));
						
						// Lump 02
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						BSPObject.setVertices(readLump(offset, length));
						
						offsetReader.skip(8); // Do not need offset/length for lump 3
						
						// Lump 04
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						BSPObject.setNodes(readLump(offset, length));
						
						// Lump 05
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						BSPObject.setTextures(readLump(offset, length));
						
						// Lump 06
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						BSPObject.setFaces(readLump(offset, length));
						
						offsetReader.skip(8); // Do not need offset/length for lump 7
						
						// Lump 08
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						BSPObject.setLeaves(readLump(offset, length));
						
						offsetReader.skip(8); // Do not need offset/length for lump 9
								
						// Lump 10
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						BSPObject.setMarkBrushes(readLump(offset, length));
						
						// Lump 11
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						BSPObject.setEdges(readLump(offset, length));
						
						// Lump 12
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						BSPObject.setSurfEdges(readLump(offset, length));
						
						// Lump 13
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						BSPObject.setModels(readLump(offset, length));
						
						// Lump 14
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						BSPObject.setBrushes(readLump(offset, length));
						
						// Lump 15
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						BSPObject.setBrushSides(readLump(offset, length));
						break;
					case BSP.TYPE_RAVEN:
						Window.println("Raven Software BSP (Modified id Tech 3)",Window.VERBOSITY_ALWAYS);
						offsetReader.skip(8); // Skip the file header, putting the reader into the offset/length pairs
						
						// Lump 00
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						BSPObject.setEntities(readLump(offset, length));
						
						// Lump 01
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						BSPObject.setTextures(readLump(offset, length));
						
						// Lump 02
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						BSPObject.setPlanes(readLump(offset, length));
						
						offsetReader.skip(32); // Do not need offset/length for lumps 3-6
						
						// Lump 07
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						BSPObject.setModels(readLump(offset, length));
						
						// Lump 08
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						BSPObject.setBrushes(readLump(offset, length));
						
						// Lump 09
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						BSPObject.setBrushSides(readLump(offset, length));
						
						// Lump 10
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						BSPObject.setVertices(readLump(offset, length));
						
						offsetReader.skip(16); // Do not need offset/length for lumps 11 and 12
						
						// Lump 13
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						BSPObject.setFaces(readLump(offset, length));
						break;
					case BSP.TYPE_SOURCE17:
					case BSP.TYPE_SOURCE18:
					case BSP.TYPE_SOURCE19:
					case BSP.TYPE_SOURCE20:
					case BSP.TYPE_SOURCE21:
					case BSP.TYPE_SOURCE22:
					case BSP.TYPE_SOURCE23:
						Window.println("Source BSP",Window.VERBOSITY_ALWAYS);
						// Left 4 Dead 2, for some reason, made the order "version, offset, length" for lump header structure,
						// rather than the usual "offset, length, version". I guess someone at Valve got bored.
						boolean isL4D2=false;
						int lumpVersion=0;
						offsetReader.skip(8); // Skip the VBSP and version number
						
						/* Needed source BSP lumps
						I can easily add more to the parser later if this list needs to be expanded
						0 ents
						1 planes
						2 texdata
						3 vertices
						5 nodes
						6 texinfo
						7 faces
						10 leaves
						12 edges
						13 surfedges
						14 models
						17 leafbrushes
						18 brushes
						19 brushsides
						26 displacement info
						27 original faces
						33 Displacement vertices
						40 Pakfile - can just be dumped
						43 texdata strings
						44 texdata string table
						48 displacement triangles
						*/
						
						// Lump 00
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						lumpVersion=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.skip(4);
						if(offset<1036) { // This is less than the total length of the file header. Probably indicates a L4D2 map.
							isL4D2=true;   // Although unused lumps have everything set to 0 in their info, Entities are NEVER EVER UNUSED! EVER!
						}                 // A BSP file without entities is geometry with no life, no worldspawn. That's never acceptable.
						if(isL4D2) {
							BSPObject.setEntities(readLump(length, version));
						} else {
							BSPObject.setEntities(readLump(offset, length));
						}
						
						// Lump 01
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						lumpVersion=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.skip(4);
						if(isL4D2) {
							BSPObject.setPlanes(readLump(length, version));
						} else {
							BSPObject.setPlanes(readLump(offset, length));
						}
						
						// Lump 02
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						lumpVersion=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.skip(4);
						if(isL4D2) {
							BSPObject.setTexDatas(readLump(length, version));
						} else {
							BSPObject.setTexDatas(readLump(offset, length));
						}
						
						// Lump 03
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						lumpVersion=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.skip(4);
						if(isL4D2) {
							BSPObject.setVertices(readLump(length, version));
						} else {
							BSPObject.setVertices(readLump(offset, length));
						}
						
						offsetReader.skip(16); // Skip lump 4 data
						
						// Lump 05
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						lumpVersion=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.skip(4);
						if(isL4D2) {
							BSPObject.setNodes(readLump(length, version));
						} else {
							BSPObject.setNodes(readLump(offset, length));
						}
						
						// Lump 06
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						lumpVersion=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.skip(4);
						if(isL4D2) {
							BSPObject.setTexInfo(readLump(length, version));
						} else {
							BSPObject.setTexInfo(readLump(offset, length));
						}
						
						// Lump 07
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						lumpVersion=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.skip(4);
						if(isL4D2) {
							BSPObject.setFaces(readLump(length, version));
						} else {
							BSPObject.setFaces(readLump(offset, length));
						}
						
						offsetReader.skip(32); // skip lump 8 and 9 data
						
						// Lump 10
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						lumpVersion=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.skip(4);
						if(isL4D2) {
							BSPObject.setLeaves(readLump(length, version));
						} else {
							BSPObject.setLeaves(readLump(offset, length));
						}
						
						offsetReader.skip(16);
						
						// Lump 12
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						lumpVersion=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.skip(4);
						if(isL4D2) {
							BSPObject.setEdges(readLump(length, version));
						} else {
							BSPObject.setEdges(readLump(offset, length));
						}
						
						// Lump 13
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						lumpVersion=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.skip(4);
						if(isL4D2) {
							BSPObject.setSurfEdges(readLump(length, version));
						} else {
							BSPObject.setSurfEdges(readLump(offset, length));
						}
						
						// Lump 14
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						lumpVersion=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.skip(4);
						if(isL4D2) {
							BSPObject.setModels(readLump(length, version));
						} else {
							BSPObject.setModels(readLump(offset, length));
						}
						
						offsetReader.skip(32); // Skip lumps 15 and 16
						
						// Lump 17
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						lumpVersion=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.skip(4);
						if(isL4D2) {
							BSPObject.setMarkBrushes(readLump(length, version));
						} else {
							BSPObject.setMarkBrushes(readLump(offset, length));
						}
						
						// Lump 18
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						lumpVersion=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.skip(4);
						if(isL4D2) {
							BSPObject.setBrushes(readLump(length, version));
						} else {
							BSPObject.setBrushes(readLump(offset, length));
						}
						
						// Lump 19
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						lumpVersion=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.skip(4);
						if(isL4D2) {
							BSPObject.setBrushSides(readLump(length, version));
						} else {
							BSPObject.setBrushSides(readLump(offset, length));
						}
						
						offsetReader.skip(96); // Skip entries for lumps 20 21 22 23 24 25
						
						// Lump 26
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						lumpVersion=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.skip(4);
						if(isL4D2) {
							BSPObject.setDispInfos(readLump(length, version));
						} else {
							BSPObject.setDispInfos(readLump(offset, length));
						}
						
						// Lump 27
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						lumpVersion=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.skip(4);
						if(isL4D2) {
							BSPObject.setOriginalFaces(readLump(length, version));
						} else {
							BSPObject.setOriginalFaces(readLump(offset, length));
						}
						
						offsetReader.skip(80); // Lumps 28 29 30 31 32
						
						// Lump 33
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						lumpVersion=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.skip(4);
						if(isL4D2) {
							BSPObject.setDispVerts(readLump(length, version));
						} else {
							BSPObject.setDispVerts(readLump(offset, length));
						}
						
						offsetReader.skip(16);
						
						// Lump 35, Game lump
						// This lump SUCKS. It's a lump containing nested lumps for game specific data.
						// What we need out of it is the static prop lump.
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						lumpVersion=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.skip(4);
						byte[] gamelumpData;
						int gamelumpFileOffset;
						if(isL4D2) {
							gamelumpData=readLump(length, version);
							gamelumpFileOffset=length;
						} else {
							gamelumpData=readLump(offset, length);
							gamelumpFileOffset=offset;
						}
						int numGamelumps=DataReader.readInt(gamelumpData[0], gamelumpData[1], gamelumpData[2], gamelumpData[3]);
						int gamelumpOffset=4;
						if(numGamelumps>1) {
							byte[] staticPropLump=new byte[0];
							int staticPropLumpVersion=0;
							boolean isRelativeToLumpStart=false;
							for(int i=0;i<numGamelumps;i++) {
								int gamelumpID=DataReader.readInt(gamelumpData[gamelumpOffset++], gamelumpData[gamelumpOffset++], gamelumpData[gamelumpOffset++], gamelumpData[gamelumpOffset++]);
								gamelumpOffset+=2; // skip flags
								short gamelumpVersion=DataReader.readShort(gamelumpData[gamelumpOffset++], gamelumpData[gamelumpOffset++]);
								int internalOffset=DataReader.readInt(gamelumpData[gamelumpOffset++], gamelumpData[gamelumpOffset++], gamelumpData[gamelumpOffset++], gamelumpData[gamelumpOffset++]);
								int internalLength=DataReader.readInt(gamelumpData[gamelumpOffset++], gamelumpData[gamelumpOffset++], gamelumpData[gamelumpOffset++], gamelumpData[gamelumpOffset++]);
								if(internalOffset<gamelumpFileOffset) {
									isRelativeToLumpStart=true;
								}
								if(gamelumpID==1936749168) { // "prps"
									staticPropLumpVersion=gamelumpVersion;
									if(isRelativeToLumpStart) {
										staticPropLump=readLump(gamelumpFileOffset+internalOffset, internalLength);
									} else {
										staticPropLump=readLump(internalOffset, internalLength);
									}
								}
								// Other game lumps would go here
							}
							BSPObject.setStaticProps(staticPropLump, staticPropLumpVersion);
						}
						
						offsetReader.skip(64);
						
						if(Window.extractZipIsSelected()) {
							// Lump 40
							offsetReader.read(read); // Read 4 bytes
							offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
							offsetReader.read(read); // Read 4 more bytes
							length=DataReader.readInt(read[0], read[1], read[2], read[3]);
							offsetReader.read(read); // Read 4 more bytes
							lumpVersion=DataReader.readInt(read[0], read[1], read[2], read[3]);
							offsetReader.skip(4);
							try {
								Window.print("Extracting internal PAK file... ",Window.VERBOSITY_ALWAYS);
								Date begin=new Date();
								FileOutputStream PAKWriter;
								if(Window.getOutputFolder().equals("default")) {
									PAKWriter=new FileOutputStream(new File(BSPObject.getPath().substring(0, BSPObject.getPath().length()-4)+".zip"));
								} else {
									PAKWriter=new FileOutputStream(new File(Window.getOutputFolder()+"\\"+BSPObject.getMapName().substring(0, BSPObject.getMapName().length()-4)+".zip"));
								}
								if(isL4D2) {
									PAKWriter.write(readLump(length, version));
								} else {
									PAKWriter.write(readLump(offset, length));
								}
								PAKWriter.close();
								Date end=new Date();
								Window.println(end.getTime()-begin.getTime()+"ms",Window.VERBOSITY_ALWAYS);
							} catch(java.io.IOException e) {
								Window.println("WARNING: Unable to write PAKFile! Path: "+BSPFile.getAbsolutePath().substring(0,BSPFile.getAbsolutePath().length()-4)+".zip",Window.VERBOSITY_WARNINGS);
							}
						} else {
							offsetReader.skip(16);
						}
						
						offsetReader.skip(16);
						
						// Lump 42
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						lumpVersion=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.skip(4);
						if(isL4D2) {
							BSPObject.setCubemaps(readLump(length, version));
						} else {
							BSPObject.setCubemaps(readLump(offset, length));
						}
						
						// Lump 43
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						lumpVersion=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.skip(4);
						if(isL4D2) {
							BSPObject.setTextures(readLump(length, version));
						} else {
							BSPObject.setTextures(readLump(offset, length));
						}
						
						// Lump 44
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						lumpVersion=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.skip(4);
						if(isL4D2) {
							BSPObject.setTexTable(readLump(length, version));
						} else {
							BSPObject.setTexTable(readLump(offset, length));
						}
						
						offsetReader.skip(48);
						
						// Lump 48
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						lumpVersion=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.skip(4);
						if(isL4D2) {
							BSPObject.setDispTris(readLump(length, version));
						} else {
							BSPObject.setDispTris(readLump(offset, length));
						}
						break;
					case BSP.TYPE_QUAKE2:
						Window.println("BSP v38 (Quake 2)",Window.VERBOSITY_ALWAYS);
						offsetReader.skip(8); // Skip the file header, putting the reader into the offset/length pairs
						
						// Lump 00
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						BSPObject.setEntities(readLump(offset, length));
						
						// Lump 01
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						BSPObject.setPlanes(readLump(offset, length));
						
						// Lump 02
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						BSPObject.setVertices(readLump(offset, length));
						
						offsetReader.skip(8); // Do not need offset/length for lump 3
						
						// Lump 04
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						BSPObject.setNodes(readLump(offset, length));
						
						// Lump 05
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						BSPObject.setTextures(readLump(offset, length));
						
						// Lump 06
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						BSPObject.setFaces(readLump(offset, length));
						
						offsetReader.skip(8); // Do not need offset/length for lump 7
						
						// Lump 08
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						BSPObject.setLeaves(readLump(offset, length));
						
						offsetReader.skip(8); // Do not need offset/length for lump 9
						
						// Lump 10
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						BSPObject.setMarkBrushes(readLump(offset, length));
						
						// Lump 11
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						BSPObject.setEdges(readLump(offset, length));
						
						// Lump 12
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						BSPObject.setSurfEdges(readLump(offset, length));
						
						// Lump 13
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						BSPObject.setModels(readLump(offset, length));
						
						// Lump 14
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						BSPObject.setBrushes(readLump(offset, length));
						
						// Lump 15
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						BSPObject.setBrushSides(readLump(offset, length));
						/*
						offsetReader.skip(16); // Do not need offset/length for lumps 16 or 17
						
						// Lump 18
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						BSPObject.setAreaPortals(readLump(offset, length));*/
						break;
					case BSP.TYPE_SOF: // Uses the same header as Q3.
						Window.println("Soldier of Fortune BSP (modified id Tech 2)",Window.VERBOSITY_ALWAYS);
						offsetReader.skip(8); // Skip the file header, putting the reader into the offset/length pairs
										
						// Lump 00
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						BSPObject.setEntities(readLump(offset, length));
						
						// Lump 01
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						BSPObject.setPlanes(readLump(offset, length));
						
						// Lump 02
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						BSPObject.setVertices(readLump(offset, length));
						
						offsetReader.skip(8); // Do not need offset/length for lump 3
						
						// Lump 04
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						BSPObject.setNodes(readLump(offset, length));
						
						// Lump 05
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						BSPObject.setTextures(readLump(offset, length));
						
						// Lump 06
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						BSPObject.setFaces(readLump(offset, length));
						
						offsetReader.skip(8); // Do not need offset/length for lump 7
						
						// Lump 08
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						BSPObject.setLeaves(readLump(offset, length));
						
						offsetReader.skip(8); // Do not need offset/length for lump 9
						
						// Lump 10
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						BSPObject.setMarkBrushes(readLump(offset, length));
						
						// Lump 11
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						BSPObject.setEdges(readLump(offset, length));
						
						// Lump 12
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						BSPObject.setSurfEdges(readLump(offset, length));
						
						// Lump 13
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						BSPObject.setModels(readLump(offset, length));
						
						// Lump 14
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						BSPObject.setBrushes(readLump(offset, length));
						
						// Lump 15
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						BSPObject.setBrushSides(readLump(offset, length));
						break;
					case BSP.TYPE_QUAKE3:
						Window.println("BSP v46 (id Tech 3)",Window.VERBOSITY_ALWAYS);
						offsetReader.skip(8);
						
						// Lump 00
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						BSPObject.setEntities(readLump(offset, length));
						
						// Lump 01
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						BSPObject.setTextures(readLump(offset, length));
						
						// Lump 02
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						BSPObject.setPlanes(readLump(offset, length));
						
						offsetReader.skip(32); // Do not need offset/length for lumps 3-6
						
						// Lump 07
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						BSPObject.setModels(readLump(offset, length));
						
						// Lump 08
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						BSPObject.setBrushes(readLump(offset, length));
						
						// Lump 09
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						BSPObject.setBrushSides(readLump(offset, length));
						
						// Lump 10
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						BSPObject.setVertices(readLump(offset, length));
						
						offsetReader.skip(16); // Do not need offset/length for lumps 11 and 12
						
						// Lump 13
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						BSPObject.setFaces(readLump(offset, length));
						break;
					case BSP.TYPE_COD:
						Window.println("BSP v59 (Call of Duty)",Window.VERBOSITY_ALWAYS);
						offsetReader.skip(8); // Skip the file header, putting the reader into the length/offset pairs
						
						// Lump 00
						offsetReader.read(read); // Read 4 bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						BSPObject.setTextures(readLump(offset, length));
						
						offsetReader.skip(8);
						
						// Lump 02
						offsetReader.read(read); // Read 4 bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						BSPObject.setPlanes(readLump(offset, length));
						
						// Lump 03
						offsetReader.read(read); // Read 4 bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						BSPObject.setBrushSides(readLump(offset, length));
						
						// Lump 04
						offsetReader.read(read); // Read 4 bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						BSPObject.setBrushes(readLump(offset, length));
						
						offsetReader.skip(176); // Skip lumps 5-26
						
						// Lump 27
						offsetReader.read(read); // Read 4 bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						BSPObject.setModels(readLump(offset, length));
						
						offsetReader.skip(8); // Skip lumps 5-26
						
						// Lump 29
						offsetReader.read(read); // Read 4 bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						BSPObject.setEntities(readLump(offset, length));
						break;
					case BSP.TYPE_COD2:
						Window.println("Call of Duty 2 BSP",Window.VERBOSITY_ALWAYS);
						offsetReader.skip(8); // Skip the file header, putting the reader into the length/offset pairs
						
						// Lump 00
						offsetReader.read(read); // Read 4 bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						BSPObject.setTextures(readLump(offset, length));
						
						offsetReader.skip(24); // skip 1,2,3
						
						// Lump 04
						offsetReader.read(read); // Read 4 bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						BSPObject.setPlanes(readLump(offset, length));
						
						// Lump 05
						offsetReader.read(read); // Read 4 bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						BSPObject.setBrushSides(readLump(offset, length));
						
						// Lump 06
						offsetReader.read(read); // Read 4 bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						BSPObject.setBrushes(readLump(offset, length));
						
						offsetReader.skip(224); // Skip lumps 7-34
						
						// Lump 35
						offsetReader.read(read); // Read 4 bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						BSPObject.setModels(readLump(offset, length));
						
						offsetReader.skip(8); // Skip lump 36
						
						// Lump 37
						offsetReader.read(read); // Read 4 bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						BSPObject.setEntities(readLump(offset, length));
						break;
					case BSP.TYPE_COD4:
						Window.println("Call of Duty 4 BSP",Window.VERBOSITY_ALWAYS);
						// CoD4 is somewhat unique, it calls for a different reader. However it's still doable.
						offsetReader.skip(8); // IBSP version 22
						offsetReader.read(read);
						int numlumps=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offset=(numlumps*8)+12;
						for(int i=0;i<numlumps;i++) {
							offsetReader.read(read);
							int id=DataReader.readInt(read[0], read[1], read[2], read[3]);
							offsetReader.read(read);
							length=DataReader.readInt(read[0], read[1], read[2], read[3]);
							
							switch(id) {
								case 0:
									BSPObject.setTextures(readLump(offset, length));
									break;
								case 4:
									BSPObject.setPlanes(readLump(offset, length));
									break;
								case 5:
									BSPObject.setBrushSides(readLump(offset, length));
									break;
								case 8:
									BSPObject.setBrushes(readLump(offset, length));
									break;
								case 37:
									BSPObject.setModels(readLump(offset, length));
									break;
								case 39:
									BSPObject.setEntities(readLump(offset, length));
									break;
							}
							
							offset+=length;
							
							while((double)offset/(double)4 != (int)((double)offset/(double)4)) {
								offset++;
							}
						}
						break;
					default:
						Window.println("I don't know what kind of BSP this is! Please post an issue on the bug tracker!",Window.VERBOSITY_ALWAYS);
				}
				BSPObject.printBSPReport();
			}
			offsetReader.close();
		} catch(java.io.IOException e) {
			Window.println("Unable to access BSP file! Is it open in another program?",Window.VERBOSITY_ALWAYS);
		}
	}
	
	// +readLump(int, int)
	// Reads the lump length bytes long at offset in the file
	public byte[] readLump(int offset, int length) {
		byte[] input=new byte[length];
		try {
			FileInputStream fileReader=new FileInputStream(BSPFile);
			fileReader.skip(offset);
			fileReader.read(input);
			fileReader.close();
		} catch(java.io.IOException e) {
			Window.println("Unknown error reading BSP, it was working before!",Window.VERBOSITY_ALWAYS);
		}
		return input;
	}
				
	// ACCESSORS/MUTATORS
	
	public BSP getBSPObject() {
		return BSPObject;
	}
	
	public boolean isWAD() {
		return wad;
	}
	
	public int getVersion() throws java.io.IOException {
		if(readAs!=-1) {
			return readAs;
		}
		if(version==0) {
			byte[] read=new byte[4];
			FileInputStream versionNumberReader=new FileInputStream(BSPFile); // This filestream will be used to read version number only
			versionNumberReader.read(read);
			int in=DataReader.readInt(read[0], read[1], read[2], read[3]);
			if(in == 1347633737) { // 1347633737 reads in ASCII as "IBSP"
				versionNumberReader.read(read);
				version=DataReader.readInt(read[0], read[1], read[2], read[3]);
				switch(version) {
					case 4:
						version=BSP.TYPE_COD2;
						break;
					case 22:
						version=BSP.TYPE_COD4;
						break;
					case 59:
						version=BSP.TYPE_COD;
						break;
					case 38:
						version=BSP.TYPE_QUAKE2;
						break;
					case 46:
						FileInputStream secondOffsetReader = new FileInputStream(BSPFile);
						secondOffsetReader.skip(8);
						version=BSP.TYPE_QUAKE3;
						for(int i=0;i<17;i++) {
							// Find out where the first lump starts, based on offsets.
							// This process assumes the file header has not been tampered with in any way.
							// Unfortunately it could inadvertantly lead to a method of decompile protection.
							secondOffsetReader.read(read);
							int temp=DataReader.readInt(read[0], read[1], read[2], read[3]);
							if(temp==184) {
								version=BSP.TYPE_SOF;
								break;
							} else {
								if(temp==144) {
									break;
								}
							}
							secondOffsetReader.skip(4);
						}
						secondOffsetReader.close();
						break;
					case 47:
						version=BSP.TYPE_QUAKE3;
						break;
				}
			} else {
				if(in == 892416050) { // 892416050 reads in ASCII as "2015," the game studio which developed MoHAA
					version=BSP.TYPE_MOHAA;
				} else {
				if(in == 1095516485) { // 1095516485 reads in ASCII as "EALA," the ones who developed MoHAA Spearhead and Breakthrough
						version=BSP.TYPE_MOHAA; // Should be 21
					} else {
						if(in == 1347633750) { // 1347633750 reads in ASCII as "VBSP." Indicates Source engine.
							versionNumberReader.read(read);
							// Some source games handle this as 2 shorts. Since most version numbers
							// are below 65535, I can always read the first number as a short, because
							// the least significant bits come first in little endian.
							version=(int)DataReader.readShort(read[0], read[1]);
							switch(version) {
								case 17:
									version=BSP.TYPE_SOURCE17;
									break;
								case 18:
									version=BSP.TYPE_SOURCE18;
									break;
								case 19:
									version=BSP.TYPE_SOURCE19;
									break;
								case 20:
									version=BSP.TYPE_SOURCE20;
									break;
								case 21:
									version=BSP.TYPE_SOURCE21;
									break;
								case 22:
									version=BSP.TYPE_SOURCE22;
									break;
								case 23:
									version=BSP.TYPE_SOURCE23;
									break;
							}
							version2=(int)DataReader.readShort(read[2], read[3]);
						} else {
							if(in==1347633746) { // Reads in ASCII as "RBSP". Raven software's modification of Q3BSP
								FileInputStream secondOffsetReader = new FileInputStream(BSPFile);
								secondOffsetReader.skip(8);
								version=BSP.TYPE_RAVEN;
								for(int i=0;i<17;i++) {
									// Find out where the first lump starts, based on offsets.
									// This process assumes the file header has not been tampered with in any way.
									// Unfortunately it could inadvertantly lead to a method of decompile protection.
									secondOffsetReader.read(read);
									int temp=DataReader.readInt(read[0], read[1], read[2], read[3]);
									if(temp==168) {
										version=BSP.TYPE_SIN;
										break;
									} else {
										if(temp==152) {
											break;
										}
									}
									secondOffsetReader.skip(4);
								}
								secondOffsetReader.close();
							} else {
								if(in == 556942917) { // "EF2!"
									version=BSP.TYPE_STEF2;
								} else {
									if(in == 1145132873 || in == 1145132880) { // "IWAD" or "PWAD"
										wad=true;
										version=1;
									} else {
										if(in == 1263223110) { // "FAKK"
											versionNumberReader.read(read);
											version=DataReader.readInt(read[0], read[1], read[2], read[3]);
											switch(version) {
												case 19:
													version=BSP.TYPE_STEF2DEMO;
													break;
												case 12:
												case 42:
													version=BSP.TYPE_FAKK;
													break;
											}
										} else {
											switch(in) {
												case 29:
												case 30:
													version=BSP.TYPE_QUAKE;
													break;
												case 42:
													version=BSP.TYPE_NIGHTFIRE;
													break;
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
		return version;
	}
}
