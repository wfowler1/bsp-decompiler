// Vertices class

// This class holds an array of vertices of the Vertex class. Really it's an array
// of float3 but that's how it is for consistency's sake.

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.File;

public class Vertices {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	public final int X=0;
	public final int Y=1;
	public final int Z=2;
	
	private File data;
	private int length;
	private int numVerts=0;
	private Point3D[] vertices;
	
	// CONSTRUCTORS
	
	// This one accepts the lump path as a String
	public Vertices(String in) {
		data=new File(in);
		try {
			numVerts=getNumElements();
			length=(int)data.length();
			vertices=new Point3D[numVerts];
			populateVertexList();
		} catch(java.io.FileNotFoundException e) {
			Window.window.println("ERROR: File "+data+" not found!");
		} catch(java.io.IOException e) {
			Window.window.println("ERROR: File "+data+" could not be read, ensure the file is not open in another program");
		}
	}
	
	// This one accepts the input file path as a File
	public Vertices(File in) {
		data=in;
		try {
			numVerts=getNumElements();
			length=(int)data.length();
			vertices=new Point3D[numVerts];
			populateVertexList();
		} catch(java.io.FileNotFoundException e) {
			Window.window.println("ERROR: File "+data+" not found!");
		} catch(java.io.IOException e) {
			Window.window.println("ERROR: File "+data+" could not be read, ensure the file is not open in another program");
		}
	}
	
	public Vertices(byte[] in) {
		int offset=0;
		numVerts=in.length/12;
		length=in.length;
		vertices=new Point3D[numVerts];
		try {
			for(int i=0;i<numVerts;i++) {
				byte[] vertexBytes=new byte[12];
				for(int j=0;j<12;j++) {
					vertexBytes[j]=in[offset+j];
				}
				vertices[i]=new Point3D(vertexBytes);
				offset+=12;
			}
		} catch(InvalidPoint3DException e) {
			Window.window.println("WARNING: Funny lump size in "+data+", ignoring last vertex.");
		}
	}
	
	// METHODS
	
	// +populateVertexList()
	// Parses all data into an array of Point3D.
	public void populateVertexList() throws java.io.FileNotFoundException, java.io.IOException {
		FileInputStream reader=new FileInputStream(data);
		try {
			for(int i=0;i<numVerts;i++) {
				byte[] datain=new byte[12];
				reader.read(datain);
				vertices[i]=new Point3D(datain);
			}
		} catch(InvalidPoint3DException e) {
			Window.window.println("WARNING: Funny lump size in "+data+", ignoring last vertex.");
		}
		reader.close();
	}
	
	// ACCESSORS/MUTATORS
	
	// Returns the length (in bytes) of the lump
	public int getLength() {
		return length;
	}
	
	// Returns the number of vertices.
	public int getNumElements() {
		if(numVerts==0) {
			return length/12;
		} else {
			return numVerts;
		}
	}
	
	public Point3D getVertex(int i) {
		return vertices[i];
	}
	
	public Point3D[] getVertices() {
		return vertices;
	}
}