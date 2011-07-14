// Decompiler class
// This class gathers all relevant information from the lumps, and attempts
// to recreate the source MAP file of the BSP as accurately as possible.

// Also sets up and runs the GUI through which the actions of this class are
// controlled. It doesn't need to be very complicated.

import java.io.File;
import java.awt.*;
import javax.swing.*;

public class Decompiler {

	protected static Window window;

	// main method
	// Launches and sets up the GUI to create an Object of this class
	public static void main(String[] args) {
		
		UIManager myUI=new UIManager();
		try {
			myUI.setLookAndFeel(myUI.getSystemLookAndFeelClassName());
		} catch(java.lang.Exception e) {
			;
		}
		
		JFrame frame = new JFrame("BSP v42 Decompiler by 005");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		window = new Window(frame.getContentPane());

		frame.setPreferredSize(new Dimension(450, 400));

		frame.pack();
		frame.setResizable(false);
		frame.setVisible(true);
	}

	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	Runtime r = Runtime.getRuntime(); // Get a runtime object. This is for calling
	                                  // Java's garbage collector and does not need
												 // to be ported. I try not to leave memory leaks
												 // but since Java has no way explicitly reallocate
												 // unused memory I have to tell it when a good
												 // time is to run the garbage collector, by
												 // calling gc(). Also, it is used to execute EXEs
												 // from within the program by calling .exec(path).

	// All lumps will be in the same folder. This String IS that folder.
	private String filepath;
	
	// Each lump has its own class for handling its specific data structures.
	// These are the only lumps we need for decompilation.
	private Lump00 myL0;
	private Lump01 myL1;
	private Lump02 myL2;
	private Lump03 myL3;
	private Lump04 myL4;
	private Lump09 myL9;
	private Lump11 myL11;
	private Lump13 myL13;
	private Lump14 myL14;
	private Lump15 myL15;
	private Lump16 myL16;
	private Lump17 myL17;
	
	public final int NUMLUMPS=18;
	
	// This just allows us to reference the lump by name rather than index.
	public final int ENTITIES=0;
	public final int PLANES=1;
	public final int TEXTURES=2;
	public final int MATERIALS=3;
	public final int VERTICES=4;
	public final int INDICES=6;
	public final int FACES=9;
	public final int LEAVES=11;
	public final int MARKBRUSHES=13;
	public final int MODELS=14;
	public final int BRUSHES=15;
	public final int BRUSHSIDES=16;
	public final int TEXMATRIX=17;
	
	// Allows us to reference the X Y or Z components of a vector by their letter
	public final int X=0;
	public final int Y=1;
	public final int Z=2;
	
	// This allows us to get the name of the lump using its index.
	public static final String[] LUMPNAMES = {"Entities", "Planes", "Textures", "Materials", "Vertices", "Normals", "Indices", "Visibility", "Nodes", "Faces",
	                             "Lighting", "Leaves", "Mark Surfaces", "Mark Brushes", "Models", "Brushes", "Brushsides", "Texmatrix"};
		
	// This holds the size of the data structures for each lump. If
	// the lump does not have a set size, the size is -1. If the lump's
	// size has not been determined yet, the size is 0.
	// The entities lump does not have a set data length per entity
	// Since there is literally no data in lump05, there is no data structure
	// Visibility varies for every map, and will be determined by constructor
	public int[] lumpDataSizes = {-1, 20, 64, 64, 12, -1, 4, 0, 36, 48, 3, 48, 4, 4, 56, 12, 8, 32};
	
	// Declare this here since the lumps path of the BSP probably will not change
	private LS ls;
	
	// CONSTRUCTORS
	// This accepts a file path and parses it into the form needed. If the folder is empty (or not found)
	// the program fails nicely.
	public Decompiler(String in) {
		try {
			window.clearConsole();
		
			filepath=in.substring(0,in.length()-4)+"\\";
			ls=new LS(in);
			ls.separateLumps();
			
			myL0 = new Lump00(filepath+"00 - Entities.txt");
			myL1 = new Lump01(filepath+"01 - Planes.hex");
			myL2 = new Lump02(filepath+"02 - Textures.hex");
			myL3 = new Lump03(filepath+"03 - Materials.hex");
			myL4 = new Lump04(filepath+"04 - Vertices.hex");
			myL9 = new Lump09(filepath+"09 - Faces.hex");
			myL11 = new Lump11(filepath+"11 - Leaves.hex");
			myL13 = new Lump13(filepath+"13 - Mark Brushes.hex");
			myL14 = new Lump14(filepath+"14 - Models.hex");
			myL15 = new Lump15(filepath+"15 - Brushes.hex");
			myL16 = new Lump16(filepath+"16 - Brushsides.hex");
			myL17 = new Lump17(filepath+"17 - Texmatrix.hex");
			
			r.gc(); // Take a minute to collect garbage, all the file parsing can leave a lot of crap data.
			
			window.println("Entities lump: "+myL0.getLength()+" bytes, "+myL0.getNumElements()+" items");
			window.println("Planes lump: "+myL1.getLength()+" bytes, "+myL1.getNumElements()+" items");
			window.println("Textures lump: "+myL2.getLength()+" bytes, "+myL2.getNumElements()+" items");
			window.println("Materials lump: "+myL3.getLength()+" bytes, "+myL3.getNumElements()+" items");
			window.println("Vertices lump: "+myL4.getLength()+" bytes, "+myL4.getNumElements()+" items");
			window.println("Faces lump: "+myL9.getLength()+" bytes, "+myL9.getNumElements()+" items");
			window.println("Leaves lump: "+myL11.getLength()+" bytes, "+myL11.getNumElements()+" items");
			window.println("Leaf brushes lump: "+myL13.getLength()+" bytes, "+myL13.getNumElements()+" items");
			window.println("Models lump: "+myL14.getLength()+" bytes, "+myL14.getNumElements()+" items");
			window.println("Brushes lump: "+myL15.getLength()+" bytes, "+myL15.getNumElements()+" items");
			window.println("Brush sides lump: "+myL16.getLength()+" bytes, "+myL16.getNumElements()+" items");
			window.println("Texture scales lump: "+myL17.getLength()+" bytes, "+myL17.getNumElements()+" items");
			
		} catch(java.lang.StringIndexOutOfBoundsException e) {
			window.println("Error: invalid path");
		}
	}
	
	// METHODS
	
	// +decompile()
	// Attempts to convert the BSP file back into a .MAP file.
	//
	// TODO: There's one extremely hard problem. Sometimes planes are resused backward. It's nearly
	// impossible to determine which way the plane needs to be flipped. Other than that, this
	// algorithm is 100% finished.
	//
	// This is another one of the most complex things I've ever had to code. I've
	// never nested for loops four deep before.
	// Iterators:
	// i: Current entity in the list
	//  j: Current leaf, referenced in a list by the model referenced by the current entity
	//   k: Current brush, referenced in a list by the current leaf.
	//    l: Current side of the current brush.
	public void decompile(String path) {
		// Begin by copying all the entities into another Lump00 object. This is
		// necessary because if I just modified the current entity list then it
		// could be saved back into the BSP and really mess some stuff up.
		Lump00 mapFile=new Lump00(myL0);
		// Then I need to go through each entity and see if it's brush-based.
		// Worldspawn is brush-based as well as any entity with model *#.
		for(int i=0;i<mapFile.getNumElements();i++) { // For each entity
			int currentModel=-1;
			if(mapFile.getEntity(i).isBrushBased()) {
				currentModel=myL0.getEntity(i).getModelNumber();
			} else {
				if(mapFile.getEntity(i).getAttribute("classname").equalsIgnoreCase("worldspawn")) {
					currentModel=0; // If the entity is worldspawn, we're dealing with model 0, which is the world.
				}
			}
			if(currentModel!=-1) { // If this is still -1 then it's strictly a point-based entity. Move on to the next one.
				double[] origin=mapFile.getEntity(i).getOrigin();
				int[] usedPlanes=new int[myL1.getNumElements()];
				Lump01 newPlanes=new Lump01(myL1);
				int firstLeaf=myL14.getModel(currentModel).getLeaf();
				int numLeaves=myL14.getModel(currentModel).getNumLeafs();
				boolean[] brushesUsed=new boolean[myL15.getNumElements()]; // Keep a list of brushes already in the model, since sometimes the leaves lump references one brush several times
				int numBrshs=0;
				for(int j=0;j<numLeaves;j++) { // For each leaf in the bunch
					int firstBrushIndex=myL11.getLeaf(j+firstLeaf).getMarkBrush();
					int numBrushIndices=myL11.getLeaf(j+firstLeaf).getNumMarkBrushes();
					if(numBrushIndices>0) { // A lot of leaves reference no brushes. If this is one, this iteration of the j loop is finished
						for(int k=0;k<numBrushIndices;k++) { // For each brush referenced
							if(!brushesUsed[myL13.getMarkBrush(firstBrushIndex+k)]) {
								brushesUsed[myL13.getMarkBrush(firstBrushIndex+k)]=true;
								Brush currentBrush=myL15.getBrush(myL13.getMarkBrush(firstBrushIndex+k)); // Get a handle to the brush
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
									BrushSide currentSide=myL16.getBrushSide(firstSide+l);
									Face currentFace=myL9.getFace(currentSide.getFace()); // To find those three points, I can use vertices referenced by faces.
									if(!myL2.getTexture(currentFace.getTexture()).equalsIgnoreCase("special/bevel")) { // If this face uses special/bevel, skip the face completely
										int firstVertex=currentFace.getVert();
										int numVertices=currentFace.getNumVerts();
										usedPlanes[currentSide.getPlane()]++;
										Plane currentPlane=newPlanes.getPlane(currentSide.getPlane());
										boolean pointsWorked=false;
										if(numVertices!=0) { // If the face actually references a set of vertices
											plane[0]=new VertexD(myL4.getVertex(firstVertex)); // Grab and store the first one
											int m=1;
											for(m=1;m<numVertices;m++) { // For each point after the first one
												plane[1]=new VertexD(myL4.getVertex(firstVertex+m));
												if(!plane[0].equals(plane[1])) { // Make sure the point isn't the same as the first one
													break; // If it isn't the same, this point is good
												}
											}
											for(m=m+1;m<numVertices;m++) { // For each point after the previous one used
												plane[2]=new VertexD(myL4.getVertex(firstVertex+m));
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
															plane[0]=new VertexD(-1, 1, -((double)currentPlane.getB()-(double)currentPlane.getDist())/(double)currentPlane.getC());
															plane[1]=new VertexD(0, 0, (double)currentPlane.getDist()/(double)currentPlane.getC());
															plane[2]=new VertexD(1, 1, -((double)currentPlane.getB()-(double)currentPlane.getDist())/(double)currentPlane.getC());
															if(currentPlane.getC()>0) {
																plane=flipPlane(plane);
															}
														} else {
															if(currentPlane.getB()==0) { // parallel to Y axis
																plane[0]=new VertexD(-((double)currentPlane.getC()-(double)currentPlane.getDist())/(double)currentPlane.getA(), -1, 1);
																plane[1]=new VertexD((double)currentPlane.getDist()/(double)currentPlane.getA(), 0, 0);
																plane[2]=new VertexD(-((double)currentPlane.getC()-(double)currentPlane.getDist())/(double)currentPlane.getA(), 1, 1);
																if(currentPlane.getA()>0) {
																	plane=flipPlane(plane);
																}
															} else {
																if(currentPlane.getC()==0) { // parallel to Z axis
																	plane[0]=new VertexD(1, -((double)currentPlane.getA()-(double)currentPlane.getDist())/(double)currentPlane.getB(), -1);
																	plane[1]=new VertexD(0, (double)currentPlane.getDist()/(double)currentPlane.getB(), 0);
																	plane[2]=new VertexD(1, -((double)currentPlane.getA()-(double)currentPlane.getDist())/(double)currentPlane.getB(), 1);
																	if(currentPlane.getB()>0) {
																		plane=flipPlane(plane);
																	}
																} else { // If you reach this point the plane is not parallel to any axis. Therefore, any two coordinates will give a third.
																	plane[0]=new VertexD(-1, 1, -(-(double)currentPlane.getA()+(double)currentPlane.getB()-(double)currentPlane.getDist())/(double)currentPlane.getC());
																	plane[1]=new VertexD(0, 0, (double)currentPlane.getDist()/(double)currentPlane.getC());
																	plane[2]=new VertexD(1, 1, -((double)currentPlane.getA()+(double)currentPlane.getB()-(double)currentPlane.getDist())/(double)currentPlane.getC());
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
										String texture=myL2.getTexture(currentFace.getTexture());
										double[] textureS=new double[3];
										double[] textureT=new double[3];
										TexMatrix currentTexMatrix=myL17.getTexMatrix(currentFace.getTexStyle());
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
										String material=myL3.getMaterial(currentFace.getMaterial());
										double lgtScale=16; // These values are impossible to get from a compiled map since they
										double lgtRot=0;    // are used by RAD for generating lightmaps, then are discarded, I believe.
										try {
											brushSides[l]=new MAPBrushSide(plane, texture, textureS, textureShiftS, textureT, textureShiftT,
										                                          texRot, texScaleS, texScaleT, flags, material, lgtScale, lgtRot);
										} catch(InvalidMAPBrushSideException e) {
											window.println("Error creating brush side "+l+" on brush "+k+" in leaf "+j+" in model "+i+", side not written.");
										}
										numRealFaces++;
									}
								}
								// FOR DETERMINING PLANE FLIP
								// To figure out the correct flip for the plane, I use two facts about BSP brushes
								// 1. They are always convex, therefore a point on one of the faces will be on the negative side of every other plane in the brush
								// 2. The positive side of every plane in a brush must face outwards, and the positive side is the side with the face
								// Limitation: There must be at least one face which was defined by vertices.
								// TODO: This doesn't fucking work. What is wrong? Probably the point used
								/*if(numVertFacesThisBrsh>0) { // If there was a face defined by vertices, if not just move on
									Vertex[] points=brushSides[vertFaceIndex].getPlane();
									// Find the point in the middle of these three points. It'll be on the plane.
									Vertex temp=new Vertex(points[0].getX()+(points[0].getX()-points[1].getX())/-2, points[0].getY()+(points[0].getY()-points[1].getY())/-2, points[0].getZ()+(points[0].getZ()-points[1].getZ())/-2);
									Vertex point=new Vertex(temp.getX()+(temp.getX()-points[2].getX())/-2, temp.getY()+(temp.getY()-points[2].getY())/-2, temp.getZ()+(temp.getZ()-points[2].getZ())/-2);
									for(int l=0;l<numSides;l++) { // For each side, AFTER the entire MAPBrushSide list has been populated
										if(!vertFaces[l] && l!=vertFaceIndex) { // If the current face was not defined by vertices
											BrushSide currentSide=myL16.getBrushSide(firstSide+l);
											Face currentFace=myL9.getFace(currentSide.getFace());
											if(!myL2.getTexture(currentFace.getTexture()).equalsIgnoreCase("special/bevel")) { // If this face uses special/bevel, skip the face completely
												Plane currentPlane=newPlanes.getPlane(currentSide.getPlane());
												// Formula for the signed distance from a plane to a point, I hope to jesus this works
												// Source: http://mathworld.wolfram.com/Point-PlaneDistance.html
												double signedDist=(currentPlane.getA()*point.getX()+currentPlane.getB()*point.getY()+currentPlane.getC()*point.getZ()-currentPlane.getDist())/(Math.sqrt(Math.pow(currentPlane.getA(),2)+Math.pow(currentPlane.getB(),2)+Math.pow(currentPlane.getC(),2)));
												if(signedDist>0) { // This > may need to be flipped, I don't know yet
													brushSides[l].flipPlane();
												}
											}
										}
									}
								}*/
								
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
							window.println("Bad origin brush, there's something wrong with the code.");
						}
					}
					newOriginBrush.addAttribute("}");
					mapFile.getEntity(i).addAttribute(newOriginBrush.toString());
				}
				mapFile.getEntity(i).deleteAttribute("origin");
			}
		}
		window.println("Saving "+path+"...");
		mapFile.save(path);
		window.println("Deleting lump files");
		ls.deleteLumps();
		window.println("Process completed!");
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
	
	// ACCESSORS/MUTATORS
	
	public Lump00 getLump00() {
		return myL0;
	}
	
	public Lump01 getLump01() {
		return myL1;
	}
	
	public Lump02 getLump02() {
		return myL2;
	}
	
	public Lump03 getLump03() {
		return myL3;
	}
	
	public Lump04 getLump04() {
		return myL4;
	}
	
	public Lump09 getLump09() {
		return myL9;
	}
	
	public Lump11 getLump11() {
		return myL11;
	}
	
	public Lump13 getLump13() {
		return myL13;
	}
	
	public Lump14 getLump14() {
		return myL14;
	}
	
	public Lump15 getLump15() {
		return myL15;
	}
	
	public Lump16 getLump16() {
		return myL16;
	}
	
	public Lump17 getLump17() {
		return myL17;
	}
}
