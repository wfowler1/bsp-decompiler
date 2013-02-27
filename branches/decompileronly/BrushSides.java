// BrushSides class

// Maintains an array of BrushSides.

import java.io.FileInputStream;
import java.io.File;

public class BrushSides {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	private File data;
	private int length;
	private BrushSide[] elements;
	
	private int structLength;

	// CONSTRUCTORS
	
	// Accepts a filepath as a String
	public BrushSides(String in, int type, boolean isVindictus) throws java.lang.InterruptedException {
		new BrushSides(new File(in), type, isVindictus);
	}
	
	// This one accepts the input file path as a File
	public BrushSides(File in, int type, boolean isVindictus) throws java.lang.InterruptedException {
		data=in;
		try {
			FileInputStream fileReader=new FileInputStream(data);
			byte[] temp=new byte[(int)data.length()];
			fileReader.read(temp);
			new BrushSides(temp, type, isVindictus);
			fileReader.close();
		} catch(java.io.FileNotFoundException e) {
			Window.println("ERROR: File "+data.getPath()+" not found!",Window.VERBOSITY_ALWAYS);
		} catch(java.io.IOException e) {
			Window.println("ERROR: File "+data.getPath()+" could not be read, ensure the file is not open in another program",Window.VERBOSITY_ALWAYS);
		}
	}
	
	// Takes a byte array, as if read from a FileInputStream
	public BrushSides(byte[] in, int type, boolean isVindictus) throws java.lang.InterruptedException {
		switch(type) {
			case BSP.TYPE_QUAKE2:
			case BSP.TYPE_DAIKATANA:
			case BSP.TYPE_SOF:
				structLength=4;
				break;
			case BSP.TYPE_COD:
			case BSP.TYPE_COD2:
			case BSP.TYPE_COD4:
			case BSP.TYPE_SIN:
			case BSP.TYPE_NIGHTFIRE:
			case BSP.TYPE_QUAKE3:
			case BSP.TYPE_STEF2:
			case BSP.TYPE_STEF2DEMO:
			case BSP.TYPE_SOURCE17:
			case BSP.TYPE_SOURCE18:
			case BSP.TYPE_SOURCE19:
			case BSP.TYPE_SOURCE20:
			case BSP.TYPE_SOURCE21:
			case BSP.TYPE_SOURCE22:
			case BSP.TYPE_SOURCE23:
			case BSP.TYPE_FAKK:
				structLength=8;
				break;
			case BSP.TYPE_MOHAA:
			case BSP.TYPE_RAVEN:
				structLength=12;
				break;
			default:
				structLength=0; // This will cause the shit to hit the fan.
		}
		if(isVindictus) {
			structLength=16;
		}
		int offset=0;
		length=in.length;
		elements=new BrushSide[in.length/structLength];
		byte[] bytes=new byte[structLength];
		for(int i=0;i<elements.length;i++) {
			if(Thread.currentThread().interrupted()) {
				throw new java.lang.InterruptedException("while populating Brush Side array");
			}
			for(int j=0;j<structLength;j++) {
				bytes[j]=in[offset+j];
			}
			elements[i]=new BrushSide(bytes, type, isVindictus);
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
	
	public BrushSide getElement(int i) {
		return elements[i];
	}
	
	public BrushSide[] getElements() {
		return elements;
	}
}