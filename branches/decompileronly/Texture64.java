// Texture64 class

// This class holds an array of Strings which is a list of textures. These
// do not end in ".PNG" because the game engine reads them with no extension.

import java.io.File;
import java.util.Scanner;
import java.io.PrintWriter;

public class Texture64 {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	private File data;
	private int length;
	private int numTxts=0;
	private String[] textures;
	
	// CONSTRUCTORS
	
	// This one accepts the lump path as a String
	public Texture64(String in) {
		data=new File(in);
		try {
			numTxts=getNumElements();
			length=(int)data.length();
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
			length=(int)data.length();
			textures=new String[numTxts];
			populateTextureList();
		} catch(java.io.FileNotFoundException e) {
			Window.window.println("ERROR: File "+data+" not found!");
		}
	}
	
	public Texture64(byte[] in) {
		int offset=0;
		numTxts=in.length/64;
		length=in.length;
		textures=new String[numTxts];
		for(int i=0;i<numTxts;i++) {
			textures[i]=(char)in[offset]+""; // must do this first. Doing += right away adds "null" to the beginning
			for(int j=1;j<64;j++) {
				if(in[offset+j]==0x00) {
					break;
				} // else
				textures[i]+=(char)in[offset+j]+"";
			}
			offset+=64;
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
	
	// +printTxts()
	// Prints the contents of every String in the array
	public void printTxts() {
		for(int i=0;i<numTxts;i++) {
			Window.window.println(textures[i]);
		}
	}
		
	// ACCESSORS/MUTATORS
	
	// Returns the length (in bytes) of the lump
	public int getLength() {
		return length;
	}
	
	// Returns the number of textures.
	public int getNumElements() {
		if(numTxts==0) {
			return length/64;
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