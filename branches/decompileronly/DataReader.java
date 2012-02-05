// DataReader class

// Static class
// Contains methods for reading certain data types from byte
// arrays. Useful for:
// Reading data from a bytesteam
// Avoiding confusion between big and little endian values (all are assumed little endian)
// Cleaning up code

public class DataReader {

	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	// CONSTRUCTORS
	
	// METHODS
	
	public static short readShort(byte first, byte second) {
		return (short)((second << 8) | (first & 0xff));
	}
	
	public static short readShort(byte[] in) {
		return readShort(in[0], in[1]);
	}
	
	public static int readInt(byte first, byte second, byte third, byte fourth) {
		return ((fourth << 24) | ((third & 0xff) << 16) | ((second & 0xff) << 8) | (first & 0xff));
	}
	
	public static int readInt(byte[] in) {
		return readInt(in[0], in[1], in[2], in[3]);
	}
	
	public static float readFloat(byte first, byte second, byte third, byte fourth) {
		return Float.intBitsToFloat((fourth << 24) | ((third & 0xff) << 16) | ((second & 0xff) << 8) | (first & 0xff));
	}
	
	public static float readFloat(byte[] in) {
		return readFloat(in[0], in[1], in[2], in[3]);
	}
	
	public static Point3D readPoint3F(byte first, byte second, byte third, byte fourth,
	                                byte fifth, byte sixth, byte seventh, byte eighth,
	                                byte ninth, byte tenth, byte eleventh, byte twelfth) {
		return new Point3D(readFloat(first,second,third,fourth), readFloat(fifth, sixth, seventh, eighth), readFloat(ninth, tenth, eleventh, twelfth));
	}
	
	public static Point3D readPoint3F(byte[] in) {
		return new Point3D(readFloat(in[0], in[1], in[2], in[3]), readFloat(in[4], in[5], in[6], in[7]), readFloat(in[8], in[9], in[10], in[11]));
	}
	
	// ACCESSORS/MUTATORS
}