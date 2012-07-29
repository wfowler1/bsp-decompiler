// MAP510Writer class
//
// Writes a Gearcraft .MAP file from a passed Entities object

import java.io.File;
import java.io.FileOutputStream;
import java.text.DecimalFormat;

public class MAP510Writer {

	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	private String path;
	private Entities data;
	private File mapFile;
	private boolean gearcraftDecs;
	
	private static DecimalFormat fmtPoints = new DecimalFormat("0.000000");
	private static DecimalFormat fmtScales = new DecimalFormat("0.####");
	
	// CONSTRUCTORS
	
	public MAP510Writer(Entities from, String to, boolean gearcraftDecs) {
		this.data=from;
		this.path=to;
		this.gearcraftDecs=gearcraftDecs;
		this.mapFile=new File(path);
	}
	
	// METHODS
	
	// write()
	// Saves the lump to the specified path.
	// Handling file I/O with Strings is generally a bad idea. If you have maybe a couple hundred
	// Strings to write then it'll probably be okay, but when you have on the order of 10,000 Strings
	// it gets VERY slow, even if you concatenate them all before writing.
	public void write() throws java.io.IOException {
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
			
			FileOutputStream mapWriter=new FileOutputStream(mapFile);
			byte[][] entityBytes=new byte[data.getNumElements()][];
			int totalLength=0;
			for(int i=0;i<data.getNumElements();i++) {
				entityBytes[i]=entityToByteArray(data.getEntity(i), i);
				totalLength+=entityBytes[i].length;
			}
			byte[] allEnts=new byte[totalLength];
			int offset=0;
			for(int i=0;i<data.getNumElements();i++) {
				for(int j=0;j<entityBytes[i].length;j++) {
					allEnts[offset+j]=entityBytes[i][j];
				}
				offset+=entityBytes[i].length;
			}
			mapWriter.write(allEnts);
			mapWriter.close();
		} catch(java.io.IOException e) {
			Window.window.println("ERROR: Could not save "+mapFile.getPath()+", ensure the file is not open in another program and the path "+path+" exists");
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
	private byte[] entityToByteArray(Entity in, int num) {
		byte[] out;
		double[] origin;
		if(in.isBrushBased()) {
			origin=in.getOrigin();
			in.deleteAttribute("origin");
		} else {
			origin=new double[3];
		}
		int len=0;
		// Get the lengths of all attributes together
		for(int i=0;i<in.getAttributes().length;i++) {
			len+=in.getAttributes()[i].length()+2; // Gonna need a newline after each attribute or they'll get jumbled together
			if(in.getAttributes()[i].equals("{")) {
				String temp=" // Entity "+num;
				len+=temp.length();
			}
		}
		out=new byte[len];
		int offset=0;
		for(int i=0;i<in.getAttributes().length;i++) { // For each attribute
			if(in.getAttributes()[i].equals("{")) {
				in.getAttributes()[i]="{ // Entity "+num;
			} else {
				if(in.getAttributes()[i].equals("}")) {
					int brushArraySize=0;
					byte[][] brushes=new byte[in.getBrushes().length][];
					for(int j=0;j<in.getBrushes().length;j++) { // For each brush in the entity
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
							newOut[j+out.length-3]=brushArray[j];
						}
						offset+=brushArray.length;
						out=newOut;
					}
				}
			}
			for(int j=0;j<in.getAttributes()[i].length();j++) { // Then for each byte in the attribute
				out[j+offset]=(byte)in.getAttributes()[i].charAt(j); // add it to the output array
			}
			out[offset+in.getAttributes()[i].length()]=(byte)0x0D;
			offset+=in.getAttributes()[i].length()+1;
			out[offset]=(byte)0x0A;
			offset++;
		}
		return out;
	}
	
	private byte[] brushToByteArray(MAPBrush in, int num) {
		if(in.getNumSides() < 4) { // Can't create a brush with less than 4 sides
			Window.println("Tried to create brush from "+in.getNumSides()+" sides!");
			return new byte[0];
		}
		String brush="{ // Brush "+num+(char)0x0D+(char)0x0A;
		if(in.isDetailBrush()) {
			brush+="\"BRUSHFLAGS\" \"DETAIL\""+(char)0x0D+(char)0x0A;
		}
		for(int i=0;i<in.getNumSides();i++) {
			brush+=brushSideToString(in.getSide(i))+(char)0x0D+(char)0x0A;
		}
		brush+="}"+(char)0x0D+(char)0x0A;
		if(brush.length() < 45) { // Any brush this short contains no sides.
			Window.println("Brush with no sides being written! Oh no!");
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
			Vector3D[] triangle=in.getTriangle();
			String texture=in.getTexture();
			Vector3D textureS=in.getTextureS();
			Vector3D textureT=in.getTextureT();
			double textureShiftS=in.getTextureShiftS();
			double textureShiftT=in.getTextureShiftT();
			float texRot=in.getTexRot();
			double texScaleX=in.getTexScaleX();
			double texScaleY=in.getTexScaleY();
			int flags=in.getFlags();
			String material=in.getMaterial();
			double lgtScale=in.getLgtScale();
			double lgtRot=in.getLgtRot();
			if(gearcraftDecs) {
				return "( "+fmtPoints.format((double)Math.round(triangle[0].getX()*1000000.0)/1000000.0)+" "+fmtPoints.format((double)Math.round(triangle[0].getY()*1000000.0)/1000000.0)+" "+fmtPoints.format((double)Math.round(triangle[0].getZ()*1000000.0)/1000000.0)+" ) "+
				       "( "+fmtPoints.format((double)Math.round(triangle[1].getX()*1000000.0)/1000000.0)+" "+fmtPoints.format((double)Math.round(triangle[1].getY()*1000000.0)/1000000.0)+" "+fmtPoints.format((double)Math.round(triangle[1].getZ()*1000000.0)/1000000.0)+" ) "+
				       "( "+fmtPoints.format((double)Math.round(triangle[2].getX()*1000000.0)/1000000.0)+" "+fmtPoints.format((double)Math.round(triangle[2].getY()*1000000.0)/1000000.0)+" "+fmtPoints.format((double)Math.round(triangle[2].getZ()*1000000.0)/1000000.0)+" ) "+
				       texture + 
				       " [ "+fmtPoints.format((double)Math.round(textureS.getX()*1000000.0)/1000000.0)+" "+fmtPoints.format((double)Math.round(textureS.getY()*1000000.0)/1000000.0)+" "+fmtPoints.format((double)Math.round(textureS.getZ()*1000000.0)/1000000.0)+" "+Math.round(textureShiftS)+" ]"+
				       " [ "+fmtPoints.format((double)Math.round(textureT.getX()*1000000.0)/1000000.0)+" "+fmtPoints.format((double)Math.round(textureT.getY()*1000000.0)/1000000.0)+" "+fmtPoints.format((double)Math.round(textureT.getZ()*1000000.0)/1000000.0)+" "+Math.round(textureShiftT)+" ] "+
				       fmtScales.format((double)Math.round(texRot*10000.0)/10000.0)+" "+fmtScales.format((double)Math.round(texScaleX*10000.0)/10000.0)+" "+fmtScales.format((double)Math.round(texScaleY*10000.0)/10000.0)+" "+flags+" "+
				       material +
				       " [ "+fmtScales.format((double)Math.round(lgtScale*1000000.0)/1000000.0)+" "+fmtScales.format((double)Math.round(lgtRot*1000000.0)/1000000.0)+" ]";
			} else {
				return "( "+triangle[0].getX()+" "+triangle[0].getY()+" "+triangle[0].getZ()+" ) "+
				       "( "+triangle[1].getX()+" "+triangle[1].getY()+" "+triangle[1].getZ()+" ) "+
				       "( "+triangle[2].getX()+" "+triangle[2].getY()+" "+triangle[2].getZ()+" ) "+
				       texture + 
				       " [ "+textureS.getX()+" "+textureS.getY()+" "+textureS.getZ()+" "+textureShiftS+" ]"+
				       " [ "+textureT.getX()+" "+textureT.getY()+" "+textureT.getZ()+" "+textureShiftT+" ] "+
				       texRot+" "+texScaleX+" "+texScaleY+" "+flags+" "+
				       material +
				       " [ "+lgtScale+" "+lgtRot+" ]";
			}
		} catch(java.lang.NullPointerException e) {
			Window.window.println("Side with bad data! Not exported!");
			return "";
		}
	}
	
	// ACCESSORS/MUTATORS
	
}