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
			Window.window.println("ERROR: File "+dataFile+" not found!");
		} catch(java.io.IOException e) {
			Window.window.println("ERROR: File "+dataFile+" could not be read, ensure the file is not open in another program");
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
			Window.window.println("ERROR: File "+dataFile+" not found!");
		} catch(java.io.IOException e) {
			Window.window.println("ERROR: File "+dataFile+" could not be read, ensure the file is not open in another program");
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
		Window.window.print("Populating entity list... ");
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
			do {
				current+=currentChar+""; // adds characters to the current string
				offset++;
				currentChar=(char)data[offset];
			} while(currentChar!='}'); // Read bytes until we find the end of the current entity structure
			current+=currentChar+""; // adds the '}' to the current string
			entities[i]=new Entity(current); // puts the resulting String into the constructor of the Entity class
		}
		Date end=new Date();
		Window.window.println(end.getTime()-begin.getTime()+"ms");
	}
	
	// +printEnts()
	// prints all parsed entities into the console
	public void printEnts() {
		for(int i=0;i<numEnts;i++) {
			Window.window.print(entities[i].toString()+"\n");
		}
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
		Entity newEnt=new Entity(in); // puts the String into the constructor of the Entity class
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
	
	// Save(String)
	// Saves the lump to the specified path.
	// Handling file I/O with Strings is generally a bad idea. If you have maybe a couple hundred
	// Strings to write then it'll probably be okay, but when you have on the order of 10,000 Strings
	// it gets VERY slow, even if you concatenate them all before writing.
	public void save(String path) {
		File newFile;
		if(path.substring(path.length()-4).equalsIgnoreCase(".map") || path.substring(path.length()-4).equalsIgnoreCase(".vmf")) {
			newFile=new File(path);
		} else {
			newFile=new File(path+"\\00 - Entities.txt");
		}
		try {
			File absolutepath=new File(newFile.getParent()+"\\");
			if(!absolutepath.exists()) {
				absolutepath.mkdir();
			}
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
				if(path.substring(path.length()-4).equals(".map")) {
					String tempString="{ // Entity "+i+""+(char)0x0D+(char)0x0A;
					temp=tempString.getBytes();
				} else {
					if(path.substring(path.length()-4).equals(".vmf")) {
						if(entities[i].getAttribute("classname").equalsIgnoreCase("worldspawn")) {
							String tempString="versioninfo"+(char)0x0D+(char)0x0A+"{"+(char)0x0D+(char)0x0A+"	\"editorversion\" \"400\""+(char)0x0D+(char)0x0A+"	\"editorbuild\" \"3325\""+(char)0x0D+(char)0x0A+"	\"mapversion\" \"0\""+(char)0x0D+(char)0x0A+"	\"formatversion\" \"100\""+(char)0x0D+(char)0x0A+"	\"prefab\" \"0\""+(char)0x0D+(char)0x0A+"}"+(char)0x0D+(char)0x0A+"";
							tempString+="visgroups"+(char)0x0D+(char)0x0A+"{"+(char)0x0D+(char)0x0A+"}"+(char)0x0D+(char)0x0A+"";
							tempString+="viewsettings"+(char)0x0D+(char)0x0A+"{"+(char)0x0D+(char)0x0A+"	\"bSnapToGrid\" \"1\""+(char)0x0D+(char)0x0A+"	\"bShowGrid\" \"1\""+(char)0x0D+(char)0x0A+"	\"bShowLogicalGrid\" \"0\""+(char)0x0D+(char)0x0A+"	\"nGridSpacing\" \"64\""+(char)0x0D+(char)0x0A+"	\"bShow3DGrid\" \"0\""+(char)0x0D+(char)0x0A+"}"+(char)0x0D+(char)0x0A+"";
							tempString+="world"+(char)0x0D+(char)0x0A+"";
							temp=tempString.getBytes();
							entities[i].setAttribute("mapversion", "638");
						} else {
							String tempString="entity"+(char)0x0D+(char)0x0A+"";
							temp=tempString.getBytes();
						}
						entityWriter.write(temp);
					}
				}
				entityWriter.write(entities[i].toByteArray(path.substring(path.length()-4).equals(".vmf"), i));
			}
			if(!(path.substring(path.length()-4).equalsIgnoreCase(".map") || path.substring(path.length()-4).equalsIgnoreCase(".vmf"))) {
				byte[] temp= { (byte)0x00 };
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
		save(dataFile.getParent());
	}
	
	// ACCESSORS/MUTATORS
		
	// Returns the length (in bytes) of the lump
	public int getLength() {
		return length;
	}
	
	// Returns the number of entities.
	public int getNumElements(byte[] data) {
		if (numEnts==0) {
			Window.window.print("Counting entities... ");
			Date begin=new Date();
			int count=0;
			for(int i=0;i<data.length;i++) {
				if(data[i] == '{') {
					count++;
				}
			}
			Date end=new Date();
			Window.window.println(end.getTime()-begin.getTime()+"ms");
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
