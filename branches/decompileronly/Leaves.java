// Leaves class

// This class keeps and maintains an array, which is a list
// of the leaves in the map.

import java.io.FileInputStream;
import java.io.File;

public class Leaves {

	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	private File data;
	private int numLeaves=0;
	private int length;
	private int numWorldLeaves=0;
	private int numModelLeaves=numLeaves-numWorldLeaves;
	private Leaf[] leaves;
	
	// CONSTRUCTORS
	
	// This one accepts the lump path as a String
	public Leaves(String in) {
		data=new File(in);
		length=(int)data.length();
		try {
			numLeaves=getNumElements();
			leaves=new Leaf[numLeaves];
			populateLeafList();
		} catch(java.io.FileNotFoundException e) {
			Window.window.println("ERROR: File "+data+" not found!");
		} catch(java.io.IOException e) {
			Window.window.println("ERROR: File "+data+" could not be read, ensure the file is not open in another program");
		}
	}
	
	// This one accepts the input file path as a File
	public Leaves(File in) {
		data=in;
		length=(int)data.length();
		try {
			numLeaves=getNumElements();
			leaves=new Leaf[numLeaves];
			populateLeafList();
		} catch(java.io.FileNotFoundException e) {
			Window.window.println("ERROR: File "+data+" not found!");
		} catch(java.io.IOException e) {
			Window.window.println("ERROR: File "+data+" could not be read, ensure the file is not open in another program");
		}
	}
	
	public Leaves(Leaf[] in) {
		leaves=in;
		numLeaves=leaves.length;
	}
	
	public Leaves(byte[] in) {
		int offset=0;
		length=in.length;
		numLeaves=in.length/48;
		leaves=new Leaf[numLeaves];
		try {
			for(int i=0;i<numLeaves;i++) {
				byte[] leafBytes=new byte[48];
				for(int j=0;j<48;j++) {
					leafBytes[j]=in[offset+j];
				}
				leaves[i]=new Leaf(leafBytes);
				offset+=48;
			}
		} catch(InvalidLeafException e) {
			Window.window.println("WARNING: Funny lump size in "+data+", ignoring last leaf.");
		}
	}
	
	// METHODS
	
	// -populateLeafList()
	// Uses the instance data to populate the array of Leaf
	private void populateLeafList() throws java.io.FileNotFoundException, java.io.IOException {
		FileInputStream reader=new FileInputStream(data);
		try {
			for(int i=0;i<numLeaves;i++) {
				byte[] datain=new byte[48];
				reader.read(datain);
				leaves[i]=new Leaf(datain);
			}
			reader.close();
		} catch(InvalidLeafException e) {
			Window.window.println("WARNING: funny lump size in "+data+", ignoring last leaf.");
		}
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
	
	public Leaf getLeaf(int i) {
		return leaves[i];
	}
	
	public Leaf[] getLeaves() {
		return leaves;
	}
}
