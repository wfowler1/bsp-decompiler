// SourceTextures class
// Reads and maintains an array of Strings which is the textures in a Source map

import java.io.File;
import java.io.FileInputStream;

public class SourceTextures {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	private File data;
	private String[] textures;
	private int length;
	
	// CONSTRUCTORS
	
	public SourceTextures(String in) {
		new SourceTextures(new File(in));
	}
	
	public SourceTextures(File in) {
		data=in;
		try {
			FileInputStream fileReader=new FileInputStream(data);
			byte[] temp=new byte[(int)data.length()];
			fileReader.read(temp);
			new SourceTextures(temp);
			fileReader.close();
		} catch(java.io.FileNotFoundException e) {
			Window.println("ERROR: File not found: "+data.getPath()+" Please ensure the file exists!",0);
		} catch(java.io.IOException e) {
			Window.println("ERROR: Unable to read file: "+data.getPath()+" Please ensure the file is not open in another program!",0);
		}
	}
	
	public SourceTextures(byte[] in) {
		length=in.length;
		int cnt=0;
		for(int i=0;i<in.length;i++) {
			if(in[i]==0x00) {
				cnt++;
			}
		}
		textures=new String[cnt];
		int current=0;
		for(int i=0;i<in.length;i++) {
			if(textures[current]==null) {
				textures[current]="";
			}
			if(in[i]==0) {
				current++;
			} else {
				textures[current]+=(char)in[i];
			}
		}
	}
	
	// METHODS
	
	public void printTextures() {
		for(int i=0;i<textures.length;i++) {
			Window.println(textures[i],0);
		}
	}
	
	// ACCESSORS/MUTATORS
	
	public void setElement(int i, String in) {
		textures[i]=in;
	}
	
	public String getElement(int i) {
		return textures[i];
	}
	
	public String getTextureAtOffset(int target) {
		String temp="";
		int offset=0;
		for(int i=0;i<textures.length;i++) {
			if(offset<target) {
				offset+=textures[i].length()+1; // Add 1 for the now missing null byte. I really did think of everything! :D
			} else {
				return textures[i];
			}
		}
		// If we get to this point, the strings ended before target offset was reached
		return null; // Perhaps this will throw an exception down the line? :trollface:
	}
	
	public int getOffsetOf(String inTexture) {
		int offset=0;
		for(int i=0;i<textures.length;i++) {
			if(!textures[i].equalsIgnoreCase(inTexture)) {
				offset+=textures[i].length()+1;
			} else {
				return offset;
			}
		}
		// If we get to here, the requested texture didn't exist.
		return -1; // This will PROBABLY throw an exception later.
	}
	
	// getLength returns datafile length
	public int getLength() {
		return length;
	}
	
	// length returns the number of strings in the array
	public int length() {
		return textures.length;
	}
}