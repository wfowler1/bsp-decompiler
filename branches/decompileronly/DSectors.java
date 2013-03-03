// DSectors class

// Maintains an array of DSectors.

import java.io.FileInputStream;
import java.io.File;

public class DSectors {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	private File data;
	private int length;
	private DSector[] elements;
	
	private int structLength=26;

	// CONSTRUCTORS
	
	// Accepts a filepath as a String
	public DSectors(String in) throws java.lang.InterruptedException {
		new DSectors(new File(in));
	}
	
	// This one accepts the input file path as a File
	public DSectors(File in) throws java.lang.InterruptedException {
		data=in;
		try {
			FileInputStream fileReader=new FileInputStream(data);
			byte[] temp=new byte[(int)data.length()];
			fileReader.read(temp);
			new DSectors(temp);
			fileReader.close();
		} catch(java.io.FileNotFoundException e) {
			Window.println("ERROR: File "+data.getPath()+" not found!",Window.VERBOSITY_ALWAYS);
		} catch(java.io.IOException e) {
			Window.println("ERROR: File "+data.getPath()+" could not be read, ensure the file is not open in another program",Window.VERBOSITY_ALWAYS);
		}
	}
	
	// Takes a byte array, as if read from a FileInputStream
	public DSectors(byte[] in) throws java.lang.InterruptedException {
		int offset=0;
		length=in.length;
		elements=new DSector[in.length/structLength];
		byte[] bytes=new byte[structLength];
		for(int i=0;i<elements.length;i++) {
			if(Thread.currentThread().interrupted()) {
				throw new java.lang.InterruptedException("while populating Sector array");
			}
			for(int j=0;j<structLength;j++) {
				bytes[j]=in[offset+j];
			}
			elements[i]=new DSector(bytes);
			offset+=structLength;
		}
	}
	
	// METHODS
	
	// ACCESSORS/MUTATORS
	
	// Returns the length (in bytes) of the lump
	public int getLength() {
		return length;
	}
	
	// Returns the number of elements.
	public int length() {
		return elements.length;
	}
	
	public DSector getElement(int i) {
		return elements[i];
	}
	
	public DSector[] getElements() {
		return elements;
	}
}