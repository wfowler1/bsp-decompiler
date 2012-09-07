// SourceLeaves class

// Contains all information for leaves for a BSPv38

import java.io.FileInputStream;
import java.io.File;

public class SourceLeaves {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	private File data;
	private int numElems=0;
	private int length;
	private SourceLeaf[] elements;
	
	private int version;
	
	private int structLength=32;

	// CONSTRUCTORS
	
	public SourceLeaves(String in, int version) {
		this.version=version;
		if(version==18 || version==19) {
			structLength=56;
		} else {
			structLength=32;
		}
		data=new File(in);
		length=(int)data.length();
		try {
			numElems=length();
			elements=new SourceLeaf[numElems];
			populateList();
		} catch(java.io.FileNotFoundException e) {
			Window.println("ERROR: File "+data.getPath()+" not found!",0);
		} catch(java.io.IOException e) {
			Window.println("ERROR: File "+data.getPath()+" could not be read, ensure the file is not open in another program",0);
		}
	}
	
	// This one accepts the input file path as a File
	public SourceLeaves(File in, int version) {
		this.version=version;
		if(version==18 || version==19) {
			structLength=56;
		} else {
			structLength=32;
		}
		data=in;
		length=(int)data.length();
		try {
			numElems=length();
			elements=new SourceLeaf[numElems];
			populateList();
		} catch(java.io.FileNotFoundException e) {
			Window.println("ERROR: File "+data.getPath()+" not found!",0);
		} catch(java.io.IOException e) {
			Window.println("ERROR: File "+data.getPath()+" could not be read, ensure the file is not open in another program",0);
		}
	}
	
	public SourceLeaves(byte[] in, int version) {
		this.version=version;
		if(version==18 || version==19) {
			structLength=56;
		} else {
			structLength=32;
		}
		int offset=0;
		length=in.length;
		numElems=in.length/structLength;
		elements=new SourceLeaf[numElems];
		for(int i=0;i<numElems;i++) {
			byte[] bytes=new byte[structLength];
			for(int j=0;j<structLength;j++) {
				bytes[j]=in[offset+j];
			}
			elements[i]=new SourceLeaf(bytes,version);
			offset+=structLength;
		}
	}
	
	// METHODS
	
	// -populateList()
	// Uses the instance data to populate the array of SourceLeaf
	private void populateList() throws java.io.FileNotFoundException, java.io.IOException {
		FileInputStream reader=new FileInputStream(data);
		for(int i=0;i<numElems;i++) {
			byte[] datain=new byte[structLength];
			reader.read(datain);
			elements[i]=new SourceLeaf(datain,version);
		}
		reader.close();
	}
	
	// ACCESSORS/MUTATORS
	
	// Returns the length (in bytes) of the lump
	public int getLength() {
		return length;
	}
	
	// Returns the number of Leaves.
	public int length() {
		if(numElems==0) {
			return length/structLength;
		} else {
			return numElems;
		}
	}
	
	public SourceLeaf getLeaf(int i) {
		return elements[i];
	}
	
	public SourceLeaf[] getLeaves() {
		return elements;
	}
}