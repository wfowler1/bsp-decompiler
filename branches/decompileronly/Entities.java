// Entities class

// This class handles and maintains an array of entities

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.util.Scanner;

public class Entities {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	private File data;
	private int numEnts=0;
	private Entity[] entities;
	
	// CONSTRUCTORS
	
	// This one accepts the lump path as a String
	public Entities(String in) {
		data=new File(in);
		try {
			numEnts=getNumElements();
			entities = new Entity[numEnts];
			populateEntityList();
		} catch(java.io.FileNotFoundException e) {
			Window.window.println("ERROR: File "+data+" not found!");
		} catch(java.io.IOException e) {
			Window.window.println("ERROR: File "+data+" could not be read, ensure the file is not open in another program");
		}
	}
	
	// This one accepts the input file path as a File
	public Entities(File in) {
		data=in;
		try {
			numEnts=getNumElements();
			entities = new Entity[numEnts];
			populateEntityList();
		} catch(java.io.FileNotFoundException e) {
			Window.window.println("ERROR: File "+data+" not found!");
		} catch(java.io.IOException e) {
			Window.window.println("ERROR: File "+data+" could not be read, ensure the file is not open in another program");
		}
	}
	
	// This one accepts a Entities and copies it
	public Entities(Entities in) {
		entities=new Entity[in.getNumElements()];
		numEnts=entities.length;
		for(int i=0;i<numEnts;i++) {
			entities[i]=new Entity(in.getEntity(i).toString());
		}
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
	private void populateEntityList() throws java.io.FileNotFoundException, java.io.IOException {
		Window.window.println("Populating entity list...");
		// I'd love to use Scanner here, but Scanner doesn't like using delimiters
		// with "{" or "}" in them, which I fucking NEED
		FileInputStream reader=new FileInputStream(data); // reads the file
		char currentChar; // The current character being read in the file. This is necessary because
		                  // we need to know exactly when the { and } characters occur and capture
								// all text between them.
		for(int i=0;i<numEnts;i++) { // For every entity
			String current=""; // This will be the resulting entity, fed into the Entity class
			currentChar=(char)reader.read(); // begin reading the file
			while(currentChar!='{') { // Eat bytes until we find the beginning of an entity structure
				currentChar=(char)reader.read();
			}
			reader.read(); // This will eat a 0x0A
			currentChar=(char)reader.read(); // reads the first character after the 0x0A, which will be 
			                                 // a quote (though it doesn't matter what the hell it is)
			do {
				current+=currentChar+""; // adds characters to the current string
				currentChar=(char)reader.read();
			} while(currentChar!='}'); // Read bytes until we find the end of the current entity structure
			entities[i]=new Entity(current); // puts the resulting String into the constructor of the Entity class
		}
		reader.close();
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
		Scanner reader=new Scanner(in);
		reader.useDelimiter("");
		char currentChar; // The current character being read in the String.
		String current=""; // This will be the resulting entity, fed into the Entity class
		currentChar=reader.next().charAt(0); // Stupid Scanner has no nextChar() method
		while(currentChar!='{') { // Eat bytes until we find the beginning of an entity structure
			currentChar=reader.next().charAt(0);
		}
		reader.next(); // This will eat a 0x0A
		currentChar=reader.next().charAt(0); // reads the first character after the 0x0A, which will be 
		                                     // a quote (though it doesn't matter what the hell it is)
		do {
			current+=currentChar+""; // adds characters to the current string
			currentChar=reader.next().charAt(0);
		} while(currentChar!='}'); // Read bytes until we find the end of the current entity structure
		Entity newEnt=new Entity(current); // puts the resulting String into the constructor of the Entity class
		add(newEnt);
		reader.close();
	}
	
	// +add(Entity)
	// This is definitely the easiest to do. Adds an entity to the list
	// which is already of type Entity. It can be assumes it's already
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
	
	// +add(Entities)
	// Adds every entity in the passed Entities into this one
	// This was the hardest lump to add the ability to combine, since all
	// references to other lumps are Strings contained in ATTRIBUTES. agh
	public void add(Entities in) throws java.io.FileNotFoundException, java.io.IOException {
		Entity[] newlist=new Entity[numEnts+in.getNumElements()];
		for(int i=0;i<numEnts;i++) { // copy the entities from this lump into a new array
			newlist[i]=entities[i];
		}
		File myLump14=new File(data.getParent()+"\\14 - Models.hex");
		int num14objs=(int)myLump14.length()/56;
		for(int i=0;i<in.getEntities().length;i++) {
			int oldModelNumber=in.getEntity(i).getModelNumber();
			if(oldModelNumber>0) {
				int newModelNumber=in.getEntity(i).getModelNumber()+num14objs-1; // Must subtract 1 from model number,
				in.getEntity(i).setAttribute("model", "*"+newModelNumber);       // since model 0 is the world, and that
			}                                                                   // is getting combined into one model
		}                                                                      // for both maps
		for(int i=0;i<in.getNumElements();i++) {
			newlist[i+numEnts]=in.getEntity(i);
		}
		
		numEnts=numEnts+in.getNumElements();
		entities=newlist;
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
	
	// Save(String)
	// Saves the lump to the specified path.
	// Handling file I/O with Strings is generally a bad idea. If you have maybe a couple hundred
	// Strings to write then it'll probably be okay, but when you have on the order of 10,000 Strings
	// it gets VERY slow, even if you concatenate them all before writing.
	public void save(String path) {
		File newFile;
		if(path.substring(path.length()-4).equalsIgnoreCase(".map")) {
			newFile=new File(path);
		} else {
			newFile=new File(path+"\\00 - Entities.txt");
		}
		try {
			if(!newFile.exists()) {
				newFile.createNewFile();
			} else {
				newFile.delete();
				newFile.createNewFile();
			}
			
			// PrintWriter entityWriter=new PrintWriter(newFile);
			FileOutputStream entityWriter=new FileOutputStream(newFile);
			for(int i=0;i<numEnts;i++) {
				byte[] temp;
				if(!path.substring(path.length()-4).equals(".map")) {
					temp=new byte[2];
					temp[0]=(byte)'{';
					temp[1]=(byte)0x0A;
				} else {
					String tempString="{ // Entity "+i+"\n";
					temp=tempString.getBytes();
				}
				entityWriter.write(temp);
				entityWriter.write(entities[i].toByteArray());
				byte [] temp2=new byte[2];
				temp2[0]=(byte)'}';
				temp2[1]=(byte)0x0A;
				entityWriter.write(temp2);
			}
			if(!path.substring(path.length()-4).equals(".map")) {
				byte[] temp=new byte[1];
				temp[0]=(byte)0x00;
				entityWriter.write(temp); // The entities lump always ends with a 00 byte,
				                          // otherwise the game engine could start reading into
				                          // the next lump, looking for another entity. It's
				                          // kind of stupid that way, since lump sizes are
				                          // clearly defined in the BSP header.
			}
			entityWriter.close();
		} catch(java.io.IOException e) {
			Window.window.println("ERROR: Could not save "+newFile+", ensure the file is not open in another program and the path "+path+" exists");
		}
	}
	
	// save()
	// Saves the lump, overwriting the one data was read from
	public void save() {
		save(data.getParent());
	}
	
	// ACCESSORS/MUTATORS
		
	// Returns the length (in bytes) of the lump
	public long getLength() {
		return data.length();
	}
	
	// Returns the number of entities.
	public int getNumElements() {
		if (numEnts==0) {
			Window.window.println("Counting entities...");
			int count=0;
			try {
				FileInputStream fileReader = new FileInputStream(data);
				for(int i=0;i<data.length();i++) {
					if(fileReader.read() == '{') {
						count++;
					}
				}
				fileReader.close();
			} catch(java.io.IOException e) {
				Window.window.println("Unable to read Entities.txt!");
			}
			return count;
		} else {
			return numEnts;
		}
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
