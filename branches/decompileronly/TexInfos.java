// TexInfos class

// Maintains an array of TexInfos.

import java.io.FileInputStream;
import java.io.File;

public class TexInfos {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	private File data;
	private int length;
	private TexInfo[] elements;
	
	private int structLength;

	// CONSTRUCTORS
	
	// Accepts a filepath as a String
	public TexInfos(String in, int type) throws java.lang.InterruptedException {
		new TexInfos(new File(in), type);
	}
	
	// This one accepts the input file path as a File
	public TexInfos(File in, int type) throws java.lang.InterruptedException {
		data=in;
		try {
			FileInputStream fileReader=new FileInputStream(data);
			byte[] temp=new byte[(int)data.length()];
			fileReader.read(temp);
			new TexInfos(temp, type);
			fileReader.close();
		} catch(java.io.FileNotFoundException e) {
			Window.println("ERROR: File "+data.getPath()+" not found!",Window.VERBOSITY_ALWAYS);
		} catch(java.io.IOException e) {
			Window.println("ERROR: File "+data.getPath()+" could not be read, ensure the file is not open in another program",Window.VERBOSITY_ALWAYS);
		}
	}
	
	// Takes a byte array, as if read from a FileInputStream
	public TexInfos(byte[] in, int type) throws java.lang.InterruptedException {
		switch(type) {
			case BSP.TYPE_NIGHTFIRE:
				structLength=32;
				break;
			case BSP.TYPE_QUAKE:
				structLength=40;
				break;
			case BSP.TYPE_SOURCE17:
			case BSP.TYPE_SOURCE18:
			case BSP.TYPE_SOURCE19:
			case BSP.TYPE_SOURCE20:
			case BSP.TYPE_SOURCE21:
			case BSP.TYPE_SOURCE22:
			case BSP.TYPE_SOURCE23:
			case BSP.TYPE_VINDICTUS:
				structLength=72;
				break;
			case BSP.TYPE_DMOMAM:
				structLength=96;
				break;
			default:
				structLength=0; // This will break it.
		}
		int offset=0;
		length=in.length;
		elements=new TexInfo[in.length/structLength];
		byte[] bytes=new byte[structLength];
		for(int i=0;i<elements.length;i++) {
			if(Thread.currentThread().interrupted()) {
				throw new java.lang.InterruptedException("while populating TexInfo array");
			}
			for(int j=0;j<structLength;j++) {
				bytes[j]=in[offset+j];
			}
			elements[i]=new TexInfo(bytes, type);
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
	
	public TexInfo getElement(int i) {
		return elements[i];
	}
	
	public TexInfo[] getElements() {
		return elements;
	}
}