// SourceDispInfos class

// Maintains and array of SourceDispInfo for a Source engine BSP.

import java.io.FileInputStream;
import java.io.File;

public class SourceDispInfos {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	private File data;
	private int length;
	private SourceDispInfo[] elements;
	
	public int structLength=0;

	// CONSTRUCTORS
	
	// Accepts a filepath as a String
	public SourceDispInfos(String in, int type) throws java.lang.InterruptedException {
		new SourceDispInfos(new File(in), type);
	}
	
	// This one accepts the input file path as a File
	public SourceDispInfos(File in, int type) throws java.lang.InterruptedException {
		data=in;
		try {
			FileInputStream fileReader=new FileInputStream(data);
			byte[] temp=new byte[(int)data.length()];
			fileReader.read(temp);
			new SourceDispInfos(temp, type);
			fileReader.close();
		} catch(java.io.FileNotFoundException e) {
			Window.println("ERROR: File "+data.getPath()+" not found!",Window.VERBOSITY_ALWAYS);
		} catch(java.io.IOException e) {
			Window.println("ERROR: File "+data.getPath()+" could not be read, ensure the file is not open in another program",Window.VERBOSITY_ALWAYS);
		}
	}
	
	// Takes a byte array, as if read from a FileInputStream
	public SourceDispInfos(byte[] in, int type) throws java.lang.InterruptedException {
		switch(type) {
			case BSP.TYPE_SOURCE17:
			case BSP.TYPE_SOURCE18:
			case BSP.TYPE_SOURCE19:
			case BSP.TYPE_SOURCE20:
			case BSP.TYPE_SOURCE21:
			case BSP.TYPE_DMOMAM:
				structLength=176;
				break;
			case BSP.TYPE_SOURCE22:
				structLength=180;
				break;
			case BSP.TYPE_SOURCE23:
				structLength=184;
				break;
			case BSP.TYPE_VINDICTUS:
				structLength=232;
				break;
			default:
				structLength=0; // This will cause the shit to hit the fan.
		}
		int offset=0;
		length=in.length;
		elements=new SourceDispInfo[in.length/structLength];
		byte[] bytes=new byte[structLength];
		for(int i=0;i<elements.length;i++) {
			if(Thread.currentThread().interrupted()) {
				throw new java.lang.InterruptedException("while populating Displacement Info array");
			}
			for(int j=0;j<structLength;j++) {
				bytes[j]=in[offset+j];
			}
			elements[i]=new SourceDispInfo(bytes, type);
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
	
	public SourceDispInfo getElement(int i) {
		return elements[i];
	}
	
	public SourceDispInfo[] getElements() {
		return elements;
	}
}