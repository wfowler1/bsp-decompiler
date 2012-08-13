// DLinedefs class

// Contains all information for linedefs in a Doom map

import java.io.FileInputStream;
import java.io.File;

public class DLinedefs {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	private File data;
	private int numElems=0;
	private int length;
	private DLinedef[] elements;

	public static int structureLength=14;

	// CONSTRUCTORS
	
	public DLinedefs(String in) {
		data=new File(in);
		length=(int)data.length();
		try {
			numElems=getNumElements();
			elements=new DLinedef[numElems];
			populateList();
		} catch(java.io.FileNotFoundException e) {
			Window.println("ERROR: File "+data.getName()+" not found!",0);
		} catch(java.io.IOException e) {
			Window.println("ERROR: File "+data.getName()+" could not be read, ensure the file is not open in another program",0);
		}
	}
	
	// This one accepts the input file path as a File
	public DLinedefs(File in) {
		data=in;
		length=(int)data.length();
		try {
			numElems=getNumElements();
			elements=new DLinedef[numElems];
			populateList();
		} catch(java.io.FileNotFoundException e) {
			Window.println("ERROR: File "+data.getName()+" not found!",0);
		} catch(java.io.IOException e) {
			Window.println("ERROR: File "+data.getName()+" could not be read, ensure the file is not open in another program",0);
		}
	}
	
	public DLinedefs(byte[] in) {
		int offset=0;
		length=in.length;
		numElems=in.length/structureLength;
		elements=new DLinedef[numElems];
		for(int i=0;i<numElems;i++) {
			byte[] bytes=new byte[structureLength];
			for(int j=0;j<structureLength;j++) {
				bytes[j]=in[offset+j];
			}
			elements[i]=new DLinedef(bytes);
			offset+=structureLength;
		}
	}
	
	// METHODS
	
	// -populateList()
	// Uses the instance data to populate the array of DLinedef
	private void populateList() throws java.io.FileNotFoundException, java.io.IOException {
		FileInputStream reader=new FileInputStream(data);
		for(int i=0;i<numElems;i++) {
			byte[] datain=new byte[structureLength];
			reader.read(datain);
			elements[i]=new DLinedef(datain);
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
	
	public DLinedef getLinedef(int i) {
		return elements[i];
	}
	
	public DLinedef[] getLinedefs() {
		return elements;
	}
}