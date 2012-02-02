// TextureMatrices class

// Holds the information for texture scaling and alignment.
// Referenced only by faces. The data contained here could
// potentially be recycled.

import java.io.FileInputStream;
import java.io.File;

public class TextureMatrices {

	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	private File data;
	private int length;
	private int numTxmatxs=0; // I really don't know what to call this lump
	private TexMatrix[] texturematrix;
	
	// CONSTRUCTORS
	
	// This one accepts the lump path as a String
	public TextureMatrices(String in) {
		data=new File(in);
		length=(int)data.length();
		try {
			numTxmatxs=getNumElements();
			texturematrix=new TexMatrix[numTxmatxs];
			populateTextureMatrixList();
		} catch(java.io.FileNotFoundException e) {
			Window.window.println("ERROR: File "+data+" not found!");
		} catch(java.io.IOException e) {
			Window.window.println("ERROR: File "+data+" could not be read, ensure the file is not open in another program");
		}
	}
	
	// This one accepts the input file path as a File
	public TextureMatrices(File in) {
		data=in;
		length=(int)data.length();
		try {
			numTxmatxs=getNumElements();
			texturematrix=new TexMatrix[numTxmatxs];
			populateTextureMatrixList();
		} catch(java.io.FileNotFoundException e) {
			Window.window.println("ERROR: File "+data+" not found!");
		} catch(java.io.IOException e) {
			Window.window.println("ERROR: File "+data+" could not be read, ensure the file is not open in another program");
		}
	}
	
	public TextureMatrices(byte[] in) {
		int offset=0;
		numTxmatxs=in.length/32;
		length=in.length;
		texturematrix=new TexMatrix[numTxmatxs];
		try {
			for(int i=0;i<numTxmatxs;i++) {
				byte[] texMatrixBytes=new byte[32];
				for(int j=0;j<32;j++) {
					texMatrixBytes[j]=in[offset+j];
				}
				texturematrix[i]=new TexMatrix(texMatrixBytes);
				offset+=32;
			}
		} catch(InvalidTextureMatrixException e) {
			Window.window.println("WARNING: Funny lump size in "+data+", ignoring last texture matrix.");
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
			Window.window.println("WARNING: Funny lump size in "+data+", ignoring last texture matrix.");
		}
	}
		
	// ACCESSORS/MUTATORS
	
	// Returns the length (in bytes) of the lump
	public int getLength() {
		return length;
	}
	
	// Returns the number of texture scales.
	public int getNumElements() {
		if(numTxmatxs==0) {
			return length/32;
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