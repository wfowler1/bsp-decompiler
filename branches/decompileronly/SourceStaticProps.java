// SourceStaticProps class

// Extends the Lump class, contains data only relevant to Static Props, like
// the dictionary of actual model paths.

public class SourceStaticProps extends Lump<SourceStaticProp> {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	private String[] dictionary;

	// CONSTRUCTORS
	
	// Takes a byte array, as if read from a FileInputStream
	public SourceStaticProps(SourceStaticProp[] elements, String[] dictionary, int length) {
		super(elements, length, 0);
		this.dictionary=dictionary;
	}
	
	// METHODS
	
	// ACCESSORS/MUTATORS
	
	public String[] getDictionary() {
		return dictionary;
	}
}