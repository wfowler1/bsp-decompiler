// LS Class
//
// Tracks one file path and contains methods for separating/combining
// the lumps of the BSP specified by that path.
//
// This class mostly exists to keep the lump separator and combiner
// code clean and out of the Decompiler class. In all the other classes
// I've written I tried to release all file handles after I was
// done reading or writing from them, so this shouldn't have any
// problems reading or writing unless the files are open in an
// external program.
//
// This is not a lump separator with profile support (in other words,
// it only works for NightFire at the moment). For other games it's
// probably best to use FordGT90Concept's LS3.0 available at
// http://wiki.nfbsp.com/index.php/Lump_Separator and change the games.ini.
// However, both this lump separator and the Decompiler class both use
// the file naming conventions used by LS3.0 for NightFire lumps.

import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.File;

public class LS {

	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS

	private String BSP; // path to the actual BSP file
	private String folderPath; // path to the folder containing the lumps
	
	// CONSTRUCTORS
	
	// Takes a String in and assumes it is a path. If the String does not end
	// in .BSP it is assumed to be a folder full of all 17 lump files. If it
	// does end in .BSP then that file is the BSP and its name minus the .BSP
	// extension is assumed to be the folder. See comments below for clarification.
	// Case does not matter on the extension, so it could be .BSP, .bsp, etc.
	public LS(String in) {
		if (in.substring(in.length()-4).equalsIgnoreCase(".BSP")) { // If the input String ends in .BSP
			BSP=in; // The input String points directly to the BSP file.
			folderPath=in.substring(0,in.length()-4)+"\\"; // The input string minus the .BSP is the lumps folder
		} else {
			folderPath=in;
			if (folderPath.charAt(folderPath.length()-1) != '\\' && folderPath.charAt(folderPath.length()-1) != '/') {
				folderPath+="\\"; // Add a '\' character to the end of the path if it isn't already there
				BSP=in+".bsp";
			} else {
				BSP=in.substring(0,in.length()-1) + ".bsp";
			}
		}
	}
	
	// separateLumps()
	// Separates lumps into the folder specified. This stuff works
	// exceptionally fast, since hard drives like writing a few
	// large chunks of data rather than a million little nibbles.
	// TODO: I hate the code I wrote for this method. It needs a
	// complete rewrite to use arrays and loops instead of so much
	// almost identical code.
	public void separateLumps() {
		new File(folderPath).mkdir(); // Create the lumps folder if it does not exist. If it does this will do nothing.
		File inBSP=new File(BSP);
		// Do all file stuff first, make sure the BSP exists and the lump files can be written/rewritten
		if(!inBSP.exists()) {
			System.out.println("Unable to open source BSP file, please ensure the BSP exists.");
		} else {
			try {
				
				// TODO: Code cleanliness
				// ALL of this could be done with arrays of File and FileOutputStream. Rewrite to use loops and arrays.
				
				File Lump00 = new File(folderPath+"00 - "+Decompiler.LUMPNAMES[0]+".txt");
				if(!Lump00.exists()) {
					Lump00.createNewFile();
				} else {
					Lump00.delete();
					Lump00.createNewFile();
				}
				File Lump01 = new File(folderPath+"01 - "+Decompiler.LUMPNAMES[1]+".hex");
				if(!Lump01.exists()) {
					Lump01.createNewFile();
				} else {
					Lump01.delete();
					Lump01.createNewFile();
				}
				File Lump02 = new File(folderPath+"02 - "+Decompiler.LUMPNAMES[2]+".hex");
				if(!Lump02.exists()) {
					Lump02.createNewFile();
				} else {
					Lump02.delete();
					Lump02.createNewFile();
				}
				File Lump03 = new File(folderPath+"03 - "+Decompiler.LUMPNAMES[3]+".hex");
				if(!Lump03.exists()) {
					Lump03.createNewFile();
				} else {
					Lump03.delete();
					Lump03.createNewFile();
				}
				File Lump04 = new File(folderPath+"04 - "+Decompiler.LUMPNAMES[4]+".hex");
				if(!Lump04.exists()) {
					Lump04.createNewFile();
				} else {
					Lump04.delete();
					Lump04.createNewFile();
				}
				File Lump09 = new File(folderPath+"09 - "+Decompiler.LUMPNAMES[9]+".hex");
				if(!Lump09.exists()) {
					Lump09.createNewFile();
				} else {
					Lump09.delete();
					Lump09.createNewFile();
				}
				File Lump11 = new File(folderPath+"11 - "+Decompiler.LUMPNAMES[11]+".hex");
				if(!Lump11.exists()) {
					Lump11.createNewFile();
				} else {
					Lump11.delete();
					Lump11.createNewFile();
				}
				File Lump13 = new File(folderPath+"13 - "+Decompiler.LUMPNAMES[13]+".hex");
				if(!Lump13.exists()) {
					Lump13.createNewFile();
				} else {
					Lump13.delete();
					Lump13.createNewFile();
				}
				File Lump14 = new File(folderPath+"14 - "+Decompiler.LUMPNAMES[14]+".hex");
				if(!Lump14.exists()) {
					Lump14.createNewFile();
				} else {
					Lump14.delete();
					Lump14.createNewFile();
				}
				File Lump15 = new File(folderPath+"15 - "+Decompiler.LUMPNAMES[15]+".hex");
				if(!Lump15.exists()) {
					Lump15.createNewFile();
				} else {
					Lump15.delete();
					Lump15.createNewFile();
				}
				File Lump16 = new File(folderPath+"16 - "+Decompiler.LUMPNAMES[16]+".hex");
				if(!Lump16.exists()) {
					Lump16.createNewFile();
				} else {
					Lump16.delete();
					Lump16.createNewFile();
				}
				File Lump17 = new File(folderPath+"17 - "+Decompiler.LUMPNAMES[17]+".hex");
				if(!Lump17.exists()) {
					Lump17.createNewFile();
				} else {
					Lump17.delete();
					Lump17.createNewFile();
				}
				
				// Get the offsets/lengths of the lumps in the BSP
				// Don't forget, Java uses BIG ENDIAN BYTE ORDER, so all numbers have to be read and written backwards.
				byte[] input=new byte[4];
				int[][] offsetlengths=new int[2][18]; // First parameter is 0 for offset, 1 for length, second parameter is the lump
				final int OFFSET=0;
				final int LENGTH=1;
				FileInputStream BSP=new FileInputStream(inBSP);
				BSP.read(input);
				int version=(input[3] << 24) | ((input[2] & 0xff) << 16) | ((input[1] & 0xff) << 8) | (input[0] & 0xff);
				if(version!=42) {
					System.out.println("WARNING: BSP version is not 42.");
				}
				for(int i=0;i<18;i++) {
					BSP.read(input);
					offsetlengths[OFFSET][i]=(input[3] << 24) | ((input[2] & 0xff) << 16) | ((input[1] & 0xff) << 8) | (input[0] & 0xff);
					BSP.read(input);
					offsetlengths[LENGTH][i]=(input[3] << 24) | ((input[2] & 0xff) << 16) | ((input[1] & 0xff) << 8) | (input[0] & 0xff);
				}
				BSP.close();
				
				// Read lump00 data into 00 - Entities.txt
				// Handling this stuff as binary data (rather than Strings) speeds things up considerably
				input = new byte[offsetlengths[LENGTH][0]]; // Changes input to be the length of the lump as defined in the header
				FileInputStream Lump00Reader = new FileInputStream(inBSP);
				Lump00Reader.skip(offsetlengths[OFFSET][0]);
				Lump00Reader.read(input);
				FileOutputStream Lump00Writer = new FileOutputStream(Lump00);
				Lump00Writer.write(input);
				Lump00Reader.close();
				Lump00Writer.close();
				
				// Read lump01 data into 01 - Planes.hex
				input = new byte[offsetlengths[LENGTH][1]]; // Changes input to be the length of the lump as defined in the header
				FileInputStream Lump01Reader = new FileInputStream(inBSP);
				Lump01Reader.skip(offsetlengths[OFFSET][1]);
				Lump01Reader.read(input);
				FileOutputStream Lump01Writer = new FileOutputStream(Lump01);
				Lump01Writer.write(input);
				Lump01Reader.close();
				Lump01Writer.close();
				
				// Read lump02 data into 02 - Textures.hex
				input = new byte[offsetlengths[LENGTH][2]]; // Changes input to be the length of the lump as defined in the header
				FileInputStream Lump02Reader = new FileInputStream(inBSP);
				Lump02Reader.skip(offsetlengths[OFFSET][2]);
				Lump02Reader.read(input);
				FileOutputStream Lump02Writer = new FileOutputStream(Lump02);
				Lump02Writer.write(input);
				Lump02Reader.close();
				Lump02Writer.close();
				
				// Read lump data into file
				input = new byte[offsetlengths[LENGTH][3]]; // Changes input to be the length of the lump as defined in the header
				FileInputStream Lump03Reader = new FileInputStream(inBSP);
				Lump03Reader.skip(offsetlengths[OFFSET][3]);
				Lump03Reader.read(input);
				FileOutputStream Lump03Writer = new FileOutputStream(Lump03);
				Lump03Writer.write(input);
				Lump03Reader.close();
				Lump03Writer.close();
				
				// Read lump data into file
				input = new byte[offsetlengths[LENGTH][4]]; // Changes input to be the length of the lump as defined in the header
				FileInputStream Lump04Reader = new FileInputStream(inBSP);
				Lump04Reader.skip(offsetlengths[OFFSET][4]);
				Lump04Reader.read(input);
				FileOutputStream Lump04Writer = new FileOutputStream(Lump04);
				Lump04Writer.write(input);
				Lump04Reader.close();
				Lump04Writer.close();
				
				// Read lump data into file
				input = new byte[offsetlengths[LENGTH][9]]; // Changes input to be the length of the lump as defined in the header
				FileInputStream Lump09Reader = new FileInputStream(inBSP);
				Lump09Reader.skip(offsetlengths[OFFSET][9]);
				Lump09Reader.read(input);
				FileOutputStream Lump09Writer = new FileOutputStream(Lump09);
				Lump09Writer.write(input);
				Lump09Reader.close();
				Lump09Writer.close();
				
				// Read lump data into file
				input = new byte[offsetlengths[LENGTH][11]]; // Changes input to be the length of the lump as defined in the header
				FileInputStream Lump11Reader = new FileInputStream(inBSP);
				Lump11Reader.skip(offsetlengths[OFFSET][11]);
				Lump11Reader.read(input);
				FileOutputStream Lump11Writer = new FileOutputStream(Lump11);
				Lump11Writer.write(input);
				Lump11Reader.close();
				Lump11Writer.close();
				
				// Read lump data into file
				input = new byte[offsetlengths[LENGTH][13]]; // Changes input to be the length of the lump as defined in the header
				FileInputStream Lump13Reader = new FileInputStream(inBSP);
				Lump13Reader.skip(offsetlengths[OFFSET][13]);
				Lump13Reader.read(input);
				FileOutputStream Lump13Writer = new FileOutputStream(Lump13);
				Lump13Writer.write(input);
				Lump13Reader.close();
				Lump13Writer.close();
				
				// Read lump data into file
				input = new byte[offsetlengths[LENGTH][14]]; // Changes input to be the length of the lump as defined in the header
				FileInputStream Lump14Reader = new FileInputStream(inBSP);
				Lump14Reader.skip(offsetlengths[OFFSET][14]);
				Lump14Reader.read(input);
				FileOutputStream Lump14Writer = new FileOutputStream(Lump14);
				Lump14Writer.write(input);
				Lump14Reader.close();
				Lump14Writer.close();
				
				// Read lump data into file
				input = new byte[offsetlengths[LENGTH][15]]; // Changes input to be the length of the lump as defined in the header
				FileInputStream Lump15Reader = new FileInputStream(inBSP);
				Lump15Reader.skip(offsetlengths[OFFSET][15]);
				Lump15Reader.read(input);
				FileOutputStream Lump15Writer = new FileOutputStream(Lump15);
				Lump15Writer.write(input);
				Lump15Reader.close();
				Lump15Writer.close();
				
				// Read lump data into file
				input = new byte[offsetlengths[LENGTH][16]]; // Changes input to be the length of the lump as defined in the header
				FileInputStream Lump16Reader = new FileInputStream(inBSP);
				Lump16Reader.skip(offsetlengths[OFFSET][16]);
				Lump16Reader.read(input);
				FileOutputStream Lump16Writer = new FileOutputStream(Lump16);
				Lump16Writer.write(input);
				Lump16Reader.close();
				Lump16Writer.close();
				
				// Read lump data into file
				input = new byte[offsetlengths[LENGTH][17]]; // Changes input to be the length of the lump as defined in the header
				FileInputStream Lump17Reader = new FileInputStream(inBSP);
				Lump17Reader.skip(offsetlengths[OFFSET][17]);
				Lump17Reader.read(input);
				FileOutputStream Lump17Writer = new FileOutputStream(Lump17);
				Lump17Writer.write(input);
				Lump17Reader.close();
				Lump17Writer.close();
			} catch(java.io.IOException e) {
				System.out.println("ERROR: Unable to open or rewrite at least one lump file, please ensure none of the lumps are open in another program.");
			}
		}
	}
}
