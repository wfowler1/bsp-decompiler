// v46Textures class

// Contains all information for leaves for a BSPv46

import java.io.FileInputStream;
import java.io.File;

public class v46Textures {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	private File data;
	private int numElems=0;
	private int length;
	private v46Texture[] elements;
	
	public static int structLength=72;

	// CONSTRUCTORS
	
	public v46Textures(String in) {
		data=new File(in);
		length=(int)data.length();
		try {
			numElems=getNumElements();
			elements=new v46Texture[numElems];
			populateList();
		} catch(java.io.FileNotFoundException e) {
			Window.println("ERROR: File "+data.getPath()+" not found!",0);
		} catch(java.io.IOException e) {
			Window.println("ERROR: File "+data.getPath()+" could not be read, ensure the file is not open in another program",0);
		}
	}
	
	// This one accepts the input file path as a File
	public v46Textures(File in) {
		data=in;
		length=(int)data.length();
		try {
			numElems=getNumElements();
			elements=new v46Texture[numElems];
			populateList();
		} catch(java.io.FileNotFoundException e) {
			Window.println("ERROR: File "+data.getPath()+" not found!",0);
		} catch(java.io.IOException e) {
			Window.println("ERROR: File "+data.getPath()+" could not be read, ensure the file is not open in another program",0);
		}
	}
	
	public v46Textures(byte[] in) {
		int offset=0;
		length=in.length;
		numElems=in.length/structLength;
		elements=new v46Texture[numElems];
		for(int i=0;i<numElems;i++) {
			byte[] bytes=new byte[structLength];
			for(int j=0;j<structLength;j++) {
				bytes[j]=in[offset+j];
			}
			elements[i]=new v46Texture(bytes);
			offset+=structLength;
		}
	}
	
	// METHODS
	
	// -populateList()
	// Uses the instance data to populate the array of v38Texture
	private void populateList() throws java.io.FileNotFoundException, java.io.IOException {
		FileInputStream reader=new FileInputStream(data);
		for(int i=0;i<numElems;i++) {
			byte[] datain=new byte[structLength];
			reader.read(datain);
			elements[i]=new v46Texture(datain);
		}
		reader.close();
	}
	
	// ACCESSORS/MUTATORS
	
	// Returns the length (in bytes) of the lump
	public int getLength() {
		return length;
	}
	
	// Returns the number of Models.
	public int getNumElements() {
		if(numElems==0) {
			return length/structLength;
		} else {
			return numElems;
		}
	}
	
	public v46Texture getTexture(int i) {
		return elements[i];
	}
	
	public v46Texture[] getTextures() {
		return elements;
	}
}