// MOHRadiantMAPWriter class
//
// Writes a Radiant .MAP file from a passed Entities object

import java.io.File;
import java.io.FileOutputStream;
import java.text.DecimalFormat;

public class MOHRadiantMAPWriter {

	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	private String path;
	private Entities data;
	private File mapFile;
	private int BSPVersion;
	
	private int currentEntity;
	
	private static DecimalFormat fmtFloats = new DecimalFormat("0.######");
	private static DecimalFormat fmtRot = new DecimalFormat("0.00");
	
	// CONSTRUCTORS
	
	public MOHRadiantMAPWriter(Entities from, String to, int BSPVersion) {
		this.data=new Entities(from);
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
			
			// Preprocessing entity corrections
			if(BSPVersion==42) {
				for(int i=1;i<data.length();i++) {
					for(int j=0;j<data.getEntity(i).getNumBrushes();j++) {
						if(data.getEntity(i).getBrushes()[j].isWaterBrush()) {
							data.getEntity(0).addBrush(data.getEntity(i).getBrushes()[j]);
							// TODO: Textures on this brush
						}
					}
					if(data.getEntity(i).getAttribute("classname").equalsIgnoreCase("func_water")) {
						data.delete(i);
						i--;
					}
				}
			}
			
			FileOutputStream mapWriter=new FileOutputStream(mapFile);
			byte[][] entityBytes=new byte[data.length()][];
			int totalLength=0;
			for(currentEntity=0;currentEntity<data.length();currentEntity++) {
				try {
					entityBytes[currentEntity]=entityToByteArray(data.getEntity(currentEntity), currentEntity);
				} catch(java.lang.ArrayIndexOutOfBoundsException e) { // This happens when entities are added after the array is made
					byte[][] newList=new byte[data.length()][]; // Create a new array with the new length
					for(int j=0;j<entityBytes.length;j++) {
						newList[j]=entityBytes[j];
					}
					newList[currentEntity]=entityToByteArray(data.getEntity(currentEntity), currentEntity);
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
	private byte[] entityToByteArray(Entity in, int num) {
		byte[] out;
		double[] origin;
		// Correct some attributes of entities
		switch(BSPVersion) {
			case 42: // Nightfire
				in=ent42ToEntRad(in);
				break;
			case 38:
				in=ent38ToEntRad(in);
				break;
			case 1: // Doom! I can use any versioning system I want!
				break;
		}
		if(in.isBrushBased()) {
			origin=in.getOrigin();
			in.deleteAttribute("origin");
			in.deleteAttribute("model");
			if(origin[0]!=0 || origin[1]!=0 || origin[2]!=0) { // If this entity uses the "origin" attribute
				MAPBrush newOriginBrush=GenericMethods.createBrush(new Vector3D(-Window.getOriginBrushSize(),-Window.getOriginBrushSize(),-Window.getOriginBrushSize()),new Vector3D(Window.getOriginBrushSize(),Window.getOriginBrushSize(),Window.getOriginBrushSize()),"special/origin");
				in.addBrush(newOriginBrush);
			}
		} else {
			origin=new double[3];
		}
		int len=0;
		// Get the lengths of all attributes together
		for(int i=0;i<in.getAttributes().length;i++) {
			len+=in.getAttributes()[i].length()+2; // Gonna need a newline after each attribute or they'll get jumbled together
			if(in.getAttributes()[i].equals("{") && !in.getAttribute("classname").equals("worldspawn")) {
				String temp="// Entity "+num+(char)0x0D+(char)0x0A+"{";
				len+=temp.length();
			}
		}
		out=new byte[len];
		int offset=0;
		for(int i=0;i<in.getAttributes().length;i++) { // For each attribute
			if(in.getAttributes()[i].equals("{") && !in.getAttribute("classname").equals("worldspawn")) {
				in.getAttributes()[i]="// Entity "+num+(char)0x0D+(char)0x0A+"{";
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
			Window.println("WARNING: Tried to create brush from "+in.getNumSides()+" sides!",Window.VERBOSITY_WARNINGS);
			return new byte[0];
		}
		String brush="// Brush "+num+(char)0x0D+(char)0x0A+"{"+(char)0x0D+(char)0x0A;
		for(int i=0;i<in.getNumSides();i++) {
			brush+=brushSideToString(in.getSide(i), in.isDetailBrush())+(char)0x0D+(char)0x0A;
		}
		brush+="}"+(char)0x0D+(char)0x0A;
		if(brush.length() < 45) { // Any brush this short contains no sides.
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
	
	private String brushSideToString(MAPBrushSide in, boolean isDetail) {
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
			String temp="";
			// Correct textures here
			if(BSPVersion==42 || BSPVersion==1) {
				if(texture.equalsIgnoreCase("special/nodraw") || texture.equalsIgnoreCase("special/null")) {
					texture="common/nodraw";
				} else {
					if(texture.equalsIgnoreCase("special/clip")) {
						texture="common/clip";
					} else {
						if(texture.equalsIgnoreCase("special/sky")) {
							texture="common/skyportal";
						} else {
							if(texture.equalsIgnoreCase("special/trigger")) {
								texture="common/trigger";
							} else {
								if(texture.equalsIgnoreCase("special/playerclip")) {
									texture="common/playerclip";
								} else {
									if(texture.equalsIgnoreCase("special/npcclip") || texture.equalsIgnoreCase("special/enemyclip")) {
										texture="common/tankclip";
									}
								}
							}
						}
					}
				}
			} else {
				if(BSPVersion==38) {
					try {
						if(texture.equalsIgnoreCase("special/hint")) {
							texture="common/hint";
						} else {
							if(texture.equalsIgnoreCase("special/skip")) {
								texture="common/skip";
							} else {
								if(texture.equalsIgnoreCase("special/sky")) {
									texture="common/skyportal";
								} else {
									if(texture.substring(texture.length()-8).equalsIgnoreCase("/trigger")) {
										texture="common/trigger";
									} else {
										if(texture.substring(texture.length()-5).equalsIgnoreCase("/clip")) {
											texture="common/clip";
										}
									}
								}
							}
						}
					} catch(StringIndexOutOfBoundsException e) {
						;
					}
				}
			}
			if(Window.roundNumsIsSelected()) {
				temp = "( "+fmtFloats.format((double)Math.round(triangle[0].getX()*1000000.0)/1000000.0)+" "+fmtFloats.format((double)Math.round(triangle[0].getY()*1000000.0)/1000000.0)+" "+fmtFloats.format((double)Math.round(triangle[0].getZ()*1000000.0)/1000000.0)+" ) "+
				       "( "+fmtFloats.format((double)Math.round(triangle[1].getX()*1000000.0)/1000000.0)+" "+fmtFloats.format((double)Math.round(triangle[1].getY()*1000000.0)/1000000.0)+" "+fmtFloats.format((double)Math.round(triangle[1].getZ()*1000000.0)/1000000.0)+" ) "+
				       "( "+fmtFloats.format((double)Math.round(triangle[2].getX()*1000000.0)/1000000.0)+" "+fmtFloats.format((double)Math.round(triangle[2].getY()*1000000.0)/1000000.0)+" "+fmtFloats.format((double)Math.round(triangle[2].getZ()*1000000.0)/1000000.0)+" ) "+
				       texture + " "+Math.floor(textureShiftS)+" "+Math.floor(textureShiftT)+" "+fmtRot.format((double)Math.round(texRot*100.0)/100.0)+
						 " "+fmtFloats.format((double)Math.round(texScaleX*1000000.0)/1000000.0)+" "+fmtFloats.format((double)Math.round(texScaleY*1000000.0)/1000000.0)+
						 " 0 0 0 ";
						 // flags need more attention here
			} else {
				temp = "( "+triangle[0].getX()+" "+triangle[0].getY()+" "+triangle[0].getZ()+" ) "+
				       "( "+triangle[1].getX()+" "+triangle[1].getY()+" "+triangle[1].getZ()+" ) "+
				       "( "+triangle[2].getX()+" "+triangle[2].getY()+" "+triangle[2].getZ()+" ) "+
				       texture + " "+textureShiftS+" "+textureShiftT+" "+texRot+
						 " "+texScaleX+" "+texScaleY+
						 " 0 0 0 ";
						 // flags need more attention here
			}
			if(isDetail) {
				temp+="+surfaceparm detail ";
			}
			return temp;
		} catch(java.lang.NullPointerException e) {
			Window.println("WARNING: Side with bad data! Not exported!",Window.VERBOSITY_WARNINGS);
			return "";
		}
	}
	
	// TODO: Polish these up. Entity corrections are particularly important for Radiant editors.
	// For some ridiculous reason, Radiant seems to throw out any entity of class it does not
	// recognize, and every entity thereafter.
	public Entity ent42ToEntRad(Entity in) {
		if(in.getAttribute("classname").equalsIgnoreCase("func_door_rotating")) {
			in.setAttribute("classname", "func_rotatingdoor");
		}//else {
		return in;
	}
	
	public Entity ent38ToEntRad(Entity in) {
		return in;
	}
	
	// ACCESSORS/MUTATORS
	
}