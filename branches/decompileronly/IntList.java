// IntList class

// This class holds an array of integers. The lump which these indices reference
// depends on the context.

import java.io.FileInputStream;
import java.io.File;

public class IntList {

	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	private File data;
	private int length;
	private int numInts=0;
	private int[] ints;
	
	// CONSTRUCTORS
	
	// This one accepts the lump path as a String
	public IntList(String in) {
		data=new File(in);
		try {
			numInts=getNumElements();
			length=(int)data.length();
			ints=new int[numInts];
			populateIntList();
		} catch(java.io.FileNotFoundException e) {
			Window.println("ERROR: File "+data.getPath()+" not found!",0);
		} catch(java.io.IOException e) {
			Window.println("ERROR: File "+data.getPath()+" could not be read, ensure the file is not open in another program",0);
		}
	}
	
	// This one accepts the input file path as a File
	public IntList(File in) {
		data=in;
		try {
			numInts=getNumElements();
			length=(int)data.length();
			ints=new int[numInts];
			populateIntList();
		} catch(java.io.FileNotFoundException e) {
			Window.println("ERROR: File "+data.getPath()+" not found!",0);
		} catch(java.io.IOException e) {
			Window.println("ERROR: File "+data.getPath()+" could not be read, ensure the file is not open in another program",0);
		}
	}
	
	public IntList(byte[] in) {
		int offset=0;
		numInts=in.length/4;
		length=in.length;
		ints=new int[numInts];
		for(int i=0;i<numInts;i++) {
			byte[] intBytes=new byte[4];
			for(int j=0;j<4;j++) {
				intBytes[j]=in[offset+j];
			}
			ints[i]=(intBytes[3] << 24) | ((intBytes[2] & 0xff) << 16) | ((intBytes[1] & 0xff) << 8) | (intBytes[0] & 0xff);
			offset+=4;
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
	
	// For debug purposes only
	public void printList() {
		for(int i=0;i<ints.length;i++) {
			Window.println(ints[i]+"",Window.VERBOSITY_ALWAYS);
		}
	}
	
	// ACCESSORS/MUTATORS
	
	// Returns the length (in bytes) of the lump
	public int getLength() {
		return length;
	}
	
	public int indexOf(int needle) {
		for(int i=0;i<ints.length;i++) {
			if(ints[i]==needle) {
				return i;
			}
		}
		return -1;
	}
	
	// Returns the number of brush indices. This lump is RETARDED.
	public int getNumElements() {
		if(numInts==0) {
			return length/4;
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
