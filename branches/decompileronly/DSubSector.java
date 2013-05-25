// DSubSector class

// This class holds data of a single Doom Subsector.
// I'm disappointed with the way this lump was handled. The way it is, it only
// references a set of line segments which don't even fully define it. To get
// the full picture of a subsector you must iterate through the nodes. This lump
// could have had so much more useful information, but no, it passes everything
// off to the line segments, which eventually gets to sectors though a maze
// of unnecessary references. I'm not sure why this lump is named this, it is
// only distantly related to sectors.
//
// Be that as it may, this lump fulfils the roles of both Leaves and Brushes of
// later BSP formats. Therefore, a subsector implicitly defines a convex polygon
// usable in creating brushes in a conventional .MAP file. The hard part is 
// explicitly finding the sides.

public class DSubSector extends LumpObject {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	private short numSegs;
	private short firstSeg;
	
	// CONSTRUCTORS
	
	// This one takes the components separate and in the correct data type
	/*public DSubSector(short numSegs, short firstSeg) {
		this.numSegs=numSegs;
		this.firstSeg=firstSeg;
	}*/

	// This one takes an array of bytes (as if read directly from a file) and reads them
	// directly into the proper data types.
	public DSubSector(byte[] in) {
		super(in);
		numSegs=DataReader.readShort(in[0], in[1]);
		firstSeg=DataReader.readShort(in[2], in[3]);
	}
	
	// METHODS
	public static Lump<DSubSector> createLump(byte[] in) throws java.lang.InterruptedException {
		int structLength=4;
		int offset=0;
		DSubSector[] elements=new DSubSector[in.length/structLength];
		byte[] bytes=new byte[structLength];
		for(int i=0;i<elements.length;i++) {
			if(Thread.currentThread().interrupted()) {
				throw new java.lang.InterruptedException("while populating Subsector array");
			}
			for(int j=0;j<structLength;j++) {
				bytes[j]=in[offset+j];
			}
			elements[i]=new DSubSector(bytes);
			offset+=structLength;
		}
		return new Lump<DSubSector>(elements, in.length, structLength);
	}
	
	// ACCESSORS/MUTATORS
	public short getNumSegs() {
		return numSegs;
	}
	
	public void setNumSegs(short in) {
		numSegs=in;
	}
	
	public short getFirstSeg() {
		return firstSeg;
	}
	
	public void setFirstSeg(short in) {
		firstSeg=in;
	}
}