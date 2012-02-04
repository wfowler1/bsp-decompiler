// v42Strings64 class

// This class holds an array of Strings which is a list of either textures
// or materials. Either way, the implementation is exactly the same.

import java.io.File;
import java.util.Scanner;

// 64-byte Strings used in BSP v42
public class v42Strings64 {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	private File data;
	private int length;
	private int numStrings=0;
	private String[] strings;
	
	// CONSTRUCTORS
	
	// This one accepts the lump path as a String
	public v42Strings64(String in) {
		data=new File(in);
		try {
			numStrings=getNumElements();
			length=(int)data.length();
			strings=new String[numStrings];
			populateStringList();
		} catch(java.io.FileNotFoundException e) {
			Window.window.println("ERROR: File "+data+" not found!");
		}
	}
	
	// This one accepts the input file path as a File
	public v42Strings64(File in) {
		data=in;
		try {
			numStrings=getNumElements();
			length=(int)data.length();
			strings=new String[numStrings];
			populateStringList();
		} catch(java.io.FileNotFoundException e) {
			Window.window.println("ERROR: File "+data+" not found!");
		}
	}
	
	public v42Strings64(byte[] in) {
		int offset=0;
		numStrings=in.length/64;
		length=in.length;
		strings=new String[numStrings];
		for(int i=0;i<numStrings;i++) {
			strings[i]=(char)in[offset]+""; // must do this first. Doing += right away adds "null" to the beginning
			for(int j=1;j<64;j++) {
				if(in[offset+j]==0x00) {
					break;
				} // else
				strings[i]+=(char)in[offset+j]+"";
			}
			offset+=64;
		}
	}

	// METHODS

	// -populateStringList()
	// Uses the file provided in the instance data to populate an array of String.
	// The null characters which pad the string to 64 bytes are omitted
	private void populateStringList() throws java.io.FileNotFoundException {
		Scanner reader=new Scanner(data);
		reader.useDelimiter((char)0x00+"");
		int current=0;
		while(current<numStrings) {
			strings[current]=reader.next();
			if(!strings[current].equals("")) {
				current++;
			}
		}
	}
	
	// +printStrings()
	// Prints the contents of every String in the array
	public void printStrings() {
		for(int i=0;i<numStrings;i++) {
			Window.window.println(strings[i]);
		}
	}
		
	// ACCESSORS/MUTATORS
	
	// Returns the length (in bytes) of the lump
	public int getLength() {
		return length;
	}
	
	// Returns the number of Strings.
	public int getNumElements() {
		if(numStrings==0) {
			return length/64;
		} else {
			return numStrings;
		}
	}
	
	// Returns the String
	public String getString(int i) {
		return strings[i];
	}
	
	public String[] getStrings() {
		return strings;
	}
	
	public void setString(int i, String in) {
		strings[i]=in;
	}
}
