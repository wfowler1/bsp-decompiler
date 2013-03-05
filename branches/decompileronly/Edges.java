// Edges class

// Maintains an array of Edges.

import java.io.FileInputStream;
import java.io.File;

public class Edges {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	private File data;
	private int length;
	private Edge[] elements;
	
	private int structLength=4;

	// CONSTRUCTORS
	
	// Accepts a filepath as a String
	public Edges(String in, int type) throws java.lang.InterruptedException {
		new Edges(new File(in), type);
	}
	
	// This one accepts the input file path as a File
	public Edges(File in, int type) throws java.lang.InterruptedException {
		data=in;
		try {
			FileInputStream fileReader=new FileInputStream(data);
			byte[] temp=new byte[(int)data.length()];
			fileReader.read(temp);
			new Edges(temp, type);
			fileReader.close();
		} catch(java.io.FileNotFoundException e) {
			Window.println("ERROR: File "+data.getPath()+" not found!",Window.VERBOSITY_ALWAYS);
		} catch(java.io.IOException e) {
			Window.println("ERROR: File "+data.getPath()+" could not be read, ensure the file is not open in another program",Window.VERBOSITY_ALWAYS);
		}
	}
	
	// Takes a byte array, as if read from a FileInputStream
	public Edges(byte[] in, int type) throws java.lang.InterruptedException {
		switch(type) {
			case BSP.TYPE_QUAKE:
			case BSP.TYPE_SIN:
			case BSP.TYPE_DAIKATANA:
			case BSP.TYPE_SOURCE17:
			case BSP.TYPE_SOURCE18:
			case BSP.TYPE_SOURCE19:
			case BSP.TYPE_SOURCE20:
			case BSP.TYPE_SOURCE21:
			case BSP.TYPE_SOURCE22:
			case BSP.TYPE_SOURCE23:
			case BSP.TYPE_DMOMAM:
			case BSP.TYPE_QUAKE2:
			case BSP.TYPE_SOF:
				structLength=4;
				break;
			case BSP.TYPE_VINDICTUS:
				structLength=8;
				break;
		}
		int offset=0;
		length=in.length;
		elements=new Edge[in.length/structLength];
		byte[] bytes=new byte[structLength];
		for(int i=0;i<elements.length;i++) {
			if(Thread.currentThread().interrupted()) {
				throw new java.lang.InterruptedException("while populating Edge array");
			}
			for(int j=0;j<structLength;j++) {
				bytes[j]=in[offset+j];
			}
			elements[i]=new Edge(bytes, type);
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
	
	public Edge getElement(int i) {
		return elements[i];
	}
	
	public Edge[] getElements() {
		return elements;
	}
}