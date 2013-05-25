// Entities class

// This class extends the Lump class, providing methods which are only useful
// for a group of Entity objects (and not really anything else).

public class Entities extends Lump<Entity> {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	// CONSTRUCTORS
	
	// This one accepts an Entities and copies it
	public Entities(Entities in) {
		super(new Entity[0], 0, 0);
		Entity[] entities=new Entity[in.size()];
		for(int i=0;i<entities.length;i++) {
			entities[i]=new Entity(in.getElement(i));
		}
		setElements(entities);
		setLength(in.length());
	}
	
	public Entities(Entity[] in, int length) {
		super(in, length, 0);
	}
	
	public Entities() {
		super(new Entity[0], 0, 0);
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
	
	// +findAllWithAttribute(String, String)
	// Returns an array of indices of the getElements() with the specified attribute set to
	// the specified value
	public int[] findAllWithAttribute(String attribute, String value) {
		int[] indices = new int[0];
		for(int i=0;i<size();i++) {
			if(getElement(i).attributeIs(attribute, value)) {
				int[] newList = new int[indices.length+1];
				for(int j=0;j<indices.length;j++) {
					newList[j]=indices[j];
				}
				newList[newList.length-1]=i;
				indices=newList;
			}
		}
		return indices;
	}
	
	// Returns the actual getElements() with the specified field
	public Entity[] returnAllWithAttribute(String attribute, String value) {
		int[] indices = findAllWithAttribute(attribute, value);
		Entity[] ents = new Entity[indices.length];
		for(int i=0;i<ents.length;i++) {
			ents[i] = getElement(indices[i]);
		}
		return ents;
	}
	
	// Returns all getElements() with the specified targetname
	public Entity[] returnAllWithName(String targetname) {
		return returnAllWithAttribute("targetname", targetname);
	}
	
	// Returns ONE (the first) entity with the specified targetname
	public Entity returnWithName(String targetname) {
		for(int i=0;i<size();i++) {
			if(getElement(i).attributeIs("targetname", targetname)) {
				return getElement(i);
			}
		}
		return null;
	}
	
	// ACCESSORS/MUTATORS
	
}
