// DSubSectors class

// Maintains an array of DSubSectors.

import java.io.FileInputStream;
import java.io.File;

public class DSubSectors {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	private File data;
	private int length;
	private DSubSector[] elements;
	
	private int structLength=4;

	// CONSTRUCTORS
	
	// Accepts a filepath as a String
	public DSubSectors(String in) throws java.lang.InterruptedException {
		new DSubSectors(new File(in));
	}
	
	// This one accepts the input file path as a File
	public DSubSectors(File in) throws java.lang.InterruptedException {
		data=in;
		try {
			FileInputStream fileReader=new FileInputStream(data);
			byte[] temp=new byte[(int)data.length()];
			fileReader.read(temp);
			new DSubSectors(temp);
			fileReader.close();
		} catch(java.io.FileNotFoundException e) {
			Window.println("ERROR: File "+data.getPath()+" not found!",Window.VERBOSITY_ALWAYS);
		} catch(java.io.IOException e) {
			Window.println("ERROR: File "+data.getPath()+" could not be read, ensure the file is not open in another program",Window.VERBOSITY_ALWAYS);
		}
	}
	
	// Takes a byte array, as if read from a FileInputStream
	public DSubSectors(byte[] in) throws java.lang.InterruptedException {
		int offset=0;
		length=in.length;
		elements=new DSubSector[in.length/structLength];
		byte[] bytes=new byte[structLength];
		for(int i=0;i<elements.length;i++) {
			if(Thread.currentThread().interrupted()) {
				throw new java.lang.InterruptedException("while populating Subsector array");
			}
			for(int j=0;j<structLength;j++) {
				bytes[j]=in[offset+j];
			}
			elements[i]=new DSubSector(bytes);
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
		if(elements.length==0) {
			return length/structLength;
		} else {
			return elements.length;
		}
	}
	
	public DSubSector getElement(int i) {
		return elements[i];
	}
	
	public DSubSector[] getElements() {
		return elements;
	}
}