// Lump06 class

// This lump is really a list of ints. These ints are indices to a specific part
// of the lump04 vertices. It's all handled by lump09.

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.File;

public class Lump06 {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	private File data;
	private int numMeshes=0;
	private int[] meshes;
	
	// CONSTRUCTORS
	
	// This one accepts the lump path as a String
	public Lump06(String in) {
		data=new File(in);
		try {
			numMeshes=getNumElements();
			meshes=new int[numMeshes];
			populateMeshList();
		} catch(java.io.FileNotFoundException e) {
			System.out.println("ERROR: File "+data+" not found!");
		} catch(java.io.IOException e) {
			System.out.println("ERROR: File "+data+" could not be read, ensure the file is not open in another program");
		}
	}
	
	// This one accepts the input file path as a File
	public Lump06(File in) {
		data=in;
		try {
			numMeshes=getNumElements();
			meshes=new int[numMeshes];
			populateMeshList();
		} catch(java.io.FileNotFoundException e) {
			System.out.println("ERROR: File "+data+" not found!");
		} catch(java.io.IOException e) {
			System.out.println("ERROR: File "+data+" could not be read, ensure the file is not open in another program");
		}
	}
	
	// METHODS
	
	// -populateMeshList()
	// Populates a list of meshes, which are really indices to a specific set of
	// vertices determined by lump09.
	private void populateMeshList() throws java.io.IOException, java.io.IOException {
		FileInputStream reader=new FileInputStream(data);
		byte[] in=new byte[4];
		for(int i=0;i<numMeshes;i++) {
			reader.read(in);
			meshes[i]=(in[3] << 24) | ((in[2] & 0xff) << 16) | ((in[1] & 0xff) << 8) | (in[0] & 0xff);
		}
		reader.close();
	}
		
	// ACCESSORS/MUTATORS
	
	// Returns the length (in bytes) of the lump
	public long getLength() {
		return data.length();
	}
	
	// Returns the number of meshes.
	public int getNumElements() {
		if(numMeshes==0) {
			return (int)data.length()/4;
		} else {
			return numMeshes;
		}
	}
	
	public int getMesh(int i) {
		return meshes[i];
	}
	
	public int[] getMeshes() {
		return meshes;
	}
	
	public void setMesh(int i, int in) {
		meshes[i]=in;
	}
	
	public void setMeshes(int[] in) {
		meshes=in;
		numMeshes=in.length;
	}
}
