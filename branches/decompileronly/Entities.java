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
	private int numEnts=0;
	private Entity[] entities;
	
	// CONSTRUCTORS
	
	// This one accepts the lump path as a String
	public Entities(String in) {
		dataFile=new File(in);
		try {
			FileInputStream reader=new FileInputStream(dataFile); // reads the file
			byte[] data=new byte[(int)dataFile.length()];
			reader.read(data);
			length=data.length;
			reader.close();
			numEnts=getNumElements(data);
			entities = new Entity[numEnts];
			populateEntityList(data);
		} catch(java.io.FileNotFoundException e) {
			Window.println("ERROR: File "+dataFile.getPath()+" not found!",0);
		} catch(java.io.IOException e) {
			Window.println("ERROR: File "+dataFile.getPath()+" could not be read, ensure the file is not open in another program",0);
		}
	}
	
	// This one accepts the input file path as a File
	public Entities(File in) {
		dataFile=in;
		try {
			FileInputStream reader=new FileInputStream(dataFile); // reads the file
			byte[] data=new byte[(int)dataFile.length()];
			reader.read(data);
			length=data.length;
			reader.close();
			numEnts=getNumElements(data);
			entities = new Entity[numEnts];
			populateEntityList(data);
		} catch(java.io.FileNotFoundException e) {
			Window.println("ERROR: File "+dataFile.getPath()+" not found!",0);
		} catch(java.io.IOException e) {
			Window.println("ERROR: File "+dataFile.getPath()+" could not be read, ensure the file is not open in another program",0);
		}
	}
	
	// This one accepts a Entities and copies it
	public Entities(Entities in) {
		entities=new Entity[in.getNumElements()];
		numEnts=entities.length;
		for(int i=0;i<numEnts;i++) {
			entities[i]=new Entity(in.getEntity(i));
		}
	}
	
	// This one just takes an array of byte[]
	public Entities(byte[] data) {
		length=data.length;
		numEnts=getNumElements(data);
		entities = new Entity[numEnts];
		populateEntityList(data);
	}
	
	public Entities() {
		length=0;
		numEnts=0;
		entities = new Entity[0];
	}
	
	// METHODS
	
	// -populateEntityList()
	// Uses the instance data to populate an array of Entity classes. This
	// will eat a shitton of memory but works surprisingly fast even for
	// large maps. This method takes advantage of the constructors in the
	// Entity class, look there to see how deep the nested loops really go.
	// Even so, this method is a complete mess, so documentation is provided
	// whenever possible.
	// TODO: Rewrite this, try to make it faster.
	private void populateEntityList(byte[] data) {
		Window.print("Populating entity list... ",1);
		Date begin=new Date();
		// I'd love to use Scanner here, but Scanner doesn't like using delimiters
		// with "{" or "}" in them, which I NEED
		char currentChar; // The current character being read in the file. This is necessary because
		                  // we need to know exactly when the { and } characters occur and capture
								// all text between them.
		int offset=0;
		for(int i=0;i<numEnts;i++) { // For every entity
			String current=""; // This will be the resulting entity, fed into the Entity class
			currentChar=(char)data[offset]; // begin reading the file
			while(currentChar!='{') { // Eat bytes until we find the beginning of an entity structure
				offset++;
				currentChar=(char)data[offset];
			}
			boolean inQuotes=false; // Keep track of whether or not we're in a set of quotation marks.
			// I came across a map where the idiot map maker used { and } in a value. This broke the code prior to revision 55.
			do {
				if(currentChar=='\"') {
					inQuotes=!inQuotes;
				}
				current+=currentChar+""; // adds characters to the current string
				offset++;
				currentChar=(char)data[offset];
			} while(currentChar!='}' || inQuotes); // Read bytes until we find the end of the current entity structure
			current+=currentChar+""; // adds the '}' to the current string
			entities[i]=new Entity(); // puts the resulting String into the constructor of the Entity class
			entities[i].setData(current);
		}
		Date end=new Date();
		Window.println(end.getTime()-begin.getTime()+"ms",1);
	}
	
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
		numEnts++;
		Entity[] newList=new Entity[numEnts];
		for(int i=0;i<numEnts-1;i++) {
			newList[i]=entities[i];
		}
		newList[numEnts-1]=in;
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
		Entity[] newList=new Entity[numEnts-1];
		for(int i=0;i<numEnts-1;i++) {
			if(i<index) {
				newList[i]=entities[i];
			} else {
				newList[i]=entities[i+1];
			}
		}
		numEnts-=1;
		entities=newList;
	}
	
	// +findAllWithAttribute(String, String)
	// Returns an array of indices of the entities with the specified attribute set to
	// the specified value
	public int[] findAllWithAttribute(String attribute, String value) {
		int[] indices;
		int num=0;
		for(int i=0;i<numEnts;i++) {
			if(entities[i].getAttribute(attribute).equalsIgnoreCase(value)) {
				num++;
			}
		}
		indices=new int[num];
		int current=0;
		for(int i=0;i<numEnts && current<num;i++) {
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
	
	// Returns the number of entities.
	public int getNumElements(byte[] data) {
		if (numEnts==0) {
			Window.print("Counting entities... ",1);
			Date begin=new Date();
			int count=0;
			boolean inQuotes=false; // Keep track of whether or not we're in a set of quotation marks.
			// I came across a map where the idiot map maker used { and } in a value. This broke the code prior to revision 55.
			for(int i=0;i<data.length;i++) {
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
			Date end=new Date();
			Window.println(end.getTime()-begin.getTime()+"ms",1);
			return count;
		} else {
			return numEnts;
		}
	}
	
	public int getNumElements() {
		return numEnts;
	}
	
	// Returns a specific entity as an Entity object.
	public Entity getEntity(int i) {
		return entities[i];
	}
	
	// Returns a reference to the entities array
	public Entity[] getEntities() {
		return entities;
	}
}
