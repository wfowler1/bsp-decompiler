// Lump class
// My first successful attempt to do away with the many list classes I had, using generics.
// If special treatment is needed for a list, another class can be made to extend this one.

public class Lump<T extends LumpObject> {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	private int length;
	private T[] elements;
	private int structLength=0;

	// CONSTRUCTORS
	
	public Lump(T[] elements, int length, int structLength) {
		this.elements=elements;
		this.structLength=structLength;
		this.length=length;
	}
	
	// METHODS
	public boolean hasFunnySize() {
		if(structLength < 1 || elements.length < 1) {
			return false;
		}
		if(length%elements.length!=0) {
			return true;
		}
		return false;
	}
	
	// +add(T)
	// Adds another T to the array
	public void add(T in) {
		T[] newList=(T[])(new LumpObject[elements.length+1]);
		for(int i=0;i<elements.length;i++) {
			newList[i]=elements[i];
		}
		newList[elements.length]=in;
		elements=newList;
	}
	
	// +delete(int)
	// Deletes the entity at the specified index
	public void delete(int index) {
		T[] newList=(T[])(new LumpObject[elements.length-1]);
		for(int i=0;i<elements.length-1;i++) {
			if(i<index) {
				newList[i]=elements[i];
			} else {
				newList[i]=elements[i+1];
			}
		}
		elements=newList;
	}
	
	// ACCESSORS/MUTATORS
	
	public int length() {
		return length;
	}
	
	// Returns the number of elements.
	public int size() {
		return elements.length;
	}
	
	public T getElement(int i) {
		return elements[i];
	}
	
	public T[] getElements() {
		return elements;
	}
	
	public void setElements(T[] in) {
		elements=in;
	}
	
	public void setLength(int in) {
		length=in;
	}
}