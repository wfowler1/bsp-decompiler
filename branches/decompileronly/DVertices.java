// DVertices class

// Contains all information for vertices in a Doom map

import java.io.FileInputStream;
import java.io.File;

public class DVertices {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	private File data;
	private int numElems=0;
	private int length;
	private Vector3D[] elements;

	public static int structureLength=4;

	// CONSTRUCTORS
	
	public DVertices(String in) {
		data=new File(in);
		length=(int)data.length();
		try {
			numElems=getNumElements();
			elements=new Vector3D[numElems];
			populateList();
		} catch(java.io.FileNotFoundException e) {
			Window.window.println("ERROR: File "+data.getName()+" not found!");
		} catch(java.io.IOException e) {
			Window.window.println("ERROR: File "+data.getName()+" could not be read, ensure the file is not open in another program");
		}
	}
	
	// This one accepts the input file path as a File
	public DVertices(File in) {
		data=in;
		length=(int)data.length();
		try {
			numElems=getNumElements();
			elements=new Vector3D[numElems];
			populateList();
		} catch(java.io.FileNotFoundException e) {
			Window.window.println("ERROR: File "+data.getName()+" not found!");
		} catch(java.io.IOException e) {
			Window.window.println("ERROR: File "+data.getName()+" could not be read, ensure the file is not open in another program");
		}
	}
	
	public DVertices(byte[] in) {
		int offset=0;
		length=in.length;
		numElems=in.length/structureLength;
		elements=new Vector3D[numElems];
		for(int i=0;i<numElems;i++) {
			byte[] bytes=new byte[structureLength];
			for(int j=0;j<structureLength;j++) {
				bytes[j]=in[offset+j];
			}
			short x=DataReader.readShort(bytes[0], bytes[1]);
			short y=DataReader.readShort(bytes[2], bytes[3]);
			elements[i]=new Vector3D(x, y);
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
			elements[i]=new Vector3D(datain);
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
	
	public Vector3D getVertex(int i) {
		return elements[i];
	}
	
	public Vector3D[] getVertices() {
		return elements;
	}
}