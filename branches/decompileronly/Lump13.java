// Lump13 class

// This class holds an array of integers which are ALL indices into
// lump 15. This lump is about as retarded as lump 12.

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.File;

public class Lump13 {

	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	private File data;
	private int numMBrshs=0;
	private int[] markbrushes;
	
	// CONSTRUCTORS
	
	// This one accepts the lump path as a String
	public Lump13(String in) {
		data=new File(in);
		try {
			numMBrshs=getNumElements();
			markbrushes=new int[numMBrshs];
			populateMBrshList();
		} catch(java.io.FileNotFoundException e) {
			System.out.println("ERROR: File "+data+" not found!");
		} catch(java.io.IOException e) {
			System.out.println("ERROR: File "+data+" could not be read, ensure the file is not open in another program");
		}
	}
	
	// This one accepts the input file path as a File
	public Lump13(File in) {
		data=in;
		try {
			numMBrshs=getNumElements();
			markbrushes=new int[numMBrshs];
			populateMBrshList();
		} catch(java.io.FileNotFoundException e) {
			System.out.println("ERROR: File "+data+" not found!");
		} catch(java.io.IOException e) {
			System.out.println("ERROR: File "+data+" could not be read, ensure the file is not open in another program");
		}
	}
	
	// METHODS
	
	// -populateMBrshList()
	// Uses the file in the instance data to populate the list of indices
	private void populateMBrshList() throws java.io.FileNotFoundException, java.io.IOException {
		FileInputStream reader=new FileInputStream(data);
		for(int i=0;i<numMBrshs;i++) {
			byte[] datain=new byte[4];
			reader.read(datain);
			markbrushes[i]=(datain[3] << 24) | ((datain[2] & 0xff) << 16) | ((datain[1] & 0xff) << 8) | (datain[0] & 0xff);
		}
		reader.close();
	}
	
	// +add(int)
	// adds a single int to the list
	public void add(int in) {
		int[] newList=new int[numMBrshs+1];
		for(int i=0;i<numMBrshs;i++) {
			newList[i]=markbrushes[i];
		}
		newList[numMBrshs]=in;
		numMBrshs++;
		markbrushes=newList;
	}
	
	// +add(Lump13)
	// adds every item from another lump13 object.
	public void add(Lump13 in) {
		int[] newList=new int[numMBrshs+in.getNumElements()];
		File myLump15=new File(data.getParent()+"//15 - Brushes.hex");
		int sizeL15=(int)myLump15.length()/12;
		for(int i=0;i<numMBrshs;i++) {
			newList[i]=markbrushes[i];
		}
		for(int i=0;i<in.getNumElements(); i++) {
			newList[i+numMBrshs]=in.getMarkBrush(i)+sizeL15;
		}
		numMBrshs=numMBrshs+in.getNumElements();
		markbrushes=newList;
	}
	
	// save(String)
	// Saves the lump to the specified path.
	public void save(String path) {
		File newFile=new File(path+"\\13 - Mark Brushes.hex");
		try {
			if(!newFile.exists()) {
				newFile.createNewFile();
			} else {
				newFile.delete();
				newFile.createNewFile();
			}
			FileOutputStream mBrushWriter=new FileOutputStream(newFile);
			byte[] data=new byte[numMBrshs*4];
			for(int i=0;i<numMBrshs;i++) {
				// This is MUCH faster than using DataOutputStream
				data[(i*4)+3]=(byte)((markbrushes[i] >> 24) & 0xFF);
				data[(i*4)+2]=(byte)((markbrushes[i] >> 16) & 0xFF);
				data[(i*4)+1]=(byte)((markbrushes[i] >> 8) & 0xFF);
				data[i*4]=(byte)((markbrushes[i] >> 0) & 0xFF);
			}
			mBrushWriter.write(data);
			mBrushWriter.close();
		} catch(java.io.IOException e) {
			System.out.println("ERROR: Could not save "+newFile+", ensure the file is not open in another program and the path "+path+" exists");
		}
	}
	
	// save()
	// Saves the lump, overwriting the one data was read from
	public void save() {
		save(data.getParent());
	}
	
	// ACCESSORS/MUTATORS
	
	// Returns the length (in bytes) of the lump
	public long getLength() {
		return data.length();
	}
	
	// Returns the number of brush indices. This lump is RETARDED.
	public int getNumElements() {
		if(numMBrshs==0) {
			return (int)data.length()/4;
		} else {
			return numMBrshs;
		}
	}
	
	public void setMarkBrush(int i, int in) {
		markbrushes[i]=in;
	}
	
	public int getMarkBrush(int i) {
		return markbrushes[i];
	}
	
	public int[] getMarkBrushes() {
		return markbrushes;
	}
}
