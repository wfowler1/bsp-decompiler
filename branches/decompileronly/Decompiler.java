// Decompiler class

// Handles the actual decompilation.

import java.util.Date;

public class Decompiler {

	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	public static final int A = 0;
	public static final int B = 1;
	public static final int C = 2;
	
	public static final int X = 0;
	public static final int Y = 1;
	public static final int Z = 2;
	
	private boolean vertexDecomp;
	private boolean correctPlaneFlip;
	private double planePointCoef;
	private boolean toVMF;
	private boolean calcVerts;
	private boolean roundNums;
	
	private int numFlips=0;
	private int numFlipBrshs=0;
	
	private Entities mapFile; // Most MAP file formats (including GearCraft) are simply a bunch of nested entities
	private int numBrshs;
	private int numIDs; // Everything in a VMF file has a unique ID. Just keep counting up
	
	// Declare all kinds of BSPs here, the one actually used will be determined by constructor
	// private BSPv29n30
	private v38BSP BSP38;
	private v42BSP BSP42;
	private v46BSP BSP46;
	// private BSPv47
	// private MOHAABSP
	// private SourceBSPv20
	
	private int version; // The constructor will set this properly
	
	// CONSTRUCTORS
	
	// This constructor sets everything according to specified settings.
	public Decompiler(v38BSP BSP38, boolean vertexDecomp, boolean correctPlaneFlip, boolean calcVerts, boolean roundNums, boolean toVMF, double planePointCoef) {
		// Set up global variables
		this.BSP38=BSP38;
		version=38;
		this.vertexDecomp=vertexDecomp;
		this.correctPlaneFlip=correctPlaneFlip;
		this.planePointCoef=planePointCoef;
		this.toVMF=toVMF;
		this.calcVerts=calcVerts;
		this.roundNums=roundNums;
	}
	
	// This constructor sets everything according to specified settings.
	public Decompiler(v42BSP BSP42, boolean vertexDecomp, boolean correctPlaneFlip, boolean calcVerts, boolean roundNums, boolean toVMF, double planePointCoef) {
		// Set up global variables
		this.BSP42=BSP42;
		version=42;
		this.vertexDecomp=vertexDecomp;
		this.correctPlaneFlip=correctPlaneFlip;
		this.planePointCoef=planePointCoef;
		this.toVMF=toVMF;
		this.calcVerts=calcVerts;
		this.roundNums=roundNums;
	}
	
	// This constructor sets everything according to specified settings.
	public Decompiler(v46BSP BSP46, boolean vertexDecomp, boolean correctPlaneFlip, boolean calcVerts, boolean roundNums, boolean toVMF, double planePointCoef) {
		// Set up global variables
		this.BSP46=BSP46;
		version=46;
		this.vertexDecomp=vertexDecomp;
		this.correctPlaneFlip=correctPlaneFlip;
		this.planePointCoef=planePointCoef;
		this.toVMF=toVMF;
		this.calcVerts=calcVerts;
		this.roundNums=roundNums;
	}
	
	// METHODS
	
	// +decompile()
	// Starts the decompilation process. This is leftover from when multithreading
	// was handled through the decompiler.
	public void decompile() {
		Date begin=new Date();
		switch(version) {
			case 38:
				Window.window.println("Decompilation of Quake 2 map not yet written!");
				break;
			case 42:
				decompileBSP42();
				break;
			case 46:
				Window.window.println("Decompilation of Quake 3 map not yet written!");
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
	public void decompileBSP42() {
		// Begin by copying all the entities into another Lump00 object. This is
		// necessary because if I just modified the current entity list then it
		// could be saved back into the BSP and really mess some stuff up.
		mapFile=new Entities(BSP42.getEntities());
		int numTotalItems=0;
		// Then I need to go through each entity and see if it's brush-based.
		// Worldspawn is brush-based as well as any entity with model *#.
		for(int i=0;i<mapFile.getNumElements();i++) { // For each entity
			if(toVMF) {
				mapFile.getEntity(i).setAttribute("id", new Integer(++numIDs).toString());
			}
			int currentModel=-1;
			numBrshs=0; // Reset the brush count for each entity
			if(mapFile.getEntity(i).isBrushBased()) {
				currentModel=BSP42.getEntities().getEntity(i).getModelNumber();
			} else {
				if(mapFile.getEntity(i).getAttribute("classname").equalsIgnoreCase("worldspawn")) {
					currentModel=0; // If the entity is worldspawn, we're dealing with model 0, which is the world.
				}
			}
			
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
								decompileBrush42(BSP42.getBrushes().getBrush(BSP42.getMarkBrushes().getInt(firstBrushIndex+k)), i); // Decompile the brush
								numBrshs++;
								numTotalItems++;
								Window.setProgress(numTotalItems, BSP42.getBrushes().getNumElements()+BSP42.getEntities().getNumElements());
							}
						}
					}
				}
				mapFile.getEntity(i).deleteAttribute("model");
				// Recreate origin brushes for entities that need them
				// These are discarded on compile and replaced with an "origin" attribute in the entity.
				// I need to undo that. For this I will create a 32x32 brush, centered at the point defined
				// by the "origin" attribute.
				if(origin[0]!=0 || origin[1]!=0 || origin[2]!=0) { // If this brush uses the "origin" attribute
					MAPBrush newOriginBrush;
					newOriginBrush=new MAPBrush(numBrshs, ++numIDs, i, new double[3], 0);
					numBrshs++;
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
							currentEdge=new MAPBrushSide(planes[j], "special/origin", textureS[j], 0, textureT[j], 0, 0, 1, 1, 0, "wld_lightmap", 16, 0, ++numIDs);
						} else {
							currentEdge=new MAPBrushSide(planes[j], "special/origin", textureS[j], 0, textureT[j], 0, 0, 1, 1, 0, "wld_lightmap", 16, 0, ++numIDs);
						}
						newOriginBrush.add(currentEdge);
					}
					if(toVMF) {
						if(roundNums) {
							mapFile.getEntity(i).addAttributeInside(newOriginBrush.toRoundVMFBrush());
						} else {
							mapFile.getEntity(i).addAttributeInside(newOriginBrush.toVMFBrush());
						}
					} else {
						if(roundNums) {
							mapFile.getEntity(i).addAttributeInside(newOriginBrush.toRoundString());
						} else {
							mapFile.getEntity(i).addAttributeInside(newOriginBrush.toString());
						}
					}
				}
				mapFile.getEntity(i).deleteAttribute("origin");
			}
			numTotalItems++;
			Window.setProgress(numTotalItems, BSP42.getBrushes().getNumElements()+BSP42.getEntities().getNumElements());
			if(toVMF) { // correct some entities to make source ports easier
				if(mapFile.getEntity(i).getAttribute("classname").equalsIgnoreCase("light_spot")) {
					 mapFile.getEntity(i).setAttribute("pitch", new Double(mapFile.getEntity(i).getAngles()[0]).toString());
					 mapFile.getEntity(i).setAttribute("_inner_cone", mapFile.getEntity(i).getAttribute("_cone")); 
					 mapFile.getEntity(i).setAttribute("_cone", mapFile.getEntity(i).getAttribute("_cone2"));
					 mapFile.getEntity(i).deleteAttribute("_cone2");
				}
				if(mapFile.getEntity(i).getAttribute("classname").equalsIgnoreCase("func_wall")) {
					mapFile.getEntity(i).setAttribute("classname", "func_detail");
				}
				if(mapFile.getEntity(i).getAttribute("classname").equalsIgnoreCase("item_generic")) {
					mapFile.getEntity(i).setAttribute("classname", "prop_static");
				}
			}
		}
		if(!toVMF) {
			Window.window.println("Saving "+BSP42.getPath().substring(0, BSP42.getPath().length()-4)+".map...");
			mapFile.save(BSP42.getPath().substring(0, BSP42.getPath().length()-4)+".map");
		} else {
			Window.window.println("Saving "+BSP42.getPath().substring(0, BSP42.getPath().length()-4)+".vmf...");
			mapFile.save(BSP42.getPath().substring(0, BSP42.getPath().length()-4)+".vmf");
		}
		Window.window.println("Process completed!");
	}
	
	// -decompileBrush42(Brush, int)
	// Decompiles the Brush and adds it to entitiy #currentEntity as .MAP data.
	private void decompileBrush42(v42Brush brush, int currentEntity) {
		double[] origin=mapFile.getEntity(currentEntity).getOrigin();
		int firstSide=brush.getFirstSide();
		int numSides=brush.getNumSides();
		MAPBrushSide[] brushSides=new MAPBrushSide[numSides];
		MAPBrush mapBrush = new MAPBrush(numBrshs, ++numIDs, currentEntity, origin, planePointCoef);
		int numRealFaces=0;
		for(int l=0;l<numSides;l++) { // For each side of the brush
			Vector3D[] plane=new Vector3D[3]; // Three points define a plane. All I have to do is find three points on that plane.
			v42BrushSide currentSide=BSP42.getBrushSides().getBrushSide(firstSide+l);
			v42Face currentFace=BSP42.getFaces().getFace(currentSide.getFace()); // To find those three points, I can use vertices referenced by faces.
			if(!BSP42.getTextures().getString(currentFace.getTexture()).equalsIgnoreCase("special/bevel")) { // If this face uses special/bevel, skip the face completely
				int firstVertex=currentFace.getVert();
				int numVertices=currentFace.getNumVerts();
				Plane currentPlane=BSP42.getPlanes().getPlane(currentSide.getPlane()).getPlane();
				boolean pointsWorked=false;
				if(numVertices!=0 && vertexDecomp) { // If the face actually references a set of vertices
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
					                                   // we must find them ourselves using the A, B, C and D values.
					plane=GenericMethods.extrapPlanePoints(currentPlane, planePointCoef);
				}
				String texture=BSP42.getTextures().getString(currentFace.getTexture());
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
								if(texture.equalsIgnoreCase("special/playertrigger")) {
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
				String material=BSP42.getMaterials().getString(currentFace.getMaterial());
				double lgtScale=16; // These values are impossible to get from a compiled map since they
				double lgtRot=0;    // are used by RAD for generating lightmaps, then are discarded, I believe.
				brushSides[l]=new MAPBrushSide(plane, texture, textureS, textureShiftS, textureT, textureShiftT,
				                               texRot, texScaleS, texScaleT, flags, material, lgtScale, lgtRot, ++numIDs);
				numRealFaces++;
				if(brushSides[l]!=null) {
					if(pointsWorked) {
						mapBrush.add(brushSides[l], plane, currentPlane, true); // Add the MAPBrushSide to the current brush
					} else {
						mapBrush.add(brushSides[l], new Vector3D[0], currentPlane, false);
					}
				}
			}
		}
		
		if(correctPlaneFlip) {
			mapBrush.correctPlanes(); // If planar decompile, this will already calculate corners
			if(calcVerts && vertexDecomp) { // So, only allow this if vertex decompile.
				mapBrush.recalcCorners();
			}
		}
		
		if(toVMF) {
			if(roundNums) {
				mapFile.getEntity(currentEntity).addAttributeInside(mapBrush.toRoundVMFBrush());
			} else {
				mapFile.getEntity(currentEntity).addAttributeInside(mapBrush.toVMFBrush()); // This adds the brush we've been finding and creating to
			}                                                                              // the current entity as an attribute. The way I've coded
		} else {                                                                          // this whole program and the entities parser, this shouldn't
			if(roundNums) {                                                                // cause any issues at all.
				mapFile.getEntity(currentEntity).addAttributeInside(mapBrush.toRoundString());
			} else {
				mapFile.getEntity(currentEntity).addAttributeInside(mapBrush.toString());
			}
		}
	}
	
	// Attempt to turn the Quake 3 BSP into a .MAP file
	public void decompileBSP46() {
		mapFile=new Entities(BSP46.getEntities());
		int numTotalItems=0;
		for(int i=0;i<mapFile.getNumElements();i++) { // For each entity	
			
		}
	}
}
