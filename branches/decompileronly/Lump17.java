// Lump17 class

// Holds the information for texture scaling and alignment.
// Referenced only by faces. The data contained here could
// potentially be recycled.

import java.io.FileInputStream;
import java.io.File;

public class Lump17 {

	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	private File data;
	private int numTxmatxs=0; // I really don't know what to call this lump
	private TexMatrix[] texturematrix;
	
	// CONSTRUCTORS
	
	// This one accepts the lump path as a String
	public Lump17(String in) {
		data=new File(in);
		try {
			numTxmatxs=getNumElements();
			texturematrix=new TexMatrix[numTxmatxs];
			populateTextureMatrixList();
		} catch(java.io.FileNotFoundException e) {
			Decompiler.window.println("ERROR: File "+data+" not found!");
		} catch(java.io.IOException e) {
			Decompiler.window.println("ERROR: File "+data+" could not be read, ensure the file is not open in another program");
		}
	}
	
	// This one accepts the input file path as a File
	public Lump17(File in) {
		data=in;
		try {
			numTxmatxs=getNumElements();
			texturematrix=new TexMatrix[numTxmatxs];
			populateTextureMatrixList();
		} catch(java.io.FileNotFoundException e) {
			Decompiler.window.println("ERROR: File "+data+" not found!");
		} catch(java.io.IOException e) {
			Decompiler.window.println("ERROR: File "+data+" could not be read, ensure the file is not open in another program");
		}
	}
	
	// METHODS
	
	// -populateTextureMatrixList()
	// If you don't know what this does by now then look at
	// the other lump classes.
	private void populateTextureMatrixList() throws java.io.FileNotFoundException, java.io.IOException {
		FileInputStream reader=new FileInputStream(data);
		try {
			for(int i=0;i<numTxmatxs;i++) {
				byte[] datain=new byte[32];
				reader.read(datain);
				texturematrix[i]=new TexMatrix(datain);
			}
			reader.close();
		} catch(InvalidTextureMatrixException e) {
			Decompiler.window.println("WARNING: Funny lump size in "+data+", ignoring last texture matrix.");
		}
	}
		
	// ACCESSORS/MUTATORS
	
	// Returns the length (in bytes) of the lump
	public long getLength() {
		return data.length();
	}
	
	// Returns the number of texture scales.
	public int getNumElements() {
		if(numTxmatxs==0) {
			return (int)data.length()/32;
		} else {
			return numTxmatxs;
		}
	}
	
	public TexMatrix getTexMatrix(int i) {
		return texturematrix[i];
	}
	
	public TexMatrix[] getTexMatrices() {
		return texturematrix;
	}
	
	public void setTexMatrix(int i, TexMatrix in) {
		texturematrix[i] = in;
	}
}