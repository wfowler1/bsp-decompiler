// Faces class

// Maintains an array of Faces.

import java.io.FileInputStream;
import java.io.File;

public class Faces {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	private File data;
	private int length;
	private Face[] elements;
	
	private int structLength;

	// CONSTRUCTORS
	
	// Accepts a filepath as a String
	public Faces(String in, int type, boolean isVindictus) throws java.lang.InterruptedException {
		new Faces(new File(in), type, isVindictus);
	}
	
	// This one accepts the input file path as a File
	public Faces(File in, int type, boolean isVindictus) throws java.lang.InterruptedException {
		data=in;
		try {
			FileInputStream fileReader=new FileInputStream(data);
			byte[] temp=new byte[(int)data.length()];
			fileReader.read(temp);
			new Faces(temp, type, isVindictus);
			fileReader.close();
		} catch(java.io.FileNotFoundException e) {
			Window.println("ERROR: File "+data.getPath()+" not found!",Window.VERBOSITY_ALWAYS);
		} catch(java.io.IOException e) {
			Window.println("ERROR: File "+data.getPath()+" could not be read, ensure the file is not open in another program",Window.VERBOSITY_ALWAYS);
		}
	}
	
	// Takes a byte array, as if read from a FileInputStream
	public Faces(byte[] in, int type, boolean isVindictus) throws java.lang.InterruptedException {
		switch(type) {
			case BSP.TYPE_QUAKE:
			case BSP.TYPE_QUAKE2:
			case BSP.TYPE_DAIKATANA:
				structLength=20;
				break;
			case BSP.TYPE_SIN:
				structLength=36;
				break;
			case BSP.TYPE_SOF:
				structLength=40;
				break;
			case BSP.TYPE_NIGHTFIRE:
				structLength=48;
				break;
			case BSP.TYPE_SOURCE17:
			case BSP.TYPE_SOURCE18:
			case BSP.TYPE_SOURCE19:
			case BSP.TYPE_SOURCE20:
			case BSP.TYPE_SOURCE21:
			case BSP.TYPE_SOURCE22:
			case BSP.TYPE_SOURCE23:
				structLength=56;
				break;
			case BSP.TYPE_QUAKE3:
				structLength=104;
				break;
			case BSP.TYPE_MOHAA:
				structLength=108;
				break;
			case BSP.TYPE_STEF2:
			case BSP.TYPE_STEF2DEMO:
				structLength=132;
				break;
			case BSP.TYPE_RAVEN:
				structLength=148;
				break;
			default:
				structLength=0; // This will cause the shit to hit the fan.
		}
		if(isVindictus) {
			structLength=72;
		}
		int offset=0;
		length=in.length;
		elements=new Face[in.length/structLength];
		byte[] bytes=new byte[structLength];
		for(int i=0;i<elements.length;i++) {
			if(Thread.currentThread().interrupted()) {
				throw new java.lang.InterruptedException("while populating Face array");
			}
			for(int j=0;j<structLength;j++) {
				bytes[j]=in[offset+j];
			}
			elements[i]=new Face(bytes, type, isVindictus);
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
	
	public Face getElement(int i) {
		return elements[i];
	}
	
	public Face[] getElements() {
		return elements;
	}
}