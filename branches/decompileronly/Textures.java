// Textures class

// Maintains an array of Textures.

import java.io.FileInputStream;
import java.io.File;

public class Textures {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	private File data;
	private int length;
	private Texture[] elements;
	
	private int structLength;

	// CONSTRUCTORS
	
	// Accepts a filepath as a String
	public Textures(String in, int type) {
		new Textures(new File(in), type);
	}
	
	// This one accepts the input file path as a File
	public Textures(File in, int type) {
		data=in;
		try {
			FileInputStream fileReader=new FileInputStream(data);
			byte[] temp=new byte[(int)data.length()];
			fileReader.read(temp);
			new Textures(temp, type);
			fileReader.close();
		} catch(java.io.FileNotFoundException e) {
			Window.println("ERROR: File "+data.getPath()+" not found!",Window.VERBOSITY_ALWAYS);
		} catch(java.io.IOException e) {
			Window.println("ERROR: File "+data.getPath()+" could not be read, ensure the file is not open in another program",Window.VERBOSITY_ALWAYS);
		}
	}
	
	// Takes a byte array, as if read from a FileInputStream
	public Textures(byte[] in, int type) {
		int numElements=-1; // For Quake and Source, which use nonconstant struct lengths
		int[] offsets=new int[0]; // For Quake, which stores offsets to each texture definition structure, which IS a constant length
		switch(type) {
			case BSP.TYPE_NIGHTFIRE:
				structLength=64;
				break;
			case BSP.TYPE_QUAKE3:
				structLength=72;
				break;
			case BSP.TYPE_QUAKE2:
			case BSP.TYPE_STEF2:
				structLength=76;
				break;
			case BSP.TYPE_MOHAA:
				structLength=140;
				break;
			case BSP.TYPE_SIN:
				structLength=180;
				break;
			case BSP.TYPE_SOURCE17:
			case BSP.TYPE_SOURCE18:
			case BSP.TYPE_SOURCE19:
			case BSP.TYPE_SOURCE20:
			case BSP.TYPE_SOURCE21:
			case BSP.TYPE_SOURCE22:
			case BSP.TYPE_SOURCE23:
				numElements=0;
				for(int i=0;i<in.length;i++) {
					if(in[i]==0x00) {
						numElements++;
					}
				}
				break;
			case BSP.TYPE_QUAKE:
				numElements=DataReader.readInt(in[0], in[1], in[2], in[3]);
				offsets=new int[numElements];
				for(int i=0;i<numElements;i++) {
					offsets[i]=DataReader.readInt(in[((i+1)*4)], in[((i+1)*4)+1], in[((i+1)*4)+2], in[((i+1)*4)+3]);
				}
				structLength=40;
				break;
			default:
				structLength=0; // This will cause the shit to hit the fan.
		}
		if(numElements==-1) {
			int offset=0;
			length=in.length;
			elements=new Texture[in.length/structLength];
			byte[] bytes=new byte[structLength];
			for(int i=0;i<elements.length;i++) {
				for(int j=0;j<structLength;j++) {
					bytes[j]=in[offset+j];
				}
				elements[i]=new Texture(bytes, type);
				offset+=structLength;
			}
		} else {
			elements=new Texture[numElements];
			if(offsets.length!=0) { // Quake/GoldSrc
				for(int i=0;i<numElements;i++) {
					int offset=offsets[i];
					byte[] bytes=new byte[structLength];
					for(int j=0;j<structLength;j++) {
						bytes[j]=in[offset+j];
					}
					elements[i]=new Texture(bytes, type);
					offset+=structLength;
				}
			} else {  // Source
				int offset=0;
				int current=0;
				length=in.length;
				byte[] bytes=new byte[0];
				for(int i=0;i<in.length;i++) {
					if(in[i]==(byte)0x00) { // They are null-terminated strings, of non-constant length (not padded)
						elements[current]=new Texture(bytes, type);
						bytes=new byte[0];
						current++;
					} else {
						byte[] newList=new byte[bytes.length+1];
						for(int j=0;j<bytes.length;j++) {
							newList[j]=bytes[j];
						}
						newList[bytes.length]=in[i];
						bytes=newList;
					}
					offset++;
				}
			}
		}
	}
	
	// METHODS
	public void printTextures() { // FOR DEBUG PURPOSES ONLY
		for(int i=0;i<elements.length;i++) {
			System.out.println(elements[i].getName());
		}
	}
	
	// ACCESSORS/MUTATORS
	
	// Returns the length (in bytes) of the lump
	public int getLength() {
		return length;
	}
	
	// Returns the number of elements.
	public int length() {
		if(elements.length==0) {
			return length/structLength;
		} else {
			return elements.length;
		}
	}
	
	public String getTextureAtOffset(int target) {
		String temp="";
		int offset=0;
		for(int i=0;i<elements.length;i++) {
			if(offset<target) {
				offset+=elements[i].getName().length()+1; // Add 1 for the now missing null byte. I really did think of everything! :D
			} else {
				return elements[i].getName();
			}
		}
		// If we get to this point, the strings ended before target offset was reached
		return null; // Perhaps this will throw an exception down the line? :trollface:
	}
	
	public int getOffsetOf(String inTexture) {
		int offset=0;
		for(int i=0;i<elements.length;i++) {
			if(!elements[i].getName().equalsIgnoreCase(inTexture)) {
				offset+=elements[i].getName().length()+1;
			} else {
				return offset;
			}
		}
		// If we get to here, the requested texture didn't exist.
		return -1; // This will PROBABLY throw an exception later.
	}
	
	public Texture getElement(int i) {
		return elements[i];
	}
	
	public Texture[] getElements() {
		return elements;
	}
}