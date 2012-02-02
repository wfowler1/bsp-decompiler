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

	private BSPData myBSP;
	
	public final int OFFSET=0;
	public final int LENGTH=1;
	
	private boolean vertexDecomp;
	private boolean checkVerts;
	private boolean correctPlaneFlip;
	private double planePointCoef;
	
	// This constructor sets everything according to specified settings.
	// DELETE THIS CONSTRUCTOR once the decompiler classes are ready, this is
	// hacky and prohibits use of multithreading!!!!!!
	public BSPReader(String in, boolean vertexDecomp, boolean checkVerts, boolean correctPlaneFlip, double planePointCoef) {
		BSP=new File(in); // The read String points directly to the BSP file.
		if(!BSP.exists()) {
			Window.window.println("Unable to open source BSP file, please ensure the BSP exists.");
		} else {
			folder=BSP.getParent(); // The read string minus the .BSP is the lumps folder
			if(folder==null) {
				folder="";
			}
			// Set up global variables
			this.vertexDecomp=vertexDecomp;
			this.checkVerts=checkVerts;
			this.correctPlaneFlip=correctPlaneFlip;
			this.planePointCoef=planePointCoef;
		}
	}
	
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
		BSP=in; // The read String points directly to the BSP file.
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
			// Don't forget, Java uses BIG ENDIAN BYTE ORDER, so all numbers have to be read and written backwards. // Quake
			int version=getVersion();
			if(mohaa) {
				Window.window.println("Sorry, no MOHAA support (yet)!");
			} else {
				if(source) {
					Window.window.println("Sorry, no source map support (yet)!");
				} else {
					switch(version) {
						case 29: // Quake
						case 30: // Half-life
							Window.window.println("Sorry, no Quake/Half-life support (yet)!");
							break;
						case 38: // Quake 2
							Window.window.println("Sorry, no Quake 2 support (yet)!");
							break;
						/*case 41: // JBN Beta
							numLumps=16; //??????
							break;*/
						case 42: // JBN
							Window.window.println("BSP v42 found (Nightfire)");
							FileInputStream offsetReader = new FileInputStream(BSP);
							myBSP = new BSPData(BSP.getPath());
							byte[] read=new byte[4];
							offsetReader.skip(4); // Skip the file header, putting the reader into the offset/length pairs
							
							// Lump 00
							offsetReader.read(read); // Read 4 bytes
							int offset=(read[3] << 24) | ((read[2] & 0xff) << 16) | ((read[1] & 0xff) << 8) | (read[0] & 0xff);
							offsetReader.read(read); // Read 4 more bytes
							int length=(read[3] << 24) | ((read[2] & 0xff) << 16) | ((read[1] & 0xff) << 8) | (read[0] & 0xff);
							myBSP.setEntities(readLump(offset, length));
							
							// Lump 01
							offsetReader.read(read); // Read 4 bytes
							offset=(read[3] << 24) | ((read[2] & 0xff) << 16) | ((read[1] & 0xff) << 8) | (read[0] & 0xff);
							offsetReader.read(read); // Read 4 more bytes
							length=(read[3] << 24) | ((read[2] & 0xff) << 16) | ((read[1] & 0xff) << 8) | (read[0] & 0xff);
							myBSP.setPlanes(readLump(offset, length));
							
							// Lump 02
							offsetReader.read(read); // Read 4 bytes
							offset=(read[3] << 24) | ((read[2] & 0xff) << 16) | ((read[1] & 0xff) << 8) | (read[0] & 0xff);
							offsetReader.read(read); // Read 4 more bytes
							length=(read[3] << 24) | ((read[2] & 0xff) << 16) | ((read[1] & 0xff) << 8) | (read[0] & 0xff);
							myBSP.setTextures(readLump(offset, length));
							
							// Lump 03
							offsetReader.read(read); // Read 4 bytes
							offset=(read[3] << 24) | ((read[2] & 0xff) << 16) | ((read[1] & 0xff) << 8) | (read[0] & 0xff);
							offsetReader.read(read); // Read 4 more bytes
							length=(read[3] << 24) | ((read[2] & 0xff) << 16) | ((read[1] & 0xff) << 8) | (read[0] & 0xff);
							myBSP.setMaterials(readLump(offset, length));
							
							// Lump 04
							offsetReader.read(read); // Read 4 bytes
							offset=(read[3] << 24) | ((read[2] & 0xff) << 16) | ((read[1] & 0xff) << 8) | (read[0] & 0xff);
							offsetReader.read(read); // Read 4 more bytes
							length=(read[3] << 24) | ((read[2] & 0xff) << 16) | ((read[1] & 0xff) << 8) | (read[0] & 0xff);
							myBSP.setVertices(readLump(offset, length));
							
							offsetReader.skip(32); // Do not need offset/length for lumps 5-8
							
							// Lump 09
							offsetReader.read(read); // Read 4 bytes
							offset=(read[3] << 24) | ((read[2] & 0xff) << 16) | ((read[1] & 0xff) << 8) | (read[0] & 0xff);
							offsetReader.read(read); // Read 4 more bytes
							length=(read[3] << 24) | ((read[2] & 0xff) << 16) | ((read[1] & 0xff) << 8) | (read[0] & 0xff);
							myBSP.setFaces(readLump(offset, length));
							
							offsetReader.skip(8); // Do not need offset/length for lump 10
							
							// Lump 11
							offsetReader.read(read); // Read 4 bytes
							offset=(read[3] << 24) | ((read[2] & 0xff) << 16) | ((read[1] & 0xff) << 8) | (read[0] & 0xff);
							offsetReader.read(read); // Read 4 more bytes
							length=(read[3] << 24) | ((read[2] & 0xff) << 16) | ((read[1] & 0xff) << 8) | (read[0] & 0xff);
							myBSP.setLeaves(readLump(offset, length));
							
							offsetReader.skip(8); // Do not need offset/length for lump 12
							
							// Lump 13
							offsetReader.read(read); // Read 4 bytes
							offset=(read[3] << 24) | ((read[2] & 0xff) << 16) | ((read[1] & 0xff) << 8) | (read[0] & 0xff);
							offsetReader.read(read); // Read 4 more bytes
							length=(read[3] << 24) | ((read[2] & 0xff) << 16) | ((read[1] & 0xff) << 8) | (read[0] & 0xff);
							myBSP.setMarkBrushes(readLump(offset, length));
							
							// Lump 14
							offsetReader.read(read); // Read 4 bytes
							offset=(read[3] << 24) | ((read[2] & 0xff) << 16) | ((read[1] & 0xff) << 8) | (read[0] & 0xff);
							offsetReader.read(read); // Read 4 more bytes
							length=(read[3] << 24) | ((read[2] & 0xff) << 16) | ((read[1] & 0xff) << 8) | (read[0] & 0xff);
							myBSP.setModels(readLump(offset, length));
							
							// Lump 15
							offsetReader.read(read); // Read 4 bytes
							offset=(read[3] << 24) | ((read[2] & 0xff) << 16) | ((read[1] & 0xff) << 8) | (read[0] & 0xff);
							offsetReader.read(read); // Read 4 more bytes
							length=(read[3] << 24) | ((read[2] & 0xff) << 16) | ((read[1] & 0xff) << 8) | (read[0] & 0xff);
							myBSP.setBrushes(readLump(offset, length));
							
							// Lump 16
							offsetReader.read(read); // Read 4 bytes
							offset=(read[3] << 24) | ((read[2] & 0xff) << 16) | ((read[1] & 0xff) << 8) | (read[0] & 0xff);
							offsetReader.read(read); // Read 4 more bytes
							length=(read[3] << 24) | ((read[2] & 0xff) << 16) | ((read[1] & 0xff) << 8) | (read[0] & 0xff);
							myBSP.setBrushSides(readLump(offset, length));
							
							// Lump 17
							offsetReader.read(read); // Read 4 bytes
							offset=(read[3] << 24) | ((read[2] & 0xff) << 16) | ((read[1] & 0xff) << 8) | (read[0] & 0xff);
							offsetReader.read(read); // Read 4 more bytes
							length=(read[3] << 24) | ((read[2] & 0xff) << 16) | ((read[1] & 0xff) << 8) | (read[0] & 0xff);
							myBSP.setTextureMatrices(readLump(offset, length));
							
							offsetReader.close();
							
							myBSP.printBSPReport();
							
							// In the near future, decompiler won't be called from here.
							Decompiler decompiler=new Decompiler(vertexDecomp, checkVerts, correctPlaneFlip, planePointCoef);
							decompiler.decompileBSP42(myBSP);
							
							break;
						case 46: // Quake 3/close derivative
							Window.window.println("Sorry, no Quake 3 support (yet)!");
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
		byte[] read=new byte[4];
		FileInputStream versionNumberReader=new FileInputStream(BSP); // This filestream will be used to read version number only
		versionNumberReader.read(read);
		int version=0;
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
						version=in;
					}
				}
			}
		}
		return version;
	}
}
