// v38Faces class

// Contains all information for faces for a BSPv38

import java.io.FileInputStream;
import java.io.File;

public class v38Faces {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	private File data;
	private int numFaces=0;
	private int length;
	private v38Face[] faces;

	// CONSTRUCTORS
	
	public v38Faces(String in) {
		data=new File(in);
		length=(int)data.length();
		try {
			numFaces=getNumElements();
			faces=new v38Face[numFaces];
			populateFaceList();
		} catch(java.io.FileNotFoundException e) {
			Window.window.println("ERROR: File "+data+" not found!");
		} catch(java.io.IOException e) {
			Window.window.println("ERROR: File "+data+" could not be read, ensure the file is not open in another program");
		}
	}
	
	// This one accepts the input file path as a File
	public v38Faces(File in) {
		data=in;
		length=(int)data.length();
		try {
			numFaces=getNumElements();
			faces=new v38Face[numFaces];
			populateFaceList();
		} catch(java.io.FileNotFoundException e) {
			Window.window.println("ERROR: File "+data+" not found!");
		} catch(java.io.IOException e) {
			Window.window.println("ERROR: File "+data+" could not be read, ensure the file is not open in another program");
		}
	}
	
	public v38Faces(byte[] in) {
		int offset=0;
		length=in.length;
		numFaces=in.length/20;
		faces=new v38Face[numFaces];
		for(int i=0;i<numFaces;i++) {
			byte[] faceBytes=new byte[20];
			for(int j=0;j<20;j++) {
				faceBytes[j]=in[offset+j];
			}
			faces[i]=new v38Face(faceBytes);
			offset+=20;
		}
	}
	
	// METHODS
	
	// -populateFaceList()
	// Uses the instance data to populate the array of v38Face
	private void populateFaceList() throws java.io.FileNotFoundException, java.io.IOException {
		FileInputStream reader=new FileInputStream(data);
		for(int i=0;i<numFaces;i++) {
			byte[] datain=new byte[20];
			reader.read(datain);
			faces[i]=new v38Face(datain);
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
		if(numFaces==0) {
			return length/76;
		} else {
			return numFaces;
		}
	}
	
	public v38Face getFace(int i) {
		return faces[i];
	}
	
	public v38Face[] getFaces() {
		return faces;
	}
}