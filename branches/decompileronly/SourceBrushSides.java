// SourceBrushSides class

// Maintains and array of SourceBrushSide for a Source engine BSP.

import java.io.FileInputStream;
import java.io.File;

public class SourceBrushSides {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	private File data;
	private int length;
	private SourceBrushSide[] elements;
	
	public static final int structLength=8;

	// CONSTRUCTORS
	
	// Accepts a filepath as a String
	public SourceBrushSides(String in) {
		new SourceBrushSides(new File(in));
	}
	
	// This one accepts the input file path as a File
	public SourceBrushSides(File in) {
		data=in;
		try {
			FileInputStream fileReader=new FileInputStream(data);
			byte[] temp=new byte[(int)data.length()];
			fileReader.read(temp);
			new SourceBrushSides(temp);
			fileReader.close();
		} catch(java.io.FileNotFoundException e) {
			Window.println("ERROR: File "+data.getPath()+" not found!",0);
		} catch(java.io.IOException e) {
			Window.println("ERROR: File "+data.getPath()+" could not be read, ensure the file is not open in another program",0);
		}
	}
	
	// Takes a byte array, as if read from a FileInputStream
	public SourceBrushSides(byte[] in) {
		int offset=0;
		length=in.length;
		elements=new SourceBrushSide[in.length/structLength];
		byte[] bytes=new byte[structLength];
		for(int i=0;i<elements.length;i++) {
			for(int j=0;j<structLength;j++) {
				bytes[j]=in[offset+j];
			}
			elements[i]=new SourceBrushSide(bytes);
			offset+=structLength;
		}
	}
	
	// METHODS
	
	// I coded this to test whether the conversion from little-endian unsigned short to Java's  big-endian signed int worked properly.
	public void printPlaneRefs() {
		for(int i=0;i<elements.length;i++) {
			Window.println(elements[i].getPlane()+"",0);
		}
	}
	
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
	
	public SourceBrushSide getElement(int i) {
		return elements[i];
	}
	
	public SourceBrushSide[] getElements() {
		return elements;
	}
}