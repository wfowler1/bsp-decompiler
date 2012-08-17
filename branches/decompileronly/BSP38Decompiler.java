// BSP38Decompiler class
// Decompile BSP v38

import java.util.Date;
import java.util.Scanner;

public class BSP38Decompiler {

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
	
	private boolean vertexDecomp;
	private boolean correctPlaneFlip;
	private boolean toHammer;
	private boolean toRadiant;
	private boolean toGearcraft;
	private boolean calcVerts;
	private boolean roundNums;
	
	private int jobnum;
	
	private Entities mapFile; // Most MAP file formats (including GearCraft) are simply a bunch of nested entities
	private int numBrshs;
	private int numSimpleCorrects=0;
	private int numAdvancedCorrects=0;
	private int numGoodBrushes=0;
	
	private v38BSP BSP38;
	
	// CONSTRUCTORS

	// This constructor sets everything according to specified settings.
	public BSP38Decompiler(v38BSP BSP38, boolean vertexDecomp, boolean correctPlaneFlip, boolean calcVerts, boolean roundNums, boolean toHammer, boolean toRadiant, boolean toGearcraft, int jobnum) {
		// Set up global variables
		this.BSP38=BSP38;
		this.vertexDecomp=vertexDecomp;
		this.correctPlaneFlip=correctPlaneFlip;
		this.toHammer=toHammer;
		this.toRadiant=toRadiant;
		this.toGearcraft=toGearcraft;
		this.calcVerts=calcVerts;
		this.roundNums=roundNums;
		this.jobnum=jobnum;
	}
	
	// METHODS

	// Attempt to turn the Quake 2 BSP into a .MAP file
	public void decompile() throws java.io.IOException {
		Date begin=new Date();
		// Begin by copying all the entities into another Lump00 object. This is
		// necessary because if I just modified the current entity list then it
		// could be saved back into the BSP and really mess some stuff up.
		mapFile=new Entities(BSP38.getEntities());
		//int numAreaPortals=0;
		int numTotalItems=0;
		boolean containsAreaPortals=false;
		for(int i=0;i<BSP38.getEntities().getNumElements();i++) { // For each entity
			Window.println("Entity "+i+": "+mapFile.getEntity(i).getAttribute("classname"),4);
			if(toHammer) { // correct some entities to make source ports easier, TODO
				if(mapFile.getEntity(i).getAttribute("classname").equalsIgnoreCase("func_wall")) {
					mapFile.getEntity(i).setAttribute("classname", "func_brush"); // Doubt I need a case for func_detail here
				}
			}
			if(toGearcraft) { // Gearcraft also requires some changes, do those here
				if(mapFile.getEntity(i).getAttribute("classname").equalsIgnoreCase("func_wall")) {
					if(!mapFile.getEntity(i).getAttribute("targetname").equals("")) { // Really this should depend on spawnflag 2 or 4
						mapFile.getEntity(i).setAttribute("classname", "func_wall_toggle");
					} // 2 I believe is "Start enabled" and 4 is "toggleable", or the other way around. Not sure. Could use an OR.
				}
				if(mapFile.getEntity(i).getAttribute("classname").equalsIgnoreCase("item_flag_team2")) { // Blue flag
					mapFile.getEntity(0).setAttribute("defaultctf", "1"); // Turn CTF on in the worldspawn entity
					mapFile.getEntity(i).setAttribute("classname", "item_ctfflag");
					mapFile.getEntity(i).setAttribute("skin", "1"); // 0 for PHX, 1 for MI6
					mapFile.getEntity(i).setAttribute("goal_no", "1"); // 2 for PHX, 1 for MI6
					mapFile.getEntity(i).setAttribute("goal_max", "16 16 72");
					mapFile.getEntity(i).setAttribute("goal_min", "-16 -16 0");
					Entity flagBase=new Entity("item_ctfbase");
					flagBase.setAttribute("origin", mapFile.getEntity(i).getAttribute("origin"));
					flagBase.setAttribute("angles", mapFile.getEntity(i).getAttribute("angles"));
					flagBase.setAttribute("angle", mapFile.getEntity(i).getAttribute("angle"));
					flagBase.setAttribute("goal_no", "1");
					flagBase.setAttribute("model", "models/ctf_flag_stand_mi6.mdl");
					flagBase.setAttribute("goal_max", "16 16 72");
					flagBase.setAttribute("goal_min", "-16 -16 0");
					mapFile.add(flagBase);
				}
				if(mapFile.getEntity(i).getAttribute("classname").equalsIgnoreCase("item_flag_team1")) { // Red flag
					mapFile.getEntity(0).setAttribute("defaultctf", "1"); // Turn CTF on in the worldspawn entity
					mapFile.getEntity(i).setAttribute("classname", "item_ctfflag");
					mapFile.getEntity(i).setAttribute("skin", "0"); // 0 for PHX, 1 for MI6
					mapFile.getEntity(i).setAttribute("goal_no", "2"); // 2 for PHX, 1 for MI6
					mapFile.getEntity(i).setAttribute("goal_max", "16 16 72");
					mapFile.getEntity(i).setAttribute("goal_min", "-16 -16 0");
					Entity flagBase=new Entity("item_ctfbase");
					flagBase.setAttribute("origin", mapFile.getEntity(i).getAttribute("origin"));
					flagBase.setAttribute("angles", mapFile.getEntity(i).getAttribute("angles"));
					flagBase.setAttribute("angle", mapFile.getEntity(i).getAttribute("angle"));
					flagBase.setAttribute("goal_no", "2");
					flagBase.setAttribute("model", "models/ctf_flag_stand_phoenix.mdl");
					flagBase.setAttribute("goal_max", "16 16 72");
					flagBase.setAttribute("goal_min", "-16 -16 0");
					mapFile.add(flagBase);
				}
				if(mapFile.getEntity(i).getAttribute("classname").equalsIgnoreCase("info_player_team1")) {
					mapFile.getEntity(i).setAttribute("classname", "info_ctfspawn");
					mapFile.getEntity(i).setAttribute("team_no", "2");
				}
				if(mapFile.getEntity(i).getAttribute("classname").equalsIgnoreCase("info_player_team2")) {
					mapFile.getEntity(i).setAttribute("classname", "info_ctfspawn");
					mapFile.getEntity(i).setAttribute("team_no", "1");
				}
				if(mapFile.getEntity(i).getAttribute("classname").equalsIgnoreCase("worldspawn")) {
					mapFile.getEntity(i).setAttribute("mapversion", "510"); // Otherwise Gearcraft cries.
				}
				if(mapFile.getEntity(i).getAttribute("classname").equalsIgnoreCase("info_player_start")) {
					double[] origin=mapFile.getEntity(i).getOrigin();
					mapFile.getEntity(i).setAttribute("origin", origin[X]+" "+origin[Y]+" "+(origin[Z]+18));
				}
				if(mapFile.getEntity(i).getAttribute("classname").equalsIgnoreCase("info_player_coop")) {
					double[] origin=mapFile.getEntity(i).getOrigin();
					mapFile.getEntity(i).setAttribute("origin", origin[X]+" "+origin[Y]+" "+(origin[Z]+18));
				}
				if(mapFile.getEntity(i).getAttribute("classname").equalsIgnoreCase("info_player_deathmatch")) {
					double[] origin=mapFile.getEntity(i).getOrigin();
					mapFile.getEntity(i).setAttribute("origin", origin[X]+" "+origin[Y]+" "+(origin[Z]+18));
				}
				if(mapFile.getEntity(i).getAttribute("classname").equalsIgnoreCase("light")) {
					String color=mapFile.getEntity(i).getAttribute("_color");
					String intensity=mapFile.getEntity(i).getAttribute("light");
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
					mapFile.getEntity(i).deleteAttribute("_color");
					mapFile.getEntity(i).deleteAttribute("light");
					mapFile.getEntity(i).setAttribute("_light", lightNumbers[r]+" "+lightNumbers[g]+" "+lightNumbers[b]+" "+lightNumbers[s]);
				}
			}
			if(toRadiant) {
				;
			}
			// And of course, they may both necessitate some changes
			if(mapFile.getEntity(i).getAttribute("classname").equalsIgnoreCase("func_areaportal")) {
				mapFile.getEntity(i).deleteAttribute("style");
				containsAreaPortals=true;
			}
			if(!mapFile.getEntity(i).getAttribute("angle").equals("")) {
				mapFile.getEntity(i).setAttribute("angles", "0 "+mapFile.getEntity(i).getAttribute("angle")+" 0");
				mapFile.getEntity(i).deleteAttribute("angle");
			}
			if(mapFile.getEntity(i).getAttribute("classname").equalsIgnoreCase("misc_teleporter")) {
				mapFile.getEntity(i).addBrush(createTriggerBrush(i));
				mapFile.getEntity(i).deleteAttribute("origin");
				mapFile.getEntity(i).setAttribute("classname", "trigger_teleport");
			}
			if(mapFile.getEntity(i).getAttribute("classname").equalsIgnoreCase("misc_teleporter_dest")) {
				mapFile.getEntity(i).setAttribute("classname", "info_teleport_destination");
			}
			// getModelNumber() returns 0 for worldspawn, the *# for brush based entities, and -1 for everything else
			int currentModel=mapFile.getEntity(i).getModelNumber();
			
			if(currentModel!=-1) { // If this is still -1 then it's strictly a point-based entity. Move on to the next one.
				double[] origin=mapFile.getEntity(i).getOrigin();
				int firstLeaf=BSP38.getModels().getModel(currentModel).getHead();
				v38Leaf[] leaves=BSP38.getLeavesInModel(currentModel);
				int numLeaves=leaves.length;
				boolean[] brushesUsed=new boolean[BSP38.getBrushes().length()]; // Keep a list of brushes already in the model, since sometimes the leaves lump references one brush several times
				numBrshs=0; // Reset the brush count for each entity
				for(int j=0;j<numLeaves;j++) { // For each leaf in the bunch
					v38Leaf currentLeaf=leaves[j];
					if(Window.visLeafBBoxesIsSelected()) {
						if(toGearcraft) {
							mapFile.getEntity(0).addBrush(GenericMethods.createBrush(currentLeaf.getMins(), currentLeaf.getMaxs(), "special/hint"));
						} else {
							if(toHammer) {
								mapFile.getEntity(0).addBrush(GenericMethods.createBrush(currentLeaf.getMins(), currentLeaf.getMaxs(), "tools/toolshint"));
							} else {
								if(toRadiant) {
									mapFile.getEntity(0).addBrush(GenericMethods.createBrush(currentLeaf.getMins(), currentLeaf.getMaxs(), "common/hint"));
								}
							}
						}
					}
					short firstBrushIndex=currentLeaf.getFirstMarkBrush();
					short numBrushIndices=currentLeaf.getNumMarkBrushes();
					if(numBrushIndices>0) { // A lot of leaves reference no brushes. If this is one, this iteration of the j loop is finished
						for(int k=0;k<numBrushIndices;k++) { // For each brush referenced
							if(!brushesUsed[BSP38.getMarkBrushes().getShort(firstBrushIndex+k)]) { // If the current brush has NOT been used in this entity
								Window.print("Brush "+(k+numBrushIndices),4);
								brushesUsed[BSP38.getMarkBrushes().getShort(firstBrushIndex+k)]=true;
								Brush brush=BSP38.getBrushes().getBrush(BSP38.getMarkBrushes().getShort(firstBrushIndex+k));
								if(!(brush.getAttributes()[1]==-128)) {
									decompileBrush(brush, i); // Decompile the brush
								} else {
									containsAreaPortals=true;
								}
								numBrshs++;
								numTotalItems++;
								Window.setProgress(jobnum, numTotalItems, BSP38.getBrushes().length()+BSP38.getEntities().getNumElements(), "Decompiling...");
							}
						}
					}
				}
				mapFile.getEntity(i).deleteAttribute("model");
				// Recreate origin brushes for entities that need them, only for GearCraft though.
				// These are discarded on compile and replaced with an "origin" attribute in the entity.
				// I need to undo that. For this I will create a 32x32 brush, centered at the point defined
				// by the "origin" attribute. Hammer keeps the "origin" attribute and uses it directly
				// instead, so we'll keep it in a VMF.
				if(!toHammer) {
					if(origin[0]!=0 || origin[1]!=0 || origin[2]!=0) { // If this brush uses the "origin" attribute
						MAPBrush newOriginBrush;
						if(toGearcraft) {
							newOriginBrush=GenericMethods.createBrush(new Vector3D(-Window.getOriginBrushSize(),-Window.getOriginBrushSize(),-Window.getOriginBrushSize()),new Vector3D(Window.getOriginBrushSize(),Window.getOriginBrushSize(),Window.getOriginBrushSize()),"special/origin");
						} else {
							newOriginBrush=GenericMethods.createBrush(new Vector3D(-Window.getOriginBrushSize(),-Window.getOriginBrushSize(),-Window.getOriginBrushSize()),new Vector3D(Window.getOriginBrushSize(),Window.getOriginBrushSize(),Window.getOriginBrushSize()),"common/origin");
						}
						mapFile.getEntity(i).addBrush(newOriginBrush);
					}
				}
			}
			numTotalItems++; // This entity
			Window.setProgress(jobnum, numTotalItems, BSP38.getBrushes().length()+BSP38.getEntities().getNumElements(), "Decompiling...");
		}
		if(containsAreaPortals) { // If this map was found to have area portals
			int j=0;
			for(int i=0;i<BSP38.getBrushes().length();i++) { // For each brush in this map
				if(BSP38.getBrushes().getBrush(i).getAttributes()[1]==-128) { // If the brush is an area portal brush
					for(j++;j<BSP38.getEntities().getNumElements();j++) { // Find an areaportal entity
						if(BSP38.getEntities().getEntity(j).getAttribute("classname").equalsIgnoreCase("func_areaportal")) {
							decompileBrush(BSP38.getBrushes().getBrush(i), j); // Add the brush to that entity
							break; // And break out of the inner loop, but remember your place.
						}
					}
					if(j==BSP38.getEntities().getNumElements()) { // If we're out of entities, stop this whole thing.
						break;
					}
				}
			}
		}
		Window.setProgress(jobnum, numTotalItems, BSP38.getBrushes().length()+BSP38.getEntities().getNumElements(), "Saving...");
		if(toHammer) {
			VMFWriter VMFMaker;
			if(Window.getOutputFolder().equals("default")) {
				Window.println("Saving "+BSP38.getPath().substring(0, BSP38.getPath().length()-4)+".vmf...",0);
				VMFMaker=new VMFWriter(mapFile, BSP38.getPath().substring(0, BSP38.getPath().length()-4), roundNums);
			} else {
				Window.println("Saving "+Window.getOutputFolder()+"\\"+BSP38.getMapName().substring(0, BSP38.getMapName().length()-4)+".vmf...",0);
				VMFMaker=new VMFWriter(mapFile, Window.getOutputFolder()+"\\"+BSP38.getMapName().substring(0, BSP38.getMapName().length()-4), roundNums);
			}
			VMFMaker.write();
		}
		if(toRadiant) {
			RadiantMAPWriter MAPMaker;
			if(Window.getOutputFolder().equals("default")) {
				Window.println("Saving "+BSP38.getPath().substring(0, BSP38.getPath().length()-4)+"_radiant.map...",0);
				MAPMaker=new RadiantMAPWriter(mapFile, BSP38.getPath().substring(0, BSP38.getPath().length()-4), roundNums);
			} else {
				Window.println("Saving "+Window.getOutputFolder()+"\\"+BSP38.getMapName().substring(0, BSP38.getMapName().length()-4)+"_radiant.map...",0);
				MAPMaker=new RadiantMAPWriter(mapFile, Window.getOutputFolder()+"\\"+BSP38.getMapName().substring(0, BSP38.getMapName().length()-4)+"_radiant", roundNums);
			}
			MAPMaker.write();
		}
		if(toGearcraft) {
			MAP510Writer MAPMaker;
			if(Window.getOutputFolder().equals("default")) {
				Window.println("Saving "+BSP38.getPath().substring(0, BSP38.getPath().length()-4)+".map...",0);
				MAPMaker=new MAP510Writer(mapFile, BSP38.getPath().substring(0, BSP38.getPath().length()-4), roundNums);
			} else {
				Window.println("Saving "+Window.getOutputFolder()+"\\"+BSP38.getMapName().substring(0, BSP38.getMapName().length()-4)+".map...",0);
				MAPMaker=new MAP510Writer(mapFile, Window.getOutputFolder()+"\\"+BSP38.getMapName().substring(0, BSP38.getMapName().length()-4), roundNums);
			}
			MAPMaker.write();
		}
		Window.println("Process completed!",0);
		if(correctPlaneFlip) {
			Window.println("Num simple corrected brushes: "+numSimpleCorrects,1); 
			Window.println("Num advanced corrected brushes: "+numAdvancedCorrects,1); 
			Window.println("Num good brushes: "+numGoodBrushes,1); 
		}
		Date end=new Date();
		Window.window.println("Time taken: "+(end.getTime()-begin.getTime())+"ms"+(char)0x0D+(char)0x0A,0);
	}

	// -decompileBrush38(Brush, int, boolean)
	// Decompiles the Brush and adds it to entitiy #currentEntity as .MAP data.
	private void decompileBrush(Brush brush, int currentEntity) {
		double[] origin=mapFile.getEntity(currentEntity).getOrigin();
		boolean containsWaterTexture=false;
		int firstSide=brush.getFirstSide();
		int numSides=brush.getNumSides();
		MAPBrushSide[] brushSides=new MAPBrushSide[numSides];
		MAPBrush mapBrush = new MAPBrush(numBrshs, currentEntity, false);
		Window.println(": "+numSides+" sides",4);
		for(int l=0;l<numSides;l++) { // For each side of the brush
			Vector3D[] plane=new Vector3D[3]; // Three points define a plane. All I have to do is find three points on that plane.
			v38BrushSide currentSide=BSP38.getBrushSides().getBrushSide(firstSide+l);
			Plane currentPlane=BSP38.getPlanes().getPlane(currentSide.getPlane()).getPlane(); // To find those three points, I must extrapolate from planes until I find a way to associate faces with brushes
			v38Texture currentTexture;
			boolean isDuplicate=false;
			for(int i=l+1;i<numSides;i++) { // For each subsequent side of the brush
				if(currentPlane.equals(BSP38.getPlanes().getPlane(BSP38.getBrushSides().getBrushSide(firstSide+i).getPlane()))) {
					Window.println("Duplicate planes, sides "+l+" and "+i,2);
				}
			}
			if(!isDuplicate) {
				if(currentSide.getTexInfo()>-1) {
					currentTexture=BSP38.getTextures().getTexture(currentSide.getTexInfo());
				} else {
					currentTexture=createPerpTexture38(currentPlane); // Create a texture plane perpendicular to current plane's normal
				}
				//int firstVertex=currentFace.getVert();
				//int numVertices=currentFace.getNumVerts();
				// boolean pointsWorked=false; // Need to figure out how to get faces from brush sides, then use vertices
				/*if(numVertices!=0 && vertexDecomp) { // If the face actually references a set of vertices
					plane[0]=new Vector3D(BSP42.getVertices().getVertex(firstVertex)); // Grab and store the first one
					int m=1;
					for(m=1;m<numVertices;m++) { // For each point after the first one
						plane[1]=new Vector3D(BSP42.getVertices().getVertex(firstVertex+m));
						if(!plane[0].equals(plane[1])) { // Make sure the point isn't the same as the first one
							break; // If it isn't the same, this point is good
						}
					}
					for(m=m+1;m<numVertices;m++) { // For each point after the previous one used
						plane[2]=new Vector3D(BSP42.getVertices().getVertex(firstVertex+m));
						if(!plane[2].equals(plane[0]) && !plane[2].equals(plane[1])) { // Make sure no point is equal to the third one
							if((Vector3D.crossProduct(plane[0].subtract(plane[1]), plane[0].subtract(plane[2])).getX()!=0) || // Make sure all
							   (Vector3D.crossProduct(plane[0].subtract(plane[1]), plane[0].subtract(plane[2])).getY()!=0) || // three points 
							   (Vector3D.crossProduct(plane[0].subtract(plane[1]), plane[0].subtract(plane[2])).getZ()!=0)) { // are not collinear
									pointsWorked=true;
								break;
							}
						}
					}
				}*/
				//if(numVertices==0 || !pointsWorked) { // Fallback to planar decompilation. Since there are no explicitly defined points anymore,
					                                   // we must find them ourselves using the A, B, C and D values.
					plane=GenericMethods.extrapPlanePoints(currentPlane);
				//}
				String texture=currentTexture.getTexture();
				if(texture.equalsIgnoreCase("e1u1/brwater") || 
				   texture.equalsIgnoreCase("e1u1/water1_8") || 
				   texture.equalsIgnoreCase("e1u1/water4") || 
				   texture.equalsIgnoreCase("e1u1/water8") || 
				   texture.equalsIgnoreCase("e1u3/brwater") || 
				   texture.equalsIgnoreCase("e2u3/water6") || 
				   texture.equalsIgnoreCase("e2u3/water8") || 
				   texture.equalsIgnoreCase("e3u1/brwater") || 
				   texture.equalsIgnoreCase("e3u2/water2") || 
				   texture.equalsIgnoreCase("e3u3/awater") || 
				   texture.equalsIgnoreCase("e3u3/water7") ||
				   texture.equalsIgnoreCase("e1u1/bluwter") ||
				   texture.equalsIgnoreCase("e1u2/bluwter") ||
				   texture.equalsIgnoreCase("e1u3/bluwter") ||
				   texture.equalsIgnoreCase("e2u2/bluwter") ||
				   texture.equalsIgnoreCase("e2u3/bluwter") ||
				   texture.equalsIgnoreCase("e3u1/bluwter")) {
					containsWaterTexture=true;
				} else {
					if(toHammer) { // TODO more to do here
						try {
							if(texture.substring(texture.length()-5).equalsIgnoreCase("/hint")) {
								if(currentEntity==0) {
									texture="tools/toolshint";
								} else {
									texture="tools/toolstrigger";
								}
							} else {
								if(texture.substring(texture.length()-5).equalsIgnoreCase("/skip")) {
									texture="tools/toolsskip";
								} else {
									if(texture.substring(texture.length()-5).equalsIgnoreCase("/clip")) {
										texture="tools/toolsclip";
									} else {
										if(texture.substring(texture.length()-8).equalsIgnoreCase("/trigger")) {
											texture="tools/toolstrigger";
										} else {
											if(texture.substring(texture.length()-5).equalsIgnoreCase("/sky1")) {
												texture="tools/toolsskybox";
											} else {
												if(texture.substring(texture.length()-5).equalsIgnoreCase("/sky2")) {
													texture="tools/toolsskybox";
												}
											}
										}
									}
								}
							}
						} catch(StringIndexOutOfBoundsException e) {
							;
						}
					} else {
						try {
							if(texture.substring(texture.length()-5).equalsIgnoreCase("/hint")) {
								if(currentEntity==0) {
									texture="special/hint"; // Hint was not used the same way in Quake 2 as other games.
								} else {                   // For example, a Hint brush CAN be used for a trigger in Q2 and is used as such a lot.
									texture="special/trigger";
								}
							} else {
								if(texture.substring(texture.length()-5).equalsIgnoreCase("/skip")) {
									texture="special/skip";
								} else {
									if(texture.substring(texture.length()-5).equalsIgnoreCase("/clip")) {
										texture="special/clip";
									} else {
										if(texture.substring(texture.length()-8).equalsIgnoreCase("/trigger")) {
											texture="special/trigger";
										} else {
											if(texture.substring(texture.length()-5).equalsIgnoreCase("/sky1")) {
												texture="special/sky";
											} else {
												if(texture.substring(texture.length()-5).equalsIgnoreCase("/sky2")) {
													texture="special/sky";
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
				}
				double[] textureS=new double[3];
				double[] textureT=new double[3];
				// Get the lengths of the axis vectors
				double UAxisLength=Math.sqrt(Math.pow((double)currentTexture.getU().getX(),2)+Math.pow((double)currentTexture.getU().getY(),2)+Math.pow((double)currentTexture.getU().getZ(),2));
				double VAxisLength=Math.sqrt(Math.pow((double)currentTexture.getV().getX(),2)+Math.pow((double)currentTexture.getV().getY(),2)+Math.pow((double)currentTexture.getV().getZ(),2));
				// In compiled maps, shorter vectors=longer textures and vice versa. This will convert their lengths back to 1. We'll use the actual scale values for length.
				double texScaleS=(1/UAxisLength);// Let's use these values using the lengths of the U and V axes we found above.
				double texScaleT=(1/VAxisLength);
				textureS[0]=((double)currentTexture.getU().getX()/UAxisLength);
				textureS[1]=((double)currentTexture.getU().getY()/UAxisLength);
				textureS[2]=((double)currentTexture.getU().getZ()/UAxisLength);
				double originShiftS=(((double)currentTexture.getU().getX()/UAxisLength)*origin[X]+((double)currentTexture.getU().getY()/UAxisLength)*origin[Y]+((double)currentTexture.getU().getZ()/UAxisLength)*origin[Z])/texScaleS;
				double textureShiftS=(double)currentTexture.getUShift()-originShiftS;
				textureT[0]=((double)currentTexture.getV().getX()/VAxisLength);
				textureT[1]=((double)currentTexture.getV().getY()/VAxisLength);
				textureT[2]=((double)currentTexture.getV().getZ()/VAxisLength);
				double originShiftT=(((double)currentTexture.getV().getX()/VAxisLength)*origin[X]+((double)currentTexture.getV().getY()/VAxisLength)*origin[Y]+((double)currentTexture.getV().getZ()/VAxisLength)*origin[Z])/texScaleT;
				double textureShiftT=(double)currentTexture.getVShift()-originShiftT;
				float texRot=0; // In compiled maps this is calculated into the U and V axes, so set it to 0 until I can figure out a good way to determine a better value.
				int flags=0; // Set this to 0 until we can somehow associate faces with brushes
				String material="wld_lightmap"; // Since materials are a NightFire only thing, set this to a good default
				double lgtScale=16; // These values are impossible to get from a compiled map since they
				double lgtRot=0;    // are used by RAD for generating lightmaps, then are discarded, I believe.
				// If flags weren't already 0
				/* if(Window.noFaceFlagsIsSelected()) {
					flags=0;
				}*/
				brushSides[l]=new MAPBrushSide(plane, texture, textureS, textureShiftS, textureT, textureShiftT,
				                               texRot, texScaleS, texScaleT, flags, material, lgtScale, lgtRot);
				if(brushSides[l]!=null) {
					mapBrush.add(brushSides[l]);
				}
			}
		}
		
		if(correctPlaneFlip) {
			if(mapBrush.hasBadSide()) { // If there's a side that might be backward
				if(mapBrush.hasGoodSide()) { // If there's a side that is forward
					mapBrush=GenericMethods.SimpleCorrectPlanes(mapBrush);
					numSimpleCorrects++;
					if(calcVerts) { // This is performed in advancedcorrect, so don't use it if that's happening
						mapBrush=GenericMethods.CalcBrushVertices(mapBrush);
						Window.println("Calculating vertices",4);
					}
				} else { // If no forward side exists
					try {
						mapBrush=GenericMethods.AdvancedCorrectPlanes(mapBrush);
						numAdvancedCorrects++;
					} catch(java.lang.ArithmeticException e) {
						Window.println("Plane correct returned 0 triangles for entity "+mapBrush.getEntnum()+" brush "+mapBrush.getBrushnum()+"",2);
					}
				}
			} else {
				numGoodBrushes++;
			}
		} else {
			if(calcVerts) { // This is performed in advancedcorrect, so don't use it if that's happening
				mapBrush=GenericMethods.CalcBrushVertices(mapBrush);
			}
		}
		
		// This adds the brush we've been finding and creating to
		// the current entity as an attribute. The way I've coded
		// this whole program and the entities parser, this shouldn't
		// cause any issues at all.
		if(Window.brushesToWorldIsSelected()) {
			mapFile.getEntity(0).addBrush(mapBrush);
		} else {
			if(containsWaterTexture) {
				Entity newWaterEntity=new Entity("func_water");
				newWaterEntity.setAttribute("rendercolor", "0 0 0");
				newWaterEntity.setAttribute("speed", "100");
				newWaterEntity.setAttribute("wait", "4");
				newWaterEntity.setAttribute("skin", "-3");
				newWaterEntity.setAttribute("WaveHeight", "3.2");
				newWaterEntity.addBrush(mapBrush);
				mapFile.add(newWaterEntity);
			} else {
				mapFile.getEntity(currentEntity).addBrush(mapBrush);
			}
		}
	}
	
	public MAPBrush createTriggerBrush(int ent) {
		double[] origin=mapFile.getEntity(ent).getOrigin();
		MAPBrush newTriggerBrush=new MAPBrush(numBrshs++, ent, false);
		Vector3D[][] planes=new Vector3D[6][3]; // Six planes for a cube brush, three vertices for each plane
		double[][] textureS=new double[6][3];
		double[][] textureT=new double[6][3];
		// The planes and their texture scales
		// I got these from an origin brush created by Gearcraft. Don't worry where these numbers came from, they work.
		// Top
		planes[0][0]=new Vector3D(-24+origin[0], 24+origin[1], 48+origin[2]);
		planes[0][1]=new Vector3D(24+origin[0], 24+origin[1], 48+origin[2]);
		planes[0][2]=new Vector3D(24+origin[0], -24+origin[1], 48+origin[2]);
		textureS[0][0]=1;
		textureT[0][1]=-1;
		// Bottom
		planes[1][0]=new Vector3D(-24+origin[0], -24+origin[1], -24+origin[2]);
		planes[1][1]=new Vector3D(24+origin[0], -24+origin[1], -24+origin[2]);
		planes[1][2]=new Vector3D(24+origin[0], 24+origin[1], -24+origin[2]);
		textureS[1][0]=1;
		textureT[1][1]=-1;
		// Left
		planes[2][0]=new Vector3D(-24+origin[0], 24+origin[1], 48+origin[2]);
		planes[2][1]=new Vector3D(-24+origin[0], -24+origin[1], 48+origin[2]);
		planes[2][2]=new Vector3D(-24+origin[0], -24+origin[1], -24+origin[2]);
		textureS[2][1]=1;
		textureT[2][2]=-1;
		// Right
		planes[3][0]=new Vector3D(24+origin[0], 24+origin[1], -24+origin[2]);
		planes[3][1]=new Vector3D(24+origin[0], -24+origin[1], -24+origin[2]);
		planes[3][2]=new Vector3D(24+origin[0], -24+origin[1], 48+origin[2]);
		textureS[3][1]=1;
		textureT[3][2]=-1;
		// Near
		planes[4][0]=new Vector3D(24+origin[0], 24+origin[1], 48+origin[2]);
		planes[4][1]=new Vector3D(-24+origin[0], 24+origin[1], 48+origin[2]);
		planes[4][2]=new Vector3D(-24+origin[0], 24+origin[1], -24+origin[2]);
		textureS[4][0]=1;
		textureT[4][2]=-1;
		// Far
		planes[5][0]=new Vector3D(24+origin[0], -24+origin[1], -24+origin[2]);
		planes[5][1]=new Vector3D(-24+origin[0], -24+origin[1], -24+origin[2]);
		planes[5][2]=new Vector3D(-24+origin[0], -24+origin[1], 48+origin[2]);
		textureS[5][0]=1;
		textureT[5][2]=-1;
		
		for(int j=0;j<6;j++) {
			MAPBrushSide currentEdge;
			if(toHammer) {
				currentEdge=new MAPBrushSide(planes[j], "tools/toolstrigger", textureS[j], 0, textureT[j], 0, 0, 1, 1, 0, "wld_lightmap", 16, 0);
			} else {
			// if(toGearcraft) {
				currentEdge=new MAPBrushSide(planes[j], "special/trigger", textureS[j], 0, textureT[j], 0, 0, 1, 1, 0, "wld_lightmap", 16, 0);
			}
			newTriggerBrush.add(currentEdge);
		}
		return newTriggerBrush;
	}
	
	public v38Texture createPerpTexture38(Plane in) {
		Vector3D[] points=GenericMethods.extrapPlanePoints(in);
		Vector3D U=new Vector3D(points[1].getX()-points[0].getX(), points[1].getY()-points[0].getY(), points[1].getZ()-points[0].getZ());
		Vector3D V=new Vector3D(points[1].getX()-points[2].getX(), points[1].getY()-points[2].getY(), points[1].getZ()-points[2].getZ());
		U.normalize();
		V.normalize();
		v38Texture currentTexture= new v38Texture(U, 0, V, 0, 0, 0, "special/clip", 0);
		return currentTexture;
	} 
}
