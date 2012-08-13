// v38BrushSides class

// Contains all information for brush sides for a BSPv38
// Virtually identical to v46 except uses shorts instead of ints.

import java.io.FileInputStream;
import java.io.File;

public class v38BrushSides {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	private File data;
	private int numElems=0;
	private int length;
	private v38BrushSide[] elements;
	
	public static int structLength=4;

	// CONSTRUCTORS
	
	public v38BrushSides(String in) {
		data=new File(in);
		length=(int)data.length();
		try {
			numElems=getNumElements();
			elements=new v38BrushSide[numElems];
			populateList();
		} catch(java.io.FileNotFoundException e) {
			Window.println("ERROR: File "+data.getPath()+" not found!",0);
		} catch(java.io.IOException e) {
			Window.println("ERROR: File "+data.getPath()+" could not be read, ensure the file is not open in another program",0);
		}
	}
	
	// This one accepts the input file path as a File
	public v38BrushSides(File in) {
		data=in;
		length=(int)data.length();
		try {
			numElems=getNumElements();
			elements=new v38BrushSide[numElems];
			populateList();
		} catch(java.io.FileNotFoundException e) {
			Window.println("ERROR: File "+data.getPath()+" not found!",0);
		} catch(java.io.IOException e) {
			Window.println("ERROR: File "+data.getPath()+" could not be read, ensure the file is not open in another program",0);
		}
	}
	
	public v38BrushSides(byte[] in) {
		int offset=0;
		length=in.length;
		numElems=in.length/structLength;
		elements=new v38BrushSide[numElems];
		for(int i=0;i<numElems;i++) {
			byte[] bytes=new byte[structLength];
			for(int j=0;j<structLength;j++) {
				bytes[j]=in[offset+j];
			}
			elements[i]=new v38BrushSide(bytes);
			offset+=structLength;
		}
	}
	
	// METHODS
	
	// -populateList()
	// Uses the instance data to populate the array of v38BrushSide
	private void populateList() throws java.io.FileNotFoundException, java.io.IOException {
		FileInputStream reader=new FileInputStream(data);
		for(int i=0;i<numElems;i++) {
			byte[] datain=new byte[structLength];
			reader.read(datain);
			elements[i]=new v38BrushSide(datain);
		}
		reader.close();
	}
	
	// ACCESSORS/MUTATORS
	
	// Returns the length (in bytes) of the lump
	public int getLength() {
		return length;
	}
	
	// Returns the number of Leaves.
	public int getNumElements() {
		if(numElems==0) {
			return length/structLength;
		} else {
			return numElems;
		}
	}
	
	public v38BrushSide getBrushSide(int i) {
		return elements[i];
	}
	
	public v38BrushSide[] getBrushSides() {
		return elements;
	}
}