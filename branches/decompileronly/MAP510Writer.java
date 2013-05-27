// MAP510Writer class
//
// Writes a Gearcraft .MAP file from a passed Entities object

import java.io.File;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.util.Scanner;

public class MAP510Writer {

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
	private int BSPVersion;
	
	private int currentEntity;
	
	private static DecimalFormat fmtPoints = new DecimalFormat("0.000000");
	private static DecimalFormat fmtScales = new DecimalFormat("0.####");
	
	private static boolean ctfEnts=false;
	
	// CONSTRUCTORS
	
	public MAP510Writer(Entities from, String to, int BSPVersion) {
		this.data=from;
		this.path=to;
		this.BSPVersion=BSPVersion;
	}
	
	// METHODS
	
	// write()
	// Saves the lump to the specified path.
	// Handling file I/O with Strings is generally a bad idea. If you have maybe a couple hundred
	// Strings to write then it'll probably be okay, but when you have on the order of 10,000 Strings
	// it gets VERY slow, even if you concatenate them all before writing.
	public void write() throws java.io.IOException, java.lang.InterruptedException {
		// Preprocessing entity corrections
		if(BSPVersion!=42) {
			Entity waterEntity=null;
			boolean newEnt=false;
			for(int i=0;i<data.size();i++) {
				if(data.getElement(i).getAttribute("classname").equalsIgnoreCase("func_water")) {
					waterEntity=data.getElement(i);
					break;
				} else {
					try {
						if(data.getElement(i).getAttribute("classname").substring(0,8).equalsIgnoreCase("team_CTF") || data.getElement(i).getAttribute("classname").equalsIgnoreCase("ctf_flag_hardcorps")) {
							ctfEnts=true;
						}
					} catch(java.lang.StringIndexOutOfBoundsException e) {
						;
					}
				}
			}
			if(waterEntity==null) {
				newEnt=true;
				waterEntity=new Entity("func_water");
				waterEntity.setAttribute("rendercolor", "0 0 0");
				waterEntity.setAttribute("speed", "100");
				waterEntity.setAttribute("wait", "4");
				waterEntity.setAttribute("skin", "-3");
				waterEntity.setAttribute("WaveHeight", "3.2");
			} // TODO: Y U NO WORK?!?!?
			for(int i=0;i<data.getElement(0).getNumBrushes();i++) {
				if(data.getElement(0).getBrushes()[i].isWaterBrush()) {
					waterEntity.addBrush(data.getElement(0).getBrushes()[i]);
					data.getElement(0).deleteBrush(i);
				}
			}
			if(newEnt && waterEntity.getBrushes().length!=0) {
				data.add(waterEntity);
			}
		}
		// Correct some attributes of entities
		for(int i=0;i<data.size();i++) {
			Entity current=data.getElement(i);
			switch(BSPVersion) {
				case BSP.TYPE_QUAKE3:
				case BSP.TYPE_FAKK:
				case BSP.TYPE_RAVEN:
				case BSP.TYPE_MOHAA:
				case BSP.TYPE_STEF2:
				case BSP.TYPE_STEF2DEMO:
					current=ent46ToEntM510(current);
					break;
				case BSP.TYPE_NIGHTFIRE: // Nightfire
					current=ent42ToEntM510(current);
					break;
				case BSP.TYPE_QUAKE2:
				case BSP.TYPE_SIN:
				case BSP.TYPE_SOF:
					current=ent38ToEntM510(current);
					break;
				case DoomMap.TYPE_DOOM:
				case DoomMap.TYPE_HEXEN:
					break;
				case BSP.TYPE_COD:
					current=ent59ToEntM510(current);
					break;
				case BSP.TYPE_SOURCE17:
				case BSP.TYPE_SOURCE18:
				case BSP.TYPE_SOURCE19:
				case BSP.TYPE_SOURCE20:
				case BSP.TYPE_SOURCE21:
				case BSP.TYPE_SOURCE22:
				case BSP.TYPE_SOURCE23:
				case BSP.TYPE_DMOMAM:
				case BSP.TYPE_VINDICTUS:
				case BSP.TYPE_TACTICALINTERVENTION:
					current=entSourceToEntM510(current);
					break;
			}
		}
		
		byte[][] entityBytes=new byte[data.size()][];
		int totalLength=0;
		for(currentEntity=0;currentEntity<data.size();currentEntity++) {
			if(Thread.currentThread().interrupted()) {
				throw new java.lang.InterruptedException("while writing GearCraft map.");
			}
			try {
				entityBytes[currentEntity]=entityToByteArray(data.getElement(currentEntity), currentEntity);
			} catch(java.lang.ArrayIndexOutOfBoundsException e) { // This happens when entities are added after the array is made
				byte[][] newList=new byte[data.size()][]; // Create a new array with the new length
				for(int j=0;j<entityBytes.length;j++) {
					newList[j]=entityBytes[j];
				}
				newList[currentEntity]=entityToByteArray(data.getElement(currentEntity), currentEntity);
				entityBytes=newList;
			}
			totalLength+=entityBytes[currentEntity].length;
		}
		byte[] allEnts=new byte[totalLength];
		int offset=0;
		for(int i=0;i<data.size();i++) {
			for(int j=0;j<entityBytes[i].length;j++) {
				allEnts[offset+j]=entityBytes[i][j];
			}
			offset+=entityBytes[i].length;
		}
		MAPMaker.write(allEnts, path, false);
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
	private byte[] entityToByteArray(Entity in, int num) throws java.lang.InterruptedException {
		byte[] out;
		double[] origin=in.getOrigin();
		if(in.getAttribute("classname").equalsIgnoreCase("worldspawn")) {
			in.setAttribute("mapversion", "510");
			if(ctfEnts) {
				in.setAttribute("defaultctf", "1");
			}
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
			if(Thread.currentThread().interrupted()) {
				throw new java.lang.InterruptedException("while writing GearCraft map.");
			}
			if(in.getAttributes()[i].equals("{")) {
				in.getAttributes()[i]="{ // Entity "+num;
			} else {
				if(in.getAttributes()[i].equals("}")) {
					int brushArraySize=0;
					byte[][] brushes=new byte[in.getBrushes().length][];
					for(int j=0;j<in.getBrushes().length;j++) { // For each brush in the entity
						if(Thread.currentThread().interrupted()) {
							throw new java.lang.InterruptedException("while writing GearCraft map.");
						}
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
		String brush="{ // Brush "+num+(char)0x0D+(char)0x0A;
		if((in.isDetailBrush() || in.getSide(0).getDisplacement()!=null) && currentEntity==0) {
			brush+="\"BRUSHFLAGS\" \"DETAIL\""+(char)0x0D+(char)0x0A;
		}
		for(int i=0;i<in.getNumSides();i++) {
			brush+=brushSideToString(in.getSide(i))+(char)0x0D+(char)0x0A;
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
			// Correct special textures on Q2 maps
			if(!Window.noTexCorrectionsIsSelected()) {
				if(BSPVersion==BSP.TYPE_QUAKE2 || BSPVersion==BSP.TYPE_SIN) { // Many of the special textures are taken care of in the decompiler method itself
					try {                                                      // using face flags, rather than texture names.
						if(texture.substring(texture.length()-8).equalsIgnoreCase("/trigger")) {
							texture="special/trigger";
						} else {
							if(texture.substring(texture.length()-5).equalsIgnoreCase("/clip")) {
								texture="special/clip";
							} else {
								if(texture.equalsIgnoreCase("*** unsused_texinfo ***")) {
									texture="special/nodraw";
								}
							}
						}
					} catch(StringIndexOutOfBoundsException e) {
						;
					}
				}
				if(BSPVersion==BSP.TYPE_SOURCE17 || BSPVersion==BSP.TYPE_SOURCE18 || BSPVersion==BSP.TYPE_SOURCE19 || BSPVersion==BSP.TYPE_SOURCE20 || BSPVersion==BSP.TYPE_SOURCE21 || BSPVersion==BSP.TYPE_SOURCE22 || BSPVersion==BSP.TYPE_SOURCE23 || BSPVersion==BSP.TYPE_DMOMAM || BSPVersion==BSP.TYPE_VINDICTUS || BSPVersion==BSP.TYPE_TACTICALINTERVENTION) {
					try {
						if(texture.equalsIgnoreCase("tools/toolshint")) {
							texture="special/hint";
						} else {
							if(texture.equalsIgnoreCase("tools/toolsskip")) {
								texture="special/skip";
							} else {
								if(texture.equalsIgnoreCase("tools/toolsclip")) {
									texture="special/clip";
								} else {
									if(texture.equalsIgnoreCase("tools/toolstrigger") || texture.equalsIgnoreCase("TOOLS/TOOLSFOG")) {
										texture="special/trigger";
									} else {
										if(texture.equalsIgnoreCase("tools/TOOLSSKYBOX")) {
											texture="special/sky";
										} else {
											if(texture.equalsIgnoreCase("tools/toolsnodraw")) {
												texture="special/nodraw";
											} else {
												if(texture.equalsIgnoreCase("TOOLS/TOOLSPLAYERCLIP")) {
													texture="special/playerclip";
												} else {
													if(texture.equalsIgnoreCase("TOOLS/TOOLSNPCCLIP")) {
														texture="special/enemyclip";
													} else {
														if(texture.equalsIgnoreCase("TOOLS/TOOLSBLACK")) {
															texture="special/black";
														} else {
															if(texture.equalsIgnoreCase("TOOLS/TOOLSINVISIBLE")) {
																texture="special/clip";
															} else {
																if(texture.substring(0,5).equalsIgnoreCase("maps/")) {
																	texture=texture.substring(5);
																	for(int i=0;i<texture.length();i++) {
																		if(texture.charAt(i)=='/') {
																			texture=texture.substring(i+1);
																			break;
																		}
																	}
																}
																// Find cubemap textures
																int numUnderscores=0;
																boolean validnumber=false;
																for(int i=texture.length()-1;i>0;i--) {
																	if(texture.charAt(i)<='9' && texture.charAt(i)>='0') { // Current is a number, start building string
																		validnumber=true;
																	} else {
																		if(texture.charAt(i)=='-') { // Current is a minus sign (-).
																			if(!validnumber) {
																				break; // Make sure there's a number to add the minus sign to. If not, kill the loop.
																			}
																		} else {
																			if(texture.charAt(i)=='_') { // Current is an underscore (_)
																				if(validnumber) { // Make sure there is a number in the current string
																					numUnderscores++; // before moving on to the next one.
																					validnumber=false;
																					if(numUnderscores==3) { // If we've got all our numbers
																						texture=texture.substring(0,i); // Cut the texture string
																						break; // Kill the loop, we're done
																					}
																				} else { // No number after the underscore
																					break;
																				}
																			} else { // Not an acceptable character
																				break;
																			}
																		}
																	}
																}
															}
														}
													}
												}
											}
										}
									}
								}
							}
						}
					} catch(StringIndexOutOfBoundsException e) {
						;
					}
				}
				if(BSPVersion==BSP.TYPE_QUAKE3 || BSPVersion==BSP.TYPE_MOHAA || BSPVersion==BSP.TYPE_COD || BSPVersion==BSP.TYPE_STEF2 || BSPVersion==BSP.TYPE_STEF2DEMO || BSPVersion==BSP.TYPE_FAKK) {
					try {
						if(texture.substring(0,9).equalsIgnoreCase("textures/")) {
							texture=texture.substring(9);
						}
					} catch(StringIndexOutOfBoundsException e) {
						;
					}
					if(texture.equalsIgnoreCase("common/clip")) {
						texture="special/clip";
					} else {
						if(texture.equalsIgnoreCase("common/trigger")) {
							texture="special/trigger";
						} else {
							if(texture.equalsIgnoreCase("noshader")) {
								texture="special/nodraw";
							} else {
								if(texture.equalsIgnoreCase("common/physics_clip")) {
									texture="special/clip";
								} else {
									if(texture.equalsIgnoreCase("common/caulk") || texture.equalsIgnoreCase("common/caulkshadow")) {
										texture="special/nodraw";
									} else {
										if(texture.equalsIgnoreCase("common/do_not_enter") || texture.equalsIgnoreCase("common/donotenter") || texture.equalsIgnoreCase("common/monsterclip")) {
											texture="special/npcclip";
										} else {
											if(texture.equalsIgnoreCase("common/caulksky") || texture.equalsIgnoreCase("common/skyportal")) {
												texture="special/sky";
											} else {
												if(texture.equalsIgnoreCase("common/hint")) {
													texture="special/hint";
												} else {
													if(texture.equalsIgnoreCase("common/nodraw")) {
														texture="special/nodraw";
													} else {
														if(texture.equalsIgnoreCase("common/metalclip")) {
															texture="special/clip";
														} else {
															if(texture.equalsIgnoreCase("common/grassclip")) {
																texture="special/clip";
															} else {
																if(texture.equalsIgnoreCase("common/paperclip")) {
																	texture="special/clip";
																} else {
																	if(texture.equalsIgnoreCase("common/woodclip")) {
																		texture="special/clip";
																	} else {
																		if(texture.equalsIgnoreCase("common/waterskip")) {
																			texture="liquids/!water";
																		} else {
																			if(texture.equalsIgnoreCase("common/glassclip")) {
																				texture="special/clip";
																			} else {
																				if(texture.equalsIgnoreCase("common/playerclip")) {
																					texture="special/playerclip";
																				} else {
																					if(texture.equalsIgnoreCase("common/nodrawnonsolid")) {
																						texture="special/trigger";
																					} else {
																						if(texture.equalsIgnoreCase("common/clipfoliage")) {
																							texture="special/clip";
																						} else {
																							if(texture.equalsIgnoreCase("common/foliageclip")) {
																								texture="special/clip";
																							} else {
																								if(texture.equalsIgnoreCase("common/carpetclip")) {
																									texture="special/clip";
																								} else {
																									if(texture.equalsIgnoreCase("common/dirtclip")) {
																										texture="special/clip";
																									} else {
																										try {
																											if(texture.substring(0,4).equalsIgnoreCase("sky/")) {
																												texture="special/sky";
																											}
																										} catch(java.lang.StringIndexOutOfBoundsException e) {
																											; // I couldn't give a fuck
																										}
																									}
																								}
																							}
																						}
																					}
																				}
																			}
																		}
																	}
																}
															}
														}
													}
												}
											}
										}
									}
								}
							}
						}
					}
				}
				if(BSPVersion==BSP.TYPE_RAVEN) {
					try {
						if(texture.substring(0,9).equalsIgnoreCase("textures/")) {
							texture=texture.substring(9);
						}
					} catch(StringIndexOutOfBoundsException e) {
						;
					}
					if(texture.equalsIgnoreCase("system/clip")) {
						texture="special/clip";
					} else {
						if(texture.equalsIgnoreCase("system/trigger")) {
							texture="special/trigger";
						} else {
							if(texture.equalsIgnoreCase("noshader")) {
								texture="special/nodraw";
							} else {
								if(texture.equalsIgnoreCase("system/physics_clip")) {
									texture="special/clip";
								} else {
									if(texture.equalsIgnoreCase("system/caulk")) {
										texture="special/nodraw";
									} else {
										if(texture.equalsIgnoreCase("system/do_not_enter")) {
											texture="special/nodraw";
										}
									}
								}
							}
						}
					}
				}
			}
			if(Window.roundNumsIsSelected()) {
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
			Window.println("WARNING: Side with bad data! Not exported!",Window.VERBOSITY_WARNINGS);
			return "";
		}
	}
	
	public Entity ent42ToEntM510(Entity in) {
		if(in.isBrushBased()) {
			double[] origin=in.getOrigin();
			in.deleteAttribute("origin");
			in.deleteAttribute("model");
			if((origin[0]!=0 || origin[1]!=0 || origin[2]!=0) && !Window.noOriginBrushesIsSelected()) { // If this brush uses the "origin" attribute
				MAPBrush newOriginBrush=GenericMethods.createBrush(new Vector3D(-Window.getOriginBrushSize(),-Window.getOriginBrushSize(),-Window.getOriginBrushSize()),new Vector3D(Window.getOriginBrushSize(),Window.getOriginBrushSize(),Window.getOriginBrushSize()),"special/origin");
				in.addBrush(newOriginBrush);
			}
			for(int i=0;i<in.getNumBrushes();i++) {
				MAPBrush currentBrush=in.getBrush(i);
				currentBrush.translate(new Vector3D(origin));
			}
		}
		return in;
	}
	
	// Turn a CoD entity into a Gearcraft one.
	public Entity ent59ToEntM510(Entity in) {
		if(in.isBrushBased()) {
			double[] origin=in.getOrigin();
			in.deleteAttribute("origin");
			in.deleteAttribute("model");
			if(in.getAttribute("classname").equalsIgnoreCase("func_rotating")) { // TODO: What entities require origin brushes in CoD?
				if((origin[0]!=0 || origin[1]!=0 || origin[2]!=0) && !Window.noOriginBrushesIsSelected()) { // If this brush uses the "origin" attribute
					MAPBrush newOriginBrush=GenericMethods.createBrush(new Vector3D(-Window.getOriginBrushSize(),-Window.getOriginBrushSize(),-Window.getOriginBrushSize()),new Vector3D(Window.getOriginBrushSize(),Window.getOriginBrushSize(),Window.getOriginBrushSize()),"special/origin");
					in.addBrush(newOriginBrush);
				}
			}
			for(int i=0;i<in.getNumBrushes();i++) {
				MAPBrush currentBrush=in.getBrush(i);
				currentBrush.translate(new Vector3D(origin));
			}
		} else {
			if(in.getAttribute("classname").equalsIgnoreCase("light")) {
				in.setAttribute("_light", "255 255 255 "+in.getAttribute("light"));
				in.deleteAttribute("light");
			} else {
				if(in.getAttribute("classname").equalsIgnoreCase("mp_deathmatch_spawn")) {
					in.setAttribute("classname", "info_player_deathmatch");
				} else {
					if(in.getAttribute("classname").equalsIgnoreCase("mp_teamdeathmatch_spawn")) {
						in.setAttribute("classname", "info_player_deathmatch");
					} else {
						if(in.getAttribute("classname").equalsIgnoreCase("mp_searchanddestroy_spawn_allied")) {
							in.setAttribute("classname", "info_player_ctfspawn");
							in.setAttribute("team_no", "1");
							in.deleteAttribute("model");
						} else {
							if(in.getAttribute("classname").equalsIgnoreCase("mp_searchanddestroy_spawn_axis")) {
								in.setAttribute("classname", "info_player_ctfspawn");
								in.setAttribute("team_no", "2");
								in.deleteAttribute("model");
							}
						}
					}
				}
			}
		}
		return in;
	}
	
	// Turn a Q3 entity into a Gearcraft one (generally for use with nightfire)
	// This won't magically fix every single thing to work in Gearcraft, for example
	// the Nightfire engine had no support for area portals. But it should save map
	// porters some time, especially when it comes to the Capture The Flag mod.
	public Entity ent46ToEntM510(Entity in) {
		if(in.isBrushBased()) {
			double[] origin=in.getOrigin();
			in.deleteAttribute("origin");
			in.deleteAttribute("model");
			if(in.attributeIs("classname", "func_rotating") || in.attributeIs("classname", "func_rotatingdoor")) { // TODO: What entities require origin brushes in CoD?
				if((origin[0]!=0 || origin[1]!=0 || origin[2]!=0) && !Window.noOriginBrushesIsSelected()) { // If this brush uses the "origin" attribute
					MAPBrush newOriginBrush=GenericMethods.createBrush(new Vector3D(-Window.getOriginBrushSize(),-Window.getOriginBrushSize(),-Window.getOriginBrushSize()),new Vector3D(Window.getOriginBrushSize(),Window.getOriginBrushSize(),Window.getOriginBrushSize()),"special/origin");
					in.addBrush(newOriginBrush);
				}
			}
			for(int i=0;i<in.getNumBrushes();i++) {
				MAPBrush currentBrush=in.getBrush(i);
				currentBrush.translate(new Vector3D(origin));
			}
		}
		if(in.getAttribute("classname").equalsIgnoreCase("team_CTF_blueflag")) { // Blue flag
			in.setAttribute("classname", "item_ctfflag");
			in.setAttribute("skin", "1"); // 0 for PHX, 1 for MI6
			in.setAttribute("goal_no", "1"); // 2 for PHX, 1 for MI6
			in.setAttribute("goal_max", "16 16 72");
			in.setAttribute("goal_min", "-16 -16 0");
			in.setAttribute("model", "models/ctf_flag.mdl");
			Entity flagBase=new Entity("item_ctfbase");
			flagBase.setAttribute("origin", in.getAttribute("origin"));
			flagBase.setAttribute("angles", in.getAttribute("angles"));
			flagBase.setAttribute("angle", in.getAttribute("angle"));
			flagBase.setAttribute("goal_no", "1");
			flagBase.setAttribute("model", "models/ctf_flag_stand_mi6.mdl");
			flagBase.setAttribute("goal_max", "16 16 72");
			flagBase.setAttribute("goal_min", "-16 -16 0");
			data.add(flagBase);
		} else {
			if(in.getAttribute("classname").equalsIgnoreCase("team_CTF_redflag")) { // Red flag
				in.setAttribute("classname", "item_ctfflag");
				in.setAttribute("skin", "0"); // 0 for PHX, 1 for MI6
				in.setAttribute("goal_no", "2"); // 2 for PHX, 1 for MI6
				in.setAttribute("goal_max", "16 16 72");
				in.setAttribute("goal_min", "-16 -16 0");
				in.setAttribute("model", "models/ctf_flag.mdl");
				Entity flagBase=new Entity("item_ctfbase");
				flagBase.setAttribute("origin", in.getAttribute("origin"));
				flagBase.setAttribute("angles", in.getAttribute("angles"));
				flagBase.setAttribute("angle", in.getAttribute("angle"));
				flagBase.setAttribute("goal_no", "2");
				flagBase.setAttribute("model", "models/ctf_flag_stand_phoenix.mdl");
				flagBase.setAttribute("goal_max", "16 16 72");
				flagBase.setAttribute("goal_min", "-16 -16 0");
				data.add(flagBase);
			} else {
				if(in.getAttribute("classname").equalsIgnoreCase("team_CTF_redspawn") || in.getAttribute("classname").equalsIgnoreCase("info_player_axis")) {
					in.setAttribute("classname", "info_ctfspawn");
					in.setAttribute("team_no", "2");
					double[] origin=in.getOrigin();
					in.setAttribute("origin", origin[X]+" "+origin[Y]+" "+(origin[Z]+24));
				} else {
					if(in.getAttribute("classname").equalsIgnoreCase("team_CTF_bluespawn") || in.getAttribute("classname").equalsIgnoreCase("info_player_allied")) {
						in.setAttribute("classname", "info_ctfspawn");
						in.setAttribute("team_no", "1");
						double[] origin=in.getOrigin();
						in.setAttribute("origin", origin[X]+" "+origin[Y]+" "+(origin[Z]+24));
					} else {
						if(in.getAttribute("classname").equalsIgnoreCase("info_player_start")) {
							double[] origin=in.getOrigin();
							in.setAttribute("origin", origin[X]+" "+origin[Y]+" "+(origin[Z]+24));
						} else {
							if(in.getAttribute("classname").equalsIgnoreCase("info_player_coop")) {
								double[] origin=in.getOrigin();
								in.setAttribute("origin", origin[X]+" "+origin[Y]+" "+(origin[Z]+24));
							} else {
								if(in.getAttribute("classname").equalsIgnoreCase("info_player_deathmatch")) {
									double[] origin=in.getOrigin();
									in.setAttribute("origin", origin[X]+" "+origin[Y]+" "+(origin[Z]+24));
								} else {
									if(in.getAttribute("classname").equalsIgnoreCase("light")) {
										String color=in.getAttribute("_color");
										String intensity=in.getAttribute("light");
										Scanner colorScanner=new Scanner(color);
										double[] lightNumbers=new double[4];
										for(int j=0;j<3 && colorScanner.hasNext();j++) {
											try {
												lightNumbers[j]=Double.parseDouble(colorScanner.next());
												lightNumbers[j]*=255; // Quake 3's numbers are from 0 to 1, Nightfire are from 0 to 255
											} catch(java.lang.NumberFormatException e) {
												;
											}
										}
										try {
											lightNumbers[s]=Double.parseDouble(intensity);
										} catch(java.lang.NumberFormatException e) {
											;
										}
										in.deleteAttribute("_color");
										in.deleteAttribute("light");
										in.setAttribute("_light", lightNumbers[r]+" "+lightNumbers[g]+" "+lightNumbers[b]+" "+lightNumbers[s]);
									} else {
										if(in.getAttribute("classname").equalsIgnoreCase("func_rotatingdoor")) {
											in.setAttribute("classname", "func_door_rotating");
										} else {
											if(in.getAttribute("classname").equalsIgnoreCase("info_pathnode")) {
												in.setAttribute("classname", "info_node");
											} else {
												if(in.getAttribute("classname").equalsIgnoreCase("trigger_ladder")) {
													in.setAttribute("classname", "func_ladder");
												} else {
													if(in.getAttribute("classname").equalsIgnoreCase("worldspawn")) {
														if(!in.getAttribute("suncolor").equals("")) {
															Entity light_environment=new Entity("light_environment");
															light_environment.setAttribute("_light", in.getAttribute("suncolor"));
															light_environment.setAttribute("angles", in.getAttribute("sundirection"));
															light_environment.setAttribute("_fade", in.getAttribute("sundiffuse"));
															in.deleteAttribute("suncolor");
															in.deleteAttribute("sundirection");
															in.deleteAttribute("sundiffuse");
															in.deleteAttribute("sundiffusecolor");
															data.add(light_environment);
														}
													} else {
														if(in.getAttribute("classname").equalsIgnoreCase("trigger_use")) {
															in.setAttribute("classname", "func_button");
															in.setAttribute("spawnflags", "1");
															in.setAttribute("wait", "1");
														}
													}
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
		return in;
	}

	
	// Turn a Q2 entity into a Gearcraft one (generally for use with nightfire)
	// This won't magically fix every single thing to work in Gearcraft, for example
	// the Nightfire engine had no support for area portals. But it should save map
	// porters some time, especially when it comes to the Capture The Flag mod.
	public Entity ent38ToEntM510(Entity in) {
		if(!in.getAttribute("angle").equals("")) {
			in.setAttribute("angles", "0 "+in.getAttribute("angle")+" 0");
			in.deleteAttribute("angle");
		}
		if(in.isBrushBased()) {
			double[] origin=in.getOrigin();
			in.deleteAttribute("origin");
			in.deleteAttribute("model");
			if(in.attributeIs("classname", "func_rotating")) { // TODO: What entities require origin brushes in CoD?
				if((origin[0]!=0 || origin[1]!=0 || origin[2]!=0) && !Window.noOriginBrushesIsSelected()) { // If this brush uses the "origin" attribute
					MAPBrush newOriginBrush=GenericMethods.createBrush(new Vector3D(-Window.getOriginBrushSize(),-Window.getOriginBrushSize(),-Window.getOriginBrushSize()),new Vector3D(Window.getOriginBrushSize(),Window.getOriginBrushSize(),Window.getOriginBrushSize()),"special/origin");
					in.addBrush(newOriginBrush);
				}
			}
			for(int i=0;i<in.getNumBrushes();i++) {
				MAPBrush currentBrush=in.getBrush(i);
				currentBrush.translate(new Vector3D(origin));
			}
		}
		if(in.getAttribute("classname").equalsIgnoreCase("func_wall")) {
			if(!in.getAttribute("targetname").equals("")) { // Really this should depend on spawnflag 2 or 4
				in.setAttribute("classname", "func_wall_toggle");
			} // 2 I believe is "Start enabled" and 4 is "toggleable", or the other way around. Not sure. Could use an OR.
		} else {
			if(in.getAttribute("classname").equalsIgnoreCase("item_flag_team2") || in.getAttribute("classname").equalsIgnoreCase("ctf_flag_hardcorps")) { // Blue flag
				in.setAttribute("classname", "item_ctfflag");
				in.setAttribute("skin", "1"); // 0 for PHX, 1 for MI6
				in.setAttribute("goal_no", "1"); // 2 for PHX, 1 for MI6
				in.setAttribute("goal_max", "16 16 72");
				in.setAttribute("goal_min", "-16 -16 0");
				Entity flagBase=new Entity("item_ctfbase");
				flagBase.setAttribute("origin", in.getAttribute("origin"));
				flagBase.setAttribute("angles", in.getAttribute("angles"));
				flagBase.setAttribute("angle", in.getAttribute("angle"));
				flagBase.setAttribute("goal_no", "1");
				flagBase.setAttribute("model", "models/ctf_flag_stand_mi6.mdl");
				flagBase.setAttribute("goal_max", "16 16 72");
				flagBase.setAttribute("goal_min", "-16 -16 0");
				data.add(flagBase);
			} else {
				if(in.getAttribute("classname").equalsIgnoreCase("item_flag_team1") || in.getAttribute("classname").equalsIgnoreCase("ctf_flag_sintek")) { // Red flag
					in.setAttribute("classname", "item_ctfflag");
					in.setAttribute("skin", "0"); // 0 for PHX, 1 for MI6
					in.setAttribute("goal_no", "2"); // 2 for PHX, 1 for MI6
					in.setAttribute("goal_max", "16 16 72");
					in.setAttribute("goal_min", "-16 -16 0");
					Entity flagBase=new Entity("item_ctfbase");
					flagBase.setAttribute("origin", in.getAttribute("origin"));
					flagBase.setAttribute("angles", in.getAttribute("angles"));
					flagBase.setAttribute("angle", in.getAttribute("angle"));
					flagBase.setAttribute("goal_no", "2");
					flagBase.setAttribute("model", "models/ctf_flag_stand_phoenix.mdl");
					flagBase.setAttribute("goal_max", "16 16 72");
					flagBase.setAttribute("goal_min", "-16 -16 0");
					data.add(flagBase);
				} else {
					if(in.getAttribute("classname").equalsIgnoreCase("info_player_team1") || in.getAttribute("classname").equalsIgnoreCase("info_player_sintek")) {
						in.setAttribute("classname", "info_ctfspawn");
						in.setAttribute("team_no", "2");
					} else {
						if(in.getAttribute("classname").equalsIgnoreCase("info_player_team2") || in.getAttribute("classname").equalsIgnoreCase("info_player_hardcorps")) {
							in.setAttribute("classname", "info_ctfspawn");
							in.setAttribute("team_no", "1");
						} else {
							if(in.getAttribute("classname").equalsIgnoreCase("info_player_start")) {
								double[] origin=in.getOrigin();
								in.setAttribute("origin", origin[X]+" "+origin[Y]+" "+(origin[Z]+18));
							} else {
								if(in.getAttribute("classname").equalsIgnoreCase("info_player_coop")) {
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
												lightNumbers[s]=Double.parseDouble(intensity);
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
												in.addBrush(GenericMethods.createBrush(mins,maxs,"special/trigger"));
												in.deleteAttribute("origin");
												in.setAttribute("classname", "trigger_teleport");
											} else {
												if(in.getAttribute("classname").equalsIgnoreCase("misc_teleporter_dest")) {
													in.setAttribute("classname", "info_teleport_destination");
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
		return in;
	}
	
	private Entity entSourceToEntM510(Entity in) {
		if(in.isBrushBased()) {
			double[] origin=in.getOrigin();
			in.deleteAttribute("origin");
			in.deleteAttribute("model");
			if(in.attributeIs("classname", "func_door_rotating")) { // TODO: What entities require origin brushes?
				if((origin[0]!=0 || origin[1]!=0 || origin[2]!=0) && !Window.noOriginBrushesIsSelected()) { // If this brush uses the "origin" attribute
					MAPBrush newOriginBrush=GenericMethods.createBrush(new Vector3D(-Window.getOriginBrushSize(),-Window.getOriginBrushSize(),-Window.getOriginBrushSize()),new Vector3D(Window.getOriginBrushSize(),Window.getOriginBrushSize(),Window.getOriginBrushSize()),"special/origin");
					in.addBrush(newOriginBrush);
				}
			}
			for(int i=0;i<in.getNumBrushes();i++) {
				MAPBrush currentBrush=in.getBrush(i);
				currentBrush.translate(new Vector3D(origin));
			}
		}
		if(in.getAttribute("classname").equalsIgnoreCase("func_breakable_surf")) {
			in.setAttribute("classname", "func_breakable");
		} else {
			if(in.getAttribute("classname").equalsIgnoreCase("func_brush")) {
				if(in.getAttribute("solidity").equals("0")) {
					in.setAttribute("classname", "func_wall_toggle");
					if(in.getAttribute("StartDisabled").equals("1")) {
						in.setAttribute("spawnflags", "1");
					} else {
						in.setAttribute("spawnflags", "0");
					}
					in.deleteAttribute("StartDisabled");
				} else {
					if(in.getAttribute("solidity").equals("1")) {
						in.setAttribute("classname", "func_illusionary");
					} else {
						in.setAttribute("classname", "func_wall");
					}
				}
				in.deleteAttribute("solidity");
			} else {
				if(in.getAttribute("classname").equalsIgnoreCase("env_fog_controller")) {
					in.setAttribute("classname", "env_fog");
					in.setAttribute("rendercolor", in.getAttribute("fogcolor"));
					in.deleteAttribute("fogcolor");
				} else {
					if(in.getAttribute("classname").equalsIgnoreCase("prop_static")) {
						in.setAttribute("classname", "item_generic");
					} else {
						if(in.getAttribute("classname").equalsIgnoreCase("info_player_rebel") || in.getAttribute("classname").equalsIgnoreCase("info_player_janus") || in.getAttribute("classname").equalsIgnoreCase("ctf_rebel_player_spawn")) {
							in.setAttribute("classname", "info_ctfspawn");
							in.setAttribute("team_no", "2");
							double[] origin=in.getOrigin();
							in.setAttribute("origin", origin[X]+" "+origin[Y]+" "+(origin[Z]+40));
						} else {
							if(in.getAttribute("classname").equalsIgnoreCase("info_player_combine") || in.getAttribute("classname").equalsIgnoreCase("info_player_mi6") || in.getAttribute("classname").equalsIgnoreCase("ctf_combine_player_spawn")) {
								in.setAttribute("classname", "info_ctfspawn");
								in.setAttribute("team_no", "1");
								double[] origin=in.getOrigin();
								in.setAttribute("origin", origin[X]+" "+origin[Y]+" "+(origin[Z]+40));
							} else {
								if(in.getAttribute("classname").equalsIgnoreCase("info_player_deathmatch")) {
									double[] origin=in.getOrigin();
									in.setAttribute("origin", origin[X]+" "+origin[Y]+" "+(origin[Z]+40));
								} else {
									if(in.getAttribute("classname").equalsIgnoreCase("ctf_combine_flag")) {
										in.deleteAttribute("targetname");
										in.deleteAttribute("SpawnWithCaptureEnabled");
										in.setAttribute("skin", "1");
										in.setAttribute("goal_max", "16 16 72");
										in.setAttribute("goal_min", "-16 -16 0");
										in.setAttribute("goal_no", "1");
										in.setAttribute("model", "models/ctf_flag.mdl");
										in.setAttribute("classname", "item_ctfflag");
										Entity newFlagBase = new Entity("item_ctfbase");
										newFlagBase.setAttribute("origin", in.getAttribute("origin"));
										newFlagBase.setAttribute("angles", in.getAttribute("angles"));
										newFlagBase.setAttribute("goal_max", "16 16 72");
										newFlagBase.setAttribute("goal_min", "-16 -16 0");
										newFlagBase.setAttribute("goal_no", "1");
										newFlagBase.setAttribute("model", "models/ctf_flag_stand_mi6.mdl");
										data.add(newFlagBase);
									} else {
										if(in.getAttribute("classname").equalsIgnoreCase("ctf_rebel_flag")) {
											in.deleteAttribute("targetname");
											in.deleteAttribute("SpawnWithCaptureEnabled");
											in.setAttribute("skin", "0");
											in.setAttribute("goal_max", "16 16 72");
											in.setAttribute("goal_min", "-16 -16 0");
											in.setAttribute("goal_no", "2");
											in.setAttribute("model", "models/ctf_flag.mdl");
											in.setAttribute("classname", "item_ctfflag");
											Entity newFlagBase = new Entity("item_ctfbase");
											newFlagBase.setAttribute("origin", in.getAttribute("origin"));
											newFlagBase.setAttribute("angles", in.getAttribute("angles"));
											newFlagBase.setAttribute("goal_max", "16 16 72");
											newFlagBase.setAttribute("goal_min", "-16 -16 0");
											newFlagBase.setAttribute("goal_no", "2");
											newFlagBase.setAttribute("model", "models/ctf_flag_stand_mi6.mdl");
											data.add(newFlagBase);
										}
									}
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