// BrushSides class

// This class holds references to all the brush sides defined
// by the map. These are referenced directly by the previous
// lump (Lump15).

import java.io.FileInputStream;
import java.io.File;

public class BrushSides {

	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	private File data;
	private int length;
	private int numBrshsds=0;
	private BrushSide[] brushsides;
	
	// CONSTRUCTORS
	
	// This one accepts the lump path as a String
	public BrushSides(String in) {
		data=new File(in);
		length=(int)data.length();
		try {
			numBrshsds=getNumElements();
			brushsides=new BrushSide[numBrshsds];
			populateBrushSideList();
		} catch(java.io.FileNotFoundException e) {
			Window.window.println("ERROR: File "+data+" not found!");
		} catch(java.io.IOException e) {
			Window.window.println("ERROR: File "+data+" could not be read, ensure the file is not open in another program");
		}
	}
	
	// This one accepts the input file path as a File
	public BrushSides(File in) {
		data=in;
		length=(int)data.length();
		try {
			numBrshsds=getNumElements();
			brushsides=new BrushSide[numBrshsds];
			populateBrushSideList();
		} catch(java.io.FileNotFoundException e) {
			Window.window.println("ERROR: File "+data+" not found!");
		} catch(java.io.IOException e) {
			Window.window.println("ERROR: File "+data+" could not be read, ensure the file is not open in another program");
		}
	}
	
	public BrushSides(byte[] in) {
		int offset=0;
		numBrshsds=in.length/8;
		length=in.length;
		brushsides=new BrushSide[numBrshsds];
		try {
			for(int i=0;i<numBrshsds;i++) {
				byte[] brushSideBytes=new byte[8];
				for(int j=0;j<8;j++) {
					brushSideBytes[j]=in[offset+j];
				}
				brushsides[i]=new BrushSide(brushSideBytes);
				offset+=8;
			}
		} catch(InvalidBrushSideException e) {
			Window.window.println("WARNING: Funny lump size in "+data+", ignoring last brush side.");
		}
	}
	
	// METHODS
	
	// -populateBrushSideList()
	// Uses the data file in the instance data to populate the
	// array of BrushSide objects with the data from the file
	private void populateBrushSideList() throws java.io.FileNotFoundException, java.io.IOException {
		FileInputStream reader=new FileInputStream(data);
		try {
			for(int i=0;i<numBrshsds;i++) {
				byte[] datain=new byte[8];
				reader.read(datain);
				brushsides[i]=new BrushSide(datain);
			}
			reader.close();
		} catch(InvalidBrushSideException e) {
			Window.window.println("WARNING: Funny lump size in "+data+", ignoring last brush side.");
		}
	}
		
	// ACCESSORS/MUTATORS
	
	// Returns the length (in bytes) of the lump
	public int getLength() {
		return length;
	}
	
	// Returns the number of brush sides.
	public int getNumElements() {
		if(numBrshsds==0) {
			return length/8;
		} else {
			return numBrshsds;
		}
	}
	
	public BrushSide getBrushSide(int i) {
		return brushsides[i];
	}
	
	public BrushSide[] getBrushSides() {
		return brushsides;
	}
	
	public void setBrushSide(int i, BrushSide in) {
		brushsides[i]=in;
	}
}