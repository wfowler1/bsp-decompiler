// VMFWriter class
//
// Writes a Hammer .VMF file from a passed Entities object

import java.io.File;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.util.Scanner;

public class VMFWriter {

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
	
	int nextID=1;
	
	private static DecimalFormat fmt = new DecimalFormat("0.##########");
	
	// CONSTRUCTORS
	
	public VMFWriter(Entities from, String to, int BSPVersion) {
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
	public void write() throws java.io.IOException {
		if(!path.substring(path.length()-4).equalsIgnoreCase(".vmf")) {
			mapFile=new File(path+".vmf");
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
			
			byte[] temp;
			
			FileOutputStream mapWriter=new FileOutputStream(mapFile);
			String tempString="versioninfo"+(char)0x0D+(char)0x0A+"{"+(char)0x0D+(char)0x0A+"	\"editorversion\" \"400\""+(char)0x0D+(char)0x0A+"	\"editorbuild\" \"3325\""+(char)0x0D+(char)0x0A+"	\"mapversion\" \"0\""+(char)0x0D+(char)0x0A+"	\"formatversion\" \"100\""+(char)0x0D+(char)0x0A+"	\"prefab\" \"0\""+(char)0x0D+(char)0x0A+"}"+(char)0x0D+(char)0x0A+"";
			tempString+="visgroups"+(char)0x0D+(char)0x0A+"{"+(char)0x0D+(char)0x0A+"}"+(char)0x0D+(char)0x0A+"";
			tempString+="viewsettings"+(char)0x0D+(char)0x0A+"{"+(char)0x0D+(char)0x0A+"	\"bSnapToGrid\" \"1\""+(char)0x0D+(char)0x0A+"	\"bShowGrid\" \"1\""+(char)0x0D+(char)0x0A+"	\"bShowLogicalGrid\" \"0\""+(char)0x0D+(char)0x0A+"	\"nGridSpacing\" \"64\""+(char)0x0D+(char)0x0A+"	\"bShow3DGrid\" \"0\""+(char)0x0D+(char)0x0A+"}"+(char)0x0D+(char)0x0A+"";
			temp=tempString.getBytes();
			
			mapWriter.write(temp);
			
			byte[][] entityBytes=new byte[data.length()][];
			int totalLength=0;
			for(currentEntity=0;currentEntity<data.length();currentEntity++) {
				try {
					entityBytes[currentEntity]=entityToByteArray(data.getEntity(currentEntity));
				} catch(java.lang.ArrayIndexOutOfBoundsException e) { // This happens when entities are added after the array is made
					byte[][] newList=new byte[data.length()][]; // Create a new array with the new length
					for(int j=0;j<entityBytes.length;j++) {
						newList[j]=entityBytes[j];
					}
					newList[currentEntity]=entityToByteArray(data.getEntity(currentEntity));
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
	private byte[] entityToByteArray(Entity in) {
		in.setAttribute("id", new Integer(nextID++).toString());
		byte[] out;
		double[] origin=new double[3];
		// Correct some attributes of entities
		switch(BSPVersion) {
			case 42: // Nightfire
				in=ent42ToEntVMF(in);
				break;
			case 38:
				in=ent38ToEntVMF(in);
				break;
			case 1: // Doom! I can use any versioning system I want!
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
			if(in.getAttributes()[i].equals("{")) {
				len+=10; // "world"+(char)0x0D+(char)0x0A+"{"+(char)0x0D+(char)0x0A;
				if(!in.getAttribute("classname").equalsIgnoreCase("worldspawn")) {
					len+=1; // "entity"+(char)0x0D+(char)0x0A+"{"+(char)0x0D+(char)0x0A; instead of world
				}
			} else {
				if(in.getAttributes()[i].equals("}")) {
					len+=3;
				} else {
					len+=in.getAttributes()[i].length()+3; // Three for a tab and a newline
				}
			}
		}
		out=new byte[len];
		int offset=0;
		for(int i=0;i<in.getAttributes().length;i++) { // For each attribute
			if(in.getAttributes()[i].equals("{")) {
				if(in.getAttribute("classname").equalsIgnoreCase("worldspawn")) {
					in.getAttributes()[i]="world"+(char)0x0D+(char)0x0A+"{";
				} else {
					in.getAttributes()[i]="entity"+(char)0x0D+(char)0x0A+"{"; // instead of world
				}
			} else {
				if(in.getAttributes()[i].equals("}")) {
					int brushArraySize=0;
					byte[][] brushes=new byte[in.getBrushes().length][];
					for(int j=0;j<in.getBrushes().length;j++) { // For each brush in the entity
						if(in.getBrush(j).isDetailBrush()) {
							in.getBrush(j).setDetail(false);
							Entity newDetailEntity=new Entity("func_detail");
							newDetailEntity.addBrush(in.getBrush(j));
							data.add(newDetailEntity);
							brushes[j]=new byte[0]; // No data here! The brush will be output in its entity instead.
						} else {
							in.getBrush(j).shift(new Vector3D(origin));
							brushes[j]=brushToByteArray(in.getBrush(j));
							brushArraySize+=brushes[j].length;
						}
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
				} else { // If the attribute is neither { nor }
					out[offset++]=0x09; // Tabulation
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
	
	private byte[] brushToByteArray(MAPBrush in) {
		if(in.getNumSides() < 4) { // Can't create a brush with less than 4 sides
			Window.println("WARNING: Tried to create brush from "+in.getNumSides()+" sides!",Window.VERBOSITY_WARNINGS);
			return new byte[0];
		}
		String brush=(char)0x09+"solid"+(char)0x0D+(char)0x0A+(char)0x09+"{"+(char)0x0D+(char)0x0A+(char)0x09+(char)0x09+"\"id\" \""+(nextID++)+"\""+(char)0x0D+(char)0x0A;
		for(int i=0;i<in.getNumSides();i++) {
			brush+=brushSideToString(in.getSide(i));
		}
		brush+=(char)0x09+"}"+(char)0x0D+(char)0x0A;
		if(brush.length() < 40) { // Any brush this short contains no sides.
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
			Vector3D[] triangle=in.getTriangle();
			String texture=in.getTexture();
			Vector3D textureS=in.getTextureS();
			Vector3D textureT=in.getTextureT();
			double textureShiftS=in.getTextureShiftS();
			double textureShiftT=in.getTextureShiftT();
			double texScaleX=in.getTexScaleX();
			double texScaleY=in.getTexScaleY();
			float texRot=in.getTexRot();
			double lgtScale=in.getLgtScale();
			if(BSPVersion==42 || BSPVersion==1) {
				if(texture.equalsIgnoreCase("special/nodraw") || texture.equalsIgnoreCase("special/null")) {
					texture="tools/toolsnodraw";
				} else {
					if(texture.equalsIgnoreCase("special/clip")) {
						texture="tools/toolsclip";
					} else {
						if(texture.equalsIgnoreCase("special/sky")) {
							texture="tools/toolsskybox";
						} else {
							if(texture.equalsIgnoreCase("special/trigger")) {
								texture="tools/toolstrigger";
							} else {
								if(texture.equalsIgnoreCase("special/playerclip")) {
									texture="tools/toolsplayerclip";
								} else {
									if(texture.equalsIgnoreCase("special/npcclip") || texture.equalsIgnoreCase("special/enemyclip")) {
										texture="tools/toolsnpcclip";
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
							texture="tools/toolshint";
						} else {
							if(texture.equalsIgnoreCase("special/skip")) {
								texture="tools/toolsskip";
							} else {
								if(texture.equalsIgnoreCase("special/sky")) {
									texture="tools/toolsskybox";
								} else {
									if(texture.substring(texture.length()-8).equalsIgnoreCase("/trigger")) {
										texture="tools/toolstrigger";
									} else {
										if(texture.substring(texture.length()-5).equalsIgnoreCase("/clip")) {
											texture="tools/toolsclip";
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
				String out="		side"+(char)0x0D+(char)0x0A+"		{"+(char)0x0D+(char)0x0A;
				out+="			\"id\" \""+(nextID++)+"\""+(char)0x0D+(char)0x0A;
				out+="			\"plane\" \"("+fmt.format((double)Math.round(triangle[0].getX()*1000000.0)/1000000.0)+" "+fmt.format((double)Math.round(triangle[0].getY()*1000000.0)/1000000.0)+" "+fmt.format((double)Math.round(triangle[0].getZ()*1000000.0)/1000000.0)+") ";
				out+="("+fmt.format((double)Math.round(triangle[1].getX()*1000000.0)/1000000.0)+" "+fmt.format((double)Math.round(triangle[1].getY()*1000000.0)/1000000.0)+" "+fmt.format((double)Math.round(triangle[1].getZ()*1000000.0)/1000000.0)+") ";
				out+="("+fmt.format((double)Math.round(triangle[2].getX()*1000000.0)/1000000.0)+" "+fmt.format((double)Math.round(triangle[2].getY()*1000000.0)/1000000.0)+" "+fmt.format((double)Math.round(triangle[2].getZ()*1000000.0)/1000000.0)+")\""+(char)0x0D+(char)0x0A;
				out+="			\"material\" \"" + texture + "\""+(char)0x0D+(char)0x0A;
				out+="			\"uaxis\" \"["+fmt.format((double)Math.round(textureS.getX()*1000000.0)/1000000.0)+" "+fmt.format((double)Math.round(textureS.getY()*1000000.0)/1000000.0)+" "+fmt.format((double)Math.round(textureS.getZ()*1000000.0)/1000000.0)+" "+Math.round(textureShiftS)+"] "+fmt.format((double)Math.round(texScaleX*10000.0)/10000.0)+"\""+(char)0x0D+(char)0x0A;
				out+="			\"vaxis\" \"["+fmt.format((double)Math.round(textureT.getX()*1000000.0)/1000000.0)+" "+fmt.format((double)Math.round(textureT.getY()*1000000.0)/1000000.0)+" "+fmt.format((double)Math.round(textureT.getZ()*1000000.0)/1000000.0)+" "+Math.round(textureShiftT)+"] "+fmt.format((double)Math.round(texScaleY*10000.0)/10000.0)+"\""+(char)0x0D+(char)0x0A;
				out+="			\"rotation\" \""+fmt.format((double)Math.round(texRot*10000.0)/10000.0)+"\""+(char)0x0D+(char)0x0A;
				out+="			\"lightmapscale\" \""+fmt.format((double)Math.round(lgtScale*10000.0)/10000.0)+"\""+(char)0x0D+(char)0x0A;
				out+="			\"smoothing_groups\" \"0\""+(char)0x0D+(char)0x0A+"		}"+(char)0x0D+(char)0x0A;
				return out;
			} else {
				String out="		side"+(char)0x0D+(char)0x0A+"		{"+(char)0x0D+(char)0x0A;
				out+="			\"id\" \""+(nextID++)+"\""+(char)0x0D+(char)0x0A;
				out+="			\"plane\" \"("+triangle[0].getX()+" "+triangle[0].getY()+" "+triangle[0].getZ()+") ";
				out+="("+triangle[1].getX()+" "+triangle[1].getY()+" "+triangle[1].getZ()+") ";
				out+="("+triangle[2].getX()+" "+triangle[2].getY()+" "+triangle[2].getZ()+")\""+(char)0x0D+(char)0x0A;
				out+="			\"material\" \"" + texture + "\""+(char)0x0D+(char)0x0A;
				out+="			\"uaxis\" \"["+textureS.getX()+" "+textureS.getY()+" "+textureS.getZ()+" "+textureShiftS+"] "+texScaleX+"\""+(char)0x0D+(char)0x0A;
				out+="			\"vaxis\" \"["+textureT.getX()+" "+textureT.getY()+" "+textureT.getZ()+" "+textureShiftT+"] "+texScaleY+"\""+(char)0x0D+(char)0x0A;
				out+="			\"rotation\" \""+texRot+"\""+(char)0x0D+(char)0x0A;
				out+="			\"lightmapscale\" \""+lgtScale+"\""+(char)0x0D+(char)0x0A;
				out+="			\"smoothing_groups\" \"0\""+(char)0x0D+(char)0x0A+"		}"+(char)0x0D+(char)0x0A;
				return out;
			}
		} catch(java.lang.NullPointerException e) {
			Window.println("WARNING: Side with bad data! Not exported!",Window.VERBOSITY_WARNINGS);
			return null;
		}
	}
	
	// Turn a Q2 entity into a Hammer one. This won't magically fix every single
	// thing to work in Gearcraft, for example the Nightfire engine had no support
	// for area portals. But it should save map porters some time, especially when
	// it comes to the Capture The Flag mod.
	public Entity ent38ToEntVMF(Entity in) {
		if(!in.getAttribute("angle").equals("")) {
			in.setAttribute("angles", "0 "+in.getAttribute("angle")+" 0");
			in.deleteAttribute("angle");
		}
		if(in.getAttribute("classname").equalsIgnoreCase("func_wall")) {
			in.setAttribute("classname", "func_brush");
			if(!in.getAttribute("targetname").equals("")) { // Really this should depend on spawnflag 2 or 4
				in.setAttribute("solidity", "0"); // TODO: Make sure the attribute is actually "solidity"
			} else { // 2 I believe is "Start enabled" and 4 is "toggleable", or the other way around. Not sure. Could use an OR.
				in.setAttribute("solidity", "2");
			}
		} else {
			if(in.getAttribute("classname").equalsIgnoreCase("info_player_start")) {
				double[] origin=in.getOrigin();
				in.setAttribute("origin", origin[X]+" "+origin[Y]+" "+(origin[Z]+18));
			} else {
				if(in.getAttribute("classname").equalsIgnoreCase("info_player_deathmatch")) {
					double[] origin=in.getOrigin();
					in.setAttribute("origin", origin[X]+" "+origin[Y]+" "+(origin[Z]+18));
				} else {
					if(in.getAttribute("classname").equalsIgnoreCase("light")) {
						String color=in.getAttribute("_color");
						String intensity=in.getAttribute("light");
						Scanner colorScanner=new Scanner(color);
						double[] lightNumbers=new double[4];
						for(int j=0;j<3 && colorScanner.hasNext();j++) {
							try {
								lightNumbers[j]=Double.parseDouble(colorScanner.next());
								lightNumbers[j]*=255; // Quake 2's numbers are from 0 to 1, Nightfire are from 0 to 255
							} catch(java.lang.NumberFormatException e) {
								;
							}
						}
						try {
							lightNumbers[s]=Double.parseDouble(intensity)/2; // Quake 2's light intensity is waaaaaay too bright
						} catch(java.lang.NumberFormatException e) {
							;
						}
						in.deleteAttribute("_color");
						in.deleteAttribute("light");
						in.setAttribute("_light", lightNumbers[r]+" "+lightNumbers[g]+" "+lightNumbers[b]+" "+lightNumbers[s]);
					} else {
						if(in.getAttribute("classname").equalsIgnoreCase("misc_teleporter")) {
							double[] origin=in.getOrigin();
							Vector3D mins=new Vector3D(origin[X]-24, origin[Y]-24, origin[Z]-24);
							Vector3D maxs=new Vector3D(origin[X]+24, origin[Y]+24, origin[Z]+48);
							in.addBrush(GenericMethods.createBrush(mins,maxs,"tools/toolstrigger"));
							in.deleteAttribute("origin");
							in.setAttribute("classname", "trigger_teleport");
						} else {
							if(in.getAttribute("classname").equalsIgnoreCase("misc_teleporter_dest")) {
								in.setAttribute("classname", "info_target");
							}
						}
					}
				}
			}
		}
		return in;
	}
	
	// Turn a Nightfire entity into a Hammer one.
	public Entity ent42ToEntVMF(Entity in) {
		if(!in.getAttribute("body").equalsIgnoreCase("")) {
			in.renameAttribute("body", "SetBodyGroup");
		}
		if(in.getAttribute("rendercolor").equals("0 0 0")) {
			in.setAttribute("rendercolor", "255 255 255");
		}
		try {
			if(in.getAttribute("model").substring(in.getAttribute("model").length()-4).equalsIgnoreCase(".spz")) {
				in.setAttribute("model", in.getAttribute("model").substring(0, in.getAttribute("model").length()-4)+".spr");
			}
		} catch(java.lang.StringIndexOutOfBoundsException e) {
			;
		}
		if(in.getAttribute("classname").equalsIgnoreCase("light_spot")) {
			in.setAttribute("pitch", new Double(in.getAngles()[0]).toString());
			in.renameAttribute("_cone", "_inner_cone"); 
			in.renameAttribute("_cone2", "_cone");
			try {
				if(Double.parseDouble(in.getAttribute("_cone"))>90.0) {
					in.setAttribute("_cone", "90");
				} else {
					if(Double.parseDouble(in.getAttribute("_cone"))<0.0) {
						in.setAttribute("_cone", "0");
					}
				}
				if(Double.parseDouble(in.getAttribute("_cone2"))>90.0) {
					in.setAttribute("_cone2", "90");
				} else {
					if(Double.parseDouble(in.getAttribute("_cone2"))<0.0) {
						in.setAttribute("_cone2", "0");
					}
				}
			} catch(java.lang.NumberFormatException e) {
				;
			}
		} else {
			if(in.getAttribute("classname").equalsIgnoreCase("func_wall")) {
				if(in.getAttribute("rendermode").equals("0")) {
					in.setAttribute("classname", "func_detail");
					in.deleteAttribute("rendermode");
				} else {
					in.setAttribute("classname", "func_brush");
					in.setAttribute("solidity", "2");
				}
			} else {
				if(in.getAttribute("classname").equalsIgnoreCase("func_wall_toggle")) {
					in.setAttribute("classname", "func_brush");
					in.setAttribute("solidity", "0");
					try {
						if((Double.parseDouble(in.getAttribute("spawnflags")))/2.0 == Double.parseDouble(in.getAttribute("spawnflags"))) {
							in.setAttribute("StartDisabled", "1"); // If spawnflags is an odd number, the start disabled flag is set.
						} else {
							in.setAttribute("StartDisabled", "0");
						}
					} catch(java.lang.NumberFormatException e) {
						in.setAttribute("StartDisabled", "0");
					}
				} else {
					if(in.getAttribute("classname").equalsIgnoreCase("func_illusionary")) {
						in.setAttribute("classname", "func_brush");
						in.setAttribute("solidity", "1");
					} else {
						if(in.getAttribute("classname").equalsIgnoreCase("item_generic")) {
							in.setAttribute("classname", "prop_dynamic");
							in.setAttribute("solid", "0");
							in.deleteAttribute("effects");
							in.deleteAttribute("fixedlight");
						} else {
							if(in.getAttribute("classname").equalsIgnoreCase("env_glow")) {
								in.setAttribute("classname", "env_sprite");
							} else {
								if(in.getAttribute("classname").equalsIgnoreCase("info_teleport_destination")) {
									in.setAttribute("classname", "info_target");
								}
							}
						}
					}
				}
			}
		}
		return in;
	}
	
	// ACCESSORS/MUTATORS
	
}