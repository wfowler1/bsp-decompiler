// DNodes class

// Contains all information for nodes in a Doom map

import java.io.FileInputStream;
import java.io.File;

public class DNodes {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	private File data;
	private int numElems=0;
	private int length;
	private DNode[] elements;

	public static int structureLength=28;

	// CONSTRUCTORS
	
	public DNodes(String in) {
		data=new File(in);
		length=(int)data.length();
		try {
			numElems=getNumElements();
			elements=new DNode[numElems];
			populateList();
		} catch(java.io.FileNotFoundException e) {
			Window.println("ERROR: File "+data.getName()+" not found!",0);
		} catch(java.io.IOException e) {
			Window.println("ERROR: File "+data.getName()+" could not be read, ensure the file is not open in another program",0);
		}
	}
	
	// This one accepts the input file path as a File
	public DNodes(File in) {
		data=in;
		length=(int)data.length();
		try {
			numElems=getNumElements();
			elements=new DNode[numElems];
			populateList();
		} catch(java.io.FileNotFoundException e) {
			Window.println("ERROR: File "+data.getName()+" not found!",0);
		} catch(java.io.IOException e) {
			Window.println("ERROR: File "+data.getName()+" could not be read, ensure the file is not open in another program",0);
		}
	}
	
	public DNodes(byte[] in) {
		int offset=0;
		length=in.length;
		numElems=in.length/structureLength;
		elements=new DNode[numElems];
		for(int i=0;i<numElems;i++) {
			byte[] bytes=new byte[structureLength];
			for(int j=0;j<structureLength;j++) {
				bytes[j]=in[offset+j];
			}
			elements[i]=new DNode(bytes);
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
			elements[i]=new DNode(datain);
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
	
	public DNode getNode(int i) {
		return elements[i];
	}
	
	public DNode[] getNodes() {
		return elements;
	}
}