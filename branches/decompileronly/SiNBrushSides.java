// SiNBrushSides class

// Maintains an array of SiNBrushSides.

import java.io.FileInputStream;
import java.io.File;

public class SiNBrushSides {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	private File data;
	private int length;
	private SiNBrushSide[] elements;
	
	public static final int structLength=8;

	// CONSTRUCTORS
	
	// Accepts a filepath as a String
	public SiNBrushSides(String in) {
		new SiNBrushSides(new File(in));
	}
	
	// This one accepts the input file path as a File
	public SiNBrushSides(File in) {
		data=in;
		try {
			FileInputStream fileReader=new FileInputStream(data);
			byte[] temp=new byte[(int)data.length()];
			fileReader.read(temp);
			new SiNBrushSides(temp);
			fileReader.close();
		} catch(java.io.FileNotFoundException e) {
			Window.println("ERROR: File "+data.getPath()+" not found!",Window.VERBOSITY_ALWAYS);
		} catch(java.io.IOException e) {
			Window.println("ERROR: File "+data.getPath()+" could not be read, ensure the file is not open in another program",Window.VERBOSITY_ALWAYS);
		}
	}
	
	// Takes a byte array, as if read from a FileInputStream
	public SiNBrushSides(byte[] in) {
		int offset=0;
		length=in.length;
		elements=new SiNBrushSide[in.length/structLength];
		byte[] bytes=new byte[structLength];
		for(int i=0;i<elements.length;i++) {
			for(int j=0;j<structLength;j++) {
				bytes[j]=in[offset+j];
			}
			elements[i]=new SiNBrushSide(bytes);
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
	
	public SiNBrushSide getElement(int i) {
		return elements[i];
	}
	
	public SiNBrushSide[] getElements() {
		return elements;
	}
}