// DSidedefs class

// Contains all information for sidedefs in a Doom map

import java.io.FileInputStream;
import java.io.File;

public class DSidedefs {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	private File data;
	private int numElems=0;
	private int length;
	private DSidedef[] elements;

	public static int structureLength=30;

	// CONSTRUCTORS
	
	public DSidedefs(String in) {
		data=new File(in);
		length=(int)data.length();
		try {
			numElems=getNumElements();
			elements=new DSidedef[numElems];
			populateList();
		} catch(java.io.FileNotFoundException e) {
			Window.println("ERROR: File "+data.getName()+" not found!",0);
		} catch(java.io.IOException e) {
			Window.println("ERROR: File "+data.getName()+" could not be read, ensure the file is not open in another program",0);
		}
	}
	
	// This one accepts the input file path as a File
	public DSidedefs(File in) {
		data=in;
		length=(int)data.length();
		try {
			numElems=getNumElements();
			elements=new DSidedef[numElems];
			populateList();
		} catch(java.io.FileNotFoundException e) {
			Window.println("ERROR: File "+data.getName()+" not found!",0);
		} catch(java.io.IOException e) {
			Window.println("ERROR: File "+data.getName()+" could not be read, ensure the file is not open in another program",0);
		}
	}
	
	public DSidedefs(byte[] in) {
		int offset=0;
		length=in.length;
		numElems=in.length/structureLength;
		elements=new DSidedef[numElems];
		for(int i=0;i<numElems;i++) {
			byte[] bytes=new byte[structureLength];
			for(int j=0;j<structureLength;j++) {
				bytes[j]=in[offset+j];
			}
			elements[i]=new DSidedef(bytes);
			offset+=structureLength;
		}
	}
	
	// METHODS
	
	// -populateList()
	// Uses the instance data to populate the array of DSidedef
	private void populateList() throws java.io.FileNotFoundException, java.io.IOException {
		FileInputStream reader=new FileInputStream(data);
		for(int i=0;i<numElems;i++) {
			byte[] datain=new byte[structureLength];
			reader.read(datain);
			elements[i]=new DSidedef(datain);
		}
		reader.close();
	}
	
	// ACCESSORS/MUTATORS
	
	// Returns the length (in bytes) of the lump
	public int getLength() {
		return length;
	}
	
	public int getNumElements() {
		if(numElems==0) {
			return length/structureLength;
		} else {
			return numElems;
		}
	}
	
	public DSidedef getSide(int i) {
		return elements[i];
	}
	
	public DSidedef[] getSides() {
		return elements;
	}
}