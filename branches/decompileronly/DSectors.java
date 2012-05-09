// DSectors class

// Contains all information for sectors in a Doom map

import java.io.FileInputStream;
import java.io.File;

public class DSectors {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	private File data;
	private int numElems=0;
	private int length;
	private DSector[] elements;

	public static int structureLength=26;

	// CONSTRUCTORS
	
	public DSectors(String in) {
		data=new File(in);
		length=(int)data.length();
		try {
			numElems=getNumElements();
			elements=new DSector[numElems];
			populateList();
		} catch(java.io.FileNotFoundException e) {
			Window.window.println("ERROR: File "+data.getName()+" not found!");
		} catch(java.io.IOException e) {
			Window.window.println("ERROR: File "+data.getName()+" could not be read, ensure the file is not open in another program");
		}
	}
	
	// This one accepts the input file path as a File
	public DSectors(File in) {
		data=in;
		length=(int)data.length();
		try {
			numElems=getNumElements();
			elements=new DSector[numElems];
			populateList();
		} catch(java.io.FileNotFoundException e) {
			Window.window.println("ERROR: File "+data.getName()+" not found!");
		} catch(java.io.IOException e) {
			Window.window.println("ERROR: File "+data.getName()+" could not be read, ensure the file is not open in another program");
		}
	}
	
	public DSectors(byte[] in) {
		int offset=0;
		length=in.length;
		numElems=in.length/structureLength;
		elements=new DSector[numElems];
		for(int i=0;i<numElems;i++) {
			byte[] bytes=new byte[structureLength];
			for(int j=0;j<structureLength;j++) {
				bytes[j]=in[offset+j];
			}
			elements[i]=new DSector(bytes);
			offset+=structureLength;
		}
	}
	
	// METHODS
	
	// -populateList()
	// Uses the instance data to populate the array of DNode
	private void populateList() throws java.io.FileNotFoundException, java.io.IOException {
		FileInputStream reader=new FileInputStream(data);
		for(int i=0;i<numElems;i++) {
			byte[] datain=new byte[structureLength];
			reader.read(datain);
			elements[i]=new DSector(datain);
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
	
	public DSector getSector(int i) {
		return elements[i];
	}
	
	public DSector[] getSectors() {
		return elements;
	}
}