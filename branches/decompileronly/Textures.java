// Textures class

// Extends LumpObject with some useful methods for manipulating Texture objects,
// especially when handling them as a group.

public class Textures extends Lump<Texture> {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS

	// CONSTRUCTORS
	
	public Textures(Texture[] elements, int length, int structLength) {
		super(elements, length, structLength);
	}
	
	// METHODS
	public void printTextures() { // FOR DEBUG PURPOSES ONLY
		for(int i=0;i<getElements().length;i++) {
			System.out.println(getElement(i).getName());
		}
	}
	
	// ACCESSORS/MUTATORS
	
	public String getTextureAtOffset(int target) {
		String temp="";
		int offset=0;
		for(int i=0;i<getElements().length;i++) {
			if(offset<target) {
				offset+=getElement(i).getName().length()+1; // Add 1 for the now missing null byte. I really did think of everything! :D
			} else {
				return getElement(i).getName();
			}
		}
		// If we get to this point, the strings ended before target offset was reached
		return null; // Perhaps this will throw an exception down the line? :trollface:
	}
	
	public int getOffsetOf(String inTexture) {
		int offset=0;
		for(int i=0;i<getElements().length;i++) {
			if(!getElement(i).getName().equalsIgnoreCase(inTexture)) {
				offset+=getElement(i).getName().length()+1;
			} else {
				return offset;
			}
		}
		// If we get to here, the requested texture didn't exist.
		return -1; // This will PROBABLY throw an exception later.
	}
}