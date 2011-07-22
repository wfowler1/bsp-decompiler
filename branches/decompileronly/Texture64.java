// Texture64 class

// This class holds an array of Strings which is a list of textures. These
// do not end in ".PNG" because the game engine reads them with no extension.

import java.io.File;
import java.util.Scanner;
import java.io.PrintWriter;

public class Texture64 {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	private File data;
	private int numTxts=0;
	private String[] textures;
	
	// CONSTRUCTORS
	
	// This one accepts the lump path as a String
	public Texture64(String in) {
		data=new File(in);
		try {
			numTxts=getNumElements();
			textures=new String[numTxts];
			populateTextureList();
		} catch(java.io.FileNotFoundException e) {
			Window.window.println("ERROR: File "+data+" not found!");
		}
	}
	
	// This one accepts the input file path as a File
	public Texture64(File in) {
		data=in;
		try {
			numTxts=getNumElements();
			textures=new String[numTxts];
			populateTextureList();
		} catch(java.io.FileNotFoundException e) {
			Window.window.println("ERROR: File "+data+" not found!");
		}
	}
	
	// METHODS
	
	// -populateTextureList()
	// Uses the file provided in the instance data to populate an array of String.
	// This array is really a list of textures.
	private void populateTextureList() throws java.io.FileNotFoundException {
		Scanner reader=new Scanner(data);
		reader.useDelimiter((char)0x00+"");
		int current=0;
		while(current<numTxts) {
			textures[current]=reader.next();
			if(!textures[current].equals("")) {
				current++;
			}
		}
	}
		
	// ACCESSORS/MUTATORS
	
	// Returns the length (in bytes) of the lump
	public long getLength() {
		return data.length();
	}
	
	// Returns the number of textures.
	public int getNumElements() {
		if(numTxts==0) {
			return (int)data.length()/64;
		} else {
			return numTxts;
		}
	}
	
	// Returns the texture as a String
	public String getTexture(int i) {
		return textures[i];
	}
	
	public String[] getTextures() {
		return textures;
	}
	
	public void setTexture(int i, String in) {
		textures[i]=in;
	}
}