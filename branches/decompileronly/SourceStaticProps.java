// SourceStaticProps class

// Maintains an array of SourceStaticProps.

import java.io.FileInputStream;
import java.io.File;

public class SourceStaticProps {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	private File data;
	private int length;
	private SourceStaticProp[] elements;
	
	private int version;
	
	private int structLength;
	
	private String[] dictionary;

	// CONSTRUCTORS
	
	// Accepts a filepath as a String
	public SourceStaticProps(String in, int type) throws java.lang.InterruptedException {
		new SourceStaticProps(new File(in), type);
	}
	
	// This one accepts the input file path as a File
	public SourceStaticProps(File in, int type) throws java.lang.InterruptedException {
		data=in;
		try {
			FileInputStream fileReader=new FileInputStream(data);
			byte[] temp=new byte[(int)data.length()];
			fileReader.read(temp);
			new SourceStaticProps(temp, type, 0);
			fileReader.close();
		} catch(java.io.FileNotFoundException e) {
			Window.println("ERROR: File "+data.getPath()+" not found!",Window.VERBOSITY_ALWAYS);
		} catch(java.io.IOException e) {
			Window.println("ERROR: File "+data.getPath()+" could not be read, ensure the file is not open in another program",Window.VERBOSITY_ALWAYS);
		}
	}
	
	// Takes a byte array, as if read from a FileInputStream
	public SourceStaticProps(byte[] in, int type, int version) throws java.lang.InterruptedException {
		if(in.length>0) {
			this.version=version;
			/*switch(type) { // It's possible to determine structlength using arithmetic rather than version numbering
				case BSP.TYPE_SOURCE17:
				case BSP.TYPE_SOURCE18:
				case BSP.TYPE_SOURCE19:
				case BSP.TYPE_SOURCE20:
				case BSP.TYPE_SOURCE21:
				case BSP.TYPE_SOURCE22:
				case BSP.TYPE_SOURCE23:
				switch(version) {
					case 4:
						structLength=56;
						break;
					case 5:
						structLength=60;
						break;
					case 6:
						structLength=64;
						break;
					case 7:
						structLength=68;
						break;
					case 8:
						structLength=72;
						break;
					case 9:
						structLength=73; // ??? The last entry is a boolean, is it stored as a byte?
						break;
					default:
						structLength=0;
						break;
				default:
					structLength=0;
			}*/
			int offset=0;
			length=in.length;
			dictionary=new String[DataReader.readInt(in[offset++], in[offset++], in[offset++], in[offset++])];
			for(int i=0;i<dictionary.length;i++) {
				byte[] temp=new byte[128];
				for(int j=0;j<128;j++) {
					temp[j]=in[offset++];
				}
				dictionary[i]=DataReader.readNullTerminatedString(temp);
			}
			int numLeafDefinitions=DataReader.readInt(in[offset++], in[offset++], in[offset++], in[offset++]);
			for(int i=0;i<numLeafDefinitions;i++) {
				offset+=2; // Each leaf index is an unsigned short, which i just want to skip
			}
			elements=new SourceStaticProp[DataReader.readInt(in[offset++], in[offset++], in[offset++], in[offset++])];
			if(elements.length>0) {
				structLength=(in.length-offset)/elements.length;
				byte[] bytes=new byte[structLength];
				for(int i=0;i<elements.length;i++) {
					if(Thread.currentThread().interrupted()) {
						throw new java.lang.InterruptedException("while populating SourceStaticProp array");
					}
					for(int j=0;j<structLength;j++) {
						bytes[j]=in[offset+j];
					}
					elements[i]=new SourceStaticProp(bytes, type, version);
					offset+=structLength;
				}
			}
		} else {
			elements=new SourceStaticProp[0];
		}
	}
	
	// METHODS
	
	// ACCESSORS/MUTATORS
	
	// Returns the length (in bytes) of the lump
	public int getLength() {
		return length;
	}
	
	// Returns the number of elements.
	public int length() {
		return elements.length;
	}
	
	public SourceStaticProp getElement(int i) {
		return elements[i];
	}
	
	public SourceStaticProp[] getElements() {
		return elements;
	}
	
	public String[] getDictionary() {
		return dictionary;
	}
}