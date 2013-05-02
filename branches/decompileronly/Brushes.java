// Brushes class

// Maintains an array of Brushes.

import java.io.FileInputStream;
import java.io.File;

public class Brushes {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	private File data;
	private int length;
	private Brush[] elements;
	
	private int structLength;

	// CONSTRUCTORS
	
	// Accepts a filepath as a String
	public Brushes(String in, int type) throws java.lang.InterruptedException {
		new Brushes(new File(in), type);
	}
	
	// This one accepts the input file path as a File
	public Brushes(File in, int type) throws java.lang.InterruptedException {
		data=in;
		try {
			FileInputStream fileReader=new FileInputStream(data);
			byte[] temp=new byte[(int)data.length()];
			fileReader.read(temp);
			new Brushes(temp, type);
			fileReader.close();
		} catch(java.io.FileNotFoundException e) {
			Window.println("ERROR: File "+data.getPath()+" not found!",Window.VERBOSITY_ALWAYS);
		} catch(java.io.IOException e) {
			Window.println("ERROR: File "+data.getPath()+" could not be read, ensure the file is not open in another program",Window.VERBOSITY_ALWAYS);
		}
	}
	
	// Takes a byte array, as if read from a FileInputStream
	public Brushes(byte[] in, int type) throws java.lang.InterruptedException {
		switch(type) {
			case BSP.TYPE_QUAKE2:
			case BSP.TYPE_DAIKATANA:
			case BSP.TYPE_SIN:
			case BSP.TYPE_SOF:
			case BSP.TYPE_SOURCE17:
			case BSP.TYPE_SOURCE18:
			case BSP.TYPE_SOURCE19:
			case BSP.TYPE_SOURCE20:
			case BSP.TYPE_SOURCE21:
			case BSP.TYPE_SOURCE22:
			case BSP.TYPE_SOURCE23:
			case BSP.TYPE_TACTICALINTERVENTION:
			case BSP.TYPE_VINDICTUS:
			case BSP.TYPE_DMOMAM:
			case BSP.TYPE_NIGHTFIRE:
			case BSP.TYPE_STEF2:
			case BSP.TYPE_MOHAA:
			case BSP.TYPE_STEF2DEMO:
			case BSP.TYPE_RAVEN:
			case BSP.TYPE_QUAKE3:
			case BSP.TYPE_FAKK:
				structLength=12;
				break;
			case BSP.TYPE_COD:
			case BSP.TYPE_COD2:
			case BSP.TYPE_COD4:
				structLength=4;
				break;
			default:
				structLength=0; // This will cause the shit to hit the fan.
		}
		int offset=0;
		length=in.length;
		elements=new Brush[in.length/structLength];
		byte[] bytes=new byte[structLength];
		for(int i=0;i<elements.length;i++) {
			if(Thread.currentThread().interrupted()) {
				throw new java.lang.InterruptedException("while populating Brush array");
			}
			for(int j=0;j<structLength;j++) {
				bytes[j]=in[offset+j];
			}
			elements[i]=new Brush(bytes, type);
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
	
	public Brush getElement(int i) {
		return elements[i];
	}
	
	public Brush[] getElements() {
		return elements;
	}
}