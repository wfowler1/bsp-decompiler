// v46Faces class

// Contains all information for faces for a BSPv46

import java.io.FileInputStream;
import java.io.File;

public class v46Faces {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	private File data;
	private int numElems=0;
	private int length;
	private v46Face[] elements;

	public static int structureLength=104;

	// CONSTRUCTORS
	
	public v46Faces(String in) {
		data=new File(in);
		length=(int)data.length();
		try {
			numElems=getNumElements();
			elements=new v46Face[numElems];
			populateList();
		} catch(java.io.FileNotFoundException e) {
			Window.println("ERROR: File "+data.getPath()+" not found!",0);
		} catch(java.io.IOException e) {
			Window.println("ERROR: File "+data.getPath()+" could not be read, ensure the file is not open in another program",0);
		}
	}
	
	// This one accepts the input file path as a File
	public v46Faces(File in) {
		data=in;
		length=(int)data.length();
		try {
			numElems=getNumElements();
			elements=new v46Face[numElems];
			populateList();
		} catch(java.io.FileNotFoundException e) {
			Window.println("ERROR: File "+data.getPath()+" not found!",0);
		} catch(java.io.IOException e) {
			Window.println("ERROR: File "+data.getPath()+" could not be read, ensure the file is not open in another program",0);
		}
	}
	
	public v46Faces(byte[] in) {
		int offset=0;
		length=in.length;
		numElems=in.length/structureLength;
		elements=new v46Face[numElems];
		for(int i=0;i<numElems;i++) {
			byte[] bytes=new byte[structureLength];
			for(int j=0;j<structureLength;j++) {
				bytes[j]=in[offset+j];
			}
			elements[i]=new v46Face(bytes);
			offset+=structureLength;
		}
	}
	
	// METHODS
	
	// -populateList()
	// Uses the instance data to populate the array of v46Face
	private void populateList() throws java.io.FileNotFoundException, java.io.IOException {
		FileInputStream reader=new FileInputStream(data);
		for(int i=0;i<numElems;i++) {
			byte[] datain=new byte[structureLength];
			reader.read(datain);
			elements[i]=new v46Face(datain);
		}
		reader.close();
	}
	
	// ACCESSORS/MUTATORS
	
	// Returns the length (in bytes) of the lump
	public int getLength() {
		return length;
	}
	
	// Returns the number of Faces.
	public int getNumElements() {
		if(numElems==0) {
			return length/structureLength;
		} else {
			return numElems;
		}
	}
	
	public v46Face getFace(int i) {
		return elements[i];
	}
	
	public v46Face[] getFaces() {
		return elements;
	}
}