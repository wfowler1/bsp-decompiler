// Leaves class

// Maintains an array of Leaves.

import java.io.FileInputStream;
import java.io.File;

public class Leaves {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	private File data;
	private int length;
	private Leaf[] elements;
	
	private int structLength;

	// CONSTRUCTORS
	
	// Accepts a filepath as a String
	public Leaves(String in, int type) {
		new Leaves(new File(in), type);
	}
	
	// This one accepts the input file path as a File
	public Leaves(File in, int type) {
		data=in;
		try {
			FileInputStream fileReader=new FileInputStream(data);
			byte[] temp=new byte[(int)data.length()];
			fileReader.read(temp);
			new Leaves(temp, type);
			fileReader.close();
		} catch(java.io.FileNotFoundException e) {
			Window.println("ERROR: File "+data.getPath()+" not found!",Window.VERBOSITY_ALWAYS);
		} catch(java.io.IOException e) {
			Window.println("ERROR: File "+data.getPath()+" could not be read, ensure the file is not open in another program",Window.VERBOSITY_ALWAYS);
		}
	}
	
	// Takes a byte array, as if read from a FileInputStream
	public Leaves(byte[] in, int type) {
		switch(type) {
			case Leaf.TYPE_QUAKE:
			case Leaf.TYPE_QUAKE2:
				structLength=28;
				break;
			case Leaf.TYPE_NIGHTFIRE:
				structLength=48;
				break;
			case Leaf.TYPE_SOURCE19:
				structLength=56;
				break;
			case Leaf.TYPE_SOURCE20:
				structLength=32;
				break;
			default:
				structLength=0; // This will cause the shit to hit the fan.
		}
		int offset=0;
		length=in.length;
		elements=new Leaf[in.length/structLength];
		byte[] bytes=new byte[structLength];
		for(int i=0;i<elements.length;i++) {
			for(int j=0;j<structLength;j++) {
				bytes[j]=in[offset+j];
			}
			elements[i]=new Leaf(bytes, type);
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
	
	public Leaf getElement(int i) {
		return elements[i];
	}
	
	public Leaf[] getElements() {
		return elements;
	}
}