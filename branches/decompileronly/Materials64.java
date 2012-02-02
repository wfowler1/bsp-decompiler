// Materials64 class

// This class holds an array of Strings which is a list of materials. These
// do not end in ".RMT" because the game engine reads them with no extension.

import java.io.File;
import java.util.Scanner;
import java.io.PrintWriter;

public class Materials64 {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	private File data;
	private int length;
	private int numMtrls=0;
	private String[] materials;
	
	// CONSTRUCTORS
	
	// This one accepts the lump path as a String
	public Materials64(String in) {
		data=new File(in);
		try {
			numMtrls=getNumElements();
			length=(int)data.length();
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
			length=(int)data.length();
			materials=new String[numMtrls];
			populateMaterialList();
		} catch(java.io.FileNotFoundException e) {
			Window.window.println("ERROR: File "+data+" not found!");
		}
	}
	
	public Materials64(byte[] in) {
		int offset=0;
		numMtrls=in.length/64;
		length=in.length;
		materials=new String[numMtrls];
		for(int i=0;i<numMtrls;i++) {
			materials[i]=(char)in[offset]+""; // must do this first. Doing += right away adds "null" to the beginning
			for(int j=1;j<64;j++) {
				if(in[offset+j]==0x00) {
					break;
				} // else
				materials[i]+=(char)in[offset+j]+"";
			}
			offset+=64;
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
	public int getLength() {
		return length;
	}
	
	// Returns the number of materials.
	public int getNumElements() {
		if(numMtrls==0) {
			return length/64;
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