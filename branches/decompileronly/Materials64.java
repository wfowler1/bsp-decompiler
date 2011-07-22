// Materials64 class

// This class holds an array of Strings which is a list of materials. These
// do not end in ".RMT" because the game engine reads them with no extension.

import java.io.File;
import java.util.Scanner;
import java.io.PrintWriter;

public class Materials64 {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	private File data;
	private int numMtrls=0;
	private String[] materials;
	
	// CONSTRUCTORS
	
	// This one accepts the lump path as a String
	public Materials64(String in) {
		data=new File(in);
		try {
			numMtrls=getNumElements();
			materials=new String[numMtrls];
			populateMaterialList();
		} catch(java.io.FileNotFoundException e) {
			Window.window.println("ERROR: File "+data+" not found!");
		}
	}
	
	// This one accepts the input file path as a File
	public Materials64(File in) {
		data=in;
		try {
			numMtrls=getNumElements();
			materials=new String[numMtrls];
			populateMaterialList();
		} catch(java.io.FileNotFoundException e) {
			Window.window.println("ERROR: File "+data+" not found!");
		}
	}
	
	// METHODS
	
	// -populateMaterialList()
	// Uses the file defined in the instance data to populate a list of materials.
	private void populateMaterialList() throws java.io.FileNotFoundException {
		Scanner reader=new Scanner(data);
		reader.useDelimiter((char)0x00+"");
		int current=0;
		while(current<numMtrls) {
			materials[current]=reader.next();
			if(!materials[current].equals("")) {
				current++;
			}
		}
	}
		
	// ACCESSORS/MUTATORS
	
	// Returns the length (in bytes) of the lump
	public long getLength() {
		return data.length();
	}
	
	// Returns the number of materials.
	public int getNumElements() {
		if(numMtrls==0) {
			return (int)data.length()/64;
		} else {
			return numMtrls;
		}
	}
	
	public String getMaterial(int i) {
		return materials[i];
	}
	
	public String[] getMaterials() {
		return materials;
	}
	
	public void setMaterial(int i, String in) {
		materials[i]=in;
	}
}