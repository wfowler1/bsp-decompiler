// v38Textures class

// This class keeps and maintains an array, which is a list
// of the textures and their information in the map.

import java.io.FileInputStream;
import java.io.File;

public class v38Textures {

	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	private File data;
	private int numTxts=0;
	private int length;
	private v38Texture[] textures;
	
	// CONSTRUCTORS
	
	// This one accepts the lump path as a String
	public v38Textures(String in) {
		data=new File(in);
		length=(int)data.length();
		try {
			numTxts=getNumElements();
			textures=new v38Texture[numTxts];
			populateTextureList();
		} catch(java.io.FileNotFoundException e) {
			Window.println("ERROR: File "+data+" not found!",0);
		} catch(java.io.IOException e) {
			Window.println("ERROR: File "+data+" could not be read, ensure the file is not open in another program",0);
		}
	}
	
	// This one accepts the input file path as a File
	public v38Textures(File in) {
		data=in;
		length=(int)data.length();
		try {
			numTxts=getNumElements();
			textures=new v38Texture[numTxts];
			populateTextureList();
		} catch(java.io.FileNotFoundException e) {
			Window.println("ERROR: File "+data+" not found!",0);
		} catch(java.io.IOException e) {
			Window.println("ERROR: File "+data+" could not be read, ensure the file is not open in another program",0);
		}
	}
	
	public v38Textures(byte[] in) {
		int offset=0;
		length=in.length;
		numTxts=in.length/76;
		textures=new v38Texture[numTxts];
		for(int i=0;i<numTxts;i++) {
			byte[] textureBytes=new byte[76];
			for(int j=0;j<76;j++) {
				textureBytes[j]=in[offset+j];
			}
			textures[i]=new v38Texture(textureBytes);
			offset+=76;
		}
	}
	
	// METHODS
	
	// -populateTextureList()
	// Uses the instance data to populate the array of v38Texture
	private void populateTextureList() throws java.io.FileNotFoundException, java.io.IOException {
		FileInputStream reader=new FileInputStream(data);
		for(int i=0;i<numTxts;i++) {
			byte[] datain=new byte[76];
			reader.read(datain);
			textures[i]=new v38Texture(datain);
		}
		reader.close();
	}
		
	// ACCESSORS/MUTATORS
	
	// Returns the length (in bytes) of the lump
	public int getLength() {
		return length;
	}
	
	// Returns the number of Leaves.
	public int getNumElements() {
		if(numTxts==0) {
			return length/76;
		} else {
			return numTxts;
		}
	}
	
	public v38Texture getTexture(int i) {
		return textures[i];
	}
	
	public v38Texture[] getTextures() {
		return textures;
	}
}
