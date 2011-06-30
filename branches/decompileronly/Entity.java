// Entity class

// This class holds data on ONE entity. It's only really useful when
// used in an array along with many others. Each value is stored as
// a separate attribute, in an array.

// A small note, I was tempted to use an Attribute class and just
// make an array of that, but that's breaking it down too far.

import java.util.Scanner; // Perfect for String handling
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class Entity {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	private String[] attributes;
	private int numAttributes=0;
	
	// CONSTRUCTORS
	
	public Entity(byte[] in) {
		String me="";
		for(int i=0;i<in.length;i++) {
			me+=(char)in[i];
		}
		setData(me);
	}

	public Entity(String me) {
		setData(me);
	}
	
	// METHODS
	
	// deleteAttribute(String)
	// Deletes the specified attribute from the attributes list. If it wasn't found it does nothing.
	public void deleteAttribute(String attribute) {
		int index=0;
		boolean found=false;
		for(index=0;index<numAttributes;index++) {
			try {
				if(attributes[index].substring(0,attribute.length()+2).compareToIgnoreCase("\""+attribute+"\"")==0) {
					found=true;
					break; // If the attribute is found, break the loop since the appropriate index in in the variable "index"
				}
			} catch(StringIndexOutOfBoundsException e) { // for cases where the whole String is shorter than
				;                                         // the name of the attribute we're looking for. Do nothing.
			}
		}
		if (found) {
			deleteAttribute(index);
		}
	}
	
	// deleteAttribute(int)
	// Deletes the attribute at the specified index in the list
	public void deleteAttribute(int index) {
		String[] newList=new String[numAttributes-1];
		for(int i=0;i<numAttributes-1;i++) {
			if(i<index) {
				newList[i]=attributes[i];
			}
			if(i>=index) {
				newList[i]=attributes[i+1];
			}
		}
		attributes=newList;
		numAttributes--;
	}
	
	// addAttribute(String)
	// Simply adds the input String to the attribute list. This String can be anything,
	// even containing newlines or curly braces. BE CAREFUL.
	public void addAttribute(String in) {
		String[] newList=new String[numAttributes+1];
		for(int i=0;i<numAttributes;i++) { // copy the current attribute list
			newList[i]=attributes[i];
		}
		newList[numAttributes]=in;
		attributes=newList;
		numAttributes++;
	}
	
	// addAttribute(String, String)
	// Adds the specified attribute with the specified value to the list.
	public void addAttribute(String attribute, String value) {
		addAttribute("\""+attribute+"\" \""+value+"\"");
	}
	
	// +toByteArray()
	// Returns the entity as an array of bytes. This could be faster than just
	// concatenating all the damn Strings, it depends on how the String class
	// handles the getBytes() conversion. Judging by how much faster my code
	// is than the stupid DataOutputStream, it won't be great. I might have
	// to look at the code in the String class and try to think of a better
	// way than what they have there.
	//
	// This works much faster than toString, especially for entities with a LOT
	// of data. All the String concatenation in toString was building up, with
	// thousands of Strings to stick together and
	public byte[] toByteArray() {
		byte[] out;
		int len=0;
		// Get the lengths of all attributes together
		for(int i=0;i<attributes.length;i++) {
			len+=attributes[i].length()+1; // Gonna need a newline after each attribute or they'll get jumbled together
		}
		out=new byte[len];
		int offset=0;
		for(int i=0;i<attributes.length;i++) { // For each attribute
			for(int j=0;j<attributes[i].length();j++) { // Then for each byte in the attribute
				out[j+offset]=(byte)attributes[i].charAt(j); // add it to the output array
			}
			out[offset+attributes[i].length()]=(byte)0x0A;
			offset+=attributes[i].length()+1;
		}
		return out;
	}

	// +toString()
	// Returns the entity as an ASCII entity structure. The output of this method
	// reads as a complete entity that could be put into a map with no problems,
	// and that will be the primary use case for this method. Be sure to add the
	// newlines and curly braces around the entity if using this method to create
	// a new entities lump file, unless curly braces are part of the attributes
	// array.
	public String toString() {
		String out="";
		for(int i=0;i<attributes.length;i++) {
			if(!(attributes[i].charAt(0)=='}')) {
				out+=attributes[i]+"\n";
			} else { // Character is '}'
				out+=attributes[i];
			}
		}
		return out;
	}
	
	// +isBrushBased()
	// Reads the first character of the model attribute. If it's *, then it's a brush
	// based entity and this method returns true. If not, it returns false.
	public boolean isBrushBased() {
		try {
			return (getAttribute("model").charAt(0)=='*');
		} catch(java.lang.StringIndexOutOfBoundsException e) { // The entity has no "model"
			return false;
		}
	}
	
	// ACCESSORS/MUTATORS
	
	// +setData(String)
	// Used by constructors and can be used by outside classes to set or change the data
	// in this specific entity. It takes a String and parses it into the attributes array.
	// This input String CAN include the { and } from the entity structure, in that it 
	// won't cause any errors in the program. However if and when you want to write the
	// entities back into a lump you must remember whether you included them or not. The
	// behavior of the Lump00 class is not to include them.
	public void setData(String in) {
		Scanner reader=new Scanner(in);
		reader.useDelimiter((char)0x0A+"");
		Scanner counter=new Scanner(in);
		counter.useDelimiter((char)0x0A+"");
		
		numAttributes=0;
		while(counter.hasNext()) {
			counter.next();
			numAttributes++;
		}
		
		attributes=new String[numAttributes];
		for(int i=0;i<numAttributes;i++) {
			String current=reader.next();
			// This will trim all the 0D bytes before the 0A delimiters if they exist,
			// since the Windows newline sequence is 0D0A and all its text editors use
			// that. This keeps the data from getting confusing to this program, but
			// also saves a small amount of space in the output lump itself.
			if(current.charAt(current.length()-1)==(char)0x0D) {
				current=current.substring(0,current.length()-1);
			}
			attributes[i]=current;
		}
	}
	
	// +getAttribute(String)
	// Takes in an attribute as a String and returns the value of that attribute,
	// if it exists. If not, return null. I used to have an exception for this,
	// but always catching it was a pain in the ass. So instead, if the attribute
	// doesn't exist, just return an empty String. I don't think I've ever seen
	// an empty string used as a value in a map before, and either way the setAttribute
	// method will automatically add an attribute if it doesn't exist, and just
	// change it if it does, whether it's empty or not.
	public String getAttribute(String attribute) {
		String output="";
		for(int i=0;i<numAttributes;i++) {
			try {
				if(attributes[i].substring(0,attribute.length()+2).compareToIgnoreCase("\""+attribute+"\"")==0) {
					output=attributes[i].substring(attribute.length()+4,attributes[i].length()-1);
					break;
				}
			} catch(StringIndexOutOfBoundsException e) { // for cases where the whole String is shorter than
				;                                         // the name of the attribute we're looking for. Do nothing.
			}
		}
		return output;
	}
	
	// +getModelNumber()
	// If there's a model number in the attributes list, this method fetches it
	// and returns it. If there is no model defined, or it's not a numerical 
	// value, then -1 is returned.
	public int getModelNumber() {
		int number=-1;
		for(int i=0;i<numAttributes;i++) {
			try {
				if(attributes[i].substring(0,7).compareToIgnoreCase("\"model\"")==0) {
					// This substring skips the "model" "* and gets to the number
					number=Integer.parseInt(attributes[i].substring(10,attributes[i].length()-1));
					break;
				}
			} catch(StringIndexOutOfBoundsException e) { // substring(0,7) was longer than the String
				;
			} catch(NumberFormatException e) { // The model wasn't a number
				;
			}
		}
		return number;
	}
	
	// setAttribute()
	// Set an attribute. If it doesn't exist, it is added. If it does, it is
	// overwritten with the new one, since that's much easier to do than edit
	// the preexisting one.
	public void setAttribute(String attribute, String value) {
		boolean done=false;
		for(int i=0;i<numAttributes && !done;i++) {
			try {
				if(attributes[i].substring(0,attribute.length()+2).compareToIgnoreCase("\""+attribute+"\"")==0) {
					attributes[i]="\""+attribute+"\" \""+value+"\"";
					done=true;
				}
			} catch(StringIndexOutOfBoundsException e) {
				;
			}
		}
		if(!done) {
			addAttribute(attribute, value);
		}
	}
	
	// getOrigin()
	// Returns the three components of the entity's "origin" attribute as an array
	// of three doubles. Since everything is a string anyway, I can be as precise
	// as I want.
	public double[] getOrigin() {
		double[] output=new double[3]; // initializes to {0,0,0}
		if(!getAttribute("origin").equals("")) {
			String origin=getAttribute("origin");
			Scanner numGetter=new Scanner(origin);
			for(int i=0;i<3&&numGetter.hasNext();i++) {
				output[i]=numGetter.nextDouble();
			}
		}
		return output;
	}
	
	// getNumAttributes()
	// Returns the number of attributes in the entity
	public int getNumAttributes() {
		return attributes.length;
	}
}
