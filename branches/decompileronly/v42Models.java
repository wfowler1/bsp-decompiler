// v42Models class

// This class holds an array of Model objects. Each model is a crazy
// amount of data.

import java.io.FileInputStream;
import java.io.File;

public class v42Models {

	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	private File data;
	private int length;
	private int numModels=0;
	private v42Model[] models;
	
	// CONSTRUCTORS
	
	// This one accepts the lump path as a String
	public v42Models(String in) {
		data=new File(in);
		length=(int)data.length();
		try {
			numModels=getNumElements();
			models=new v42Model[numModels];
			populateModelList();
		} catch(java.io.FileNotFoundException e) {
			Window.println("ERROR: File "+data.getPath()+" not found!",0);
		} catch(java.io.IOException e) {
			Window.println("ERROR: File "+data.getPath()+" could not be read, ensure the file is not open in another program",0);
		}
	}
	
	// This one accepts the input file path as a File
	public v42Models(File in) {
		data=in;
		length=(int)data.length();
		try {
			numModels=getNumElements();
			models=new v42Model[numModels];
			populateModelList();
		} catch(java.io.FileNotFoundException e) {
			Window.println("ERROR: File "+data.getPath()+" not found!",0);
		} catch(java.io.IOException e) {
			Window.println("ERROR: File "+data.getPath()+" could not be read, ensure the file is not open in another program",0);
		}
	}
	
	public v42Models(byte[] in) {
		int offset=0;
		numModels=in.length/56;
		length=in.length;
		models=new v42Model[numModels];
		for(int i=0;i<numModels;i++) {
			byte[] modelBytes=new byte[56];
			for(int j=0;j<56;j++) {
				modelBytes[j]=in[offset+j];
			}
			models[i]=new v42Model(modelBytes);
			offset+=56;
		}
	}
	
	// METHODS
	
	// -populateModelList()
	// This method uses the data file in the instance data to
	// populate an array of Model objects which are the models
	// of the map.
	private void populateModelList() throws java.io.FileNotFoundException, java.io.IOException {
		FileInputStream reader=new FileInputStream(data);
		for(int i=0;i<numModels;i++) {
			byte[] datain=new byte[56];
			reader.read(datain);
			models[i]=new v42Model(datain);
		}
		reader.close();
	}
	
	// ACCESSORS/MUTATORS
	
	// Returns the length (in bytes) of the lump
	public int getLength() {
		return length;
	}
	
	// Returns the number of models.
	public int getNumElements() {
		if(numModels==0) {
			return length/56;
		} else {
			return numModels;
		}
	}
	
	public v42Model[] getModels() {
		return models;
	}
	
	public v42Model getModel(int i) {
		return models[i];
	}
	
	public void setModel(int i, v42Model in) {
		models[i]=in;
	}
}
