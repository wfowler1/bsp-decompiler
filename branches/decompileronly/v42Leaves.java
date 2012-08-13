// v42Leaves class

// This class keeps and maintains an array, which is a list
// of the leaves in the map.

import java.io.FileInputStream;
import java.io.File;

public class v42Leaves {

	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	private File data;
	private int numLeaves=0;
	private int length;
	private int numWorldLeaves=0;
	private int numModelLeaves=numLeaves-numWorldLeaves;
	private v42Leaf[] leaves;
	
	// CONSTRUCTORS
	
	// This one accepts the lump path as a String
	public v42Leaves(String in) {
		data=new File(in);
		length=(int)data.length();
		try {
			numLeaves=getNumElements();
			leaves=new v42Leaf[numLeaves];
			populateLeafList();
		} catch(java.io.FileNotFoundException e) {
			Window.println("ERROR: File "+data.getPath()+" not found!",0);
		} catch(java.io.IOException e) {
			Window.println("ERROR: File "+data.getPath()+" could not be read, ensure the file is not open in another program",0);
		}
	}
	
	// This one accepts the input file path as a File
	public v42Leaves(File in) {
		data=in;
		length=(int)data.length();
		try {
			numLeaves=getNumElements();
			leaves=new v42Leaf[numLeaves];
			populateLeafList();
		} catch(java.io.FileNotFoundException e) {
			Window.println("ERROR: File "+data.getPath()+" not found!",0);
		} catch(java.io.IOException e) {
			Window.println("ERROR: File "+data.getPath()+" could not be read, ensure the file is not open in another program",0);
		}
	}
	
	public v42Leaves(v42Leaf[] in) {
		leaves=in;
		numLeaves=leaves.length;
	}
	
	public v42Leaves(byte[] in) {
		int offset=0;
		length=in.length;
		numLeaves=in.length/48;
		leaves=new v42Leaf[numLeaves];
		for(int i=0;i<numLeaves;i++) {
			byte[] leafBytes=new byte[48];
			for(int j=0;j<48;j++) {
				leafBytes[j]=in[offset+j];
			}
			leaves[i]=new v42Leaf(leafBytes);
			offset+=48;
		}
	}
	
	// METHODS
	
	// -populateLeafList()
	// Uses the instance data to populate the array of Leaf
	private void populateLeafList() throws java.io.FileNotFoundException, java.io.IOException {
		FileInputStream reader=new FileInputStream(data);
		for(int i=0;i<numLeaves;i++) {
			byte[] datain=new byte[48];
			reader.read(datain);
			leaves[i]=new v42Leaf(datain);
		}
		reader.close();
	}
		
	// ACCESSORS/MUTATORS
	
	// Returns the length (in bytes) of the lump
	public int getLength() {
		return length;
	}
	
	// Returns the number of Leaves.
	public int getNumElements() {
		if(numLeaves==0) {
			return length/48;
		} else {
			return numLeaves;
		}
	}
	
	public v42Leaf getLeaf(int i) {
		return leaves[i];
	}
	
	public v42Leaf[] getLeaves() {
		return leaves;
	}
}
