// Lump14 class

// This class holds an array of Model objects. Each model is a crazy
// amount of data.

import java.io.FileInputStream;
import java.io.File;

public class Lump14 {

	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	private File data;
	private int numModels=0;
	private Model[] models;
	
	// CONSTRUCTORS
	
	// This one accepts the lump path as a String
	public Lump14(String in) {
		data=new File(in);
		try {
			numModels=getNumElements();
			models=new Model[numModels];
			populateModelList();
		} catch(java.io.FileNotFoundException e) {
			System.out.println("ERROR: File "+data+" not found!");
		} catch(java.io.IOException e) {
			System.out.println("ERROR: File "+data+" could not be read, ensure the file is not open in another program");
		}
	}
	
	// This one accepts the input file path as a File
	public Lump14(File in) {
		data=in;
		try {
			numModels=getNumElements();
			models=new Model[numModels];
			populateModelList();
		} catch(java.io.FileNotFoundException e) {
			System.out.println("ERROR: File "+data+" not found!");
		} catch(java.io.IOException e) {
			System.out.println("ERROR: File "+data+" could not be read, ensure the file is not open in another program");
		}
	}
	
	// METHODS
	
	// -populateModelList()
	// This method uses the data file in the instance data to
	// populate an array of Model objects which are the models
	// of the map.
	private void populateModelList() throws java.io.FileNotFoundException, java.io.IOException {
		FileInputStream reader=new FileInputStream(data);
		try {
			for(int i=0;i<numModels;i++) {
				byte[] datain=new byte[56];
				reader.read(datain);
				models[i]=new Model(datain);
			}
			reader.close();
		} catch(InvalidModelException e) {
			System.out.println("WARNING: Funny lump size in "+data+", ignoring last model.");
		}
	}
	
	// ACCESSORS/MUTATORS
	
	// Returns the length (in bytes) of the lump
	public long getLength() {
		return data.length();
	}
	
	// Returns the number of models.
	public int getNumElements() {
		if(numModels==0) {
			return (int)data.length()/56;
		} else {
			return numModels;
		}
	}
	
	public Model[] getModels() {
		return models;
	}
	
	public Model getModel(int i) {
		return models[i];
	}
	
	public void setModel(int i, Model in) {
		models[i]=in;
	}
}
