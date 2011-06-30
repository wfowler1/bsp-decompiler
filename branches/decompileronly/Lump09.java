// Lump09 class

// Stores references to each face object in a BSP.

import java.io.FileInputStream;
import java.io.File;

public class Lump09 {

	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	private File data;
	private int numFaces=0;
	// There are two types of faces, world and model. The BSP compiler and the game engine
	// fails to make the distinction, instead reading all world faces into model 0, and all
	// model faces are read by models 1-numModels. Model 0 is the world model, dereference
	// all the faces/leaves for it and the world disappears visually but is still physically
	// there.
	private int numWorldFaces=0;
	private int numModelFaces=0;
	private Face[] faces;
	
	// CONSTRUCTORS
	
	// This one accepts the lump path as a String
	public Lump09(String in) {
		data=new File(in);
		try {
			numFaces=getNumElements();
			numWorldFaces=getNumWorldFaces();
			numModelFaces=numFaces-numWorldFaces;
			faces=new Face[numFaces];
			populateFaceList();
		} catch(java.io.FileNotFoundException e) {
			System.out.println("ERROR: File "+data+" not found!");
		} catch(java.io.IOException e) {
			System.out.println("ERROR: File "+data+" could not be read, ensure the file is not open in another program");
		}
	}
	
	// This one accepts the input file path as a File
	public Lump09(File in) {
		data=in;
		try {
			numFaces=getNumElements();
			numWorldFaces=getNumWorldFaces();
			numModelFaces=numFaces-numWorldFaces;
			faces=new Face[numFaces];
			populateFaceList();
		} catch(java.io.FileNotFoundException e) {
			System.out.println("ERROR: File "+data+" not found!");
		} catch(java.io.IOException e) {
			System.out.println("ERROR: File "+data+" could not be read, ensure the file is not open in another program");
		}
	}
	
	// Accepts an array of Face objects and sets the entire lump to it
	public Lump09(Face[] in) {
		faces=in;
		numFaces=faces.length;
	}
	
	// METHODS
	
	// -populateFaceList()
	// Creates an array of all the faces in the lump using the instance data.
	private void populateFaceList() throws java.io.FileNotFoundException, java.io.IOException {
		FileInputStream reader=new FileInputStream(data);
		try {
			for(int i=0;i<numFaces;i++) {
				byte[] datain=new byte[48];
				reader.read(datain);
				faces[i]=new Face(datain);
			}
		} catch(InvalidFaceException e) {
			System.out.println("WARNING: Funny lump size in "+data+", ignoring last face.");
		}
		reader.close();
	}
		
	// ACCESSORS/MUTATORS
	
	// Returns the length (in bytes) of the lump
	public long getLength() {
		return data.length();
	}
	
	// Returns the number of faces.
	public int getNumElements() {
		if(numFaces==0) {
			return (int)data.length()/48;
		} else {
			return numFaces;
		}
	}
	
	// The only way to separate world faces from model faces is to check lump14.
	// One limitation: This depends on the world faces starting from face 0, as
	// done by all compilers. This could complicate things with manually added
	// faces.
	public int getNumWorldFaces() throws java.io.FileNotFoundException, java.io.IOException {
		if(numWorldFaces!=0) {
			return numWorldFaces;
		} // else
		FileInputStream numWorldFaceGrabber=new FileInputStream(data.getParent()+"\\14 - Models.hex");
		byte[] numWorldFacesAsByteArray=new byte[4];
		numWorldFaceGrabber.skip(52);
		numWorldFaceGrabber.read(numWorldFacesAsByteArray);
		int numWF = numWorldFacesAsByteArray[0] + numWorldFacesAsByteArray[1]*256 + numWorldFacesAsByteArray[2]*65536 + numWorldFacesAsByteArray[3]*16777216;
		numWorldFaceGrabber.close();
		return numWF;
	}
	
	public int getNumModelFaces() {
		return numModelFaces;
	}
	
	public Face getFace(int i) {
		return faces[i];
	}
	
	public Face[] getFaces() {
		return faces;
	}
}
