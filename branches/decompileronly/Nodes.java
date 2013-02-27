// Nodes class

// Maintains an array of Nodes.

import java.io.FileInputStream;
import java.io.File;

public class Nodes {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	private File data;
	private int length;
	private Node[] elements;
	
	private int structLength;

	// CONSTRUCTORS
	
	// Accepts a filepath as a String
	public Nodes(String in, int type, boolean isVindictus) throws java.lang.InterruptedException {
		new Nodes(new File(in), type, isVindictus);
	}
	
	// This one accepts the input file path as a File
	public Nodes(File in, int type, boolean isVindictus) throws java.lang.InterruptedException {
		data=in;
		try {
			FileInputStream fileReader=new FileInputStream(data);
			byte[] temp=new byte[(int)data.length()];
			fileReader.read(temp);
			new Nodes(temp, type, isVindictus);
			fileReader.close();
		} catch(java.io.FileNotFoundException e) {
			Window.println("ERROR: File "+data.getPath()+" not found!",Window.VERBOSITY_ALWAYS);
		} catch(java.io.IOException e) {
			Window.println("ERROR: File "+data.getPath()+" could not be read, ensure the file is not open in another program",Window.VERBOSITY_ALWAYS);
		}
	}
	
	// Takes a byte array, as if read from a FileInputStream
	public Nodes(byte[] in, int type, boolean isVindictus) throws java.lang.InterruptedException {
		switch(type) {
			case BSP.TYPE_QUAKE:
				structLength=24;
				break;
			case BSP.TYPE_QUAKE2:
			case BSP.TYPE_SIN:
			case BSP.TYPE_SOF:
			case BSP.TYPE_DAIKATANA:
				structLength=28;
				break;
			case BSP.TYPE_SOURCE17:
			case BSP.TYPE_SOURCE18:
			case BSP.TYPE_SOURCE19:
			case BSP.TYPE_SOURCE20:
			case BSP.TYPE_SOURCE21:
			case BSP.TYPE_SOURCE22:
			case BSP.TYPE_SOURCE23:
				structLength=32;
				break;
			case BSP.TYPE_QUAKE3:
			case BSP.TYPE_FAKK:
			case BSP.TYPE_COD:
			case BSP.TYPE_STEF2:
			case BSP.TYPE_STEF2DEMO:
			case BSP.TYPE_MOHAA:
			case BSP.TYPE_RAVEN:
			case BSP.TYPE_NIGHTFIRE:
				structLength=36;
				break;
			default:
				structLength=0; // This will cause the shit to hit the fan.
		}
		if(isVindictus) {
			structLength=48;
		}
		int offset=0;
		length=in.length;
		elements=new Node[in.length/structLength];
		byte[] bytes=new byte[structLength];
		for(int i=0;i<elements.length;i++) {
			if(Thread.currentThread().interrupted()) {
				throw new java.lang.InterruptedException("while populating Node array");
			}
			for(int j=0;j<structLength;j++) {
				bytes[j]=in[offset+j];
			}
			elements[i]=new Node(bytes, type, isVindictus);
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
	
	public Node getElement(int i) {
		return elements[i];
	}
	
	public Node[] getElements() {
		return elements;
	}
}