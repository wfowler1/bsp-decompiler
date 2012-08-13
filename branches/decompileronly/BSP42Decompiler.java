// BSP42Decompiler
// Decompiles a Nightfire BSP

import java.util.Date;

public class BSP42Decompiler {

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
	
	private v42BSP BSP42;
	
	// CONSTRUCTORS
	
	// This constructor sets everything according to specified settings.
	public BSP42Decompiler(v42BSP BSP42, boolean vertexDecomp, boolean correctPlaneFlip, boolean calcVerts, boolean roundNums, boolean toHammer, boolean toRadiant, boolean toGearcraft, int jobnum) {
		// Set up global variables
		this.BSP42=BSP42;
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
	
	// -decompile()
	// Attempts to convert the Nightfire BSP file back into a .MAP file.
	//
	// This is another one of the most complex things I've ever had to code. I've
	// never nested for loops four deep before.
	// Iterators:
	// i: Current entity in the list
	//  j: Current leaf, referenced in a list by the model referenced by the current entity
	//   k: Current brush, referenced in a list by the current leaf.
	//    l: Current side of the current brush.
	//     m: When attempting vertex decompilation, the current vertex.
	public void decompile() throws java.io.IOException {
		Date begin=new Date();
		// Begin by copying all the entities into another Lump00 object. This is
		// necessary because if I just modified the current entity list then it
		// could be saved back into the BSP and really mess some stuff up.
		mapFile=new Entities(BSP42.getEntities());
		int numTotalItems=0;
		// Next make a list of all detail brushes.
		boolean[] detailBrush=new boolean[BSP42.getBrushes().getNumElements()];
		for(int i=0;i<BSP42.getBrushes().getNumElements();i++) {	// For every brush
			if(BSP42.getBrushes().getBrush(i).getAttributes()[1]==0x02) { // This attribute seems to indicate no affect on vis
				detailBrush[i]=true; // Flag the brush as detail
			}
		}
		// Then I need to go through each entity and see if it's brush-based.
		// Worldspawn is brush-based as well as any entity with model *#.
		for(int i=0;i<BSP42.getEntities().getNumElements();i++) { // For each entity
			Window.println("Entity "+i+": "+mapFile.getEntity(i).getAttribute("classname"),4);
			if(toHammer) { // correct some entities to make source ports easier, TODO add more
				if(mapFile.getEntity(i).getAttribute("classname").equalsIgnoreCase("light_spot")) {
					mapFile.getEntity(i).setAttribute("pitch", new Double(mapFile.getEntity(i).getAngles()[0]).toString());
					mapFile.getEntity(i).renameAttribute("_cone", "_inner_cone"); 
					mapFile.getEntity(i).renameAttribute("_cone2", "_cone");
					try {
						if(Double.parseDouble(mapFile.getEntity(i).getAttribute("_cone"))>90.0) {
							mapFile.getEntity(i).setAttribute("_cone", "90");
						} else {
							if(Double.parseDouble(mapFile.getEntity(i).getAttribute("_cone"))<0.0) {
								mapFile.getEntity(i).setAttribute("_cone", "0");
							}
						}
						if(Double.parseDouble(mapFile.getEntity(i).getAttribute("_cone2"))>90.0) {
							mapFile.getEntity(i).setAttribute("_cone2", "90");
						} else {
							if(Double.parseDouble(mapFile.getEntity(i).getAttribute("_cone2"))<0.0) {
								mapFile.getEntity(i).setAttribute("_cone2", "0");
							}
						}
					} catch(java.lang.NumberFormatException e) {
						;
					}
				}
				if(mapFile.getEntity(i).getAttribute("classname").equalsIgnoreCase("func_wall")) {
					if(mapFile.getEntity(i).getAttribute("rendermode").equals("0")) {
						mapFile.getEntity(i).setAttribute("classname", "func_detail");
						mapFile.getEntity(i).deleteAttribute("rendermode");
					} else {
						mapFile.getEntity(i).setAttribute("classname", "func_brush");
						mapFile.getEntity(i).setAttribute("Solidity", "2");
					}
				}
				if(mapFile.getEntity(i).getAttribute("classname").equalsIgnoreCase("func_wall_toggle")) {
					mapFile.getEntity(i).setAttribute("classname", "func_brush");
					mapFile.getEntity(i).setAttribute("Solidity", "0");
					try {
						if((Double.parseDouble(mapFile.getEntity(i).getAttribute("spawnflags")))/2.0 == Double.parseDouble(mapFile.getEntity(i).getAttribute("spawnflags"))) {
							mapFile.getEntity(i).setAttribute("StartDisabled", "1"); // If spawnflags is an odd number, the start disabled flag is set.
						} else {
							mapFile.getEntity(i).setAttribute("StartDisabled", "0");
						}
					} catch(java.lang.NumberFormatException e) {
						mapFile.getEntity(i).setAttribute("StartDisabled", "0");
					}
				}
				if(mapFile.getEntity(i).getAttribute("classname").equalsIgnoreCase("func_illusionary")) {
					mapFile.getEntity(i).setAttribute("classname", "func_brush");
					mapFile.getEntity(i).setAttribute("Solidity", "1");
				}
				if(mapFile.getEntity(i).getAttribute("classname").equalsIgnoreCase("item_generic")) {
					mapFile.getEntity(i).setAttribute("classname", "prop_dynamic");
					mapFile.getEntity(i).setAttribute("solid", "0");
					mapFile.getEntity(i).deleteAttribute("effects");
					mapFile.getEntity(i).deleteAttribute("fixedlight");
				}
				if(mapFile.getEntity(i).getAttribute("classname").equalsIgnoreCase("env_glow")) {
					mapFile.getEntity(i).setAttribute("classname", "env_sprite");
				}
				if(!mapFile.getEntity(i).getAttribute("body").equalsIgnoreCase("")) {
					mapFile.getEntity(i).renameAttribute("body", "SetBodyGroup");
				}
				if(mapFile.getEntity(i).getAttribute("rendercolor").equals("0 0 0")) {
					mapFile.getEntity(i).setAttribute("rendercolor", "255 255 255");
				}
				try {
					if(mapFile.getEntity(i).getAttribute("model").substring(mapFile.getEntity(i).getAttribute("model").length()-4).equalsIgnoreCase(".spz")) {
						mapFile.getEntity(i).setAttribute("model", mapFile.getEntity(i).getAttribute("model").substring(0, mapFile.getEntity(i).getAttribute("model").length()-4)+".spr");
					}
				} catch(java.lang.StringIndexOutOfBoundsException e) {
					;
				}
			}
			if(toRadiant) { // figure out some good stuff to do here :P
				;
			}
			if(toGearcraft) { // Gearcraft needs a couple things, too. These things usually make it into the compiled map, but just in case.
				if(mapFile.getEntity(i).getAttribute("classname").equalsIgnoreCase("worldspawn")) {
					mapFile.getEntity(i).setAttribute("mapversion", "510"); // Otherwise Gearcraft cries.
				}
			}
			numBrshs=0; // Reset the brush count for each entity
			// getModelNumber() returns 0 for worldspawn, the *# for brush based entities, and -1 for everything else
			int currentModel=mapFile.getEntity(i).getModelNumber();
			
			if(currentModel!=-1) { // If this is still -1 then it's strictly a point-based entity. Move on to the next one.
				double[] origin=mapFile.getEntity(i).getOrigin();
				int firstLeaf=BSP42.getModels().getModel(currentModel).getLeaf();
				int numLeaves=BSP42.getModels().getModel(currentModel).getNumLeafs();
				boolean[] brushesUsed=new boolean[BSP42.getBrushes().getNumElements()]; // Keep a list of brushes already in the model, since sometimes the leaves lump references one brush several times
				numBrshs=0;
				for(int j=0;j<numLeaves;j++) { // For each leaf in the bunch
					v42Leaf currentLeaf=BSP42.getLeaves().getLeaf(j+firstLeaf);
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
					int firstBrushIndex=currentLeaf.getMarkBrush();
					int numBrushIndices=currentLeaf.getNumMarkBrushes();
					if(numBrushIndices>0) { // A lot of leaves reference no brushes. If this is one, this iteration of the j loop is finished
						for(int k=0;k<numBrushIndices;k++) { // For each brush referenced
							if(!brushesUsed[BSP42.getMarkBrushes().getInt(firstBrushIndex+k)]) { // If the current brush has NOT been used in this entity
								Window.print("Brush "+(k+firstBrushIndex),4);
								brushesUsed[BSP42.getMarkBrushes().getInt(firstBrushIndex+k)]=true;
								if(detailBrush[BSP42.getMarkBrushes().getInt(firstBrushIndex+k)] && currentModel==0 && !Window.noDetailIsSelected()) {
									decompileBrush(BSP42.getBrushes().getBrush(BSP42.getMarkBrushes().getInt(firstBrushIndex+k)), i, true); // Decompile the brush, as not detail
								} else {
									decompileBrush(BSP42.getBrushes().getBrush(BSP42.getMarkBrushes().getInt(firstBrushIndex+k)), i, false); // Decompile the brush, as detail
								}
								numBrshs++;
								numTotalItems++;
								Window.setProgress(jobnum, numTotalItems, BSP42.getBrushes().getNumElements()+BSP42.getEntities().getNumElements(), "Decompiling...");
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
					if(origin[0]!=0 || origin[1]!=0 || origin[2]!=0) { // If this entity uses the "origin" attribute
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
			numTotalItems++;
			Window.setProgress(jobnum, numTotalItems, BSP42.getBrushes().getNumElements()+BSP42.getEntities().getNumElements(), "Decompiling...");
		}
		Window.setProgress(jobnum, numTotalItems, BSP42.getBrushes().getNumElements()+BSP42.getEntities().getNumElements(), "Saving...");
		if(toHammer) {
			Window.println("Saving "+BSP42.getPath().substring(0, BSP42.getPath().length()-4)+".vmf...",0);
			VMFWriter VMFMaker=new VMFWriter(mapFile, BSP42.getPath().substring(0, BSP42.getPath().length()-4), roundNums);
			VMFMaker.write();
		}
		if(toRadiant) {
			Window.println("Saving "+BSP42.getPath().substring(0, BSP42.getPath().length()-4)+"_radiant.map...",0);
			RadiantMAPWriter MAPMaker=new RadiantMAPWriter(mapFile, BSP42.getPath().substring(0, BSP42.getPath().length()-4)+"_radiant", roundNums);
			MAPMaker.write();
		}
		if(toGearcraft) {
			Window.println("Saving "+BSP42.getPath().substring(0, BSP42.getPath().length()-4)+".map...",0);
			MAP510Writer MAPMaker=new MAP510Writer(mapFile, BSP42.getPath().substring(0, BSP42.getPath().length()-4), roundNums);
			MAPMaker.write();
		}
		Window.println("Process completed!",0);
		if(correctPlaneFlip) {
			Window.println("Num simple corrected brushes: "+numSimpleCorrects,1); 
			Window.println("Num advanced corrected brushes: "+numAdvancedCorrects,1); 
			Window.println("Num good brushes: "+numGoodBrushes,1); 
		}
		Date end=new Date();
		Window.println("Time taken: "+(end.getTime()-begin.getTime())+"ms"+(char)0x0D+(char)0x0A,0);
	}
	
	// -decompileBrush(Brush, int, boolean)
	// Decompiles the Brush and adds it to entitiy #currentEntity as .MAP data.
	private void decompileBrush(v42Brush brush, int currentEntity, boolean isDetailBrush) {
		double[] origin=mapFile.getEntity(currentEntity).getOrigin();
		int firstSide=brush.getFirstSide();
		int numSides=brush.getNumSides();
		MAPBrushSide[] brushSides=new MAPBrushSide[0];
		MAPBrush mapBrush = new MAPBrush(numBrshs, currentEntity, isDetailBrush);
		int numRealFaces=0;
		boolean containsNonClipSide=false;
		Plane[] brushPlanes=new Plane[0];
		Window.println(": "+numSides+" sides",4);
		for(int l=0;l<numSides;l++) { // For each side of the brush
			v42BrushSide currentSide=BSP42.getBrushSides().getBrushSide(firstSide+l);
			v42Face currentFace=BSP42.getFaces().getFace(currentSide.getFace()); // To find those three points, I can use vertices referenced by faces.
			String texture=BSP42.getTextures().getString(currentFace.getTexture());
			if(currentFace.getType()!=800) { // These surfaceflags (512 + 256 + 32) are set only by the compiler, on faces that need to be thrown out.
				if(!texture.equalsIgnoreCase("special/clip") && !texture.equalsIgnoreCase("special/playerclip") && !texture.equalsIgnoreCase("special/enemyclip")) {
					containsNonClipSide=true;
					if(Window.replaceWithNullIsSelected() && (currentFace.getType()==512 || currentFace.getType()==544) && !texture.equalsIgnoreCase("special/trigger")) {
						if(toHammer) {
							texture="special/nodraw";
						} else {
							texture="special/null";
						}
						currentFace.setType(0);
					}
				}

				int firstVertex=currentFace.getVert();
				int numVertices=currentFace.getNumVerts();
				Plane currentPlane;
				try { // I've only ever come across this error once or twice, but something causes it very rarely
					currentPlane=BSP42.getPlanes().getPlane(currentSide.getPlane()).getPlane();
				} catch(java.lang.ArrayIndexOutOfBoundsException e) {
					try { // So try to get the plane index from somewhere else
						currentPlane=BSP42.getPlanes().getPlane(currentFace.getPlane()).getPlane();
					}  catch(java.lang.ArrayIndexOutOfBoundsException f) { // If that fails, BS something
						Window.println("WARNING: BSP has error, references nonexistant plane "+currentSide.getPlane()+", bad side "+(l)+" of brush "+numBrshs+" Entity "+currentEntity,2);
						currentPlane=new Plane((double)1, (double)0, (double)0, (double)0);
					}
				}
				Vector3D[] triangle=new Vector3D[0];
				boolean pointsWorked=false;
				if(numVertices!=0 && vertexDecomp) { // If the face actually references a set of vertices
					triangle=new Vector3D[3]; // Three points define a plane. All I have to do is find three points on that plane.
					triangle[0]=new Vector3D(BSP42.getVertices().getVertex(firstVertex)); // Grab and store the first one
					int m=1;
					for(m=1;m<numVertices;m++) { // For each point after the first one
						triangle[1]=new Vector3D(BSP42.getVertices().getVertex(firstVertex+m));
						if(!triangle[0].equals(triangle[1])) { // Make sure the point isn't the same as the first one
							break; // If it isn't the same, this point is good
						}
					}
					for(m=m+1;m<numVertices;m++) { // For each point after the previous one used
						triangle[2]=new Vector3D(BSP42.getVertices().getVertex(firstVertex+m));
						if(!triangle[2].equals(triangle[0]) && !triangle[2].equals(triangle[1])) { // Make sure no point is equal to the third one
							// Make sure all three points are non collinear
							Vector3D cr=Vector3D.crossProduct(triangle[0].subtract(triangle[1]), triangle[0].subtract(triangle[2]));
							if(cr.length() > Window.getPrecision()) { // vector length is never negative.
								pointsWorked=true;
								break;
							}
						}
					}
				}
				// Correct texture names for Source engine
				if(toHammer) {
					if(texture.equalsIgnoreCase("special/nodraw")) {
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
				}
				double[] textureS=new double[3];
				double[] textureT=new double[3];
				v42TexMatrix currentTexMatrix=BSP42.getTextureMatrices().getTexMatrix(currentFace.getTexStyle());
				// Get the lengths of the axis vectors
				double UAxisLength=Math.sqrt(Math.pow((double)currentTexMatrix.getUAxisX(),2)+Math.pow((double)currentTexMatrix.getUAxisY(),2)+Math.pow((double)currentTexMatrix.getUAxisZ(),2));
				double VAxisLength=Math.sqrt(Math.pow((double)currentTexMatrix.getVAxisX(),2)+Math.pow((double)currentTexMatrix.getVAxisY(),2)+Math.pow((double)currentTexMatrix.getVAxisZ(),2));
				// In compiled maps, shorter vectors=longer textures and vice versa. This will convert their lengths back to 1. We'll use the actual scale values for length.
				double texScaleS=(1/UAxisLength);// Let's use these values using the lengths of the U and V axes we found above.
				double texScaleT=(1/VAxisLength);
				textureS[0]=((double)currentTexMatrix.getUAxisX()/UAxisLength);
				textureS[1]=((double)currentTexMatrix.getUAxisY()/UAxisLength);
				textureS[2]=((double)currentTexMatrix.getUAxisZ()/UAxisLength);
				double originShiftS=(((double)currentTexMatrix.getUAxisX()/UAxisLength)*origin[X]+((double)currentTexMatrix.getUAxisY()/UAxisLength)*origin[Y]+((double)currentTexMatrix.getUAxisZ()/UAxisLength)*origin[Z])/texScaleS;
				double textureShiftS=(double)currentTexMatrix.getUShift()-originShiftS;
				textureT[0]=((double)currentTexMatrix.getVAxisX()/VAxisLength);
				textureT[1]=((double)currentTexMatrix.getVAxisY()/VAxisLength);
				textureT[2]=((double)currentTexMatrix.getVAxisZ()/VAxisLength);
				double originShiftT=(((double)currentTexMatrix.getVAxisX()/VAxisLength)*origin[X]+((double)currentTexMatrix.getVAxisY()/VAxisLength)*origin[Y]+((double)currentTexMatrix.getVAxisZ()/VAxisLength)*origin[Z])/texScaleT;
				double textureShiftT=(double)currentTexMatrix.getVShift()-originShiftT;
				float texRot=0; // In compiled maps this is calculated into the U and V axes, so set it to 0 until I can figure out a good way to determine a better value.
				int flags=currentFace.getType(); // This is actually a set of flags. Whatever.
				String material;
				try {
					material=BSP42.getMaterials().getString(currentFace.getMaterial());
				} catch(java.lang.ArrayIndexOutOfBoundsException e) { // In case the BSP has some strange error making it reference nonexistant materials
					Window.println("WARNING: Map referenced nonexistant material #"+currentFace.getMaterial()+", using wld_lightmap instead!",2);
					material="wld_lightmap";
				}
				double lgtScale=16; // These values are impossible to get from a compiled map since they
				double lgtRot=0;    // are used by RAD for generating lightmaps, then are discarded, I believe.
				MAPBrushSide[] newList=new MAPBrushSide[brushSides.length+1];
				for(int i=0;i<brushSides.length;i++) {
					newList[i]=brushSides[i];
				}
				if(Window.noFaceFlagsIsSelected()) {
					flags=0;
				}
				if(pointsWorked) {
					newList[brushSides.length]=new MAPBrushSide(currentPlane, triangle, texture, textureS, textureShiftS, textureT, textureShiftT,
					                                            texRot, texScaleS, texScaleT, flags, material, lgtScale, lgtRot);
				} else {
					newList[brushSides.length]=new MAPBrushSide(currentPlane, texture, textureS, textureShiftS, textureT, textureShiftT,
					                                            texRot, texScaleS, texScaleT, flags, material, lgtScale, lgtRot);
				}
				brushSides=newList;
				numRealFaces++;
			}
		}
		
		for(int i=0;i<brushSides.length;i++) {
			mapBrush.add(brushSides[i]);
		}
		
		brushPlanes=new Plane[mapBrush.getNumSides()];
		for(int i=0;i<brushPlanes.length;i++) {
			brushPlanes[i]=mapBrush.getSide(i).getPlane();
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
						Window.println("WARNING: Plane correct returned 0 triangles for entity "+mapBrush.getEntnum()+" brush "+mapBrush.getBrushnum()+"",2);
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
			if(toHammer && isDetailBrush && containsNonClipSide) {
				Entity newDetailEntity=new Entity("func_detail");
				newDetailEntity.addBrush(mapBrush);
				mapFile.add(newDetailEntity);
			} else {
				mapFile.getEntity(currentEntity).addBrush(mapBrush);
			}
		}
	}
}
