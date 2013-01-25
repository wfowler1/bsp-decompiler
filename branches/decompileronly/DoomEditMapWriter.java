// DoomEditMapWriter class
//
// Writes a DoomEdit (or other id tech 5 editor) file from a passed Entities object

import java.io.File;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.util.Scanner;

public class DoomEditMapWriter {

	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	public static final int A = 0;
	public static final int B = 1;
	public static final int C = 2;
	
	public static final int X = 0;
	public static final int Y = 1;
	public static final int Z = 2;
	
	// These are lowercase so as not to conflict with A B and C
	// Light entity attributes; red, green, blue, strength (can't use i for intensity :P)
	public static final int r = 0;
	public static final int g = 1;
	public static final int b = 2;
	public static final int s = 3;
	
	private String path;
	private Entities data;
	private File mapFile;
	private int BSPVersion;
	
	private int currentEntity;
	
	private static DecimalFormat fmt = new DecimalFormat("0.##########");
	
	// CONSTRUCTORS
	
	public DoomEditMapWriter(Entities from, String to, int BSPVersion) {
		this.data=from;
		this.path=to;
		this.mapFile=new File(path);
		this.BSPVersion=BSPVersion;
	}
	
	// METHODS
	
	// write()
	// Saves the lump to the specified path.
	// Handling file I/O with Strings is generally a bad idea. If you have maybe a couple hundred
	// Strings to write then it'll probably be okay, but when you have on the order of 10,000 Strings
	// it gets VERY slow, even if you concatenate them all before writing.
	public void write() throws java.io.IOException, java.lang.InterruptedException {
		if(!path.substring(path.length()-4).equalsIgnoreCase(".map")) {
			mapFile=new File(path+".map");
		}
		try {
			File absolutepath=new File(mapFile.getParent()+"\\");
			if(!absolutepath.exists()) {
				absolutepath.mkdir();
			}
			if(!mapFile.exists()) {
				mapFile.createNewFile();
			} else {
				mapFile.delete();
				mapFile.createNewFile();
			}
			
			// Preprocessing entity corrections
			if(BSPVersion==42) {
				for(int i=1;i<data.length();i++) {
					for(int j=0;j<data.getElement(i).getNumBrushes();j++) {
						if(data.getElement(i).getBrushes()[j].isWaterBrush()) {
							data.getElement(0).addBrush(data.getElement(i).getBrushes()[j]);
							// TODO: Textures on this brush
						}
					}
					if(data.getElement(i).getAttribute("classname").equalsIgnoreCase("func_water")) {
						data.delete(i);
						i--;
					}
				}
			}
			
			byte[] temp;
			
			FileOutputStream mapWriter=new FileOutputStream(mapFile);
			String tempString="Version 2\n";
			temp=tempString.getBytes();
			
			mapWriter.write(temp);
			
			byte[][] entityBytes=new byte[data.length()][];
			int totalLength=0;
			for(currentEntity=0;currentEntity<data.length();currentEntity++) {
				if(Thread.currentThread().interrupted()) {
					throw new java.lang.InterruptedException("while writing DoomEdit map.");
				}
				try {
					entityBytes[currentEntity]=entityToByteArray(data.getElement(currentEntity));
				} catch(java.lang.ArrayIndexOutOfBoundsException e) { // This happens when entities are added after the array is made
					byte[][] newList=new byte[data.length()][]; // Create a new array with the new length
					for(int j=0;j<entityBytes.length;j++) {
						newList[j]=entityBytes[j];
					}
					newList[currentEntity]=entityToByteArray(data.getElement(currentEntity));
					entityBytes=newList;
				}
				totalLength+=entityBytes[currentEntity].length;
			}
			byte[] allEnts=new byte[totalLength];
			int offset=0;
			for(int i=0;i<data.length();i++) {
				for(int j=0;j<entityBytes[i].length;j++) {
					allEnts[offset+j]=entityBytes[i][j];
				}
				offset+=entityBytes[i].length;
			}
			mapWriter.write(allEnts);
			mapWriter.close();
		} catch(java.io.IOException e) {
			Window.println("ERROR: Could not save "+mapFile.getPath()+", ensure the file is not open in another program and the path "+path+" exists",Window.VERBOSITY_ALWAYS);
			throw e;
		}
	}
	
	// -entityToByteArray()
	// Converts the entity and its brushes into byte arrays rather than Strings,
	// which can then be written to a file much faster. Concatenating Strings is
	// a costly operation, especially when hundreds of thousands of Strings are
	// in play. This is one of two parts to writing a file quickly. The second
	// part is to call the FileOutputStream.write() method only once, with a
	// gigantic array, rather than several times with many small arrays. File I/O
	// from a hard drive is another costly operation, best done by handling
	// massive amounts of data in one go, rather than tiny amounts of data thousands
	// of times.
	private byte[] entityToByteArray(Entity in) throws java.lang.InterruptedException {
		byte[] out;
		double[] origin=new double[3];
		// Correct some attributes of entities
		switch(BSPVersion) {
			// TODO
			default:
				break;
		}
		if(in.isBrushBased()) {
			in.deleteAttribute("model");
		}
		if(in.getBrushes().length>0) {
			origin=in.getOrigin();
		}
		int len=0;
		// Get the lengths of all attributes together
		for(int i=0;i<in.getAttributes().length;i++) {
			len+=in.getAttributes()[i].length()+1; // Gonna need a newline after each attribute or they'll get jumbled together
			if(in.getAttributes()[i].equals("{")) {
				String temp="// entity "+currentEntity;
				len+=temp.length()+1;
			}
		}
		out=new byte[len];
		int offset=0;
		for(int i=0;i<in.getAttributes().length;i++) { // For each attribute
			if(Thread.currentThread().interrupted()) {
				throw new java.lang.InterruptedException("while writing DoomEdit map.");
			}
			if(in.getAttributes()[i].equals("{")) {
				in.getAttributes()[i]="// entity "+currentEntity+(char)0x0A+"{";
			} else {
				if(in.getAttributes()[i].equals("}")) {
					int brushArraySize=0;
					byte[][] brushes=new byte[in.getBrushes().length][];
					for(int j=0;j<in.getBrushes().length;j++) { // For each brush in the entity
						if(Thread.currentThread().interrupted()) {
							throw new java.lang.InterruptedException("while writing DoomEdit map.");
						}
						// models with origin brushes need to be offset into their in-use position
						in.getBrush(j).shift(new Vector3D(origin));
						brushes[j]=brushToByteArray(in.getBrush(j), j);
						brushArraySize+=brushes[j].length;
					}
					int brushoffset=0;
					byte[] brushArray=new byte[brushArraySize];
					for(int j=0;j<in.getBrushes().length;j++) { // For each brush in the entity
						for(int k=0;k<brushes[j].length;k++) {
							brushArray[brushoffset+k]=brushes[j][k];
						}
						brushoffset+=brushes[j].length;
					}
					if(brushArray.length!=0) {
						len+=brushArray.length;
						byte[] newOut=new byte[len];
						for(int j=0;j<out.length;j++) {
							newOut[j]=out[j];
						}
						for(int j=0;j<brushArray.length;j++) {
							newOut[j+out.length-2]=brushArray[j];
						}
						offset+=brushArray.length;
						out=newOut;
					}
				}
			}
			for(int j=0;j<in.getAttributes()[i].length();j++) { // Then for each byte in the attribute
				out[j+offset]=(byte)in.getAttributes()[i].charAt(j); // add it to the output array
			}
			offset+=in.getAttributes()[i].length();
			out[offset]=(byte)0x0A;
			offset++;
		}
		return out;
	}
	
	private byte[] brushToByteArray(MAPBrush in, int num) {
		if(in.getNumSides() < 4) { // Can't create a brush with less than 4 sides
			Window.println("WARNING: Tried to create brush from "+in.getNumSides()+" sides!",Window.VERBOSITY_WARNINGS);
			return new byte[0];
		}
		String brush="// primitive "+num+(char)0x0A+"{"+(char)0x0A+" brushDef3"+(char)0x0A+" {"+(char)0x0A;
		for(int i=0;i<in.getNumSides();i++) {
			brush+="  "+brushSideToString(in.getSide(i))+(char)0x0A;
		}
		brush+=" }"+(char)0x0A+"}"+(char)0x0A;
		if(brush.length() < 58) { // Any brush this short contains no sides.
			Window.println("WARNING: Brush with no sides being written! Oh no!",Window.VERBOSITY_WARNINGS);
			return new byte[0];
		} else {
			byte[] brushbytes=new byte[brush.length()];
			for(int i=0;i<brush.length();i++) {
				brushbytes[i]=(byte)brush.charAt(i);
			}
			return brushbytes;
		}
	}
	
	private String brushSideToString(MAPBrushSide in) {
		try {
			String texture=in.getTexture();
			Plane plane=in.getPlane();
			Vector3D textureS=in.getTextureS();
			Vector3D textureT=in.getTextureT();
			double textureShiftS=in.getTextureShiftS();
			double textureShiftT=in.getTextureShiftT();
			double texScaleX=in.getTexScaleX();
			double texScaleY=in.getTexScaleY();
			if(Window.roundNumsIsSelected()) {
				String out="( "+fmt.format(plane.getA())+" "+fmt.format(plane.getB())+" "+fmt.format(plane.getC())+" "+fmt.format(plane.getDist())+" ) "+
				           "( ( 1 0 "+fmt.format(textureShiftS)+" ) ( 0 1 "+fmt.format(textureShiftT)+" ) ) "+
							  "\""+texture+"\" 0 0 0";
				return out;
			} else {
				String out="( "+plane.getA()+" "+plane.getB()+" "+plane.getC()+" "+plane.getDist()+" ) "+
				           "( ( 1 0 "+textureShiftS+" ) ( 0 1 "+textureShiftT+" ) ) "+
							  "\""+texture+"\" 0 0 0";
				return out;
			}
		} catch(java.lang.NullPointerException e) {
			Window.println("WARNING: Side with bad data! Not exported!",Window.VERBOSITY_WARNINGS);
			return null;
		}
	}
	
	// ACCESSORS/MUTATORS
	
}