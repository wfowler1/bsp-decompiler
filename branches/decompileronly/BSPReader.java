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
	// Both MoHAA and Source engines use different version numbering systems than the standard.
	private boolean source=false;
	private boolean mohaa=false;
	private boolean raven=false;
	private boolean sin=false;
	private boolean sof=false;
	private boolean wad=false;
	private boolean ef2=false;
	private boolean fakk=false;
	
	public final int OFFSET=0;
	public final int LENGTH=1;
	// These are only used in Source BSPs, which have a lot of different structures
	public final int LUMPVERSION=2;
	public final int FOURCC=3;
	
	private int version=0;
	private int version2=0;
	
	// Declare all kinds of BSPs here, the one actually used will be determined by constructor
	protected DoomMap[] doomMaps;
	protected BSP BSPObject;
	// protected SourceBSP SourceBSPObject;
	// SUPERCEDED BY BSP CLASS
	// protected v38BSP BSP38;
	// protected v42BSP BSP42;
	// protected v46BSP BSP46;
	// protected EF2BSP STEF2BSP;
	// protected MoHAABSP MOHAABSP;
	// protected RavenBSP ravenBSP;
	// protected CoDBSP CODBSP;
	// protected SiNBSP SINBSP;
	
	// CONSTRUCTORS
	
	// Takes a String in and assumes it is a path. That path is the path to the file
	// that is the BSP and its name minus the .BSP extension is assumed to be the folder.
	// See comments below for clarification. Case does not matter on the extension, so it
	// could be .BSP, .bsp, etc.
	public BSPReader(String in) {
		new BSPReader(new File(in));
	}
	
	public BSPReader(File in) {
		BSPFile=in;
		if(!BSPFile.exists()) {
			Window.println("Unable to open source BSP file, please ensure the BSP exists.",Window.VERBOSITY_ALWAYS);
		} else {
			folder=BSPFile.getParent(); // The read string minus the .BSP is the lumps folder
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
			FileInputStream offsetReader;
			byte[] read=new byte[4];
			int offset;
			int length;
			if(mohaa) {
				Window.println("MOHAA BSP found (modified id Tech 3)",Window.VERBOSITY_ALWAYS);
				offsetReader = new FileInputStream(BSPFile);
				BSPObject = new BSP(BSPFile.getPath(), BSP.TYPE_MOHAA);
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
				
				offsetReader.close();
				
				BSPObject.printBSPReport();
			} else {
				if(source) {
					int lumpVersion=0;
					switch(version) {
						// Gonna handle all Source BSP formats here.
						// Might have to deal with format differences later.
						// For now, focus on HL2, or v19.
						case 17: // Vampire: The Masquerades Bloodlines
						case 18: // HL2 Beta
						case 19: // HL2, CSS, DoDS
						case 20: // HL2E1, HL2E2, Portal, L4D, TF2
						case 21: // L4D2, Portal 2, CSGO
						case 22: // Dota 2
						case 23: // Also Dota 2? OMG HAX
							Window.println("Source BSP v"+version+" found",Window.VERBOSITY_ALWAYS);
							offsetReader = new FileInputStream(BSPFile);
							// Left 4 Dead 2, for some reason, made the order "version, offset, length" for lump header structure,
							// rather than the usual "offset, length, version". I guess someone at Valve got bored.
							boolean isL4D2=false;
							BSPObject = new BSP(BSPFile.getPath(),1347633750+version);
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
							//	BSPObject.setFaces(readLump(length, version));
							} else {
							//	BSPObject.setFaces(readLump(offset, length));
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
							//	BSPObject.setOriginalFaces(readLump(length, version));
							} else {
							//	BSPObject.setOriginalFaces(readLump(offset, length));
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
							
							offsetReader.skip(96);
							
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
										PAKWriter=new FileOutputStream(new File(BSPObject.getPath().substring(0, BSPObject.getPath().length()-4)+".pak"));
									} else {
										PAKWriter=new FileOutputStream(new File(Window.getOutputFolder()+"\\"+BSPObject.getMapName().substring(0, BSPObject.getMapName().length()-4)+".pak"));
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
									Window.println("WARNING: Unable to write PAKFile! Path: "+BSPFile.getAbsolutePath().substring(0,BSPFile.getAbsolutePath().length()-4)+".pak",Window.VERBOSITY_WARNINGS);
								}
							} else {
								offsetReader.skip(16);
							}
							
							offsetReader.skip(32);
							
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
							FileOutputStream fuck=new FileOutputStream(new File(BSPObject.getPath().substring(0, BSPObject.getPath().length()-4)+".pak"));
							if(isL4D2) {
								fuck.write(readLump(length, version));
							} else {
								fuck.write(readLump(offset, length));
							}
							fuck.close();
							
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
							
							offsetReader.close();
							
							BSPObject.printBSPReport();
							
						break;
					}
				} else {
					if(raven) { // This header format is used by two different games.
						offsetReader = new FileInputStream(BSPFile);
						FileInputStream secondOffsetReader = new FileInputStream(BSPFile);
						offsetReader.skip(8); // Skip the file header, putting the reader into the offset/length pairs
						secondOffsetReader.skip(8);
						sin=false;
						for(int i=0;i<17;i++) {
							// Find out where the first lump starts, based on offsets.
							// This process assumes the file header has not been tampered with in any way.
							// Unfortunately it could inadvertantly lead to a method of decompile protection.
							secondOffsetReader.read(read);
							int temp=DataReader.readInt(read[0], read[1], read[2], read[3]);
							if(temp==168) {
								sin=true;
								break;
							} else {
								if(temp==152) {
									sin=false;
									break;
								}
							}
							secondOffsetReader.skip(4);
						}
						secondOffsetReader.close();
						
						if(!sin) {
							Window.println("Raven Software BSP found (Modified id Tech 3)",Window.VERBOSITY_ALWAYS);
							BSPObject = new BSP(BSPFile.getPath(), BSP.TYPE_RAVEN);
							
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
							
							offsetReader.close();
							
							BSPObject.printBSPReport();
						} else {
							Window.println("SiN BSP found (Modified Quake 2)",Window.VERBOSITY_ALWAYS);
							BSPObject = new BSP(BSPFile.getPath(), BSP.TYPE_SIN);
							
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
							
							offsetReader.close();
							
							BSPObject.printBSPReport();
						}
					} else {
						if(ef2) {
							switch(version) {
								case 19:
									Window.println("Star Trek Elite Force 2 Demo BSP Found",Window.VERBOSITY_ALWAYS);
									BSPObject = new BSP(BSPFile.getPath(), BSP.TYPE_STEF2DEMO);
									break;
								case 20:
									Window.println("Star Trek Elite Force 2 BSP Found",Window.VERBOSITY_ALWAYS);
									BSPObject = new BSP(BSPFile.getPath(), BSP.TYPE_STEF2);
									break;
							}
							offsetReader = new FileInputStream(BSPFile);
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
							
							offsetReader.close();
							
							BSPObject.printBSPReport();
						} else {
							if(fakk) {
								switch(version) {
									case 12:
									case 42:
										Window.println("American McGee's Alice BSP Found",Window.VERBOSITY_ALWAYS);
										BSPObject = new BSP(BSPFile.getPath(), BSP.TYPE_FAKK);
										
										offsetReader = new FileInputStream(BSPFile);
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
										
										BSPObject.printBSPReport();
										offsetReader.close();
										
										break;
								}
							} else {
								switch(version) {
									case 1: // WAD file
										Window.println("WAD file found",Window.VERBOSITY_ALWAYS);
										offsetReader = new FileInputStream(BSPFile);
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
												
												Window.window.addJob(null, doomMaps[doomMaps.length-1]);
											}
										}
										
										directoryReader.close();
										offsetReader.close();
									break;
									case 4: // CoD2. They changed their versioning scheme but so far there's no conflicts
										Window.println("Call of Duty 2 BSP found",Window.VERBOSITY_ALWAYS);
										offsetReader = new FileInputStream(BSPFile);
										BSPObject = new BSP(BSPFile.getPath(), BSP.TYPE_COD);
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
										
										offsetReader.close();
										
										BSPObject.printBSPReport();
	
										break;
									case 22: // Call of Duty 4. Once again, different versioning scheme but no conflicts.
										Window.println("Call of Duty 4 BSP found",Window.VERBOSITY_ALWAYS);
										// CoD4 is somewhat unique, it calls for a different reader. However it's still doable.
										offsetReader = new FileInputStream(BSPFile);
										BSPObject=new BSP(BSPFile.getPath(), BSP.TYPE_COD);
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
										offsetReader.close();
										
										BSPObject.printBSPReport();
	
										break;
									case 29: // Quake
									case 30: // Half-life
										Window.println("Quake 1/Half-life BSP found",Window.VERBOSITY_ALWAYS);
										offsetReader = new FileInputStream(BSPFile);
										BSPObject = new BSP(BSPFile.getPath(), BSP.TYPE_QUAKE);
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
										
										// TODO
										
										break;
									case 38: // Quake 2
										Window.println("BSP v38 found (Quake 2)",Window.VERBOSITY_ALWAYS);
										offsetReader = new FileInputStream(BSPFile);
										BSPObject = new BSP(BSPFile.getPath(), BSP.TYPE_QUAKE2);
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
										
										offsetReader.close();
										
										BSPObject.printBSPReport();
										break;
									case 42: // JBN
										Window.println("BSP v42 found (Nightfire)",Window.VERBOSITY_ALWAYS);
										offsetReader = new FileInputStream(BSPFile);
										BSPObject = new BSP(BSPFile.getPath(), BSP.TYPE_NIGHTFIRE);
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
										
										offsetReader.close();
										
										BSPObject.printBSPReport();
										break;
									case 46: // Quake 3/close derivative
									case 47: // RTCW
										offsetReader = new FileInputStream(BSPFile);
										FileInputStream secondOffsetReader = new FileInputStream(BSPFile);
										offsetReader.skip(8); // Skip the file header, putting the reader into the offset/length pairs
										secondOffsetReader.skip(8);
										sof=false;
										for(int i=0;i<17;i++) {
											// Find out where the first lump starts, based on offsets.
											// This process assumes the file header has not been tampered with in any way.
											// Unfortunately it could inadvertantly lead to a method of decompile protection.
											secondOffsetReader.read(read);
											int temp=DataReader.readInt(read[0], read[1], read[2], read[3]);
											if(temp==184) {
												sof=true;
												break;
											} else {
												if(temp==144) {
													sof=false;
													break;
												}
											}
											secondOffsetReader.skip(4);
										}
										secondOffsetReader.close();
										
										if(sof) {
											Window.println("Soldier of Fortune BSP found (modified id Tech 2)",Window.VERBOSITY_ALWAYS);
											BSPObject = new BSP(BSPFile.getPath(), BSP.TYPE_SOF);
										
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
										} else {
											Window.println("BSP v46 found (id Tech 3)",Window.VERBOSITY_ALWAYS);
											BSPObject = new BSP(BSPFile.getPath(), BSP.TYPE_QUAKE3);
											
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
										}
											
										offsetReader.close();
										
										BSPObject.printBSPReport();
										
										break;
									case 59: // Call of Duty (1)
										Window.println("BSP v59 found (Call of Duty)",Window.VERBOSITY_ALWAYS);
										offsetReader = new FileInputStream(BSPFile);
										BSPObject = new BSP(BSPFile.getPath(), BSP.TYPE_COD);
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
										
										offsetReader.close();
										
										BSPObject.printBSPReport();
										
										break;
									default:
										Window.println("I don't know what kind of BSP this is! Please post an issue on the bug tracker!",Window.VERBOSITY_ALWAYS);
								}
							}
						}
					}
				}
			}
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
	
	public boolean isSource() {
		return source;
	}
	
	public boolean isSin() {
		return sin;
	}
	
	public boolean isSoF() {
		return sof;
	}
	
	public boolean isRaven() {
		return raven;
	}
	
	public boolean isMOHAA() {
		return mohaa;
	}
	
	public boolean isEF2() {
		return ef2;
	}
	
	public boolean isFAKK() {
		return fakk;
	}
	
	public int getVersion() throws java.io.IOException {
		if(version==0) {
			byte[] read=new byte[4];
			FileInputStream versionNumberReader=new FileInputStream(BSPFile); // This filestream will be used to read version number only
			versionNumberReader.read(read);
			int in=DataReader.readInt(read[0], read[1], read[2], read[3]);
			if(in == 1347633737) { // 1347633737 reads in ASCII as "IBSP"
				versionNumberReader.read(read);
				version=DataReader.readInt(read[0], read[1], read[2], read[3]);
			} else {
				if(in == 892416050) { // 892416050 reads in ASCII as "2015," the game studio which developed MoHAA
					mohaa=true;
					versionNumberReader.read(read);
					version=DataReader.readInt(read[0], read[1], read[2], read[3]); // Should be 19
				} else {
				if(in == 1095516485) { // 1095516485 reads in ASCII as "EALA," the ones who developed MoHAA Spearhead and Breakthrough
						mohaa=true;
						versionNumberReader.read(read);
						version=DataReader.readInt(read[0], read[1], read[2], read[3]); // Should be 21
					} else {
						if(in == 1347633750) { // 1347633750 reads in ASCII as "VBSP." Indicates Source engine.
							source=true;
							versionNumberReader.read(read);
							// Some source games handle this as 2 shorts. Since most version numbers
							// are below 65535, I can always read the first number as a short, because
							// the least significant bits come first in little endian.
							version=(int)DataReader.readShort(read[0], read[1]);
							version2=(int)DataReader.readShort(read[2], read[3]);
						} else {
							if(in==1347633746) { // Reads in ASCII as "RBSP". Raven software's modification of Q3BSP
								raven=true;
								versionNumberReader.read(read);
								version=DataReader.readInt(read[0], read[1], read[2], read[3]); // Probably 1
							} else {
								if(in == 556942917) { // "EF2!"
									versionNumberReader.read(read);
									version=DataReader.readInt(read[0], read[1], read[2], read[3]);
									if(version==20) {
										ef2=true;
									}
								} else {
									if(in == 1145132873 || in == 1145132880) { // "IWAD" or "PWAD"
										wad=true;
										version=1;
									} else {
										if(in == 1263223110) { // "FAKK"
											versionNumberReader.read(read);
											version=DataReader.readInt(read[0], read[1], read[2], read[3]);
											if(version == 19) {
												ef2=true;
											}
											if(version == 42 || version == 12) {
												fakk=true;
											}
										} else {
											version=in;
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
