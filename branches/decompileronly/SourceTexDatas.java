// SourceTexDatas class

// Maintains and array of SourceTexData for a Source engine BSP.

import java.io.FileInputStream;
import java.io.File;

public class SourceTexDatas {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	private File data;
	private int length;
	private SourceTexData[] elements;
	
	public static final int structLength=32;

	// CONSTRUCTORS
	
	public SourceTexDatas(String in) {
		new SourceTexDatas(new File(in));
	}
	
	// This one accepts the input file path as a File
	public SourceTexDatas(File in) {
		data=in;
		length=(int)data.length();
		try {
			elements=new SourceTexData[length/structLength];
			populateList();
		} catch(java.io.FileNotFoundException e) {
			Window.println("ERROR: File "+data.getPath()+" not found!",0);
		} catch(java.io.IOException e) {
			Window.println("ERROR: File "+data.getPath()+" could not be read, ensure the file is not open in another program",0);
		}
	}
	
	public SourceTexDatas(byte[] in) {
		int offset=0;
		length=in.length;
		elements=new SourceTexData[in.length/structLength];
		byte[] bytes=new byte[structLength];
		for(int i=0;i<elements.length;i++) {
			for(int j=0;j<structLength;j++) {
				bytes[j]=in[offset+j];
			}
			elements[i]=new SourceTexData(bytes);
			offset+=structLength;
		}
	}
	
	// METHODS
	
	// -populateList()
	// Uses the instance data to populate the array
	private void populateList() throws java.io.FileNotFoundException, java.io.IOException {
		FileInputStream reader=new FileInputStream(data);
		byte[] datain=new byte[structLength];
		for(int i=0;i<elements.length;i++) {
			reader.read(datain);
			elements[i]=new SourceTexData(datain);
		}
		reader.close();
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
	
	public SourceTexData getElement(int i) {
		return elements[i];
	}
	
	public SourceTexData[] getElements() {
		return elements;
	}
}