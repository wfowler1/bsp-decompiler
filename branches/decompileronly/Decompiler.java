// Decompiler class

// Handles the actual decompilation.

public class Decompiler {

	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	private static Runtime r = Runtime.getRuntime();
	
	public static final int A = 0;
	public static final int B = 1;
	public static final int C = 2;
	
	public static final int X = 0;
	public static final int Y = 1;
	public static final int Z = 2;
	
	private BSPData BSP;
	private boolean vertexDecomp;
	private boolean checkVerts;
	private boolean facesOnly;
	private double planePointCoef;
	
	private Entities mapFile;
	private int numBrshs;
	
	// CONSTRUCTORS
	
	// This constructor controls the entire process.
	public Decompiler(BSPData BSP, boolean vertexDecomp, boolean checkVerts, boolean facesOnly, double planePointCoef) {
		// Set up global variables
		this.BSP=BSP;
		this.vertexDecomp=vertexDecomp;
		this.checkVerts=checkVerts;
		this.facesOnly=facesOnly;
		this.planePointCoef=planePointCoef;
		
		decompile();
	}
	
	// METHODS
	
	// +decompile()
	// Attempts to convert the BSP file back into a .MAP file.
	//
	// This is another one of the most complex things I've ever had to code. I've
	// never nested for loops four deep before.
	// Iterators:
	// i: Current entity in the list
	//  j: Current leaf, referenced in a list by the model referenced by the current entity
	//   k: Current brush, referenced in a list by the current leaf.
	//    l: Current side of the current brush.
	//     m: When attempting vertex decompilation, the current vertex.
	public void decompile() {
		// Begin by copying all the entities into another Lump00 object. This is
		// necessary because if I just modified the current entity list then it
		// could be saved back into the BSP and really mess some stuff up.
		mapFile=new Entities(BSP.getEntities());
		// Then I need to go through each entity and see if it's brush-based.
		// Worldspawn is brush-based as well as any entity with model *#.
		for(int i=0;i<mapFile.getNumElements();i++) { // For each entity
			int currentModel=-1;
			if(mapFile.getEntity(i).isBrushBased()) {
				currentModel=BSP.getEntities().getEntity(i).getModelNumber();
			} else {
				if(mapFile.getEntity(i).getAttribute("classname").equalsIgnoreCase("worldspawn")) {
					currentModel=0; // If the entity is worldspawn, we're dealing with model 0, which is the world.
				}
			}
			
			if(currentModel!=-1) { // If this is still -1 then it's strictly a point-based entity. Move on to the next one.
				double[] origin=mapFile.getEntity(i).getOrigin();
				Planes newPlanes=new Planes(BSP.getPlanes());
				int firstLeaf=BSP.getModels().getModel(currentModel).getLeaf();
				int numLeaves=BSP.getModels().getModel(currentModel).getNumLeafs();
				boolean[] brushesUsed=new boolean[BSP.getBrushes().getNumElements()]; // Keep a list of brushes already in the model, since sometimes the leaves lump references one brush several times
				numBrshs=0;
				for(int j=0;j<numLeaves;j++) { // For each leaf in the bunch
					int firstBrushIndex=BSP.getLeaves().getLeaf(j+firstLeaf).getMarkBrush();
					int numBrushIndices=BSP.getLeaves().getLeaf(j+firstLeaf).getNumMarkBrushes();
					if(numBrushIndices>0) { // A lot of leaves reference no brushes. If this is one, this iteration of the j loop is finished
						for(int k=0;k<numBrushIndices;k++) { // For each brush referenced
							if(!brushesUsed[BSP.getMarkBrushes().getInt(firstBrushIndex+k)]) {
								brushesUsed[BSP.getMarkBrushes().getInt(firstBrushIndex+k)]=true;
								Brush currentBrush=BSP.getBrushes().getBrush(BSP.getMarkBrushes().getInt(firstBrushIndex+k)); // Get a handle to the brush
								int firstSide=currentBrush.getFirstSide();
								int numSides=currentBrush.getNumSides();
								int numPlaneFacesThisBrsh=0;
								int numVertFacesThisBrsh=0;
								boolean[] vertFaces=new boolean[numSides]; // vertFaces[X] will be true if face X was defined by vertices
								MAPBrushSide[] brushSides=new MAPBrushSide[numSides];
								Entity mapBrush=new Entity("{ // Brush "+numBrshs);
								int numRealFaces=0;
								for(int l=0;l<numSides;l++) { // For each side of the brush
									VertexD[] plane=new VertexD[3]; // Three points define a plane. All I have to do is find three points on that plane.
									BrushSide currentSide=BSP.getBrushSides().getBrushSide(firstSide+l);
									Face currentFace=BSP.getFaces().getFace(currentSide.getFace()); // To find those three points, I can use vertices referenced by faces.
									if(!BSP.getTextures().getTexture(currentFace.getTexture()).equalsIgnoreCase("special/bevel")) { // If this face uses special/bevel, skip the face completely
										int firstVertex=currentFace.getVert();
										int numVertices=currentFace.getNumVerts();
										Plane currentPlane=newPlanes.getPlane(currentSide.getPlane());
										boolean pointsWorked=false;
										if(numVertices!=0 && vertexDecomp) { // If the face actually references a set of vertices
											plane[0]=new VertexD(BSP.getVertices().getVertex(firstVertex)); // Grab and store the first one
											int m=1;
											for(m=1;m<numVertices;m++) { // For each point after the first one
												plane[1]=new VertexD(BSP.getVertices().getVertex(firstVertex+m));
												if(!plane[0].equals(plane[1])) { // Make sure the point isn't the same as the first one
													break; // If it isn't the same, this point is good
												}
											}
											for(m=m+1;m<numVertices;m++) { // For each point after the previous one used
												plane[2]=new VertexD(BSP.getVertices().getVertex(firstVertex+m));
												if(!plane[2].equals(plane[0]) && !plane[2].equals(plane[1])) { // Make sure no point is equal to the third one
													if((crossProduct(plane[0].subtract(plane[1]), plane[0].subtract(plane[2])).getX()!=0) || // Make sure all
													   (crossProduct(plane[0].subtract(plane[1]), plane[0].subtract(plane[2])).getY()!=0) || // three points 
													   (crossProduct(plane[0].subtract(plane[1]), plane[0].subtract(plane[2])).getZ()!=0)) { // are not collinear
														numVertFacesThisBrsh++;
														vertFaces[l]=true;
														pointsWorked=true;
														break;
													}
												}
											}
											if(pointsWorked && checkVerts) { // If the process above worked, check to make sure the normal generated from the
											                                 // vertices is flipped the same as the one given by the plane. This is necessary
											                                 // because I'm not always using the first three vertices.
											                                 // However could cause problems with custom compiled maps, since they have wacky
											                                 // plane flips.
												VertexD vertCross = normalizedCrossProduct(plane[0].subtract(plane[2]), plane[0].subtract(plane[1]));
												VertexD normalAdd = vertCross.add(new VertexD(currentPlane.getA(), currentPlane.getB(), currentPlane.getC()));
												if(Math.sqrt(Math.pow(normalAdd.getX(), 2) + Math.pow(normalAdd.getY(), 2) + Math.pow(normalAdd.getZ(), 2)) < 1) {
													plane=flipPlane(plane);
												}
											}
										}
										if(numVertices==0 || !pointsWorked) { // Fallback to planar decompilation. Since there are no explicitly defined points anymore,
											                                   // we must find them ourselves using the A, B, C and D values.
											numPlaneFacesThisBrsh++;
											// Figure out if the plane is parallel to two of the axes. If so it can be reproduced easily
											if(currentPlane.getB()==0 && currentPlane.getC()==0) { // parallel to plane YZ
												plane[0]=new VertexD(currentPlane.getDist()/currentPlane.getA(), -1, 1);
												plane[1]=new VertexD(currentPlane.getDist()/currentPlane.getA(), 0, 0);
												plane[2]=new VertexD(currentPlane.getDist()/currentPlane.getA(), 1, 1);
												if(currentPlane.getA()>0) {
													plane=flipPlane(plane);
												}
											} else {
												if(currentPlane.getA()==0 && currentPlane.getC()==0) { // parallel to plane XZ
													plane[0]=new VertexD(1, currentPlane.getDist()/currentPlane.getB(), -1);
													plane[1]=new VertexD(0, currentPlane.getDist()/currentPlane.getB(), 0);
													plane[2]=new VertexD(1, currentPlane.getDist()/currentPlane.getB(), 1);
													if(currentPlane.getB()>0) {
														plane=flipPlane(plane);
													}
												} else {
													if(currentPlane.getA()==0 && currentPlane.getB()==0) { // parallel to plane XY
														plane[0]=new VertexD(-1, 1, currentPlane.getDist()/currentPlane.getC());
														plane[1]=new VertexD(0, 0, currentPlane.getDist()/currentPlane.getC());
														plane[2]=new VertexD(1, 1, currentPlane.getDist()/currentPlane.getC());
														if(currentPlane.getC()>0) {
															plane=flipPlane(plane);
														}
													} else { // If you reach this point the plane is not parallel to any two-axis plane.
														if(currentPlane.getA()==0) { // parallel to X axis
															plane[0]=new VertexD(-planePointCoef, planePointCoef*planePointCoef, -(planePointCoef*planePointCoef*(double)currentPlane.getB()-(double)currentPlane.getDist())/(double)currentPlane.getC());
															plane[1]=new VertexD(0, 0, (double)currentPlane.getDist()/(double)currentPlane.getC());
															plane[2]=new VertexD(planePointCoef, planePointCoef*planePointCoef, -(planePointCoef*planePointCoef*(double)currentPlane.getB()-(double)currentPlane.getDist())/(double)currentPlane.getC());
															if(currentPlane.getC()>0) {
																plane=flipPlane(plane);
															}
														} else {
															if(currentPlane.getB()==0) { // parallel to Y axis
																plane[0]=new VertexD(-(planePointCoef*planePointCoef*(double)currentPlane.getC()-(double)currentPlane.getDist())/(double)currentPlane.getA(), -planePointCoef, planePointCoef*planePointCoef);
																plane[1]=new VertexD((double)currentPlane.getDist()/(double)currentPlane.getA(), 0, 0);
																plane[2]=new VertexD(-(planePointCoef*planePointCoef*(double)currentPlane.getC()-(double)currentPlane.getDist())/(double)currentPlane.getA(), planePointCoef, planePointCoef*planePointCoef);
																if(currentPlane.getA()>0) {
																	plane=flipPlane(plane);
																}
															} else {
																if(currentPlane.getC()==0) { // parallel to Z axis
																	plane[0]=new VertexD(planePointCoef*planePointCoef, -(planePointCoef*planePointCoef*(double)currentPlane.getA()-(double)currentPlane.getDist())/(double)currentPlane.getB(), -planePointCoef);
																	plane[1]=new VertexD(0, (double)currentPlane.getDist()/(double)currentPlane.getB(), 0);
																	plane[2]=new VertexD(planePointCoef*planePointCoef, -(planePointCoef*planePointCoef*(double)currentPlane.getA()-(double)currentPlane.getDist())/(double)currentPlane.getB(), planePointCoef);
																	if(currentPlane.getB()>0) {
																		plane=flipPlane(plane);
																	}
																} else { // If you reach this point the plane is not parallel to any axis. Therefore, any two coordinates will give a third.
																	plane[0]=new VertexD(-planePointCoef, planePointCoef*planePointCoef, -(-planePointCoef*(double)currentPlane.getA()+planePointCoef*planePointCoef*(double)currentPlane.getB()-(double)currentPlane.getDist())/(double)currentPlane.getC());
																	plane[1]=new VertexD(0, 0, (double)currentPlane.getDist()/(double)currentPlane.getC());
																	plane[2]=new VertexD(planePointCoef, planePointCoef*planePointCoef, -(planePointCoef*(double)currentPlane.getA()+planePointCoef*planePointCoef*(double)currentPlane.getB()-(double)currentPlane.getDist())/(double)currentPlane.getC());
																	if(currentPlane.getC()>0) {
																		plane=flipPlane(plane);
																	}
																}
															}
														}
													}
												}
											}
										} // End plane stuff
										plane[0].setX(plane[0].getX()+origin[X]);
										plane[0].setY(plane[0].getY()+origin[Y]);
										plane[0].setZ(plane[0].getZ()+origin[Z]);
										plane[1].setX(plane[1].getX()+origin[X]);
										plane[1].setY(plane[1].getY()+origin[Y]);
										plane[1].setZ(plane[1].getZ()+origin[Z]);
										plane[2].setX(plane[2].getX()+origin[X]);
										plane[2].setY(plane[2].getY()+origin[Y]);
										plane[2].setZ(plane[2].getZ()+origin[Z]);
										String texture=BSP.getTextures().getTexture(currentFace.getTexture());
										double[] textureS=new double[3];
										double[] textureT=new double[3];
										TexMatrix currentTexMatrix=BSP.getTextureMatrices().getTexMatrix(currentFace.getTexStyle());
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
										String material=BSP.getMaterials().getMaterial(currentFace.getMaterial());
										double lgtScale=16; // These values are impossible to get from a compiled map since they
										double lgtRot=0;    // are used by RAD for generating lightmaps, then are discarded, I believe.
										try {
											brushSides[l]=new MAPBrushSide(plane, texture, textureS, textureShiftS, textureT, textureShiftT,
										                                          texRot, texScaleS, texScaleT, flags, material, lgtScale, lgtRot);
										} catch(InvalidMAPBrushSideException e) {
											Window.window.println("Error creating brush side "+l+" on brush "+k+" in leaf "+j+" in model "+i+", side not written.");
										}
										numRealFaces++;
									}
								}
								
								for(int l=0;l<numSides;l++) { // For each side, AFTER the entire MAPBrushSide list has been populated and plane flip is sorted out
									try {
										mapBrush.addAttribute(brushSides[l].toString()); // Add the MAPBrushSide to the current brush as an attribute
									} catch(java.lang.NullPointerException e) { // If the object was never created, because the face was special/bevel
										; // Do nothing, it doesn't matter
									}
								}
								mapBrush.addAttribute("}");
								mapFile.getEntity(i).addAttribute(mapBrush.toString()); // Remember entity i? It's the current entity. This
								                                                        // adds the brush we've been finding and creating to
								                                                        // entity i as an attribute. The way I've coded this
								                                                        // whole program and the entities parser, this shouldn't
							                                                           // cause any issues at all.
								
								numBrshs++;
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
					Entity newOriginBrush=new Entity("{ // Brush "+numBrshs);
					numBrshs++;
					VertexD[][] planes=new VertexD[6][3]; // Six planes for a cube brush, three vertices for each plane
					double[][] textureS=new double[6][3];
					double[][] textureT=new double[6][3];
					// The planes and their texture scales
					// I got these from an origin brush created by Gearcraft. Don't worry where these numbers came from, they work.
					// Top
					planes[0][0]=new VertexD(-16+origin[0], 16+origin[1], 16+origin[2]);
					planes[0][1]=new VertexD(16+origin[0], 16+origin[1], 16+origin[2]);
					planes[0][2]=new VertexD(16+origin[0], -16+origin[1], 16+origin[2]);
					textureS[0][0]=1;
					textureT[0][1]=-1;
					// Bottom
					planes[1][0]=new VertexD(-16+origin[0], -16+origin[1], -16+origin[2]);
					planes[1][1]=new VertexD(16+origin[0], -16+origin[1], -16+origin[2]);
					planes[1][2]=new VertexD(16+origin[0], 16+origin[1], -16+origin[2]);
					textureS[1][0]=1;
					textureT[1][1]=-1;
					// Left
					planes[2][0]=new VertexD(-16+origin[0], 16+origin[1], 16+origin[2]);
					planes[2][1]=new VertexD(-16+origin[0], -16+origin[1], 16+origin[2]);
					planes[2][2]=new VertexD(-16+origin[0], -16+origin[1], -16+origin[2]);
					textureS[2][1]=1;
					textureT[2][2]=-1;
					// Right
					planes[3][0]=new VertexD(16+origin[0], 16+origin[1], -16+origin[2]);
					planes[3][1]=new VertexD(16+origin[0], -16+origin[1], -16+origin[2]);
					planes[3][2]=new VertexD(16+origin[0], -16+origin[1], 16+origin[2]);
					textureS[3][1]=1;
					textureT[3][2]=-1;
					// Near
					planes[4][0]=new VertexD(16+origin[0], 16+origin[1], 16+origin[2]);
					planes[4][1]=new VertexD(-16+origin[0], 16+origin[1], 16+origin[2]);
					planes[4][2]=new VertexD(-16+origin[0], 16+origin[1], -16+origin[2]);
					textureS[4][0]=1;
					textureT[4][2]=-1;
					// Far
					planes[5][0]=new VertexD(16+origin[0], -16+origin[1], -16+origin[2]);
					planes[5][1]=new VertexD(-16+origin[0], -16+origin[1], -16+origin[2]);
					planes[5][2]=new VertexD(-16+origin[0], -16+origin[1], 16+origin[2]);
					textureS[5][0]=1;
					textureT[5][2]=-1;
					
					for(int j=0;j<6;j++) {
						try {
							MAPBrushSide currentEdge=new MAPBrushSide(planes[j], "special/origin", textureS[j], 0, textureT[j], 0, 0, 1, 1, 0, "wld_lightmap", 16, 0);
							newOriginBrush.addAttribute(currentEdge.toString());
						} catch(InvalidMAPBrushSideException e) {
							// This message will never be displayed.
							Window.window.println("Bad origin brush, there's something wrong with the code.");
						}
					}
					newOriginBrush.addAttribute("}");
					mapFile.getEntity(i).addAttribute(newOriginBrush.toString());
				}
				mapFile.getEntity(i).deleteAttribute("origin");
			}
		}
		Window.window.println("Saving "+BSP.getPath().substring(0, BSP.getPath().length()-1)+".map...");
		mapFile.save(BSP.getPath().substring(0, BSP.getPath().length()-1)+".map");
		Window.window.println("Process completed!");
		r.gc(); // Collect garbage, there will be a lot of it
	}
	
	// -flipPlane(VertexD[])
	// Takes a plane as an array of vertices and flips it over.
	private VertexD[] flipPlane(VertexD[] in) {
		VertexD[] out={in[0], in[2], in[1]};
		return out;
	}
	
	// +dotProduct()
	// Takes two Vertex objects which are read as vectors, then returns the dot product
	public static double dotProduct(VertexD first, VertexD second) {
		return (first.getX()*second.getX())+(first.getY()*second.getY())+(first.getZ()*second.getZ());
	}
	
	// +crossProduct()
	// Takes two Vertex objects which are read as vectors, then returns their cross product
	public static VertexD crossProduct(VertexD first, VertexD second) {
		return new VertexD((first.getY()*second.getZ())-(first.getZ()*second.getY()),
		                   (first.getZ()*second.getX())-(first.getX()*second.getZ()),
								 (first.getX()*second.getY())-(first.getY()*second.getX()));
	}
	
	// +normalizedCrossProduct()
	// Takes two Vertex objects which are read as vectors, then returns their normalized cross product.
	// "normalized" means the length of the cross will be 1.
	public static VertexD normalizedCrossProduct(VertexD first, VertexD second) {
		VertexD result = crossProduct(first, second);
		double len = Math.sqrt((Math.pow(result.getX(), 2) + Math.pow(result.getY(), 2) + Math.pow(result.getZ(), 2)));
		return new VertexD(result.getX()/len, result.getY()/len, result.getZ()/len);
	}
}