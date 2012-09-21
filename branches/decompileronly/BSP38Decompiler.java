// BSP38Decompiler class
// Decompile BSP v38

import java.util.Date;

public class BSP38Decompiler {

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
	
	private v38BSP BSP38;
	
	// CONSTRUCTORS

	// This constructor sets everything according to specified settings.
	public BSP38Decompiler(v38BSP BSP38, int jobnum) {
		// Set up global variables
		this.BSP38=BSP38;
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
		for(int i=0;i<BSP38.getEntities().length();i++) { // For each entity
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
				v38Leaf[] leaves=BSP38.getLeavesInModel(currentModel);
				int numLeaves=leaves.length;
				boolean[] brushesUsed=new boolean[BSP38.getBrushes().length()]; // Keep a list of brushes already in the model, since sometimes the leaves lump references one brush several times
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
							if(!brushesUsed[BSP38.getMarkBrushes().getShort(firstBrushIndex+k)]) { // If the current brush has NOT been used in this entity
								Window.print("Brush "+(k+numBrushIndices),Window.VERBOSITY_BRUSHCREATION);
								brushesUsed[BSP38.getMarkBrushes().getShort(firstBrushIndex+k)]=true;
								Brush brush=BSP38.getBrushes().getBrush(BSP38.getMarkBrushes().getShort(firstBrushIndex+k));
								if(!(brush.getAttributes()[1]==-128)) {
									decompileBrush(brush, i); // Decompile the brush
								} else {
									containsAreaPortals=true;
								}
								numBrshs++;
								numTotalItems++;
								Window.setProgress(jobnum, numTotalItems, BSP38.getBrushes().length()+BSP38.getEntities().length(), "Decompiling...");
							}
						}
					}
				}
			}
			numTotalItems++; // This entity
			Window.setProgress(jobnum, numTotalItems, BSP38.getBrushes().length()+BSP38.getEntities().length(), "Decompiling...");
		}
		if(containsAreaPortals) { // If this map was found to have area portals
			int j=0;
			for(int i=0;i<BSP38.getBrushes().length();i++) { // For each brush in this map
				if(BSP38.getBrushes().getBrush(i).getAttributes()[1]==-128) { // If the brush is an area portal brush
					for(j++;j<BSP38.getEntities().length();j++) { // Find an areaportal entity
						if(BSP38.getEntities().getEntity(j).getAttribute("classname").equalsIgnoreCase("func_areaportal")) {
							decompileBrush(BSP38.getBrushes().getBrush(i), j); // Add the brush to that entity
							break; // And break out of the inner loop, but remember your place.
						}
					}
					if(j==BSP38.getEntities().length()) { // If we're out of entities, stop this whole thing.
						break;
					}
				}
			}
		}
		Window.setProgress(jobnum, numTotalItems, BSP38.getBrushes().length()+BSP38.getEntities().length(), "Saving...");
		MAPMaker.outputMaps(mapFile, BSP38.getMapName(), BSP38.getPath(), BSP38.VERSION);
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
		MAPBrush mapBrush = new MAPBrush(numBrshs, currentEntity, false);
		Window.println(": "+numSides+" sides",Window.VERBOSITY_BRUSHCREATION);
		if((brush.getAttributes()[0] & ((byte)1 << 5)) != 0) {
			mapBrush.setWater(true);
		}
		for(int i=0;i<numSides;i++) { // For each side of the brush
			Vector3D[] plane=new Vector3D[3]; // Three points define a plane. All I have to do is find three points on that plane.
			v38BrushSide currentSide=BSP38.getBrushSides().getBrushSide(firstSide+i);
			Plane currentPlane=BSP38.getPlanes().getPlane(currentSide.getPlane()); // To find those three points, I must extrapolate from planes until I find a way to associate faces with brushes
			v38Texture currentTexture;
			boolean isDuplicate=false;
			for(int j=i+1;j<numSides;j++) { // For each subsequent side of the brush
				if(currentPlane.equals(BSP38.getPlanes().getPlane(BSP38.getBrushSides().getBrushSide(firstSide+j).getPlane()))) {
					Window.println("WARNING: Duplicate planes in a brush, sides "+i+" and "+j,Window.VERBOSITY_WARNINGS);
					isDuplicate=true;
				}
			}
			if(!isDuplicate) {
				if(currentSide.getTexInfo()>-1) {
					currentTexture=BSP38.getTextures().getTexture(currentSide.getTexInfo());
				} else {
					currentTexture=createPerpTexture38(currentPlane); // Create a texture plane perpendicular to current plane's normal
				}/*
				if(!Window.planarDecompIsSelected()) {
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
					plane=GenericMethods.extrapPlanePoints(currentPlane);
				// }
				String texture=currentTexture.getTexture();
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
