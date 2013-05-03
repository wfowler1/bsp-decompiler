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
	
	// Decryptor class for Tactical Intervention
	private TIDecryptor tidecryptor;
	
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
	
	public void readBSP() throws java.lang.InterruptedException, java.lang.Exception {
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
							try {
								doomMaps[doomMaps.length-1].setSidedefs(readLump(offset, length));
							} catch(java.lang.Exception e) {
								if(Window.dumpLumpIsSelected()) {
									FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
									initExceptionDebug.write(readLump(offset, length));
									initExceptionDebug.close();
								}
								throw e;
							}
						}
						
						lumpReader.read(readDirectory);
						
						offset=DataReader.readInt(readDirectory[0], readDirectory[1], readDirectory[2], readDirectory[3]);
						length=DataReader.readInt(readDirectory[4], readDirectory[5], readDirectory[6], readDirectory[7]);
						lumpName=new String(new byte[] { readDirectory[8], readDirectory[9], readDirectory[10], readDirectory[11], readDirectory[12], readDirectory[13], readDirectory[14], readDirectory[15] });
						if(lumpName.equalsIgnoreCase("VERTEXES")) {
							try {
								doomMaps[doomMaps.length-1].setVertices(readLump(offset, length));
							} catch(java.lang.Exception e) {
								if(Window.dumpLumpIsSelected()) {
									FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
									initExceptionDebug.write(readLump(offset, length));
									initExceptionDebug.close();
								}
								throw e;
							}
						}
						
						lumpReader.read(readDirectory);
						
						offset=DataReader.readInt(readDirectory[0], readDirectory[1], readDirectory[2], readDirectory[3]);
						length=DataReader.readInt(readDirectory[4], readDirectory[5], readDirectory[6], readDirectory[7]);
						lumpName=new String(new byte[] { readDirectory[8], readDirectory[9], readDirectory[10], readDirectory[11], readDirectory[12], readDirectory[13], readDirectory[14], readDirectory[15] });
						if(lumpName.substring(0,4).equalsIgnoreCase("SEGS")) {
							try {
								doomMaps[doomMaps.length-1].setSegments(readLump(offset, length));
							} catch(java.lang.Exception e) {
								if(Window.dumpLumpIsSelected()) {
									FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
									initExceptionDebug.write(readLump(offset, length));
									initExceptionDebug.close();
								}
								throw e;
							}
						}
						
						lumpReader.read(readDirectory);
						
						offset=DataReader.readInt(readDirectory[0], readDirectory[1], readDirectory[2], readDirectory[3]);
						length=DataReader.readInt(readDirectory[4], readDirectory[5], readDirectory[6], readDirectory[7]);
						lumpName=new String(new byte[] { readDirectory[8], readDirectory[9], readDirectory[10], readDirectory[11], readDirectory[12], readDirectory[13], readDirectory[14], readDirectory[15] });
						if(lumpName.equalsIgnoreCase("SSECTORS")) {
							try {
								doomMaps[doomMaps.length-1].setSubSectors(readLump(offset, length));
							} catch(java.lang.Exception e) {
								if(Window.dumpLumpIsSelected()) {
									FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
									initExceptionDebug.write(readLump(offset, length));
									initExceptionDebug.close();
								}
								throw e;
							}
						}
						
						lumpReader.read(readDirectory);
						
						offset=DataReader.readInt(readDirectory[0], readDirectory[1], readDirectory[2], readDirectory[3]);
						length=DataReader.readInt(readDirectory[4], readDirectory[5], readDirectory[6], readDirectory[7]);
						lumpName=new String(new byte[] { readDirectory[8], readDirectory[9], readDirectory[10], readDirectory[11], readDirectory[12], readDirectory[13], readDirectory[14], readDirectory[15] });
						if(lumpName.substring(0,5).equalsIgnoreCase("NODES")) {
							try {
								doomMaps[doomMaps.length-1].setNodes(readLump(offset, length));
							} catch(java.lang.Exception e) {
								if(Window.dumpLumpIsSelected()) {
									FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
									initExceptionDebug.write(readLump(offset, length));
									initExceptionDebug.close();
								}
								throw e;
							}
						}
						
						lumpReader.read(readDirectory);
						
						offset=DataReader.readInt(readDirectory[0], readDirectory[1], readDirectory[2], readDirectory[3]);
						length=DataReader.readInt(readDirectory[4], readDirectory[5], readDirectory[6], readDirectory[7]);
						lumpName=new String(new byte[] { readDirectory[8], readDirectory[9], readDirectory[10], readDirectory[11], readDirectory[12], readDirectory[13], readDirectory[14], readDirectory[15] });
						if(lumpName.substring(0,7).equalsIgnoreCase("SECTORS")) {
							try {
								doomMaps[doomMaps.length-1].setSectors(readLump(offset, length));
							} catch(java.lang.Exception e) {
								if(Window.dumpLumpIsSelected()) {
									FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
									initExceptionDebug.write(readLump(offset, length));
									initExceptionDebug.close();
								}
								throw e;
							}
						}
						
						lumpReader.skip(32);
						lumpReader.read(readDirectory);
						
						lumpName=new String(new byte[] { readDirectory[8], readDirectory[9], readDirectory[10], readDirectory[11], readDirectory[12], readDirectory[13], readDirectory[14], readDirectory[15] });
						if(lumpName.equalsIgnoreCase("BEHAVIOR")) {
							try {
								doomMaps[doomMaps.length-1].setThings(readLump(thingsoffset, thingslength), DoomMap.TYPE_HEXEN);
							} catch(java.lang.Exception e) {
								if(Window.dumpLumpIsSelected()) {
									FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
									initExceptionDebug.write(readLump(thingsoffset, thingslength));
									initExceptionDebug.close();
								}
								throw e;
							}
							try {
								doomMaps[doomMaps.length-1].setLinedefs(readLump(linesoffset, lineslength), DoomMap.TYPE_HEXEN);
							} catch(java.lang.Exception e) {
								if(Window.dumpLumpIsSelected()) {
									FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
									initExceptionDebug.write(readLump(linesoffset, lineslength));
									initExceptionDebug.close();
								}
								throw e;
							}
						} else {
							try {
								doomMaps[doomMaps.length-1].setThings(readLump(thingsoffset, thingslength), DoomMap.TYPE_DOOM);
							} catch(java.lang.Exception e) {
								if(Window.dumpLumpIsSelected()) {
									FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
									initExceptionDebug.write(readLump(thingsoffset, thingslength));
									initExceptionDebug.close();
								}
								throw e;
							}
							try {
								doomMaps[doomMaps.length-1].setLinedefs(readLump(linesoffset, lineslength), DoomMap.TYPE_DOOM);
							} catch(java.lang.Exception e) {
								if(Window.dumpLumpIsSelected()) {
									FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
									initExceptionDebug.write(readLump(linesoffset, lineslength));
									initExceptionDebug.close();
								}
								throw e;
							}
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
						try {
							BSPObject.setEntities(readLump(offset, length));
						} catch(java.lang.Exception e) {
							if(Window.dumpLumpIsSelected()) {
								FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
								initExceptionDebug.write(readLump(offset, length));
								initExceptionDebug.close();
							}
							throw e;
						}
						
						// Lump 01
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						try {
							BSPObject.setPlanes(readLump(offset, length));
						} catch(java.lang.Exception e) {
							if(Window.dumpLumpIsSelected()) {
								FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
								initExceptionDebug.write(readLump(offset, length));
								initExceptionDebug.close();
							}
							throw e;
						}
						
						// Lump 02
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						try {
							BSPObject.setTextures(readLump(offset, length));
						} catch(java.lang.Exception e) {
							if(Window.dumpLumpIsSelected()) {
								FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
								initExceptionDebug.write(readLump(offset, length));
								initExceptionDebug.close();
							}
							throw e;
						}
						
						// Lump 03
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						try {
							BSPObject.setVertices(readLump(offset, length));
						} catch(java.lang.Exception e) {
							if(Window.dumpLumpIsSelected()) {
								FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
								initExceptionDebug.write(readLump(offset, length));
								initExceptionDebug.close();
							}
							throw e;
						}
						
						offsetReader.skip(8);
						
						// Lump 05
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						try {
							BSPObject.setNodes(readLump(offset, length));
						} catch(java.lang.Exception e) {
							if(Window.dumpLumpIsSelected()) {
								FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
								initExceptionDebug.write(readLump(offset, length));
								initExceptionDebug.close();
							}
							throw e;
						}
						
						// Lump 06
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						try {
							BSPObject.setTexInfo(readLump(offset, length));
						} catch(java.lang.Exception e) {
							if(Window.dumpLumpIsSelected()) {
								FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
								initExceptionDebug.write(readLump(offset, length));
								initExceptionDebug.close();
							}
							throw e;
						}
						
						// Lump 07
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						try {
							BSPObject.setFaces(readLump(offset, length));
						} catch(java.lang.Exception e) {
							if(Window.dumpLumpIsSelected()) {
								FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
								initExceptionDebug.write(readLump(offset, length));
								initExceptionDebug.close();
							}
							throw e;
						}
						
						offsetReader.skip(16);
						
						// Lump 10
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						try {
							BSPObject.setLeaves(readLump(offset, length));
						} catch(java.lang.Exception e) {
							if(Window.dumpLumpIsSelected()) {
								FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
								initExceptionDebug.write(readLump(offset, length));
								initExceptionDebug.close();
							}
							throw e;
						}
						
						// Lump 11
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						try {
							BSPObject.setMarkSurfaces(readLump(offset, length));
						} catch(java.lang.Exception e) {
							if(Window.dumpLumpIsSelected()) {
								FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
								initExceptionDebug.write(readLump(offset, length));
								initExceptionDebug.close();
							}
							throw e;
						}
						
						// Lump 12
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						try {
							BSPObject.setEdges(readLump(offset, length), version);
						} catch(java.lang.Exception e) {
							if(Window.dumpLumpIsSelected()) {
								FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
								initExceptionDebug.write(readLump(offset, length));
								initExceptionDebug.close();
							}
							throw e;
						}
						
						// Lump 13
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						try {
							BSPObject.setSurfEdges(readLump(offset, length));
						} catch(java.lang.Exception e) {
							if(Window.dumpLumpIsSelected()) {
								FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
								initExceptionDebug.write(readLump(offset, length));
								initExceptionDebug.close();
							}
							throw e;
						}
						
						// Lump 14
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						try {
							BSPObject.setModels(readLump(offset, length));
						} catch(java.lang.Exception e) {
							if(Window.dumpLumpIsSelected()) {
								FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
								initExceptionDebug.write(readLump(offset, length));
								initExceptionDebug.close();
							}
							throw e;
						}
						break;
					case BSP.TYPE_NIGHTFIRE:
						Window.println("BSP v42 (Nightfire)",Window.VERBOSITY_ALWAYS);
						offsetReader.skip(4); // Skip the file header, putting the reader into the offset/length pairs
						
						// Lump 00
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						try {
							BSPObject.setEntities(readLump(offset, length));
						} catch(java.lang.Exception e) {
							if(Window.dumpLumpIsSelected()) {
								FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
								initExceptionDebug.write(readLump(offset, length));
								initExceptionDebug.close();
							}
							throw e;
						}
						
						// Lump 01
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						try {
							BSPObject.setPlanes(readLump(offset, length));
						} catch(java.lang.Exception e) {
							if(Window.dumpLumpIsSelected()) {
								FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
								initExceptionDebug.write(readLump(offset, length));
								initExceptionDebug.close();
							}
							throw e;
						}
						
						// Lump 02
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						try {
							BSPObject.setTextures(readLump(offset, length));
						} catch(java.lang.Exception e) {
							if(Window.dumpLumpIsSelected()) {
								FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
								initExceptionDebug.write(readLump(offset, length));
								initExceptionDebug.close();
							}
							throw e;
						}
						
						// Lump 03
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						try {
							BSPObject.setMaterials(readLump(offset, length));
						} catch(java.lang.Exception e) {
							if(Window.dumpLumpIsSelected()) {
								FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
								initExceptionDebug.write(readLump(offset, length));
								initExceptionDebug.close();
							}
							throw e;
						}
						
						// Lump 04
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						try {
							BSPObject.setVertices(readLump(offset, length));
						} catch(java.lang.Exception e) {
							if(Window.dumpLumpIsSelected()) {
								FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
								initExceptionDebug.write(readLump(offset, length));
								initExceptionDebug.close();
							}
							throw e;
						}
						
						offsetReader.skip(32); // Do not need offset/length for lumps 5-8
						
						// Lump 09
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						try {
							BSPObject.setFaces(readLump(offset, length));
						} catch(java.lang.Exception e) {
							if(Window.dumpLumpIsSelected()) {
								FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
								initExceptionDebug.write(readLump(offset, length));
								initExceptionDebug.close();
							}
							throw e;
						}
						
						offsetReader.skip(8); // Do not need offset/length for lump 10
						
						// Lump 11
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						try {
							BSPObject.setLeaves(readLump(offset, length));
						} catch(java.lang.Exception e) {
							if(Window.dumpLumpIsSelected()) {
								FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
								initExceptionDebug.write(readLump(offset, length));
								initExceptionDebug.close();
							}
							throw e;
						}
						
						offsetReader.skip(8); // Do not need offset/length for lump 12
						
						// Lump 13
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						try {
							BSPObject.setMarkBrushes(readLump(offset, length));
						} catch(java.lang.Exception e) {
							if(Window.dumpLumpIsSelected()) {
								FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
								initExceptionDebug.write(readLump(offset, length));
								initExceptionDebug.close();
							}
							throw e;
						}
						
						// Lump 14
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						try {
							BSPObject.setModels(readLump(offset, length));
						} catch(java.lang.Exception e) {
							if(Window.dumpLumpIsSelected()) {
								FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
								initExceptionDebug.write(readLump(offset, length));
								initExceptionDebug.close();
							}
							throw e;
						}
						
						// Lump 15
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						try {
							BSPObject.setBrushes(readLump(offset, length));
						} catch(java.lang.Exception e) {
							if(Window.dumpLumpIsSelected()) {
								FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
								initExceptionDebug.write(readLump(offset, length));
								initExceptionDebug.close();
							}
							throw e;
						}
						
						// Lump 16
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						try {
							BSPObject.setBrushSides(readLump(offset, length));
						} catch(java.lang.Exception e) {
							if(Window.dumpLumpIsSelected()) {
								FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
								initExceptionDebug.write(readLump(offset, length));
								initExceptionDebug.close();
							}
							throw e;
						}
						
						// Lump 17
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						try {
							BSPObject.setTexInfo(readLump(offset, length));
						} catch(java.lang.Exception e) {
							if(Window.dumpLumpIsSelected()) {
								FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
								initExceptionDebug.write(readLump(offset, length));
								initExceptionDebug.close();
							}
							throw e;
						}
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
						try {
							BSPObject.setTextures(readLump(offset, length));
						} catch(java.lang.Exception e) {
							if(Window.dumpLumpIsSelected()) {
								FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
								initExceptionDebug.write(readLump(offset, length));
								initExceptionDebug.close();
							}
							throw e;
						}
						
						// Lump 01
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						try {
							BSPObject.setPlanes(readLump(offset, length));
						} catch(java.lang.Exception e) {
							if(Window.dumpLumpIsSelected()) {
								FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
								initExceptionDebug.write(readLump(offset, length));
								initExceptionDebug.close();
							}
							throw e;
						}
						
						offsetReader.skip(80); // Lumps 02-11 aren't needed
						
						// Lump 12
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						try {
							BSPObject.setBrushSides(readLump(offset, length));
						} catch(java.lang.Exception e) {
							if(Window.dumpLumpIsSelected()) {
								FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
								initExceptionDebug.write(readLump(offset, length));
								initExceptionDebug.close();
							}
							throw e;
						}
						
						// Lump 13
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						try {
							BSPObject.setBrushes(readLump(offset, length));
						} catch(java.lang.Exception e) {
							if(Window.dumpLumpIsSelected()) {
								FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
								initExceptionDebug.write(readLump(offset, length));
								initExceptionDebug.close();
							}
							throw e;
						}
						
						offsetReader.skip(8);
						
						// Lump 15
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						try {
							BSPObject.setModels(readLump(offset, length));
						} catch(java.lang.Exception e) {
							if(Window.dumpLumpIsSelected()) {
								FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
								initExceptionDebug.write(readLump(offset, length));
								initExceptionDebug.close();
							}
							throw e;
						}
						
						// Lump 16
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						try {
							BSPObject.setEntities(readLump(offset, length));
						} catch(java.lang.Exception e) {
							if(Window.dumpLumpIsSelected()) {
								FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
								initExceptionDebug.write(readLump(offset, length));
								initExceptionDebug.close();
							}
							throw e;
						}
						break;
					case BSP.TYPE_MOHAA:
						Window.println("MOHAA BSP (modified id Tech 3)",Window.VERBOSITY_ALWAYS);
						offsetReader.skip(12); // Skip the file header, putting the reader into the offset/length pairs
						
						// Lump 00
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						try {
							BSPObject.setTextures(readLump(offset, length));
						} catch(java.lang.Exception e) {
							if(Window.dumpLumpIsSelected()) {
								FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
								initExceptionDebug.write(readLump(offset, length));
								initExceptionDebug.close();
							}
							throw e;
						}
						
						// Lump 01
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						try {
							BSPObject.setPlanes(readLump(offset, length));
						} catch(java.lang.Exception e) {
							if(Window.dumpLumpIsSelected()) {
								FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
								initExceptionDebug.write(readLump(offset, length));
								initExceptionDebug.close();
							}
							throw e;
						}
						
						offsetReader.skip(72); // Do not need offset/length for lumps 2-10
						
						// Lump 11
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						try {
							BSPObject.setBrushSides(readLump(offset, length));
						} catch(java.lang.Exception e) {
							if(Window.dumpLumpIsSelected()) {
								FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
								initExceptionDebug.write(readLump(offset, length));
								initExceptionDebug.close();
							}
							throw e;
						}
						
						// Lump 12
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						try {
							BSPObject.setBrushes(readLump(offset, length));
						} catch(java.lang.Exception e) {
							if(Window.dumpLumpIsSelected()) {
								FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
								initExceptionDebug.write(readLump(offset, length));
								initExceptionDebug.close();
							}
							throw e;
						}
						
						// Lump 13
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						try {
							BSPObject.setModels(readLump(offset, length));
						} catch(java.lang.Exception e) {
							if(Window.dumpLumpIsSelected()) {
								FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
								initExceptionDebug.write(readLump(offset, length));
								initExceptionDebug.close();
							}
							throw e;
						}
						
						// Lump 14
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						try {
							BSPObject.setEntities(readLump(offset, length));
						} catch(java.lang.Exception e) {
							if(Window.dumpLumpIsSelected()) {
								FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
								initExceptionDebug.write(readLump(offset, length));
								initExceptionDebug.close();
							}
							throw e;
						}
						
						/*TODO
						offsetReader.skip(80); // Do not need offset/length for lumps 15-24
						
						// Lump 24
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						try {
							BSPObject.setStaticProps(readLump(offset, length));
						} catch(java.lang.Exception e) {
							if(Window.dumpLumpIsSelected()) {
								FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
								initExceptionDebug.write(readLump(offset, length));
								initExceptionDebug.close();
							}
							throw e;
						}*/
						break;
					case BSP.TYPE_FAKK:
						Window.println("Heavy Metal FAKK² BSP",Window.VERBOSITY_ALWAYS);
						offsetReader.skip(12); // Skip the file header, putting the reader into the lump directory
						
						// Lump 00
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						try {
							BSPObject.setTextures(readLump(offset, length));
						} catch(java.lang.Exception e) {
							if(Window.dumpLumpIsSelected()) {
								FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
								initExceptionDebug.write(readLump(offset, length));
								initExceptionDebug.close();
							}
							throw e;
						}
						
						// Lump 01
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						try {
							BSPObject.setPlanes(readLump(offset, length));
						} catch(java.lang.Exception e) {
							if(Window.dumpLumpIsSelected()) {
								FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
								initExceptionDebug.write(readLump(offset, length));
								initExceptionDebug.close();
							}
							throw e;
						}
						
						offsetReader.skip(64);
						
						// Lump 10
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						try {
							BSPObject.setBrushSides(readLump(offset, length));
						} catch(java.lang.Exception e) {
							if(Window.dumpLumpIsSelected()) {
								FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
								initExceptionDebug.write(readLump(offset, length));
								initExceptionDebug.close();
							}
							throw e;
						}
						
						// Lump 11
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						try {
							BSPObject.setBrushes(readLump(offset, length));
						} catch(java.lang.Exception e) {
							if(Window.dumpLumpIsSelected()) {
								FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
								initExceptionDebug.write(readLump(offset, length));
								initExceptionDebug.close();
							}
							throw e;
						}
						
						offsetReader.skip(8);
						
						// Lump 13
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						try {
							BSPObject.setModels(readLump(offset, length));
						} catch(java.lang.Exception e) {
							if(Window.dumpLumpIsSelected()) {
								FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
								initExceptionDebug.write(readLump(offset, length));
								initExceptionDebug.close();
							}
							throw e;
						}
						
						// Lump 14
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						try {
							BSPObject.setEntities(readLump(offset, length));
						} catch(java.lang.Exception e) {
							if(Window.dumpLumpIsSelected()) {
								FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
								initExceptionDebug.write(readLump(offset, length));
								initExceptionDebug.close();
							}
							throw e;
						}
						break;
					case BSP.TYPE_SIN:
						Window.println("SiN BSP (Modified Quake 2)",Window.VERBOSITY_ALWAYS);
						offsetReader.skip(8); // Skip the file header, putting the reader into the offset/length pairs
						
						// Lump 00
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						try {
							BSPObject.setEntities(readLump(offset, length));
						} catch(java.lang.Exception e) {
							if(Window.dumpLumpIsSelected()) {
								FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
								initExceptionDebug.write(readLump(offset, length));
								initExceptionDebug.close();
							}
							throw e;
						}
						
						// Lump 01
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						try {
							BSPObject.setPlanes(readLump(offset, length));
						} catch(java.lang.Exception e) {
							if(Window.dumpLumpIsSelected()) {
								FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
								initExceptionDebug.write(readLump(offset, length));
								initExceptionDebug.close();
							}
							throw e;
						}
						
						// Lump 02
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						try {
							BSPObject.setVertices(readLump(offset, length));
						} catch(java.lang.Exception e) {
							if(Window.dumpLumpIsSelected()) {
								FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
								initExceptionDebug.write(readLump(offset, length));
								initExceptionDebug.close();
							}
							throw e;
						}
						
						offsetReader.skip(8); // Do not need offset/length for lump 3
						
						// Lump 04
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						try {
							BSPObject.setNodes(readLump(offset, length));
						} catch(java.lang.Exception e) {
							if(Window.dumpLumpIsSelected()) {
								FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
								initExceptionDebug.write(readLump(offset, length));
								initExceptionDebug.close();
							}
							throw e;
						}
						
						// Lump 05
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						try {
							BSPObject.setTextures(readLump(offset, length));
						} catch(java.lang.Exception e) {
							if(Window.dumpLumpIsSelected()) {
								FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
								initExceptionDebug.write(readLump(offset, length));
								initExceptionDebug.close();
							}
							throw e;
						}
						
						// Lump 06
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						try {
							BSPObject.setFaces(readLump(offset, length));
						} catch(java.lang.Exception e) {
							if(Window.dumpLumpIsSelected()) {
								FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
								initExceptionDebug.write(readLump(offset, length));
								initExceptionDebug.close();
							}
							throw e;
						}
						
						offsetReader.skip(8); // Do not need offset/length for lump 7
						
						// Lump 08
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						try {
							BSPObject.setLeaves(readLump(offset, length));
						} catch(java.lang.Exception e) {
							if(Window.dumpLumpIsSelected()) {
								FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
								initExceptionDebug.write(readLump(offset, length));
								initExceptionDebug.close();
							}
							throw e;
						}
						
						offsetReader.skip(8); // Do not need offset/length for lump 9
								
						// Lump 10
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						try {
							BSPObject.setMarkBrushes(readLump(offset, length));
						} catch(java.lang.Exception e) {
							if(Window.dumpLumpIsSelected()) {
								FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
								initExceptionDebug.write(readLump(offset, length));
								initExceptionDebug.close();
							}
							throw e;
						}
						
						// Lump 11
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						try {
							BSPObject.setEdges(readLump(offset, length), version);
						} catch(java.lang.Exception e) {
							if(Window.dumpLumpIsSelected()) {
								FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
								initExceptionDebug.write(readLump(offset, length));
								initExceptionDebug.close();
							}
							throw e;
						}
						
						// Lump 12
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						try {
							BSPObject.setSurfEdges(readLump(offset, length));
						} catch(java.lang.Exception e) {
							if(Window.dumpLumpIsSelected()) {
								FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
								initExceptionDebug.write(readLump(offset, length));
								initExceptionDebug.close();
							}
							throw e;
						}
						
						// Lump 13
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						try {
							BSPObject.setModels(readLump(offset, length));
						} catch(java.lang.Exception e) {
							if(Window.dumpLumpIsSelected()) {
								FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
								initExceptionDebug.write(readLump(offset, length));
								initExceptionDebug.close();
							}
							throw e;
						}
						
						// Lump 14
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						try {
							BSPObject.setBrushes(readLump(offset, length));
						} catch(java.lang.Exception e) {
							if(Window.dumpLumpIsSelected()) {
								FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
								initExceptionDebug.write(readLump(offset, length));
								initExceptionDebug.close();
							}
							throw e;
						}
						
						// Lump 15
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						try {
							BSPObject.setBrushSides(readLump(offset, length));
						} catch(java.lang.Exception e) {
							if(Window.dumpLumpIsSelected()) {
								FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
								initExceptionDebug.write(readLump(offset, length));
								initExceptionDebug.close();
							}
							throw e;
						}
						break;
					case BSP.TYPE_DAIKATANA:
						Window.println("Daikatana BSP (Modified Quake 2)",Window.VERBOSITY_ALWAYS);
						offsetReader.skip(8); // Skip the file header, putting the reader into the offset/length pairs
						
						// Lump 00
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						try {
							BSPObject.setEntities(readLump(offset, length));
						} catch(java.lang.Exception e) {
							if(Window.dumpLumpIsSelected()) {
								FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
								initExceptionDebug.write(readLump(offset, length));
								initExceptionDebug.close();
							}
							throw e;
						}
						
						// Lump 01
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						try {
							BSPObject.setPlanes(readLump(offset, length));
						} catch(java.lang.Exception e) {
							if(Window.dumpLumpIsSelected()) {
								FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
								initExceptionDebug.write(readLump(offset, length));
								initExceptionDebug.close();
							}
							throw e;
						}
						
						// Lump 02
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						try {
							BSPObject.setVertices(readLump(offset, length));
						} catch(java.lang.Exception e) {
							if(Window.dumpLumpIsSelected()) {
								FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
								initExceptionDebug.write(readLump(offset, length));
								initExceptionDebug.close();
							}
							throw e;
						}
						
						offsetReader.skip(8); // Do not need offset/length for lump 3
						
						// Lump 04
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						try {
							BSPObject.setNodes(readLump(offset, length));
						} catch(java.lang.Exception e) {
							if(Window.dumpLumpIsSelected()) {
								FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
								initExceptionDebug.write(readLump(offset, length));
								initExceptionDebug.close();
							}
							throw e;
						}
						
						// Lump 05
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						try {
							BSPObject.setTextures(readLump(offset, length));
						} catch(java.lang.Exception e) {
							if(Window.dumpLumpIsSelected()) {
								FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
								initExceptionDebug.write(readLump(offset, length));
								initExceptionDebug.close();
							}
							throw e;
						}
						
						// Lump 06
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						try {
							BSPObject.setFaces(readLump(offset, length));
						} catch(java.lang.Exception e) {
							if(Window.dumpLumpIsSelected()) {
								FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
								initExceptionDebug.write(readLump(offset, length));
								initExceptionDebug.close();
							}
							throw e;
						}
						
						offsetReader.skip(8); // Do not need offset/length for lump 7
						
						// Lump 08
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						try {
							BSPObject.setLeaves(readLump(offset, length));
						} catch(java.lang.Exception e) {
							if(Window.dumpLumpIsSelected()) {
								FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
								initExceptionDebug.write(readLump(offset, length));
								initExceptionDebug.close();
							}
							throw e;
						}
						
						offsetReader.skip(8); // Do not need offset/length for lump 9
								
						// Lump 10
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						try {
							BSPObject.setMarkBrushes(readLump(offset, length));
						} catch(java.lang.Exception e) {
							if(Window.dumpLumpIsSelected()) {
								FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
								initExceptionDebug.write(readLump(offset, length));
								initExceptionDebug.close();
							}
							throw e;
						}
						
						// Lump 11
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						try {
							BSPObject.setEdges(readLump(offset, length), version);
						} catch(java.lang.Exception e) {
							if(Window.dumpLumpIsSelected()) {
								FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
								initExceptionDebug.write(readLump(offset, length));
								initExceptionDebug.close();
							}
							throw e;
						}
						
						// Lump 12
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						try {
							BSPObject.setSurfEdges(readLump(offset, length));
						} catch(java.lang.Exception e) {
							if(Window.dumpLumpIsSelected()) {
								FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
								initExceptionDebug.write(readLump(offset, length));
								initExceptionDebug.close();
							}
							throw e;
						}
						
						// Lump 13
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						try {
							BSPObject.setModels(readLump(offset, length));
						} catch(java.lang.Exception e) {
							if(Window.dumpLumpIsSelected()) {
								FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
								initExceptionDebug.write(readLump(offset, length));
								initExceptionDebug.close();
							}
							throw e;
						}
						
						// Lump 14
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						try {
							BSPObject.setBrushes(readLump(offset, length));
						} catch(java.lang.Exception e) {
							if(Window.dumpLumpIsSelected()) {
								FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
								initExceptionDebug.write(readLump(offset, length));
								initExceptionDebug.close();
							}
							throw e;
						}
						
						// Lump 15
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						try {
							BSPObject.setBrushSides(readLump(offset, length));
						} catch(java.lang.Exception e) {
							if(Window.dumpLumpIsSelected()) {
								FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
								initExceptionDebug.write(readLump(offset, length));
								initExceptionDebug.close();
							}
							throw e;
						}
						break;
					case BSP.TYPE_RAVEN:
						Window.println("Raven Software BSP (Modified id Tech 3)",Window.VERBOSITY_ALWAYS);
						offsetReader.skip(8); // Skip the file header, putting the reader into the offset/length pairs
						
						// Lump 00
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						try {
							BSPObject.setEntities(readLump(offset, length));
						} catch(java.lang.Exception e) {
							if(Window.dumpLumpIsSelected()) {
								FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
								initExceptionDebug.write(readLump(offset, length));
								initExceptionDebug.close();
							}
							throw e;
						}
						
						// Lump 01
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						try {
							BSPObject.setTextures(readLump(offset, length));
						} catch(java.lang.Exception e) {
							if(Window.dumpLumpIsSelected()) {
								FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
								initExceptionDebug.write(readLump(offset, length));
								initExceptionDebug.close();
							}
							throw e;
						}
						
						// Lump 02
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						try {
							BSPObject.setPlanes(readLump(offset, length));
						} catch(java.lang.Exception e) {
							if(Window.dumpLumpIsSelected()) {
								FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
								initExceptionDebug.write(readLump(offset, length));
								initExceptionDebug.close();
							}
							throw e;
						}
						
						offsetReader.skip(32); // Do not need offset/length for lumps 3-6
						
						// Lump 07
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						try {
							BSPObject.setModels(readLump(offset, length));
						} catch(java.lang.Exception e) {
							if(Window.dumpLumpIsSelected()) {
								FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
								initExceptionDebug.write(readLump(offset, length));
								initExceptionDebug.close();
							}
							throw e;
						}
						
						// Lump 08
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						try {
							BSPObject.setBrushes(readLump(offset, length));
						} catch(java.lang.Exception e) {
							if(Window.dumpLumpIsSelected()) {
								FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
								initExceptionDebug.write(readLump(offset, length));
								initExceptionDebug.close();
							}
							throw e;
						}
						
						// Lump 09
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						try {
							BSPObject.setBrushSides(readLump(offset, length));
						} catch(java.lang.Exception e) {
							if(Window.dumpLumpIsSelected()) {
								FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
								initExceptionDebug.write(readLump(offset, length));
								initExceptionDebug.close();
							}
							throw e;
						}
						
						// Lump 10
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						try {
							BSPObject.setVertices(readLump(offset, length));
						} catch(java.lang.Exception e) {
							if(Window.dumpLumpIsSelected()) {
								FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
								initExceptionDebug.write(readLump(offset, length));
								initExceptionDebug.close();
							}
							throw e;
						}
						
						offsetReader.skip(16); // Do not need offset/length for lumps 11 and 12
						
						// Lump 13
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						try {
							BSPObject.setFaces(readLump(offset, length));
						} catch(java.lang.Exception e) {
							if(Window.dumpLumpIsSelected()) {
								FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
								initExceptionDebug.write(readLump(offset, length));
								initExceptionDebug.close();
							}
							throw e;
						}
						break;
					case BSP.TYPE_SOURCE17:
					case BSP.TYPE_SOURCE18:
					case BSP.TYPE_SOURCE19:
					case BSP.TYPE_SOURCE20:
					case BSP.TYPE_SOURCE21:
					case BSP.TYPE_SOURCE22:
					case BSP.TYPE_SOURCE23:
					case BSP.TYPE_DMOMAM:
					case BSP.TYPE_TACTICALINTERVENTION:
						Window.println("Source BSP",Window.VERBOSITY_ALWAYS);
						// Left 4 Dead 2, for some reason, made the order "version, offset, length" for lump header structure,
						// rather than the usual "offset, length, version". I guess someone at Valve got bored.
						boolean isL4D2=false;
						int lumpVersion=0;
						offsetReader.skip(8); // Skip the VBSP and version number
						
						// Lump 00
						offsetReader.read(read); // Read 4 bytes
						if(version==BSP.TYPE_TACTICALINTERVENTION) {
							read=tidecryptor.decrypt(read,8);
						}
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						if(version==BSP.TYPE_TACTICALINTERVENTION) {
							read=tidecryptor.decrypt(read,12);
						}
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						if(version==BSP.TYPE_TACTICALINTERVENTION) {
							read=tidecryptor.decrypt(read,16);
						}
						lumpVersion=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.skip(4);
						if(offset<1036) { // This is less than the total length of the file header. Probably indicates a L4D2 map.
							isL4D2=true;   // Although unused lumps have everything set to 0 in their info, Entities are NEVER EVER UNUSED! EVER!
						}                 // A BSP file without entities is geometry with no life, no worldspawn. That's never acceptable.
						if(isL4D2) {
							try {
								BSPObject.setEntities(readLump(length, lumpVersion));
							} catch(java.lang.Exception e) {
								if(Window.dumpLumpIsSelected()) {
									FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
									initExceptionDebug.write(readLump(length, lumpVersion));
									initExceptionDebug.close();
								}
								throw e;
							}
						} else {
							try {
								BSPObject.setEntities(readLump(offset, length));
							} catch(java.lang.Exception e) {
								if(Window.dumpLumpIsSelected()) {
									FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
									initExceptionDebug.write(readLump(offset, length));
									initExceptionDebug.close();
								}
								throw e;
							}
						}
						
						// Lump 35, Game lump
						// Need to handle this here in order to detect Vindictus maps.
						// This lump SUCKS. It's a lump containing nested lumps for game specific data.
						// What we need out of it is the static prop lump.
						FileInputStream gamelumpStream=new FileInputStream(BSPFile);
						gamelumpStream.skip(568);
						gamelumpStream.read(read); // Read 4 bytes
						if(version==BSP.TYPE_TACTICALINTERVENTION) {
							read=tidecryptor.decrypt(read,24);
						}
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						gamelumpStream.read(read); // Read 4 more bytes
						if(version==BSP.TYPE_TACTICALINTERVENTION) {
							read=tidecryptor.decrypt(read,28);
						}
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						gamelumpStream.read(read); // Read 4 more bytes
						if(version==BSP.TYPE_TACTICALINTERVENTION) {
							read=tidecryptor.decrypt(read,0);
						}
						lumpVersion=DataReader.readInt(read[0], read[1], read[2], read[3]);
						gamelumpStream.close();
						byte[] gamelumpData;
						int gamelumpFileOffset;
						if(isL4D2) {
							gamelumpData=readLump(length, lumpVersion);
							gamelumpFileOffset=length;
						} else {
							gamelumpData=readLump(offset, length);
							gamelumpFileOffset=offset;
						}
						try {
							int numGamelumps=DataReader.readInt(gamelumpData[0], gamelumpData[1], gamelumpData[2], gamelumpData[3]);
							int gamelumpOffset=4;
							if(version==BSP.TYPE_DMOMAM) {
								int next4=DataReader.readInt(gamelumpData[4], gamelumpData[5], gamelumpData[6], gamelumpData[7]);
								if(next4==1) {
									gamelumpOffset+=4;
								} else {
									version=BSP.TYPE_SOURCE20;
									BSPObject.setVersion(BSP.TYPE_SOURCE20);
								}
							}
							if(numGamelumps>1) {
								byte[] staticPropLump=new byte[0];
								int staticPropLumpVersion=0;
								boolean isRelativeToLumpStart=false;
								for(int i=0;i<numGamelumps;i++) {
									int gamelumpID=DataReader.readInt(gamelumpData[gamelumpOffset], gamelumpData[gamelumpOffset+1], gamelumpData[gamelumpOffset+2], gamelumpData[gamelumpOffset+3]);
									int gamelumpVersion=(int)DataReader.readShort(gamelumpData[gamelumpOffset+6], gamelumpData[gamelumpOffset+7]);
									int internalOffset=DataReader.readInt(gamelumpData[gamelumpOffset+8], gamelumpData[gamelumpOffset+9], gamelumpData[gamelumpOffset+10], gamelumpData[gamelumpOffset+11]);
									int internalLength=DataReader.readInt(gamelumpData[gamelumpOffset+12], gamelumpData[gamelumpOffset+13], gamelumpData[gamelumpOffset+14], gamelumpData[gamelumpOffset+15]);
									if((internalOffset<4+(16*numGamelumps) && version==BSP.TYPE_SOURCE20 && readAs==-1) || version==BSP.TYPE_VINDICTUS || readAs==BSP.TYPE_VINDICTUS) { // Even if the offset is relative to start of game lump, it will never be below this. If it is, it uses this format instead.
										BSPObject.setVersion(BSP.TYPE_VINDICTUS);
										version=BSP.TYPE_VINDICTUS;
										gamelumpVersion=DataReader.readInt(gamelumpData[gamelumpOffset+8], gamelumpData[gamelumpOffset+9], gamelumpData[gamelumpOffset+10], gamelumpData[gamelumpOffset+11]);
										internalOffset=DataReader.readInt(gamelumpData[gamelumpOffset+12], gamelumpData[gamelumpOffset+13], gamelumpData[gamelumpOffset+14], gamelumpData[gamelumpOffset+15]);
										internalLength=DataReader.readInt(gamelumpData[gamelumpOffset+16], gamelumpData[gamelumpOffset+17], gamelumpData[gamelumpOffset+18], gamelumpData[gamelumpOffset+19]);
										gamelumpOffset+=20;
									} else {
										gamelumpOffset+=16;
										if(version==BSP.TYPE_DMOMAM) {
											gamelumpOffset+=4;
										}
									}
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
								try {
									BSPObject.setStaticProps(staticPropLump, staticPropLumpVersion);
								} catch(java.lang.Exception e) {
									if(Window.dumpLumpIsSelected()) {
										FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
										initExceptionDebug.write(staticPropLump);
										initExceptionDebug.close();
									}
									throw e;
								}
							}
						} catch(java.lang.Exception e) {
							if(Window.dumpLumpIsSelected()) {
								FileOutputStream penisinitExceptionDebug=new FileOutputStream("GameLump"+lumpVersion+".lmp");
								penisinitExceptionDebug.write(gamelumpData);
								penisinitExceptionDebug.close();
							}
							throw e;
						}
						
						// Lump 01
						offsetReader.read(read); // Read 4 bytes
						if(version==BSP.TYPE_TACTICALINTERVENTION) {
							read=tidecryptor.decrypt(read,24);
						}
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						if(version==BSP.TYPE_TACTICALINTERVENTION) {
							read=tidecryptor.decrypt(read,28);
						}
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						if(version==BSP.TYPE_TACTICALINTERVENTION) {
							read=tidecryptor.decrypt(read,0);
						}
						lumpVersion=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.skip(4);
						if(isL4D2) {
							try {
								BSPObject.setPlanes(readLump(length, lumpVersion));
							} catch(java.lang.Exception e) {
								if(Window.dumpLumpIsSelected()) {
									FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
									initExceptionDebug.write(readLump(length, lumpVersion));
									initExceptionDebug.close();
								}
								throw e;
							}
						} else {
							try {
								BSPObject.setPlanes(readLump(offset, length));
							} catch(java.lang.Exception e) {
								if(Window.dumpLumpIsSelected()) {
									FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
									initExceptionDebug.write(readLump(offset, length));
									initExceptionDebug.close();
								}
								throw e;
							}
						}
						
						// Lump 02
						offsetReader.read(read); // Read 4 bytes
						if(version==BSP.TYPE_TACTICALINTERVENTION) {
							read=tidecryptor.decrypt(read,8);
						}
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						if(version==BSP.TYPE_TACTICALINTERVENTION) {
							read=tidecryptor.decrypt(read,12);
						}
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						if(version==BSP.TYPE_TACTICALINTERVENTION) {
							read=tidecryptor.decrypt(read,16);
						}
						lumpVersion=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.skip(4);
						if(isL4D2) {
							try {
								BSPObject.setTexDatas(readLump(length, lumpVersion));
							} catch(java.lang.Exception e) {
								if(Window.dumpLumpIsSelected()) {
									FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
									initExceptionDebug.write(readLump(length, lumpVersion));
									initExceptionDebug.close();
								}
								throw e;
							}
						} else {
							try {
								BSPObject.setTexDatas(readLump(offset, length));
							} catch(java.lang.Exception e) {
								if(Window.dumpLumpIsSelected()) {
									FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
									initExceptionDebug.write(readLump(offset, length));
									initExceptionDebug.close();
								}
								throw e;
							}
						}
						
						// Lump 03
						offsetReader.read(read); // Read 4 bytes
						if(version==BSP.TYPE_TACTICALINTERVENTION) {
							read=tidecryptor.decrypt(read,24);
						}
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						if(version==BSP.TYPE_TACTICALINTERVENTION) {
							read=tidecryptor.decrypt(read,28);
						}
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						if(version==BSP.TYPE_TACTICALINTERVENTION) {
							read=tidecryptor.decrypt(read,0);
						}
						lumpVersion=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.skip(4);
						if(isL4D2) {
							try {
								BSPObject.setVertices(readLump(length, lumpVersion));
							} catch(java.lang.Exception e) {
								if(Window.dumpLumpIsSelected()) {
									FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
									initExceptionDebug.write(readLump(length, lumpVersion));
									initExceptionDebug.close();
								}
								throw e;
							}
						} else {
							try {
								BSPObject.setVertices(readLump(offset, length));
							} catch(java.lang.Exception e) {
								if(Window.dumpLumpIsSelected()) {
									FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
									initExceptionDebug.write(readLump(offset, length));
									initExceptionDebug.close();
								}
								throw e;
							}
						}
						
						offsetReader.skip(16); // Skip lump 4 data
						
						// Lump 05
						offsetReader.read(read); // Read 4 bytes
						if(version==BSP.TYPE_TACTICALINTERVENTION) {
							read=tidecryptor.decrypt(read,24);
						}
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						if(version==BSP.TYPE_TACTICALINTERVENTION) {
							read=tidecryptor.decrypt(read,28);
						}
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						if(version==BSP.TYPE_TACTICALINTERVENTION) {
							read=tidecryptor.decrypt(read,0);
						}
						lumpVersion=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.skip(4);
						if(isL4D2) {
							try {
								BSPObject.setNodes(readLump(length, lumpVersion));
							} catch(java.lang.Exception e) {
								if(Window.dumpLumpIsSelected()) {
									FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
									initExceptionDebug.write(readLump(length, lumpVersion));
									initExceptionDebug.close();
								}
								throw e;
							}
						} else {
							try {
								BSPObject.setNodes(readLump(offset, length));
							} catch(java.lang.Exception e) {
								if(Window.dumpLumpIsSelected()) {
									FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
									initExceptionDebug.write(readLump(offset, length));
									initExceptionDebug.close();
								}
								throw e;
							}
						}
						
						// Lump 06
						offsetReader.read(read); // Read 4 bytes
						if(version==BSP.TYPE_TACTICALINTERVENTION) {
							read=tidecryptor.decrypt(read,8);
						}
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						if(version==BSP.TYPE_TACTICALINTERVENTION) {
							read=tidecryptor.decrypt(read,12);
						}
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						if(version==BSP.TYPE_TACTICALINTERVENTION) {
							read=tidecryptor.decrypt(read,16);
						}
						lumpVersion=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.skip(4);
						if(isL4D2) {
							try {
								BSPObject.setTexInfo(readLump(length, lumpVersion));
							} catch(java.lang.Exception e) {
								if(Window.dumpLumpIsSelected()) {
									FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
									initExceptionDebug.write(readLump(length, lumpVersion));
									initExceptionDebug.close();
								}
								throw e;
							}
						} else {
							try {
								BSPObject.setTexInfo(readLump(offset, length));
							} catch(java.lang.Exception e) {
								if(Window.dumpLumpIsSelected()) {
									FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
									initExceptionDebug.write(readLump(offset, length));
									initExceptionDebug.close();
								}
								throw e;
							}
						}
						
						// Lump 07
						offsetReader.read(read); // Read 4 bytes
						if(version==BSP.TYPE_TACTICALINTERVENTION) {
							read=tidecryptor.decrypt(read,24);
						}
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						if(version==BSP.TYPE_TACTICALINTERVENTION) {
							read=tidecryptor.decrypt(read,28);
						}
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						if(version==BSP.TYPE_TACTICALINTERVENTION) {
							read=tidecryptor.decrypt(read,0);
						}
						lumpVersion=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.skip(4);
						if(isL4D2) {
							try {
								BSPObject.setFaces(readLump(length, lumpVersion));
							} catch(java.lang.Exception e) {
								if(Window.dumpLumpIsSelected()) {
									FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
									initExceptionDebug.write(readLump(length, lumpVersion));
									initExceptionDebug.close();
								}
								throw e;
							}
						} else {
							try {
								BSPObject.setFaces(readLump(offset, length));
							} catch(java.lang.Exception e) {
								if(Window.dumpLumpIsSelected()) {
									FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
									initExceptionDebug.write(readLump(offset, length));
									initExceptionDebug.close();
								}
								throw e;
							}
						}
						
						offsetReader.skip(32); // skip lump 8 and 9 data
						
						// Lump 10
						offsetReader.read(read); // Read 4 bytes
						if(version==BSP.TYPE_TACTICALINTERVENTION) {
							read=tidecryptor.decrypt(read,8);
						}
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						if(version==BSP.TYPE_TACTICALINTERVENTION) {
							read=tidecryptor.decrypt(read,12);
						}
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						if(version==BSP.TYPE_TACTICALINTERVENTION) {
							read=tidecryptor.decrypt(read,16);
						}
						lumpVersion=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.skip(4);
						if(isL4D2) {
							try {
								BSPObject.setLeaves(readLump(length, lumpVersion));
							} catch(java.lang.Exception e) {
								if(Window.dumpLumpIsSelected()) {
									FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
									initExceptionDebug.write(readLump(length, lumpVersion));
									initExceptionDebug.close();
								}
								throw e;
							}
						} else {
							try {
								BSPObject.setLeaves(readLump(offset, length));
							} catch(java.lang.Exception e) {
								if(Window.dumpLumpIsSelected()) {
									FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
									initExceptionDebug.write(readLump(offset, length));
									initExceptionDebug.close();
								}
								throw e;
							}
						}
						
						offsetReader.skip(16);
						
						// Lump 12
						offsetReader.read(read); // Read 4 bytes
						if(version==BSP.TYPE_TACTICALINTERVENTION) {
							read=tidecryptor.decrypt(read,8);
						}
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						if(version==BSP.TYPE_TACTICALINTERVENTION) {
							read=tidecryptor.decrypt(read,12);
						}
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						if(version==BSP.TYPE_TACTICALINTERVENTION) {
							read=tidecryptor.decrypt(read,16);
						}
						lumpVersion=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.skip(4);
						if(isL4D2) {
							try {
								BSPObject.setEdges(readLump(length, lumpVersion), version);
							} catch(java.lang.Exception e) {
								if(Window.dumpLumpIsSelected()) {
									FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
									initExceptionDebug.write(readLump(length, lumpVersion));
									initExceptionDebug.close();
								}
								throw e;
							}
						} else {
							try {
								BSPObject.setEdges(readLump(offset, length), version);
							} catch(java.lang.Exception e) {
								if(Window.dumpLumpIsSelected()) {
									FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
									initExceptionDebug.write(readLump(offset, length));
									initExceptionDebug.close();
								}
								throw e;
							}
						}
						
						// Lump 13
						offsetReader.read(read); // Read 4 bytes
						if(version==BSP.TYPE_TACTICALINTERVENTION) {
							read=tidecryptor.decrypt(read,24);
						}
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						if(version==BSP.TYPE_TACTICALINTERVENTION) {
							read=tidecryptor.decrypt(read,28);
						}
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						if(version==BSP.TYPE_TACTICALINTERVENTION) {
							read=tidecryptor.decrypt(read,0);
						}
						lumpVersion=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.skip(4);
						if(isL4D2) {
							try {
								BSPObject.setSurfEdges(readLump(length, lumpVersion));
							} catch(java.lang.Exception e) {
								if(Window.dumpLumpIsSelected()) {
									FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
									initExceptionDebug.write(readLump(length, lumpVersion));
									initExceptionDebug.close();
								}
								throw e;
							}
						} else {
							try {
								BSPObject.setSurfEdges(readLump(offset, length));
							} catch(java.lang.Exception e) {
								if(Window.dumpLumpIsSelected()) {
									FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
									initExceptionDebug.write(readLump(offset, length));
									initExceptionDebug.close();
								}
								throw e;
							}
						}
						
						// Lump 14
						offsetReader.read(read); // Read 4 bytes
						if(version==BSP.TYPE_TACTICALINTERVENTION) {
							read=tidecryptor.decrypt(read,8);
						}
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						if(version==BSP.TYPE_TACTICALINTERVENTION) {
							read=tidecryptor.decrypt(read,12);
						}
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						if(version==BSP.TYPE_TACTICALINTERVENTION) {
							read=tidecryptor.decrypt(read,16);
						}
						lumpVersion=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.skip(4);
						if(isL4D2) {
							try {
								BSPObject.setModels(readLump(length, lumpVersion));
							} catch(java.lang.Exception e) {
								if(Window.dumpLumpIsSelected()) {
									FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
									initExceptionDebug.write(readLump(length, lumpVersion));
									initExceptionDebug.close();
								}
								throw e;
							}
						} else {
							try {
								BSPObject.setModels(readLump(offset, length));
							} catch(java.lang.Exception e) {
								if(Window.dumpLumpIsSelected()) {
									FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
									initExceptionDebug.write(readLump(offset, length));
									initExceptionDebug.close();
								}
								throw e;
							}
						}
						
						offsetReader.skip(32); // Skip lumps 15 and 16
						
						// Lump 17
						offsetReader.read(read); // Read 4 bytes
						if(version==BSP.TYPE_TACTICALINTERVENTION) {
							read=tidecryptor.decrypt(read,24);
						}
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						if(version==BSP.TYPE_TACTICALINTERVENTION) {
							read=tidecryptor.decrypt(read,28);
						}
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						if(version==BSP.TYPE_TACTICALINTERVENTION) {
							read=tidecryptor.decrypt(read,0);
						}
						lumpVersion=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.skip(4);
						if(isL4D2) {
							try {
								BSPObject.setMarkBrushes(readLump(length, lumpVersion));
							} catch(java.lang.Exception e) {
								if(Window.dumpLumpIsSelected()) {
									FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
									initExceptionDebug.write(readLump(length, lumpVersion));
									initExceptionDebug.close();
								}
								throw e;
							}
						} else {
							try {
								BSPObject.setMarkBrushes(readLump(offset, length));
							} catch(java.lang.Exception e) {
								if(Window.dumpLumpIsSelected()) {
									FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
									initExceptionDebug.write(readLump(offset, length));
									initExceptionDebug.close();
								}
								throw e;
							}
						}
						
						// Lump 18
						offsetReader.read(read); // Read 4 bytes
						if(version==BSP.TYPE_TACTICALINTERVENTION) {
							read=tidecryptor.decrypt(read,8);
						}
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						if(version==BSP.TYPE_TACTICALINTERVENTION) {
							read=tidecryptor.decrypt(read,12);
						}
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						if(version==BSP.TYPE_TACTICALINTERVENTION) {
							read=tidecryptor.decrypt(read,16);
						}
						lumpVersion=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.skip(4);
						if(isL4D2) {
							try {
								BSPObject.setBrushes(readLump(length, lumpVersion));
							} catch(java.lang.Exception e) {
								if(Window.dumpLumpIsSelected()) {
									FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
									initExceptionDebug.write(readLump(length, lumpVersion));
									initExceptionDebug.close();
								}
								throw e;
							}
						} else {
							try {
								BSPObject.setBrushes(readLump(offset, length));
							} catch(java.lang.Exception e) {
								if(Window.dumpLumpIsSelected()) {
									FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
									initExceptionDebug.write(readLump(offset, length));
									initExceptionDebug.close();
								}
								throw e;
							}
						}
						
						// Lump 19
						offsetReader.read(read); // Read 4 bytes
						if(version==BSP.TYPE_TACTICALINTERVENTION) {
							read=tidecryptor.decrypt(read,24);
						}
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						if(version==BSP.TYPE_TACTICALINTERVENTION) {
							read=tidecryptor.decrypt(read,28);
						}
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						if(version==BSP.TYPE_TACTICALINTERVENTION) {
							read=tidecryptor.decrypt(read,0);
						}
						lumpVersion=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.skip(4);
						if(isL4D2) {
							try {
								BSPObject.setBrushSides(readLump(length, lumpVersion));
							} catch(java.lang.Exception e) {
								if(Window.dumpLumpIsSelected()) {
									FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
									initExceptionDebug.write(readLump(length, lumpVersion));
									initExceptionDebug.close();
								}
								throw e;
							}
						} else {
							try {
								BSPObject.setBrushSides(readLump(offset, length));
							} catch(java.lang.Exception e) {
								if(Window.dumpLumpIsSelected()) {
									FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
									initExceptionDebug.write(readLump(offset, length));
									initExceptionDebug.close();
								}
								throw e;
							}
						}
						
						offsetReader.skip(96); // Skip entries for lumps 20 21 22 23 24 25
						
						// Lump 26
						offsetReader.read(read); // Read 4 bytes
						if(version==BSP.TYPE_TACTICALINTERVENTION) {
							read=tidecryptor.decrypt(read,8);
						}
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						if(version==BSP.TYPE_TACTICALINTERVENTION) {
							read=tidecryptor.decrypt(read,12);
						}
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						if(version==BSP.TYPE_TACTICALINTERVENTION) {
							read=tidecryptor.decrypt(read,16);
						}
						lumpVersion=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.skip(4);
						if(isL4D2) {
							try {
								BSPObject.setDispInfos(readLump(length, lumpVersion));
							} catch(java.lang.Exception e) {
								if(Window.dumpLumpIsSelected()) {
									FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
									initExceptionDebug.write(readLump(length, lumpVersion));
									initExceptionDebug.close();
								}
								throw e;
							}
						} else {
							try {
								BSPObject.setDispInfos(readLump(offset, length));
							} catch(java.lang.Exception e) {
								if(Window.dumpLumpIsSelected()) {
									FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
									initExceptionDebug.write(readLump(offset, length));
									initExceptionDebug.close();
								}
								throw e;
							}
						}
						
						// Lump 27
						offsetReader.read(read); // Read 4 bytes
						if(version==BSP.TYPE_TACTICALINTERVENTION) {
							read=tidecryptor.decrypt(read,24);
						}
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						if(version==BSP.TYPE_TACTICALINTERVENTION) {
							read=tidecryptor.decrypt(read,28);
						}
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						if(version==BSP.TYPE_TACTICALINTERVENTION) {
							read=tidecryptor.decrypt(read,0);
						}
						lumpVersion=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.skip(4);
						if(isL4D2) {
							try {
								BSPObject.setOriginalFaces(readLump(length, lumpVersion));
							} catch(java.lang.Exception e) {
								if(Window.dumpLumpIsSelected()) {
									FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
									initExceptionDebug.write(readLump(length, lumpVersion));
									initExceptionDebug.close();
								}
								throw e;
							}
						} else {
							try {
								BSPObject.setOriginalFaces(readLump(offset, length));
							} catch(java.lang.Exception e) {
								if(Window.dumpLumpIsSelected()) {
									FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
									initExceptionDebug.write(readLump(offset, length));
									initExceptionDebug.close();
								}
								throw e;
							}
						}
						
						offsetReader.skip(80); // Lumps 28 29 30 31 32
						
						// Lump 33
						offsetReader.read(read); // Read 4 bytes
						if(version==BSP.TYPE_TACTICALINTERVENTION) {
							read=tidecryptor.decrypt(read,24);
						}
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						if(version==BSP.TYPE_TACTICALINTERVENTION) {
							read=tidecryptor.decrypt(read,28);
						}
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						if(version==BSP.TYPE_TACTICALINTERVENTION) {
							read=tidecryptor.decrypt(read,0);
						}
						lumpVersion=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.skip(4);
						if(isL4D2) {
							try {
								BSPObject.setDispVerts(readLump(length, lumpVersion));
							} catch(java.lang.Exception e) {
								if(Window.dumpLumpIsSelected()) {
									FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
									initExceptionDebug.write(readLump(length, lumpVersion));
									initExceptionDebug.close();
								}
								throw e;
							}
						} else {
							try {
								BSPObject.setDispVerts(readLump(offset, length));
							} catch(java.lang.Exception e) {
								if(Window.dumpLumpIsSelected()) {
									FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
									initExceptionDebug.write(readLump(offset, length));
									initExceptionDebug.close();
								}
								throw e;
							}
						}
						
						offsetReader.skip(96); // Did game lump after entities
						
						if(Window.extractZipIsSelected()) {
							// Lump 40
							offsetReader.read(read); // Read 4 bytes
							if(version==BSP.TYPE_TACTICALINTERVENTION) {
								read=tidecryptor.decrypt(read,8);
							}
							offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
							offsetReader.read(read); // Read 4 more bytes
							if(version==BSP.TYPE_TACTICALINTERVENTION) {
								read=tidecryptor.decrypt(read,12);
							}
							length=DataReader.readInt(read[0], read[1], read[2], read[3]);
							offsetReader.read(read); // Read 4 more bytes
							if(version==BSP.TYPE_TACTICALINTERVENTION) {
								read=tidecryptor.decrypt(read,16);
							}
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
									PAKWriter.write(readLump(length, lumpVersion));
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
						if(version==BSP.TYPE_TACTICALINTERVENTION) {
							read=tidecryptor.decrypt(read,8);
						}
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						if(version==BSP.TYPE_TACTICALINTERVENTION) {
							read=tidecryptor.decrypt(read,12);
						}
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						if(version==BSP.TYPE_TACTICALINTERVENTION) {
							read=tidecryptor.decrypt(read,16);
						}
						lumpVersion=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.skip(4);
						if(isL4D2) {
							try {
								BSPObject.setCubemaps(readLump(length, lumpVersion));
							} catch(java.lang.Exception e) {
								if(Window.dumpLumpIsSelected()) {
									FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
									initExceptionDebug.write(readLump(length, lumpVersion));
									initExceptionDebug.close();
								}
								throw e;
							}
						} else {
							try {
								BSPObject.setCubemaps(readLump(offset, length));
							} catch(java.lang.Exception e) {
								if(Window.dumpLumpIsSelected()) {
									FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
									initExceptionDebug.write(readLump(offset, length));
									initExceptionDebug.close();
								}
								throw e;
							}
						}
						
						// Lump 43
						offsetReader.read(read); // Read 4 bytes
						if(version==BSP.TYPE_TACTICALINTERVENTION) {
							read=tidecryptor.decrypt(read,24);
						}
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						if(version==BSP.TYPE_TACTICALINTERVENTION) {
							read=tidecryptor.decrypt(read,28);
						}
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						if(version==BSP.TYPE_TACTICALINTERVENTION) {
							read=tidecryptor.decrypt(read,0);
						}
						lumpVersion=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.skip(4);
						if(isL4D2) {
							try {
								BSPObject.setTextures(readLump(length, lumpVersion));
							} catch(java.lang.Exception e) {
								if(Window.dumpLumpIsSelected()) {
									FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
									initExceptionDebug.write(readLump(length, lumpVersion));
									initExceptionDebug.close();
								}
								throw e;
							}
						} else {
							try {
								BSPObject.setTextures(readLump(offset, length));
							} catch(java.lang.Exception e) {
								if(Window.dumpLumpIsSelected()) {
									FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
									initExceptionDebug.write(readLump(offset, length));
									initExceptionDebug.close();
								}
								throw e;
							}
						}
						
						// Lump 44
						offsetReader.read(read); // Read 4 bytes
						if(version==BSP.TYPE_TACTICALINTERVENTION) {
							read=tidecryptor.decrypt(read,8);
						}
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						if(version==BSP.TYPE_TACTICALINTERVENTION) {
							read=tidecryptor.decrypt(read,12);
						}
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						if(version==BSP.TYPE_TACTICALINTERVENTION) {
							read=tidecryptor.decrypt(read,16);
						}
						lumpVersion=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.skip(4);
						if(isL4D2) {
							try {
								BSPObject.setTexTable(readLump(length, lumpVersion));
							} catch(java.lang.Exception e) {
								if(Window.dumpLumpIsSelected()) {
									FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
									initExceptionDebug.write(readLump(length, lumpVersion));
									initExceptionDebug.close();
								}
								throw e;
							}
						} else {
							try {
								BSPObject.setTexTable(readLump(offset, length));
							} catch(java.lang.Exception e) {
								if(Window.dumpLumpIsSelected()) {
									FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
									initExceptionDebug.write(readLump(offset, length));
									initExceptionDebug.close();
								}
								throw e;
							}
						}
						
						offsetReader.skip(48);
						
						// Lump 48
						offsetReader.read(read); // Read 4 bytes
						if(version==BSP.TYPE_TACTICALINTERVENTION) {
							read=tidecryptor.decrypt(read,8);
						}
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						if(version==BSP.TYPE_TACTICALINTERVENTION) {
							read=tidecryptor.decrypt(read,12);
						}
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						if(version==BSP.TYPE_TACTICALINTERVENTION) {
							read=tidecryptor.decrypt(read,16);
						}
						lumpVersion=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.skip(4);
						if(isL4D2) {
							try {
								BSPObject.setDispTris(readLump(length, lumpVersion));
							} catch(java.lang.Exception e) {
								if(Window.dumpLumpIsSelected()) {
									FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
									initExceptionDebug.write(readLump(length, lumpVersion));
									initExceptionDebug.close();
								}
								throw e;
							}
						} else {
							try {
								BSPObject.setDispTris(readLump(offset, length));
							} catch(java.lang.Exception e) {
								if(Window.dumpLumpIsSelected()) {
									FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
									initExceptionDebug.write(readLump(offset, length));
									initExceptionDebug.close();
								}
								throw e;
							}
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
						try {
							BSPObject.setEntities(readLump(offset, length));
						} catch(java.lang.Exception e) {
							if(Window.dumpLumpIsSelected()) {
								FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
								initExceptionDebug.write(readLump(offset, length));
								initExceptionDebug.close();
							}
							throw e;
						}
						
						// Lump 01
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						try {
							BSPObject.setPlanes(readLump(offset, length));
						} catch(java.lang.Exception e) {
							if(Window.dumpLumpIsSelected()) {
								FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
								initExceptionDebug.write(readLump(offset, length));
								initExceptionDebug.close();
							}
							throw e;
						}
						
						// Lump 02
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						try {
							BSPObject.setVertices(readLump(offset, length));
						} catch(java.lang.Exception e) {
							if(Window.dumpLumpIsSelected()) {
								FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
								initExceptionDebug.write(readLump(offset, length));
								initExceptionDebug.close();
							}
							throw e;
						}
						
						offsetReader.skip(8); // Do not need offset/length for lump 3
						
						// Lump 04
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						try {
							BSPObject.setNodes(readLump(offset, length));
						} catch(java.lang.Exception e) {
							if(Window.dumpLumpIsSelected()) {
								FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
								initExceptionDebug.write(readLump(offset, length));
								initExceptionDebug.close();
							}
							throw e;
						}
						
						// Lump 05
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						try {
							BSPObject.setTextures(readLump(offset, length));
						} catch(java.lang.Exception e) {
							if(Window.dumpLumpIsSelected()) {
								FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
								initExceptionDebug.write(readLump(offset, length));
								initExceptionDebug.close();
							}
							throw e;
						}
						
						// Lump 06
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						try {
							BSPObject.setFaces(readLump(offset, length));
						} catch(java.lang.Exception e) {
							if(Window.dumpLumpIsSelected()) {
								FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
								initExceptionDebug.write(readLump(offset, length));
								initExceptionDebug.close();
							}
							throw e;
						}
						
						offsetReader.skip(8); // Do not need offset/length for lump 7
						
						// Lump 08
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						try {
							BSPObject.setLeaves(readLump(offset, length));
						} catch(java.lang.Exception e) {
							if(Window.dumpLumpIsSelected()) {
								FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
								initExceptionDebug.write(readLump(offset, length));
								initExceptionDebug.close();
							}
							throw e;
						}
						
						offsetReader.skip(8); // Do not need offset/length for lump 9
						
						// Lump 10
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						try {
							BSPObject.setMarkBrushes(readLump(offset, length));
						} catch(java.lang.Exception e) {
							if(Window.dumpLumpIsSelected()) {
								FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
								initExceptionDebug.write(readLump(offset, length));
								initExceptionDebug.close();
							}
							throw e;
						}
						
						// Lump 11
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						try {
							BSPObject.setEdges(readLump(offset, length), version);
						} catch(java.lang.Exception e) {
							if(Window.dumpLumpIsSelected()) {
								FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
								initExceptionDebug.write(readLump(offset, length));
								initExceptionDebug.close();
							}
							throw e;
						}
						
						// Lump 12
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						try {
							BSPObject.setSurfEdges(readLump(offset, length));
						} catch(java.lang.Exception e) {
							if(Window.dumpLumpIsSelected()) {
								FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
								initExceptionDebug.write(readLump(offset, length));
								initExceptionDebug.close();
							}
							throw e;
						}
						
						// Lump 13
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						try {
							BSPObject.setModels(readLump(offset, length));
						} catch(java.lang.Exception e) {
							if(Window.dumpLumpIsSelected()) {
								FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
								initExceptionDebug.write(readLump(offset, length));
								initExceptionDebug.close();
							}
							throw e;
						}
						
						// Lump 14
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						try {
							BSPObject.setBrushes(readLump(offset, length));
						} catch(java.lang.Exception e) {
							if(Window.dumpLumpIsSelected()) {
								FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
								initExceptionDebug.write(readLump(offset, length));
								initExceptionDebug.close();
							}
							throw e;
						}
						
						// Lump 15
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						try {
							BSPObject.setBrushSides(readLump(offset, length));
						} catch(java.lang.Exception e) {
							if(Window.dumpLumpIsSelected()) {
								FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
								initExceptionDebug.write(readLump(offset, length));
								initExceptionDebug.close();
							}
							throw e;
						}
						/*
						offsetReader.skip(16); // Do not need offset/length for lumps 16 or 17
						
						// Lump 18
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						try {
							BSPObject.setAreaPortals(readLump(offset, length));
						} catch(java.lang.Exception e) {
							if(Window.dumpLumpIsSelected()) {
								FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
								initExceptionDebug.write(readLump(offset, length));
								initExceptionDebug.close();
							}
							throw e;
						}*/
						break;
					case BSP.TYPE_SOF: // Uses the same header as Q3.
						Window.println("Soldier of Fortune BSP (modified id Tech 2)",Window.VERBOSITY_ALWAYS);
						offsetReader.skip(8); // Skip the file header, putting the reader into the offset/length pairs
										
						// Lump 00
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						try {
							BSPObject.setEntities(readLump(offset, length));
						} catch(java.lang.Exception e) {
							if(Window.dumpLumpIsSelected()) {
								FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
								initExceptionDebug.write(readLump(offset, length));
								initExceptionDebug.close();
							}
							throw e;
						}
						
						// Lump 01
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						try {
							BSPObject.setPlanes(readLump(offset, length));
						} catch(java.lang.Exception e) {
							if(Window.dumpLumpIsSelected()) {
								FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
								initExceptionDebug.write(readLump(offset, length));
								initExceptionDebug.close();
							}
							throw e;
						}
						
						// Lump 02
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						try {
							BSPObject.setVertices(readLump(offset, length));
						} catch(java.lang.Exception e) {
							if(Window.dumpLumpIsSelected()) {
								FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
								initExceptionDebug.write(readLump(offset, length));
								initExceptionDebug.close();
							}
							throw e;
						}
						
						offsetReader.skip(8); // Do not need offset/length for lump 3
						
						// Lump 04
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						try {
							BSPObject.setNodes(readLump(offset, length));
						} catch(java.lang.Exception e) {
							if(Window.dumpLumpIsSelected()) {
								FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
								initExceptionDebug.write(readLump(offset, length));
								initExceptionDebug.close();
							}
							throw e;
						}
						
						// Lump 05
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						try {
							BSPObject.setTextures(readLump(offset, length));
						} catch(java.lang.Exception e) {
							if(Window.dumpLumpIsSelected()) {
								FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
								initExceptionDebug.write(readLump(offset, length));
								initExceptionDebug.close();
							}
							throw e;
						}
						
						// Lump 06
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						try {
							BSPObject.setFaces(readLump(offset, length));
						} catch(java.lang.Exception e) {
							if(Window.dumpLumpIsSelected()) {
								FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
								initExceptionDebug.write(readLump(offset, length));
								initExceptionDebug.close();
							}
							throw e;
						}
						
						offsetReader.skip(8); // Do not need offset/length for lump 7
						
						// Lump 08
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						try {
							BSPObject.setLeaves(readLump(offset, length));
						} catch(java.lang.Exception e) {
							if(Window.dumpLumpIsSelected()) {
								FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
								initExceptionDebug.write(readLump(offset, length));
								initExceptionDebug.close();
							}
							throw e;
						}
						
						offsetReader.skip(8); // Do not need offset/length for lump 9
						
						// Lump 10
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						try {
							BSPObject.setMarkBrushes(readLump(offset, length));
						} catch(java.lang.Exception e) {
							if(Window.dumpLumpIsSelected()) {
								FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
								initExceptionDebug.write(readLump(offset, length));
								initExceptionDebug.close();
							}
							throw e;
						}
						
						// Lump 11
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						try {
							BSPObject.setEdges(readLump(offset, length), version);
						} catch(java.lang.Exception e) {
							if(Window.dumpLumpIsSelected()) {
								FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
								initExceptionDebug.write(readLump(offset, length));
								initExceptionDebug.close();
							}
							throw e;
						}
						
						// Lump 12
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						try {
							BSPObject.setSurfEdges(readLump(offset, length));
						} catch(java.lang.Exception e) {
							if(Window.dumpLumpIsSelected()) {
								FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
								initExceptionDebug.write(readLump(offset, length));
								initExceptionDebug.close();
							}
							throw e;
						}
						
						// Lump 13
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						try {
							BSPObject.setModels(readLump(offset, length));
						} catch(java.lang.Exception e) {
							if(Window.dumpLumpIsSelected()) {
								FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
								initExceptionDebug.write(readLump(offset, length));
								initExceptionDebug.close();
							}
							throw e;
						}
						
						// Lump 14
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						try {
							BSPObject.setBrushes(readLump(offset, length));
						} catch(java.lang.Exception e) {
							if(Window.dumpLumpIsSelected()) {
								FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
								initExceptionDebug.write(readLump(offset, length));
								initExceptionDebug.close();
							}
							throw e;
						}
						
						// Lump 15
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						try {
							BSPObject.setBrushSides(readLump(offset, length));
						} catch(java.lang.Exception e) {
							if(Window.dumpLumpIsSelected()) {
								FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
								initExceptionDebug.write(readLump(offset, length));
								initExceptionDebug.close();
							}
							throw e;
						}
						break;
					case BSP.TYPE_QUAKE3:
						Window.println("BSP v46 (id Tech 3)",Window.VERBOSITY_ALWAYS);
						offsetReader.skip(8);
						
						// Lump 00
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						try {
							BSPObject.setEntities(readLump(offset, length));
						} catch(java.lang.Exception e) {
							if(Window.dumpLumpIsSelected()) {
								FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
								initExceptionDebug.write(readLump(offset, length));
								initExceptionDebug.close();
							}
							throw e;
						}
						
						// Lump 01
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						try {
							BSPObject.setTextures(readLump(offset, length));
						} catch(java.lang.Exception e) {
							if(Window.dumpLumpIsSelected()) {
								FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
								initExceptionDebug.write(readLump(offset, length));
								initExceptionDebug.close();
							}
							throw e;
						}
						
						// Lump 02
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						try {
							BSPObject.setPlanes(readLump(offset, length));
						} catch(java.lang.Exception e) {
							if(Window.dumpLumpIsSelected()) {
								FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
								initExceptionDebug.write(readLump(offset, length));
								initExceptionDebug.close();
							}
							throw e;
						}
						
						offsetReader.skip(32); // Do not need offset/length for lumps 3-6
						
						// Lump 07
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						try {
							BSPObject.setModels(readLump(offset, length));
						} catch(java.lang.Exception e) {
							if(Window.dumpLumpIsSelected()) {
								FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
								initExceptionDebug.write(readLump(offset, length));
								initExceptionDebug.close();
							}
							throw e;
						}
						
						// Lump 08
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						try {
							BSPObject.setBrushes(readLump(offset, length));
						} catch(java.lang.Exception e) {
							if(Window.dumpLumpIsSelected()) {
								FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
								initExceptionDebug.write(readLump(offset, length));
								initExceptionDebug.close();
							}
							throw e;
						}
						
						// Lump 09
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						try {
							BSPObject.setBrushSides(readLump(offset, length));
						} catch(java.lang.Exception e) {
							if(Window.dumpLumpIsSelected()) {
								FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
								initExceptionDebug.write(readLump(offset, length));
								initExceptionDebug.close();
							}
							throw e;
						}
						
						// Lump 10
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						try {
							BSPObject.setVertices(readLump(offset, length));
						} catch(java.lang.Exception e) {
							if(Window.dumpLumpIsSelected()) {
								FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
								initExceptionDebug.write(readLump(offset, length));
								initExceptionDebug.close();
							}
							throw e;
						}
						
						offsetReader.skip(16); // Do not need offset/length for lumps 11 and 12
						
						// Lump 13
						offsetReader.read(read); // Read 4 bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						try {
							BSPObject.setFaces(readLump(offset, length));
						} catch(java.lang.Exception e) {
							if(Window.dumpLumpIsSelected()) {
								FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
								initExceptionDebug.write(readLump(offset, length));
								initExceptionDebug.close();
							}
							throw e;
						}
						break;
					case BSP.TYPE_COD:
						Window.println("BSP v59 (Call of Duty)",Window.VERBOSITY_ALWAYS);
						offsetReader.skip(8); // Skip the file header, putting the reader into the length/offset pairs
						
						// Lump 00
						offsetReader.read(read); // Read 4 bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						try {
							BSPObject.setTextures(readLump(offset, length));
						} catch(java.lang.Exception e) {
							if(Window.dumpLumpIsSelected()) {
								FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
								initExceptionDebug.write(readLump(offset, length));
								initExceptionDebug.close();
							}
							throw e;
						}
						
						offsetReader.skip(8);
						
						// Lump 02
						offsetReader.read(read); // Read 4 bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						try {
							BSPObject.setPlanes(readLump(offset, length));
						} catch(java.lang.Exception e) {
							if(Window.dumpLumpIsSelected()) {
								FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
								initExceptionDebug.write(readLump(offset, length));
								initExceptionDebug.close();
							}
							throw e;
						}
						
						// Lump 03
						offsetReader.read(read); // Read 4 bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						try {
							BSPObject.setBrushSides(readLump(offset, length));
						} catch(java.lang.Exception e) {
							if(Window.dumpLumpIsSelected()) {
								FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
								initExceptionDebug.write(readLump(offset, length));
								initExceptionDebug.close();
							}
							throw e;
						}
						
						// Lump 04
						offsetReader.read(read); // Read 4 bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						try {
							BSPObject.setBrushes(readLump(offset, length));
						} catch(java.lang.Exception e) {
							if(Window.dumpLumpIsSelected()) {
								FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
								initExceptionDebug.write(readLump(offset, length));
								initExceptionDebug.close();
							}
							throw e;
						}
						
						offsetReader.skip(176); // Skip lumps 5-26
						
						// Lump 27
						offsetReader.read(read); // Read 4 bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						try {
							BSPObject.setModels(readLump(offset, length));
						} catch(java.lang.Exception e) {
							if(Window.dumpLumpIsSelected()) {
								FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
								initExceptionDebug.write(readLump(offset, length));
								initExceptionDebug.close();
							}
							throw e;
						}
						
						offsetReader.skip(8); // Skip lumps 5-26
						
						// Lump 29
						offsetReader.read(read); // Read 4 bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						try {
							BSPObject.setEntities(readLump(offset, length));
						} catch(java.lang.Exception e) {
							if(Window.dumpLumpIsSelected()) {
								FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
								initExceptionDebug.write(readLump(offset, length));
								initExceptionDebug.close();
							}
							throw e;
						}
						break;
					case BSP.TYPE_COD2:
						Window.println("Call of Duty 2 BSP",Window.VERBOSITY_ALWAYS);
						offsetReader.skip(8); // Skip the file header, putting the reader into the length/offset pairs
						
						// Lump 00
						offsetReader.read(read); // Read 4 bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						try {
							BSPObject.setTextures(readLump(offset, length));
						} catch(java.lang.Exception e) {
							if(Window.dumpLumpIsSelected()) {
								FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
								initExceptionDebug.write(readLump(offset, length));
								initExceptionDebug.close();
							}
							throw e;
						}
						
						offsetReader.skip(24); // skip 1,2,3
						
						// Lump 04
						offsetReader.read(read); // Read 4 bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						try {
							BSPObject.setPlanes(readLump(offset, length));
						} catch(java.lang.Exception e) {
							if(Window.dumpLumpIsSelected()) {
								FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
								initExceptionDebug.write(readLump(offset, length));
								initExceptionDebug.close();
							}
							throw e;
						}
						
						// Lump 05
						offsetReader.read(read); // Read 4 bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						try {
							BSPObject.setBrushSides(readLump(offset, length));
						} catch(java.lang.Exception e) {
							if(Window.dumpLumpIsSelected()) {
								FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
								initExceptionDebug.write(readLump(offset, length));
								initExceptionDebug.close();
							}
							throw e;
						}
						
						// Lump 06
						offsetReader.read(read); // Read 4 bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						try {
							BSPObject.setBrushes(readLump(offset, length));
						} catch(java.lang.Exception e) {
							if(Window.dumpLumpIsSelected()) {
								FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
								initExceptionDebug.write(readLump(offset, length));
								initExceptionDebug.close();
							}
							throw e;
						}
						
						offsetReader.skip(224); // Skip lumps 7-34
						
						// Lump 35
						offsetReader.read(read); // Read 4 bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						try {
							BSPObject.setModels(readLump(offset, length));
						} catch(java.lang.Exception e) {
							if(Window.dumpLumpIsSelected()) {
								FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
								initExceptionDebug.write(readLump(offset, length));
								initExceptionDebug.close();
							}
							throw e;
						}
						
						offsetReader.skip(8); // Skip lump 36
						
						// Lump 37
						offsetReader.read(read); // Read 4 bytes
						length=DataReader.readInt(read[0], read[1], read[2], read[3]);
						offsetReader.read(read); // Read 4 more bytes
						offset=DataReader.readInt(read[0], read[1], read[2], read[3]);
						try {
							BSPObject.setEntities(readLump(offset, length));
						} catch(java.lang.Exception e) {
							if(Window.dumpLumpIsSelected()) {
								FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
								initExceptionDebug.write(readLump(offset, length));
								initExceptionDebug.close();
							}
							throw e;
						}
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
									try {
										BSPObject.setTextures(readLump(offset, length));
									} catch(java.lang.Exception e) {
										if(Window.dumpLumpIsSelected()) {
											FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
											initExceptionDebug.write(readLump(offset, length));
											initExceptionDebug.close();
										}
										throw e;
									}
									break;
								case 4:
									try {
										BSPObject.setPlanes(readLump(offset, length));
									} catch(java.lang.Exception e) {
										if(Window.dumpLumpIsSelected()) {
											FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
											initExceptionDebug.write(readLump(offset, length));
											initExceptionDebug.close();
										}
										throw e;
									}
									break;
								case 5:
									try {
										BSPObject.setBrushSides(readLump(offset, length));
									} catch(java.lang.Exception e) {
										if(Window.dumpLumpIsSelected()) {
											FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
											initExceptionDebug.write(readLump(offset, length));
											initExceptionDebug.close();
										}
										throw e;
									}
									break;
								case 8:
									try {
										BSPObject.setBrushes(readLump(offset, length));
									} catch(java.lang.Exception e) {
										if(Window.dumpLumpIsSelected()) {
											FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
											initExceptionDebug.write(readLump(offset, length));
											initExceptionDebug.close();
										}
										throw e;
									}
									break;
								case 37:
									try {
										BSPObject.setModels(readLump(offset, length));
									} catch(java.lang.Exception e) {
										if(Window.dumpLumpIsSelected()) {
											FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
											initExceptionDebug.write(readLump(offset, length));
											initExceptionDebug.close();
										}
										throw e;
									}
									break;
								case 39:
									try {
										BSPObject.setEntities(readLump(offset, length));
									} catch(java.lang.Exception e) {
										if(Window.dumpLumpIsSelected()) {
											FileOutputStream initExceptionDebug=new FileOutputStream("CrashLump.lmp");
											initExceptionDebug.write(readLump(offset, length));
											initExceptionDebug.close();
										}
										throw e;
									}
									break;
							}
							
							offset+=length;
							
							while((double)offset/(double)4 != (int)((double)offset/(double)4)) {
								offset++;
							}
						}
						break;
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
			if(version==BSP.TYPE_TACTICALINTERVENTION) {
				input=tidecryptor.decrypt(input, offset);
			}
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
		} // else
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
					case 41:
						version=BSP.TYPE_DAIKATANA;
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
						version=BSP.TYPE_MOHAA;
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
									version2=(int)DataReader.readShort(read[2], read[3]);
									if(version2==4) {
										version=BSP.TYPE_DMOMAM;
									} else {
										version=BSP.TYPE_SOURCE20;
									}
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
						} else {
							if(in == 325088789) {
								version=BSP.TYPE_TACTICALINTERVENTION;
								FileInputStream encryptionKeyReader = new FileInputStream(BSPFile);
								encryptionKeyReader.skip(384);
								byte[] key = new byte[32];
								encryptionKeyReader.read(key);
								tidecryptor = new TIDecryptor(key);
							} else {
								if(in==1347633746) { // Reads in ASCII as "RBSP". Raven software's modification of Q3BSP, or Ritual's modification of Q2.
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
											} else {
												version=1;
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
											version=1145132868;
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
		}
		return version;
	}
}
