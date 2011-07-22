// IntList class

// This class holds an array of integers. The lump which these indices reference
// depends on the context.

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.File;

public class IntList {

	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	private File data;
	private int numInts=0;
	private int[] ints;
	
	// CONSTRUCTORS
	
	// This one accepts the lump path as a String
	public IntList(String in) {
		data=new File(in);
		try {
			numInts=getNumElements();
			ints=new int[numInts];
			populateIntList();
		} catch(java.io.FileNotFoundException e) {
			Window.window.println("ERROR: File "+data+" not found!");
		} catch(java.io.IOException e) {
			Window.window.println("ERROR: File "+data+" could not be read, ensure the file is not open in another program");
		}
	}
	
	// This one accepts the input file path as a File
	public IntList(File in) {
		data=in;
		try {
			numInts=getNumElements();
			ints=new int[numInts];
			populateIntList();
		} catch(java.io.FileNotFoundException e) {
			Window.window.println("ERROR: File "+data+" not found!");
		} catch(java.io.IOException e) {
			Window.window.println("ERROR: File "+data+" could not be read, ensure the file is not open in another program");
		}
	}
	
	// METHODS
	
	// -populateIntList()
	// Uses the file in the instance data to populate the list of indices
	private void populateIntList() throws java.io.FileNotFoundException, java.io.IOException {
		FileInputStream reader=new FileInputStream(data);
		for(int i=0;i<numInts;i++) {
			byte[] datain=new byte[4];
			reader.read(datain);
			ints[i]=(datain[3] << 24) | ((datain[2] & 0xff) << 16) | ((datain[1] & 0xff) << 8) | (datain[0] & 0xff);
		}
		reader.close();
	}
	
	// ACCESSORS/MUTATORS
	
	// Returns the length (in bytes) of the lump
	public long getLength() {
		return data.length();
	}
	
	// Returns the number of brush indices. This lump is RETARDED.
	public int getNumElements() {
		if(numInts==0) {
			return (int)data.length()/4;
		} else {
			return numInts;
		}
	}
	
	public void setInt(int i, int in) {
		ints[i]=in;
	}
	
	public int getInt(int i) {
		return ints[i];
	}
	
	public int[] getInt() {
		return ints;
	}
}
