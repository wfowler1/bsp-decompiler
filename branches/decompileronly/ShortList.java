// ShortList class

// This class holds an array of integers. The lump which these indices reference
// depends on the context.

import java.io.FileInputStream;
import java.io.File;

public class ShortList {

	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	private File data;
	private int length;
	private int numShorts=0;
	private short[] shorts;
	
	// CONSTRUCTORS
	
	// This one accepts the lump path as a String
	public ShortList(String in) {
		data=new File(in);
		try {
			numShorts=getNumElements();
			length=(int)data.length();
			shorts=new short[numShorts];
			populateShortList();
		} catch(java.io.FileNotFoundException e) {
			Window.window.println("ERROR: File "+data+" not found!");
		} catch(java.io.IOException e) {
			Window.window.println("ERROR: File "+data+" could not be read, ensure the file is not open in another program");
		}
	}
	
	// This one accepts the input file path as a File
	public ShortList(File in) {
		data=in;
		try {
			numShorts=getNumElements();
			length=(int)data.length();
			shorts=new short[numShorts];
			populateShortList();
		} catch(java.io.FileNotFoundException e) {
			Window.window.println("ERROR: File "+data+" not found!");
		} catch(java.io.IOException e) {
			Window.window.println("ERROR: File "+data+" could not be read, ensure the file is not open in another program");
		}
	}
	
	public ShortList(byte[] in) {
		int offset=0;
		numShorts=in.length/2;
		length=in.length;
		shorts=new short[numShorts];
		for(int i=0;i<numShorts;i++) {
			byte[] bytes=new byte[2];
			for(int j=0;j<2;j++) {
				bytes[j]=in[offset+j];
			}
			shorts[i]=(short)((bytes[1] << 8) | (bytes[0] & 0xff));
			offset+=2;
		}
	}
	
	// METHODS
	
	// -populateShortList()
	// Uses the file in the instance data to populate the list of indices
	private void populateShortList() throws java.io.FileNotFoundException, java.io.IOException {
		FileInputStream reader=new FileInputStream(data);
		for(int i=0;i<numShorts;i++) {
			byte[] datain=new byte[2];
			reader.read(datain);
			shorts[i]=(short)((datain[1] << 8) | (datain[0] & 0xff));
		}
		reader.close();
	}
	
	// ACCESSORS/MUTATORS
	
	// Returns the length (in bytes) of the lump
	public int getLength() {
		return length;
	}
	
	// Returns the number of brush indices. This lump is RETARDED.
	public int getNumElements() {
		if(numShorts==0) {
			return length/2;
		} else {
			return numShorts;
		}
	}
	
	public void setShort(int i, short in) {
		shorts[i]=in;
	}
	
	public short getShort(int i) {
		return shorts[i];
	}
	
	public short[] getShort() {
		return shorts;
	}
}
