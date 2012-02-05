// PlaneList class

// This class holds references to an array of Plane classes which
// hold the data for all the planes.

import java.io.FileInputStream;
import java.io.File;

public class PlaneList {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	private File data;
	private int length;
	private int numPlns=0;
	private Plane[] planes;
	
	// CONSTRUCTORS
	
	// This one accepts the lump path as a String
	public PlaneList(String in) {
		data=new File(in);
		try {
			numPlns=getNumElements();
			length=(int)data.length();
			planes=new Plane[numPlns];
			populatePlaneList();
		} catch(java.io.FileNotFoundException e) {
			Window.window.println("ERROR: File "+data+" not found!");
		} catch(java.io.IOException e) {
			Window.window.println("ERROR: File "+data+" could not be read, ensure the file is not open in another program");
		}
	}
	
	// This one accepts the input file path as a File
	public PlaneList(File in) {
		data=in;
		try {
			numPlns=getNumElements();
			length=(int)data.length();
			planes=new Plane[numPlns];
			populatePlaneList();
		} catch(java.io.FileNotFoundException e) {
			Window.window.println("ERROR: File "+data+" not found!");
		} catch(java.io.IOException e) {
			Window.window.println("ERROR: File "+data+" could not be read, ensure the file is not open in another program");
		}
	}
	
	public PlaneList(PlaneList in) {
		numPlns=in.getNumElements();
		length=in.length*16;
		planes=new Plane[numPlns];
		for(int i=0;i<numPlns;i++) {
			planes[i]=new Plane(in.getPlane(i).getA(), in.getPlane(i).getB(), in.getPlane(i).getC(), in.getPlane(i).getDist());
		}
	}
	
	public PlaneList(byte[] in) {
		int offset=0;
		length=in.length;
		numPlns=in.length/16;
		planes=new Plane[numPlns];
		for(int i=0;i<numPlns;i++) {
			byte[] planeBytes=new byte[16];
			for(int j=0;j<16;j++) {
				planeBytes[j]=in[offset+j];
			}
			planes[i]=new Plane(planeBytes);
			offset+=16;
		}
	}
	
	// METHODS
	
	// -populatePlaneList()
	// Parses all data into an array of Plane.
	private void populatePlaneList() throws java.io.FileNotFoundException, java.io.IOException {
		FileInputStream reader=new FileInputStream(data);
		for(int i=0;i<numPlns;i++) {
			byte[] datain=new byte[16];
			reader.read(datain);
			planes[i]=new Plane(datain);
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
			return length/16;
		} else {
			return numPlns;
		}
	}
	
	public Plane getPlane(int i) {
		return planes[i];
	}
	
	public Plane[] getPlanes() {
		return planes;
	}
}