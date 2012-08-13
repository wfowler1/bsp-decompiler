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
	private Vector3D[] vertices;
	
	// CONSTRUCTORS
	
	// This one accepts the lump path as a String
	public Vertices(String in) {
		data=new File(in);
		try {
			numVerts=getNumElements();
			length=(int)data.length();
			vertices=new Vector3D[numVerts];
			populateVertexList();
		} catch(java.io.FileNotFoundException e) {
			Window.println("ERROR: File "+data.getName()+" not found!",0);
		} catch(java.io.IOException e) {
			Window.println("ERROR: File "+data.getName()+" could not be read, ensure the file is not open in another program",0);
		}
	}
	
	// This one accepts the input file path as a File
	public Vertices(File in) {
		data=in;
		try {
			numVerts=getNumElements();
			length=(int)data.length();
			vertices=new Vector3D[numVerts];
			populateVertexList();
		} catch(java.io.FileNotFoundException e) {
			Window.println("ERROR: File "+data.getName()+" not found!",0);
		} catch(java.io.IOException e) {
			Window.println("ERROR: File "+data.getName()+" could not be read, ensure the file is not open in another program",0);
		}
	}
	
	public Vertices(byte[] in) {
		int offset=0;
		numVerts=in.length/12;
		length=in.length;
		vertices=new Vector3D[numVerts];
		for(int i=0;i<numVerts;i++) {
			byte[] vertexBytes=new byte[12];
			for(int j=0;j<12;j++) {
				vertexBytes[j]=in[offset+j];
			}
			vertices[i]=new Vector3D(vertexBytes);
			offset+=12;
		}
	}
	
	// METHODS
	
	// +populateVertexList()
	// Parses all data into an array of Vector3D.
	public void populateVertexList() throws java.io.FileNotFoundException, java.io.IOException {
		FileInputStream reader=new FileInputStream(data);
		for(int i=0;i<numVerts;i++) {
			byte[] datain=new byte[12];
			reader.read(datain);
			vertices[i]=new Vector3D(datain);
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
	
	public Vector3D getVertex(int i) {
		return vertices[i];
	}
	
	public Vector3D[] getVertices() {
		return vertices;
	}
}