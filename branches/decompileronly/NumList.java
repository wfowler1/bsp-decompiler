// NumList class

// This class holds an array of integers. These may be read from a lump as a list of
// byte, ubyte, short, ushort, int, uint, or long.
// This provides a unified structure for any number listing lumps.

public class NumList {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	public static final int TYPE_BYTE=0;
	public static final int TYPE_UBYTE=1;
	public static final int TYPE_SHORT=2;
	public static final int TYPE_USHORT=3;
	public static final int TYPE_INT=4;
	public static final int TYPE_UINT=5;
	public static final int TYPE_LONG=6;
	
	private int length;
	private long[] elements;
	private int type;

	// CONSTRUCTORS
	
	// Takes a byte array, as if read from a FileInputStream
	public NumList(byte[] in, int type) throws java.lang.InterruptedException {
		length=in.length;
		this.type=type;
		switch(type) {
			case TYPE_BYTE:
				elements=new long[in.length];
				for(int i=0;i<elements.length;i++) {
					if(Thread.currentThread().interrupted()) {
						throw new java.lang.InterruptedException("while populating an array of int8");
					}
					elements[i]=(int)in[i];
				}
				break;
			case TYPE_UBYTE:
				elements=new long[in.length];
				for(int i=0;i<elements.length;i++) {
					if(Thread.currentThread().interrupted()) {
						throw new java.lang.InterruptedException("while populating an array of uint8");
					}
					elements[i]=(int)DataReader.readUByte(in[i]);
				}
				break;
			case TYPE_SHORT:
				elements=new long[in.length/2];
				for(int i=0;i<elements.length;i++) {
					if(Thread.currentThread().interrupted()) {
						throw new java.lang.InterruptedException("while populating an array of int16");
					}
					elements[i]=(int)DataReader.readShort(in[i*2], in[(i*2)+1]);
				}
				break;
			case TYPE_USHORT:
				elements=new long[in.length/2];
				for(int i=0;i<elements.length;i++) {
					if(Thread.currentThread().interrupted()) {
						throw new java.lang.InterruptedException("while populating an array of uint16");
					}
					elements[i]=(int)DataReader.readUShort(in[i*2], in[(i*2)+1]);
				}
				break;
			case TYPE_INT:
				elements=new long[in.length/4];
				for(int i=0;i<elements.length;i++) {
					if(Thread.currentThread().interrupted()) {
						throw new java.lang.InterruptedException("while populating an array of int32");
					}
					elements[i]=(int)DataReader.readInt(in[i*4], in[(i*4)+1], in[(i*4)+2], in[(i*4)+3]);
				}
				break;
			case TYPE_UINT:
				elements=new long[in.length/4];
				for(int i=0;i<elements.length;i++) {
					if(Thread.currentThread().interrupted()) {
						throw new java.lang.InterruptedException("while populating an array of uint32");
					}
					elements[i]=(int)DataReader.readUInt(in[i*4], in[(i*4)+1], in[(i*4)+2], in[(i*4)+3]);
				}
				break;
			case TYPE_LONG:
				elements=new long[in.length/4];
				for(int i=0;i<elements.length;i++) {
					if(Thread.currentThread().interrupted()) {
						throw new java.lang.InterruptedException("while populating an array of int64");
					}
					elements[i]=(int)DataReader.readLong(in[i*4], in[(i*4)+1], in[(i*4)+2], in[(i*4)+3], in[(i*4)+4], in[(i*4)+5], in[(i*4)+6], in[(i*4)+7]);
				}
				break;
		}
	}
	
	// METHODS
	public boolean hasFunnySize() {
		switch(type) {
			case TYPE_BYTE:
			case TYPE_UBYTE:
				return false;
			case TYPE_SHORT:
			case TYPE_USHORT:
				return (length%2!=0);
			case TYPE_INT:
			case TYPE_UINT:
				return (length%4!=0);
			case TYPE_LONG:
				return (length%8!=0);
		}
		return false;
	}
	
	// ACCESSORS/MUTATORS
	
	// Returns the length (in bytes) of the lump
	public int length() {
		return length;
	}
	
	// Returns the number of elements.
	public int size() {
		return elements.length;
	}
	
	public long getElement(int i) {
		return elements[i];
	}
	
	public long[] getElements() {
		return elements;
	}
}