// SiNBSPDecompiler class
// Decompile RBSP v1 for SiN
// Not to be confused with RBSP v1 for SoF2 or JO.

import java.util.Date;

public class SiNBSPDecompiler {

	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	public static final int A = 0;
	public static final int B = 1;
	public static final int C = 2;
	
	public static final int X = 0;
	public static final int Y = 1;
	public static final int Z = 2;
	
	private int jobnum;
	
	private Entities mapFile; // Most MAP file formats (including GearCraft) are simply a bunch of nested entities
	private int numBrshs;
	private int numSimpleCorrects=0;
	private int numAdvancedCorrects=0;
	private int numGoodBrushes=0;
	
	private SiNBSP BSP;
	
	// CONSTRUCTORS

	// This constructor sets everything according to specified settings.
	public SiNBSPDecompiler(SiNBSP BSP, int jobnum) {
		// Set up global variables
		this.BSP=BSP;
		this.jobnum=jobnum;
	}
	
	// METHODS

	// Attempt to turn the Quake 2 BSP into a .MAP file
	public void decompile() throws java.io.IOException {
		Date begin=new Date();
		// Begin by copying all the entities into another Lump00 object. This is
		// necessary because if I just modified the current entity list then it
		// could be saved back into the BSP and really mess some stuff up.
		mapFile=new Entities(BSP.getEntities());
		//int numAreaPortals=0;
		int numTotalItems=0;
		boolean containsAreaPortals=false;
		for(int i=0;i<BSP.getEntities().length();i++) { // For each entity
			Window.println("Entity "+i+": "+mapFile.getEntity(i).getAttribute("classname"),Window.VERBOSITY_ENTITIES);
			// Deal with area portals.
			if(mapFile.getEntity(i).getAttribute("classname").equalsIgnoreCase("func_areaportal")) {
				mapFile.getEntity(i).deleteAttribute("style");
				containsAreaPortals=true;
			}
			// getModelNumber() returns 0 for worldspawn, the *# for brush based entities, and -1 for everything else
			int currentModel=mapFile.getEntity(i).getModelNumber();
			
			if(currentModel!=-1) { // If this is still -1 then it's strictly a point-based entity. Move on to the next one.
				double[] origin=mapFile.getEntity(i).getOrigin();
				v38Leaf[] leaves=BSP.getLeavesInModel(currentModel);
				int numLeaves=leaves.length;
				boolean[] brushesUsed=new boolean[BSP.getBrushes().length()]; // Keep a list of brushes already in the model, since sometimes the leaves lump references one brush several times
				numBrshs=0; // Reset the brush count for each entity
				for(int j=0;j<numLeaves;j++) { // For each leaf in the bunch
					v38Leaf currentLeaf=leaves[j];
					if(Window.visLeafBBoxesIsSelected()) {
						mapFile.getEntity(0).addBrush(GenericMethods.createBrush(currentLeaf.getMins(), currentLeaf.getMaxs(), "special/hint"));
					}
					short firstBrushIndex=currentLeaf.getFirstMarkBrush();
					short numBrushIndices=currentLeaf.getNumMarkBrushes();
					if(numBrushIndices>0) { // A lot of leaves reference no brushes. If this is one, this iteration of the j loop is finished
						for(int k=0;k<numBrushIndices;k++) { // For each brush referenced
							if(!brushesUsed[BSP.getMarkBrushes().getShort(firstBrushIndex+k)]) { // If the current brush has NOT been used in this entity
								Window.print("Brush "+(k+numBrushIndices),Window.VERBOSITY_BRUSHCREATION);
								brushesUsed[BSP.getMarkBrushes().getShort(firstBrushIndex+k)]=true;
								Brush brush=BSP.getBrushes().getBrush(BSP.getMarkBrushes().getShort(firstBrushIndex+k));
								if(!(brush.getAttributes()[1]==-128)) {
									decompileBrush(brush, i); // Decompile the brush
								} else {
									containsAreaPortals=true;
								}
								numBrshs++;
								numTotalItems++;
								Window.setProgress(jobnum, numTotalItems, BSP.getBrushes().length()+BSP.getEntities().length(), "Decompiling...");
							}
						}
					}
				}
			}
			numTotalItems++; // This entity
			Window.setProgress(jobnum, numTotalItems, BSP.getBrushes().length()+BSP.getEntities().length(), "Decompiling...");
		}
		if(containsAreaPortals) { // If this map was found to have area portals
			int j=0;
			for(int i=0;i<BSP.getBrushes().length();i++) { // For each brush in this map
				if(BSP.getBrushes().getBrush(i).getAttributes()[1]==-128) { // If the brush is an area portal brush
					for(j++;j<BSP.getEntities().length();j++) { // Find an areaportal entity
						if(BSP.getEntities().getEntity(j).getAttribute("classname").equalsIgnoreCase("func_areaportal")) {
							decompileBrush(BSP.getBrushes().getBrush(i), j); // Add the brush to that entity
							break; // And break out of the inner loop, but remember your place.
						}
					}
					if(j==BSP.getEntities().length()) { // If we're out of entities, stop this whole thing.
						break;
					}
				}
			}
		}
		Window.setProgress(jobnum, numTotalItems, BSP.getBrushes().length()+BSP.getEntities().length(), "Saving...");
		MAPMaker.outputMaps(mapFile, BSP.getMapNameNoExtension(), BSP.getFolder(), BSP.VERSION);
		Window.println("Process completed!",Window.VERBOSITY_ALWAYS);
		if(!Window.skipFlipIsSelected()) {
			Window.println("Num simple corrected brushes: "+numSimpleCorrects,Window.VERBOSITY_MAPSTATS); 
			Window.println("Num advanced corrected brushes: "+numAdvancedCorrects,Window.VERBOSITY_MAPSTATS); 
			Window.println("Num good brushes: "+numGoodBrushes,Window.VERBOSITY_MAPSTATS); 
		}
		Date end=new Date();
		Window.window.println("Time taken: "+(end.getTime()-begin.getTime())+"ms"+(char)0x0D+(char)0x0A,Window.VERBOSITY_ALWAYS);
	}

	// -decompileBrush38(Brush, int, boolean)
	// Decompiles the Brush and adds it to entitiy #currentEntity as .MAP data.
	private void decompileBrush(Brush brush, int currentEntity) {
		double[] origin=mapFile.getEntity(currentEntity).getOrigin();
		int firstSide=brush.getFirstSide();
		int numSides=brush.getNumSides();
		boolean isDetail=false;
		MAPBrushSide[] brushSides=new MAPBrushSide[numSides];
		if(!Window.noDetailIsSelected() && (brush.getAttributes()[3] & ((byte)1 << 3)) != 0) { // According to Q2's source, this is the detail flag
			isDetail=true;
		}
		MAPBrush mapBrush = new MAPBrush(numBrshs, currentEntity, isDetail);
		Window.println(": "+numSides+" sides",Window.VERBOSITY_BRUSHCREATION);
		if(!Window.noWaterIsSelected() && (brush.getAttributes()[0] & ((byte)1 << 5)) != 0) {
			mapBrush.setWater(true);
		}
		for(int i=0;i<numSides;i++) { // For each side of the brush
			Vector3D[] plane=new Vector3D[3]; // Three points define a plane. All I have to do is find three points on that plane.
			SiNBrushSide currentSide=BSP.getSBrushSides().getElement(firstSide+i);
			Plane currentPlane=BSP.getPlanes().getPlane(currentSide.getPlane()); // To find those three points, I must extrapolate from planes until I find a way to associate faces with brushes
			Texture currentTexture;
			boolean isDuplicate=false;
			for(int j=i+1;j<numSides;j++) { // For each subsequent side of the brush
				if(currentPlane.equals(BSP.getPlanes().getPlane(BSP.getSBrushSides().getElement(firstSide+j).getPlane()))) {
					Window.println("WARNING: Duplicate planes in a brush, sides "+i+" and "+j,Window.VERBOSITY_WARNINGS);
					isDuplicate=true;
				}
			}
			if(!isDuplicate) {
				/*
				if(!Window.planarDecompIsSelected()) {
					// Find a face whose plane and texture information corresponds to the current side
					// It doesn't really matter if it's the actual brush's face, just as long as it provides vertices.
					SiNFace currentFace=null;
					boolean faceFound=false;
					for(int j=0;j<BSP.getSFaces().length();j++) {
						currentFace=BSP.getSFaces().getFace(j);
						if(currentFace.getPlane()==currentSide.getPlane() && currentFace.getTexInfo()==currentSide.getTexInfo() && currentFace.getNumEdges()>1) {
							faceFound=true;
							break;
						}
					}
					if(faceFound) {
						int markEdge=BSP.getMarkEdges().getInt(currentFace.getFirstEdge());
						int currentMarkEdge=0;
						int firstVertex;
						int secondVertex;
						if(markEdge>0) {
							firstVertex=BSP.getEdges().getEdge(markEdge).getFirstVertex();
							secondVertex=BSP.getEdges().getEdge(markEdge).getSecondVertex();
						} else {
							firstVertex=BSP.getEdges().getEdge(-markEdge).getSecondVertex();
							secondVertex=BSP.getEdges().getEdge(-markEdge).getFirstVertex();
						}
						int numVertices=currentFace.getNumEdges()+1;
						boolean pointsWorked=false;
						plane[0]=new Vector3D(BSP.getVertices().getVertex(firstVertex)); // Grab and store the first one
						plane[1]=new Vector3D(BSP.getVertices().getVertex(secondVertex)); // The second should be unique from the first
						boolean second=false;
						if(plane[0].equals(plane[1])) { // If for some messed up reason they are the same
							for(currentMarkEdge=1;currentMarkEdge<currentFace.getNumEdges();currentMarkEdge++) { // For each edge after the first one
								markEdge=BSP.getMarkEdges().getInt(currentFace.getFirstEdge()+currentMarkEdge);
								if(markEdge>0) {
									plane[1]=new Vector3D(BSP.getVertices().getVertex(BSP.getEdges().getEdge(markEdge).getFirstVertex()));
								} else {
									plane[1]=new Vector3D(BSP.getVertices().getVertex(BSP.getEdges().getEdge(-markEdge).getSecondVertex()));
								}
								if(!plane[0].equals(plane[1])) { // Make sure the point isn't the same as the first one
									second=false;
									break; // If it isn't the same, this point is good
								} else {
									if(markEdge>0) {
										plane[1]=new Vector3D(BSP.getVertices().getVertex(BSP.getEdges().getEdge(markEdge).getSecondVertex()));
									} else {
										plane[1]=new Vector3D(BSP.getVertices().getVertex(BSP.getEdges().getEdge(-markEdge).getFirstVertex()));
									}
									if(!plane[0].equals(plane[1])) {
										second=true;
										break;
									}
								}
							}
						}
						if(second) {
							currentMarkEdge++;
						}
						for(;currentMarkEdge<currentFace.getNumEdges();currentMarkEdge++) {
							markEdge=BSP.getMarkEdges().getInt(currentFace.getFirstEdge()+currentMarkEdge);
							if(second) {
								if(markEdge>0) {
									plane[2]=new Vector3D(BSP.getVertices().getVertex(BSP.getEdges().getEdge(markEdge).getFirstVertex()));
								} else {
									plane[2]=new Vector3D(BSP.getVertices().getVertex(BSP.getEdges().getEdge(-markEdge).getSecondVertex()));
								}
								if(!plane[2].equals(plane[0]) && !plane[2].equals(plane[1])) { // Make sure no point is equal to the third one
									if((Vector3D.crossProduct(plane[0].subtract(plane[1]), plane[0].subtract(plane[2])).getX()!=0) || // Make sure all
									   (Vector3D.crossProduct(plane[0].subtract(plane[1]), plane[0].subtract(plane[2])).getY()!=0) || // three points 
									   (Vector3D.crossProduct(plane[0].subtract(plane[1]), plane[0].subtract(plane[2])).getZ()!=0)) { // are not collinear
										pointsWorked=true;
										break;
									}
								}
							}
							// if we get to here, the first vertex of the edge failed, or was already used
							if(markEdge>0) { // use the second vertex
								plane[2]=new Vector3D(BSP.getVertices().getVertex(BSP.getEdges().getEdge(markEdge).getSecondVertex()));
							} else {
								plane[2]=new Vector3D(BSP.getVertices().getVertex(BSP.getEdges().getEdge(-markEdge).getFirstVertex()));
							}
							if(!plane[2].equals(plane[0]) && !plane[2].equals(plane[1])) { // Make sure no point is equal to the third one
								if((Vector3D.crossProduct(plane[0].subtract(plane[1]), plane[0].subtract(plane[2])).getX()!=0) || // Make sure all
								   (Vector3D.crossProduct(plane[0].subtract(plane[1]), plane[0].subtract(plane[2])).getY()!=0) || // three points 
								   (Vector3D.crossProduct(plane[0].subtract(plane[1]), plane[0].subtract(plane[2])).getZ()!=0)) { // are not collinear
									pointsWorked=true;
									break;
								}
							}
							// If we get here, neither point worked and we need to try the next edge.
							second=true;
						}
						if(!pointsWorked) {
							plane=GenericMethods.extrapPlanePoints(currentPlane);
						}
					} else { // Face not found
						plane=GenericMethods.extrapPlanePoints(currentPlane);
					}
				} else { // Planar decomp only */
					plane=GenericMethods.extrapPlanePoints(currentPlane);
				// }
				String texture="special/clip";
				double[] textureS=new double[3];
				double[] textureT=new double[3];
				double UShift=0;
				double VShift=0;
				double texScaleS=1;
				double texScaleT=1;
				if(currentSide.getTexInfo()>-1) {
					currentTexture=BSP.getTextures().getElement(currentSide.getTexInfo());
					if((currentTexture.getFlags()[0] & ((byte)1 << 2)) != 0) {
						texture="special/sky";
					} else {
						if((currentTexture.getFlags()[1] & ((byte)1 << 1)) != 0) {
							texture="special/skip";
						} else {
							if((currentTexture.getFlags()[1] & ((byte)1 << 0)) != 0) {
								if(currentEntity==0) {
									texture="special/hint"; // Hint was not used the same way in Quake 2 as other games.
								} else {                   // For example, a Hint brush CAN be used for a trigger in Q2 and is used as such a lot.
									texture="special/trigger";
								}
							} else {
								texture=currentTexture.getName();
							}
						}
					}
					// Get the lengths of the axis vectors
					double UAxisLength=Math.sqrt(Math.pow((double)currentTexture.getTexAxes().getUAxisX(),2)+Math.pow((double)currentTexture.getTexAxes().getUAxisY(),2)+Math.pow((double)currentTexture.getTexAxes().getUAxisZ(),2));
					double VAxisLength=Math.sqrt(Math.pow((double)currentTexture.getTexAxes().getVAxisX(),2)+Math.pow((double)currentTexture.getTexAxes().getVAxisY(),2)+Math.pow((double)currentTexture.getTexAxes().getVAxisZ(),2));
					// In compiled maps, shorter vectors=longer textures and vice versa. This will convert their lengths back to 1. We'll use the actual scale values for length.
					texScaleS=(1/UAxisLength);// Let's use these values using the lengths of the U and V axes we found above.
					texScaleT=(1/VAxisLength);
					textureS[0]=((double)currentTexture.getTexAxes().getUAxisX()/UAxisLength);
					textureS[1]=((double)currentTexture.getTexAxes().getUAxisY()/UAxisLength);
					textureS[2]=((double)currentTexture.getTexAxes().getUAxisZ()/UAxisLength);
					textureT[0]=((double)currentTexture.getTexAxes().getVAxisX()/VAxisLength);
					textureT[1]=((double)currentTexture.getTexAxes().getVAxisY()/VAxisLength);
					textureT[2]=((double)currentTexture.getTexAxes().getVAxisZ()/VAxisLength);
					UShift=(double)currentTexture.getTexAxes().getUShift();
					VShift=(double)currentTexture.getTexAxes().getVShift();
				} else {
					Vector3D[] axes=BSP46Decompiler.textureAxisFromPlane(currentPlane);
					textureS=axes[0].getPoint();
					textureT=axes[1].getPoint();
				}
				double originShiftS=(textureS[0]*origin[X]+textureS[1]*origin[Y]+textureS[2]*origin[Z])/texScaleS;
				double textureShiftS=UShift-originShiftS;
				double originShiftT=(textureT[0]*origin[X]+textureT[1]*origin[Y]+textureT[2]*origin[Z])/texScaleT;
				double textureShiftT=VShift-originShiftT;
				float texRot=0; // In compiled maps this is calculated into the U and V axes, so set it to 0 until I can figure out a good way to determine a better value.
				int flags=0; // Set this to 0 until we can somehow associate faces with brushes
				String material="wld_lightmap"; // Since materials are a NightFire only thing, set this to a good default
				double lgtScale=16; // These values are impossible to get from a compiled map since they
				double lgtRot=0;    // are used by RAD for generating lightmaps, then are discarded, I believe.
				brushSides[i]=new MAPBrushSide(plane, texture, textureS, textureShiftS, textureT, textureShiftT,
				                               texRot, texScaleS, texScaleT, flags, material, lgtScale, lgtRot);
				mapBrush.add(brushSides[i]);
			}
		}
		
		if(!Window.skipFlipIsSelected()) {
			if(mapBrush.hasBadSide()) { // If there's a side that might be backward
				if(mapBrush.hasGoodSide()) { // If there's a side that is forward
					mapBrush=GenericMethods.SimpleCorrectPlanes(mapBrush);
					numSimpleCorrects++;
					if(Window.calcVertsIsSelected()) { // This is performed in advancedcorrect, so don't use it if that's happening
						try {
							mapBrush=GenericMethods.CalcBrushVertices(mapBrush);
						} catch(java.lang.NullPointerException e) {
							Window.println("WARNING: Brush vertex calculation failed on entity "+mapBrush.getEntnum()+" brush "+mapBrush.getBrushnum()+"",Window.VERBOSITY_WARNINGS);
						}
					}
				} else { // If no forward side exists
					try {
						mapBrush=GenericMethods.AdvancedCorrectPlanes(mapBrush);
						numAdvancedCorrects++;
					} catch(java.lang.ArithmeticException e) {
						Window.println("WARNING: Plane correct returned 0 triangles for entity "+mapBrush.getEntnum()+" brush "+mapBrush.getBrushnum()+"",Window.VERBOSITY_WARNINGS);
					}
				}
			} else {
				numGoodBrushes++;
			}
		} else {
			if(Window.calcVertsIsSelected()) { // This is performed in advancedcorrect, so don't use it if that's happening
				try {
					mapBrush=GenericMethods.CalcBrushVertices(mapBrush);
				} catch(java.lang.NullPointerException e) {
					Window.println("WARNING: Brush vertex calculation failed on entity "+mapBrush.getEntnum()+" brush "+mapBrush.getBrushnum()+"",Window.VERBOSITY_WARNINGS);
				}
			}
		}
		
		// This adds the brush we've been finding and creating to
		// the current entity as an attribute. The way I've coded
		// this whole program and the entities parser, this shouldn't
		// cause any issues at all.
		if(Window.brushesToWorldIsSelected()) {
			mapBrush.setWater(false);
			mapFile.getEntity(0).addBrush(mapBrush);
		} else {
			mapFile.getEntity(currentEntity).addBrush(mapBrush);
		}
	}
}
