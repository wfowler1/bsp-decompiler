// Lump11 class

// This class keeps and maintains an array, which is a list
// of the leaves in the map.

import java.io.FileInputStream;
import java.io.File;

public class Lump11 {

	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	private File data;
	private int numLeaves=0;
	private int numWorldLeaves=0;
	private int numModelLeaves=numLeaves-numWorldLeaves;
	private Leaf[] leaves;
	
	// CONSTRUCTORS
	
	// This one accepts the lump path as a String
	public Lump11(String in) {
		data=new File(in);
		try {
			numLeaves=getNumElements();
			leaves=new Leaf[numLeaves];
			populateLeafList();
		} catch(java.io.FileNotFoundException e) {
			System.out.println("ERROR: File "+data+" not found!");
		} catch(java.io.IOException e) {
			System.out.println("ERROR: File "+data+" could not be read, ensure the file is not open in another program");
		}
	}
	
	// This one accepts the input file path as a File
	public Lump11(File in) {
		data=in;
		try {
			numLeaves=getNumElements();
			leaves=new Leaf[numLeaves];
			populateLeafList();
		} catch(java.io.FileNotFoundException e) {
			System.out.println("ERROR: File "+data+" not found!");
		} catch(java.io.IOException e) {
			System.out.println("ERROR: File "+data+" could not be read, ensure the file is not open in another program");
		}
	}
	
	public Lump11(Leaf[] in) {
		leaves=in;
		numLeaves=leaves.length;
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
			System.out.println("WARNING: funny lump size in "+data+", ignoring last leaf.");
		}
	}
		
	// ACCESSORS/MUTATORS
	
	// Returns the length (in bytes) of the lump
	public long getLength() {
		return data.length();
	}
	
	// Returns the number of Leaves.
	public int getNumElements() {
		if(numLeaves==0) {
			return (int)data.length()/48;
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
