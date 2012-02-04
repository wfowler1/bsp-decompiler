// v42Faces class

// Stores references to each face object in a BSP.

import java.io.FileInputStream;
import java.io.File;

public class v42Faces {

	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	private File data;
	private int numFaces=0;
	private int length;
	// There are two types of faces, world and model. The BSP compiler and the game engine
	// fails to make the distinction, instead reading all world faces into model 0, and all
	// model faces are read by models 1-numModels. Model 0 is the world model, dereference
	// all the faces/leaves for it and the world disappears visually but is still physically
	// there.
	private int numWorldFaces=0;
	private int numModelFaces=0;
	private v42Face[] faces;
	
	// CONSTRUCTORS
	
	// This one accepts the lump path as a String
	public v42Faces(String in) {
		data=new File(in);
		try {
			numFaces=getNumElements();
			length=(int)data.length();
			numWorldFaces=getNumWorldFaces();
			numModelFaces=numFaces-numWorldFaces;
			faces=new v42Face[numFaces];
			populateFaceList();
		} catch(java.io.FileNotFoundException e) {
			Window.window.println("ERROR: File "+data+" not found!");
		} catch(java.io.IOException e) {
			Window.window.println("ERROR: File "+data+" could not be read, ensure the file is not open in another program");
		}
	}
	
	// This one accepts the input file path as a File
	public v42Faces(File in) {
		data=in;
		try {
			numFaces=getNumElements();
			length=(int)data.length();
			numWorldFaces=getNumWorldFaces();
			numModelFaces=numFaces-numWorldFaces;
			faces=new v42Face[numFaces];
			populateFaceList();
		} catch(java.io.FileNotFoundException e) {
			Window.window.println("ERROR: File "+data+" not found!");
		} catch(java.io.IOException e) {
			Window.window.println("ERROR: File "+data+" could not be read, ensure the file is not open in another program");
		}
	}
	
	// Accepts an array of Face objects and sets the entire lump to it
	public v42Faces(v42Face[] in) {
		faces=in;
		numFaces=faces.length;
	}
	
	public v42Faces(byte[] in) {
		int offset=0;
		numFaces=in.length/48;
		length=in.length;
		faces=new v42Face[numFaces];
		for(int i=0;i<numFaces;i++) {
			byte[] faceBytes=new byte[48];
			for(int j=0;j<48;j++) {
				faceBytes[j]=in[offset+j];
			}
			faces[i]=new v42Face(faceBytes);
			offset+=48;
		}
	}
	
	// METHODS
	
	// -populateFaceList()
	// Creates an array of all the faces in the lump using the instance data.
	private void populateFaceList() throws java.io.FileNotFoundException, java.io.IOException {
		FileInputStream reader=new FileInputStream(data);
		for(int i=0;i<numFaces;i++) {
			byte[] datain=new byte[48];
			reader.read(datain);
			faces[i]=new v42Face(datain);
		}
		reader.close();
	}
		
	// ACCESSORS/MUTATORS
	
	// Returns the length (in bytes) of the lump
	public int getLength() {
		return length;
	}
	
	// Returns the number of faces.
	public int getNumElements() {
		if(numFaces==0) {
			return length/48;
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
		int numWF=0;
		try {
			FileInputStream numWorldFaceGrabber=new FileInputStream(data.getParent()+"\\Models.hex");
			byte[] numWorldFacesAsByteArray=new byte[4];
			numWorldFaceGrabber.skip(52);
			numWorldFaceGrabber.read(numWorldFacesAsByteArray);
			numWF = numWorldFacesAsByteArray[0] + numWorldFacesAsByteArray[1]*256 + numWorldFacesAsByteArray[2]*65536 + numWorldFacesAsByteArray[3]*16777216;
			numWorldFaceGrabber.close();
		} catch(java.io.FileNotFoundException e) {
			Window.window.println("ERROR: File "+data.getParent()+"\\Models.hex not found!");
		} catch(java.io.IOException e) {
			Window.window.println("ERROR: File "+data.getParent()+"\\Models.hex could not be read, ensure the file is not open in another program");
		}
		return numWF;
	}
	
	public int getNumModelFaces() {
		return numModelFaces;
	}
	
	public v42Face getFace(int i) {
		return faces[i];
	}
	
	public v42Face[] getFaces() {
		return faces;
	}
}
