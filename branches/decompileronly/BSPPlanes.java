// BSPPlanes class

// This class holds references to an array of BSPPlane classes which
// hold the data for all the planes.

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.File;

public class BSPPlanes {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	private File data;
	private int length;
	private int numPlns=0;
	private BSPPlane[] planes;
	
	// CONSTRUCTORS
	
	// This one accepts the lump path as a String
	public BSPPlanes(String in) {
		data=new File(in);
		try {
			numPlns=getNumElements();
			length=(int)data.length();
			planes=new BSPPlane[numPlns];
			populatePlaneList();
		} catch(java.io.FileNotFoundException e) {
			Window.println("ERROR: File "+data.getName()+" not found!",Window.VERBOSITY_ALWAYS);
		} catch(java.io.IOException e) {
			Window.println("ERROR: File "+data.getName()+" could not be read, ensure the file is not open in another program",Window.VERBOSITY_ALWAYS);
		}
	}
	
	// This one accepts the input file path as a File
	public BSPPlanes(File in) {
		data=in;
		try {
			numPlns=getNumElements();
			length=(int)data.length();
			planes=new BSPPlane[numPlns];
			populatePlaneList();
		} catch(java.io.FileNotFoundException e) {
			Window.println("ERROR: File "+data.getName()+" not found!",Window.VERBOSITY_ALWAYS);
		} catch(java.io.IOException e) {
			Window.println("ERROR: File "+data.getName()+" could not be read, ensure the file is not open in another program",Window.VERBOSITY_ALWAYS);
		}
	}
	
	public BSPPlanes(BSPPlanes in) {
		numPlns=in.getNumElements();
		length=in.length*20;
		planes=new BSPPlane[numPlns];
		for(int i=0;i<numPlns;i++) {
			planes[i]=new BSPPlane(in.getPlane(i).getA(), in.getPlane(i).getB(), in.getPlane(i).getC(), in.getPlane(i).getDist(), in.getPlane(i).getType());
		}
	}
	
	public BSPPlanes(byte[] in) {
		int offset=0;
		length=in.length;
		numPlns=in.length/20;
		planes=new BSPPlane[numPlns];
		for(int i=0;i<numPlns;i++) {
			byte[] planeBytes=new byte[20];
			for(int j=0;j<20;j++) {
				planeBytes[j]=in[offset+j];
			}
			planes[i]=new BSPPlane(planeBytes);
			offset+=20;
		}
	}
	
	// METHODS
	
	// -populatePlaneList()
	// Parses all data into an array of BSPPlane.
	private void populatePlaneList() throws java.io.FileNotFoundException, java.io.IOException {
		FileInputStream reader=new FileInputStream(data);
		for(int i=0;i<numPlns;i++) {
			byte[] datain=new byte[20];
			reader.read(datain);
			planes[i]=new BSPPlane(datain);
		}
		reader.close();
	}
		
	// ACCESSORS/MUTATORS
	
	// Returns the length (in bytes) of the lump
	public int getLength() {
		return length;
	}
	
	// Returns the number of planes.
	public int getNumElements() {
		if(numPlns==0) {
			return length/20;
		} else {
			return numPlns;
		}
	}
	
	public BSPPlane getPlane(int i) {
		return planes[i];
	}
	
	public BSPPlane[] getPlanes() {
		return planes;
	}
}