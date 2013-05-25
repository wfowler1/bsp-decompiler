// DNode class

// Holds all the data for a node in a Doom map.
// This is the one lump that has a structure similar to future BSPs.

// Though all the coordinates are handled as 2D 16-bit shortwords, I'm going to
// automatically convert everything to work with my established Vector3D.
// This simplifies my code by not needing another vector class for 2D coordinates
// defined by signed 16-bit numbers while still using the values.

public class DNode extends LumpObject {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	private Vector3D vecHead; // This format uses a vector head and tail for partitioning, rather
	private Vector3D vecTail; // than the 3D plane conventionally used by more modern engines.
	// The "tail" is actually a change in X and Y, rather than an explicitly defined point.
	private Vector3D[] RRectangle=new Vector3D[2]; // The stupid thing is, these rectangles are defined by
	private Vector3D[] LRectangle=new Vector3D[2]; // top, bottom, left, right. That's YMax, YMin, XMin, XMax, in that order
	private short RChild; // Since partitioning is done using a vector, there is still the idea of
	private short LChild; // directionality. Therefore, two subnodes can consistently be called "right" or "left"
	
	// XYZ, PDQ
	public static final int X=0;
	public static final int Y=1;
	public static final int Z=2;
	
	public static final int MINS=0;
	public static final int MAXS=1;
	
	// CONSTRUCTORS
	
	// This constructor takes all data in their proper data types
	/*public DNode(short segHeadX, short segHeadY, short segTailX, short segTailY, short RRectTop,
	             short RRectBottom, short RRectLeft, short RRectRight, short LRectTop, short LRectBottom,
	             short LRectLeft, short LRectRight, short RChild, short LChild) {
		vecHead=new Vector3D(segHeadX, segHeadY);
		vecTail=new Vector3D(segTailX, segTailY);
		Vector3D RMins=new Vector3D(RRectLeft, RRectBottom);
		Vector3D RMaxs=new Vector3D(RRectRight, RRectTop);
		Vector3D LMins=new Vector3D(LRectLeft, LRectBottom);
		Vector3D LMaxs=new Vector3D(LRectRight, LRectTop);
		RRectangle= new Vector3D[2];
		RRectangle[0] = RMins;
		RRectangle[1] = RMaxs;
		LRectangle= new Vector3D[2];
		LRectangle[0] = LMins;
		LRectangle[1] = LMaxs;
		this.RChild=RChild;
		this.LChild=LChild;
	}*/
	
	// This constructor takes in a byte array, as though
	// it had just been read by a FileInputStream.
	public DNode(byte[] in) {
		super(in);
		vecHead=new Vector3D(DataReader.readShort(in[0], in[1]), DataReader.readShort(in[2], in[3]));
		vecTail=new Vector3D(DataReader.readShort(in[4], in[5]), DataReader.readShort(in[6], in[7]));
		Vector3D RMins=new Vector3D(DataReader.readShort(in[12], in[13]), DataReader.readShort(in[10], in[11]));
		Vector3D RMaxs=new Vector3D(DataReader.readShort(in[14], in[15]), DataReader.readShort(in[8], in[9]));
		Vector3D LMins=new Vector3D(DataReader.readShort(in[20], in[21]), DataReader.readShort(in[18], in[19]));
		Vector3D LMaxs=new Vector3D(DataReader.readShort(in[22], in[23]), DataReader.readShort(in[16], in[17]));
		RRectangle= new Vector3D[2];
		RRectangle[0] = RMins;
		RRectangle[1] = RMaxs;
		LRectangle= new Vector3D[2];
		LRectangle[0] = LMins;
		LRectangle[1] = LMaxs;
		this.RChild=DataReader.readShort(in[24], in[25]);
		this.LChild=DataReader.readShort(in[26], in[27]);
	}
	
	// METHODS
	
	// intersectsBox(Vertex mins, Vertex maxs)
	// Determines if this node's partition vector (as a line segment) intersects the passed box.
	// Seems rather esoteric, no? But it's needed. Algorithm adapted from top answer at 
	// http://stackoverflow.com/questions/99353/how-to-test-if-a-line-segment-intersects-an-axis-aligned-rectange-in-2d
	public boolean intersectsBox(Vector3D mins, Vector3D maxs) {
		// Compute the signed distance from the line to each corner of the box
		double[] dist = new double[4];
		double x1=vecHead.getX();
		double x2=vecHead.getX()+vecTail.getX();
		double y1=vecHead.getY();
		double y2=vecHead.getY()+vecTail.getY();
		dist[0]=vecTail.getY()*mins.getX() + vecTail.getX()*mins.getY() + (x2*y1-x1*y2);
		dist[1]=vecTail.getY()*mins.getX() + vecTail.getX()*maxs.getY() + (x2*y1-x1*y2);
		dist[2]=vecTail.getY()*maxs.getX() + vecTail.getX()*mins.getY() + (x2*y1-x1*y2);
		dist[3]=vecTail.getY()*maxs.getX() + vecTail.getX()*maxs.getY() + (x2*y1-x1*y2);
		if(dist[0]>=0 && dist[1]>=0 && dist[2]>=0 && dist[3]>=0) {
			return false;
		} else {
			if(dist[0]<=0 && dist[1]<=0 && dist[2]<=0 && dist[3]<=0) {
				return false;
			} else { // If we get to this point, the line intersects the box. Figure out if the line SEGMENT actually cuts it.
				if((x1>maxs.getX() && x2>maxs.getX()) || (x1<mins.getX() && x2<mins.getX()) || (y1>maxs.getY() && y2>maxs.getY()) || (y1<mins.getY() && y2<mins.getY())) {
					return false;
				} else {
					return true;
				}
			}
		}
	}
	
	public static Lump<DNode> createLump(byte[] in) throws java.lang.InterruptedException {
		int structLength=28;
		int offset=0;
		DNode[] elements=new DNode[in.length/structLength];
		byte[] bytes=new byte[structLength];
		for(int i=0;i<elements.length;i++) {
			if(Thread.currentThread().interrupted()) {
				throw new java.lang.InterruptedException("while populating Brush array");
			}
			for(int j=0;j<structLength;j++) {
				bytes[j]=in[offset+j];
			}
			elements[i]=new DNode(bytes);
			offset+=structLength;
		}
		return new Lump<DNode>(elements, in.length, structLength);
	}
	
	// ACCESSORS/MUTATORS
	
	public Vector3D getVecHead() {
		return vecHead;
	}
	
	public void setVecHead(Vector3D in) {
		vecHead=in;
	}
	
	public Vector3D getVecTail() {
		return vecTail;
	}
	
	public void setVecTail(Vector3D in) {
		vecTail=in;
	}
	
	public int getChild1() {
		return RChild;
	}
	
	public void setChild1(short in) {
		RChild=in;
	}
	
	public int getChild2() {
		return LChild;
	}
	
	public void setChild2(short in) {
		LChild=in;
	}
	
	public Vector3D getRMins() {
		return RRectangle[MINS];
	}
	
	public void setRMins(Vector3D in) {
		RRectangle[MINS]=in;
	}
	
	public Vector3D getLMins() {
		return LRectangle[MINS];
	}
	
	public void setLMins(Vector3D in) {
		LRectangle[MINS]=in;
	}
	
	public Vector3D getRMaxs() {
		return RRectangle[MAXS];
	}
	
	public void setRMaxs(Vector3D in) {
		RRectangle[MAXS]=in;
	}
	
	public Vector3D getLMaxs() {
		return LRectangle[MAXS];
	}
	
	public void setLMaxs(Vector3D in) {
		LRectangle[MAXS]=in;
	}
}
