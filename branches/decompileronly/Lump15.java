// Lump15 class

// This class holds an array of Brush objects.

import java.io.FileInputStream;
import java.io.File;

public class Lump15 {

	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	private File data;
	private int numBrshs=0;
	private Brush[] brushes;
	
	// CONSTRUCTORS
	
	// This one accepts the lump path as a String
	public Lump15(String in) {
		data=new File(in);
		try {
			numBrshs=getNumElements();
			brushes=new Brush[numBrshs];
			populateBrushList();
		} catch(java.io.FileNotFoundException e) {
			System.out.println("ERROR: File "+data+" not found!");
		} catch(java.io.IOException e) {
			System.out.println("ERROR: File "+data+" could not be read, ensure the file is not open in another program");
		}
	}
	
	// This one accepts the input file path as a File
	public Lump15(File in) {
		data=in;
		try {
			numBrshs=getNumElements();
			brushes=new Brush[numBrshs];
			populateBrushList();
		} catch(java.io.FileNotFoundException e) {
			System.out.println("ERROR: File "+data+" not found!");
		} catch(java.io.IOException e) {
			System.out.println("ERROR: File "+data+" could not be read, ensure the file is not open in another program");
		}
	}
	
	// METHODS
	
	// -populateBrushList()
	// Uses the data file in the instance data to populate the array
	// of Brush objects
	private void populateBrushList() throws java.io.FileNotFoundException, java.io.IOException {
		FileInputStream reader=new FileInputStream(data);
		try {
			for(int i=0;i<numBrshs;i++) {
				byte[] datain=new byte[12];
				reader.read(datain);
				brushes[i]=new Brush(datain);
			}
			reader.close();
		} catch(InvalidBrushException e) {
			System.out.println("WARNING: Funny lump size in "+data+", ignoring last brush.");
		}
	}
	
	// ACCESSORS/MUTATORS
	
	// Returns the length (in bytes) of the lump
	public long getLength() {
		return data.length();
	}
	
	// Returns the number of brushes.
	public int getNumElements() {
		if(numBrshs==0) {
			return (int)data.length()/12;
		} else {
			return numBrshs;
		}
	}
	
	public Brush getBrush(int i) {
		return brushes[i];
	}
	
	public Brush[] getBrushes() {
		return brushes;
	}
	
	public void setBrush(int i, Brush in) {
		brushes[i]=in;
	}
}