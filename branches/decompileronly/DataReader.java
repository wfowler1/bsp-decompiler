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
	
	public static Vector3D readPoint3F(byte first, byte second, byte third, byte fourth,
	                                  byte fifth, byte sixth, byte seventh, byte eighth,
	                                  byte ninth, byte tenth, byte eleventh, byte twelfth) {
		return new Vector3D(readFloat(first,second,third,fourth), readFloat(fifth, sixth, seventh, eighth), readFloat(ninth, tenth, eleventh, twelfth));
	}
	
	public static Vector3D readPoint3F(byte[] in) {
		return new Vector3D(readFloat(in[0], in[1], in[2], in[3]), readFloat(in[4], in[5], in[6], in[7]), readFloat(in[8], in[9], in[10], in[11]));
	}
	
	public static double readDouble(byte first, byte second, byte third, byte fourth, byte fifth, byte sixth, byte seventh, byte eighth) {
		return Double.longBitsToDouble((eighth << 56) | ((seventh & 0xff) << 48) | ((sixth & 0xff) << 40) | (fifth & 0xff) << 32 | ((fourth & 0xff) << 24) | ((third & 0xff) << 16) | ((second & 0xff) << 8) | (first & 0xff));
	}
	
	public static double readDouble(byte[] in) {
		return readDouble(in[0], in[1], in[2], in[3], in[4], in[5], in[6], in[7]);
	}
	
	public static Vector3D readPoint3D(byte first, byte second, byte third, byte fourth,
	                                  byte fifth, byte sixth, byte seventh, byte eighth,
	                                  byte ninth, byte tenth, byte eleventh, byte twelfth,
	                                  byte thirteenth, byte fourteenth, byte fifteenth, byte sixteenth, 
	                                  byte seventeenth, byte eighteenth, byte ninteenth, byte twentieth,  
	                                  byte twentyfirst, byte twentysecond, byte twentythird, byte twentyfourth) {
		return new Vector3D(readDouble(first,second,third,fourth,fifth,sixth,seventh,eighth), readDouble(ninth, tenth, eleventh, twelfth, thirteenth, fourteenth, fifteenth, sixteenth), readDouble(seventeenth, eighteenth, ninteenth, twentieth, twentyfirst, twentysecond, twentythird, twentyfourth));
	}
	
	public static Vector3D readPoint3D(byte[] in) {
		return new Vector3D(readDouble(in[0], in[1], in[2], in[3], in[4], in[5], in[6], in[7]), readDouble(in[8], in[9], in[10], in[11], in[12], in[13], in[14], in[15]), readDouble(in[16], in[17], in[18], in[19], in[20], in[21], in[22], in[23]));
	}
	
	// ACCESSORS/MUTATORS
}