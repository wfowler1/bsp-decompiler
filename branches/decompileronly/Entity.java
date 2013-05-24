// Entity class

// This class holds data on ONE entity. It's only really useful when
// used in an array along with many others. Each value is stored as
// a separate attribute, in an array.

// A small note, I was tempted to use an Attribute class and just
// make an array of that, but that's breaking it down too far.

// I've also added the ability to add MAPBrush objects to the entity,
// to be processed later on.

import java.util.Scanner; // Perfect for String handling

public class Entity {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	private String[] attributes=new String[0];
	private String[] connections=new String[0];
	private MAPBrush[] brushes=new MAPBrush[0];
	
	// For building more Source engine connections, a simple state machine.
	private boolean fired=false;
	
	// CONSTRUCTORS
	
	public Entity(byte[] in) {
		String me="";
		for(int i=0;i<in.length;i++) {
			me+=(char)in[i];
		}
		setData(me);
	}

	public Entity(String classname) {
		attributes=new String[3];
		attributes[0]="{";
		attributes[1]="\"classname\" \""+classname+"\"";
		attributes[2]="}";
	}
	
	public Entity(String[] atts) {
		attributes=new String[atts.length+2];
		for(int i=0;i<atts.length;i++) {
			attributes[i+1]=atts[i];
		}
		attributes[0]="{";
		attributes[attributes.length-1]="}";
	}
	
	public Entity() {
		
	}
	
	public Entity(Entity copy) {
		attributes=new String[copy.getNumAttributes()];
		for(int i=0;i<attributes.length;i++) {
			attributes[i]=copy.getAttribute(i);
		}
		brushes=new MAPBrush[copy.getBrushes().length];
		for(int i=0;i<brushes.length;i++) {
			brushes[i]=new MAPBrush(copy.getBrush(i));
		}
	}
	
	// METHODS
	
	// renameAttribute(String, String)
	// Renames the specified attribute to the second String.
	public void renameAttribute(String attribute, String to) {
		for(int i=0;i<attributes.length;i++) {
			try {
				if(attributes[i].substring(0,attribute.length()+2).compareToIgnoreCase("\""+attribute+"\"")==0) {
					String value=getAttribute(attribute);
					attributes[i]="\""+to+"\" \""+value+"\"";
					break; // If the attribute is found, break the loop. The attribute should only exist once.
				}
			} catch(StringIndexOutOfBoundsException e) { // for cases where the whole String is shorter than
				;                                         // the name of the attribute we're looking for. Do nothing.
			}
		}
	}
	
	// deleteAttribute(String)
	// Deletes the specified attribute from the attributes list. If it wasn't found it does nothing.
	public void deleteAttribute(String attribute) {
		int index=findAttributeIndex(attribute);
		if(index>-1) {
			deleteAttribute(index);
		}
	}
	
	// deleteAttribute(int)
	// Deletes the attribute at the specified index in the list
	public void deleteAttribute(int index) {
		String[] newList=new String[attributes.length-1];
		for(int i=0;i<attributes.length-1;i++) {
			if(i<index) {
				newList[i]=attributes[i];
			}
			if(i>=index) {
				newList[i]=attributes[i+1];
			}
		}
		attributes=newList;
	}
	
	// addAttribute(String)
	// Simply adds the input String to the attribute list. This String can be anything,
	// even containing newlines or curly braces. BE CAREFUL.
	public void addAttribute(String in) {
		String[] newList=new String[attributes.length+1];
		for(int i=0;i<attributes.length;i++) { // copy the current attribute list
			newList[i]=attributes[i];
		}
		newList[attributes.length]=in;
		attributes=newList;
	}
	
	// addAttributeInside(String)
	// Does the same as above, but adds the attribute within the outermost pair
	// of { } braces, if the entity has the braces as attributes
	public void addAttributeInside(String in) {
		int addAt=attributes.length-1;
		try {
		for(;addAt>=0;addAt--) {
			if(attributes[addAt].equals("}")) {
				break;
			}
		}
		String[] newList=new String[attributes.length+1];
		for(int i=0;i<newList.length;i++) {
			if(i<addAt) {
				newList[i]=attributes[i];
			} else {
				if(i==addAt) {
					newList[i]=in;
				} else {
					newList[i]=attributes[i-1];
				}
			}
		}
		attributes=newList;
		} catch(java.lang.NullPointerException e) {
			toString();
		}
	}
	
	// addAttribute(String, String)
	// Adds the specified attribute with the specified value to the list.
	public void addAttribute(String attribute, String value) {
		addAttribute("\""+attribute+"\" \""+value+"\"");
	}
	
	// addAttributeInside(String, String)
	// Adds the specified attribute with the specified value to the list, within the outermost
	// set of { } braces.
	public void addAttributeInside(String attribute, String value) {
		addAttributeInside("\""+attribute+"\" \""+value+"\"");
	}
	
	// addBrush(MAPBrush)
	// Adds the brush to the entity as a "tie". Most of these brushes will be in a worldspawn
	// entity, but it is also used for brush based entities.
	public void addBrush(MAPBrush in) {
		MAPBrush[] newList=new MAPBrush[brushes.length+1];
		for(int i=0;i<brushes.length;i++) {
			newList[i]=brushes[i];
		}
		newList[newList.length-1]=in;
		brushes=newList;
	}
	
	// deleteBrush(int)
	// Deletes the brush at int
	public void deleteBrush(int index) {
		MAPBrush[] newList=new MAPBrush[brushes.length-1];
		for(int i=0;i<brushes.length-1;i++) {
			if(i<index) {
				newList[i]=brushes[i];
			}
			if(i>=index) {
				newList[i]=brushes[i+1];
			}
		}
		brushes=newList;
	}

	// +toString()
	// Returns the entity as an ASCII entity structure. The output of this method
	// reads as a complete entity that could be put into a map with no problems,
	// and that will be the primary use case for this method. Be sure to add the
	// newlines and curly braces around the entity if using this method to create
	// a new entities lump file, unless curly braces are part of the attributes
	// array.
	@Deprecated
	public String toString() {
		String out="";
		for(int i=0;i<attributes.length;i++) {
			try {
				if(!(attributes[i].charAt(0)=='}')) {
					out+=attributes[i]+""+(char)0x0D+(char)0x0A;
				} else { // Character is '}'
					// At this point we need to add any brushes to the entity. This method
					// outputs the brush String in Gearcraft standards.
					for(int j=0;j<brushes.length;j++) {
						out+=brushes[j].toString()+""+(char)0x0D+(char)0x0A;
					}
					out+=attributes[i]; // This will be the ending }
				}
			} catch(java.lang.NullPointerException e) {
				out+="null"+(char)0x0D+(char)0x0A;
			}
		}
		return out;
	}
	
	// +isBrushBased()
	// Reads the first character of the model attribute. If it's *, then it's a brush
	// based entity and this method returns true. If not, it returns false.
	public boolean isBrushBased() {
		return (brushes.length>0 || getModelNumber()>=0);
	}
	
	// attributeIs(String, String)
	// Returns true if the attribute String1 exists and is equivalent to String2
	public boolean attributeIs(String attribute, String check) {
		if(attributeExists(attribute)) {
			if(getAttribute(attribute).equalsIgnoreCase(check)) {
				return true;
			}
			if(attribute.equalsIgnoreCase("angles") || attribute.equalsIgnoreCase("origin") || attribute.equalsIgnoreCase("spawnflags") || attribute.equalsIgnoreCase("skin") || attribute.equalsIgnoreCase("health")) {
				if(check.equals("") || check.equals("0") || check.equals("0 0") || check.equals("0 0 0")) {
					if(getAttribute(attribute).equals("") || getAttribute(attribute).equals("0") || getAttribute(attribute).equals("0 0") || getAttribute(attribute).equals("0 0 0")) {
						return true;
					}
				}
			}
			return false;
		}
		return false;
	}
	
	// findAttributeIndex(String)
	// Returns the index of a keyvalue in this entity. If it does not exist, returns -1.
	public int findAttributeIndex(String attribute) {
		for(int i=0;i<attributes.length;i++) {
			try {
				if(attributes[i].substring(0,attribute.length()+2).compareToIgnoreCase("\""+attribute+"\"")==0) {
					return i;
				}
			} catch(StringIndexOutOfBoundsException e) { // for cases where the whole String is shorter than
				;                                         // the name of the attribute we're looking for. Do nothing.
			} catch(java.lang.NullPointerException e ) {
				break;
			}
		}
		return -1;
	}
	
	public boolean attributeExists(String attribute) {
		return (findAttributeIndex(attribute) >= 0);
	}
	
	// Bitwise spawnflags operators
	public int getSpawnflags() {
		try {
			return Integer.parseInt(getAttribute("spawnflags"));
		} catch(java.lang.NumberFormatException e) {
			return 0;
		}
	}
	
	// Returns true if the bits in "spawnflags" corresponding to the set bits in 'check' are set
	public boolean spawnflagsSet(int check) {
		return ((getSpawnflags() & check) == check);
	}
	
	// Toggles the bits in "spawnflags" which are set in "check"
	public void toggleSpawnflags(int toggle) {
		setAttribute("spawnflags", new Integer(getSpawnflags() ^ toggle).toString());
	}
	
	// Disables the bits in "spawnflags" which are set in "check"
	// Alternate method: spawnflags = (disable ^ 0xFFFFFFFF) & spawnflags
	public void disableSpawnflags(int disable) {
		toggleSpawnflags(getSpawnflags() & disable);
	}
	
	// Enables the bits in "spawnflags" which are set in "check"
	public void enableSpawnflags(int enable) {
		setAttribute("spawnflags", new Integer(getSpawnflags() | enable).toString());
	}
	
	// This doesn't really build anything, it just fills the "connections" array with the
	// attributes Hammer uses for entity I/O more complex than simple "fire target" systems.
	public void buildConnections() {
		for(int i=0;i<attributes.length;i++) {
			int numQuotes=0;
			int numCommas=0;
			for(int j=0;j<attributes[i].length();j++) {
				if(attributes[i].charAt(j)=='\"') {
					numQuotes++;
				}
				if(numQuotes==3 && attributes[i].charAt(j)==',') {
					numCommas++;
				}
			}
			if(numCommas==4 || numCommas==6) {
				addConnection(attributes[i]);
				deleteAttribute(i);
				i--;
			}
		}
		if(connections.length>0) {
			addAttributeInside("connections");
			addAttributeInside("{");
			for(int i=0;i<connections.length;i++) {
				addAttributeInside((char)0x09+""+connections[i]);
			}
			addAttributeInside("}");
		}
	}
	
	public void addConnection(String st) {
		String[] newList=new String[connections.length+1];
		for(int i=0;i<connections.length;i++) {
			newList[i]=connections[i];
		}
		newList[newList.length-1]=st;
		connections=newList;
	}
	
	// Try to determine what Source engine input this entity would perform when "fired".
	// "Firing" an entity is used in practically all other engines for entity I/O, but
	// Source replaced it with the input/output system which, while more powerful, makes
	// my job that much harder. There is no generic "Fire" input, so I need to give a
	// best guess as to the action that will actually be performed.
	public String onFire() {
		if(attributeIs("classname", "func_door") || 
		   attributeIs("classname", "func_door_rotating") || 
		   attributeIs("classname", "trigger_hurt") || 
		   attributeIs("classname", "func_brush") || 
		   attributeIs("classname", "light") || 
		   attributeIs("classname", "light_spot")) {
			return "Toggle";
		}
		if(attributeIs("classname", "ambient_generic")) {
			return "ToggleSound";
		}
		if(attributeIs("classname", "env_message")) {
			return "ShowMessage";
		}
		if(attributeIs("classname", "trigger_changelevel")) {
			return "ChangeLevel";
		}
		if(attributeIs("classname", "env_global")) {
			if(attributeIs("triggermode", "1")) {
				return "TurnOn";
			} else {
				if(attributeIs("triggermode", "3")) {
					return "Toggle";
				} else {
					return "TurnOff";
				}
			}
		}
		if(attributeIs("classname", "func_breakable")) {
			return "Break";
		}
		if(attributeIs("classname", "func_button")) {
			return "Press";
		}
		if(attributeIs("classname", "env_shake")) {
			return "StartShake";
		}
		if(attributeIs("classname", "env_fade")) {
			return "Fade";
		}
		if(attributeIs("classname", "env_sprite")) {
			return "ToggleSprite";
		}
		if(attributeIs("classname", "logic_relay")) {
			return "Trigger";
		}
		return "Toggle";
	}
	
	public String onEnable() {
		if(attributeIs("classname", "func_door") || 
		   attributeIs("classname", "func_door_rotating") || 
		   attributeIs("classname", "trigger_hurt") || 
		   attributeIs("classname", "func_brush") ||
		   attributeIs("classname", "logic_relay")) {
			return "Enable";
		}
		if(attributeIs("classname", "ambient_generic")) {
			return "PlaySound";
		}
		if(attributeIs("classname", "env_message")) {
			return "ShowMessage";
		}
		if(attributeIs("classname", "trigger_changelevel")) {
			return "ChangeLevel";
		}
		if(attributeIs("classname", "light") || 
		   attributeIs("classname", "light_spot")) {
			return "TurnOn";
		}
		if(attributeIs("classname", "func_breakable")) {
			return "Break";
		}
		if(attributeIs("classname", "env_shake")) {
			return "StartShake";
		}
		if(attributeIs("classname", "env_fade")) {
			return "Fade";
		}
		if(attributeIs("classname", "env_sprite")) {
			return "ShowSprite";
		}
		if(attributeIs("classname", "func_button")) {
			return "PressIn";
		}
		return "Enable";
	}
	
	public String onDisable() {
		if(attributeIs("classname", "func_door") || 
		   attributeIs("classname", "func_door_rotating") || 
		   attributeIs("classname", "trigger_hurt") || 
		   attributeIs("classname", "func_brush") ||
		   attributeIs("classname", "logic_relay")) {
			return "Disable";
		}
		if(attributeIs("classname", "ambient_generic")) {
			return "StopSound";
		}
		if(attributeIs("classname", "env_message")) {
			return "ShowMessage";
		}
		if(attributeIs("classname", "trigger_changelevel")) {
			return "ChangeLevel";
		}
		if(attributeIs("classname", "light") || 
		   attributeIs("classname", "light_spot")) {
			return "TurnOff";
		}
		if(attributeIs("classname", "func_breakable")) {
			return "Break";
		}
		if(attributeIs("classname", "env_shake")) {
			return "StopShake";
		}
		if(attributeIs("classname", "env_fade")) {
			return "Fade";
		}
		if(attributeIs("classname", "env_sprite")) {
			return "HideSprite";
		}
		if(attributeIs("classname", "func_button")) {
			return "PressOut";
		}
		return "Disable";
	}
	
	// Try to determine which "Output" normally causes this entity to "fire" its target.
	public String fireAction() {
		if(attributeIs("classname", "func_button") || 
		   attributeIs("classname", "func_rot_button") || 
		   attributeIs("classname", "momentary_rot_button")) {
			return "OnPressed";
		}
		if(attributeIs("classname", "trigger_multiple") || 
		   attributeIs("classname", "trigger_once") ||
		   attributeIs("classname", "logic_relay")) {
			return "OnTrigger";
		}
		if(attributeIs("classname", "logic_auto")) {
			return "OnNewGame";
		}
		if(attributeIs("classname", "func_door") || 
		   attributeIs("classname", "func_door_rotating")) {
			return "OnOpen";
		}
		if(attributeIs("classname", "func_breakable")) {
			return "OnBreak";
		}
		return "None";
	}
	
	public void setFired(boolean in) {
		fired=in;
	}
	
	public static Entity cloneNoBrushes(Entity copy) {
		return new Entity(copy.getAttributes());
	}
	
	public Entity cloneNoBrushes() {
		return new Entity(attributes);
	}
	
	// ACCESSORS/MUTATORS
	
	// +setData(String)
	// Used by constructors and can be used by outside classes to set or change the data
	// in this specific entity. It takes a String and parses it into the attributes array.
	// This input String CAN include the { and } from the entity structure, in that it 
	// won't cause any errors in the program. However if and when you want to write the
	// entities back into a lump you must remember whether you included them or not. The
	// behavior of the Entities class is to include them.
	public void setData(String in) {
		Scanner reader=new Scanner(in);
		reader.useDelimiter((char)0x0A+"");
		Scanner counter=new Scanner(in);
		counter.useDelimiter((char)0x0A+"");
		
		int numAttributes=0;
		while(counter.hasNext()) {
			counter.next();
			numAttributes++;
		}
		
		attributes=new String[numAttributes];
		for(int i=0;i<numAttributes;i++) {
			String current=reader.next();
			String backup=current;
			// This will trim all the bytes before the quotation marks if they exist,
			// since the Windows newline sequence is 0D0A and all its text editors use
			// that. This keeps the data from getting confusing to this program, but
			// also saves a small amount of space in the output lump itself.
			// Also, in Vindictus BSPs, the compiled entities lump has 0x09 "tabulation"
			// before each attribute, as if they weren't processed out by the compiler
			// as whitespace. Kinda stupid. It didn't cause any exceptions in this program,
			// but it did keep attribute keys from being read properly.
			while(current.length()>0 && current.charAt(0)!='\"' && current.charAt(0)!='{' && current.charAt(0)!='}') {
				current=current.substring(1);
			}
			while(current.length()>0 && current.charAt(current.length()-1)!='\"' && current.charAt(current.length()-1)!='{' && current.charAt(current.length()-1)!='}') {
				current=current.substring(0,current.length()-1);
			}
			String temp="";
			try {
				for(int j=0;j<current.length();j++) {
					// I've actually seen the \" escape character used in a map. It made Hammer go apeshit.
					if(!(current.charAt(j)=='\\' && current.charAt(j+1)=='\"')) {
						temp+=current.charAt(j);
					} else {
						j++;
					}
				}
				current=temp;
			} catch(java.lang.StringIndexOutOfBoundsException e) {
				;
			}
			attributes[i]=current;
		}
	}
	
	// +getAttribute(String)
	// Takes in an attribute as a String and returns the value of that attribute,
	// if it exists. If not, return empty String. I used to have an exception for this,
	// but always catching it was a pain in the ass. So instead, if the attribute
	// doesn't exist, just return an empty String. I don't think I've ever seen
	// an empty string used as a value in a map before, and either way the setAttribute
	// method will automatically add an attribute if it doesn't exist, and just
	// change it if it does, whether it's empty or not.
	public String getAttribute(String attribute) {
		String output="";
		for(int i=0;i<attributes.length;i++) {
			try {
				if(attributes[i].substring(0,attribute.length()+2).compareToIgnoreCase("\""+attribute+"\"")==0) {
					output=attributes[i].substring(attribute.length()+4,attributes[i].length()-1);
					break;
				}
			} catch(StringIndexOutOfBoundsException e) { // for cases where the whole String is shorter than
				;                                         // the name of the attribute we're looking for. Do nothing.
			} catch(java.lang.NullPointerException e) {
				break;
			}
		}
		return output;
	}
	
	// Gets the name of the attribute at index
	public String getAttributeName(int index) {
		String output = "";
		for(int i=1;i<attributes[index].length();i++) {
			if(attributes[index].charAt(i)=='\"' && attributes[index].charAt(i)!='\\') {
				break;
			} else {
				output+=attributes[index].charAt(i);
			}
		}
		return output;
	}
	
	// Gets the value of the attribute at index
	public String getAttributeValue(int index) {
		String output = "";
		for(int i=attributes[index].length()-2;i>4;i--) {
			if(attributes[index].charAt(i)=='\"' && attributes[index].charAt(i)!='\\') {
				break;
			} else {
				output=attributes[index].charAt(i)+output;
			}
		}
		return output;
	}
	
	// getAttribute(int)
	// Simply returns the attribute at the specified index
	public String getAttribute(int index) {
		try {
			return attributes[index];
		} catch(java.lang.ArrayIndexOutOfBoundsException e) {
			return null;
		}
	}
	
	public String getConnection(int index) {
		try {
			return connections[index];
		} catch(java.lang.ArrayIndexOutOfBoundsException e) {
			return null;
		}
	}
	
	// getAttributes()
	// Returns the attribute array as-is
	public String[] getAttributes() {
		return attributes;
	}
	
	public String[] getConnections() {
		return connections;
	}
	
	// getBrush(int)
	// Simply returns the brush at the specified index
	public MAPBrush getBrush(int index) {
		try {
			return brushes[index];
		} catch(java.lang.ArrayIndexOutOfBoundsException e) {
			return null;
		}
	}
	
	// getBrushes()
	// Returns the brush array as-is
	public MAPBrush[] getBrushes() {
		return brushes;
	}
	
	// +getModelNumber()
	// If there's a model number in the attributes list, this method fetches it
	// and returns it. If there is no model defined, or it's not a numerical 
	// value, then -1 is returned. If it's the worldspawn then a 0 is returned.
	public int getModelNumber() {
		if(getAttribute("classname").equalsIgnoreCase("worldspawn")) {
			return 0;
		} else {
			for(int i=0;i<attributes.length;i++) {
				try {
					if(attributes[i].substring(0,7).compareToIgnoreCase("\"model\"")==0) {
						// This substring skips the "model" "* and gets to the number
						return Integer.parseInt(attributes[i].substring(10,attributes[i].length()-1));
					}
				} catch(StringIndexOutOfBoundsException e) { // substring(0,7) was longer than the String
					;
				} catch(NumberFormatException e) { // The model wasn't a number
					break; // It's (hopefully) not going to have any other models defined
				}
			}
		}
		return -1;
	}
	
	// setAttribute()
	// Set an attribute. If it doesn't exist, it is added. If it does, it is
	// overwritten with the new one, since that's much easier to do than edit
	// the preexisting one.
	public void setAttribute(String attribute, String value) {
		boolean done=false;
		for(int i=0;i<attributes.length && !done;i++) {
			try {
				if(attributes[i].substring(0,attribute.length()+2).compareToIgnoreCase("\""+attribute+"\"")==0) {
					attributes[i]="\""+attribute+"\" \""+value+"\"";
					done=true;
				}
			} catch(StringIndexOutOfBoundsException e) {
				;
			} catch(java.lang.NullPointerException e) {
				Window.println("WARNING: Entity with null attribute?! Attribute no. "+i+(char)0x0D+(char)0x0A+toString(),Window.VERBOSITY_WARNINGS);
			}
		}
		if(!done) {
			addAttributeInside(attribute, value);
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
	
	// getAngles()
	// Returns the three components of the entity's "angles" attribute as an array
	// of three doubles. Since everything is a string anyway, I can be as precise
	// as I want.
	public double[] getAngles() {
		double[] output=new double[3]; // initializes to {0,0,0}
		if(!getAttribute("angles").equals("")) {
			String angles=getAttribute("angles");
			Scanner numGetter=new Scanner(angles);
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
	
	public int getNumBrushes() {
		return brushes.length;
	}
	
	public int getNumConnections() {
		return connections.length;
	}
}
