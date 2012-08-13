// Edges class

// Contains all information for an edges lump. This exact same structure
// appears in Half-Life, Quake and Quake 2 maps.

import java.io.FileInputStream;
import java.io.File;

public class Edges {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	private File data;
	private int numElems=0;
	private int length;
	private Edge[] elements;
	
	public static int structLength=4;

	// CONSTRUCTORS
	
	public Edges(String in) {
		data=new File(in);
		length=(int)data.length();
		try {
			numElems=getNumElements();
			elements=new Edge[numElems];
			populateList();
		} catch(java.io.FileNotFoundException e) {
			Window.println("ERROR: File "+data.getPath()+" not found!",0);
		} catch(java.io.IOException e) {
			Window.println("ERROR: File "+data.getPath()+" could not be read, ensure the file is not open in another program",0);
		}
	}
	
	// This one accepts the input file path as a File
	public Edges(File in) {
		data=in;
		length=(int)data.length();
		try {
			numElems=getNumElements();
			elements=new Edge[numElems];
			populateList();
		} catch(java.io.FileNotFoundException e) {
			Window.println("ERROR: File "+data.getPath()+" not found!",0);
		} catch(java.io.IOException e) {
			Window.println("ERROR: File "+data.getPath()+" could not be read, ensure the file is not open in another program",0);
		}
	}
	
	public Edges(byte[] in) {
		int offset=0;
		length=in.length;
		numElems=in.length/structLength;
		elements=new Edge[numElems];
		for(int i=0;i<numElems;i++) {
			byte[] bytes=new byte[structLength];
			for(int j=0;j<structLength;j++) {
				bytes[j]=in[offset+j];
			}
			elements[i]=new Edge(bytes);
			offset+=structLength;
		}
	}
	
	// METHODS
	
	// -populateList()
	// Uses the instance data to populate the array
	private void populateList() throws java.io.FileNotFoundException, java.io.IOException {
		FileInputStream reader=new FileInputStream(data);
		for(int i=0;i<numElems;i++) {
			byte[] datain=new byte[structLength];
			reader.read(datain);
			elements[i]=new Edge(datain);
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
	
	public Edge getEdge(int i) {
		return elements[i];
	}
	
	public Edge[] getEdges() {
		return elements;
	}
}