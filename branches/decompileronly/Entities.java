// Entities class

// This class handles and maintains an array of entities

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.util.Scanner;
import java.util.Date;

public class Entities {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	private File dataFile;
	private int length;
	private Entity[] entities;
	
	// CONSTRUCTORS
	
	// This one accepts the lump path as a String
	public Entities(String in) throws java.lang.InterruptedException {
		new Entities(new File(in));
	}
	
	// This one accepts the input file path as a File
	public Entities(File in) throws java.lang.InterruptedException {
		dataFile=in;
		try {
			FileInputStream reader=new FileInputStream(dataFile); // reads the file
			byte[] data=new byte[(int)dataFile.length()];
			reader.read(data);
			new Entities(data);
		} catch(java.io.FileNotFoundException e) {
			Window.println("ERROR: File "+dataFile.getPath()+" not found!",Window.VERBOSITY_ALWAYS);
		} catch(java.io.IOException e) {
			Window.println("ERROR: File "+dataFile.getPath()+" could not be read, ensure the file is not open in another program",Window.VERBOSITY_ALWAYS);
		}
	}
	
	// This one accepts an Entities and copies it
	public Entities(Entities in) {
		entities=new Entity[in.length()];
		for(int i=0;i<entities.length;i++) {
			entities[i]=new Entity(in.getElement(i));
		}
	}
	
	// This one just takes an array of byte[]
	public Entities(byte[] data) throws java.lang.InterruptedException {
		length=data.length;
		int count=0;
		boolean inQuotes=false; // Keep track of whether or not we're currently in a set of quotation marks.
		// I came across a map where the idiot map maker used { and } within a value. This broke the code prior to revision 55.
		for(int i=0;i<data.length;i++) {
			if(Thread.currentThread().interrupted()) {
				throw new java.lang.InterruptedException("while counting Entities");
			}
			if(inQuotes) {
				if(data[i]=='\"' && inQuotes) {
					inQuotes=false;
				}
			} else {
				if(data[i]=='\"') {
					inQuotes=true;
				} else {
					if(data[i] == '{') {
						count++;
					}
				}
			}
		}
		entities = new Entity[count];
		// I'd love to use Scanner here, but Scanner doesn't like using delimiters
		// with "{" or "}" in them, which I NEED
		char currentChar; // The current character being read in the file. This is necessary because
		                  // we need to know exactly when the { and } characters occur and capture
								// all text between them.
		int offset=0;
		for(int i=0;i<entities.length;i++) { // For every entity
			if(Thread.currentThread().interrupted()) {
				throw new java.lang.InterruptedException("while populating Entity array");
			}
			String current=""; // This will be the resulting entity, fed into the Entity class
			currentChar=(char)data[offset]; // begin reading the file
			while(currentChar!='{') { // Eat bytes until we find the beginning of an entity structure
				offset++;
				currentChar=(char)data[offset];
			}
			inQuotes=false;
			do {
				if(currentChar=='\"') {
					inQuotes=!inQuotes;
				}
				current+=currentChar+""; // adds characters to the current string
				offset++;
				currentChar=(char)data[offset];
			} while(currentChar!='}' || inQuotes); // Read bytes until we find the end of the current entity structure
			current+=currentChar+""; // adds the '}' to the current string
			entities[i]=new Entity();
			entities[i].setData(current);
		}
	}
	
	public Entities() {
		length=0;
		entities = new Entity[0];
	}
	
	// METHODS
	
	// +add(String)
	// Parses the string and adds it to the entity list.
	// input Strings should be of the format:
	//   {0x0A
	//   "attribute" "value"0x0A
	//   "anotherattribute" "more values"0x0A
	//   etc.0x0A
	//   }
	public void add(String in) {
		Entity newEnt=new Entity(); // puts the String into the constructor of the Entity class
		newEnt.setData(in);
		add(newEnt);
	}
	
	// +add(Entity)
	// This is definitely the easiest to do. Adds an entity to the list
	// which is already of type Entity. It can be assumed it's already
	// been parsed and is valid the way it is.
	public void add(Entity in) {
		Entity[] newList=new Entity[entities.length+1];
		for(int i=0;i<entities.length;i++) {
			newList[i]=entities[i];
		}
		newList[entities.length]=in;
		entities=newList;
	}
	
	// +deleteAllOfType(String, String)
	// Deletes all entities with attribute set to value
	public void deleteAllWithAttribute(String attribute, String value) {
		deleteEnts(findAllWithAttribute(attribute, value));
	}
	
	// +deleteEnts(int[])
	// Deletes the entities specified at all indices in the int[] array.
	public void deleteEnts(int[] in) {
		for(int i=0;i<in.length;i++) { // For each element in the array
			delete(in[i]); // Delete the element
			for(int j=i+1;j<in.length;j++) { // for each element that still needs to be deleted
				if(in[i]<in[j]) { // if the element that still needs deleting has an index higher than what was just deleted
					in[j]--; // Subtract one from that element's index to compensate for the changed list
				}
			}
		}
	}
	
	// +delete(int)
	// Deletes the entity at the specified index
	public void delete(int index) {
		Entity[] newList=new Entity[entities.length-1];
		for(int i=0;i<entities.length-1;i++) {
			if(i<index) {
				newList[i]=entities[i];
			} else {
				newList[i]=entities[i+1];
			}
		}
		entities=newList;
	}
	
	// +findAllWithAttribute(String, String)
	// Returns an array of indices of the entities with the specified attribute set to
	// the specified value
	public int[] findAllWithAttribute(String attribute, String value) {
		int[] indices;
		int num=0;
		for(int i=0;i<entities.length;i++) {
			if(entities[i].getAttribute(attribute).equalsIgnoreCase(value)) {
				num++;
			}
		}
		indices=new int[num];
		int current=0;
		for(int i=0;i<entities.length && current<num;i++) {
			if(entities[i].getAttribute(attribute).equalsIgnoreCase(value)) {
				indices[current]=i;
				current++;
			}
		}
		return indices;
	}
	
	// ACCESSORS/MUTATORS
		
	// Returns the length (in bytes) of the lump
	public int getLength() {
		return length;
	}
	
	public int length() {
		return entities.length;
	}
	
	// Returns a specific entity as an Entity object.
	public Entity getElement(int i) {
		return entities[i];
	}
	
	// Returns a reference to the entities array
	public Entity[] getElements() {
		return entities;
	}
}
