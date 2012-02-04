// v42BrushSides class

// This class holds references to all the brush sides defined
// by the map. These are referenced directly by the previous
// lump (Lump15).

import java.io.FileInputStream;
import java.io.File;

public class v42BrushSides {

	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	private File data;
	private int length;
	private int numBrshsds=0;
	private v42BrushSide[] brushsides;
	
	// CONSTRUCTORS
	
	// This one accepts the lump path as a String
	public v42BrushSides(String in) {
		data=new File(in);
		length=(int)data.length();
		try {
			numBrshsds=getNumElements();
			brushsides=new v42BrushSide[numBrshsds];
			populateBrushSideList();
		} catch(java.io.FileNotFoundException e) {
			Window.window.println("ERROR: File "+data+" not found!");
		} catch(java.io.IOException e) {
			Window.window.println("ERROR: File "+data+" could not be read, ensure the file is not open in another program");
		}
	}
	
	// This one accepts the input file path as a File
	public v42BrushSides(File in) {
		data=in;
		length=(int)data.length();
		try {
			numBrshsds=getNumElements();
			brushsides=new v42BrushSide[numBrshsds];
			populateBrushSideList();
		} catch(java.io.FileNotFoundException e) {
			Window.window.println("ERROR: File "+data+" not found!");
		} catch(java.io.IOException e) {
			Window.window.println("ERROR: File "+data+" could not be read, ensure the file is not open in another program");
		}
	}
	
	public v42BrushSides(byte[] in) {
		int offset=0;
		numBrshsds=in.length/8;
		length=in.length;
		brushsides=new v42BrushSide[numBrshsds];
		for(int i=0;i<numBrshsds;i++) {
			byte[] brushSideBytes=new byte[8];
			for(int j=0;j<8;j++) {
				brushSideBytes[j]=in[offset+j];
			}
			brushsides[i]=new v42BrushSide(brushSideBytes);
			offset+=8;
		}
	}
	
	// METHODS
	
	// -populateBrushSideList()
	// Uses the data file in the instance data to populate the
	// array of BrushSide objects with the data from the file
	private void populateBrushSideList() throws java.io.FileNotFoundException, java.io.IOException {
		FileInputStream reader=new FileInputStream(data);
		for(int i=0;i<numBrshsds;i++) {
			byte[] datain=new byte[8];
			reader.read(datain);
			brushsides[i]=new v42BrushSide(datain);
		}
		reader.close();
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
	
	public v42BrushSide getBrushSide(int i) {
		return brushsides[i];
	}
	
	public v42BrushSide[] getBrushSides() {
		return brushsides;
	}
	
	public void setBrushSide(int i, v42BrushSide in) {
		brushsides[i]=in;
	}
}