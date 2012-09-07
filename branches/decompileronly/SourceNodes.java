// SourceNodes class

// Contains all information for nodes for a BSPv38

import java.io.FileInputStream;
import java.io.File;

public class SourceNodes {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	private File data;
	private int numElems=0;
	private int length;
	private SourceNode[] elements;
	
	public static int structLength=32;

	// CONSTRUCTORS
	
	public SourceNodes(String in) {
		data=new File(in);
		length=(int)data.length();
		try {
			numElems=length();
			elements=new SourceNode[numElems];
			populateList();
		} catch(java.io.FileNotFoundException e) {
			Window.println("ERROR: File "+data.getPath()+" not found!",0);
		} catch(java.io.IOException e) {
			Window.println("ERROR: File "+data.getPath()+" could not be read, ensure the file is not open in another program",0);
		}
	}
	
	// This one accepts the input file path as a File
	public SourceNodes(File in) {
		data=in;
		length=(int)data.length();
		try {
			numElems=length();
			elements=new SourceNode[numElems];
			populateList();
		} catch(java.io.FileNotFoundException e) {
			Window.println("ERROR: File "+data.getPath()+" not found!",0);
		} catch(java.io.IOException e) {
			Window.println("ERROR: File "+data.getPath()+" could not be read, ensure the file is not open in another program",0);
		}
	}
	
	public SourceNodes(byte[] in) {
		int offset=0;
		length=in.length;
		numElems=in.length/structLength;
		elements=new SourceNode[numElems];
		for(int i=0;i<numElems;i++) {
			byte[] bytes=new byte[structLength];
			for(int j=0;j<structLength;j++) {
				bytes[j]=in[offset+j];
			}
			elements[i]=new SourceNode(bytes);
			offset+=structLength;
		}
	}
	
	// METHODS
	
	// -populateList()
	// Uses the instance data to populate the array of v38Leaf
	private void populateList() throws java.io.FileNotFoundException, java.io.IOException {
		FileInputStream reader=new FileInputStream(data);
		for(int i=0;i<numElems;i++) {
			byte[] datain=new byte[structLength];
			reader.read(datain);
			elements[i]=new SourceNode(datain);
		}
		reader.close();
	}
	
	// ACCESSORS/MUTATORS
	
	// Returns the length (in bytes) of the lump
	public int getLength() {
		return length;
	}
	
	// Returns the number of Leaves.
	public int length() {
		if(numElems==0) {
			return length/structLength;
		} else {
			return numElems;
		}
	}
	
	public SourceNode getNode(int i) {
		return elements[i];
	}
	
	public SourceNode[] getNodes() {
		return elements;
	}
}