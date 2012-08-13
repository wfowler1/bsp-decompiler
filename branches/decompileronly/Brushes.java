// Brushes class

// This class holds an array of Brush objects.

import java.io.FileInputStream;
import java.io.File;

public class Brushes {

	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	private File data;
	private int length;
	private int numBrshs=0;
	private Brush[] brushes;
	
	// CONSTRUCTORS
	
	// This one accepts the lump path as a String
	public Brushes(String in) {
		data=new File(in);
		length=(int)data.length();
		try {
			numBrshs=length();
			brushes=new Brush[numBrshs];
			populateBrushList();
		} catch(java.io.FileNotFoundException e) {
			Window.println("ERROR: File "+data.getPath()+" not found!",0);
		} catch(java.io.IOException e) {
			Window.println("ERROR: File "+data.getPath()+" could not be read, ensure the file is not open in another program",0);
		}
	}
	
	// This one accepts the input file path as a File
	public Brushes(File in) {
		data=in;
		length=(int)data.length();
		try {
			numBrshs=length();
			brushes=new Brush[numBrshs];
			populateBrushList();
		} catch(java.io.FileNotFoundException e) {
			Window.println("ERROR: File "+data.getPath()+" not found!",0);
		} catch(java.io.IOException e) {
			Window.println("ERROR: File "+data.getPath()+" could not be read, ensure the file is not open in another program",0);
		}
	}
	
	public Brushes(byte[] in) {
		int offset=0;
		numBrshs=in.length/12;
		length=in.length;
		brushes=new Brush[numBrshs];
		for(int i=0;i<numBrshs;i++) {
			byte[] brushBytes=new byte[12];
			for(int j=0;j<12;j++) {
				brushBytes[j]=in[offset+j];
			}
			brushes[i]=new Brush(brushBytes);
			offset+=12;
		}
	}
	
	// METHODS
	
	// -populateBrushList()
	// Uses the data file in the instance data to populate the array
	// of Brush objects
	private void populateBrushList() throws java.io.FileNotFoundException, java.io.IOException {
		FileInputStream reader=new FileInputStream(data);
		for(int i=0;i<numBrshs;i++) {
			byte[] datain=new byte[12];
			reader.read(datain);
			brushes[i]=new Brush(datain);
		}
		reader.close();
	}
	
	// ACCESSORS/MUTATORS
	
	// Returns the length (in bytes) of the lump
	public int getLength() {
		return length;
	}
	
	// Returns the number of brushes.
	public int length() {
		if(numBrshs==0) {
			return length/12;
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