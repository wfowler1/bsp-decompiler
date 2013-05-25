// SourceDispVertices class

// Extends Lump class, and contains methods only useful for Displacement vertices.
// Only one method in this class, can it go somewhere else?

public class SourceDispVertices extends Lump<SourceDispVertex> {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS

	// CONSTRUCTORS
	
	// Takes a byte array, as if read from a FileInputStream
	public SourceDispVertices(SourceDispVertex[] elements, int length) {
		super(elements, length, 20);
	}
	
	// METHODS
	
	// ACCESSORS/MUTATORS
	
	public SourceDispVertex[] getVertsInDisp(int first, int power) {
		int numVerts=0;
		switch(power) {
			case 2:
				numVerts=25;
				break;
			case 3:
				numVerts=81;
				break;
			case 4:
				numVerts=289;
				break;
		}
		SourceDispVertex[] out=new SourceDispVertex[numVerts];
		for(int i=0;i<numVerts;i++) {
			out[i]=getElement(first+i);
		}
		return out;
	}
}