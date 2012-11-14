// SourceBSPDecompiler class
// Decompile BSP v38

import java.util.Date;

public class SourceBSPDecompiler {

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
	
	private BSP BSPObject;
	
	// CONSTRUCTORS

	// This constructor sets everything according to specified settings.
	public SourceBSPDecompiler(BSP BSPObject, int jobnum) {
		// Set up global variables
		this.BSPObject=BSPObject;
		this.jobnum=jobnum;
	}
	
	// METHODS

	// Attempt to turn the BSP into a .MAP file
	public void decompile() throws java.io.IOException {
		Date begin=new Date();
		// Begin by copying all the entities into another Lump00 object. This is
		// necessary because if I just modified the current entity list then it
		// could be saved back into the BSP and really mess some stuff up.
		mapFile=new Entities(BSPObject.getEntities());
		//int numAreaPortals=0;
		int numTotalItems=0;
		for(int i=0;i<BSPObject.getEntities().length();i++) { // For each entity
			Window.println("Entity "+i+": "+mapFile.getEntity(i).getAttribute("classname"),Window.VERBOSITY_ENTITIES);
			// getModelNumber() returns 0 for worldspawn, the *# for brush based entities, and -1 for everything else
			int currentModel=mapFile.getEntity(i).getModelNumber();
			if(currentModel!=-1) { // If this is still -1 then it's strictly a point-based entity. Move on to the next one.
				double[] origin=mapFile.getEntity(i).getOrigin();
				Leaf[] leaves=BSPObject.getLeavesInModel(currentModel);
				int numLeaves=leaves.length;
				boolean[] brushesUsed=new boolean[BSPObject.getBrushes().length()]; // Keep a list of brushes already in the model, since sometimes the leaves lump references one brush several times
				numBrshs=0; // Reset the brush count for each entity
				for(int j=0;j<numLeaves;j++) { // For each leaf in the bunch
					Leaf currentLeaf=leaves[j];
					if(Window.visLeafBBoxesIsSelected()) {
					//	mapFile.getEntity(0).addBrush(GenericMethods.createBrush(currentLeaf.getMins(), currentLeaf.getMaxs(), "special/hint"));
					}
					int firstMarkBrushIndex=currentLeaf.getFirstMarkBrush();
					int numBrushIndices=currentLeaf.getNumMarkBrushes();
					if(numBrushIndices>0) { // A lot of leaves reference no brushes. If this is one, this iteration of the j loop is finished
						for(int k=0;k<numBrushIndices;k++) { // For each brush referenced
							long currentBrushIndex=BSPObject.getMarkBrushes().getElement(firstMarkBrushIndex+k);
							if(!brushesUsed[(int)currentBrushIndex]) { // If the current brush has NOT been used in this entity
								Window.print("Brush "+(k+numBrushIndices),Window.VERBOSITY_BRUSHCREATION);
								brushesUsed[(int)currentBrushIndex]=true;
								Brush brush=BSPObject.getBrushes().getElement((int)currentBrushIndex);
								decompileBrush(brush, i); // Decompile the brush
								numBrshs++;
								numTotalItems++;
								Window.setProgress(jobnum, numTotalItems, BSPObject.getBrushes().length()+BSPObject.getEntities().length(), "Decompiling...");
							}
						}
					}
				}
			}
			numTotalItems++; // This entity
			Window.setProgress(jobnum, numTotalItems, BSPObject.getBrushes().length()+BSPObject.getEntities().length(), "Decompiling...");
		}
		Window.setProgress(jobnum, numTotalItems, BSPObject.getBrushes().length()+BSPObject.getEntities().length(), "Saving...");
		MAPMaker.outputMaps(mapFile, BSPObject.getMapNameNoExtension(), BSPObject.getFolder(), BSPObject.getVersion());
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
		MAPBrushSide[] brushSides=new MAPBrushSide[numSides];
		boolean isDetail=false;
		if (currentEntity==0 && !Window.noDetailIsSelected() && (brush.getContents()[3] & ((byte)1 << 3)) != 0) {
			isDetail=true;
		}
		MAPBrush mapBrush = new MAPBrush(numBrshs, currentEntity, isDetail);
		if (currentEntity==0 && !Window.noWaterIsSelected() && (brush.getContents()[0] & ((byte)1 << 5)) != 0) {
			mapBrush.setWater(true);
		}
		Window.println(": "+numSides+" sides, detail: "+isDetail,Window.VERBOSITY_BRUSHCREATION);
		for(int i=0;i<numSides;i++) { // For each side of the brush
			BrushSide currentSide=BSPObject.getBrushSides().getElement(firstSide+i);
			if(currentSide.isBevel()==0) { // Bevel sides are evil
				Vector3D[] plane=new Vector3D[3]; // Three points define a plane. All I have to do is find three points on that plane.
				Plane currentPlane=BSPObject.getPlanes().getElement(currentSide.getPlane()); // To find those three points, I must extrapolate from planes until I find a way to associate faces with brushes
				boolean isDuplicate=false;/* TODO: We sure don't want duplicate planes (though this is already handled by the MAPBrush class). Make sure neither checked side is bevel.
				for(int j=i+1;j<numSides;j++) { // For each subsequent side of the brush
					if(currentPlane.equals(BSPObject.getPlanes().getPlane(BSPObject.getBrushSides().getElement(firstSide+j).getPlane()))) {
						Window.println("WARNING: Duplicate planes in a brush, sides "+i+" and "+j,Window.VERBOSITY_WARNINGS);
						isDuplicate=true;
					}
				}*/
				if(!isDuplicate) {
					TexInfo currentTexInfo;
					if(currentSide.getTexture()>-1) {
						currentTexInfo=BSPObject.getTexInfo().getElement(currentSide.getTexture());
					} else {
						Vector3D[] axes=GenericMethods.textureAxisFromPlane(currentPlane);
						currentTexInfo=new TexInfo(axes[0], 0, axes[1], 0, 0, BSPObject.findTexDataWithTexture("tools/toolsclip"));
					}
					SourceTexData currentTexData=BSPObject.getTexDatas().getElement(currentTexInfo.getTexture());
					/*if(!Window.planarDecompIsSelected()) {
						// Find a face whose plane and texture information corresponds to the current side
						// It doesn't really matter if it's the actual brush's face, just as long as it provides vertices.
						v38Face currentFace=null;
						boolean faceFound=false;
						for(int j=0;j<BSP38.getFaces().length();j++) {
							currentFace=BSP38.getFaces().getFace(j);
							if(currentFace.getPlane()==currentSide.getPlane() && currentFace.getTexInfo()==currentSide.getTexInfo() && currentFace.getNumEdges()>1) {
								faceFound=true;
								break;
							}
						}
						if(faceFound) {
							int markEdge=BSP38.getMarkEdges().getInt(currentFace.getFirstEdge());
							int currentMarkEdge=0;
							int firstVertex;
							int secondVertex;
							if(markEdge>0) {
								firstVertex=BSP38.getEdges().getEdge(markEdge).getFirstVertex();
								secondVertex=BSP38.getEdges().getEdge(markEdge).getSecondVertex();
							} else {
								firstVertex=BSP38.getEdges().getEdge(-markEdge).getSecondVertex();
								secondVertex=BSP38.getEdges().getEdge(-markEdge).getFirstVertex();
							}
							int numVertices=currentFace.getNumEdges()+1;
							boolean pointsWorked=false;
							plane[0]=new Vector3D(BSP38.getVertices().getVertex(firstVertex)); // Grab and store the first one
							plane[1]=new Vector3D(BSP38.getVertices().getVertex(secondVertex)); // The second should be unique from the first
							boolean second=false;
							if(plane[0].equals(plane[1])) { // If for some messed up reason they are the same
								for(currentMarkEdge=1;currentMarkEdge<currentFace.getNumEdges();currentMarkEdge++) { // For each edge after the first one
									markEdge=BSP38.getMarkEdges().getInt(currentFace.getFirstEdge()+currentMarkEdge);
									if(markEdge>0) {
										plane[1]=new Vector3D(BSP38.getVertices().getVertex(BSP38.getEdges().getEdge(markEdge).getFirstVertex()));
									} else {
										plane[1]=new Vector3D(BSP38.getVertices().getVertex(BSP38.getEdges().getEdge(-markEdge).getSecondVertex()));
									}
									if(!plane[0].equals(plane[1])) { // Make sure the point isn't the same as the first one
										second=false;
										break; // If it isn't the same, this point is good
									} else {
										if(markEdge>0) {
											plane[1]=new Vector3D(BSP38.getVertices().getVertex(BSP38.getEdges().getEdge(markEdge).getSecondVertex()));
										} else {
											plane[1]=new Vector3D(BSP38.getVertices().getVertex(BSP38.getEdges().getEdge(-markEdge).getFirstVertex()));
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
								markEdge=BSP38.getMarkEdges().getInt(currentFace.getFirstEdge()+currentMarkEdge);
								if(second) {
									if(markEdge>0) {
										plane[2]=new Vector3D(BSP38.getVertices().getVertex(BSP38.getEdges().getEdge(markEdge).getFirstVertex()));
									} else {
										plane[2]=new Vector3D(BSP38.getVertices().getVertex(BSP38.getEdges().getEdge(-markEdge).getSecondVertex()));
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
									plane[2]=new Vector3D(BSP38.getVertices().getVertex(BSP38.getEdges().getEdge(markEdge).getSecondVertex()));
								} else {
									plane[2]=new Vector3D(BSP38.getVertices().getVertex(BSP38.getEdges().getEdge(-markEdge).getFirstVertex()));
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
					//	plane=GenericMethods.extrapPlanePoints(currentPlane);
					// }
					String texture=BSPObject.getTextures().getTextureAtOffset((int)BSPObject.getTexTable().getElement(currentTexData.getStringTableIndex()));
					if(texture.substring(0,6).equalsIgnoreCase("tools/")) {
						// Tools textured faces do not maintain their own texture axes. Therefore, an arbitrary axis is
						// used in the compiled map. When decompiled, these axes might smear the texture on the face. Fix that.
						Vector3D[] axes=GenericMethods.textureAxisFromPlane(currentPlane);
						currentTexInfo=new TexInfo(axes[0], 0, axes[1], 0, 0, BSPObject.findTexDataWithTexture(texture));
					}
					double[] textureU=new double[3];
					double[] textureV=new double[3];
					// Get the lengths of the axis vectors
					double SAxisLength=Math.sqrt(Math.pow((double)currentTexInfo.getSAxis().getX(),2)+Math.pow((double)currentTexInfo.getSAxis().getY(),2)+Math.pow((double)currentTexInfo.getSAxis().getZ(),2));
					double TAxisLength=Math.sqrt(Math.pow((double)currentTexInfo.getTAxis().getX(),2)+Math.pow((double)currentTexInfo.getTAxis().getY(),2)+Math.pow((double)currentTexInfo.getTAxis().getZ(),2));
					// In compiled maps, shorter vectors=longer textures and vice versa. This will convert their lengths back to 1. We'll use the actual scale values for length.
					double texScaleU=(1/SAxisLength);// Let's use these values using the lengths of the U and V axes we found above.
					double texScaleV=(1/TAxisLength);
					textureU[0]=((double)currentTexInfo.getSAxis().getX()/SAxisLength);
					textureU[1]=((double)currentTexInfo.getSAxis().getY()/SAxisLength);
					textureU[2]=((double)currentTexInfo.getSAxis().getZ()/SAxisLength);
					double originShiftU=(((double)currentTexInfo.getSAxis().getX()/SAxisLength)*origin[X]+((double)currentTexInfo.getSAxis().getY()/SAxisLength)*origin[Y]+((double)currentTexInfo.getSAxis().getZ()/SAxisLength)*origin[Z])/texScaleU;
					double textureShiftU=(double)currentTexInfo.getSShift()-originShiftU;
					textureV[0]=((double)currentTexInfo.getTAxis().getX()/TAxisLength);
					textureV[1]=((double)currentTexInfo.getTAxis().getY()/TAxisLength);
					textureV[2]=((double)currentTexInfo.getTAxis().getZ()/TAxisLength);
					double originShiftV=(((double)currentTexInfo.getTAxis().getX()/TAxisLength)*origin[X]+((double)currentTexInfo.getTAxis().getY()/TAxisLength)*origin[Y]+((double)currentTexInfo.getTAxis().getZ()/TAxisLength)*origin[Z])/texScaleV;
					double textureShiftV=(double)currentTexInfo.getTShift()-originShiftV;
					float texRot=0; // In compiled maps this is calculated into the U and V axes, so set it to 0 until I can figure out a good way to determine a better value.
					int flags=0; // Set this to 0 until we can somehow associate faces with brushes
					String material="wld_lightmap"; // Since materials are a NightFire only thing, set this to a good default
					double lgtScale=16; // These values are impossible to get from a compiled map since they
					double lgtRot=0;    // are used by RAD for generating lightmaps, then are discarded, I believe.
					brushSides[i]=new MAPBrushSide(currentPlane, texture, textureU, textureShiftU, textureV, textureShiftV,
					                               texRot, texScaleU, texScaleV, flags, material, lgtScale, lgtRot);
					mapBrush.add(brushSides[i]);
				}
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
	
	public TexInfo createPerpTexInfo(Plane in) {
		Vector3D[] axes=GenericMethods.textureAxisFromPlane(in);
		return new TexInfo(axes[0], 0, axes[1], 0, 0, BSPObject.findTexDataWithTexture("tools/toolsclip"));
	} 
}
