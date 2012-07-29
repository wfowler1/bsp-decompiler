// Decompiler class

// Handles the actual decompilation.

import java.util.Date;
import java.util.Scanner;

public class Decompiler {

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
	private boolean toVMF;
	private boolean calcVerts;
	private boolean roundNums;
	
	private int numFlips=0;
	private int numFlipBrshs=0;
	private int jobnum;
	
	private Entities mapFile; // Most MAP file formats (including GearCraft) are simply a bunch of nested entities
	private int numBrshs;
	
	// Declare all kinds of BSPs here, the one actually used will be determined by constructor
	private DoomMap doomMap;
	// private BSPv29n30
	private v38BSP BSP38;
	private v42BSP BSP42;
	private v46BSP BSP46;
	// private BSPv47
	// private MOHAABSP
	// private SourceBSPv20
	
	private int version; // The constructor will set this properly
	
	// CONSTRUCTORS
	
	// This constructor set up everything to convert a Doom map into brushes compatible with modern map editors.
	// I don't know if this is decompiling, per se. I don't know if Doom maps were ever compiled or if they just had nodes built.
	public Decompiler(DoomMap doomMap, boolean roundNums, boolean toVMF, int jobnum) {
		this.doomMap=doomMap;
		version=1; // For lack of a better version number
		this.roundNums=roundNums;
		this.toVMF=toVMF;
		this.jobnum=jobnum;
	}
	
	// This constructor sets everything according to specified settings.
	public Decompiler(v38BSP BSP38, boolean vertexDecomp, boolean correctPlaneFlip, boolean calcVerts, boolean roundNums, boolean toVMF, int jobnum) {
		// Set up global variables
		this.BSP38=BSP38;
		version=38;
		this.vertexDecomp=vertexDecomp;
		this.correctPlaneFlip=correctPlaneFlip;
		this.toVMF=toVMF;
		this.calcVerts=calcVerts;
		this.roundNums=roundNums;
		this.jobnum=jobnum;
	}
	
	// This constructor sets everything according to specified settings.
	public Decompiler(v42BSP BSP42, boolean vertexDecomp, boolean correctPlaneFlip, boolean calcVerts, boolean roundNums, boolean toVMF, int jobnum) {
		// Set up global variables
		this.BSP42=BSP42;
		version=42;
		this.vertexDecomp=vertexDecomp;
		this.correctPlaneFlip=correctPlaneFlip;
		this.toVMF=toVMF;
		this.calcVerts=calcVerts;
		this.roundNums=roundNums;
		this.jobnum=jobnum;
	}
	
	// This constructor sets everything according to specified settings.
	public Decompiler(v46BSP BSP46, boolean vertexDecomp, boolean correctPlaneFlip, boolean calcVerts, boolean roundNums, boolean toVMF, int jobnum) {
		// Set up global variables
		this.BSP46=BSP46;
		version=46;
		this.vertexDecomp=vertexDecomp;
		this.correctPlaneFlip=correctPlaneFlip;
		this.toVMF=toVMF;
		this.calcVerts=calcVerts;
		this.roundNums=roundNums;
		this.jobnum=jobnum;
	}
	
	// METHODS
	
	// +decompile()
	// Starts the decompilation process. This is leftover from when multithreading
	// was handled through the decompiler.
	public void decompile() throws java.io.IOException {
		Date begin=new Date();
		switch(version) {
			case 1:
				decompileDoomMap();
				break;
			case 38:
				if(vertexDecomp) {
					Window.window.println("Quake 2 Decompilation using vertices not possible (yet). Using planes instead!");
				}
				decompileBSP38();
				break;
			case 42:
				decompileBSP42();
				break;
			case 46:
				Window.window.println("Decompilation of Quake 3 map is a work in progress! Expect inaccuracies!");
				decompileBSP46();
				break;
		}
		Date end=new Date();
		Window.window.println("Time taken: "+(end.getTime()-begin.getTime())+"ms\n");
	}
	
	// -decompileBSP42()
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
	public void decompileBSP42() throws java.io.IOException {
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
			if(toVMF) { // correct some entities to make source ports easier, TODO add more
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
			} else { // Gearcraft needs a couple things, too. These things usually make it into the compiled map, but just in case.
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
					int firstBrushIndex=currentLeaf.getMarkBrush();
					int numBrushIndices=currentLeaf.getNumMarkBrushes();
					if(numBrushIndices>0) { // A lot of leaves reference no brushes. If this is one, this iteration of the j loop is finished
						for(int k=0;k<numBrushIndices;k++) { // For each brush referenced
							if(!brushesUsed[BSP42.getMarkBrushes().getInt(firstBrushIndex+k)]) { // If the current brush has NOT been used in this entity
								brushesUsed[BSP42.getMarkBrushes().getInt(firstBrushIndex+k)]=true;
								if(detailBrush[BSP42.getMarkBrushes().getInt(firstBrushIndex+k)] && currentModel==0) {
									decompileBrush42(BSP42.getBrushes().getBrush(BSP42.getMarkBrushes().getInt(firstBrushIndex+k)), i, true); // Decompile the brush, as not detail
								} else {
									decompileBrush42(BSP42.getBrushes().getBrush(BSP42.getMarkBrushes().getInt(firstBrushIndex+k)), i, false); // Decompile the brush, as detail
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
				if(!toVMF) {
					if(origin[0]!=0 || origin[1]!=0 || origin[2]!=0) { // If this brush uses the "origin" attribute
						addOriginBrush(i);
					}
					//mapFile.getEntity(i).deleteAttribute("origin");
				}
			}
			numTotalItems++;
			Window.setProgress(jobnum, numTotalItems, BSP42.getBrushes().getNumElements()+BSP42.getEntities().getNumElements(), "Decompiling...");
		}
		Window.setProgress(jobnum, numTotalItems, BSP42.getBrushes().getNumElements()+BSP42.getEntities().getNumElements(), "Saving...");
		if(toVMF) {
			Window.window.println("Saving "+BSP42.getPath().substring(0, BSP42.getPath().length()-4)+".vmf...");
			VMFWriter VMFMaker=new VMFWriter(mapFile, BSP42.getPath().substring(0, BSP42.getPath().length()-4), roundNums);
			VMFMaker.write();
		} else {
			Window.window.println("Saving "+BSP42.getPath().substring(0, BSP42.getPath().length()-4)+".map...");
			MAP510Writer MAPMaker=new MAP510Writer(mapFile, BSP42.getPath().substring(0, BSP42.getPath().length()-4), roundNums);
			MAPMaker.write();
		}
		Window.window.println("Process completed!");
	}
	
	// -decompileBrush42(Brush, int, boolean)
	// Decompiles the Brush and adds it to entitiy #currentEntity as .MAP data.
	private void decompileBrush42(v42Brush brush, int currentEntity, boolean isDetailBrush) {
		double[] origin=mapFile.getEntity(currentEntity).getOrigin();
		int firstSide=brush.getFirstSide();
		int numSides=brush.getNumSides();
		MAPBrushSide[] brushSides=new MAPBrushSide[0];
		MAPBrush mapBrush = new MAPBrush(numBrshs, currentEntity, isDetailBrush);
		int numRealFaces=0;
		boolean containsNonClipSide=false;
		Plane[] brushPlanes=new Plane[0];
		for(int l=0;l<numSides;l++) { // For each side of the brush
			v42BrushSide currentSide=BSP42.getBrushSides().getBrushSide(firstSide+l);
			v42Face currentFace=BSP42.getFaces().getFace(currentSide.getFace()); // To find those three points, I can use vertices referenced by faces.
			String texture=BSP42.getTextures().getString(currentFace.getTexture());
			if(currentFace.getType()!=800) { // These surfaceflags (512 + 256 + 32) are set only by the compiler, on faces that need to be thrown out.
				if(!texture.equalsIgnoreCase("special/clip") && !texture.equalsIgnoreCase("special/playerclip") && !texture.equalsIgnoreCase("special/enemyclip")) {
					containsNonClipSide=true;
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
						Window.window.println("WARNING: BSP has error, references nonexistant plane "+currentSide.getPlane()+", bad side "+(l)+" of brush "+numBrshs+" Entity "+currentEntity);
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
							if((Vector3D.crossProduct(triangle[0].subtract(triangle[1]), triangle[0].subtract(triangle[2])).getX()!=0) || // Make sure all
							   (Vector3D.crossProduct(triangle[0].subtract(triangle[1]), triangle[0].subtract(triangle[2])).getY()!=0) || // three points 
							   (Vector3D.crossProduct(triangle[0].subtract(triangle[1]), triangle[0].subtract(triangle[2])).getZ()!=0)) { // are not collinear
								pointsWorked=true;
								break;
							}
						}
					}
				}
				// Correct texture names for Source engine
				if(toVMF) {
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
					Window.window.println("WARNING: Map referenced nonexistant material #"+currentFace.getMaterial()+", using wld_lightmap instead!");
					material="wld_lightmap";
				}
				double lgtScale=16; // These values are impossible to get from a compiled map since they
				double lgtRot=0;    // are used by RAD for generating lightmaps, then are discarded, I believe.
				MAPBrushSide[] newList=new MAPBrushSide[brushSides.length+1];
				for(int i=0;i<brushSides.length;i++) {
					newList[i]=brushSides[i];
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
		/*
		// TODO: Figure out why simplecorrect bombs
		if(correctPlaneFlip) {
			if(mapBrush.hasBadSide()) {
				try {
					mapBrush=GenericMethods.AdvancedCorrectPlanes(mapBrush); // This is good.
				} catch(java.lang.ArithmeticException e) {
					Window.window.println("Plane correct returned 0 triangles for entity "+mapBrush.getEntnum()+" brush "+mapBrush.getBrushnum()+"");
					if(calcVerts) {
						try {
							mapBrush=GenericMethods.CalcBrushVertices(mapBrush);
						} catch(java.lang.ArithmeticException f) {
							Window.window.println("Vertex calculation returned 0 triangles for entity "+mapBrush.getEntnum()+" brush "+mapBrush.getBrushnum()+"");
							for(int j=0;j<mapBrush.getNumSides();j++) {
								if(!mapBrush.getSide(j).isDefinedByTriangle()) {
									mapBrush.getSide(j).setSide(mapBrush.getSide(j).getPlane(), GenericMethods.extrapPlanePoints(mapBrush.getSide(j).getPlane()));
								}
							}
						}
					}
				}
			}
		} else {
			if(calcVerts) { // This is performed in advancedcorrect, so don't use it if that's happening
				try {
					mapBrush=GenericMethods.CalcBrushVertices(mapBrush);
				} catch(java.lang.ArithmeticException e) {
					Window.window.println("Vertex calculation returned 0 triangles for entity "+mapBrush.getEntnum()+" brush "+mapBrush.getBrushnum()+"");
				}
			}
		}
		*/
		
		if(correctPlaneFlip) {
			if(mapBrush.hasBadSide()) { // If there's a side that might be backward
				if(mapBrush.hasGoodSide()) { // If there's a side that is forward
					mapBrush=GenericMethods.SimpleCorrectPlanes(mapBrush);
					if(calcVerts) { // This is performed in advancedcorrect, so don't use it if that's happening
						mapBrush=GenericMethods.CalcBrushVertices(mapBrush);
					}
				} else { // If no forward side exists
					try {
						mapBrush=GenericMethods.AdvancedCorrectPlanes(mapBrush);
					} catch(java.lang.ArithmeticException e) {
						Window.window.println("Plane correct returned 0 triangles for entity "+mapBrush.getEntnum()+" brush "+mapBrush.getBrushnum()+"");
					}
				}
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
		if(toVMF && isDetailBrush && containsNonClipSide) {
			Entity newDetailEntity=new Entity("func_detail");
			newDetailEntity.addBrush(mapBrush);
			mapFile.add(newDetailEntity);
		} else {
			mapFile.getEntity(currentEntity).addBrush(mapBrush);
		}
	}
	
	// Attempt to turn the Quake 3 BSP into a .MAP file
	public void decompileBSP46() throws java.io.IOException {
		// Begin by copying all the entities into another Entities object.
		mapFile=new Entities(BSP46.getEntities());
		int numTotalItems=0;
		boolean[] detailBrush=new boolean[BSP46.getBrushes().getNumElements()];
		// Then make a list of detail brushes (does Quake 3 use details, and how?)
		/*for(int j=0;j<BSP46.getBrushes().getNumElements();j++) {	// For every brush
			// TODO: Figure out how to find detail brushes from a Quake 3 map, if possible
			if(BSP46.getBrushes().getBrush(j).getAttributes()[1]==0x02) { // This attribute seems to indicate no affect on vis
				detailBrush[j]=true; // Flag the brush as detail
			}
		}*/
		// Then I need to go through each entity and see if it's brush-based.
		// Worldspawn is brush-based as well as any entity with model *#.
		for(int i=0;i<BSP46.getEntities().getNumElements();i++) { // For each entity
			if(toVMF) {
				;
			} else {
				;
			}
			numBrshs=0; // Reset the brush count for each entity
			// getModelNumber() returns 0 for worldspawn, the *# for brush based entities, and -1 for everything else
			int currentModel=mapFile.getEntity(i).getModelNumber();
			
			if(currentModel!=-1) { // If this is still -1 then it's strictly a point-based entity. Move on to the next one.
				double[] origin=mapFile.getEntity(i).getOrigin();
				int firstBrush=BSP46.getModels().getModel(currentModel).getBrush();
				int numBrushes=BSP46.getModels().getModel(currentModel).getNumBrushes();
				numBrshs=0;
				for(int j=0;j<numBrushes;j++) { // For each brush referenced
					if(detailBrush[firstBrush+j] && currentModel==0) {
						decompileBrush46(BSP46.getBrushes().getBrush(firstBrush+j), i, true); // Decompile the brush, as not detail
					} else {
						decompileBrush46(BSP46.getBrushes().getBrush(firstBrush+j), i, false); // Decompile the brush, as detail
					}
					numBrshs++;
					numTotalItems++;
					Window.setProgress(jobnum, numTotalItems, BSP46.getBrushes().getNumElements()+BSP46.getEntities().getNumElements(), "Decompiling...");
				}
				mapFile.getEntity(i).deleteAttribute("model");
				if(!toVMF) {
					// Recreate origin brushes for entities that need them
					// These are discarded on compile and replaced with an "origin" attribute in the entity.
					// I need to undo that. For this I will create a 32x32 brush, centered at the point defined
					// by the "origin" attribute.
					if(origin[0]!=0 || origin[1]!=0 || origin[2]!=0) { // If this brush uses the "origin" attribute
						addOriginBrush(i);
					}
				}
			}
			numTotalItems++;
			Window.setProgress(jobnum, numTotalItems, BSP46.getBrushes().getNumElements()+BSP46.getEntities().getNumElements(), "Decompiling...");
		} 
		Window.setProgress(jobnum, numTotalItems, BSP46.getBrushes().getNumElements()+BSP46.getEntities().getNumElements(), "Saving..."); 
		if(toVMF) {
			Window.window.println("Saving "+BSP46.getPath().substring(0, BSP46.getPath().length()-4)+".vmf...");
			VMFWriter VMFMaker=new VMFWriter(mapFile, BSP46.getPath().substring(0, BSP46.getPath().length()-4), roundNums);
			VMFMaker.write();
		} else {
			Window.window.println("Saving "+BSP46.getPath().substring(0, BSP46.getPath().length()-4)+".map...");
			MAP510Writer MAPMaker=new MAP510Writer(mapFile, BSP46.getPath().substring(0, BSP46.getPath().length()-4), roundNums);
			MAPMaker.write();
		}
		Window.window.println("Process completed!");
	}
	
	// -decompileBrush46(Brush, int, boolean)
	// Decompiles the Brush and adds it to entitiy #currentEntity as .MAP data.
	private void decompileBrush46(v46Brush brush, int currentEntity, boolean isDetailBrush) {
		double[] origin=mapFile.getEntity(currentEntity).getOrigin();
		int firstSide=brush.getFirstSide();
		int numSides=brush.getNumSides();
		MAPBrushSide[] brushSides=new MAPBrushSide[numSides];
		MAPBrush mapBrush = new MAPBrush(numBrshs, currentEntity, isDetailBrush);
		for(int l=0;l<numSides;l++) { // For each side of the brush
			Vector3D[] plane=new Vector3D[3]; // Three points define a plane. All I have to do is find three points on that plane.
			v46BrushSide currentSide=BSP46.getBrushSides().getBrushSide(firstSide+l);
			Plane currentPlane=BSP46.getPlanes().getPlane(currentSide.getPlane()); // To find those three points, I can use vertices referenced by faces.
			v46Texture currentTexture=BSP46.getTextures().getTexture(currentSide.getTexture()); // To find those three points, I can use vertices referenced by faces.
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
			}
			if(numVertices==0 || !pointsWorked) { // Fallback to planar decompilation. Since there are no explicitly defined points anymore,
				                                   // we must find them ourselves using the A, B, C and D values.*/
				plane=GenericMethods.extrapPlanePoints(currentPlane);
			//}
			String texture=BSP46.getTextures().getTexture(currentSide.getTexture()).getTexture();
			/*if(toVMF) { // Figure out what Q3's trigger textures are
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
									if(texture.equalsIgnoreCase("special/npcclip")) {
										texture="tools/toolsnpcclip";
									}
								}
							}
						}
					}
				}
			} else { // To Gearcraft MAP. This also necessitates some corrections.
				
			}*/
			double[] textureS=new double[3];
			double[] textureT=new double[3];
			/*
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
			String material=BSP42.getMaterials().getString(currentFace.getMaterial());
			double lgtScale=16; // These values are impossible to get from a compiled map since they
			double lgtRot=0;    // are used by RAD for generating lightmaps, then are discarded, I believe.
			brushSides[l]=new MAPBrushSide(plane, texture, textureS, textureShiftS, textureT, textureShiftT,
			                               texRot, texScaleS, texScaleT, flags, material, lgtScale, lgtRot);
			numRealFaces++;
			if(brushSides[l]!=null) {
				if(pointsWorked) {
					mapBrush.add(brushSides[l], plane, currentPlane, true); // Add the MAPBrushSide to the current brush
				} else {
					mapBrush.add(brushSides[l], new Vector3D[0], currentPlane, false);
				}
			}*/
		}
		

		
		// TODO: Figure out why simplecorrect bombs
		if(correctPlaneFlip) {
			if(mapBrush.hasBadSide()) {
				try {
					mapBrush=GenericMethods.AdvancedCorrectPlanes(mapBrush); // This is good.
				} catch(java.lang.ArithmeticException e) {
					Window.window.println("Plane correct returned 0 triangles for entity "+mapBrush.getEntnum()+" brush "+mapBrush.getBrushnum()+"");
					if(calcVerts) {
						try {
							mapBrush=GenericMethods.CalcBrushVertices(mapBrush);
						} catch(java.lang.ArithmeticException f) {
							Window.window.println("Vertex calculation returned 0 triangles for entity "+mapBrush.getEntnum()+" brush "+mapBrush.getBrushnum()+"");
							for(int j=0;j<mapBrush.getNumSides();j++) {
								if(!mapBrush.getSide(j).isDefinedByTriangle()) {
									mapBrush.getSide(j).setSide(mapBrush.getSide(j).getPlane(), GenericMethods.extrapPlanePoints(mapBrush.getSide(j).getPlane()));
								}
							}
						}
					}
				}
			}
		} else {
			if(calcVerts) { // This is performed in advancedcorrect, so don't use it if that's happening
				try {
					mapBrush=GenericMethods.CalcBrushVertices(mapBrush);
				} catch(java.lang.ArithmeticException e) {
					Window.window.println("Vertex calculation returned 0 triangles for entity "+mapBrush.getEntnum()+" brush "+mapBrush.getBrushnum()+"");
				}
			}
		}
		
		/*
		if(correctPlaneFlip) {
			if(mapBrush.hasBadSide()) { // If there's a side that might be backward
				if(mapBrush.hasGoodSide()) { // If there's a side that is forward
					mapBrush=GenericMethods.SimpleCorrectPlanes(mapBrush);
					if(calcVerts) { // This is performed in advancedcorrect, so don't use it if that's happening
						mapBrush=GenericMethods.CalcBrushVertices(mapBrush);
					}
				} else { // If no forward side exists
					try {
						mapBrush=GenericMethods.AdvancedCorrectPlanes(mapBrush);
					} catch(java.lang.ArithmeticException e) {
						Window.window.println("Plane correct returned 0 triangles for entity "+mapBrush.getEntnum()+" brush "+mapBrush.getBrushnum()+"");
					}
				}
			}
		} else {
			if(calcVerts) { // This is performed in advancedcorrect, so don't use it if that's happening
				mapBrush=GenericMethods.CalcBrushVertices(mapBrush);
			}
		}
		*/
		
		// This adds the brush we've been finding and creating to
		// the current entity as an attribute. The way I've coded
		// this whole program and the entities parser, this shouldn't
		// cause any issues at all.
		if(toVMF && isDetailBrush) {
			Entity newDetailEntity=new Entity("func_detail");
			newDetailEntity.addBrush(mapBrush);
			mapFile.add(newDetailEntity);
		} else {
			mapFile.getEntity(currentEntity).addBrush(mapBrush);
		}
	}
	
	// Attempt to turn the Quake 2 BSP into a .MAP file
	public void decompileBSP38() throws java.io.IOException {
		// Begin by copying all the entities into another Lump00 object. This is
		// necessary because if I just modified the current entity list then it
		// could be saved back into the BSP and really mess some stuff up.
		mapFile=new Entities(BSP38.getEntities());
		//int numAreaPortals=0;
		int numTotalItems=0;
		boolean containsAreaPortals=false;
		for(int i=0;i<BSP38.getEntities().getNumElements();i++) { // For each entity
			if(toVMF) { // correct some entities to make source ports easier, TODO
				if(mapFile.getEntity(i).getAttribute("classname").equalsIgnoreCase("func_wall")) {
					mapFile.getEntity(i).setAttribute("classname", "func_brush"); // Doubt I need a case for func_detail here
				}
			} else { // Gearcraft also requires some changes, do those here
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
				boolean[] brushesUsed=new boolean[BSP38.getBrushes().getNumElements()]; // Keep a list of brushes already in the model, since sometimes the leaves lump references one brush several times
				numBrshs=0; // Reset the brush count for each entity
				for(int j=0;j<numLeaves;j++) { // For each leaf in the bunch
					v38Leaf currentLeaf=leaves[j];
					short firstBrushIndex=currentLeaf.getFirstMarkBrush();
					short numBrushIndices=currentLeaf.getNumMarkBrushes();
					if(numBrushIndices>0) { // A lot of leaves reference no brushes. If this is one, this iteration of the j loop is finished
						for(int k=0;k<numBrushIndices;k++) { // For each brush referenced
							if(!brushesUsed[BSP38.getMarkBrushes().getShort(firstBrushIndex+k)]) { // If the current brush has NOT been used in this entity
								brushesUsed[BSP38.getMarkBrushes().getShort(firstBrushIndex+k)]=true;
								Brush brush=BSP38.getBrushes().getBrush(BSP38.getMarkBrushes().getShort(firstBrushIndex+k));
								if(!(brush.getAttributes()[1]==-128)) {
									decompileBrush38(brush, i); // Decompile the brush
								} else {
									containsAreaPortals=true;
								}
								numBrshs++;
								numTotalItems++;
								Window.setProgress(jobnum, numTotalItems, BSP38.getBrushes().getNumElements()+BSP38.getEntities().getNumElements(), "Decompiling...");
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
				if(!toVMF) {
					if(origin[0]!=0 || origin[1]!=0 || origin[2]!=0) { // If this brush uses the "origin" attribute
						addOriginBrush(i);
					}
					//mapFile.getEntity(i).deleteAttribute("origin");
				}
			}
			numTotalItems++; // This entity
			Window.setProgress(jobnum, numTotalItems, BSP38.getBrushes().getNumElements()+BSP38.getEntities().getNumElements(), "Decompiling...");
		}
		if(containsAreaPortals) { // If this map was found to have area portals
			int j=0;
			for(int i=0;i<BSP38.getBrushes().getNumElements();i++) { // For each brush in this map
				if(BSP38.getBrushes().getBrush(i).getAttributes()[1]==-128) { // If the brush is an area portal brush
					for(j++;j<BSP38.getEntities().getNumElements();j++) { // Find an areaportal entity
						if(BSP38.getEntities().getEntity(j).getAttribute("classname").equalsIgnoreCase("func_areaportal")) {
							decompileBrush38(BSP38.getBrushes().getBrush(i), j); // Add the brush to that entity
							break; // And break out of the inner loop, but remember your place.
						}
					}
					if(j==BSP38.getEntities().getNumElements()) { // If we're out of entities, stop this whole thing.
						break;
					}
				}
			}
		}
		//Window.window.println(BSP38.getMapName()+" Num areaportals "+numAreaPortals+" area portals lump length "+BSP38.getAreaPortals().getLength());
		Window.setProgress(jobnum, numTotalItems, BSP38.getBrushes().getNumElements()+BSP38.getEntities().getNumElements(), "Saving...");
		if(toVMF) {
			Window.window.println("Saving "+BSP38.getPath().substring(0, BSP38.getPath().length()-4)+".vmf...");
			VMFWriter VMFMaker=new VMFWriter(mapFile, BSP38.getPath().substring(0, BSP38.getPath().length()-4), roundNums);
			VMFMaker.write();
		} else {
			Window.window.println("Saving "+BSP38.getPath().substring(0, BSP38.getPath().length()-4)+".map...");
			MAP510Writer MAPMaker=new MAP510Writer(mapFile, BSP38.getPath().substring(0, BSP38.getPath().length()-4), roundNums);
			MAPMaker.write();
		}
		Window.window.println("Process completed!");
	}

	// -decompileBrush38(Brush, int, boolean)
	// Decompiles the Brush and adds it to entitiy #currentEntity as .MAP data.
	private void decompileBrush38(Brush brush, int currentEntity) {
		double[] origin=mapFile.getEntity(currentEntity).getOrigin();
		boolean containsWaterTexture=false;
		int firstSide=brush.getFirstSide();
		int numSides=brush.getNumSides();
		MAPBrushSide[] brushSides=new MAPBrushSide[numSides];
		MAPBrush mapBrush = new MAPBrush(numBrshs, currentEntity, false);
		for(int l=0;l<numSides;l++) { // For each side of the brush
			Vector3D[] plane=new Vector3D[3]; // Three points define a plane. All I have to do is find three points on that plane.
			v38BrushSide currentSide=BSP38.getBrushSides().getBrushSide(firstSide+l);
			Plane currentPlane=BSP38.getPlanes().getPlane(currentSide.getPlane()).getPlane(); // To find those three points, I must extrapolate from planes until I find a way to associate faces with brushes
			v38Texture currentTexture;
			boolean isDuplicate=false;
			for(int i=l+1;i<numSides;i++) { // For each subsequent side of the brush
				if(currentPlane.equals(BSP38.getPlanes().getPlane(BSP38.getBrushSides().getBrushSide(firstSide+i).getPlane()))) {
					Window.window.println("Duplicate planes, sides "+l+" and "+i);
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
					if(toVMF) { // TODO more to do here
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
				brushSides[l]=new MAPBrushSide(plane, texture, textureS, textureShiftS, textureT, textureShiftT,
				                               texRot, texScaleS, texScaleT, flags, material, lgtScale, lgtRot);
				if(brushSides[l]!=null) {
					mapBrush.add(brushSides[l]);
				}
			}
		}

		// TODO: Figure out why simplecorrect bombs
		if(correctPlaneFlip) {
			if(mapBrush.hasBadSide()) {
				try {
					mapBrush=GenericMethods.AdvancedCorrectPlanes(mapBrush); // This is good.
				} catch(java.lang.ArithmeticException e) {
					Window.window.println("Plane correct returned 0 triangles for entity "+mapBrush.getEntnum()+" brush "+mapBrush.getBrushnum()+"");
					if(calcVerts) {
						try {
							mapBrush=GenericMethods.CalcBrushVertices(mapBrush);
						} catch(java.lang.ArithmeticException f) {
							Window.window.println("Vertex calculation returned 0 triangles for entity "+mapBrush.getEntnum()+" brush "+mapBrush.getBrushnum()+"");
							for(int j=0;j<mapBrush.getNumSides();j++) {
								if(!mapBrush.getSide(j).isDefinedByTriangle()) {
									mapBrush.getSide(j).setSide(mapBrush.getSide(j).getPlane(), GenericMethods.extrapPlanePoints(mapBrush.getSide(j).getPlane()));
								}
							}
						}
					}
				}
			}
		} else {
			if(calcVerts) { // This is performed in advancedcorrect, so don't use it if that's happening
				try {
					mapBrush=GenericMethods.CalcBrushVertices(mapBrush);
				} catch(java.lang.ArithmeticException e) {
					Window.window.println("Vertex calculation returned 0 triangles for entity "+mapBrush.getEntnum()+" brush "+mapBrush.getBrushnum()+"");
				}
			}
		}
		
		/*
		if(correctPlaneFlip) {
			if(mapBrush.hasBadSide()) { // If there's a side that might be backward
				if(mapBrush.hasGoodSide()) { // If there's a side that is forward
					mapBrush=GenericMethods.SimpleCorrectPlanes(mapBrush);
					if(calcVerts) { // This is performed in advancedcorrect, so don't use it if that's happening
						mapBrush=GenericMethods.CalcBrushVertices(mapBrush);
					}
				} else { // If no forward side exists
					try {
						mapBrush=GenericMethods.AdvancedCorrectPlanes(mapBrush);
					} catch(java.lang.ArithmeticException e) {
						Window.window.println("Plane correct returned 0 triangles for entity "+mapBrush.getEntnum()+" brush "+mapBrush.getBrushnum()+"");
					}
				}
			}
		} else {
			if(calcVerts) { // This is performed in advancedcorrect, so don't use it if that's happening
				mapBrush=GenericMethods.CalcBrushVertices(mapBrush);
			}
		}
		*/
		
		// This adds the brush we've been finding and creating to
		// the current entity as an attribute. The way I've coded
		// this whole program and the entities parser, this shouldn't
		// cause any issues at all.
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
	
	// Attempts to convert a map in a Doom WAD into a usable .MAP file. This has many
	// challenges, not the least of which is the fact that the Doom engine didn't use
	// brushes (at least, not in any sane way).
	private void decompileDoomMap() throws java.io.IOException {
		Window.window.println(doomMap.getMapName());
		
		mapFile=new Entities();
		Entity world = new Entity("worldspawn");
		world.setAttribute("mapversion", "510");
		mapFile.add(world);
		
		String[] lowerWallTextures=new String[doomMap.getSidedefs().getNumElements()];
		String[] midWallTextures=new String[doomMap.getSidedefs().getNumElements()];
		String[] higherWallTextures=new String[doomMap.getSidedefs().getNumElements()];
		
		short[] sectorType=new short[doomMap.getSectors().getNumElements()];
		
		// Since Doom relied on sectors to define a cieling and floor height, and nothing else,
		// need to find the minimum and maximum used Z values. This is because the Doom engine
		// is only a pseudo-3D engine. For all it cares, the cieling and floor extend to their
		// respective infinities. For a GC/Hammer map, however, this cannot be the case.
		int ZMin=32767;  // Even though the values in the map will never exceed these, use ints here to avoid
		int ZMax=-32768; // overflows, in case the map DOES go within 32 units of these values.
		for(int i=0;i<doomMap.getSectors().getNumElements();i++) {
			DSector currentSector=doomMap.getSectors().getSector(i);
			if(currentSector.getFloorHeight()<ZMin+32) {
				ZMin=currentSector.getFloorHeight()-32; // Can't use the actual value, because that IS the floor
			} else {
				if(currentSector.getCielingHeight()>ZMax-32) {
					ZMax=currentSector.getCielingHeight()+32; // or the cieling. Subtract or add a sane value to it.
				}
			}
		}
		
		// Also need to find minimum and maximum X and Y values. Best way to do this is probably
		// to search the vertices lump, and also pad it by 32 units.
		double XMin=32767;
		double XMax=-32768;
		double YMin=32767;
		double YMax=-32768;
		for(int i=0;i<doomMap.getVertices().getNumElements();i++) {
			Vector3D currentVertex=doomMap.getVertices().getVertex(i);
			if(currentVertex.getX()<XMin+32) {
				XMin=currentVertex.getX()-32;
			} else {
				if(currentVertex.getX()>XMax-32) {
					XMax=currentVertex.getX()+32;
				}
			}
			if(currentVertex.getY()<YMin+32) {
				YMin=currentVertex.getY()-32;
			} else {
				if(currentVertex.getY()>YMax-32) {
					YMax=currentVertex.getY()+32;
				}
			}
		}
		
		// Now create a few brush sides to be used to pad the sides of the map. This is
		// needed since walls for the outside of the map don't define the outer sides.
		// Left
		Vector3D[] outsideLeftPlane=new Vector3D[3];
		double[] outsideLeftTexS=new double[3];
		double[] outsideLeftTexT=new double[3];
		outsideLeftPlane[0]=new Vector3D(XMin, 1, 1);
		outsideLeftPlane[1]=new Vector3D(XMin, 0, 1);
		outsideLeftPlane[2]=new Vector3D(XMin, 0, 0);
		outsideLeftTexS[1]=1;
		outsideLeftTexT[2]=-1;
		MAPBrushSide outsideLeft;
		if(toVMF) {
			outsideLeft=new MAPBrushSide(outsideLeftPlane, "tools/toolsnodraw", outsideLeftTexS, 0, outsideLeftTexT, 0, 0, 1, 1, 0, "wld_lightmap", 16, 0);
		} else {
			outsideLeft=new MAPBrushSide(outsideLeftPlane, "special/nodraw", outsideLeftTexS, 0, outsideLeftTexT, 0, 0, 1, 1, 0, "wld_lightmap", 16, 0);
		}
		// Right
		Vector3D[] outsideRightPlane=new Vector3D[3];
		double[] outsideRightTexS=new double[3];
		double[] outsideRightTexT=new double[3];
		outsideRightPlane[0]=new Vector3D(XMax, 1, 0);
		outsideRightPlane[1]=new Vector3D(XMax, 0, 0);
		outsideRightPlane[2]=new Vector3D(XMax, 0, 1);
		outsideRightTexS[1]=1;
		outsideRightTexT[2]=-1;
		MAPBrushSide outsideRight;
		if(toVMF) {
			outsideRight=new MAPBrushSide(outsideRightPlane, "tools/toolsnodraw", outsideRightTexS, 0, outsideRightTexT, 0, 0, 1, 1, 0, "wld_lightmap", 16, 0);
		} else {
			outsideRight=new MAPBrushSide(outsideRightPlane, "special/nodraw", outsideRightTexS, 0, outsideRightTexT, 0, 0, 1, 1, 0, "wld_lightmap", 16, 0);
		}
		// Near
		Vector3D[] outsideNearPlane=new Vector3D[3];
		double[] outsideNearTexS=new double[3];
		double[] outsideNearTexT=new double[3];
		outsideNearPlane[0]=new Vector3D(1, YMax, 1);
		outsideNearPlane[1]=new Vector3D(0, YMax, 1);
		outsideNearPlane[2]=new Vector3D(0, YMax, 0);
		outsideNearTexS[0]=1;
		outsideNearTexT[2]=-1;
		MAPBrushSide outsideNear;
		if(toVMF) {
			outsideNear=new MAPBrushSide(outsideNearPlane, "tools/toolsnodraw", outsideNearTexS, 0, outsideNearTexT, 0, 0, 1, 1, 0, "wld_lightmap", 16, 0);
		} else {
			outsideNear=new MAPBrushSide(outsideNearPlane, "special/nodraw", outsideNearTexS, 0, outsideNearTexT, 0, 0, 1, 1, 0, "wld_lightmap", 16, 0);
		}
		// Far
		Vector3D[] outsideFarPlane=new Vector3D[3];
		double[] outsideFarTexS=new double[3];
		double[] outsideFarTexT=new double[3];
		outsideFarPlane[0]=new Vector3D(1, YMin, 0);
		outsideFarPlane[1]=new Vector3D(0, YMin, 0);
		outsideFarPlane[2]=new Vector3D(0, YMin, 1);
		outsideFarTexS[0]=1;
		outsideFarTexT[2]=-1;
		MAPBrushSide outsideFar;
		if(toVMF) {
			outsideFar=new MAPBrushSide(outsideFarPlane, "tools/toolsnodraw", outsideFarTexS, 0, outsideFarTexT, 0, 0, 1, 1, 0, "wld_lightmap", 16, 0);
		} else {
			outsideFar=new MAPBrushSide(outsideFarPlane, "special/nodraw", outsideFarTexS, 0, outsideFarTexT, 0, 0, 1, 1, 0, "wld_lightmap", 16, 0);
		}
		
		// I need to analyze the binary tree and get more information, particularly the
		// parent nodes of each subsector and node, as well as whether it's the right or
		// left child of that node. These are extremely important, as the parent defines
		// boundaries for the children, as well as inheriting further boundaries from its
		// parents. These boundaries are invaluable for forming brushes.
		int[] nodeparents = new int[doomMap.getNodes().getNumElements()];
		boolean[] nodeIsLeft = new boolean[doomMap.getNodes().getNumElements()];
		
		for(int i=0;i<doomMap.getNodes().getNumElements();i++) {
			nodeparents[i]=-1; // There should only be one node left with -1 as a parent. This SHOULD be the root.
			for(int j=0;j<doomMap.getNodes().getNumElements();j++) {
				if(doomMap.getNodes().getNode(j).getChild1() == i) {
					nodeparents[i]=j;
					break;
				} else {
					if(doomMap.getNodes().getNode(j).getChild2() == i) {
						nodeparents[i]=j;
						nodeIsLeft[i]=true;
						break;
					}
				}
			}
		}
		
		int[] subsectorSectors = new int[doomMap.getSubSectors().getNumElements()];
		// Keep a list of what sidedefs belong to what subsector as well
		int[][] subsectorSidedefs = new int[doomMap.getSubSectors().getNumElements()][];
		
		// Figure out what sector each subsector belongs to, and what node is its parent.
		// Depending on sector "tags" this will help greatly in creation of brushbased entities,
		// and also helps in finding subsector floor and cieling heights.
		int[] ssparents = new int[doomMap.getSubSectors().getNumElements()];
		boolean[] ssIsLeft = new boolean[doomMap.getSubSectors().getNumElements()];
		for(int i=0;i<doomMap.getSubSectors().getNumElements();i++) {
			// First, find the subsector's parent and whether it is the left or right child.
			ssparents[i]=-1; // No subsector should have a -1 in here
			for(int j=0;j<doomMap.getNodes().getNumElements();j++) {
				// When a node references a subsector, it is not referenced by negative
				// index, as future BSP versions do. The bits 0-14 ARE the index, and
				// bit 15 (which is the sign bit in two's compliment math) determines
				// whether or not it is a node or subsector. Therefore, we need to add
				// 2^15 to the number to produce the actual index.
				if(doomMap.getNodes().getNode(j).getChild1()+32768 == i) {
					ssparents[i]=j;
					break;
				} else {
					if(doomMap.getNodes().getNode(j).getChild2()+32768 == i) {
						ssparents[i]=j;
						ssIsLeft[i]=true;
						break;
					}
				}
			}
			
			// Second, figure out what sector a subsector belongs to, and the type of sector it is.
			subsectorSectors[i]=-1;
			DSubSector currentsubsector=doomMap.getSubSectors().getSubSector(i);
			subsectorSidedefs[i]=new int[currentsubsector.getNumSegs()];
			for(int j=0;j<currentsubsector.getNumSegs();j++) { // For each segment the subsector references
				DSegment currentsegment=doomMap.getSegments().getSegment(currentsubsector.getFirstSeg()+j);
				DLinedef currentlinedef=doomMap.getLinedefs().getLinedef(currentsegment.getLinedef());
				int currentsidedefIndex;
				int othersideIndex;
				if(currentsegment.getDirection()==0) {
					currentsidedefIndex=currentlinedef.getRight();
					othersideIndex=currentlinedef.getLeft();
				} else {
					currentsidedefIndex=currentlinedef.getLeft();
					othersideIndex=currentlinedef.getRight();
				}
				subsectorSidedefs[i][j]=currentsidedefIndex;
				DSidedef currentSidedef=doomMap.getSidedefs().getSide(currentsidedefIndex);
				if(currentlinedef.getType()!=0 && othersideIndex!=-1) { // If this is a triggering linedef
					DSidedef otherSidedef=doomMap.getSidedefs().getSide(othersideIndex);
					if(currentlinedef.getTag()!=0) { // If the target is not 0
						for(int k=0;k<doomMap.getSectors().getNumElements();k++) {
							DSector taggedsector=doomMap.getSectors().getSector(k);
							if(taggedsector.getTag()==currentlinedef.getTag()) {
								sectorType[k]=currentlinedef.getType();
							}
						}
					} else {
						sectorType[currentSidedef.getSector()]=currentlinedef.getType();
					}
				}
				if(!currentSidedef.getMidTexture().equals("-")) {
					midWallTextures[currentsidedefIndex]=doomMap.getWadName()+"/"+currentSidedef.getMidTexture();
				} else {
					if(toVMF) {
						midWallTextures[currentsidedefIndex]="tools/toolsnodraw";
					} else {
						midWallTextures[currentsidedefIndex]="special/nodraw";
					}
				}
				if(!currentSidedef.getHighTexture().equals("-")) {
					higherWallTextures[currentsidedefIndex]=doomMap.getWadName()+"/"+currentSidedef.getHighTexture();
				} else {
					if(toVMF) {
						higherWallTextures[currentsidedefIndex]="tools/toolsnodraw";
					} else {
						higherWallTextures[currentsidedefIndex]="special/nodraw";
					}
				}
				if(!currentSidedef.getLowTexture().equals("-")) {
					lowerWallTextures[currentsidedefIndex]=doomMap.getWadName()+"/"+currentSidedef.getLowTexture();
				} else {
					if(toVMF) {
						lowerWallTextures[currentsidedefIndex]="tools/toolsnodraw";
					} else {
						lowerWallTextures[currentsidedefIndex]="special/nodraw";
					}
				}
				// Sometimes a subsector seems to belong to more than one sector. I don't know why.
				if(subsectorSectors[i]!=-1 && currentSidedef.getSector()!=subsectorSectors[i]) {
					Window.window.println("WARNING: Subsector "+i+" has sides defining different sectors!");
					Window.window.println("This is probably nothing to worry about, but something might be wrong (wrong floor/cieling height)");
				} else {
					subsectorSectors[i]=currentSidedef.getSector();
				}
			}
			
			// Third, create a few brushes out of the geometry.
			MAPBrush cielingBrush=new MAPBrush(numBrshs++, 0, false);
			MAPBrush floorBrush=new MAPBrush(numBrshs++, 0, false);
			MAPBrush midBrush=new MAPBrush(numBrshs++, 0, false);
			DSector currentSector=doomMap.getSectors().getSector(subsectorSectors[i]);
			
			Vector3D[] roofPlane=new Vector3D[3];
			double[] roofTexS=new double[3];
			double[] roofTexT=new double[3];
			roofPlane[0]=new Vector3D(0, 1, ZMax);
			roofPlane[1]=new Vector3D(1, 1, ZMax);
			roofPlane[2]=new Vector3D(1, 0, ZMax);
			roofTexS[0]=1;
			roofTexT[1]=-1;
			MAPBrushSide roof=new MAPBrushSide(roofPlane, doomMap.getWadName()+"/"+currentSector.getCielingTexture(), roofTexS, 0, roofTexT, 0, 0, 1, 1, 0, "wld_lightmap", 16, 0);
			
			Vector3D[] cileingPlane=new Vector3D[3];
			double[] cileingTexS=new double[3];
			double[] cileingTexT=new double[3];
			cileingPlane[0]=new Vector3D(0, 0, currentSector.getCielingHeight());
			cileingPlane[1]=new Vector3D(1, 0, currentSector.getCielingHeight());
			cileingPlane[2]=new Vector3D(1, 1, currentSector.getCielingHeight());
			cileingTexS[0]=1;
			cileingTexT[1]=-1;
			MAPBrushSide cieling=new MAPBrushSide(cileingPlane, doomMap.getWadName()+"/"+currentSector.getCielingTexture(), cileingTexS, 0, cileingTexT, 0, 0, 1, 1, 0, "wld_lightmap", 16, 0);
			
			Vector3D[] floorPlane=new Vector3D[3];
			double[] floorTexS=new double[3];
			double[] floorTexT=new double[3];
			floorPlane[0]=new Vector3D(0, 1, currentSector.getFloorHeight());
			floorPlane[1]=new Vector3D(1, 1, currentSector.getFloorHeight());
			floorPlane[2]=new Vector3D(1, 0, currentSector.getFloorHeight());
			floorTexS[0]=1;
			floorTexT[1]=-1;
			MAPBrushSide floor=new MAPBrushSide(floorPlane, doomMap.getWadName()+"/"+currentSector.getFloorTexture(), floorTexS, 0, floorTexT, 0, 0, 1, 1, 0, "wld_lightmap", 16, 0);

			Vector3D[] foundationPlane=new Vector3D[3];
			double[] foundationTexS=new double[3];
			double[] foundationTexT=new double[3];
			foundationPlane[0]=new Vector3D(0, 0, ZMin);
			foundationPlane[1]=new Vector3D(1, 0, ZMin);
			foundationPlane[2]=new Vector3D(1, 1, ZMin);
			foundationTexS[0]=1;
			foundationTexT[1]=-1;
			MAPBrushSide foundation=new MAPBrushSide(foundationPlane, doomMap.getWadName()+"/"+currentSector.getFloorTexture(), foundationTexS, 0, foundationTexT, 0, 0, 1, 1, 0, "wld_lightmap", 16, 0);
			
			cielingBrush.add(cieling);
			cielingBrush.add(roof);
			cielingBrush.add(outsideLeft);
			cielingBrush.add(outsideRight);
			cielingBrush.add(outsideNear);
			cielingBrush.add(outsideFar);
			
			floorBrush.add(floor);
			floorBrush.add(foundation);
			floorBrush.add(outsideLeft);
			floorBrush.add(outsideRight);
			floorBrush.add(outsideNear);
			floorBrush.add(outsideFar);

			int nextNode=ssparents[i];
			boolean leftSide=ssIsLeft[i];

			for(int j=0;j<subsectorSidedefs[i].length;j++) {
				DSegment currentseg=doomMap.getSegments().getSegment(currentsubsector.getFirstSeg()+j);
				Vector3D start=doomMap.getVertices().getVertex(currentseg.getStartVertex());
				Vector3D end=doomMap.getVertices().getVertex(currentseg.getEndVertex());
				DLinedef currentLinedef=doomMap.getLinedefs().getLinedef(currentseg.getLinedef());
				
				Vector3D[] plane=new Vector3D[3];
				double[] texS=new double[3];
				double[] texT=new double[3];
				plane[0]=new Vector3D(start.getX(), start.getY(), ZMin);
				plane[1]=new Vector3D(end.getX(), end.getY(), ZMin);
				plane[2]=new Vector3D(end.getX(), end.getY(), ZMax);
				
				double sideLength=Math.sqrt(Math.pow(start.getX()-end.getX(), 2) + Math.pow(start.getY()-end.getY(),2));
				
				texS[0]=(start.getX()-end.getX())/sideLength;
				texS[1]=(start.getY()-end.getY())/sideLength;
				texS[2]=0;
				texT[0]=0;
				texT[1]=0;
				texT[2]=-1;
				MAPBrushSide low=new MAPBrushSide(plane, lowerWallTextures[subsectorSidedefs[i][j]], texS, 0, texT, 0, 0, 1, 1, 0, "wld_lightmap", 16, 0);
				MAPBrushSide high=new MAPBrushSide(plane, higherWallTextures[subsectorSidedefs[i][j]], texS, 0, texT, 0, 0, 1, 1, 0, "wld_lightmap", 16, 0);
				MAPBrushSide mid;
				
				if(currentLinedef.isOneSided()) {
					MAPBrush outsideBrush=null;
					if(midWallTextures[subsectorSidedefs[i][j]]!="special/nodraw") {
						outsideBrush = createFaceBrush(midWallTextures[subsectorSidedefs[i][j]], plane[0], plane[2]);
					} else { // If the outside sidedef uses no texture
						for(int k=0;k<subsectorSidedefs[i].length;k++) { // That is BULL SHIT! Find a side of the subsector that uses one
							if(midWallTextures[subsectorSidedefs[i][k]]!="special/nodraw") {
								outsideBrush = createFaceBrush(midWallTextures[subsectorSidedefs[i][k]], plane[0], plane[2]);
							}
						}
						if(outsideBrush==null) { // If no side of the subsector uses one, then fuck.
							if(toVMF) {
								outsideBrush = createFaceBrush("tools/toolsnodraw", plane[0], plane[2]);
							} else {
								outsideBrush = createFaceBrush("special/nodraw", plane[0], plane[2]);
							}
						}
					}
					world.addBrush(outsideBrush);
					if(toVMF) {
						mid=new MAPBrushSide(plane, "tools/toolsnodraw", texS, 0, texT, 0, 0, 1, 1, 0, "wld_lightmap", 16, 0);
					} else {
						mid=new MAPBrushSide(plane, "special/nodraw", texS, 0, texT, 0, 0, 1, 1, 0, "wld_lightmap", 16, 0);
					}
				} else {
					mid=new MAPBrushSide(plane, midWallTextures[subsectorSidedefs[i][j]], texS, 0, texT, 0, 0, 1, 1, 0, "wld_lightmap", 16, 0);
				}
				
				cielingBrush.add(high);
				midBrush.add(mid);
				floorBrush.add(low);
			}
			do {
				DNode currentNode=doomMap.getNodes().getNode(nextNode);
				Vector3D start;
				Vector3D end;
				if(leftSide) {
					start=currentNode.getVecHead().add(currentNode.getVecTail());
					end=currentNode.getVecHead();
				} else {
					start=currentNode.getVecHead();
					end=currentNode.getVecHead().add(currentNode.getVecTail());
				}
				
				Vector3D[] plane=new Vector3D[3];
				double[] texS=new double[3];
				double[] texT=new double[3];
				// This is somehow always correct. And I'm okay with that.
				plane[0]=new Vector3D(start.getX(), start.getY(), ZMin);
				plane[1]=new Vector3D(end.getX(), end.getY(), ZMin);
				plane[2]=new Vector3D(start.getX(), start.getY(), ZMax);
				
				double sideLength=Math.sqrt(Math.pow(start.getX()-end.getX(), 2) + Math.pow(start.getY()-end.getY(),2));
				
				texS[0]=(start.getX()-end.getX())/sideLength;
				texS[1]=(start.getY()-end.getY())/sideLength;
				texS[2]=0;
				texT[0]=0;
				texT[1]=0;
				texT[2]=1;
				MAPBrushSide low=new MAPBrushSide(plane, lowerWallTextures[subsectorSidedefs[i][0]], texS, 0, texT, 0, 0, 1, 1, 0, "wld_lightmap", 16, 0);
				MAPBrushSide high=new MAPBrushSide(plane, higherWallTextures[subsectorSidedefs[i][0]], texS, 0, texT, 0, 0, 1, 1, 0, "wld_lightmap", 16, 0);
				MAPBrushSide mid=new MAPBrushSide(plane, midWallTextures[subsectorSidedefs[i][0]], texS, 0, texT, 0, 0, 1, 1, 0, "wld_lightmap", 16, 0);
				
				cielingBrush.add(high);
				midBrush.add(mid);
				floorBrush.add(low);
				
				leftSide=nodeIsLeft[nextNode];
				nextNode=nodeparents[nextNode];
			} while(nextNode!=-1);
			cielingBrush=GenericMethods.cullUnusedPlanes(cielingBrush);
			midBrush=GenericMethods.cullUnusedPlanes(midBrush);
			floorBrush=GenericMethods.cullUnusedPlanes(floorBrush);
			world.addBrush(floorBrush);
			world.addBrush(cielingBrush);
			boolean containsMiddle=false; // Need to figure out how to determine this. As it is, no middle sides will come out.
			if(containsMiddle && currentSector.getCielingHeight() > currentSector.getFloorHeight()) {
				Entity middleEnt=new Entity("func_illusionary");
				Vector3D[] topPlane=new Vector3D[3];
				double[] topTexS=new double[3];
				double[] topTexT=new double[3];
				topPlane[0]=new Vector3D(0, 1, currentSector.getCielingHeight());
				topPlane[1]=new Vector3D(1, 1, currentSector.getCielingHeight());
				topPlane[2]=new Vector3D(1, 0, currentSector.getCielingHeight());
				topTexS[0]=1;
				topTexT[1]=-1;
				MAPBrushSide top=new MAPBrushSide(topPlane, doomMap.getWadName()+"/"+currentSector.getFloorTexture(), topTexS, 0, topTexT, 0, 0, 1, 1, 0, "wld_lightmap", 16, 0);
	
				Vector3D[] bottomPlane=new Vector3D[3];
				double[] bottomTexS=new double[3];
				double[] bottomTexT=new double[3];
				bottomPlane[0]=new Vector3D(0, 0, currentSector.getFloorHeight());
				bottomPlane[1]=new Vector3D(1, 0, currentSector.getFloorHeight());
				bottomPlane[2]=new Vector3D(1, 1, currentSector.getFloorHeight());
				bottomTexS[0]=1;
				bottomTexT[1]=-1;
				MAPBrushSide bottom=new MAPBrushSide(bottomPlane, doomMap.getWadName()+"/"+currentSector.getFloorTexture(), bottomTexS, 0, bottomTexT, 0, 0, 1, 1, 0, "wld_lightmap", 16, 0);

				midBrush.add(top);
				midBrush.add(bottom);
				midBrush.add(outsideLeft);
				midBrush.add(outsideRight);
				midBrush.add(outsideNear);
				midBrush.add(outsideFar);
				
				middleEnt.addBrush(midBrush);
				mapFile.add(middleEnt);
			}
			Window.setProgress(jobnum, i+1, doomMap.getSubSectors().getNumElements(), "Decompiling...");
		}
		
		Window.setProgress(jobnum, 1, 1, "Saving...");
		if(toVMF) {
			Window.window.println("Saving "+doomMap.getFolder()+doomMap.getWadName()+"\\"+doomMap.getMapName()+".vmf...");
			VMFWriter VMFMaker=new VMFWriter(mapFile, doomMap.getFolder()+doomMap.getWadName()+"\\"+doomMap.getMapName(), roundNums);
			VMFMaker.write();
		} else {
			Window.window.println("Saving "+doomMap.getFolder()+doomMap.getWadName()+"\\"+doomMap.getMapName()+".map...");
			MAP510Writer MAPMaker=new MAP510Writer(mapFile, doomMap.getFolder()+doomMap.getWadName()+"\\"+doomMap.getMapName(), roundNums);
			MAPMaker.write();
		}
	}
	
	public void addOriginBrush(int ent) {
		double[] origin=new double[3];
		MAPBrush newOriginBrush=new MAPBrush(numBrshs++, ent, false);
		Vector3D[][] planes=new Vector3D[6][3]; // Six planes for a cube brush, three vertices for each plane
		double[][] textureS=new double[6][3];
		double[][] textureT=new double[6][3];
		// The planes and their texture scales
		// I got these from an origin brush created by Gearcraft. Don't worry where these numbers came from, they work.
		// Top
		planes[0][0]=new Vector3D(-16+origin[0], 16+origin[1], 16+origin[2]);
		planes[0][1]=new Vector3D(16+origin[0], 16+origin[1], 16+origin[2]);
		planes[0][2]=new Vector3D(16+origin[0], -16+origin[1], 16+origin[2]);
		textureS[0][0]=1;
		textureT[0][1]=-1;
		// Bottom
		planes[1][0]=new Vector3D(-16+origin[0], -16+origin[1], -16+origin[2]);
		planes[1][1]=new Vector3D(16+origin[0], -16+origin[1], -16+origin[2]);
		planes[1][2]=new Vector3D(16+origin[0], 16+origin[1], -16+origin[2]);
		textureS[1][0]=1;
		textureT[1][1]=-1;
		// Left
		planes[2][0]=new Vector3D(-16+origin[0], 16+origin[1], 16+origin[2]);
		planes[2][1]=new Vector3D(-16+origin[0], -16+origin[1], 16+origin[2]);
		planes[2][2]=new Vector3D(-16+origin[0], -16+origin[1], -16+origin[2]);
		textureS[2][1]=1;
		textureT[2][2]=-1;
		// Right
		planes[3][0]=new Vector3D(16+origin[0], 16+origin[1], -16+origin[2]);
		planes[3][1]=new Vector3D(16+origin[0], -16+origin[1], -16+origin[2]);
		planes[3][2]=new Vector3D(16+origin[0], -16+origin[1], 16+origin[2]);
		textureS[3][1]=1;
		textureT[3][2]=-1;
		// Near
		planes[4][0]=new Vector3D(16+origin[0], 16+origin[1], 16+origin[2]);
		planes[4][1]=new Vector3D(-16+origin[0], 16+origin[1], 16+origin[2]);
		planes[4][2]=new Vector3D(-16+origin[0], 16+origin[1], -16+origin[2]);
		textureS[4][0]=1;
		textureT[4][2]=-1;
		// Far
		planes[5][0]=new Vector3D(16+origin[0], -16+origin[1], -16+origin[2]);
		planes[5][1]=new Vector3D(-16+origin[0], -16+origin[1], -16+origin[2]);
		planes[5][2]=new Vector3D(-16+origin[0], -16+origin[1], 16+origin[2]);
		textureS[5][0]=1;
		textureT[5][2]=-1;
		
		for(int j=0;j<6;j++) {
			MAPBrushSide currentEdge;
			if(toVMF) {
				currentEdge=new MAPBrushSide(planes[j], "tools/toolsorigin", textureS[j], 0, textureT[j], 0, 0, 1, 1, 0, "wld_lightmap", 16, 0);
			} else {
				currentEdge=new MAPBrushSide(planes[j], "special/origin", textureS[j], 0, textureT[j], 0, 0, 1, 1, 0, "wld_lightmap", 16, 0);
			}
			newOriginBrush.add(currentEdge);
		}
		mapFile.getEntity(ent).addBrush(newOriginBrush);
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
			if(toVMF) {
				currentEdge=new MAPBrushSide(planes[j], "tools/toolstrigger", textureS[j], 0, textureT[j], 0, 0, 1, 1, 0, "wld_lightmap", 16, 0);
			} else {
				currentEdge=new MAPBrushSide(planes[j], "special/trigger", textureS[j], 0, textureT[j], 0, 0, 1, 1, 0, "wld_lightmap", 16, 0);
			}
			newTriggerBrush.add(currentEdge);
		}
		return newTriggerBrush;
	}
	
	// createFaceBrush(String, Vector3D, Vector3D)
	// This creates a rectangular brush. The String is assumed to be a texture for a face, and
	// the two vectors are a bounding box to create a plane with (mins-maxs). This can be expanded
	// later to include passing of texture scaling and positioning vectors as well, but this is
	// all I need right now.
	public MAPBrush createFaceBrush(String texture, Vector3D mins, Vector3D maxs) {
		MAPBrush newBrush = new MAPBrush(numBrshs++, 0, false);
		numBrshs++;
		Vector3D[][] planes=new Vector3D[6][3]; // Six planes for a cube brush, three vertices for each plane
		double[][] texS=new double[6][3];
		double[][] texT=new double[6][3];
		
		double sideLengthXY=Math.sqrt(Math.pow(mins.getX()-maxs.getX(), 2) + Math.pow(mins.getY()-maxs.getY(),2));
		Vector3D diffVec1 = new Vector3D(mins.getX(), mins.getY(), maxs.getZ()).subtract(mins);
		Vector3D diffVec2 = new Vector3D(maxs.getX(), maxs.getY(), mins.getZ()).subtract(mins);
		Vector3D cross = Vector3D.crossProduct(diffVec2, diffVec1);
		cross.normalize();
		
		//Vector3D mins = new Vector3D(16, 0, 0);
		//Vector3D maxs = new Vector3D(16, 16, 16);
		// Face
		planes[0][0]=new Vector3D(mins.getX(), mins.getY(), maxs.getZ());
		planes[0][1]=new Vector3D(maxs.getX(), maxs.getY(), mins.getZ());
		planes[0][2]=mins;
		texS[0][0]=(mins.getX()-maxs.getX())/sideLengthXY;
		texS[0][1]=(mins.getY()-maxs.getY())/sideLengthXY;
		texT[0][2]=1;
		// Far
		planes[1][0]=new Vector3D(mins.getX(), mins.getY(), maxs.getZ()).subtract(cross);
		planes[1][1]=mins.subtract(cross);
		planes[1][2]=new Vector3D(maxs.getX(), maxs.getY(), mins.getZ()).subtract(cross);
		texS[1][0]=texS[0][0];
		texS[1][1]=texS[0][1];
		texT[1][2]=-1;
		// Top
		planes[2][0]=new Vector3D(mins.getX(), mins.getY(), maxs.getZ());
		planes[2][1]=new Vector3D(mins.getX(), mins.getY(), maxs.getZ()).subtract(cross);
		planes[2][2]=maxs;
		texS[2][0]=1;
		texT[2][1]=1;
		// Bottom
		planes[3][0]=mins;
		planes[3][1]=new Vector3D(maxs.getX(), maxs.getY(), mins.getZ());
		planes[3][2]=new Vector3D(maxs.getX(), maxs.getY(), mins.getZ()).subtract(cross);
		texS[3][0]=1;
		texT[3][1]=1;
		// Left
		planes[4][0]=mins;
		planes[4][1]=mins.subtract(cross);
		planes[4][2]=new Vector3D(mins.getX(), mins.getY(), maxs.getZ());
		texS[4][0]=texS[0][1];
		texS[4][1]=texS[0][0];
		texT[4][2]=1;
		// Right
		planes[5][0]=maxs;
		planes[5][1]=maxs.subtract(cross);
		planes[5][2]=new Vector3D(maxs.getX(), maxs.getY(), mins.getZ());
		texS[5][0]=texS[0][1];
		texS[5][1]=texS[0][0];
		texT[5][2]=1;

		MAPBrushSide front=new MAPBrushSide(planes[0], texture, texS[0], 0, texT[0], 0, 0, 1, 1, 0, "wld_lightmap", 16, 0);
		newBrush.add(front);
		for(int i=1;i<6;i++) {
			MAPBrushSide currentEdge;
			if(toVMF) {
				currentEdge=new MAPBrushSide(planes[i], "tools/toolsnodraw", texS[i], 0, texT[i], 0, 0, 1, 1, 32, "wld_lightmap", 16, 0);
			} else {
				currentEdge=new MAPBrushSide(planes[i], "special/nodraw", texS[i], 0, texT[i], 0, 0, 1, 1, 32, "wld_lightmap", 16, 0);
			}
			newBrush.add(currentEdge);
		}
		
		return newBrush;
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
